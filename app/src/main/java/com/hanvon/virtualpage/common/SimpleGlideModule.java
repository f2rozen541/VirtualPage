package com.hanvon.virtualpage.common;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.GlideModule;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/7/21
 * E_mail:  taozhi@hanwang.com.cn
 */
public class SimpleGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int memoryCacheSize = maxMemory / 8;
        builder.setMemoryCache(new LruResourceCache(memoryCacheSize));

//        File cacheDir = context.getExternalCacheDir(); // 指定的是数据的缓存地址
//        int diskCacheSize = 1024 *1024 * 30; // 最多可以缓存多少字节的数据
//        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, diskCacheSize));


    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
