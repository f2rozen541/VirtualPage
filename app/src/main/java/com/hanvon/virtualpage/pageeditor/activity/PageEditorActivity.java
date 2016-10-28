package com.hanvon.virtualpage.pageeditor.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.smartpad.SmartpadManager;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hanvon.core.Stroke;
import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Document;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.beans.PageRotation;
import com.hanvon.virtualpage.beans.UIConstants;
import com.hanvon.virtualpage.beans.VirtualPageException;
import com.hanvon.virtualpage.beans.Workspace;
import com.hanvon.virtualpage.common.AboutActivity;
import com.hanvon.virtualpage.common.ConstantValues;
import com.hanvon.virtualpage.common.EditorState;
import com.hanvon.virtualpage.common.ElementLayout;
import com.hanvon.virtualpage.common.ElementType;
import com.hanvon.virtualpage.common.GalleryListView;
import com.hanvon.virtualpage.common.GalleryListViewAdapter;
import com.hanvon.virtualpage.notecomponent.activity.ShowPagesActivity;
import com.hanvon.virtualpage.common.OnFocusItemChangedListener;
import com.hanvon.virtualpage.pageeditor.tools.stroke.CanvasLayout;
import com.hanvon.virtualpage.pageeditor.tools.stroke.NoteParams;
import com.hanvon.virtualpage.pageeditor.tools.stroke.NoteStrokeView;
import com.hanvon.virtualpage.pageeditor.tools.stroke.StrokeView;
import com.hanvon.virtualpage.pageeditor.tools.stroke.params.StrokeStyleType;
import com.hanvon.virtualpage.pageeditor.tools.stroke.params.StrokeWidthType;
import com.hanvon.virtualpage.pageeditor.view.DialogSetLocationPage;
import com.hanvon.virtualpage.pageeditor.view.PageEditorBottomView;
import com.hanvon.virtualpage.pageeditor.view.PageEditorTopView;
import com.hanvon.virtualpage.pageeditor.view.PageSettingDialog;
import com.hanvon.virtualpage.pageeditor.widget.LandscapePageList;
import com.hanvon.virtualpage.pageeditor.widget.PageSaveTask;
import com.hanvon.virtualpage.pageeditor.widget.PortablePageList;
import com.hanvon.virtualpage.utils.BitmapUtil;
import com.hanvon.virtualpage.utils.DialogShare;
import com.hanvon.virtualpage.utils.DialogTakePic;
import com.hanvon.virtualpage.utils.FileManager;
import com.hanvon.virtualpage.utils.FileManagerUtils;
import com.hanvon.virtualpage.utils.GalleryUtil;
import com.hanvon.virtualpage.utils.InformationParser;
import com.hanvon.virtualpage.utils.LogUtil;
import com.hanvon.virtualpage.utils.PermissionsActivity;
import com.hanvon.virtualpage.utils.PermissionsChecker;
import com.hanvon.virtualpage.utils.TimeHelper;
import com.orhanobut.logger.Logger;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * -------------------------------
 * Description:
 * <p/> PageEditor界面
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/2/18
 * E_mail:  taozhi@hanwang.com.cn
 */
public class PageEditorActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout ll_main;
    private FrameLayout fl_top_container;
    private FrameLayout fl_bottom_container;
    private Context mContext;
    public static Activity sActivity;
    private PageEditorTopView topView;
    private ImageButton undoButton;
    private ImageButton redoButton;
    public static PageEditorBottomView bottomView;

    private ImageView mZoomWindow;
    private ImageView mZoomVisibleWindow;

    public CanvasLayout mCanvasLayout;
    public NoteStrokeView mStrokeView;
    public PageRotation mPageRotation;
    private Page currentPage;
    private String currentPicPath;
    private long preNewPageTimeStamp = -1;
    private long curNewPageTimeStamp = -1;
//    static Bitmap bitmapTemp;

    private static final int REQUEST_CODE = 11; // 请求权限的请求码
    private static final int PERMISSION_READ_SDCARD = 101;
    private static final int PERMISSION_CAMERA = 102;
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    public static final String ACTION_SHOW_LATEST_NOTESAVER = "com.hanvon.virtualpage.PageEditorActivity.startParameter";
    public static final int TYPE_FILE_DIR = 101;
    public static final int TYPE_FILE_LATEST = 100;
    private String fileNameExtra;
    private int showDataType;
    private boolean needShowPageList = false;
    private String pageDataPath;
    private DialogShare dialogShare;

    private PageSettingDialog dialogSetting;
    private DialogTakePic dialogTakePic;
    private int currentPageBgId;
    private int currentBgIndex;
    private static final int UNDO_LIMIT = 20;

    public static LinkedList<Object> mUndoRecords = new LinkedList<Object>();
    public static Stack<Object> mRedoRecords = new Stack<Object>();
    public static final int ACTION_CUT = 2;
    public static final int ACTION_DELETE = 3;//select(delete);erase
    public static final int ACTION_PASTE = 4;
    public static final int ACTION_ROTATION = 5;
    public static LinkedList<List<Object>> mUndoSelectedRecords = new LinkedList<>();
    public static Stack<List<Object>> mRedoSelectedRecords = new Stack<>();
    public static LinkedList<Stroke> mSelectedStrokeList = new LinkedList<Stroke>();
    public static List<Object> mSelectedElementList = new ArrayList<Object>();

    //    private HorizontalPageList mHorizontalPageList;
//    private LandscapePageList mHorizontalPageList;
    private GalleryListView mPageListView;
    private GalleryListViewAdapter mListAdapter;

    private FrameLayout flListContainer;
//    private boolean hasPageDataChanged;
    //    private boolean hasPermission = false;

    //    private ProgressDialog progressDialog;
    private boolean isAddNewData = false;
    private Intent mIntent;
    //    private Handler ioHandler = new NoLeakHandler(this);
    private PermissionsChecker mPermissionsChecker;
    private static final String[] NECESSARY_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    public static final String KILL_SELF = "COM.HANVON.KILLAPP";
    public static final String FROM_NOTESAVER = "COM.HANVON.EDITOR";
    private Document currentDoc;

    private PopupWindow ppwSettings;
    private View settingView;
    private ViewTreeObserver vto;

    private PopupWindow ppwShareMenu;
    private View shareMenuView;

    private boolean isShareLocked;


    public static int screenWidth = 0;
    public static int screenHeight = 0;
    public static int rotationValueByHand = 0;
    public static int rotationValueByButton = 0;
    public static int canvasRotation = 0;
    public static int topToolBarHeight = 70;
    public static int bottomToolBarHeight = 108;
    public int thumbnailRotation = 0;
    //    private OrientationTool orientationTool;
    private boolean isFromSavePdf = false;
    private Boolean isCanPaste;
    private RetainDataFragment retainDataFragment;

    Button previewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Log.e("PageEditorActivity", "onCreate: ");
        if (savedInstanceState != null){
//            isCanPaste = savedInstanceState.getBoolean("isCanPaste");
//            Gson gson = new Gson();
//            rect = gson.fromJson(savedInstanceState.getString("rect"), Rect.class);
//            copyStrokeList = gson.fromJson(savedInstanceState.getString("copyStrokeList"), LinkedList.class);
//            copyElementList = gson.fromJson(savedInstanceState.getString("copyElementList"), ArrayList.class);
//            Log.e("wangkun", "onCreate: copyStrokeList size" + copyStrokeList.size());
//            Log.e("wangkun", "onCreate: copyElementList size" + copyElementList.size());
//            Log.e("wangkun", "onCreate: " + rect.left);
        }
        FragmentManager fm = getFragmentManager();
        retainDataFragment = (RetainDataFragment)fm.findFragmentByTag("data");
        if(retainDataFragment == null){
            retainDataFragment = new RetainDataFragment();
            fm.beginTransaction().add(retainDataFragment, "data").commit();
        }

        isCanPaste = retainDataFragment.getCanPaste();
        if(isCanPaste == null)
            isCanPaste = false;
        rect = retainDataFragment.getRect();
        if(rect == null)
            rect = new Rect();
        copyStrokeList = retainDataFragment.getCopyStrokeList();
        if(copyStrokeList == null)
            copyStrokeList = new LinkedList<Stroke>();
        copyElementList = retainDataFragment.getCopyElementList();
        if(copyElementList == null)
            copyElementList = new ArrayList<Object>();

        Log.e("wangkun", "onCreate: copyStrokeList size" + copyStrokeList.size());
        Log.e("wangkun", "onCreate: copyElementList size" + copyElementList.size());
        Log.e("wangkun", "onCreate: " + rect.left);

        sActivity = this;
        mIntent = getIntent();
        BaseApplication.setSystemUiVisibility(sActivity, true);
        mContext = BaseApplication.getContext();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_page_editor_main);
        mPermissionsChecker = new PermissionsChecker(mContext);
        initViews();
        //getDisplayRotation();
        EditorState.getInstance().setSavingFlag(false); // 刚加载进来的数据都是保存标志都置为false
