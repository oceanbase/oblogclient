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

package com.oceanbase.clogproxy.client.util;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** This is a factory class for {@link ThreadFactory}. */
public class NamedThreadFactory implements ThreadFactory {

    /** Pool number. */
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    /** Thread number. */
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    /** Thread group. */
    private final ThreadGroup group;
    /** Prefix of thread name. */
    private final String namePrefix;
    /** Flag of whether the thread is daemon. */
    private final boolean isDaemon;

    /** Constructor with no arguments. It will take "ThreadPool" as its name. */
    public NamedThreadFactory() {
        this("ThreadPool");
    }

    /**
     * Constructor with name.
     *
     * @param name Name of thread factory.
     */
    public NamedThreadFactory(String name) {
        this(name, false);
    }

    /**
     * Constructor with name prefix and daemon flag.
     *
     * @param prefix Name prefix of thread factory.
     * @param daemon A flag of whether starting the thread on daemon mode.
     */
    public NamedThreadFactory(String prefix, boolean daemon) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = prefix + "-" + POOL_NUMBER.getAndIncrement() + "-thread-";
        isDaemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        t.setDaemon(isDaemon);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
