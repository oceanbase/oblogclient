/* Copyright (c) 2021 OceanBase and/or its affiliates. All rights reserved.
oblogclient is licensed under Mulan PSL v2.
You can use this software according to the terms and conditions of the Mulan PSL v2.
You may obtain a copy of Mulan PSL v2 at:
         http://license.coscl.org.cn/MulanPSL2
THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
See the Mulan PSL v2 for more details. */

package com.oceanbase.clogproxy.client.connection;


import com.oceanbase.clogproxy.client.config.ClientConf;
import com.oceanbase.clogproxy.client.enums.ErrorCode;
import com.oceanbase.clogproxy.client.exception.LogProxyClientException;
import com.oceanbase.clogproxy.client.listener.RecordListener;
import com.oceanbase.clogproxy.client.listener.StatusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a stream of log client. Stream means a channel of log transmission here.
 */
public class ClientStream {
    private static final Logger logger = LoggerFactory.getLogger(ClientStream.class);

    /** Flag of whether the stream is started. */
    private final AtomicBoolean started = new AtomicBoolean(false);
    /** The process thread. */
    private Thread thread = null;

    /** Context of stream. */
    private StreamContext context;

    /** Checkpoint string used to resume writing into the queue. */
    private String checkpointString;

    /** Number of reconnections */
    private int retryTimes = 0;

    /** Connection to log proxy with netty channel. */
    private Connection connection = null;

    /** Flag of whether the stream is reconnecting now. */
    private final AtomicBoolean reconnecting = new AtomicBoolean(true);

    /** Flag of whether the stream need reconnect. */
    private final AtomicBoolean reconnect = new AtomicBoolean(true);

    /** The list of {@link RecordListener}. */
    private final List<RecordListener> listeners = new ArrayList<>();

    /** The list of {@link StatusListener} */
    private final List<StatusListener> statusListeners = new ArrayList<>();

    /** Reconnect state type enumeration. */
    private enum ReconnectState {
        /** success */
        SUCCESS,
        /** retry connect next round */
        RETRY,
        /** failed, exit thread */
        EXIT;
    }

    /**
     * Sole constructor.
     *
     * @param connectionParams Connection params.
     * @param clientConf Client config.
     */
    public ClientStream(ClientConf clientConf, ConnectionParams connectionParams) {
        this.context = new StreamContext(this, clientConf, connectionParams);
    }

    /** Close and wait the connection. */
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            logger.info("stopping LogProxy Client....");

            if (connection != null) {
                connection.close();
                connection = null;
            }

