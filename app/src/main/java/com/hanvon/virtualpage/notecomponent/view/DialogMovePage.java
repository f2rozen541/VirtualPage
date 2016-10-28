package com.hanvon.virtualpage.notecomponent.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Document;
import com.hanvon.virtualpage.beans.Manifest;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.beans.UIConstants;
import com.hanvon.virtualpage.beans.Workspace;
import com.hanvon.virtualpage.notecomponent.widget.DividerGridItemDecorator;
import com.hanvon.virtualpage.utils.FileManagerUtils;
import com.hanvon.virtualpage.utils.InformationParser;
import com.hanvon.virtualpage.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: TaoZhi
 * @Date: 2016/4/28
 * @E_mail: taozhi@hanwang.com.cn
 */
public class DialogMovePage extends Dialog {

    private Context mContext;
    private View parentView;
    private RecyclerView rvNoteList;
    private PPWNoteListAdapter mAdapter;
    private Document mCurrDocument;
    private List<Document> documents;
    private LinearLayout llContainer;
    private DialogMoveConfirm mDialogConfirm;
//    private AlertDialog mConfirmDialog;
    private int mDestPosition = -1;
    private OnFinishedListener mListener;
    private int mCurrPageIndex = -1;

    public DialogMovePage(Context context) {
        super(context, R.style.shareDialogTheme);
    }

    public DialogMovePage(Context context, int themeResId) {
        this(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        initView();
        initData();
    }

    public void setCurrentPageIndex(int position) {
        mCurrPageIndex = position;
    }

    private void initView() {
        parentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_move_to_note_layout, null);
        llContainer = (LinearLayout) parentView.findViewById(R.id.ll_container);
        rvNoteList = (RecyclerView) parentView.findViewById(R.id.rv_note_list);
        rvNoteList.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
        mAdapter = new PPWNoteListAdapter();
        rvNoteList.setAdapter(mAdapter);
        rvNoteList.addItemDecoration(new DividerGridItemDecorator(mContext, 14, 18, false));

        ViewGroup.LayoutParams layoutParams = llContainer.getLayoutParams();
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.height = 1040;
        } else {
            layoutParams.height = 1532;
        }
        llContainer.setLayoutParams(layoutParams);
        this.setContentView(parentView);
        getWindow().setGravity(80);

