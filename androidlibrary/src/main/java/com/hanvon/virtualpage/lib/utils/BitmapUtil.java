package com.hanvon.virtualpage.lib.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;

        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize = (outWidth / width + outHeight / height) / 2;
//            Log.d(TAG, "sampleSize = " + sampleSize);
            options.inSampleSize = sampleSize;
        } else {
            options.inSampleSize = 4;
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap docodeFileThumbnail(String path, int width, int height) {
        Bitmap bmp = BitmapFactory.decodeFile(path);
        bmp = ThumbnailUtils.extractThumbnail(bmp, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bmp;
    }

    public static void drawableToBitmap(Drawable drawable, Bitmap bitmap,
                                        int width, int height) {
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}

