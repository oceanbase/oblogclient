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

import com.oceanbase.clogproxy.client.config.ClientConf;
import com.oceanbase.clogproxy.client.enums.ErrorCode;
import com.oceanbase.clogproxy.client.exception.LogProxyClientException;
import com.oceanbase.clogproxy.client.listener.RecordListener;
import com.oceanbase.clogproxy.client.listener.StatusListener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final StreamContext context;

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

    /** Close the connection and wait the process thread. */
    public void stop() {
        if (started.compareAndSet(true, false)) {
            logger.info("Try to stop this client");

            if (connection != null) {
                connection.close();
                connection = null;
            }

            join();
            thread = null;
            logger.info("Client stopped successfully");
        }
    }

    /** Call {@link Thread#join()} method of process thread. */
    public void join() {
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.warn("Waits for process thread failed : {}", e.getMessage());
                triggerStop();
            }
        }
    }

    /** Call {@link #stop()} asynchronously. */
    public void triggerStop() {
        new Thread(this::stop).start();
    }

    /**
     * Call {@link RecordListener#onException(LogProxyClientException)} synchronously.
     *
     * @param e An exception.
     */
    public void triggerException(LogProxyClientException e) {
        for (RecordListener listener : listeners) {
            try {
                listener.onException(e);
            } catch (Throwable throwable) {
                logger.error("Failed to notify listener on exception", throwable);
            }
        }
    }

    /** Start the process thread. */
    public void start() {
        // if status listener exist, enable monitor
        context.params().setEnableMonitor(!statusListeners.isEmpty());
        retryTimes = 0;

        if (started.compareAndSet(false, true)) {
            thread =
                    new Thread(
                            () -> {
                                while (isRunning()) {
                                    ReconnectState state = reconnect();
                                    if (state == ReconnectState.EXIT) {
                                        triggerException(
                                                new LogProxyClientException(
                                                        ErrorCode.E_MAX_RECONNECT,
                                                        "Exceed max retry times",
                                                        true));
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

                                    StreamContext.TransferPacket packet = null;
                                    while (isRunning()) {
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

                                        try {
                                            setCheckpointString(
                                                    packet.getRecord().getSafeTimestamp());
                                        } catch (IllegalArgumentException e) {
                                            logger.error(
                                                    "Failed to update checkpoint for log message: "
                                                            + packet.getRecord(),
                                                    e);
                                            throw new LogProxyClientException(
                                                    ErrorCode.E_INNER,
                                                    "Failed to update checkpoint");
                                        }

                                    } catch (LogProxyClientException e) {
                                        triggerException(e);
                                        break;
                                    } catch (Exception e) {
                                        triggerException(
                                                new LogProxyClientException(ErrorCode.E_USER, e));
                                        break;
                                    }
                                }

                                triggerStop();
                                logger.info("Client process thread exit");
                            });

            thread.setDaemon(false);
            thread.start();
        }
        // add a shutdown hook to trigger the stop the process
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
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
            logger.info("Try to connect");

            try {
                if (context.config().getMaxReconnectTimes() != -1
                        && retryTimes > context.config().getMaxReconnectTimes()) {
                    logger.error(
                            "Failed to connect, exceed max reconnect retry time: {}",
                            context.config().getMaxReconnectTimes());
                    return ReconnectState.EXIT;
                }

                if (connection != null) {
                    connection.close();
                    connection = null;
                }
                // when stopped, context.recordQueue may not empty, just use checkpointString to do
                // reconnection.
                if (StringUtils.isNotEmpty(checkpointString)) {
                    logger.warn("update checkpoint: {}", checkpointString);
                    context.params().updateCheckpoint(checkpointString);
                }

                connection = ConnectionFactory.instance().createConnection(context);
                if (connection != null) {
                    logger.info("Connect successfully");
                    return ReconnectState.SUCCESS;
                }

                logger.error(
                        "Failed to connect, retry count: {}, max: {}",
                        retryTimes,
                        context.config().getMaxReconnectTimes());
                // not success, retry next time
                reconnect.set(true);
                return ReconnectState.RETRY;
            } catch (Exception e) {
                logger.error(
                        "Failed to connect, retry count: {}, max: {}, message: {}",
                        retryTimes,
                        context.config().getMaxReconnectTimes(),
                        e.getMessage());
                // not success, retry next time
                reconnect.set(true);
                return ReconnectState.RETRY;
            } finally {
                reconnecting.set(false);
                retryTimes++;
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
     * Set checkpoint string.
     *
     * @param checkpointString Checkpoint string.
     */
    private void setCheckpointString(String checkpointString) {
        long timestamp = Long.parseLong(checkpointString);
        if (timestamp <= 0) {
            throw new IllegalArgumentException(
                    "Update checkpoint with invalid value: " + timestamp);
        }
        if (this.checkpointString == null || Long.parseLong(this.checkpointString) < timestamp) {
            this.checkpointString = checkpointString;
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
