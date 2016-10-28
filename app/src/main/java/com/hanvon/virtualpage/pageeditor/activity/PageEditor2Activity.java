package com.hanvon.virtualpage.pageeditor.activity;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Document;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.beans.UIConstants;
import com.hanvon.virtualpage.beans.Workspace;
import com.hanvon.virtualpage.common.ConstantValues;
import com.hanvon.virtualpage.common.ElementType;
import com.hanvon.virtualpage.notecomponent.activity.ShowPagesActivity;
import com.hanvon.virtualpage.pageeditor.tools.stroke.CanvasLayout;
import com.hanvon.virtualpage.pageeditor.tools.stroke.NoteParams;
import com.hanvon.virtualpage.pageeditor.tools.stroke.NoteStrokeView;
import com.hanvon.virtualpage.utils.BitmapUtil;
import com.hanvon.virtualpage.utils.FileManager;
import com.hanvon.virtualpage.utils.InformationParser;
import com.hanvon.virtualpage.utils.LogUtil;
import com.hanvon.virtualpage.utils.PermissionsActivity;
import com.hanvon.virtualpage.utils.PermissionsChecker;

import java.io.File;

/**
 * -------------------------------
 * Description:
 * <p/> 这个类专门为合并生成缩略图添加的；因为生成缩略图必须走编辑界面，不能单纯的将两张图片重叠，而又不希望用户看到这个过程
 * 于是，添加了一个透明的Activity，功能加载数据保存缩略图，然后马上结束，这样就能在获取到合并后的缩略图了
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/2/18
 * E_mail:  taozhi@hanwang.com.cn
 */
//public class PageEditor2Activity extends AppCompatActivity implements PageSettingDialog.OnBackgroundChangeListener, View.OnClickListener {
public class PageEditor2Activity extends Activity {

    private Context mContext;

    public CanvasLayout mCanvasLayout;
    public NoteStrokeView mStrokeView;
    private Page currentPage;
    private Document currentDoc;