            join();
            thread = null;
        }
        logger.info("stopped LogProxy Client");
    }

    /** Call {@link Thread#join()} method of process thread. */
    public void join() {
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.warn("ClientStream thread is interrupted: " + e.getMessage());
                stop();
            }
        }
    }

    /** Call {@link #stop()} asynchronously. */
    public void triggerStop() {
        new Thread(this::stop).start();
    }

    /**
     * Call {@link RecordListener#onException(LogProxyClientException)} asynchronously.
     *
     * @param e An exception.
     */
    public void triggerException(LogProxyClientException e) {
        // use thread make sure non-blocking
        new Thread(
                        () -> {
                            for (RecordListener listener : listeners) {
                                listener.onException(e);
                            }
                        })
                .start();
    }

    /** Start the process thread. */
    public void start() {
        // if status listener exist, enable monitor
        context.params().setEnableMonitor(!statusListeners.isEmpty());

        if (started.compareAndSet(false, true)) {
            thread =
                    new Thread(
                            () -> {
                                while (isRunning()) {
                                    ReconnectState state = reconnect();
                                    if (state == ReconnectState.EXIT) {
                                        logger.error("read thread to exit");
                                        triggerException(
                                                new LogProxyClientException(
                                                        ErrorCode.E_MAX_RECONNECT,
                                                        "exceed max reconnect retry"));
                                        break;
                                    }
                                    if (state == ReconnectState.RETRY) {
                                        try {
                                            TimeUnit.SECONDS.sleep(
                                                    context.config().getRetryIntervalS());
                                        } catch (InterruptedException e) {
                                            // do nothing
                                        }
                                        continue;
                                    }

                                    StreamContext.TransferPacket packet;
                                    while (true) {
                                        try {
                                            packet =
                                                    context.recordQueue()
                                                            .poll(
                                                                    context.config()
                                                                            .getReadWaitTimeMs(),
                                                                    TimeUnit.MILLISECONDS);
                                            break;
                                        } catch (InterruptedException e) {
                                            // do nothing
                                        }
                                    }
                                    if (packet == null) {
                                        continue;
                                    }
                                    try {
                                        switch (packet.getType()) {
                                            case DATA_CLIENT:
                                                for (RecordListener listener : listeners) {
                                                    listener.notify(packet.getRecord());
                                                }
                                                break;
                                            case STATUS:
                                                for (StatusListener listener : statusListeners) {
                                                    listener.notify(packet.getStatus());
                                                }
                                                break;
                                            default:
                                                throw new LogProxyClientException(
                                                        ErrorCode.E_PROTOCOL,
                                                        "Unsupported Packet Type: "
                                                                + packet.getType());
                                        }
                                    } catch (LogProxyClientException e) {
                                        triggerStop();
                                        triggerException(e);
                                        return;

                                    } catch (Exception e) {
                                        // if exception occurred, we exit
                                        triggerStop();
                                        triggerException(
                                                new LogProxyClientException(ErrorCode.E_USER, e));
                                        return;
                                    }
                                }

                                started.set(false);
                                if (connection != null) {
                                    connection.close();
                                }
                                thread = null;

                                // TODO... if exception occurred, run handler callback

                                logger.warn("!!! read thread exit !!!");
                            });

            thread.setDaemon(false);
            thread.start();
        }
    }

    /**
     * Get the flag of whether the stream is started.
     *
     * @return The flag of whether the stream is started.
     */
    public boolean isRunning() {
        return started.get();
    }

    /**
     * Reconnect to log proxy. It is also used for first time connecting.
     *
     * @return A {@link ReconnectState}.
     */
    private ReconnectState reconnect() {
        // reconnect flag mark, tiny load for checking
        if (reconnect.compareAndSet(true, false)) {
            logger.warn("start to reconnect...");

            try {
                if (context.config().getMaxReconnectTimes() != -1
                        && retryTimes >= context.config().getMaxReconnectTimes()) {
                    logger.error(
                            "failed to reconnect, exceed max reconnect retry time: {}",
                            context.config().getMaxReconnectTimes());
                    reconnect.set(true);
                    return ReconnectState.EXIT;
                }

                if (connection != null) {
                    connection.close();
                    connection = null;
                }
                // when stopped, context.recordQueue may not empty, just use checkpointString to do
                // reconnection.
                if (StringUtils.isNotEmpty(checkpointString)) {
                    logger.warn("reconnect set checkpoint: {}", checkpointString);
                    context.params().updateCheckpoint(checkpointString);
                }
                connection = ConnectionFactory.instance().createConnection(context);
                if (connection != null) {
                    logger.warn("reconnect SUCC");
                    retryTimes = 0;
                    reconnect.compareAndSet(true, false);
                    return ReconnectState.SUCCESS;
                }

                logger.error(
                        "failed to reconnect, retry count: {}, max: {}",
                        ++retryTimes,
                        context.config().getMaxReconnectTimes());
                // not success, retry next time
                reconnect.set(true);
                return ReconnectState.RETRY;

            } catch (Exception e) {
                logger.error(
                        "failed to reconnect, retry count: {}, max: {}, message: {}",
                        ++retryTimes,
                        context.config().getMaxReconnectTimes(),
                        e);
                // not success, retry next time
                reconnect.set(true);
                return ReconnectState.RETRY;

            } finally {
                reconnecting.set(false);
            }
        }
        return ReconnectState.SUCCESS;
    }

    /** Reset the flags for reconnection. */
    public void triggerReconnect() {
        // reconnection action guard, avoid concurrent or multiple invoke
        if (reconnecting.compareAndSet(false, true)) {
            reconnect.compareAndSet(false, true);
        }
    }

    /**
     * Add a {@link RecordListener} to {@link #listeners}.
     *
     * @param recordListener A {@link RecordListener}.
     */
    public synchronized void addListener(RecordListener recordListener) {
        listeners.add(recordListener);
    }

    /**
     * Add a {@link StatusListener} to {@link #statusListeners}.
     *
     * @param statusListener A {@link StatusListener}.
     */
    public synchronized void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }
}