//        orientationTool = new OrientationTool(mContext);
//        orientationTool.enable();
//        switch (getRequestedOrientation()) {
//            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
//                Logger.i("ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE==>" + getRequestedOrientation());
//                break;
//            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
//                Logger.i("ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE==>" + getRequestedOrientation());
//                break;
//            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
//                Logger.i("ActivityInfo.SCREEN_ORIENTATION_PORTRAIT==>" + getRequestedOrientation());
//                break;
//            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
//                Logger.i("ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT==>" + getRequestedOrientation());
//                break;
//            default:
//                Logger.i("ActivityInfo.Default==>" + getRequestedOrientation());
//                break;
//        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mIntent = intent;
//        LogUtil.d("收到消息：KILL_SELF===>" + intent.getAction());
//        if (KILL_SELF.equals(intent.getAction())) { // 如果收到的Action为结束程序，就直接关闭
//            BaseApplication.getApplication().AppExit();
//        }
    }

    public static PageEditorActivity getInstance() {
        return (PageEditorActivity) sActivity;
    }

    /**
     * 加载页面数据，加载数据前需要检查权限是否全部获取到
     */
    private void loadData() {
        if (mPermissionsChecker.lacksPermissions(NECESSARY_PERMISSIONS)) {
            startPermissionCheckActivity();
        } else {
            BaseApplication.setHasPermission(true);
            InformationParser.initializedManifest(mContext, com.hanvon.virtualpage.beans.Manifest.getInstance());
//            Logger.i("111" + "loadData加载了数据");
            parseIntentData(mIntent);
            initDocumentData();
            displayedData();
            loadEditorState();
        }
    }

    /**
     * 加载并设置当前编辑器的状态
     */
    private void loadEditorState() {
        // PageList状态加载
//        setPageListState(EditorState.getInstance().isShownListState()); // View还没有显示的时候，调用滑动没有效果，在获取焦点中去加载这个方法

        //上下工具栏状态
        switch(EditorState.getInstance().getTopViewState()){
            case EditorState.FUNCTION_PAINT:
                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.STROKE);
                if (mStrokeView != null) {
                    mStrokeView.setSelectMode(0);
                    mStrokeView.deletePopwindows();
                }
                fl_bottom_container.setVisibility(View.VISIBLE);
                EditorState.getInstance().setBottomViewState(EditorState.PANEL_PAINT);
                break;
            case EditorState.FUNCTION_TEXT:
                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.TEXT);
                if (mStrokeView != null) {
                    mStrokeView.setSelectMode(0);
                    mStrokeView.deletePopwindows();
                }
                fl_bottom_container.setVisibility(View.VISIBLE);
                EditorState.getInstance().setBottomViewState(EditorState.PANEL_TEXT);
