package com.hanvon.virtualpage.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Document;
import com.hanvon.virtualpage.beans.Manifest;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.beans.UIConstants;
import com.hanvon.virtualpage.beans.VirtualPageException;
import com.hanvon.virtualpage.beans.Workspace;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Description:
 * 信息处理类
 * <p/>
 * Author:  TaoZhi
 * Date:    2016/1/29
 * E_mail:  taozhi@hanwang.com.cn
 */
public class InformationParser {

    private static String TAG = "InformationParser";

    private InformationParser() {
        throw new Error("Do not need instantiate!");
    }

    private static List<Document> noteInfoBeanList = null;

    private static List<Page> currentPages = null;

    /**
     * 获取所有的Documents信息
     *
     * @return
     */
    public static List<Document> getNotesInfo() {
        Manifest manifest = Manifest.getInstance();
        noteInfoBeanList = manifest.getDocuments();
        return noteInfoBeanList;
    }

    /**
     * 获取当前document下的所有page属性
     *
     * @param currentDocument
     * @return
     */
    public static List<Page> getCurrentPagesInfo(Document currentDocument) {
        Workspace.getInstance().setCurrentDocument(currentDocument);
        currentPages = currentDocument.getPages();
        return currentPages;
    }

    /**
     * 实例化NoteSaver,获取默认note.
     */
    public static Document getDefaultDocument(){
//        Document document = new Document();
        Document document = Manifest.getInstance().get(0);
        if (document == null) { // 第一次小窗跳转进入，可能为空，这时就新建一个Note去读取Document.xml文件
//            document = newDefaultDocument();
            document = new Document();
//            document.setTitle(UIConstants.DefaultNoteFileName);
//            try {
//                Manifest.getInstance().add(document);
//                Manifest.getInstance().save();
//            } catch (VirtualPageException e) {
//                e.printStackTrace();
//            }
        }
        document.setPath(UIConstants.ROOT_PATH + "/" + UIConstants.DefaultNoteFileName + "/");
        document.open();
        return document;
    }

    public static Page getLatestUpdatePage(List<Page> pages) {
        if (pages == null || pages.size() < 1) {
            return null;
        }
        List<Page> temp = new ArrayList<>(pages);
        Collections.sort(temp, new PageListTimeComparator());
        return temp.get(0);
    }

