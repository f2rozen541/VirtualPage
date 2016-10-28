package com.hanvon.virtualpage.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;

import java.io.ByteArrayOutputStream;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  hll
 * Date:    2016/2/24
 */
public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    public static Bitmap decodeResourceBySize(Resources res, int id, int width,
                                              int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, id, options);
        int sampleWidth = options.outWidth / width;
        int sampleHeight = options.outHeight / height;

        options.inJustDecodeBounds = false;
        options.inSampleSize = (sampleWidth > sampleHeight ? sampleHeight
                : sampleWidth);
        Bitmap bmp = BitmapFactory.decodeResource(res, id, options);
        return bmp;
    }

    public static Bitmap decodeFileBySize(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 1;

        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize = (outWidth / width + outHeight / height) / 2;
            options.inSampleSize = sampleSize;
        } else {
            options.inSampleSize = 4;
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap decodeFileThumbnail(String path, int width, int height) {
        Bitmap bmp = BitmapFactory.decodeFile(path);
        bmp = ThumbnailUtils.extractThumbnail(bmp, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bmp;
    }

    public static void drawableToBitmap(Drawable drawable, Bitmap bitmap, int width, int height) {
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap generateThumbnailByPath(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 只读边界
        BitmapFactory.decodeFile(path, options); // 解析尺寸
        options.inJustDecodeBounds = false;
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;

        int scale = 1;
        if (outWidth > outHeight && outWidth > width) { //如果宽度大的话根据宽度固定大小缩放
            scale = outWidth / width;
        } else if (outWidth < outHeight && outHeight > height) { //如果高度高的话根据宽度固定大小缩放
            scale = outHeight / height;
        }
        if (scale < 1) {
            scale = 1;
        }
        options.inSampleSize = scale; // 设置缩放比例
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * @description 计算图片的压缩比率
     *
     * @param options 参数
     * @param reqWidth 目标的宽度
     * @param reqHeight 目标的高度
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    /**
     * @description 通过传入的bitmap，进行压缩，得到符合标准的bitmap
     *
     * @param src
     * @param dstWidth
     * @param dstHeight
     * @return
     */
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight, int inSampleSize) {
        //如果inSampleSize是2的倍数，也就说这个src已经是我们想要的缩略图了，直接返回即可。
        if (inSampleSize % 2 == 0) {
            return src;
        }
        // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响，我们这里是缩小图片，所以直接设置为false
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }
    /**
     * @description 从Resources中加载图片
     *
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 设置成了true,不占用内存，只获取bitmap宽高
        BitmapFactory.decodeResource(res, resId, options); // 读取图片长款
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight); // 调用上面定义的方法计算inSampleSize值
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, options); // 载入一个稍大的缩略图
        return createScaleBitmap(src, reqWidth, reqHeight, options.inSampleSize); // 进一步得到目标大小的缩略图
    }
    /**
     * @description 从SD卡上加载图片
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight, options.inSampleSize);
    }

    /**
     *
     * @param bitmap
     * @param degrees
     * @return
     */
    public static Bitmap adjustPhotoRotation(Bitmap bitmap, int degrees) {

        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap bitmap1 = Bitmap.createBitmap(
                        bitmap,
                        0, 0,
                        bitmap.getWidth(), bitmap.getHeight(),
                        m,
                        true);
                if (bitmap != bitmap1) {
                    bitmap.recycle();  //Android开发网再次提示Bitmap操作完应该显示的释放
                    bitmap = bitmap1;
                }
            } catch (OutOfMemoryError ex) {
                // 建议如果出现了内存不足异常，最好return 原始的bitmap对象。.
//                throw new VirtualPageException("OutOfMemoryError :" + ex.toString());
                return bitmap;
            }
        }
        return bitmap;
    }


}

