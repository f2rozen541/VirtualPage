package com.hanvon.virtualpage.notecomponent.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Document;
import com.hanvon.virtualpage.beans.UIConstants;
import com.hanvon.virtualpage.utils.InformationParser;
import com.hanvon.virtualpage.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class NoteListAdapter extends RecyclerView.Adapter {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_TAIL = 2;

    private Context mContext;
    private List<Document> mData;
    private View mHeaderView, mTailView;
    private OnNoteOperateListener mNoteOperateListener;
    private boolean isClickAction = false;
//    private static final float TOLERATE_DIS = 10; // 点击事件允许的位移
    private static final float TOLERATE_DIS = ViewConfiguration.get(BaseApplication.getContext()).getScaledTouchSlop(); // 点击事件允许的位移
    private List<Integer> mBgResList = new ArrayList<>();

    public NoteListAdapter(Context context, List<Document> list) {
        this.mContext = context;
        this.mData = list;
        for (int i = 0; i < UIConstants.ARRAY_NOTE_COVER.length; i++) {
            mBgResList.add(UIConstants.ARRAY_NOTE_COVER[i]);
        }
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        mHeaderView.setVisibility(View.INVISIBLE);
        notifyItemInserted(0);
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public void setTailView(View view) {
        mTailView = view;
        mTailView.setVisibility(View.INVISIBLE);
        notifyItemInserted(getItemCount());
    }

    public View getTailView() {
        return mTailView;
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null && mTailView == null) {
            return TYPE_NORMAL;
        }
        if (mHeaderView != null && position == 0) {
            return TYPE_HEADER;
        }
        if (mTailView != null && position == (getItemCount() - 1)) {
            return TYPE_TAIL;
        }
        return TYPE_NORMAL;
    }

    public void addDatas(List<Document> datas) {
        mData.addAll(datas);
        notifyDataSetChanged();
    }

    /**
     * 设置需要显示的数据，并且通知刷新
     *
     * @param data
     */
    public void setData(List<Document> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER) {
            return new NoteViewHolder(mHeaderView);
        }
        if (mTailView != null && viewType == TYPE_TAIL) {
            return new NoteViewHolder(mTailView);
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.note_list_item_layout, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER || getItemViewType(position) == TYPE_TAIL) {
            return;
        }
        int dataPosition = getDataPosition(holder);
        Document data = mData.get(dataPosition);
        if (holder instanceof NoteViewHolder) {
            NoteViewHolder viewHolder = (NoteViewHolder) holder;
            String title = data.getTitle();
            viewHolder.tvTitle.setText(title);
            String info = data.getUpdatedTime();
            info = InformationParser.getFormatTimeInfo(info);
            info = data.getCount()+ " " + mContext.getString(R.string.pages) + " " + info;
            viewHolder.tvInfo.setText(info);
            int bgResIndex = UIConstants.getAvailableCoverIndex(data.getBackgroundResIndex());
            viewHolder.flNoteItem.setBackgroundResource(UIConstants.ARRAY_NOTE_OPEN_COVER[bgResIndex]);
            viewHolder.llFunction.setVisibility(View.GONE);
            viewHolder.onFunctionClickListener.setPosition(dataPosition);
            viewHolder.ibNew.setOnClickListener(viewHolder.onFunctionClickListener);
            viewHolder.ibSetting.setOnClickListener(viewHolder.onFunctionClickListener);
            viewHolder.ibDelete.setOnClickListener(viewHolder.onFunctionClickListener);
            if (dataPosition == 0) {
                viewHolder.ibDelete.setEnabled(false);
//                viewHolder.ibSetting.setEnabled(false);
            } else {
                viewHolder.ibDelete.setEnabled(true);
//                viewHolder.ibSetting.setEnabled(true);
            }

            viewHolder.onNoteTouchListener.setPosition(dataPosition);
            viewHolder.rl_container.setOnTouchListener(viewHolder.onNoteTouchListener);
//            viewHolder.rl_container.setEnabled(false); // 默认条目都是不可用的，只有执行完放大动画之后才能进行点击
        }
    }


    @Override
    public int getItemCount() {
        int itemCount = mData.size();
        if (mHeaderView != null) {
            itemCount++;
        }
        if (mTailView != null) {
            itemCount++;
        }
        return itemCount;
    }


    public interface OnNoteOperateListener {
        void onNewClick(int dataPosition);

        void onSettingClick(int dataPosition);

        void onDeleteClick(int dataPosition);

        void onNoteClick(int dataPosition);
    }

    public void setOnNoteOperateListener(OnNoteOperateListener listener) {
        this.mNoteOperateListener = listener;
    }

    private int getDataPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position - 1;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rl_container;
        FrameLayout flNoteItem;
        TextView tvTitle;
        TextView tvInfo;
        LinearLayout llFunction;
        ImageButton ibNew;
        ImageButton ibSetting;
        ImageButton ibDelete;
        OnFunctionClickListener onFunctionClickListener;
        OnNoteTouchListener onNoteTouchListener;

        public NoteViewHolder(View itemView) {
            super(itemView);
            if (itemView == mHeaderView || itemView == mTailView) {
                return;
            }
            rl_container = (RelativeLayout) itemView.findViewById(R.id.rl_container);
            flNoteItem = (FrameLayout) itemView.findViewById(R.id.fl_note_item_container);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_note_title);
            tvInfo = (TextView) itemView.findViewById(R.id.tv_note_info);
            llFunction = (LinearLayout) itemView.findViewById(R.id.ll_function);
            ibNew = (ImageButton) llFunction.findViewById(R.id.ib_note_new_page);
            ibSetting = (ImageButton) llFunction.findViewById(R.id.ib_note_setting);
            ibDelete = (ImageButton) llFunction.findViewById(R.id.ib_note_delete);
            onFunctionClickListener = new OnFunctionClickListener();
            onNoteTouchListener = new OnNoteTouchListener();
        }
    }

    public class OnFunctionClickListener implements View.OnClickListener {

        private int mPosition = -1;

        public void setPosition(int position) {
            this.mPosition = position;
        }

        @Override
        public void onClick(View v) {
            if (mNoteOperateListener != null) {
                switch (v.getId()) {
                    case R.id.ib_note_new_page:
                        mNoteOperateListener.onNewClick(mPosition);
                        break;
                    case R.id.ib_note_setting:
                        mNoteOperateListener.onSettingClick(mPosition);
                        break;
                    case R.id.ib_note_delete:
                        mNoteOperateListener.onDeleteClick(mPosition);
                        break;
                }
            }
        }
    }

    /**
     * 设置点击允许的滑动差值监听，区分点击事件还是滑动事件
     */
    public class OnNoteTouchListener implements View.OnTouchListener {
        private int mPosition = -1;
        private float startX;
        private float startY;
        private float deltaX;
        private float deltaY;

        public void setPosition(int position) {
            this.mPosition = position;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    startY = event.getY();
                    deltaX = 0;
                    deltaY = 0;
                    isClickAction = true;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    deltaX += (event.getX() - startX);
                    deltaY += (event.getY() - startY);
                    if (Math.abs(deltaX) > TOLERATE_DIS || Math.abs(deltaY) > TOLERATE_DIS) {
                        isClickAction = false;
                    }
                    startX = event.getX();
                    startY = event.getY();
                    return false;
                case MotionEvent.ACTION_UP:
                    if (isClickAction) {
                        if (mNoteOperateListener != null) {
                            LogUtil.e("Info: this is a Click Action!");
                            mNoteOperateListener.onNoteClick(mPosition);
                        }
                        return true;
                    }
                default:
                    break;
            }
            return false;
        }

    }
}
