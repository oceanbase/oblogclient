/*
 * Copyright 2024 OceanBase.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oceanbase.clogproxy.client.connection;


import com.google.protobuf.InvalidProtocolBufferException;
import com.oceanbase.clogproxy.client.config.ClientConf;
import com.oceanbase.clogproxy.client.enums.ErrorCode;
import com.oceanbase.clogproxy.client.exception.LogProxyClientException;
import com.oceanbase.clogproxy.common.packet.CompressType;
import com.oceanbase.clogproxy.common.packet.HeaderType;
import com.oceanbase.clogproxy.common.packet.ProtocolVersion;
import com.oceanbase.clogproxy.common.packet.protocol.LogProxyProto;
import com.oceanbase.clogproxy.common.packet.protocol.V1Proto;
import com.oceanbase.clogproxy.common.util.NetworkUtil;
import com.oceanbase.oms.logmessage.LogMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import java.util.concurrent.BlockingQueue;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.apache.commons.lang3.Conversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Compatible for legacy V0 and V1 only, however you should not use */
public class ClientHandlerV01 {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandlerV01.class);

    private static final byte[] MAGIC_STRING = new byte[] {'x', 'i', '5', '3', 'g', ']', 'q'};

    private static final String CLIENT_IP = NetworkUtil.getLocalIp();

    private final ClientConf config;
    private ConnectionParams params;
    private final BlockingQueue<StreamContext.TransferPacket> recordQueue;

    private final LZ4FastDecompressor fastDecompressor;

    enum HandshakeState {
        /** State of protocol version */
        PROTOCOL_VERSION,
        /** State of header code */
        HEADER_CODE,
        /** State of handshake response code */
        RESPONSE_CODE,
        /** State of error message */
        MESSAGE,
        /** State of handsake response version */
        LOGPROXY_IP,
        /** State of handsake response version */
        LOGPROXY_VERSION,
        /** State of data record */
        STREAM
    }

    private HandshakeState state = HandshakeState.PROTOCOL_VERSION;

    private String logProxyIp;

    public ClientHandlerV01(
            ClientConf config,
            ConnectionParams params,
            BlockingQueue<StreamContext.TransferPacket> recordQueue,
            LZ4FastDecompressor fastDecompressor) {
        this.config = config;
        this.params = params;
        this.recordQueue = recordQueue;
        this.fastDecompressor = fastDecompressor;
    }

    public void setParams(ConnectionParams params) {
        this.params = params;
    }

    public boolean channelRead(boolean poolflag, ByteBuf buffer, boolean inDataNotEnough)
            throws Exception {
        boolean dataNotEnough = inDataNotEnough;

        switch (state) {
            case PROTOCOL_VERSION:
                if (buffer.readableBytes() >= Short.BYTES) {
                    int code = buffer.readShort();
                    ProtocolVersion version = ProtocolVersion.codeOf(code);
                    if (version == null) {
                        resetState();
                        logger.error("unsupport protocol version: {}", code);
                        throw new LogProxyClientException(
                                ErrorCode.E_PROTOCOL, "unsupport protocol version: " + code);
                    }
                    state = HandshakeState.HEADER_CODE;
                } else {
                    dataNotEnough = true;
                }
                break;
            case HEADER_CODE:
                if (buffer.readableBytes() >= Integer.BYTES) {
                    int code = buffer.readInt();

                    if ((code != HeaderType.HANDSHAKE_RESPONSE_CLIENT.code())
                            && (code != HeaderType.ERROR_RESPONSE.code())) {
                        resetState();
                        logger.error(
                                "unexpected Header Type, expected: {}({}), income: {}",
                                HeaderType.HANDSHAKE_RESPONSE_CLIENT.code(),
                                HeaderType.HANDSHAKE_RESPONSE_CLIENT.name(),
                                code);
                        throw new LogProxyClientException(
                                ErrorCode.E_HEADER_TYPE, "unexpected Header Type: " + code);
                    }
                    state = HandshakeState.RESPONSE_CODE;
                } else {
                    dataNotEnough = true;
                }
                break;
            case RESPONSE_CODE:
                if (buffer.readableBytes() >= 4) {
                    int code = buffer.readInt();
                    if (code != 0) {
                        state = HandshakeState.MESSAGE;
                    } else {
                        state = HandshakeState.LOGPROXY_IP;
                    }
                } else {
                    dataNotEnough = true;
                }
                break;
            case MESSAGE:
                String message = decodeStringInt(buffer);
                if (message != null) {
                    resetState();
                    logger.error("LogProxy refused handshake request: {}", message);
                    throw new LogProxyClientException(
                            ErrorCode.NO_AUTH,
                            "LogProxy refused handshake request: " + message,
                            true);
                } else {
                    dataNotEnough = true;
                }
                break;
            case LOGPROXY_IP:
                logProxyIp = decodeStringByte(buffer);
                if (logProxyIp != null) {
                    state = HandshakeState.LOGPROXY_VERSION;
                } else {
                    dataNotEnough = true;
                }
                break;

            case LOGPROXY_VERSION:
                String logProxyVersion = decodeStringByte(buffer);
                if (logProxyVersion != null) {
                    logger.info("Connected to LogProxy: {}, {}", logProxyIp, logProxyVersion);
                    state = HandshakeState.STREAM;
                } else {
                    dataNotEnough = true;
                }
                break;
            case STREAM:
                parseData(poolflag, buffer);
                dataNotEnough = true;
                break;
        }
        return dataNotEnough;
    }

    private static String decodeStringInt(ByteBuf buffer) {
        if (buffer.readableBytes() < Integer.BYTES) {
            return null;
        }
        buffer.markReaderIndex();
        int length = buffer.readInt();
        if (buffer.readableBytes() < length) {
            buffer.resetReaderIndex();
            return null;
        }
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        String str = new String(bytes);
        if (str.isEmpty()) {
            throw new RuntimeException("decode string is null or empty");
        }
        return str;
    }

    private static String decodeStringByte(ByteBuf buffer) {
        if (buffer.readableBytes() < Byte.BYTES) {
            return null;
        }
        buffer.markReaderIndex();
        short length = buffer.readByte();
        if (buffer.readableBytes() < length) {
            buffer.resetReaderIndex();
            return null;
        }
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        String str = new String(bytes);
        if (str.isEmpty()) {
            throw new RuntimeException("decode string is null or empty");
        }
        return str;
    }

    private void parseData(boolean poolflag, ByteBuf buffer) throws LogProxyClientException {
        // TODO... parse data exception handle
        while (poolflag && buffer.readableBytes() >= 2) {
            buffer.markReaderIndex();

            int code = buffer.readShort();
            ProtocolVersion version = ProtocolVersion.codeOf(code);
            if (version == null) {
                resetState();
                logger.error("unsupport protocol version: {}", code);
                throw new LogProxyClientException(
                        ErrorCode.E_PROTOCOL, "unsupport protocol version: " + code);
            }
            boolean go;
            switch (version) {
                case V1:
                    go = parseDataV1(buffer);
                    break;
                case V0:
                default:
                    go = parseDataV0(buffer);
            }

            if (!go) {
                break;
            }
        }
    }

    private boolean parseDataV0(ByteBuf buffer) {
        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return false;
        }
        int code = buffer.readInt();
        if (code != HeaderType.DATA_CLIENT.code()) {
            resetState();
            logger.error(
                    "unexpected Header Type, expected: {}({}), income: {}",
                    HeaderType.DATA_CLIENT.code(),
                    HeaderType.DATA_CLIENT.name(),
                    code);
            throw new LogProxyClientException(
                    ErrorCode.E_HEADER_TYPE, "unexpected Header Type: " + code);
        }

        int dataLength = buffer.readInt();
        if (buffer.readableBytes() < dataLength) {
            buffer.resetReaderIndex();
            return false;
        }

        code = buffer.readByte();
        if (CompressType.codeOf(code) == null) {
            throw new LogProxyClientException(
                    ErrorCode.E_COMPRESS_TYPE, "unexpected Compress Type: " + code);
        }

        int totalLength = buffer.readInt();
        int rawDataLength = buffer.readInt();
        byte[] rawData = new byte[rawDataLength];
        buffer.readBytes(rawData);
        if (code == CompressType.LZ4.code()) {
            byte[] bytes = new byte[totalLength];
            int decompress = fastDecompressor.decompress(rawData, 0, bytes, 0, totalLength);
            if (decompress != rawDataLength) {
                throw new LogProxyClientException(
                        ErrorCode.E_LEN,
                        "decompressed length ["
                                + decompress
                                + "] is not expected ["
                                + rawDataLength
                                + "]");
            }
            parseRecord(bytes);
        } else {
            parseRecord(rawData);
        }
        // complete
        return true;
    }

    /**
     * Do parse record data from an array of bytes to a {@link LogMessage} and add it into {@link
     * #recordQueue}.
     *
     * @param bytes An array of bytes of record data.
     * @throws LogProxyClientException If exception occurs.
     */
    private void parseRecord(byte[] bytes) throws LogProxyClientException {
        int offset = 0;
        while (offset < bytes.length) {
            int dataLength = Conversion.byteArrayToInt(bytes, offset + 4, 0, 0, 4);
            dataLength = ByteBufUtil.swapInt(dataLength);

            /*
             * We must copy a byte array and call parse after then,
             * or got a !!!RIDICULOUS EXCEPTION!!!,
             * if we wrap an unpooled buffer with offset and call setByteBuf just as same as `parse` function do.
             */
            LogMessage logMessage = new LogMessage(false);
            byte[] data = new byte[dataLength];
            System.arraycopy(bytes, offset + 8, data, 0, data.length);

            try {
                logMessage.parse(data);
            } catch (Exception e) {
                if (config.isIgnoreUnknownRecordType()) {
                    // unsupported type, ignore
                    logger.debug("Unsupported record type: {}", logMessage);
                    offset += (8 + dataLength);
                    continue;
                }
                throw new LogProxyClientException(ErrorCode.E_PARSE, e);
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Log message: {}", logMessage);
            }

            while (true) {
                try {
                    recordQueue.put(new StreamContext.TransferPacket(logMessage));
                    break;
                } catch (InterruptedException e) {
                    // do nothing
                }
            }

            offset += (8 + dataLength);
        }
    }

    private boolean parseDataV1(ByteBuf buffer) {
        if (buffer.readableBytes() < 4) {
            buffer.resetReaderIndex();
            return false;
        }
        int length = buffer.readInt();
        if (buffer.readableBytes() < length) {
            buffer.resetReaderIndex();
            return false;
        }
        byte[] buff = new byte[length];
        buffer.readBytes(buff, 0, length);
        try {
            V1Proto.PbPacket packet = V1Proto.PbPacket.parseFrom(buff);

            if (packet.getCompressType() != CompressType.NONE.code()) {
                // TODO..
                throw new LogProxyClientException(
                        ErrorCode.E_COMPRESS_TYPE,
                        "Unsupport Compress Type: " + packet.getCompressType());
            }
            if (packet.getType() != HeaderType.STATUS.code()) {
                // TODO.. header type dispatcher
                throw new LogProxyClientException(
                        ErrorCode.E_HEADER_TYPE, "Unsupport Header Type: " + packet.getType());
            }
            LogProxyProto.RuntimeStatus status =
                    LogProxyProto.RuntimeStatus.parseFrom(packet.getPayload());
            if (status == null) {
                throw new LogProxyClientException(
                        ErrorCode.E_PARSE, "Failed to read PB packet, empty Runtime Status");
            }

            while (true) {
                try {
                    recordQueue.put(new StreamContext.TransferPacket(status));
                    break;
                } catch (InterruptedException e) {
                    // do nothing
                }
            }

        } catch (InvalidProtocolBufferException e) {
            throw new LogProxyClientException(ErrorCode.E_PARSE, "Failed to read PB packet", e);
        }
        return true;
    }

    public void resetState() {
        state = HandshakeState.PROTOCOL_VERSION;
    }

    public ByteBuf generateConnectRequest() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(MAGIC_STRING.length);
        byteBuf.writeBytes(MAGIC_STRING);

        // header
        byteBuf.capacity(byteBuf.capacity() + 2 + 4 + 1);
        byteBuf.writeShort(ProtocolVersion.V0.code());
        byteBuf.writeInt(HeaderType.HANDSHAKE_REQUEST_CLIENT.code());
        byteBuf.writeByte(params.getLogType().code());

        // body
        int length = CLIENT_IP.length();
        byteBuf.capacity(byteBuf.capacity() + length + 4);
        byteBuf.writeInt(length);
        byteBuf.writeBytes(CLIENT_IP.getBytes());

        length = params.getClientId().length();
        byteBuf.capacity(byteBuf.capacity() + length + 4);
        byteBuf.writeInt(length);
        byteBuf.writeBytes(params.getClientId().getBytes());

        length = ClientConf.VERSION.length();
        byteBuf.capacity(byteBuf.capacity() + length + 4);
        byteBuf.writeInt(length);
        byteBuf.writeBytes(ClientConf.VERSION.getBytes());

        length = params.getConfigurationString().length();
        byteBuf.capacity(byteBuf.capacity() + length + 4);
        byteBuf.writeInt(length);
        byteBuf.writeBytes(params.getConfigurationString().getBytes());

        return byteBuf;
    }
}
