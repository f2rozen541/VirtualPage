package com.hanvon.virtualpage.notecomponent.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.beans.Workspace;
import com.hanvon.virtualpage.notecomponent.widget.DividerGridItemDecorator;
import com.hanvon.virtualpage.pageeditor.view.AutoBgButton;
import com.hanvon.virtualpage.utils.UIUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @Description:
 * @Author: TaoZhi
 * @Date: 2016/4/28
 * @E_mail: taozhi@hanwang.com.cn
 */
public class DialogMergePage extends Dialog {

    private static final int STATE_SUCCESS = 0;
    private static final int STATE_SHOW_RESULT = 1;
    private static final int STATE_UPDATE = 2;
    private Context mContext;
    private View parentView;
    private ImageButton ibConfirmMerge;
    private RecyclerView rvPageList;
    private LinearLayout llContainer;
    private MergeListAdapter mAdapter;
    private int mRealDatePos;
    private Page mCurrPage;
    private List<Page> pages;
    private OnMergePageListener mListener;
    private boolean hasMergeSucceed;
    //    private ProgressDialog progressDialog;
    private DialogProgress dialogProgress;
    private int progressValue;
    private ImageButton ibCancelMerge;
    private boolean isThreadRunning;

    public DialogMergePage(Context context) {
        super(context, R.style.shareDialogTheme);
    }