//                BaseApplication.getApplication().setPenKeyState(SmartpadManager.PEN_KEY_LED_STATE_OFF);
                break;
            case EditorState.FUNCTION_FRAME:
                if (mStrokeView != null) {
                    mStrokeView.setSelectMode(1);
                }
                fl_bottom_container.setVisibility(View.GONE);
                EditorState.getInstance().setBottomViewState(EditorState.PANEL_GONE);
                break;
            default:
                break;
        }
        if (topView != null) {
            topView.setCurrentState(EditorState.getInstance().getTopViewState());
            topView.requestLayout();
        }

        // 笔型参数加载、笔迹颜色值加载
        switch(EditorState.getInstance().getStrokeType()){
            case EditorState.PAINT_BRUSH:
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeStyleType(StrokeStyleType.BRUSH);
                break;
            case EditorState.PAINT_PENCIL:
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeStyleType(StrokeStyleType.PENCIL);
                break;
            case EditorState.PAINT_PEN:
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeStyleType(StrokeStyleType.PEN);
                break;
            case EditorState.PAINT_ERASER:
                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.ERASER);
                break;
            default:
                break;
        }

        switch(EditorState.getInstance().getStrokeWidth()){
            case EditorState.STROKE_THIN:
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeWidthType(StrokeWidthType.THIN);
                break;
            case EditorState.STROKE_MEDIUM:
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeWidthType(StrokeWidthType.MEDIUM);
                break;
            case EditorState.STROKE_THICK:
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeWidthType(StrokeWidthType.WIDE);
                break;
            default:
                break;
        }

        NoteParams.getCurrentPenNoteParams().setCurrentStrokeColorType(EditorState.getInstance().getStrokeColor());
        if (bottomView != null) {
            bottomView.setFunctionAreaView(EditorState.getInstance().getBottomViewState());
            bottomView.requestLayout();
        }

        // 设置笔写状态是否开启
        if (EditorState.getInstance().getTopViewState() == EditorState.FUNCTION_TEXT) {
            BaseApplication.getApplication().setPenKeyState(SmartpadManager.PEN_KEY_LED_STATE_OFF);
        } else {
            BaseApplication.getApplication().setPenKeyState(SmartpadManager.PEN_KEY_LED_STATE_ON);
        }
    }

    /**
     * 解析intent
     * @param intent
     */
    private void parseIntentData(Intent intent) {

        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action != null) {
            if (FROM_NOTESAVER.equals(action)) {
                EditorState.getInstance().resetDefaultValues();
                EditorState.getInstance().setSavingFlag(false);
                clearUndoData();
                fileNameExtra = intent.getStringExtra("com.hanvon.virtualpage.PageEditorActivity.fileName");
                showDataType = intent.getIntExtra(ACTION_SHOW_LATEST_NOTESAVER, 0);
                if (!TextUtils.isEmpty(fileNameExtra)) {
                    // 这里需要去重新读取document.xml文件中的内容，并且写入Manifest.xml中
//                    Document defaultDocument = InformationParser.getDefaultDocument();
                    currentDoc = InformationParser.getDefaultDocument();
                    if (showDataType == TYPE_FILE_DIR || showDataType == TYPE_FILE_LATEST) {
                        currentPage = new Page();
                        if (currentPage.open(fileNameExtra + File.separator + UIConstants.PageFilename)) {
//                            currentPage.setOwner(currentDoc);
                            // 此处从黑屏笔写跳转过来的时候，需要显示列表，而笔写数据还没有写入Document中，列表显示的时候就没有当前的笔写数据，
                            // 此时列表中的最后一项获取焦点的时候会调用重新加载数据，所以黑屏笔写的数据被刷掉了
//                            currentDoc.add(currentPage);
//                            currentPage.save();
//                            currentDoc.save();
//                            com.hanvon.virtualpage.beans.Manifest.getInstance().save();
                            Workspace.getInstance().setCurrentDocument(currentDoc);
                            Workspace.getInstance().setCurrentPage(currentPage);
                            LogUtil.e("成功打开：" + fileNameExtra);
                        } else {
                            LogUtil.e("打开失败：" + fileNameExtra);
                        }
                    }
                } else { // 传递过来的是一个空的路径，就新建一个Page
                    currentDoc = InformationParser.getDefaultDocument();
                    if (currentDoc.getPages().size() == 1) { // 如果NoteSaver中只有一个Page，且Page为空白，就直接使用
                        Page page = currentDoc.getPage(0);
                        if (page.getCreatedTime().equals(page.getUpdatedTime())) { // 如果创建时间与更新时间相同，则可以判定为一个空白的Page
                            currentPage = page;
                            EditorState.getInstance().setSavingFlag(true);
                        } else {
                            currentPage = InformationParser.newPageInDocument(mContext, currentDoc);
                        }
                    } else { // 否则，任何其他情况，都重新创建一个Page
                        currentPage = InformationParser.newPageInDocument(mContext, currentDoc);
                    }
                    currentPage.setOwner(currentDoc);
                    Workspace.getInstance().setCurrentDocument(currentDoc);
                    Workspace.getInstance().setCurrentPage(currentPage);
                }
                needShowPageList = true;
                mIntent = null; // 解析完之后就将意图置为null，不然会导致每次启动的时候都会走这个流程
                intent.setAction(null); // 解析完之后就将意图置为null，不然会导致每次启动的时候都会走这个流程
//                EditorState.getInstance().resetDefaultValues();
            }
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
     * 获取工作空间中的数据并赋值给currentPage
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
     * 给界面控件对象设置数据
     */
    private void displayedData() {
        currentBgIndex = UIConstants.getAvailableBgResIndex(currentPage.getBackgroundResIndex());
        currentPageBgId = UIConstants.ARRAY_PAGE_BG_REPEAT[currentBgIndex];
        mStrokeView.setStrokeListData(currentPage.getStrokeList());
        mStrokeView.setBgDrawableID(currentPageBgId);
        mCanvasLayout.setElementList(currentPage.getElementList());
        mPageRotation = currentPage.getPageRotation();
    }


    /**
     * 保存当前Page中的数据
     */
    private void saveCurrentPageData() {
        if (Workspace.getInstance().getCurrentPage() != null) {
            if (bottomView != null) {
                bottomView.dismissPopWin();
            }
            reloadDocumentBeforeSave();
            savePage(Workspace.getInstance().getCurrentPage(), true);
        }
    }

    //begin by cuishuo1
    //page上画出第一笔时设置当前page的方向
    public void setPageRotation(PageRotation pagerotation) {
        mPageRotation = pagerotation;
    }
    //end by cuishuo1

    @Override
    protected void onStart() {
        LogUtil.v("onStart()");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        LogUtil.v("onRestart()");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        LogUtil.v("onResume()");
        super.onResume();
        BaseApplication.setSystemUiVisibility(sActivity, true);
        isShareLocked = false; // 每次恢复的时候，将分享标志重置
        if (isAddNewData) { // 区分是从相机获取到数据，就不用重新加载数据
            isAddNewData = false;
        } else {
            LogUtil.i("loadData", "onResume()");
            loadData();
        }
        getDisplayRotation();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Document currentDocument = Workspace.getInstance().getCurrentDocument();
            if (currentDocument == null) {
                currentDocument = com.hanvon.virtualpage.beans.Manifest.getInstance().get(0);
                if (currentDocument == null) {
                    try {
                        throw new VirtualPageException("当前Document为空");
                    } catch (VirtualPageException e) {
                        e.printStackTrace();
                    }
                    return ;
                } else {
                    Workspace.getInstance().setCurrentDocument(currentDocument);
                }
            }
            // 如果EditorState中记录的PageList界面应该显示，就直接显示，否则就看看是否是从小窗进入
            if (EditorState.getInstance().isShownListState() == true) {
                setPageListState(true);
            } else {
                setPageListState(needShowPageList);
            }
            needShowPageList = false; // 不管是怎么进入，这个标志只会使用一次
        }

    }

    @Override
    protected void onPause() {
        LogUtil.v("onPause()");
        super.onPause();
        /**modified by cuijc3
         * 这个判断会导致点击分享之后再进行绘制，不会保存最新的缩略图;并增加判断缩略图文件是否被删除，如果被删除在展示页面只显示空白页
         if (isShareLocked == true) { // 如果已经在分享中，就不用再次保存了
         return;
         }
         if (BaseApplication.hasPermission()) {*/
        if (EditorState.getInstance().isSavingFlag() || !(new File(Workspace.getInstance().getCurrentPage().getThumbnailFilePath()).exists()) && BaseApplication.hasPermission()) {
            mCanvasLayout.clearFocus();
            EditorState.getInstance().setSavingFlag(true);
            saveCurrentPageData();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e("wangkun", "onSaveInstanceState started.......");
        retainDataFragment.setCanPaste(mStrokeView.isCanPaste);
        retainDataFragment.setRect(rect);
        retainDataFragment.setCopyStrokeList(copyStrokeList);
        retainDataFragment.setCopyElementList(copyElementList);
    }

//    private boolean saveObject(Bundle outState, String key, Object obj){
//        try {
//            Gson gson = new Gson();
//            String objStr = gson.toJson(obj);
//            outState.putString(key, objStr);
//            return true;
//        } catch (Exception e){
//            Log.e("PageEditorActivity", "saveObject failed!");
//            return false;
//        }
//    }

    @Override
    protected void onStop() {
        LogUtil.v("onStop()");
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        LogUtil.v("onDestroy()");
        super.onDestroy();
        clearCanvas();
        releaseResource();
//        if (mStrokeView != null) {
//            mStrokeView.release();
//        }
        NoteParams.getCurrentPenNoteParams().deleteObservers();
        releaseStaticRes();
        mPageRotation = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.e("bmp", "Info=====>onConfigurationChanged");
    }

    private void startPermissionCheckActivity() {
        PermissionsActivity.startActivityForResult(sActivity, REQUEST_CODE, NECESSARY_PERMISSIONS);
    }

    /**
     * 释放资源，清空画布
     */
    private void clearCanvas() {
        try {
            if (mCanvasLayout != null) {
                mCanvasLayout.clearCanvas();
            }
            if (mStrokeView != null) {
                mStrokeView.clearStrokeView();
            }
        } catch (Exception e) {
            LogUtil.e("clearCanvas", "clearCanvas() 出现异常：" + e.toString());
        }
    }

    /** 释放引用 */
    private void releaseResource() {
        sActivity = null;
        if (mStrokeView != null) {
            mStrokeView.releaseStrokeViewResource();
        }
    }

    /**
     * 用指定Page刷新当前编辑界面中的数据
     * @param page 需要被加载的Page
     */
    private void updateEditorWithPageData(Page page) {
        clearCanvas();
        loadPage(page);
        currentBgIndex = UIConstants.getAvailableBgResIndex(page.getBackgroundResIndex());
        currentPageBgId = UIConstants.ARRAY_PAGE_BG_REPEAT[currentBgIndex];
        mStrokeView.setBgDrawableID(currentPageBgId);
        mStrokeView.setStrokeListData(page.getStrokeList());
        mCanvasLayout.setElementList(page.getElementList());
        mStrokeView.postInvalidate();
        mCanvasLayout.postInvalidate();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initViews() {
        ll_main = (RelativeLayout) findViewById(R.id.ll_main);
        fl_top_container = (FrameLayout) findViewById(R.id.fl_top_container);
        fl_bottom_container = (FrameLayout) findViewById(R.id.fl_bottom_container);
        flListContainer = (FrameLayout) findViewById(R.id.fl_list_container);

        vto = ll_main.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (sActivity == null) {
                    return;
                }
                if (BaseApplication.isFullScreenActivity(sActivity) == false) {
                    Log.d("vto", "onGlobalLayout() returned: 非全屏状态，调整全屏状态");
                    BaseApplication.setSystemUiVisibility(sActivity, true);
                } else {
                    Log.i("vto", "onGlobalLayout() returned: 全屏状态");
                }
            }
        });
        topView = new PageEditorTopView(mContext);
        bottomView = new PageEditorBottomView(mContext);

        topView.setOnButtonClickListener(new PageEditorTopView.OnButtonClickListener() {
            @Override
            public void onBackClick() {
                if(mStrokeView != null){
                    mStrokeView.clearTwoFingerState();//去除缩放及平移状态
                    mStrokeView.deletePopwindows();
                }

                clearUndoData();
                mCanvasLayout.deleteEmptyEditText();
                savingDataAndClose();
            }

            @Override
            public void onNewNoteClick() {
                mCanvasLayout.deleteEmptyEditText();

                if (preNewPageTimeStamp < 0) {
                    preNewPageTimeStamp = 0;
                }
                curNewPageTimeStamp = System.currentTimeMillis();
                long gapTime = curNewPageTimeStamp - preNewPageTimeStamp;
                if (gapTime < 1000) {
//                    Toast.makeText(PageEditorActivity.this, "Your operation is too frequently!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    preNewPageTimeStamp = curNewPageTimeStamp;
                }

                EditorState.getInstance().setSavingFlag(true);

                setPageListState(false);

                if(mStrokeView != null){
                    mStrokeView.clearTwoFingerState();//去除缩放及平移状态
                    mStrokeView.deletePopwindows();
                }

                reloadDocumentBeforeSave();

                if (Workspace.getInstance().getCurrentDocument().exist(Workspace.getInstance().getCurrentPage())) {
                    savePage(Workspace.getInstance().getCurrentPage(), true);
                    EditorState.getInstance().setSavingFlag(true); // 会保存前一个Page的内容，这里需要为下一个Page设置保存标记
                }
//                Page operatedPage = new Page(TimeHelper.getCurrentDateTime());
//                Workspace.getInstance().getCurrentDocument().add(operatedPage);
                Page operatedPage = InformationParser.newPageInDocument(mContext, Workspace.getInstance().getCurrentDocument());
                Workspace.getInstance().setCurrentPage(operatedPage);
//                FileManager.CheckDir(Workspace.getInstance().getCurrentPagePath());

                clearCanvas();

                clearUndoDataOnNewPage();
            }

            @Override
            public void onPaintClick() {
                BaseApplication.getApplication().setPenKeyState(SmartpadManager.PEN_KEY_LED_STATE_ON);
                if (bottomView != null) {
                    setBottomViewVisibility(true);
                    bottomView.setFunctionAreaView(PageEditorBottomView.FUNCTION_PAINT);
                    EditorState.getInstance().setBottomViewState(EditorState.PANEL_PAINT);
                }
                if (mStrokeView != null) {
                    mStrokeView.setSelectMode(0);
                    mStrokeView.deletePopwindows();
                }

                if (EditorState.getInstance().getStrokeType() == EditorState.PAINT_ERASER) {
                    NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.ERASER);
                } else {
                    NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.STROKE);
                }

                mCanvasLayout.deleteEmptyEditText();
            }

            @Override
            public void onTextClick() {
                BaseApplication.getApplication().setPenKeyState(SmartpadManager.PEN_KEY_LED_STATE_OFF);
                if (bottomView != null) {
                    setBottomViewVisibility(true);
                    bottomView.setFunctionAreaView(PageEditorBottomView.FUNCTION_TEXT);
                    EditorState.getInstance().setBottomViewState(EditorState.PANEL_TEXT);
                }
                if (mStrokeView != null) {
                    mStrokeView.setSelectMode(0);
                    mStrokeView.deletePopwindows();
                    mStrokeView.clearTwoFingerState();//去除缩放及平移状态
                }
                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.TEXT);
            }

            @Override
            public void onPhotoClick() {
                dialogTakePic = new DialogTakePic(PageEditorActivity.this);
                dialogTakePic.setOnDialogTakePicListener(PageEditorActivity.this);
                dialogTakePic.setOnDismissListener(listener);
                BaseApplication.showImmersiveDialog(dialogTakePic, PageEditorActivity.this);
//                dialogTakePic.show();
                if (bottomView != null) {
                    setBottomViewVisibility(false);
                    setPageListState(false);
                }

                if (mStrokeView != null) {
                    mStrokeView.setSelectMode(0);
                    mStrokeView.deletePopwindows();
                    mStrokeView.clearTwoFingerState();//去除缩放及平移状态
                }
                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.PICTURE);
            }

            @Override
            public void onFrameClick() {
                View view = mCanvasLayout.findFocus();
                if(view != null && (view instanceof EditText || view instanceof ImageView)){
                    view.clearFocus();
                }

                if (bottomView != null) {
                    setBottomViewVisibility(false);
                    setPageListState(false);
                    EditorState.getInstance().setBottomViewState(EditorState.PANEL_GONE);
                }
                if (mStrokeView != null) {
                    mStrokeView.setSelectMode(1);
                    mStrokeView.clearTwoFingerState();//去除缩放及平移状态
                }

                mCanvasLayout.deleteEmptyEditText();
                BaseApplication.getApplication().setPenKeyState(SmartpadManager.PEN_KEY_LED_STATE_ON);
            }

            @Override
            public void onRotateClick() {
                View view = mCanvasLayout.findFocus();
                if(view != null && (view instanceof EditText || view instanceof ImageView)){
                    view.clearFocus();
                }

                rotationValueByButton = (int)(mCanvasLayout.getRotation() + 270) % 360;
                mCanvasLayout.setRotation(rotationValueByButton);
                canvasRotation = (int)mCanvasLayout.getRotation();
                addRecord(ACTION_ROTATION);

                if (mStrokeView != null) {
                    mStrokeView.deletePopwindows();
                }
            }

            @Override
            public void onLastClick() {
                if (mStrokeView != null) {
                    mStrokeView.deletePopwindows();
                }

                undo();
            }

            @Override
            public void onNextClick() {
                if (mStrokeView != null) {
                    mStrokeView.deletePopwindows();
                }
                redo();
            }

            @Override
            public void onShareClick() {
                View view = mCanvasLayout.findFocus();
                if(view != null && (view instanceof EditText || view instanceof ImageView)){
                    view.clearFocus();
                }
                if(mStrokeView != null){
                    mStrokeView.clearTwoFingerState();//去除缩放及平移状态
                    mStrokeView.deletePopwindows();
                }
                isFromSavePdf = true;//added by cuijc3
                BaseApplication.getApplication().setPenKeyState(SmartpadManager.PEN_KEY_LED_STATE_ON);
                saveCurrentPageData();
                isShareLocked = true;
                /*
                dialogShare = new DialogShare(PageEditorActivity.this, mContext.getString(R.string.ShareDialogTitle),
                        Workspace.getInstance().getCurrentPage().getThumbnailFilePath());
                dialogShare.show();
                */
                /*
                Intent shareIntent = new Intent();

                shareIntent.setAction(Intent.ACTION_SEND);

                shareIntent.putExtra(Intent.EXTRA_TEXT, "文字");

                shareIntent.setType("text/plain");

                startActivity(Intent.createChooser(shareIntent, getString(R.string.ShareDialogTitle)));
                */

                /*
                Intent shareIntent = new Intent();

                shareIntent.setAction(Intent.ACTION_SEND);

                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Workspace.getInstance().getCurrentPage().getThumbnailFilePath()));

                shareIntent.setType("image/*");

                startActivity(Intent.createChooser(shareIntent, getString(R.string.ShareDialogTitle)));

                */

                showSharePPWindow();
                setPageListState(false);
            }

            @Override
            public void onSettingClick() {
                View view = mCanvasLayout.findFocus();
                if(view != null && (view instanceof EditText || view instanceof ImageView)){
                    view.clearFocus();
                }
                if(mStrokeView != null){
                    mStrokeView.clearTwoFingerState();//去除缩放及平移状态
                    mStrokeView.deletePopwindows();
                }

                BaseApplication.getApplication().setPenKeyState(SmartpadManager.PEN_KEY_LED_STATE_ON);
                showSettingPPWindow();
                setPageListState(false);
//                showChangeBgDialog();
            }

        });
        undoButton = (ImageButton)topView.findViewById(R.id.ib_last);
        redoButton = (ImageButton)topView.findViewById(R.id.ib_next);
        updateUndoButtonState();
        updateRedoButtonState();
