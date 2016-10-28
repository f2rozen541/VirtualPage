package com.hanvon.virtualpage.beans;

import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.pageeditor.widget.PageSaveTask;
import com.hanvon.virtualpage.utils.LogUtil;

import java.io.File;

/**
 * @Description: 保存当前工作空间状态的单例类，从这里可以获取当前操作的对象数据
 * @Author: TaoZhi
 * @Date: 2016/3/31
 * @E_mail: taozhi@hanwang.com.cn
 */
public class Workspace {

    private static Workspace sInstance;
    private Resolution mScreenResolution;
    private Document mCurrentDocument;
    private Page mCurrentPage;

//	// 主窗体各种控件、功能管理器的引用
//	public static Activity sActivity = null;
//	public static DocumentManager mDocumentManager = null;
//	public static HistoryManager mHistoryManager = null;
//	public static LayerView mLayerView = null;
//	public static PaintView mPaintView = null;

    // 画布宽、高 tobe moved
    public static int Width = 800;
    public static int Height = 800;

    // 主activity接收Result类型
    public static int ImportFromLib = 1001; // 从图库返回数据
    public static int ImportFromCamera = 1002; // 从相机返回数据
    public static int OpenFile = 1003; // 打开文件
    public static int ImportFile = 1004; // 导入图片

    // 相机缓存位置
    public static Uri CameraUri = Uri.fromFile(new File("/sdcard/hanvon/com.hanvon.hpad.coolpaint/CameraTemp.jpg"));
    // Toast
    private static View mToastView;
    private static Toast mToast;

    // 构造函数
    private Workspace() {
        // TODO: 16-3-3 初始化各变量
//        DisplayMetrics metrics = BaseApplication.getdisplayMetrics();
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.widthPixels = 800;
        metrics.heightPixels = 800;
//        DisplayMetrics metrics = BaseApplication.getContext().getResources().getDisplayMetrics();
        mScreenResolution = new Resolution(metrics.widthPixels, metrics.heightPixels);
    }

    public static Workspace getInstance() {
        if (null == sInstance) {
            sInstance = new Workspace();
            Thread PageSaveThread = new Thread(PageSaveTask.getInstance());
            PageSaveThread.setDaemon(true);
            PageSaveThread.start();
        }
        return sInstance;
    }


    public Resources getResources() {
        return BaseApplication.getContext().getResources();
//        if(BaseApplication.getContext() != null)
//            return BaseApplication.getContext().getResources();
//        else
//            return BaseApplication.getContext().getResources();
    }

    /**
     * 设置当前工作空间中的Document，并且设置当前工作空间中的Page为Document中的最后一条记录
     *
     * @param document document对象实例
     */
    public void setCurrentDocument(Document document) {
        if (document != null && !TextUtils.isEmpty(document.getPath())) {
            document.open();
            if (mCurrentDocument != null && !mCurrentDocument.equals(document)) {
                mCurrentDocument.dispose();
            }
            mCurrentDocument = document;
            // 如果当前Page为空就设置最后一条记录为默认
            if (mCurrentPage == null) {
                mCurrentPage = document.getLastPage();
            }
        } else {
            LogUtil.e("设置当前的Document失败，当前Document为null或者存储路径为null");
            mCurrentDocument = null;
            mCurrentPage = null;
        }
    }

    /**
     * 获取当前工作空间状态下的Document，可能为null，如果为空，就获取默认的Document
     *
     * @return 当前工作空间状态下的Document
     */
    public Document getCurrentDocument() {
        if (mCurrentDocument == null) {
            mCurrentDocument = Manifest.getInstance().get(0);
            setCurrentDocument(mCurrentDocument);
        }
        return mCurrentDocument;
    }

    public String getCurrentDocumentPath() {
        if (mCurrentDocument != null)
            return mCurrentDocument.getStoragePath();
        else
            return null;
    }

    public void setCurrentPage(Page page) {

        if (page == null) {
            mCurrentPage = null;
        } else {
            if (page.getOwner() == null) {
                page.setOwner(getCurrentDocument());
            }
            mCurrentPage = page;
        }

    }

    public Page getCurrentPage() {
        if (mCurrentPage == null) {
            mCurrentPage = getCurrentDocument().getLastPage();
        }
        return mCurrentPage;
    }

    public String getCurrentPagePath() {
        if (mCurrentPage != null && mCurrentDocument != null) {
            return mCurrentPage.getStoragePath();
        } else {
            return null;
        }
    }

    public Resolution getScreenResolution() {
        return mScreenResolution;
    }
}
