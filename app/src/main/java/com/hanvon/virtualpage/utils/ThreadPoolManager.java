package com.hanvon.virtualpage.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description: 已废弃
 * @Author: TaoZhi
 * @Date: 2016/5/11
 * @E_mail: taozhi@hanwang.com.cn
 */
public class ThreadPoolManager {
    private ExecutorService service;

    private ThreadPoolManager() {
        int num = Runtime.getRuntime().availableProcessors();
        service = Executors.newFixedThreadPool(num * 2);
    }

    private static ThreadPoolManager manager;

    public static ThreadPoolManager getInstance() {
        if (manager == null) {
            manager = new ThreadPoolManager();
        }
        return manager;
    }

    public void addTask(Runnable runnable) {
        service.submit(runnable);
    }

}
