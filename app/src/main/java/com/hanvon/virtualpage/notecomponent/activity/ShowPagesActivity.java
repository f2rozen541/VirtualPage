package com.hanvon.virtualpage.notecomponent.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hanvon.core.Stroke;
import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Document;
import com.hanvon.virtualpage.beans.Manifest;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.beans.UIConstants;
import com.hanvon.virtualpage.beans.Workspace;
import com.hanvon.virtualpage.common.CommonAlertDialog;
import com.hanvon.virtualpage.main.MainActivity;
import com.hanvon.virtualpage.common.OnFocusItemChangedListener;
import com.hanvon.virtualpage.notecomponent.view.DialogMergePage;
import com.hanvon.virtualpage.notecomponent.view.DialogMovePage;
import com.hanvon.virtualpage.notecomponent.widget.HorizontalPageList;
import com.hanvon.virtualpage.notecomponent.widget.PageWidgetView;
import com.hanvon.virtualpage.notecomponent.widget.VerticalPageList;
import com.hanvon.virtualpage.pageeditor.activity.PageEditor2Activity;
import com.hanvon.virtualpage.pageeditor.activity.PageEditorActivity;
import com.hanvon.virtualpage.utils.DialogShare;
import com.hanvon.virtualpage.utils.FileManagerUtils;
import com.hanvon.virtualpage.utils.InformationParser;
import com.hanvon.virtualpage.utils.LogUtil;
import com.hanvon.virtualpage.utils.TimeHelper;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/4/20
 * E_mail:  taozhi@hanwang.com.cn
 */
public class ShowPagesActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private TextView tvPageTabs;
    private TextView tvNoteName;
    private ImageView ivNewPage;
    private ImageView ivDelete;
    private ImageView ivMerge;
    private ImageView ivMove;
    private ImageView ivShare;
    private PageWidgetView pageWidgetView;
    private HorizontalPageList horizontalListView;
    private VerticalPageList verticalListView;
    private DialogShare dialogShare;
    private DialogMergePage dialogMergePage;

    private Document mNoteInfo;
    private int mCurrPagePosIndex;
    private int viewAnchorPosition;
    private List<Page> mPageInfoList;
    private FrameLayout flPageListContainer;
    private RelativeLayout rlPagesContainer;
    private boolean locateToEnd = true;

    public static final int PAGE_EDITOR_RESULT = 10;
    private static int currNoteCoverIndex = 0;
    private static final String HAS_OLD_DATA = "hasOldData";
    private static final String FOCUS_INDEX = "focusIndex";
    private boolean hasOldData;


    private PopupWindow ppwShareMenu;
    private View shareMenuView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_in_note_layout);
        if (savedInstanceState != null) {
            hasOldData = savedInstanceState.getBoolean(HAS_OLD_DATA);
            mCurrPagePosIndex = savedInstanceState.getInt(FOCUS_INDEX, 1);
            LogUtil.d("taozhi", "savedInstanceState != null: ");
        } else {
            hasOldData = false;
            LogUtil.d("taozhi", "savedInstanceState == null: ");
        }
        mContext = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initViews();