        mDialogConfirm = new DialogMoveConfirm(mContext);
        mDialogConfirm.setOnOptionListener(new DialogMoveConfirm.OnOptionListener() {
            @Override
            public void onCancelClick() {
                dismissConfirmDialog();
            }

            @Override
            public void onOKClick(int choice) {
                dismissSelf();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                int currPosition = Manifest.getInstance().indexOf(Workspace.getInstance().getCurrentDocument());
                LogUtil.d("dialogMovePage", "currPosition:" + currPosition);
                LogUtil.d("dialogMovePage", "mDestPosition:" + mDestPosition);
                if (currPosition == mDestPosition) {
                    builder.setTitle(R.string.Failed);
                    builder.setMessage(R.string.FailedInfo);
                    builder.create().show();
                } else {
                    Document currentDocument = Workspace.getInstance().getCurrentDocument();
                    Page currentPage = currentDocument.getPage(mCurrPageIndex);
                    Document destDocument = documents.get(mDestPosition);
                    String destPath = destDocument.getStoragePath() + currentPage.getPath();
                    String srcPath = currentDocument.getStoragePath() + currentPage.getPath();
                    LogUtil.e("srcPath = " + srcPath);
                    LogUtil.e("destPath = " + destPath);
                    FileManagerUtils.copyFolder(srcPath, destPath);
                    destDocument.getPages().add(currentPage);
                    destDocument.save();

//                    builder.setTitle(R.string.Success);
                    if (choice == DialogMoveConfirm.COPY_PAGE) {
//                        builder.setMessage(R.string.CopySucceed);
                    } else if (choice == DialogMoveConfirm.MOVE_PAGE) {
//                        builder.setMessage(R.string.MoveSucceed);
                        FileManagerUtils.deleteFile(srcPath);
                        currentDocument.delete(currentPage);
                        currentDocument.save();
                        if (mListener != null) {
                            mListener.onFinished();
                        }
                    }

                }
                dismissConfirmDialog();
            }
        });

//        mConfirmDialog = createConfirmDialog();
    }

    private void initData() {
        documents = Manifest.getInstance().getDocuments();
        mCurrDocument = Workspace.getInstance().getCurrentDocument();
        mAdapter.setData(documents);
    }

    public void dismissSelf() {
        if (isShowing()) {
            this.dismiss();
        }
    }

    public class PPWNoteListAdapter extends RecyclerView.Adapter {
        private List<Document> mNoteList = new ArrayList<>();
        private int curPosition;

        public PPWNoteListAdapter() {
        }

        public PPWNoteListAdapter(List<Document> noteList) {
            this.mNoteList = noteList;
        }

        public void setData(List<Document> noteList) {
            this.mNoteList = noteList;
            curPosition = mNoteList.indexOf(mCurrDocument);
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(mContext).inflate(R.layout.dialog_move_to_note_item_layout, parent, false);
            return new PPWNoteListViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            Document document = mNoteList.get(position);
            if (holder instanceof PPWNoteListViewHolder) {
                String info = document.getUpdatedTime();
                info = InformationParser.getFormatTimeInfo(info);
                info = document.getCount() + " " + mContext.getString(R.string.pages) + " " + info;
//                info = document.getCount() + mContext.getResources().getString(R.string.pages) + "," + info;
                ((PPWNoteListViewHolder) holder).flContainer.setBackgroundResource(
                        UIConstants.ARRAY_NOTE_COVER[UIConstants.getAvailableCoverIndex(document.getBackgroundResIndex())]
                );
                ((PPWNoteListViewHolder) holder).tvNoteName.setText(document.getTitle());
                ((PPWNoteListViewHolder) holder).tvNoteInfo.setText(info);
                if (position != curPosition) {
                    ((PPWNoteListViewHolder) holder).flContainer.setEnabled(true);
                    ((PPWNoteListViewHolder) holder).ivDisableCover.setVisibility(View.GONE);
                } else {
                    ((PPWNoteListViewHolder) holder).flContainer.setEnabled(false);
                    ((PPWNoteListViewHolder) holder).ivDisableCover.setVisibility(View.VISIBLE);
                }
                ((PPWNoteListViewHolder) holder).flContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDestPosition = position;
                        mDialogConfirm.show();
//                        Toast.makeText(mContext, "click:" + position, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mNoteList.size();
        }
    }

//    public AlertDialog createConfirmDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        builder.setView(mDialogConfirm);
//        final AlertDialog alertDialog = builder.create();
//        alertDialog.getWindow().setLayout(820, 450);
//        return alertDialog;
//    }

    private void dismissConfirmDialog() {
        if (mDialogConfirm != null && mDialogConfirm.isShowing()) {
            mDialogConfirm.dismiss();
        }
    }

    public class PPWNoteListViewHolder extends RecyclerView.ViewHolder {

        public FrameLayout flContainer;
        public TextView tvNoteName;
        public TextView tvNoteInfo;
        public ImageView ivDisableCover;

        public PPWNoteListViewHolder(View itemView) {
            super(itemView);
            flContainer = (FrameLayout) itemView.findViewById(R.id.fl_note_item_container);
            tvNoteName = (TextView) itemView.findViewById(R.id.tv_note_title);
            tvNoteInfo = (TextView) itemView.findViewById(R.id.tv_note_info);
            ivDisableCover = (ImageView) itemView.findViewById(R.id.iv_disable_cover);
        }
    }

    public interface OnFinishedListener {
        void onFinished();
    }

    public void setOnFinishedListener(OnFinishedListener listener) {
        this.mListener = listener;
    }
}