    private static final int REQUEST_CODE = 11; // 请求权限的请求码
    private int currentPageBgId;
    private PermissionsChecker mPermissionsChecker;
    private static final String[] NECESSARY_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private boolean hasMerged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mContext = BaseApplication.getContext();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.page_merge_save);
        mPermissionsChecker = new PermissionsChecker(mContext);
        hasMerged = false;
        initViews();
        loadData();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasMerged == false) {
            hasMerged = true;
            Log.e("shiyu->", "onWindowFocusChanged start...............................");
            setResult(ShowPagesActivity.PAGE_EDITOR_RESULT);
            saveCurrentPageData();
            finish();
            Log.e("shiyu->", "onWindowFocusChanged finish...............................");
        }

    }


    /**
     * 初始化来自Document中page的数据
     */
    private void initDocumentData() {
        checkForCurrentPage();
        String currentPagePath = Workspace.getInstance().getCurrentPagePath();
        if (!TextUtils.isEmpty(currentPagePath) && !currentPage.hasLoaded()) {
            if (FileManager.CheckDir(currentPagePath)) {
                loadPage(currentPage); // 加载当前page中的数据
            } else {
                clearCanvas();
            }
        }
    }

    /**
     * 获取currentPage的数据
     */
    private void checkForCurrentPage() {
        currentDoc = Workspace.getInstance().getCurrentDocument();
        if (currentDoc == null) {
            currentDoc = InformationParser.getDefaultDocument();
            Workspace.getInstance().setCurrentDocument(currentDoc);
        }
        currentPage = Workspace.getInstance().getCurrentPage();
        if (currentPage == null) { // 如果当前page为空
            LogUtil.e("当前的page数据为空，新建一个page");
            currentPage = InformationParser.newPageInDocument(mContext, currentDoc);
            Workspace.getInstance().setCurrentPage(currentPage);
        }
    }


    /**
     * 加载页面数据，加载数据前需要检查权限是否全部获取到
     */
    private void loadData() {
        if (mPermissionsChecker.lacksPermissions(NECESSARY_PERMISSIONS)) {
            startPermissionCheckActivity();
        } else {
            LogUtil.i("loadData", "加载了数据");
            BaseApplication.setHasPermission(true);
            initDocumentData();
            displayedData();
        }
    }


    private void saveCurrentPageData() {
        if (Workspace.getInstance().getCurrentPage() != null) {
            NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.STROKE);//if there is no stroke also save the page.
            savePage(Workspace.getInstance().getCurrentPage(), true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (BaseApplication.hasPermission()) {
            saveCurrentPageData();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        releaseResource();
        clearCanvas();
        NoteParams.getCurrentPenNoteParams().deleteObservers();
        ConstantValues.getInstance().releaseStrokeViewArray();
        Runtime.getRuntime().gc();
        System.runFinalization();
        super.onDestroy();
    }

    private void startPermissionCheckActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, NECESSARY_PERMISSIONS);
    }

    private void clearCanvas() {
        try {
            if (mCanvasLayout != null) {
                mCanvasLayout.clearCanvas();
            }
            if (mStrokeView != null) {
                mStrokeView = null;
            }
        } catch (Exception e) {
            LogUtil.e("clearCanvas() 出现异常：" + e.toString());
        }
    }
    private void releaseResource() {
        if (mStrokeView != null) {
            mStrokeView.releaseStrokeViewResource();
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initViews() {
        mCanvasLayout = (CanvasLayout) findViewById(R.id.canvas);
        mStrokeView = (NoteStrokeView) findViewById(R.id.stroke_view);
        NoteParams.getCurrentPenNoteParams().addObserver(mCanvasLayout);
        NoteParams.getCurrentPenNoteParams().addObserver(mStrokeView);

    }

    private void displayedData() {
        currentPageBgId = UIConstants.ARRAY_PAGE_BG_REPEAT[UIConstants.getAvailableBgResIndex(currentPage.getBackgroundResIndex())];
        mStrokeView.setStrokeListData(currentPage.getStrokeList());
        mStrokeView.setBgDrawableID(currentPageBgId);
        mCanvasLayout.setElementList(currentPage.getElementList());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
                    LogUtil.i("loadData", "权限不够，直接关闭");
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private synchronized void savePage(final Page page, boolean async) {
        LogUtil.i("Save Page start.");
        try {
            if (page != null) {
                page.setTitle("");
//                page.setUpdatedTime(TimeHelper.getCurrentDateTime());
                Log.d("currPage", "savePage() called with: " + "page.setUpdatedTime(TimeHelper.getCurrentDateTime())====>" + page.getUpdatedTime());
//                page.setBackgroundResIndex(String.valueOf(currentPageBgId));
                if (mStrokeView.getStrokeList() != null) { // 添加一个数据保护：因为锁屏旋屏之后，获取到的数据是null，保存之后，再次读取会导致程序崩溃；目前还不确定为何为空
                    page.setStrokeList(mStrokeView.getStrokeList());
                }
                if (mCanvasLayout.getElementList() != null) {
                    page.setElementList(mCanvasLayout.getElementList());
                }
                if (mCanvasLayout.isDrawingCacheEnabled() == false) {
                    mCanvasLayout.setDrawingCacheEnabled(true);
                }
                System.gc();
                Bitmap drawingCache = mCanvasLayout.getDrawingCache();
                LogUtil.e("size", drawingCache.getWidth() + "<--width, height-->" + drawingCache.getHeight());
                Bitmap bitmap = ThumbnailUtils.extractThumbnail(drawingCache, 640, 379, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
//                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    bitmap = BitmapUtil.adjustPhotoRotation(bitmap, 90);
//                } else {
//                    bitmap = BitmapUtil.adjustPhotoRotation(bitmap, -90);
//                }
                bitmap = BitmapUtil.adjustPhotoRotation(bitmap, -90);
                page.saveThumbnailImmediately(bitmap);
                LogUtil.d("Merge", "当前保存的的Page的缩略图路径为：" + page.getThumbnailFilePath());
                mCanvasLayout.setDrawingCacheEnabled(false);
//                if (async) {
//                    PageSaveTask.getInstance().offer(page, true);
//                } else {
//                }
//                page.save();
//                page.getOwner().save();
//                Workspace.getInstance().setCurrentDocument(page.getOwner());
                Workspace.getInstance().setCurrentPage(page);
                recycleBitmap(drawingCache);
//                recycleBitmap(bitmap);
            }
        } catch (Exception e) {
            LogUtil.e("保存数据失败：" + e);
        }
    }

    private void recycleBitmap(Bitmap bitmap) {
        if ((bitmap != null) && (bitmap.isRecycled() == false)) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    private boolean loadPage(Page page) {
        if (page.open()) {
            return true;
        } else
            return false;
    }

    private boolean loadPage(String path) {
        Page page = new Page();
        if (page.open(path + File.separator + UIConstants.PageFilename)) {
            mStrokeView.setStrokeList(page.getStrokeList());
            mCanvasLayout.setElementList(page.getElementList());
            return true;
        } else
            return false;
    }


}
