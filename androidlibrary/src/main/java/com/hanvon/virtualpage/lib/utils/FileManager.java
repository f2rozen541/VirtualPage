package com.hanvon.virtualpage.lib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;

import com.hanvon.hpad.view.SDCardState;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * -------------------------------
 * Description:
 * FileManager: save, remove......
 * -------------------------------
 * Author:  hll
 * Date:    2016/2/24
 */
public class FileManager {
    /**
     * save bitmap to file with original size
     *
     * @param bmp
     * @param path
     * @throws IOException
     */
    public static void saveBitmapToFile(Bitmap bmp, String path)
            throws IOException {
        if (bmp != null && path != null) {
            saveBitmapToFile(bmp,path,Bitmap.CompressFormat.PNG,80,bmp.getWidth(),bmp.getHeight());
        }
    }

    /**
     * save bitmap to file with the width and height
     * @param bmp
     * @param path
     * @param width
     * @param height
     * @throws IOException
     */
    public static void saveBitmapToFile(Bitmap bmp,String path,int width,int height)
            throws IOException {
        saveBitmapToFile(bmp,path,Bitmap.CompressFormat.JPEG,90,width,height);
    }

    private static void saveBitmapToFile(Bitmap bmp,String path,Bitmap.CompressFormat format,
                                         int quality,int width,int height)throws IOException{
        if(bmp!=null){
            File pngFile = new File(path);
            if (pngFile.exists())
                pngFile.delete();
            pngFile.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pngFile));
            WeakReference<Bitmap> weakBmp = new WeakReference<Bitmap>(Bitmap.createScaledBitmap(bmp, width, height, true));
            Bitmap newBmp = weakBmp.get();
            newBmp.compress(format, quality, bos);
            bos.flush();
            bos.close();
            if(!newBmp.isRecycled())
//                newBmp.recycle();
            newBmp = null;
            weakBmp=null;
//			if(!bmp.isRecycled())
//				bmp.recycle();
            bmp = null;
        }
    }

    /***
     * remove file or directory
     *
     * @param path
     */
    public static void removeFile(File path) throws IOException {
        System.out.println("removing file " + path.getPath());
        if (path.isDirectory()) {
            File[] child = path.listFiles();
            if (child != null && child.length != 0) {
                for (int i = 0; i < child.length; i++) {
                    removeFile(child[i]);
                    child[i].delete();
                }
            }
        }
        path.delete();
    }

    /***
     * check directory,create it if it does not contains
     *
     * @return
     */
    public static boolean CheckDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    /***
     * check child of directory
     *
     * @param Path
     * @param childName
     * @return
     * @throws IOException
     */
    public static String CheckChildDir(String Path, String childName)
            throws IOException {
        String childAbsPath = Path + childName;
        File childFile = new File(childAbsPath);
        if (!childFile.exists()) {
            childFile.mkdirs();
        }
        return childAbsPath;
    }

    public static String getSDCardPath() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException("SDCard is not valid!");
        } else {
            File SDCard = Environment.getExternalStorageDirectory();
            return SDCard.getAbsolutePath();
        }
    }

    public static boolean CheckSDCard(Context context,boolean showToast) {
        SDCardState sdCardState = new SDCardState(context);
        return sdCardState.CheckSDCardPrepared(showToast);
    }

    public static void copyFile(String src, String des) throws IOException {
        File srcFile = new File(src);
        File destFile = new File(des);
        FileInputStream input = new FileInputStream(srcFile);
        try {
            FileOutputStream output = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[1024];
                int n = 0;
                while (-1 != (n = input.read(buffer))) {
                    output.write(buffer, 0, n);
                }
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException ioe) {
                }
            }
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
            }
        }
    }

    // delete file
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        boolean result = false;
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }

    /**
     * Calculate the inputStream's length.
     * @param in InputStream
     * @return length
     */
    public static long Length(InputStream in){
        long length=0;
        int readSize=0;
        byte[] buffer=new byte[4096];
        try{
            while((readSize = in.read(buffer)) != -1){
                length += readSize;
            }
            return length;
        }
        catch(IOException ex){
            return 0;
        }
    }

    public static Bitmap getBitMapFromPath(String path){
        if (path != null){
            final File file = new File(path);
            if (file.exists()) {
                InputStream stream = null;
                try {
                    stream = new FileInputStream(file);
                    return BitmapFactory.decodeStream(stream);
                } catch (FileNotFoundException e) {
                } finally {
                    closeStream(stream);
                }
            }
            return null;
        }
        return null;
    }

    public static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    public static boolean isEnoughSpaceSize(long size) {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        //sd卡分区数
        int blockCounts = statFs.getBlockCount();
        //sd卡可用分区数
        int avCounts = statFs.getAvailableBlocks();
        //一个分区数的大小
        long blockSize = statFs.getBlockSize();
        //sd卡可用空间
        long spaceLeft = avCounts * blockSize;
        if (spaceLeft < size)
        {
            return false;
        }
        return true;
    }
}