    /**
     * 新建document，并设置为当前的document,保存新的main文件，保存document背景
     *
     * @param context
     * @param manifest
     */
    public static void addNewDocument(final Context context, Manifest manifest) {
        try {
            Document doc = new Document();

            String currentDateTime = TimeHelper.getCurrentDateTime();
            String strNewTitle = context.getString(R.string.noteName);
//            String strTitle = strNewTitle + manifest.getMaxDefault();
            String strTitle = manifest.getUntitledName(strNewTitle);
            String docPath = UIConstants.ROOT_PATH + "/" + currentDateTime + "/";

            doc.setTitle(strTitle);
            doc.setCreatedTime(currentDateTime);
            doc.setUpdatedTime(currentDateTime);
            doc.setPath(docPath);
//            doc.setBackgroundResIndex(String.valueOf(R.drawable.note_cover2));
//            doc.setBackgroundResIndex(String.valueOf(UIConstants.DEFAULT_COVER_INDEX));
            int randomCoverIndex = (int) (System.currentTimeMillis() % 6);
//            Toast.makeText(context, "" + randomCoverIndex, Toast.LENGTH_SHORT).show();
            doc.setBackgroundResIndex(String.valueOf(randomCoverIndex));
            doc.save();
            Workspace.getInstance().setCurrentDocument(doc);
            FileManager.CheckChildDir(FileManager.getSDCardPath(), UIConstants.ROOT_PATH);
            FileManager.CheckChildDir(FileManager.getSDCardPath(), UIConstants.ROOT_PATH + "/" + currentDateTime);

            Page page = newPageInDocument(context, doc);
            // 先建立关联，然后去写文件
            if (page != null) {
                Workspace.getInstance().setCurrentPage(page);
//                ThreadPoolManager.getInstance().addTask(new Runnable() {
//                    @Override
//                    public void run() {
////                    savePageDocument2Sdcard(context, page);
//                        try {
////                            String pagePath = FileManager.getSDCardPath() + page.getOwner().getPath() + "/" + page.getCreatedTime();
//                            String pagePath = page.getStoragePath();
//                            if (FileManager.CheckDir(pagePath)) {
//                                page.save();//保存当前页(图像和xml文件
//                                if (page.getOwner() != null) {
//                                    page.getOwner().save();//保存document.xml文件
//                                }
//                                LogUtil.e(pagePath +"====>保存完毕!");
//                            }
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                });
            }
            manifest.add(doc);
            manifest.save();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(TAG, "addNewDocument: VirtualPageException" + ex.toString());
        }
    }

    /**
     * 新建document，并设置为当前的document,保存新的main文件，保存document背景
     * NoteSaver的绝对路径为：FileManager.getSDCardPath()+UIConstants.ROOT_PATH + "/NoteSaver" + "/";
     *
     * @param context
     * @param m
     */
    public static void newDefaultDocument(final Context context, Manifest m) {
        try {
            // 1.新建一个Document
            Document doc = new Document();
            String currentDateTime = TimeHelper.getCurrentDateTime();
            String strTitle = context.getString(R.string.NoteSaver);
            String docPath = UIConstants.ROOT_PATH + "/" + UIConstants.DefaultNoteFileName + "/";

            doc.setTitle(strTitle);
            doc.setCreatedTime(currentDateTime);
            doc.setUpdatedTime(currentDateTime);
            doc.setPath(docPath);
            doc.setBackgroundResIndex(String.valueOf(UIConstants.DEFAULT_COVER_INDEX));
            FileManager.CheckChildDir(FileManager.getSDCardPath(), UIConstants.ROOT_PATH);
            FileManager.CheckChildDir(FileManager.getSDCardPath(), UIConstants.ROOT_PATH + "/" + UIConstants.DefaultNoteFileName);
            doc.save(); // 先生成文件
            // 设置为当前工作空间中的Document
            Workspace.getInstance().setCurrentDocument(doc);
            // 新建一个Page，并将Page添加到Document中
            Page newPage = newPageInDocument(context, doc);
            // 保存Page信息到文件目录中
            if (newPage != null)  {
                // 设置为当前工作空间中的Page
                Workspace.getInstance().setCurrentPage(newPage);
            }
            m.add(doc);
            m.save();
        } catch (Exception ex) {
            ex.printStackTrace();
            LogUtil.d("Exception", "newDefaultDocument Failed! Due to the reason :" + ex.toString());
        }
    }


    /**
     * Safely initialize the Manifest
     *
     * @param context
     * @param manifest
     */
    public static void initializedManifest(final Context context, Manifest manifest) {

        if (manifest.size() > 0) { // if Manifest has any Document, there is no need to create more, so Return as the end
            return;
        }
        if (BaseApplication.isFirstLoad()) {
            BaseApplication.setFirstLoad(false);
            BaseApplication.showHelpActivity();
        }
        // this is a completely new Manifest
        // so we should check the SDCard directory path to see if any other guy has saved any document
        String defaultDocPath = Environment.getExternalStorageDirectory() + UIConstants.ROOT_PATH
                + "/" + UIConstants.DefaultNoteFileName
                + "/" + UIConstants.DocumentFilename;
        File file = new File(defaultDocPath);
        LogUtil.e("Manifest", "defaultDocPath:" + defaultDocPath);
        if (file.exists()) { // if the 'document.xml' has exsited, write into the new created Manifest

            LogUtil.e("Manifest", "defaultDocPath: 当前document.xml文件已经存在了！");
            try {
                Document doc = new Document();
                // 1.将PortableInk程序保存的数据写入Manifest.xml中，并将该Document设置为工作空间中的Document
                if (doc.open(defaultDocPath)) { // 设置封面图片，写入文件目录中
                    String currentDateTime = TimeHelper.getCurrentDateTime();
                    doc.setCreatedTime(currentDateTime);
                    doc.setUpdatedTime(currentDateTime);
                    String docPath = UIConstants.ROOT_PATH + "/" + UIConstants.DefaultNoteFileName + "/";
                    doc.setPath(docPath);
                    doc.setTitle(context.getString(R.string.NoteSaver));
                    doc.setBackgroundResIndex(String.valueOf(UIConstants.DEFAULT_COVER_INDEX));
                    doc.save();
                    Workspace.getInstance().setCurrentDocument(doc);
                    manifest.add(doc);
                    manifest.save();
                }
                // 2.获取当前Document中的最新的Page，设置为工作空间中的Page
                if (doc.getCount() > 0) {
                    Page latestPage = doc.getPage(doc.getCount() - 1);
                    latestPage.setOwner(doc); // 还不确定这句话是不是多余
                    Workspace.getInstance().setCurrentPage(latestPage);
                }
            } catch (Exception ex) {
                ex.toString();
                LogUtil.d("Exception", "Attach document.xml to Manifest.xml Failed! Due to the reason :" + ex.toString());
            }
        } else { // create a default Document and write into the Manifest
            LogUtil.e("Manifest", "defaultDocPath: 当前document.xml不存在，新建一个！newDefaultDocument");
            newDefaultDocument(context, manifest);
        }
    }

    /**
     * Helper method to ensure that the given path exists.
     */
    private boolean ensureSDCardAccess(String DocPath) {
        File file = new File(DocPath);
        if (file.exists()) {
            return true;
        } else if (file.mkdirs()) {
            return true;
        }
        return false;
    }

    /**
     * 保存新建的page和他所在的document到sd卡
     *
     * @param context
     * @param page
     */
    private static void savePageDocument2Sdcard(Context context, Page page) {
        try {
            String pagePath = FileManager.getSDCardPath() + page.getOwner().getPath() + "/" + page.getCreatedTime();
            if (FileManager.CheckDir(pagePath)) {
                page.save();//保存当前页(图像和xml文件
                if (page.getOwner() != null) {
                    page.getOwner().save();//保存document.xml文件
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 在指定的document中新建一页，并保存关系
     * Page的绝对路径为：operatedPage.getStoragePath();
     *
     * @param context
     * @param document
     * @return
     */
    public static Page newPageInDocument(Context context, Document document) {
        if (document == null) {
            LogUtil.e("newPageInDocument Failed: Argument 'Document' is null!");
            return null;
        }
        String currentDateTime = TimeHelper.getCurrentDateTime();
        Page operatedPage = new Page(currentDateTime);
        document.add(operatedPage);
        operatedPage.setBackgroundResIndex(String.valueOf(UIConstants.DEFAULT_PAGE_BG_INDEX));
        operatedPage.getStoragePath();
        operatedPage.saveThumbnailImmediately(getDefaultPageBitmap(context));
//        LogUtil.e("NewPage", "=======================================================================");
//        LogUtil.i("NewPage", "newPageInDocument--->getCreatedTime():" + operatedPage.getCreatedTime());
//        LogUtil.i("NewPage", "newPageInDocument--->getPath():" + operatedPage.getPath());
//        LogUtil.i("NewPage", "newPageInDocument--->getBackgroundResIndex():" + operatedPage.getBackgroundResIndex());
//        LogUtil.i("NewPage", "newPageInDocument--->getOwner():" + operatedPage.getOwner());
//        LogUtil.i("NewPage", "newPageInDocument--->getStoragePath():" + operatedPage.getStoragePath());
//        LogUtil.i("NewPage", "newPageInDocument--->getThumbnailFilePath():" + operatedPage.getThumbnailFilePath());
//        LogUtil.e("NewPage", "=======================================================================");
        String pagePath = operatedPage.getStoragePath();
        if (FileManager.CheckDir(pagePath)) {
            operatedPage.save();//保存当前页(图像和xml文件
            if (operatedPage.getOwner() != null) {
                operatedPage.getOwner().save();//保存document.xml文件
            }
        }
        return operatedPage;
    }

    /**
     * 获取一个空白Page的缩略图
     *
     * @param context 上下文对象
     * @return 空白Page缩略图的Bitmap对象
     */
    public static Bitmap getDefaultPageBitmap(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.page);
    }


    /**
     * 根据当前的时间，格式化为指定的时间戳格式
     *
     * @param currentTimeMillis 当前的时间毫秒值
     * @return 指定的时间戳格式；例如：2016/03/08 08:00:00
     */
    public static String timeFormatToString(long currentTimeMillis) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String timeLabel = format.format(new Date(currentTimeMillis));
        return timeLabel;
    }

    /**
     * 根据当前时间获取默认的笔记本名称
     *
     * @param currentTimeMillis 当前时间的毫秒值
     * @return 格式化的笔记本名称；例：Note_20160308
     */
    public static String getDefaultNoteName(long currentTimeMillis) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String timeStamp = format.format(new Date(currentTimeMillis));
        timeStamp = "Note_" + timeStamp;
        return timeStamp;
    }

    public static String getFormatTimeInfo(String time) {
        String timeStamp = "";
        try {
            SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMddHHmmss");
            long time1 = format1.parse(time).getTime();
//            SimpleDateFormat format2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            SimpleDateFormat format2 = new SimpleDateFormat("MM/dd/yyyy");
            timeStamp = format2.format(time1);

            if (BaseApplication.specifiedLocate(BaseApplication.getContext(), "ru")) { // 如果为俄语，就按照08.16.2016格式显示
                timeStamp = timeStamp.replace('/', '.');
            }
            if (BaseApplication.specifiedLocate(BaseApplication.getContext(), "fr")) { // 如果为法语，就按照16/08/2016格式显示
                SimpleDateFormat format3 = new SimpleDateFormat("dd/MM/yyyy");
                timeStamp = format3.format(time1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeStamp;
    }

    public static class NoteListComparator implements Comparator {

        public static final String HEAD_TAG = BaseApplication.getContext().getString(R.string.NoteSaver);

        @Override
        public int compare(Object a, Object b) { // 这里的b是第一个值，a是第二个值
            Document bean1 = (Document) b;
            Document bean2 = (Document) a;

            if (bean1.getTitle().equals(HEAD_TAG)) { // 如果Note的名字为"NoteSaver"，就默认为最大的值
                return 1;
            } else if (bean2.getTitle().equals(HEAD_TAG)) {
                return -1;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String time1 = bean1.getUpdatedTime();
            String time2 = bean2.getUpdatedTime();
            try {
                Date date1 = sdf.parse(time1);
                Date date2 = sdf.parse(time2);
                if (date1.getTime() > date2.getTime()) {
                    return 1;
                } else if (date1.getTime() == date2.getTime()) {
                    return 0;
                } else {
                    return -1;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    public static class PageListTimeComparator implements Comparator {

        @Override
        public int compare(Object a, Object b) {
            if (!(a instanceof Page) || !(b instanceof Page)) {
                try {
                    throw new VirtualPageException("Can't compare this two Objects which is not instance of Page!");
                } catch (VirtualPageException e) {
                    e.printStackTrace();
                }
            }
            Page page1 = (Page)a;
            Page page2 = (Page)b;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String time1 = page1.getUpdatedTime();
            String time2 = page2.getUpdatedTime();
            try {
                Date date1 = sdf.parse(time1);
                Date date2 = sdf.parse(time2);
                if (date1.getTime() < date2.getTime()) {
                    return 1;
                } else if (date1.getTime() == date2.getTime()) {
                    return 0;
                } else {
                    return -1;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        }
    }


}