//        initData();
    }

    private void initViews() {
        tvNoteName = (TextView) findViewById(R.id.tv_note_name);
        tvPageTabs = (TextView) findViewById(R.id.tv_page_tabs);
        ivNewPage = (ImageView) findViewById(R.id.iv_new_page);
        ivDelete = (ImageView) findViewById(R.id.iv_page_delete);
        ivMerge = (ImageView) findViewById(R.id.iv_page_merge);
        ivMove = (ImageView) findViewById(R.id.iv_page_move);
        ivShare = (ImageView) findViewById(R.id.iv_page_share);
        ivNewPage.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
        ivMerge.setOnClickListener(this);
        ivMove.setOnClickListener(this);
        ivShare.setOnClickListener(this);

        rlPagesContainer = (RelativeLayout) findViewById(R.id.rl_pages_container);
        rlPagesContainer.setBackground(null); // 初次加载先清除背景

        flPageListContainer = (FrameLayout) findViewById(R.id.fl_page_list_container);

        horizontalListView = null;
        verticalListView = null;

        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            horizontalListView = new HorizontalPageList(mContext);
            horizontalListView.setOnFocusChangedListener(new OnFocusItemChangedListener() {
                @Override
                public void onNewItemFocused(int newPosition) {
                    Logger.e("Show", "从列表中调用刷新=====>" + newPosition);
                    locateViewsToAnchorPosition(newPosition);

                }

                @Override
                public void onListContentChanged(List<Page> newList) {
                    mNoteInfo.setPages(newList);
                    mNoteInfo.save();
                    mPageInfoList = newList;
                    pageWidgetView.updateDataWithCover(mPageInfoList);
                    horizontalListView.setPageListData(mPageInfoList);
                }

            });
            flPageListContainer.addView(horizontalListView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        } else {
            verticalListView = new VerticalPageList(mContext);
            verticalListView.setOnFocusChangedListener(new OnFocusItemChangedListener() {
                @Override
                public void onNewItemFocused(int newPosition) {
                    Logger.e("Show", "从列表中调用刷新=====>" + newPosition);
                    locateViewsToAnchorPosition(newPosition);
                }

                @Override
                public void onListContentChanged(List<Page> newList) {
                    mNoteInfo.setPages(newList);
                    mNoteInfo.save();
                    mPageInfoList = newList;
                    pageWidgetView.updateDataWithCover(mPageInfoList);
                    verticalListView.setPageListData(mPageInfoList);
                }

            });
            flPageListContainer.addView(verticalListView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        }
        pageWidgetView = (PageWidgetView) findViewById(R.id.page_widget_view);
        pageWidgetView.setOnPageTurnListener(new PageWidgetView.OnPageItemListener() {
            @Override
            public void onTurn(int count, int currentPosition) {
                Logger.d("Show", "从翻页控件调用刷新=====>" + currentPosition);
                locateViewsToAnchorPosition(currentPosition);
            }

            @Override
            public void onPageClick(int currentPosition) {
                Workspace.getInstance().setCurrentDocument(mNoteInfo);
                Page curPage = Workspace.getInstance().getCurrentDocument().getPage(currentPosition - 1);
                curPage.setOwner(mNoteInfo);
                Workspace.getInstance().setCurrentPage(curPage);
                Intent intent = new Intent(mContext, PageEditorActivity.class);
                mContext.startActivity(intent);
//                finish();
            }

            @Override
            public void onClickOnNoteCover() {
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
                releaseAllViews();
                finish();
            }
        });
    }

    /**
     * 同步移动到控件中当前Page的位置
     *
     * @param newPosition 新的位置
     */
    private void locateViewsToAnchorPosition(int newPosition) {
        if (viewAnchorPosition == newPosition) {
            return;
        }
        if (newPosition < 0 || newPosition > mPageInfoList.size()) {
            return;
        }
        viewAnchorPosition = newPosition;
        pageWidgetView.jumpToSpecificPosition(viewAnchorPosition);
        if (horizontalListView != null) {
            horizontalListView.moveToSpecificPosition(viewAnchorPosition);
        }
        if (verticalListView != null) {
            verticalListView.moveToSpecificPosition(viewAnchorPosition);
        }
        mCurrPagePosIndex = viewAnchorPosition;
        if (mCurrPagePosIndex > 0 && mCurrPagePosIndex <= mPageInfoList.size()) {
            Workspace.getInstance().setCurrentPage(mPageInfoList.get(viewAnchorPosition - 1));
        }
        if (tvPageTabs != null) {
            tvPageTabs.setText(mCurrPagePosIndex + "/" + mPageInfoList.size());
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mNoteInfo = Workspace.getInstance().getCurrentDocument(); // 获取当前的Document， 这里是主要获取数据的方法
        if (mNoteInfo == null) { // 默认的数据
            mPageInfoList = new ArrayList<>();
            for (int i = 0; i < 1; i++) {
                mPageInfoList.add(new Page());
            }
            mNoteInfo = new Document();
        }
        updateViewsAdapterData();
//        locateViewsToAnchorPosition(mCurrPagePosIndex);
    }

    /**
     * 更新数据
     *
     * @param noteInfo
     */
    public void setNewDocumentData(Document noteInfo) {
        if (noteInfo == null) {
            throw new IllegalArgumentException("Can not show PageDetail with null NoteInfo Object!");
        }
        this.mNoteInfo = noteInfo;
        Workspace.getInstance().setCurrentDocument(mNoteInfo);
        updateViewsAdapterData();
    }

    /**
     * 同时更新翻页显示和水平显示控件中的数据
     */
    private void updateViewsAdapterData() {
        checkLocateMode(); // 检查跳转位置
        currNoteCoverIndex = checkNoteCoverIndex(mNoteInfo);
        if (mNoteInfo != null) {
            mPageInfoList = mNoteInfo.getPages();
        }
        // 确定跳转位置，设定当前工作空间中的Page
        Page currentPage = null;
        if (hasOldData == false) { // 如果没有旧数据，就正常加载，否则加载最近保存的数据位置
            hasOldData = true;
            if (locateToEnd) {
                if (mPageInfoList.size() > 0) {
                    currentPage = mPageInfoList.get(mPageInfoList.size() - 1);
                    mCurrPagePosIndex = mPageInfoList.size();
                }
            } else {
                if (mPageInfoList.size() > 0) {
                    currentPage = InformationParser.getLatestUpdatePage(mPageInfoList);
                    mCurrPagePosIndex = mPageInfoList.indexOf(currentPage) + 1;
                }
            }
        } else { // 如果是旋屏或者返回
            currentPage = Workspace.getInstance().getCurrentPage();
            if (currentPage != null) {
                int pageDataIndex = -1;
                for (int i = 0; i < mPageInfoList.size(); i++) {
                    if (mPageInfoList.get(i).getPath().equals(currentPage.getPath())) {
                        pageDataIndex = i;
                        break;
                    }
                }
                LogUtil.e("index", "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                LogUtil.v("index", "当前获取到的工作空间pageDataIndex===>" + pageDataIndex);
                if (pageDataIndex != -1) {
                    mCurrPagePosIndex = pageDataIndex + 1;
                }
                if (mCurrPagePosIndex < 0 || mCurrPagePosIndex > mPageInfoList.size()) {
                    mCurrPagePosIndex = mPageInfoList.size();
                }
                LogUtil.v("index", "最后设定索引值mCurrentPageIndex===>" + mCurrPagePosIndex);
            } else {
                mCurrPagePosIndex = 0;
            }

        }
        Workspace.getInstance().setCurrentPage(currentPage);

        if (horizontalListView != null) {
            horizontalListView.setPageListData(mPageInfoList);
        }
        if (verticalListView != null) {
            verticalListView.setPageListData(mPageInfoList);
        }
        Page coverPage = new Page();
        coverPage.setBackgroundResIndex(String.valueOf(UIConstants.ARRAY_NOTE_OPEN_COVER[currNoteCoverIndex])); // 创建一个书皮封页
//        coverPage.setStoragePath(mNoteInfo.getStoragePath());
        if (mPageInfoList.size() == 0) {
            pageWidgetView.setVisibility(View.INVISIBLE);
        } else {
            pageWidgetView.setVisibility(View.VISIBLE);
            pageWidgetView.setStartWithCoverToPosition(coverPage, mPageInfoList, mCurrPagePosIndex);
        }
        if (rlPagesContainer.getBackground() == null) {
            rlPagesContainer.setBackgroundResource(UIConstants.ARRAY_NOTE_OPEN_BG[currNoteCoverIndex]);
        }
        Logger.i("本次updateViewsAdapterData设置的mCurrPagePosIndex值为==>" + mCurrPagePosIndex);
//        locateViewsToAnchorPosition(mCurrPagePosIndex);
        tvNoteName.setText(mNoteInfo.getTitle());
        tvPageTabs.setText(mCurrPagePosIndex + "/" + mPageInfoList.size());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Logger.e("777执行刷新方法==>viewAnchorPosition:" + viewAnchorPosition + "****mCurrPagePosIndex:" + mCurrPagePosIndex);
            viewAnchorPosition = 0;
            locateViewsToAnchorPosition(mCurrPagePosIndex);
//            if (viewAnchorPosition != mCurrPagePosIndex) {
//             }
        }
    }

    /**
     * 检查当前Document的封皮，获取对应的封皮的Index
     *
     * @param currentDocument
     * @return
     */
    private int checkNoteCoverIndex(Document currentDocument) {
        int noteCoverIndex;
        if (currentDocument != null) {
            try {
                noteCoverIndex = UIConstants.getAvailableCoverIndex(currentDocument.getBackgroundResIndex());
                if (noteCoverIndex == -1) { // 从Portableink接收过来的默认值可能为-1
//                    noteCoverIndex = R.drawable.note_cover2;
                    noteCoverIndex = UIConstants.DEFAULT_COVER_INDEX;
                }
            } catch (Exception e) {
                noteCoverIndex = UIConstants.DEFAULT_COVER_INDEX;
            }
        } else {
            noteCoverIndex = UIConstants.DEFAULT_COVER_INDEX;
        }
        currentDocument.setBackgroundResIndex(String.valueOf(noteCoverIndex));
        return noteCoverIndex;
    }

    /**
     * 获取当前最新的Page跳转模式
     */
    private void checkLocateMode() {
        int spValue = BaseApplication.getAppSharePreferences().getInt(UIConstants.LOCATE_PAGE_POSITION, UIConstants.JUMP_TO_END);
        if (spValue == UIConstants.JUMP_TO_END) {
            locateToEnd = true;
        } else {
            locateToEnd = false;
        }
    }

    public void zoomOutWithAnimation() {
        // TODO: 2016/3/31 这里添加消失的动画效果
//        this.setVisibility(INVISIBLE);
    }

    public void zoomInWithAnimation() {
        // TODO: 2016/3/31 这里添加出现的效果
//        this.setVisibility(VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        initData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        initData();
//        setNewDocumentData(Workspace.getInstance().getCurrentDocument());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BaseApplication.hasPermission()) {
            Workspace.getInstance().getCurrentDocument().save();
            Manifest.getInstance().save();
        }
        Runtime.getRuntime().gc();
        System.runFinalization();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(HAS_OLD_DATA, true);
        outState.putInt(FOCUS_INDEX, mCurrPagePosIndex);
        super.onSaveInstanceState(outState);
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        hasOldData = savedInstanceState.getBoolean(HAS_OLD_DATA, true);
//        mCurrPagePosIndex = savedInstanceState.getInt(FOCUS_INDEX, 1);
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_new_page:
                Intent intent = new Intent(mContext, PageEditorActivity.class);
                Page currentPage = InformationParser.newPageInDocument(mContext, mNoteInfo);
                Workspace.getInstance().setCurrentPage(currentPage);
                mContext.startActivity(intent);
//                finish();
                break;
            case R.id.iv_page_move:
                if (mPageInfoList.size() < 1) {
                    Toast.makeText(mContext, mContext.getString(R.string.WarningNoPageInfo), Toast.LENGTH_SHORT).show();
                    return;
                }
                DialogMovePage dialogMovePage = new DialogMovePage(mContext);
                dialogMovePage.setCurrentPageIndex(mCurrPagePosIndex - 1);
                dialogMovePage.setOnFinishedListener(new DialogMovePage.OnFinishedListener() {
                    @Override
                    public void onFinished() {
                        Document currentDocument = Workspace.getInstance().getCurrentDocument();
                        setNewDocumentData(currentDocument);
                    }
                });
                dialogMovePage.show();
                break;
            case R.id.iv_page_delete:
                if (mPageInfoList.size() < 1) {
                    Toast.makeText(mContext, mContext.getString(R.string.WarningNoPageInfo), Toast.LENGTH_SHORT).show();
                    return;
                }
                CommonAlertDialog commonAlertDialog = new CommonAlertDialog(mContext);
                commonAlertDialog.setTitle(getString(R.string.Delete))
                        .setContentInfo(getString(R.string.DeleteAPageWarningInfo))
                        .setOnDialogClickListener(new CommonAlertDialog.OnDialogOperationListener() {
                            @Override
                            public void onCancelClick() {

                            }

                            @Override
                            public void onConfirmClick() {
                                Page page = mPageInfoList.get(mCurrPagePosIndex - 1);
                                if (mPageInfoList.size() > 1) {
                                    if (mCurrPagePosIndex == mPageInfoList.size()) { // 如果当前的焦点位置是最后一页，就将焦点前移
                                        Workspace.getInstance().setCurrentPage(mPageInfoList.get(mCurrPagePosIndex - 2));
                                    } else { // 否则焦点页后移
                                        Workspace.getInstance().setCurrentPage(mPageInfoList.get(mCurrPagePosIndex));
                                    }
                                } else {
                                    Workspace.getInstance().setCurrentPage(null);
                                    mPageInfoList.clear();
                                }
//                                viewAnchorPosition--;
                                Workspace.getInstance().getCurrentDocument().delete(page);
                                Workspace.getInstance().getCurrentDocument().save();
                                Manifest.getInstance().save();

                                BaseApplication.scanPhoto(page.getThumbnailFilePath());

                                mNoteInfo = Workspace.getInstance().getCurrentDocument();
                                setNewDocumentData(mNoteInfo);
                            }
                        });
                commonAlertDialog.show();
//                Toast.makeText(mContext, "Delete", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_page_merge:
                if (mPageInfoList.size() < 1) {
                    Toast.makeText(mContext, mContext.getString(R.string.WarningNoPageInfo), Toast.LENGTH_SHORT).show();
                    return;
                }
                dialogMergePage = new DialogMergePage(mContext);
                dialogMergePage.show();
                dialogMergePage.setCurrentPosition(mCurrPagePosIndex - 1);
                dialogMergePage.setOnMergePageListener(new DialogMergePage.OnMergePageListener() {
                    @Override
                    public boolean onMergePage(int currPosition, int nextPosition) {
                        // TODO: 2016/4/28 合并操作的回调
//                        LogUtil.e("merge position:" + nextPosition + "---->to destPosition:" + currPosition);
                        if (nextPosition >= mPageInfoList.size()) {
                            nextPosition = mPageInfoList.size() - 1;
                        }
                        Page pageFirst = mPageInfoList.get(currPosition);
                        Page pageSecond = mPageInfoList.get(nextPosition);
                        if (!pageFirst.open() || !pageSecond.open()) {
                            return false;
                        } else {
                            mergeTwoPages(pageFirst, pageSecond);
                            FileManagerUtils.deleteFile(pageSecond.getStoragePath());
                            Document currentDocument = Workspace.getInstance().getCurrentDocument();
                            currentDocument.delete(pageSecond);
                            currentDocument.save();
                            Workspace.getInstance().setCurrentPage(pageFirst);
                            Intent intent = new Intent(mContext, PageEditor2Activity.class);
                            startActivityForResult(intent, PAGE_EDITOR_RESULT);

                            if (currPosition > nextPosition) {
                                viewAnchorPosition--;
                            }
                        }
                        if (dialogMergePage != null && dialogMergePage.isShowing()) {
                            dialogMergePage.dismiss();
                        }
                        return true;
                    }
                });
//                Toast.makeText(mContext, "Merge", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_page_share:
                if (mPageInfoList.size() < 1) {
                    Toast.makeText(mContext, mContext.getString(R.string.WarningNoPageInfo), Toast.LENGTH_SHORT).show();
                    return;
                }
                showSharePPWindow();

                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Runtime.getRuntime().gc();
//            System.runFinalization();
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            releaseAllViews();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 合并两个page的数据并保存
     *
     * @param firstPage  将第二个Page的数据合并到这个Page中
     * @param secondPage
     */
    private void mergeTwoPages(Page firstPage, Page secondPage) {
        List<Stroke> strokeListAll = firstPage.getStrokeList();
        strokeListAll.addAll(secondPage.getStrokeList());
        List<Object> elementListAll = firstPage.getElementList();
        elementListAll.addAll(secondPage.getElementList());
        firstPage.setStrokeList(strokeListAll);
        firstPage.setElementList(elementListAll);
        firstPage.setUpdatedTime(TimeHelper.getCurrentDateTime());
        firstPage.save();
        firstPage.getOwner().save();
        strokeListAll.clear();
        strokeListAll = null;
        elementListAll.clear();
        elementListAll = null;
    }


    //shiyu add
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        LogUtil.d("onActivityResult....................................");
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case PAGE_EDITOR_RESULT:
                dialogMergePage.setMergeProgress(100);
                Page pageFirst = Workspace.getInstance().getCurrentPage();
                LogUtil.e("Merge", "=================================");
                LogUtil.v("Merge", "合并后Page" + pageFirst.toString());
                LogUtil.v("Merge", "合并后Page" + pageFirst.getThumbnailFilePath());
                Document currentDocument = Workspace.getInstance().getCurrentDocument();
                setNewDocumentData(currentDocument);
                break;
            default:
                break;
        }
    }


    //shiyu