//        clearUndoData();
        fl_top_container.addView(topView);

        previewButton = (Button)topView.findViewById(R.id.previewButton);
        previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("wangkun", "onClick:................... ");
                //启动preView Activity
                Intent intent = new Intent(PageEditorActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });

//        mHorizontalPageList = new HorizontalPageList(mContext);
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mPageListView = new LandscapePageList(mContext);
        } else {
            mPageListView = new PortablePageList(mContext);
        }
//        mListAdapter = new GalleryListViewAdapter(mContext, Workspace.getInstance().getCurrentDocument().getPages());
//        mPageListView.setPageListAdapter(mListAdapter);
//        mPageListView.setPageListData(Workspace.getInstance().getCurrentDocument().getPages());
//        mPageListView.setPageListData(Workspace.getInstance().getCurrentDocument().getPages());
        mPageListView.getChildAt(0).setBackgroundColor(getColor(R.color.pageListBgInPageEditor));
        mPageListView.setOnFocusChangedListener(new OnFocusItemChangedListener() {
            @Override
            public void onNewItemFocused(int newPosition) {
                LogUtil.e("LoadPage==============>" + newPosition);
                Document currentDocument = Workspace.getInstance().getCurrentDocument();
                if (currentDocument.getPages().size() < 1) {
                    return;
                }
                if (newPosition < 1) {
                    newPosition = 1;
                } else if (newPosition > currentDocument.getPages().size()) {
                    newPosition = currentDocument.getPages().size();
                }
                Page page = currentDocument.getPage(newPosition - 1);
                Workspace.getInstance().setCurrentPage(page);
                updateEditorWithPageData(page);
            }

            @Override
            public void onListContentChanged(List<Page> newList) {
                Workspace.getInstance().getCurrentDocument().setPages(newList);
                Workspace.getInstance().getCurrentDocument().save();
//                mHorizontalPageList.checkFocusByChild();
                mPageListView.setPageListData(newList);
                int focusPosition = mPageListView.getFocusPosition();
                if (focusPosition >= newList.size()) {
                    if (newList.size() == 1) { // 这里修复了当只有一个Page时出现拖动导致FC的漏洞
                        focusPosition = 1;
                    } else {
                        focusPosition = newList.size();
//                        focusPosition = newList.size() - 1;
                    }
                } else if (focusPosition < 1) {
                    focusPosition = 1;
                }
                LogUtil.e("FindIndex========focusPosition======>" + focusPosition);
                Page page = newList.get(focusPosition - 1);
                Workspace.getInstance().setCurrentPage(page);
                updateEditorWithPageData(page);
            }
        });
        flListContainer.addView(mPageListView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        bottomView.setOnShowClickListener(new PageEditorBottomView.OnShowClickListener() {
            @Override
            public void onShowClick(boolean isShow) {
                saveCurrentPageData();
                setPageListState(isShow);
            }
        });

        fl_bottom_container.addView(bottomView);

        mCanvasLayout = (CanvasLayout) findViewById(R.id.canvas);
        mStrokeView = (NoteStrokeView) findViewById(R.id.stroke_view);
        NoteParams.getCurrentPenNoteParams().addObserver(mCanvasLayout);
        NoteParams.getCurrentPenNoteParams().addObserver(mStrokeView);
        mCanvasLayout.setRemoveListener(removeListener);
        mStrokeView.setZoomCanvasListener(zoomListener);
        mStrokeView.setSelectedRectListener(selectedRectListener);
        mStrokeView.setHideToolBarListener(hideListener);
        mStrokeView.setClearFocusListener(clearFocusListener);
        mStrokeView.setOnStartTouchStrokeViewListener(new StrokeView.StartTouchStrokeViewListener() {
            @Override
            public boolean onStartTouchStrokeView() {
                if (flListContainer.getVisibility() == View.VISIBLE) {
                    setPageListState(false);
                    return true;
                } else {
                    return false;
                }
            }
        });
        mStrokeView.isCanPaste = isCanPaste;
        bottomView.setCanvasLayout(mCanvasLayout);

        mZoomWindow = (ImageView) findViewById(R.id.view_zoom_window);
        mZoomVisibleWindow = (ImageView) findViewById(R.id.view_zoom_visible_window);

    }

    public void setPageListState(boolean isShown) {
        View view = mCanvasLayout.findFocus();
        if(view != null && ( view instanceof ImageView)){
            view.clearFocus();
        }

        if (isShown) {
            flListContainer.setVisibility(View.VISIBLE);
            List<Page> pages = Workspace.getInstance().getCurrentDocument().getPages();
            mPageListView.setPageListData(pages);
            Page currentPage = Workspace.getInstance().getCurrentPage();
            int index = -1;
            for (int i = 0; i < pages.size(); i++) {
                if (pages.get(i).getPath().equals(currentPage.getPath())) {
                    index = i;
                    break;
                }
            }
            LogUtil.e("FindIndex==============>" + index);
            if (index < 0) {
                index = pages.size() - 1;
            }
            mPageListView.moveToSpecificPosition(index + 1);
        } else {
            flListContainer.setVisibility(View.INVISIBLE);
        }

        if (bottomView != null) {
            bottomView.setPageListState(isShown);
        }
    }

    private void showSettingPPWindow() {
        if (ppwSettings == null) {
            settingView = LayoutInflater.from(mContext).inflate(R.layout.ppw_editor_setting, null);
            ppwSettings = new PopupWindow(settingView, 400, 380);
            ppwSettings.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    BaseApplication.setSystemUiVisibility(sActivity, true);
                }
            });
        }
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tv_setting_background:
                        showChangeBgDialog();
                        break;
                    case R.id.ll_setting_open_position:
                        DialogSetLocationPage dialogSetLocationPage = new DialogSetLocationPage(PageEditorActivity.this);
                        BaseApplication.showImmersiveDialog(dialogSetLocationPage, PageEditorActivity.sActivity);
//                        dialogSetLocationPage.show();
//                        Toast.makeText(PageEditorActivity.this, "Ask", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tv_setting_help:
                        BaseApplication.showHelpActivity();
//                        Intent intentHelp = new Intent(BaseApplication.getContext(), HelpActivity.class);
////                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intentHelp);
                        break;
                    case R.id.tv_setting_about:
//                        Toast.makeText(PageEditorActivity.this, "版本更新时间：2016年5月27日 11:52:27", Toast.LENGTH_SHORT).show();
                        Intent intentAbout = new Intent(BaseApplication.getApplication(), AboutActivity.class);
                        startActivity(intentAbout);
                        break;
                    default:
                        break;
                }
                hidePopupWindow();
            }
        };
        settingView.findViewById(R.id.tv_setting_background).setOnClickListener(listener);
        settingView.findViewById(R.id.ll_setting_open_position).setOnClickListener(listener);
        settingView.findViewById(R.id.tv_setting_help).setOnClickListener(listener);
        settingView.findViewById(R.id.tv_setting_about).setOnClickListener(listener);
        settingView.setOnClickListener(listener);
        TextView tvLocateInfo = (TextView) settingView.findViewById(R.id.tv_locate_info);
        int locatePosition = BaseApplication.getAppSharePreferences().getInt(UIConstants.LOCATE_PAGE_POSITION, 1);
        if (locatePosition == 1) {
            tvLocateInfo.setText(mContext.getString(R.string.JumpToEnd));
        } else {
            tvLocateInfo.setText(mContext.getString(R.string.JumpToLatest));
        }
