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

public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private final AtomicInteger        threadNumber = new AtomicInteger(1);
    private final ThreadGroup          group;
    private final String               namePrefix;
    private final boolean              isDaemon;

    public NamedThreadFactory() {
        this("ThreadPool");
    }

    public NamedThreadFactory(String name) {
        this(name, false);
    }

    public NamedThreadFactory(String prefix, boolean daemon) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = prefix + "-" + POOL_NUMBER.getAndIncrement() + "-thread-";
        isDaemon = daemon;
    }

    /**
     * Create a thread.
     *
     * @see ThreadFactory#newThread(Runnable)
     */
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