////////////////////////////////////////////////////////////////////
    private void showSharePPWindow() {
        if (ppwShareMenu == null) {
            shareMenuView = LayoutInflater.from(mContext).inflate(R.layout.share_menu, null);
            ppwShareMenu = new PopupWindow(shareMenuView, 400, 200);
            ppwShareMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {

                }
            });
        }


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.tv_share_menu_save_as_pdf:

                        dialogShare = new DialogShare(ShowPagesActivity.this, mContext.getString(R.string.ShareDialogTitle),
                                Workspace.getInstance().getCurrentPage().getThumbnailFilePath());

                        dialogShare.ShareSaveAsPDF(ShowPagesActivity.this, mContext.getString(R.string.ShareDialogTitle),
                                Workspace.getInstance().getCurrentPage().getThumbnailFilePath());

                        break;
                    case R.id.tv_share_menu_share_to:
                        Intent shareIntent = new Intent();

                        shareIntent.setAction(Intent.ACTION_SEND);

                        Page currentPage = Workspace.getInstance().getCurrentPage();
                        if (currentPage == null) {
                            break;
                        }
                        String thumbnailFilePath = currentPage.getThumbnailFilePath();
                        if(thumbnailFilePath == null)
                        {
                            Document currentDocument = Workspace.getInstance().getCurrentDocument();
                            currentPage.setOwner(currentDocument);
                            thumbnailFilePath = currentPage.getThumbnailFilePath();
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

        ppwShareMenu.setFocusable(true);
        ppwShareMenu.setOutsideTouchable(true);

        int[] location = new int[2];
        ivShare.getLocationOnScreen(location);

        ppwShareMenu.showAtLocation(ivShare, Gravity.NO_GRAVITY, location[0], location[1] - ppwShareMenu.getHeight());
    }

    private void hideShareMenuPopupWindow() {
        if (ppwShareMenu != null && ppwShareMenu.isShowing()) {
            ppwShareMenu.dismiss();
        }
    }
////////////////////////////////////////////////////////////////////////////////////////

    private void releaseAllViews() {
//        mContext = null;
//        tvPageTabs = null;
//        tvNoteName = null;
//        ivNewPage = null;
//        ivDelete = null;
//        ivMerge = null;
//        ivMove = null;
//        ivShare = null;
//        pageWidgetView = null;
        horizontalListView = null;
        verticalListView = null;
        dialogShare = null;
        dialogMergePage = null;

        flPageListContainer = null;
        rlPagesContainer = null;

        ppwShareMenu = null;
        shareMenuView = null;

        setContentView(R.layout.layout_null_view);
    }

}
