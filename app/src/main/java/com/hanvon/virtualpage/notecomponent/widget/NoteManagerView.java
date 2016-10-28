package com.hanvon.virtualpage.notecomponent.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Document;
import com.hanvon.virtualpage.beans.Manifest;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.beans.UIConstants;
import com.hanvon.virtualpage.beans.Workspace;
import com.hanvon.virtualpage.common.CommonAlertDialog;
import com.hanvon.virtualpage.notecomponent.activity.ShowPagesActivity;
import com.hanvon.virtualpage.notecomponent.adapter.NoteListAdapter;
import com.hanvon.virtualpage.notecomponent.view.DialogNoteSetting;
import com.hanvon.virtualpage.pageeditor.activity.PageEditorActivity;
import com.hanvon.virtualpage.utils.InformationParser;
import com.hanvon.virtualpage.utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * @Description: Note管理页控件
 * @Author: TaoZhi
 * @Date: 2016/3/31
 * @E_mail: taozhi@hanwang.com.cn
 */
public class NoteManagerView extends RelativeLayout implements View.OnClickListener {
    private Context mContext;
    private RecyclerView mRVNotes;
    private List<Document> mNoteList;
    private View parentView;
    private NoteListAdapter mNoteListAdapter;
    private View mHeader;
    private View mTail;
    private int mWidth = 0;
    private int mHeight = 0;
    private int mNoteItemWidth;
    private boolean isOriginSize;
    private View mFocusView;
    private int mFocusView_X_Left;
    private int mFocusViewPosition = -1;
    private int mDestPosition = -1;
    private TextView tvNoteTabs;
    private int mCurrOrientation;
    private ImageButton ibNoteNewNote;

    private boolean needRelayout;
    private List<Integer> coversList = new ArrayList<>();
    private ImageButton ibNoteNewPage;
    private ImageButton ibNoteSetting;
    private ImageButton ibNoteDelete;
    private LinearLayout llFunction;
    private long preNewNoteTimeStamp = -1;
    private long curNewNoteTimeStamp = -1;


    public NoteManagerView(Context context) {
        this(context, null);
    }

