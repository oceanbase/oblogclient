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
