package com.hanvon.virtualpage.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * @Description: 所有需要处理的图片统一管理（已废弃：原本计划手动管理所有图片资源，后来使用Glide框架来替换这种方式）
 * @Author: TaoZhi
 * @Date: 2016/5/12
 * @E_mail: taozhi@hanwang.com.cn
 */
public class LruCacheUtils {

    private LruCache<String, Bitmap> mMemoryCache;
    private static LruCacheUtils sInstance;

    private LruCacheUtils() {
        // 获取到可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常。
        // LruCache通过构造函数传入缓存值，以KB为单位。
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // 使用最大可用内存值的1/8作为缓存的大小。
        int cacheSize = maxMemory / 8; // 这里是传递给LruCache构造函数的一个参数，最大可用的缓存大小
//        LogUtil.d("LruCache", "最大可用缓存大小为：" + cacheSize + "KB");
//        LogUtil.d("LruCache", "************************************");
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 重写此方法来衡量每张图片的大小，默认返回图片数量。

                int size = bitmap.getByteCount() / 1024;
//                LogUtil.d("LruCache", "添加了一张图片大小为：" + size + "KB");
//                LogUtil.d("LruCache", "============================");
                return size;
            }

//            @Override
//            protected Bitmap create(String key) {
//                LogUtil.d("LruCache", "create() called with: " + "key = [" + key + "]");
//                return BitmapUtil.generateThumbnailByPath(key, 379, 640);
////                return super.create(key);
//            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
//                LogUtil.d("LruCache", "entryRemoved() called with: " + "evicted = [" + evicted + "], key = [" + key + "], oldValue = [" + oldValue + "], newValue = [" + newValue + "]");
                // evicted: true-->表示是为了释放控件导致的记录被移除
                //          false-->表示是由于put或者remove操作触发
                // newValue： non-null-->由put操作触发
                //            null-->由remove操作触发
                if (oldValue != null) {
                    if (!oldValue.isRecycled()) {
//                        LogUtil.d("LruCache","释放了" + key);
                        oldValue.recycle();
                    }
                }
            }
        };
    }

    public synchronized void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key != null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
//            if (getBitmapFromMemCache(key) == null) {
//                LogUtil.i("bmp", "======>添加了一张：" + key);
//            } else {
//                LogUtil.i("bmp", "======>更新了一张：" + key);
//                mMemoryCache.remove(key);
//                mMemoryCache.put(key, bitmap);
//            }
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
//        LogUtil.d("bmp", "------>从缓存中取:" + key);
        if (key == null) {
//            LogUtil.e("bmp", "------>Key=null，传个空值过来是几个意思？");
            return null;
        }
        return mMemoryCache.get(key);
    }


    public static LruCacheUtils getInstance() {
        if (null == sInstance) {
            synchronized (LruCacheUtils.class) {
                if (null == sInstance) {
                    sInstance = new LruCacheUtils();
                }
            }
        }
        return sInstance;
    }

}