    public DialogMergePage(Context context, int themeResId) {
        this(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mContext = getContext();
        initViews();
        initData();
    }

    private void initViews() {
        parentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_merge_page_layout, null);
        llContainer = (LinearLayout) parentView.findViewById(R.id.ll_container);
        ibConfirmMerge = (ImageButton) parentView.findViewById(R.id.ib_merge_confirm);
        ibCancelMerge = (ImageButton) parentView.findViewById(R.id.ib_merge_cancel);
        rvPageList = (RecyclerView) parentView.findViewById(R.id.rv_page_list);
        rvPageList.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
        rvPageList.addItemDecoration(new DividerGridItemDecorator(mContext, 24, 24, false));
        mAdapter = new MergeListAdapter();
        rvPageList.setAdapter(mAdapter);
        rvPageList.addItemDecoration(new DividerGridItemDecorator(mContext));
        ViewGroup.LayoutParams layoutParams = llContainer.getLayoutParams();
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.height = 1040;
        } else {
            layoutParams.height = 1532;
        }
        llContainer.setLayoutParams(layoutParams);
        this.setContentView(parentView, new ViewGroup.LayoutParams(
                mContext.getResources().getDisplayMetrics().widthPixels,
                mContext.getResources().getDisplayMetrics().heightPixels));
//        getWindow().setGravity(80);
        ibConfirmMerge.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mAdapter.getCheckedPosition() < 0) {
//                    Toast.makeText(mContext, "You must choose a page to merge!", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialogProgress = new DialogProgress(mContext);
                dialogProgress.setOnStopClickListener(new DialogProgress.OnStopClickListener() {
                    @Override
                    public void onStopClick() {
                        hasMergeSucceed = false;
                        isThreadRunning = false;
                        progressValue = 100;
                        mHandler.sendEmptyMessage(STATE_UPDATE);
                        dismiss();
                    }
                });
                dialogProgress.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.sendEmptyMessage(STATE_SHOW_RESULT);
                        progressValue = 0;
                    }
                });
                dialogProgress.show();
                isThreadRunning = true;
                new Thread() {
                    @Override
                    public void run() {
                        while (progressValue < 90) {
                            if (isThreadRunning == false) { // 如果已经被中断了，就直接结束
                                break;
                            } else {
                                progressValue = progressValue + 5;
                                mHandler.sendEmptyMessage(STATE_UPDATE);
                                try {
                                    SystemClock.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                }.start();
            }
        });
        ibCancelMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowing()) {
                    dismiss();
                }
            }
        });
    }

    public void setCurrentPosition(int position) {
        mRealDatePos = position;
    }


    public void setMergeProgress(int progress) {
        progressValue = progress;
        mHandler.sendEmptyMessage(STATE_UPDATE);
    }

    private Handler mHandler = new ProgressHandler();

    public class ProgressHandler extends Handler {

        private AlertDialog resultDialog;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATE_SUCCESS:
                    hasMergeSucceed = true;
                    dialogProgress.dismiss();
                    break;
                case STATE_SHOW_RESULT:
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
                    if (hasMergeSucceed) {
//                        builder.setTitle(R.string.MergeSucceed);
//                        builder.setMessage(mContext.getText(R.string.MergeSuccessInfo));
                    } else {
                        builder.setTitle(R.string.MergeFailed);
                        builder.setMessage(mContext.getText(R.string.MergeFailedInfo));
                        resultDialog = builder.create();
                        resultDialog.getWindow().setLayout(720, 300);
                        resultDialog.setCanceledOnTouchOutside(false);
                        resultDialog.show();
                    }
                    // 1s后自动消失
                    UIUtils.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (resultDialog != null && resultDialog.isShowing()) {
                                resultDialog.dismiss();
                            }
                        }
                    }, 1000);
                    break;
                case STATE_UPDATE:
                    if (progressValue == 70) { // 当进度条走到70%时开始合并操作
                        Logger.e("process 开始进行合并了哦！");
                        dialogProgress.setOnStopClickListener(null);
                        if (mListener != null) {
//                            if (mRealDatePos == mAdapter.getCheckedPosition() || mAdapter.getCheckedPosition() < 0) {
                            int checkedPosition = mAdapter.getCheckedPosition();
                            if (checkedPosition < 1) {
                                return;
                            }
                            // 因为数据变化过，获取到选中的位置需要判断
                            if (checkedPosition <= mRealDatePos) { // 如果当前选中的合并页位置在真实位置之前，则需要将选中位置-1
                                checkedPosition = checkedPosition - 1;
                            }
                            hasMergeSucceed = mListener.onMergePage(mRealDatePos, checkedPosition);
                        }
                    } else if (progressValue == 100) {
                        if (dialogProgress != null && dialogProgress.isShowing()) {
                            dialogProgress.dismiss();
                        }
                        isThreadRunning = false;
                        return;
                    }
                    dialogProgress.setSmoothProgress(progressValue / 100f);
                    break;
            }
        }
    }

    private void initData() {
        pages = Workspace.getInstance().getCurrentDocument().getPages();
        mCurrPage = Workspace.getInstance().getCurrentPage();
        // 将当前的Pages数据拷贝一份
        List<Page> tempList = new ArrayList<>();
        tempList.addAll(pages);
        Collections.copy(tempList, pages);
        // 然后移除当前Page
        if (tempList.contains(mCurrPage)) {
            tempList.remove(mCurrPage);
        }
        // 重新构造数据源
        List<Page> resultList = new ArrayList<>();
        resultList.add(mCurrPage);
        resultList.addAll(tempList);
        tempList.clear();
        mAdapter.setData(resultList);
        // 当前Page肯定在第一位
//        mRealDatePos = 0;
//        mRealDatePos = pages.indexOf(mCurrPage);
//        mAdapter.setCurrentPosition(mRealDatePos);
        progressValue = 0;
    }

    public class MergeListAdapter extends RecyclerView.Adapter {
        private List<Page> mPageList;
        private int mCheckedPosition = -1;
        private AutoBgButton mCheckedView;
//        private int mCurrentPos = 0;

        public MergeListAdapter() {
        }

        public MergeListAdapter(List<Page> mPageList) {
            this.mPageList = mPageList;
        }

        public void setData(List<Page> pageList) {
            this.mPageList = pageList;
            notifyDataSetChanged();
        }

//        public void setCurrentPosition(int position) {
//            mCurrentPos = position;
//        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(mContext).inflate(R.layout.dialog_merge_page_item_layout, parent, false);
            return new MergeItemViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            Page currPage = mPageList.get(position);
            if (holder instanceof MergeItemViewHolder) {
                ImageView ivPage = ((MergeItemViewHolder) holder).ivPage;
                Glide.with(BaseApplication.getContext())
                        .load(currPage.getThumbnailFilePath())
//                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                        .skipMemoryCache(true)
                        .into(ivPage);
                final AutoBgButton abbCheckFlag = ((MergeItemViewHolder) holder).abbCheckFlag;
                abbCheckFlag.setSelectedState(false);
                if (position == 0) { // 第一位默认是当前页，不可操作
                    abbCheckFlag.setBackgroundResource(R.drawable.ic_merge_page_selected);
                    ((MergeItemViewHolder) holder).flDisableFlag.setVisibility(View.VISIBLE);
                    ((MergeItemViewHolder) holder).rlContainer.setEnabled(false);
                    abbCheckFlag.setBackgroundResource(R.drawable.ic_merge_page_select_disable);
                } else {
                    ((MergeItemViewHolder) holder).flDisableFlag.setVisibility(View.GONE);
                    ((MergeItemViewHolder) holder).rlContainer.setEnabled(true);
                    abbCheckFlag.setBackground(null);
                }

                ((MergeItemViewHolder) holder).rlContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCheckedPosition == position && mCheckedView != null) {
                            mCheckedView.setSelectedState(!mCheckedView.getSelectState());
                            if (!mCheckedView.getSelectState()) {
                                mCheckedPosition = -1;
                            }
                        } else {
                            if (mCheckedView != null) {
                                mCheckedView.setSelectedState(false);
                            }
                            mCheckedView = abbCheckFlag;
                            mCheckedView.setSelectedState(true);
                            mCheckedPosition = position;
                        }

                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mPageList.size();
        }

        public int getCheckedPosition() {
            return mCheckedPosition;
        }

        public class MergeItemViewHolder extends RecyclerView.ViewHolder {
            public ImageView ivPage;
            public AutoBgButton abbCheckFlag;
            public FrameLayout flDisableFlag;
            public RelativeLayout rlContainer;

            public MergeItemViewHolder(View itemView) {
                super(itemView);
                ivPage = (ImageView) itemView.findViewById(R.id.iv_page);
                rlContainer = (RelativeLayout) itemView.findViewById(R.id.rl_container);
                abbCheckFlag = (AutoBgButton) itemView.findViewById(R.id.abb_check_flag);
                flDisableFlag = (FrameLayout) itemView.findViewById(R.id.fl_disable_flag);
            }
        }
    }

    public interface OnMergePageListener {
        boolean onMergePage(int currPosition, int nextPosition);
    }

    public void setOnMergePageListener(OnMergePageListener listener) {
        this.mListener = listener;
    }

}