//        ppwSettings.setFocusable(true); // 当可以获取焦点的时候底部状态栏会弹出，这里改为默认的false
        ppwSettings.setOutsideTouchable(true);

        ppwSettings.showAtLocation(topView, Gravity.RIGHT | Gravity.TOP, 16, 56);
    }

    private void hidePopupWindow() {
        if (ppwSettings != null && ppwSettings.isShowing()) {
            ppwSettings.dismiss();
        }
    }


    private void showSharePPWindow() {
        if (ppwShareMenu == null) {
            shareMenuView = LayoutInflater.from(mContext).inflate(R.layout.share_menu, null);
            ppwShareMenu = new PopupWindow(shareMenuView, 400, 200);
            ppwShareMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    BaseApplication.setSystemUiVisibility(sActivity, true);
                }
            });
        }


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tv_share_menu_save_as_pdf:
                        /**start modifided by cuijc3
                         dialogShare = new DialogShare(PageEditorActivity.this, mContext.getString(R.string.ShareDialogTitle),
                         Workspace.getInstance().getCurrentPage().getThumbnailFilePath());


                         dialogShare.ShareSaveAsPDF(PageEditorActivity.this, mContext.getString(R.string.ShareDialogTitle),
                         Workspace.getInstance().getCurrentPage().getThumbnailFilePath());
                         */
                        dialogShare = new DialogShare(PageEditorActivity.this, mContext.getString(R.string.ShareDialogTitle),
                                Workspace.getInstance().getCurrentPage().getPdfOriginalImageFilePath());


                        dialogShare.ShareSaveAsPDF(PageEditorActivity.this, mContext.getString(R.string.ShareDialogTitle),
                                Workspace.getInstance().getCurrentPage().getPdfOriginalImageFilePath());
                        //end modified by cuijc3
                        break;
                    case R.id.tv_share_menu_share_to:
                        Intent shareIntent = new Intent();

                        shareIntent.setAction(Intent.ACTION_SEND);


                        Page currentPage = Workspace.getInstance().getCurrentPage();
                        if (currentPage == null) {
                            break;
                        }
                        //String thumbnailFilePath = currentPage.getThumbnailFilePath();
                        String thumbnailFilePath = currentPage.getPdfOriginalImageFilePath();//modified by cuijc3
                        if(thumbnailFilePath == null)
                        {
                            Document currentDocument = Workspace.getInstance().getCurrentDocument();
                            currentPage.setOwner(currentDocument);
                            //thumbnailFilePath = currentPage.getThumbnailFilePath();
                            thumbnailFilePath = currentPage.getPdfOriginalImageFilePath();
                        }
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + thumbnailFilePath));

                        //shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Workspace.getInstance().getCurrentPage().getThumbnailFilePath()));

                        shareIntent.setType("image/*");

                        try
                        {
                            startActivity(Intent.createChooser(shareIntent, getString(R.string.ShareDialogTitle)));
                        }
                        catch (android.content.ActivityNotFoundException ex)
                        {
                            Logger.i("Can't found activity");
                        }

                        /*
                        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(shareIntent, 0);
                        if (!resInfo.isEmpty()) {
                            List<Intent> targetedShareIntents = new ArrayList<Intent>();
                            for (ResolveInfo info : resInfo) {
                                Intent targeted = new Intent(Intent.ACTION_SEND);
                                targeted.setType("image/*");
                                ActivityInfo activityInfo = info.activityInfo;

                                // judgments : activityInfo.packageName, activityInfo.name, etc.
                                //if (activityInfo.packageName.contains("bluetooth")) {
                                //    continue;
                                //}
                                targeted.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Workspace.getInstance().getCurrentPage().getThumbnailFilePath()));
                                targeted.setPackage(activityInfo.packageName);
                                targetedShareIntents.add(targeted);
                            }
                            Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), getString(R.string.ShareDialogTitle));
                            if (chooserIntent == null) {
                                return;
                            }
                            // A Parcelable[] of Intent or LabeledIntent objects as set with
                            // putExtra(String, Parcelable[]) of additional activities to place
                            // a the front of the list of choices, when shown to the user with a
                            // ACTION_CHOOSER.
                            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[] {}));
                            try {
                                startActivity(chooserIntent);
                            } catch (android.content.ActivityNotFoundException ex) {
                                Logger.i("Can't found activity");
                            }
                        }
                        */
                        break;
                    default:
                        break;
                }
                hideShareMenuPopupWindow();
            }
        };
        shareMenuView.findViewById(R.id.tv_share_menu_save_as_pdf).setOnClickListener(listener);
        shareMenuView.findViewById(R.id.tv_share_menu_share_to).setOnClickListener(listener);
        shareMenuView.setOnClickListener(listener);

        ppwShareMenu.setFocusable(false);
        ppwShareMenu.setOutsideTouchable(true);

        ppwShareMenu.showAtLocation(topView, Gravity.RIGHT | Gravity.TOP, 16, 56);
    }

    private void hideShareMenuPopupWindow() {
        if (ppwShareMenu != null && ppwShareMenu.isShowing()) {
            ppwShareMenu.dismiss();
        }
    }
////////////////////////////////////////////////////////////////////////////////////////



    /**
     * 先保存数据
     */
    private void savingDataAndClose() {
        Runtime.getRuntime().gc();
        System.runFinalization();
        EditorState.getInstance().resetDefaultValues();
        ConstantValues.getInstance().releaseStrokeViewArray();
        Intent intent = new Intent(mContext, ShowPagesActivity.class);
        startActivity(intent);
        finish();
    }



    /**
     * 结束当前进程
     */