    public NoteManagerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteManagerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        parentView = LayoutInflater.from(mContext).inflate(R.layout.note_manager_layout, this);
        initData();
        initNotesView();
    }

    /**
     * 设置界面需要显示的数据
     * @param documentList
     */
    public void setData(List<Document> documentList) {
        mNoteList = documentList;
        mNoteListAdapter.setData(documentList);
        mRVNotes.smoothScrollBy(2, 0); // 激活滑动事件
    }

    /**
     * 初始化控件数据，默认会给出一条数据进行展示
     */
    private void initData() {
        if (mFocusViewPosition == -1) {
            mFocusViewPosition = 1;
        }
        if (mNoteList == null) {
            mNoteList = new ArrayList<>();
        }
        if (coversList.size() == 0) {
            for (int i = 0; i < UIConstants.ARRAY_NOTE_COVER.length; i++) {
                coversList.add(UIConstants.ARRAY_NOTE_COVER[i]);
            }
        }
        mCurrOrientation = mContext.getResources().getConfiguration().orientation;
        if (Manifest.getInstance().getDocuments().size() == 0) { // 如果为空，设置默认的数据
            for (int i = 0; i < 1; i++) {
                Document note = new Document();
//                note.setBackgroundResIndex(String.valueOf(R.drawable.note_cover2));
                note.setBackgroundResIndex(String.valueOf(UIConstants.DEFAULT_COVER_INDEX));
                note.setTitle(mContext.getString(R.string.NoteSaver));
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                String dateStr = df.format(calendar.getTime());
                note.setUpdatedTime(dateStr);
                mNoteList.add(note);
            }
        } else { // 获取所有的Document，这里主要是通过全局的方法获取数据
            mNoteList = Manifest.getInstance().getDocuments();
        }
        isOriginSize = true;
        needRelayout = true;
    }

    /** 初始化控件对象，并为对象设置事件响应 */
    private void initNotesView() {
        mRVNotes = (RecyclerView) parentView.findViewById(R.id.rv_note_list);
        tvNoteTabs = (TextView) parentView.findViewById(R.id.tv_note_tabs);
        llFunction = (LinearLayout) findViewById(R.id.ll_function);
        ibNoteNewNote = (ImageButton) parentView.findViewById(R.id.ib_note_new_note);
        ibNoteNewPage = (ImageButton) findViewById(R.id.ib_note_new_page);
        ibNoteSetting = (ImageButton) findViewById(R.id.ib_note_setting);
        ibNoteDelete = (ImageButton) findViewById(R.id.ib_note_delete);
        ibNoteNewPage.setOnClickListener(this);
        ibNoteSetting.setOnClickListener(this);
        ibNoteDelete.setOnClickListener(this);
        ibNoteNewNote.setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRVNotes.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mRVNotes.setLayoutManager(layoutManager);
        // 构建NoteListAdapter
        mNoteListAdapter = new NoteListAdapter(mContext, mNoteList);
        if (mHeader == null) {
            mHeader = generateView();
        }
        if (mTail == null) {
            mTail = generateView();
        }
        mNoteListAdapter.setHeaderView(mHeader);
        mNoteListAdapter.setTailView(mTail);
        // 原来加载到每个Note条目上的方法已经被废弃，而是统一到了页面上的工具栏上
        mNoteListAdapter.setOnNoteOperateListener(new NoteListAdapter.OnNoteOperateListener() {
            @Override
            public void onNewClick(int dataPosition) {
//
//                Document document = mNoteList.get(dataPosition);
//                Workspace.getInstance().setCurrentDocument(document);
//                Page currentPage = InformationParser.newPageInDocument(mContext, document);
//                Workspace.getInstance().setCurrentPage(currentPage);
//                Intent intent = new Intent(mContext, PageEditorActivity.class);
//                mContext.startActivity(intent);
            }

            @Override
            public void onSettingClick(int dataPosition) {
//                showNoteEditDialog();
            }

            @Override
            public void onDeleteClick(final int dataPosition) {
//                Builder builder = new Builder(mContext);
//                builder.setTitle(mContext.getString(R.string.Delete)).
//                        setMessage(mContext.getString(R.string.configure_to_del)).
//                        setPositiveButton(mContext.getString(R.string.OK), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Manifest.getInstance().delete(mNoteList.get(dataPosition));
//                                mNoteList.remove(dataPosition);
//                                Manifest.getInstance().save();
//                                Logger.d("剩余的数据为" + Manifest.getInstance().getDocuments().size());
//                                mNoteListAdapter.notifyDataSetChanged();
//                            }
//                        }).setNegativeButton(mContext.getString(R.string.Cancel), null);
//
//                android.support.v7.app.AlertDialog alertDialog = builder.create();
//                alertDialog.show();
            }

            @Override
            public void onNoteClick(int dataPosition) {
                int viewPosition = dataPosition + 1;
                if (viewPosition == mFocusViewPosition) {
                    // TODO: 2016/5/28 暂时作为调试状态
                    Workspace.getInstance().setCurrentDocument(mNoteList.get(dataPosition));
                    Intent intent = new Intent(mContext, ShowPagesActivity.class);
                    mContext.startActivity(intent);
                } else {
                    scrollToSpecifiedPosition(viewPosition);
                }
            }
        });
        mRVNotes.setAdapter(mNoteListAdapter);
        mRVNotes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int deltaX = 0;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                LogUtil.i("taozhi9", "当前的状态----->" + newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mFocusView = checkForFocusView();
                    if (mFocusView != null && mFocusView.getLeft() == mFocusView_X_Left) {
                        zoomInAnimation(mFocusView, 200, true);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                deltaX += dx;
                if (!isOriginSize && mFocusView != null) {
                    zoomOutAnimation(mFocusView, 200, true);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_note_new_page:
                Document document = mNoteList.get(mFocusViewPosition - 1);
                Workspace.getInstance().setCurrentDocument(document);
                Page currentPage = InformationParser.newPageInDocument(mContext, document);
                Workspace.getInstance().setCurrentPage(currentPage);
                Intent intent = new Intent(mContext, PageEditorActivity.class);
                mContext.startActivity(intent);
                break;
            case R.id.ib_note_setting:
                showNoteEditDialog();
                break;
            case R.id.ib_note_delete:

                CommonAlertDialog commonAlertDialog = new CommonAlertDialog(mContext);
                commonAlertDialog.setTitle(mContext.getString(R.string.Delete))
                        .setContentInfo(mContext.getString(R.string.confirm_to_del));
                commonAlertDialog.setOnDialogClickListener(new CommonAlertDialog.OnDialogOperationListener() {
                    @Override
                    public void onCancelClick() {

                    }

                    @Override
                    public void onConfirmClick() {
                        Manifest.getInstance().delete(mNoteList.get(mFocusViewPosition - 1));
                        Manifest.getInstance().save();
                        mFocusViewPosition = mFocusViewPosition - 1;
                        mNoteList = Manifest.getInstance().getDocuments();
                        setData(mNoteList);
                        scrollToSpecifiedPosition(mFocusViewPosition);
//                                Logger.d("剩余的数据为" + Manifest.getInstance().getDocuments().size());
                    }
                });
                commonAlertDialog.show();
                break;
            case R.id.ib_note_new_note:
                if (preNewNoteTimeStamp < 0) {
                    preNewNoteTimeStamp = 0;
                }
                curNewNoteTimeStamp = System.currentTimeMillis();
                long gapTimeStamp = curNewNoteTimeStamp - preNewNoteTimeStamp;
                if (gapTimeStamp < 1000) {
//                    Toast.makeText(mContext, "Your operation is too frequently!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    preNewNoteTimeStamp = curNewNoteTimeStamp;
                }
                InformationParser.addNewDocument(mContext, com.hanvon.virtualpage.beans.Manifest.getInstance());
                mNoteList = Manifest.getInstance().getDocuments();
                Collections.sort(mNoteList, new InformationParser.NoteListComparator());
                setData(mNoteList);
                scrollToSpecifiedPosition(2);
                break;
            default:
                break;
        }
    }

    /**
     * 设置滑动到指定位置
     * @param position
     */
    private void scrollToSpecifiedPosition(int position) {
        if (position < 1 || position > mNoteList.size()) {
            return;
        }
        LogUtil.e("wocao", "mFocusViewPosition=====>" + mFocusViewPosition + "======mDestPosition===>" + mDestPosition);
        int gapIndex = position - mFocusViewPosition;
        mRVNotes.smoothScrollBy(gapIndex * mNoteItemWidth, 0); // 给一个偏移量，让它可以调用onScrollStateChanged方法
    }

    public void locateToSpecifiedPosition(int position) {
        scrollToSpecifiedPosition(position);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (needRelayout) {
            Log.e("hello", "onLayout() called with: " + "needRelayout = [" + needRelayout + "]");
            needRelayout = false;
            mWidth = getWidth();
            mHeight = getHeight();
            mNoteItemWidth = mRVNotes.getChildAt(1).getWidth();
            mFocusView_X_Left = (mWidth - mNoteItemWidth + 1) / 2; // 总长度去掉一个Note的宽度，然后平分给两边，计算出当前获取焦点的NoteItem的左边坐标值, 注：这里+1是因为计算可能导致最后的Note达不到焦点坐标
            relayoutViews();
//            mRVNotes.smoothScrollBy(2, 0); // 触发滑动事件
//            locateToSpecifiedPosition(mFocusViewPosition);
        }
    }

    /** 根据当前屏幕状态，设置控件布局位置和尺寸 */
    private void relayoutViews() {
        LayoutParams lpNoteTabs = (LayoutParams) tvNoteTabs.getLayoutParams();
        LayoutParams lpNewNote = (LayoutParams) ibNoteNewNote.getLayoutParams();
        if (mCurrOrientation == Configuration.ORIENTATION_PORTRAIT) {
            lpNoteTabs.topMargin = 88;
            lpNewNote.topMargin = 44;
            lpNewNote.rightMargin = 44;
        } else {
            lpNoteTabs.topMargin = 18;
            lpNewNote.topMargin = 22;
            lpNewNote.rightMargin = 22;
        }
        tvNoteTabs.setLayoutParams(lpNoteTabs);
        ibNoteNewNote.setLayoutParams(lpNewNote);

        ViewGroup.LayoutParams lpHeader = mHeader.getLayoutParams();
        lpHeader.width = mFocusView_X_Left;
        mHeader.setLayoutParams(lpHeader);
        ViewGroup.LayoutParams lpTail = mTail.getLayoutParams();
        lpTail.width = mFocusView_X_Left;
        mTail.setLayoutParams(lpTail);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Log.e("hello", "onWindowFocusChanged() called with: " + "hasWindowFocus = [" + hasWindowFocus + "]");
        if (hasWindowFocus) {
            mRVNotes.smoothScrollBy(2, 0); // 激活滑动事件，让系统重新计算焦点位置
            if (mDestPosition != -1) {
                scrollToSpecifiedPosition(mDestPosition);
                mDestPosition = -1;
            }
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("hello", "onConfigurationChanged() called with: " + "newConfig = [" + newConfig + "]");
        // 横竖屏切换的时候就滑动一定距离，保证横竖屏切换时，焦点不会发生改变。这里是因为旋屏时，屏幕尺寸切换，导致计算会出现偏差，这里进行偏差校正
        mCurrOrientation = newConfig.orientation;
        needRelayout = true;
        if (mFocusViewPosition > 1) {
            if (mCurrOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mRVNotes.smoothScrollBy(-mNoteItemWidth / 2, 0);
            } else {
                mRVNotes.smoothScrollBy(mNoteItemWidth / 2, 0);
            }
        } else {
            mRVNotes.smoothScrollBy(2, 0);
        }
    }

    /**
     * 根据当前HorizontalView中的控件位置，设置当前应该被聚焦的条目
     */
    public View checkForFocusView() {
        int i = 1; // 这里必须从1开始计算，因为横屏状态下，第一个child就已经满足了，但是这是headView不能被聚焦
        View child = mRVNotes.getChildAt(i);
        int mStepWidth = mNoteItemWidth;
        while (child != null) {
            int gapX = mFocusView_X_Left - child.getLeft(); // 计算焦点框与item之间的距离
            if (gapX < mStepWidth) { // 找到最近的page
                if (gapX == 0) {
                    break;
                }
                if (gapX <= mStepWidth / 2) { // 如果超过一半在焦点框内，就向右滑动
                    mRVNotes.smoothScrollBy(-gapX, 0);
                } else { // 否则，就向左滑动
                    mRVNotes.smoothScrollBy(mStepWidth - gapX, 0);
                    child = mRVNotes.getChildAt(i + 1);
                }
                break;
            }
            i++;
            child = mRVNotes.getChildAt(i);
        }
        mFocusViewPosition = mRVNotes.getChildAdapterPosition(child);
//        LogUtil.w("taozhi9", "checkForFocusView(99)====>" + mFocusViewPosition);
        if (mFocusViewPosition < 1) {
            mFocusViewPosition = 1;
        } else if (mFocusViewPosition > mNoteList.size()) {
            mFocusViewPosition = mNoteList.size();
        }
        return child;
    }


    /**
     * 执行放大动画
     * @param view
     * @param duringTime
     * @param isFillAfter
     */
    private void zoomInAnimation(final View view, long duringTime, boolean isFillAfter) {
        if (view == null) {
            return;
        }
        Animation animation = new ScaleAnimation(
                1.0f, 1.1f,
                1.0f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        animation.setDuration(duringTime);
        animation.setFillAfter(isFillAfter);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llFunction.setVisibility(VISIBLE);
                ibNoteDelete.setEnabled(mFocusViewPosition != 1);
                Workspace.getInstance().setCurrentDocument(mNoteList.get(mFocusViewPosition - 1));
//                view.findViewById(R.id.ll_function).setVisibility(VISIBLE);
//                view.setEnabled(true);
//                tvNoteTabs.setText(mFocusViewPosition + "/" + mNoteList.size());
//                tvNoteTabs.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.findViewById(R.id.fl_note_item_container).startAnimation(animation);
        isOriginSize = false;
    }

    /**
     * 指定缩小动画
     * @param view
     * @param duringTime
     * @param isFillAfter
     */
    private void zoomOutAnimation(final View view, long duringTime, boolean isFillAfter) {
        if (view == null) {
            return;
        }
        Animation animation = new ScaleAnimation(
                1.1f, 1.0f,
                1.1f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        animation.setDuration(duringTime);
        animation.setFillAfter(isFillAfter);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                llFunction.setVisibility(INVISIBLE);
//                view.findViewById(R.id.ll_function).setVisibility(INVISIBLE);
//                view.setEnabled(false);
//                tvNoteTabs.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.findViewById(R.id.fl_note_item_container).startAnimation(animation);
        isOriginSize = true;
    }

    /**
     * 生成一个占位控件，该控件不可见，在真正显示的时候会被重新设定宽高
     * @return
     */
    private View generateView() {
        View view = new View(mContext);
        view.setLayoutParams(new ViewGroup.LayoutParams(10, 10));
        return view;
    }

    /** 显示Note设置对话框 */
    private void showNoteEditDialog() {
        DialogNoteSetting dialogNoteSetting = new DialogNoteSetting(mContext);
        dialogNoteSetting.setOnDialogOperationListener(new DialogNoteSetting.OnDialogOperationListener() {
            @Override
            public void onConfirmClick() {
                mNoteList = Manifest.getInstance().getDocuments();
                Collections.sort(mNoteList, new InformationParser.NoteListComparator());
                setData(mNoteList);
                if (mFocusViewPosition > 1) { // 如果不是修改的第一条数据，就滑动到第二条
                    mDestPosition = 2;
                } else {
                    mDestPosition = 1;
                }
            }

            @Override
            public void onCancelClick() {

            }
        });
        dialogNoteSetting.show();
    }

    /**
     * 设置焦点位置
     * @param position
     */
    public void setFocusViewPosition(int position) {
        mDestPosition = position;
    }
}
