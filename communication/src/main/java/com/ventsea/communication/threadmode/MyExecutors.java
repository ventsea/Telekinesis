package com.ventsea.communication.threadmode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyExecutors {

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

//    private static ExecutorService executor = Executors.newCachedThreadPool();

    public static ExecutorService newFixedThreadPool() {
        return executor;
    }
}