//    private void killMyself() {
//        Intent MyIntent = new Intent(Intent.ACTION_MAIN);
//        MyIntent.addCategory(Intent.CATEGORY_HOME);
//        startActivity(MyIntent);
//        finish();
//        android.os.Process.killProcess(android.os.Process.myPid());
//    }

    /**
     * 获取当前屏幕旋转角度
     * @return 0表示是竖屏; 90表示是左横屏; 180表示是反向竖屏; 270表示是右横屏
     */
    public void getDisplayRotation() {
        WindowManager mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        if (mWindowManager != null) {
            DisplayMetrics metric = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(metric);
            screenWidth = metric.widthPixels;
            screenHeight = metric.heightPixels;

            int rotation = mWindowManager.getDefaultDisplay().getRotation();
            //begin by cuishuo1
            //从page.xml中获取mPageRotation，判断纸张方向并进行纸张的旋转
            int start_rotation = -1;
            int end_rotation = -1;
            boolean reverse_rotation = false;
            if(mPageRotation != null) {
                start_rotation = mPageRotation.startRotation;
                end_rotation = mPageRotation.endRotation;
                if(rotation == start_rotation) {
                    if(mPageRotation.reverseRotation) {
                        EditorState.getInstance().setSavingFlag(true);
                    }
                    mPageRotation.reverseRotation = false;
                } else if(rotation == end_rotation) {
                    if(!mPageRotation.reverseRotation) {
                        EditorState.getInstance().setSavingFlag(true);
                    }
                    mPageRotation.reverseRotation = true;
                }
                reverse_rotation = mPageRotation.reverseRotation;
            }
            LogUtil.i("getDisplayRotation start_rotation = "+start_rotation);
            LogUtil.i("getDisplayRotation end_rotation = "+end_rotation);
            LogUtil.i("getDisplayRotation reverse_rotation = "+reverse_rotation);

            switch (rotation) {
                case Surface.ROTATION_0:
                    if(rotation == end_rotation) {
                        if(mCanvasLayout != null) {
                            mCanvasLayout.setRotation(270);
                            mCanvasLayout.setTranslationX(-360);
                            mCanvasLayout.setTranslationY(360);
                            rotationValueByHand = 270;
                        }
                    } else {
                        if(reverse_rotation) {
                            if(mCanvasLayout != null) {
                                mCanvasLayout.setRotation(270);
                                mCanvasLayout.setTranslationX(-360);
                                mCanvasLayout.setTranslationY(360);
                                rotationValueByHand = 270;
                            }
                        } else {
                            if(mCanvasLayout != null) {
                                mCanvasLayout.setRotation(90);
                                mCanvasLayout.setTranslationX(-360);
                                mCanvasLayout.setTranslationY(360);
                                rotationValueByHand = 90;
                            }
                        }
                    }
//                    thumbnailRotation = 180;
                    break;
                case Surface.ROTATION_90:
                    if(rotation == end_rotation) {
                        if(mCanvasLayout != null) {
                            mCanvasLayout.setRotation(180);
                            rotationValueByHand = 180;
                        }
                    } else {
                        if(reverse_rotation) {
                            if(mCanvasLayout != null) {
                                mCanvasLayout.setRotation(180);
                                rotationValueByHand = 180;
                            }
                        } else {
                            if(mCanvasLayout != null) {
                                mCanvasLayout.setRotation(0);
                                rotationValueByHand = 0;
                            }
                        }
                    }
//                    thumbnailRotation = 0;
                    break;
                case Surface.ROTATION_180:
                    if(rotation == end_rotation) {
                        if(mCanvasLayout != null) {
                            mCanvasLayout.setRotation(90);
                            mCanvasLayout.setTranslationX(-360);
                            mCanvasLayout.setTranslationY(360);
                            rotationValueByHand = 90;
                        }
                    } else {
                        if(reverse_rotation) {
                            if(mCanvasLayout != null) {
                                mCanvasLayout.setRotation(90);
                                mCanvasLayout.setTranslationX(-360);
                                mCanvasLayout.setTranslationY(360);
                                rotationValueByHand = 90;
                            }
                        } else {
                            if(mCanvasLayout != null) {
                                mCanvasLayout.setRotation(270);
                                mCanvasLayout.setTranslationX(-360);
                                mCanvasLayout.setTranslationY(360);
                                rotationValueByHand = 270;
                            }
                        }
                    }
//                    thumbnailRotation = 0;
                    break;
                case Surface.ROTATION_270:
                    if(rotation == end_rotation) {
                        if(mCanvasLayout != null){
                            mCanvasLayout.setRotation(0);
                            rotationValueByHand = 0;
                        }
                    } else {
                        if(reverse_rotation) {
                            if(mCanvasLayout != null){
                                mCanvasLayout.setRotation(0);
                                rotationValueByHand = 0;
                            }
                        } else {
                            if(mCanvasLayout != null){
                                mCanvasLayout.setRotation(180);
                                rotationValueByHand = 180;
                            }
                        }
                    }
//                    thumbnailRotation = 0;
                    break;
            }
            //end by cuishuo1

            canvasRotation = (int)mCanvasLayout.getRotation();
        }
    }


    /**
     * 设置底部视图是否可见
     *
     * @param isShow
     */
    private void setBottomViewVisibility(boolean isShow) {
        if (isShow) {
            fl_bottom_container.setVisibility(View.VISIBLE);
        } else {
            fl_bottom_container.setVisibility(View.GONE);
        }
    }

    private void setTopViewVisibility(boolean isShow) {
        if (isShow) {
            fl_top_container.setVisibility(View.VISIBLE);
        } else {
            fl_top_container.setVisibility(View.GONE);
        }
    }

    private Bitmap adjustBitmapRotation(Bitmap bitmap, final int orientationDegree) {
        Matrix matrix = new Matrix();
        matrix.setRotate(orientationDegree, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

        try {
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return newBitmap;
        } catch (OutOfMemoryError ex) {
        }
        return null;
    }

    private CanvasLayout.RemoveElementsListener removeListener = new CanvasLayout.RemoveElementsListener() {
        @Override
        public void onRemoveElementByPoint(int x, int y) {
            mStrokeView.requestFocus();
            mStrokeView.deleteStrokeByPoint(x, y);
        }
    };

    private StrokeView.ZoomCanvasListener zoomListener = new StrokeView.ZoomCanvasListener() {
        @Override
        public void displayZoomWindow(boolean isDisplay, Bitmap screenBitmap, float rate, float translateX, float translateY) {
            Bitmap bitmap = null;
            if (isDisplay) {
                mZoomWindow.setVisibility(View.VISIBLE);
                mZoomVisibleWindow.setVisibility(View.VISIBLE);

                bitmap = adjustBitmapRotation(screenBitmap, (int)mCanvasLayout.getRotation());
                if(bitmap != null){
                    mZoomWindow.setBackground(new BitmapDrawable(bitmap));
                }

                AbsoluteLayout.LayoutParams zoomParam = (AbsoluteLayout.LayoutParams)mZoomWindow.getLayoutParams();
                AbsoluteLayout.LayoutParams zoomVisibleParam = (AbsoluteLayout.LayoutParams)mZoomVisibleWindow.getLayoutParams();

                if(mCanvasLayout.getRotation() == 0){
                    zoomParam.x = 0;
                    zoomParam.y = 144;
                    zoomParam.width = 384;
                    zoomParam.height = 240;

                    zoomVisibleParam.x = (int)(75 + (192 - 192 / rate) - translateX * 0.2);
                    zoomVisibleParam.y = (int)(219 + (120 - 120 / rate) - translateY * 0.2);
                    zoomVisibleParam.width = (int)(384 / rate);
                    zoomVisibleParam.height = (int)(240 / rate);
                } else if (mCanvasLayout.getRotation() == 90){
                    zoomParam.x = 144;
                    zoomParam.y = 0;
                    zoomParam.width = 240;
                    zoomParam.height = 384;

                    zoomVisibleParam.x = (int)(219 + (120 - 120 / rate) + translateY * 0.2);
                    zoomVisibleParam.y = (int)(75 + (192 - 192 / rate) - translateX * 0.2);
                    zoomVisibleParam.width = (int)(240 / rate);
                    zoomVisibleParam.height = (int)(384 / rate);
                } else if (mCanvasLayout.getRotation() == 180){
                    zoomParam.x = 0;
                    zoomParam.y = 144;
                    zoomParam.width = 384;
                    zoomParam.height = 240;

                    zoomVisibleParam.x = (int)(75 + (192 - 192 / rate) + translateX * 0.2);
                    zoomVisibleParam.y = (int)(219 + (120 - 120 / rate) + translateY * 0.2);
                    zoomVisibleParam.width = (int)(384 / rate);
                    zoomVisibleParam.height = (int)(240 / rate);
                } else if (mCanvasLayout.getRotation() == 270){
                    zoomParam.x = 144;
                    zoomParam.y = 0;
                    zoomParam.width = 240;
                    zoomParam.height = 384;

                    zoomVisibleParam.x = (int)(219 + (120 - 120 / rate) - translateY * 0.2);
                    zoomVisibleParam.y = (int)(75 + (192 - 192 / rate) + translateX * 0.2);
                    zoomVisibleParam.width = (int)(240 / rate);
                    zoomVisibleParam.height = (int)(384 / rate);
                }

                mZoomWindow.setLayoutParams(zoomParam);
                mZoomVisibleWindow.setLayoutParams(zoomVisibleParam);
            } else {
                recycleBitmap(bitmap);
                mZoomWindow.setVisibility(View.GONE);
                mZoomVisibleWindow.setVisibility(View.GONE);
            }
        }
    };

    Rect rect;
    LinkedList<Stroke> copyStrokeList;
    List<Object> copyElementList;
    private StrokeView.SelectedRectListener selectedRectListener = new StrokeView.SelectedRectListener() {
        @Override
        public void copySelectedRect() {
            rect = mStrokeView.getSelectedRect();
            copyStrokeList = mStrokeView.getSelectedStrokeList(rect);
            copyElementList = mCanvasLayout.getSelectedElementList(rect);
        }

        @Override
        public void cutSelectedRect() {
            rect = mStrokeView.getSelectedRect();
            copyStrokeList = mStrokeView.getSelectedStrokeList(rect);
            copyElementList = mCanvasLayout.getSelectedElementList(rect);

            if (copyStrokeList.size() == 0 && copyElementList.size() == 0) {
                return;
            }

            addRecord(ACTION_CUT);
            mSelectedStrokeList = copyStrokeList;
            mSelectedElementList = copyElementList;
            mStrokeView.removeSelectedStrokeList(mStrokeView.getSelectedStrokeList(rect));
            mCanvasLayout.removeSelectedElementList(mCanvasLayout.getSelectedElementList(rect));
            mUndoSelectedRecords.add(listAddList(mSelectedElementList, mSelectedStrokeList));
            mRedoSelectedRecords.clear();
        }

        @Override
        public void deleteSelectedRect() {
            rect = mStrokeView.getSelectedRect();
            mSelectedStrokeList = mStrokeView.getSelectedStrokeList(rect);
            mSelectedElementList = mCanvasLayout.getSelectedElementList(rect);

            if (mSelectedStrokeList.size() == 0 && mSelectedElementList.size() == 0) {
                return;
            }

            addRecord(ACTION_DELETE);
            mStrokeView.removeSelectedStrokeList(mStrokeView.getSelectedStrokeList(rect));
            mCanvasLayout.removeSelectedElementList(mCanvasLayout.getSelectedElementList(rect));
            mUndoSelectedRecords.add(listAddList(mSelectedElementList, mSelectedStrokeList));
            mRedoSelectedRecords.clear();
        }

        @Override
        public void pasteSelectedRect() {
            if (copyStrokeList.size() == 0 && copyElementList.size() == 0) {
                return;
            }

            addRecord(ACTION_PASTE);
            Point pastePoint = mStrokeView.getPasteStartPoint();
            mSelectedStrokeList = mStrokeView.pasteSelectedStrokeList(rect, copyStrokeList);
            mSelectedElementList = mCanvasLayout.pasteSelectedElementList(pastePoint, rect, copyElementList);
            mUndoSelectedRecords.add(listAddList(mSelectedElementList, mSelectedStrokeList));
            mRedoSelectedRecords.clear();
        }
    };

    private StrokeView.HideToolBarListener hideListener = new StrokeView.HideToolBarListener() {
        @Override
        public void hideTopToolBar(boolean hideFlag) {
            if (hideFlag) {
                if (topView != null) {
                    setTopViewVisibility(false);
                }
            } else {
                if (topView != null) {
                    setTopViewVisibility(true);
                }
            }
        }

        @Override
        public void hideBottomToolBar(boolean hideFlag) {
            if (hideFlag) {
                if (bottomView != null) {
                    setBottomViewVisibility(false);
                }
                setPageListState(false);

            } else {
                if (bottomView != null) {
                    setBottomViewVisibility(true);
                }
            }
        }
    };

    private StrokeView.ClearFocusListener clearFocusListener = new StrokeView.ClearFocusListener() {
        @Override
        public void clearFocusListener() {
            View view = mCanvasLayout.findFocus();
            if(view != null && (view instanceof EditText || view instanceof ImageView)){
                view.clearFocus();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UIConstants.TAKE_PHOTO:
//                isAddNewData = true;
                if (currentPicPath != null) {
                    File pictureFile = new File(currentPicPath);
                    if (pictureFile.exists()) {
                        Bitmap bmp = BitmapUtil.decodeFileBySize(currentPicPath, 1000, 1000);
                        if (bmp != null) {
                            int finalWidth;
                            int finalHeight;
                            int actualWidth = bmp.getWidth();
                            int actualHeight = bmp.getHeight();
                            if (actualWidth < actualHeight) {
                                finalWidth = 400;
                                finalHeight = 400 * actualHeight / actualWidth;
                            } else {
                                finalWidth = 400 * actualWidth / actualHeight;
                                finalHeight = 400;
                            }

                            mCanvasLayout.addNotePicture(currentPicPath, finalWidth, finalHeight);
                        }
                    }
                }
                break;
            case UIConstants.CHOSE_IMAGE:
//                isAddNewData = true;
                if (data != null) {
                    Uri originalUri = data.getData();
                    String originalPath = GalleryUtil.getPath(PageEditorActivity.this, originalUri);
                    currentPicPath = FileManager.getSDCardPath() + "/hanvon/com.hanvon.virtualpage/temp/";
                    FileManager.CheckDir(currentPicPath);
                    String path = currentPicPath + new File(originalPath.trim()).getName();
                    FileManagerUtils.copyFile(originalPath, path);
                    LogUtil.e("onActivityResult:path \t " + path);
                    if (path != null) {
                        File pictureFile = new File(path);
                        if (pictureFile.exists()) {
                            Bitmap bmp = BitmapUtil.decodeFileBySize(path, 1000, 1000);
                            if (bmp != null) {
                                int finalWidth;
                                int finalHeight;
                                int actualWidth = bmp.getWidth();
                                int actualHeight = bmp.getHeight();
                                if (actualWidth < actualHeight) {
                                    finalWidth = 400;
                                    finalHeight = 400 * actualHeight / actualWidth;
                                } else {
                                    finalWidth = 400 * actualWidth / actualHeight;
                                    finalHeight = 400;
                                }

                                mCanvasLayout.addNotePicture(path, finalWidth, finalHeight);
                            }
                        }
                    }
                }
                break;
            case REQUEST_CODE:
//                isAddNewData = false;
                if (resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
                    LogUtil.i("loadData", "权限不够，直接关闭");
                    BaseApplication.setHasPermission(false);
                    BaseApplication.getApplication().AppExit();
                }
                BaseApplication.setHasPermission(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_chose_image:
                isAddNewData = true;
                dialogTakePic.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                try
                {
                    startActivityForResult(intent, UIConstants.CHOSE_IMAGE);
                }
                catch (android.content.ActivityNotFoundException ex)
                {
                    Logger.i("Can't found activity");
                }

                break;
            case R.id.ll_take_photo:
                isAddNewData = true;
                if (ContextCompat.checkSelfPermission(sActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "No Permission to Camera!", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(sActivity, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
                } else {
                    dialogTakePic.dismiss();
                    startActivityForPhoto();
                }
                break;
        }
    }

    private void startActivityForPhoto() {
        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        currentPicPath = FileManager.getSDCardPath() + "/hanvon/com.hanvon.virtualpage/temp/";
        FileManager.CheckDir(currentPicPath);
        File f = new File(currentPicPath + TimeHelper.getCurrentDateTime() + ".png");
        currentPicPath = f.getAbsolutePath();
        Uri uri = Uri.fromFile(f);
        intent1.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        try
        {
            startActivityForResult(intent1, UIConstants.TAKE_PHOTO);
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            Logger.i("Can't found activity");
        }
    }

    public DialogInterface.OnDismissListener listener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            //if (StateBeforePicture == StateType.STROKE) {
            switch (EditorState.getInstance().getTopViewState()) {
                case EditorState.FUNCTION_PAINT:
                    if (bottomView != null) {
                        setBottomViewVisibility(true);
                        bottomView.setFunctionAreaView(PageEditorBottomView.FUNCTION_PAINT);
                    }
                    if (mStrokeView != null) {
                        mStrokeView.setSelectMode(0);
                    }
                    if (EditorState.getInstance().getStrokeType() == EditorState.PAINT_ERASER) {
                        NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.ERASER);
                    } else {
                        NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.STROKE);
                    }
                    break;
                case EditorState.FUNCTION_TEXT:
                    NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.TEXT);
                    if (bottomView != null) {
                        setBottomViewVisibility(true);
                        bottomView.setFunctionAreaView(PageEditorBottomView.FUNCTION_TEXT);
                    }
                    if (mStrokeView != null) {
                        mStrokeView.setSelectMode(0);
                    }
                    break;
                case EditorState.FUNCTION_FRAME:
                    if (mStrokeView != null) {
                        mStrokeView.setSelectMode(1);
                    }
                    if (bottomView != null) {
                        setBottomViewVisibility(false);
                    }
                    break;
                default:
                    break;

            }
//            if(EditorState.getInstance().getTopViewState() == EditorState.FUNCTION_PAINT){
//                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.STROKE);
//                if (bottomView != null) {
//                    setBottomViewVisibility(true);
//                    bottomView.setFunctionAreaView(PageEditorBottomView.FUNCTION_PAINT);
//                }
//                if (mStrokeView != null) {
//                    mStrokeView.setSelectMode(0);
//                }
//            }
//            if (StateBeforePicture == StateType.ERASER) {
//                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.ERASER);
//                if (bottomView != null) {
//                    setBottomViewVisibility(true);
//                    bottomView.setFunctionAreaView(PageEditorBottomView.FUNCTION_PAINT);
//                }
//                if (mStrokeView != null) {
//                    mStrokeView.setSelectMode(0);
//                }
//            }
//            if (StateBeforePicture == StateType.TEXT) {
//                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.TEXT);
//                if (bottomView != null) {
//                    setBottomViewVisibility(true);
//                    bottomView.setFunctionAreaView(PageEditorBottomView.FUNCTION_TEXT);
//                }
//                if (mStrokeView != null) {
//                    mStrokeView.setSelectMode(0);
//                }
//            }
//            if (StateBeforePicture == StateType.SELECT) {
//                if (mStrokeView != null) {
//                    mStrokeView.setSelectMode(1);
//                }
//                if (bottomView != null) {
//                    setBottomViewVisibility(false);
//                }
//            }
        }
    };



    private synchronized void savePage(final Page page, boolean async) {
        LogUtil.e("Save Page start.");
        // 如果page中没有数据，就不保存page
//        if (page != null && (mStrokeView.getStrokeList().size() > 0 || mCanvasLayout.getElementList().size() > 0)) {
//        if (mStrokeView.getStrokeList().size() == 0 || mCanvasLayout.getElementList().size() == 0) {
//            return;
//
        /**modified by cuijc3
         if (EditorState.getInstance().isSavingFlag() == false) {
         Logger.i("No changes have done, save operation canceled!");
         return;
         } else {
         Logger.e("Saving operation will be done soon!");
         EditorState.getInstance().setSavingFlag(false);
         }*/
        boolean isNotSavingFlagWithoutPdf = false;
        if (EditorState.getInstance().isSavingFlag() == false) {
            if (isFromSavePdf) {//判断未更新page时点击savepdf，pdf文件夹是否存在,如果存在就不再保存bitmap
                try {
                    if (new File(page.getPdfOriginalImageFilePath()).exists()) {
                        LogUtil.e("cuijc3", "===savePage()===page.getPdfOriginalImageFilePath()).exists()==true");
                        isFromSavePdf = false;
                        return;
                    }
                } catch (Exception e) {
                    isNotSavingFlagWithoutPdf = true;
                    e.printStackTrace();
                }
            } else {
                Logger.i("No changes have done, save operation canceled!");
                isFromSavePdf = false;
                return;
            }
        } else {
            Logger.e("Saving operation will be done soon!");
            EditorState.getInstance().setSavingFlag(false);
        }
        Logger.i("savePage执行保存操作了");
        try {
            if (page != null) {
                page.setTitle("");
                if (!isNotSavingFlagWithoutPdf){//added by cuijc3 ensure when there is no new operation of edit,and no pdf file exists,do not update time
                    page.setUpdatedTime(TimeHelper.getCurrentDateTime());
                }
                if(TextUtils.isEmpty(page.getUpdatedTime())){//added by cuijc3 in case of updatetime is null in some case
                    page.setUpdatedTime(TimeHelper.getCurrentDateTime());
                }
                page.setBackgroundResIndex(String.valueOf(currentBgIndex));
                //begin by cuishuo1
                //设置当前page的方向用于保存
                if(mPageRotation != null) {
                    LogUtil.i("savePage start_rotation = "+mPageRotation.startRotation);
                    LogUtil.i("savePage end_rotation = "+mPageRotation.endRotation);
                    LogUtil.i("savePage reverse_rotation = "+mPageRotation.reverseRotation);
                    page.setPageRotation(mPageRotation);
                }
                //end by cuishuo1
                LinkedList<Stroke> stroke_list = mStrokeView.getStrokeList();
                List<Object> canvas_list = mCanvasLayout.getElementList();
                if (stroke_list != null) { // 添加一个数据保护：因为锁屏旋屏之后，获取到的数据是null，保存之后，再次读取会导致程序崩溃；目前还不确定为何为空
                    page.setStrokeList(stroke_list);
                }
                if (canvas_list != null) {
                    page.setElementList(canvas_list);
                }
//                if (mStrokeView.getStrokeList() != null) { // 添加一个数据保护：因为锁屏旋屏之后，获取到的数据是null，保存之后，再次读取会导致程序崩溃；目前还不确定为何为空
//                    page.setStrokeList(mStrokeView.getStrokeList());
//                }
//                if (mCanvasLayout.getElementList() != null) {
//                    page.setElementList(mCanvasLayout.getElementList());
//                }
                if (mCanvasLayout.isDrawingCacheEnabled() == false) {
                    mCanvasLayout.setDrawingCacheEnabled(true);
                }
                Bitmap drawingCache = mCanvasLayout.getDrawingCache(true);
                //start added by cuijc3
                LogUtil.e("cuijc3", "isFromSavePdf::" + isFromSavePdf);
                if (isFromSavePdf) {
                    Bitmap padfUsageBitmap = ThumbnailUtils.extractThumbnail(drawingCache, drawingCache.getWidth(), drawingCache.getHeight());
                    padfUsageBitmap = BitmapUtil.adjustPhotoRotation(padfUsageBitmap, 90);
                    try {
                        boolean isSavePdfImageSuccessfully = page.savePdfImageImmediately(padfUsageBitmap);
                        LogUtil.e("cuijc3", "isFromSavePdf::" + isFromSavePdf + "isSavePdfImageSuccessfully::" + isSavePdfImageSuccessfully);
                    } catch (Exception e) {
                        LogUtil.e("cuijc3", "save pdfimagefile error!" + e.toString());
                    }
                    BaseApplication.scanPhoto(page.getPdfOriginalImageFilePath());
                }
                //end added by cuijc3
                LogUtil.e("size", drawingCache.getWidth() + "<--width, height-->" + drawingCache.getHeight());
//                Bitmap bitmap = ThumbnailUtils.extractThumbnail(drawingCache, 640, 379, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                Bitmap bitmap = drawingCache;
                //begin by cuishuo1
                //根据纸张方向对缩略图进行旋转保存
                //bitmap = BitmapUtil.adjustPhotoRotation(bitmap, -90);
                if(mPageRotation != null) {
                    switch (mPageRotation.startRotation) {
                        case Surface.ROTATION_0:
                            bitmap = BitmapUtil.adjustPhotoRotation(bitmap, 90);
                            break;
                        case Surface.ROTATION_90:
                            bitmap = BitmapUtil.adjustPhotoRotation(bitmap, -90);
                            break;
                        case Surface.ROTATION_180:
                            bitmap = BitmapUtil.adjustPhotoRotation(bitmap, -90);
                            break;
                        case Surface.ROTATION_270:
                            bitmap = BitmapUtil.adjustPhotoRotation(bitmap, 90);
                            break;
                    }
                }
                //end by cuishuo1
//                bitmap = BitmapUtil.adjustPhotoRotation(bitmap, -90 + thumbnailRotation);
//                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    bitmap = BitmapUtil.adjustPhotoRotation(bitmap, 90);
//                } else {
//                    bitmap = BitmapUtil.adjustPhotoRotation(bitmap, -90);
//                }
                page.saveThumbnailImmediately(bitmap);

                BaseApplication.scanPhoto(page.getThumbnailFilePath());
                mCanvasLayout.setDrawingCacheEnabled(false);
                if (async) {
                    PageSaveTask.getInstance().offer(page, true);
                } else {
                    page.save();
                    page.getOwner().save();
                }
                Logger.i("savePage保存执行完毕");
                recycleBitmap(drawingCache);
                Workspace.getInstance().setCurrentPage(page);
//                recycleBitmap(bitmap);
            }
        } catch (Exception e) {
            LogUtil.e("保存数据失败：" + e);
        }
        isFromSavePdf = false;//added by cuijc3
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mStrokeView.clearTwoFingerState();//去除缩放及平移状态

            clearUndoData();
            mCanvasLayout.deleteEmptyEditText();
            savingDataAndClose();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void addRecord(Object undoRecord) {
        EditorState.getInstance().setSavingFlag(true);
        Logger.i("new operation has done!");
        if (mUndoRecords.size() == UNDO_LIMIT) {
            if (mUndoRecords.getFirst().equals(ACTION_CUT)
                    || mUndoRecords.getFirst().equals(ACTION_DELETE)
                    || mUndoRecords.getFirst().equals(ACTION_PASTE)) {
                if(mUndoSelectedRecords.size() > 0 && mUndoSelectedRecords.getFirst() != null){
                    mUndoSelectedRecords.removeFirst();
                }
            }
            mUndoRecords.removeFirst();
        }

        mUndoRecords.add(undoRecord);
        mRedoRecords.clear();
        updateUndoButtonState();
        updateRedoButtonState();
    }

    private void undo() {
        if (mUndoRecords.size() > 0) {
            Object undo = mUndoRecords.getLast();
            mUndoRecords.removeLast();
            mRedoRecords.add(undo);

            if (undo.equals(ACTION_CUT)
                    || undo.equals(ACTION_DELETE)
                    || undo.equals(ACTION_PASTE)) {
                if(mUndoSelectedRecords.size() > 0){
                    List<Object> selectedList = mUndoSelectedRecords.getLast();
                    if (selectedList != null) {
                        mUndoSelectedRecords.removeLast();
                        mRedoSelectedRecords.add(selectedList);
                        for (int i = 0; i < selectedList.size(); i++) {
                            if (selectedList.get(i) instanceof Stroke) {
                                mStrokeView.undo(undo, (Stroke) selectedList.get(i));
                            } else if (selectedList.get(i) instanceof ElementLayout) {
                                mCanvasLayout.undo(undo, (ElementLayout) selectedList.get(i));
                            }
                        }
                    }
                }
            } else if (undo.equals(ACTION_ROTATION)) {
                rotationValueByButton = (int)(mCanvasLayout.getRotation() + 90) % 360;
                mCanvasLayout.setRotation(rotationValueByButton);
                canvasRotation = (int)mCanvasLayout.getRotation();
            } else if (undo instanceof Stroke) {
                mStrokeView.undo((Stroke) undo);
            } else if (undo instanceof ElementLayout) {
                mCanvasLayout.undo((ElementLayout) undo);
            }
        }
        updateUndoButtonState();
        updateRedoButtonState();
    }

    private void redo() {
        if (mRedoRecords.size() > 0) {
            Object redo = mRedoRecords.pop();
            mUndoRecords.add(redo);

            if (redo.equals(ACTION_CUT)
                    || redo.equals(ACTION_DELETE)
                    || redo.equals(ACTION_PASTE)) {
                if(mRedoSelectedRecords.size() > 0){
                    List<Object> selectedList = mRedoSelectedRecords.pop();
                    if (selectedList != null) {
                        mUndoSelectedRecords.add(selectedList);
                        for (int i = 0; i < selectedList.size(); i++) {
                            if (selectedList.get(i) instanceof Stroke) {
                                mStrokeView.redo(redo, (Stroke) selectedList.get(i));
                            } else if (selectedList.get(i) instanceof ElementLayout) {
                                mCanvasLayout.redo(redo, (ElementLayout) selectedList.get(i));
                            }
                        }
                    }
                }
            } else if (redo.equals(ACTION_ROTATION)) {
                rotationValueByButton = (int)(mCanvasLayout.getRotation() + 270) % 360;
                mCanvasLayout.setRotation(rotationValueByButton);
                canvasRotation = (int)mCanvasLayout.getRotation();
            } else if (redo instanceof Stroke) {
                mStrokeView.redo((Stroke) redo);
            } else if (redo instanceof ElementLayout) {
                mCanvasLayout.redo((ElementLayout) redo);

            }
        }
        updateUndoButtonState();
        updateRedoButtonState();
    }

    public static List<Object> listAddList(List<Object> listA, List<Stroke> listB) {
        List<Object> list = new ArrayList<>();
        if (listA != null) {
            list.addAll(listA);
        }
        if (listB != null) {
            list.addAll(listB);
        }

        return list;
    }

    private void clearUndoData() {
        mUndoRecords.clear();
        mRedoRecords.clear();
        mSelectedStrokeList.clear();
        mSelectedElementList.clear();
        mUndoSelectedRecords.clear();
        mRedoSelectedRecords.clear();
        updateUndoButtonState();
        updateRedoButtonState();
    }

    private void clearUndoDataOnNewPage(){
        mUndoRecords.clear();
        mRedoRecords.clear();
        mUndoSelectedRecords.clear();
        mRedoSelectedRecords.clear();
        updateUndoButtonState();
        updateRedoButtonState();
    }

    private void updateUndoButtonState(){
        if (undoButton != null) {
            if(mUndoRecords.size() == 0){
                undoButton.setEnabled(false);
            } else {
                undoButton.setEnabled(true);
            }
        }
    }

    private void updateRedoButtonState(){
        if (redoButton != null) {
            if(mRedoRecords.size() == 0){
                redoButton.setEnabled(false);
            } else {
                redoButton.setEnabled(true);
            }
        }
    }

    private void showChangeBgDialog() {
        dialogSetting = new PageSettingDialog(PageEditorActivity.this, R.style.DialogTranslucentBg, currentBgIndex);
        dialogSetting.setOnBgChangeListener(new PageSettingDialog.OnBackgroundChangeListener() {
            @Override
            public void onButtonClick(int viewId, int selectPageBgIndex) {
                dialogSetting.dismiss();
                switch (viewId) {
                    case R.id.tv_apply:
                        EditorState.getInstance().setSavingFlag(true);
                        currentBgIndex = selectPageBgIndex;
                        currentPageBgId = UIConstants.ARRAY_PAGE_BG_REPEAT[currentBgIndex];
                        mStrokeView.setBgDrawableID(currentPageBgId);
                        break;
                    case R.id.tv_cancel:
                        break;
                }
            }
        });
//        dialogSetting.show();
        BaseApplication.showImmersiveDialog(dialogSetting, PageEditorActivity.sActivity);
    }

    private void releaseStaticRes() {
        bottomView = null;
    }

    /**
     * 保存之前检查是否为NoteSaver，如果是NoteSaver，在保存前需要重新加载Document.xml中的数据，防止黑屏笔写数据丢失
     */
    private void reloadDocumentBeforeSave() {
        Document currentDocument = Workspace.getInstance().getCurrentDocument();
        int indexOf = com.hanvon.virtualpage.beans.Manifest.getInstance().getDocuments().indexOf(currentDocument);
        if (indexOf == 0) { // 如果是NoteSaver，保存之前需要重新去加载一下Document.xml文件中的数据，防止黑屏笔写数据丢失
            currentDocument = InformationParser.getDefaultDocument();
            Workspace.getInstance().setCurrentDocument(currentDocument);
            Page currentPage = Workspace.getInstance().getCurrentPage();
            if (currentPage != null) {
                for (Page page : currentDocument.getPages()) {
                    if (currentPage.getPath().equals(page.getPath())) {
                        Workspace.getInstance().setCurrentPage(page);
                    }
                }
            }
        }
    }
}
