package com.hanvon.virtualpage.utils;

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
 * @Description:
 * @Author: TaoZhi
 * @Date: 2016/4/28
 * @E_mail: taozhi@hanwang.com.cn
 */
public class FileManagerUtils {

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
                newBmp.recycle();
            newBmp = null;
            weakBmp=null;
			if(!bmp.isRecycled())
				bmp.recycle();
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

//    public static void copyFile(String src, String des) throws IOException {
//        File srcFile = new File(src);
//        File destFile = new File(des);
//        FileInputStream input = new FileInputStream(srcFile);
//        try {
//            FileOutputStream output = new FileOutputStream(destFile);
//            try {
//                byte[] buffer = new byte[1024];
//                int n = 0;
//                while (-1 != (n = input.read(buffer))) {
//                    output.write(buffer, 0, n);
//                }
//            } finally {
//                try {
//                    if (output != null) {
//                        output.close();
//                    }
//                } catch (IOException ioe) {
//                }
//            }
//        } finally {
//            try {
//                if (input != null) {
//                    input.close();
//                }
//            } catch (IOException ioe) {
//            }
//        }
//    }

//    // delete file
//    public static boolean deleteFile(String filePath) {
//        File file = new File(filePath);
//        boolean result = false;
//        if (file.exists()) {
//            result = file.delete();
//        }
//        return result;
//    }

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



    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    LogUtil.e("bytesum = " + bytesum);

                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }

    }


    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath) {

        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();

        }

    }


    /**
     * 递归删除文件和文件夹
     *
     * @param path 要删除的根目录
     */
    public static void deleteFile(String path) {

        File file = new File(path);

        if (file.exists() == false) {
            return;
        } else {
            if (file.isFile()) {
                file.delete();
                return;
            }
            if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile == null || childFile.length == 0) {
                    file.delete();
                    return;
                }
                for (File file1 : childFile) {
                    deleteFile(file1.getAbsolutePath());
                }
                file.delete();
            }
        }
    }

}
