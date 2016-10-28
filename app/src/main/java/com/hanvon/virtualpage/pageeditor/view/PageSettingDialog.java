package com.hanvon.virtualpage.pageeditor.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.base.BaseDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shihuijie on 16-4-10.
 */
public class PageSettingDialog extends BaseDialog implements View.OnClickListener {

    private LinearLayout linearLayout;
    private OnBackgroundChangeListener onBgChangeListener;
    public List<Integer> mOptionResIdList;
//    private int currentBgId;
    private int focusPosition = 0; // 设置一个默认值
    private Context mContext;
    private View parentView;

    public PageSettingDialog(Context context) {
        super(context);
    }


//    public PageSettingDialog(Context context, int currentBgId) {
//        this(context, 0, currentBgId);
//    }

//    public PageSettingDialog(Context context, int themeResId, int currentBgId) {
//        super(context, themeResId);
//        mContext = context;
//        this.currentBgId = currentBgId;
//    }

    public PageSettingDialog(Context context, int themeResId, int currentBgIndex) {
        super(context, themeResId);
        mContext = context;
        this.focusPosition = currentBgIndex;
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//    }

    @Override
    public View getContentLayoutView() {
        Log.e("wangkun", "getContentLayoutView: " + this);
        initData();
        initViews();
        return parentView;
    }

    private void initData() {
        if (mOptionResIdList == null) {
            mOptionResIdList = new ArrayList<>();
        }
        mOptionResIdList.clear();
        mOptionResIdList.add(R.drawable.page_editor_opt_default);
        mOptionResIdList.add(R.drawable.page_editor_opt_1);
        mOptionResIdList.add(R.drawable.page_editor_opt_2);
        mOptionResIdList.add(R.drawable.page_editor_opt_3);
        mOptionResIdList.add(R.drawable.page_editor_opt_4);
        mOptionResIdList.add(R.drawable.page_editor_opt_5);
    }

    private void initViews() {
        parentView = LayoutInflater.from(mContext).inflate(R.layout.page_chang_bg, null);
        TextView tvApply = (TextView) parentView.findViewById(R.id.tv_apply);
        TextView tvCancel = (TextView) parentView.findViewById(R.id.tv_cancel);
        tvApply.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        GridView gridBgs = (GridView) parentView.findViewById(R.id.gv_bg_selector);
        PageBgAdapter pageBgAdapter = new PageBgAdapter(mContext, mOptionResIdList);
        gridBgs.setAdapter(pageBgAdapter);
//        setContentView(parentView, new ViewGroup.LayoutParams(
//                mContext.getResources().getDisplayMetrics().widthPixels,
//                mContext.getResources().getDisplayMetrics().heightPixels));
    }

    @Override
    public void onClick(View v) {
        if (onBgChangeListener != null) {
            onBgChangeListener.onButtonClick(v.getId(), focusPosition);
        }
    }

    private class PageBgAdapter extends BaseAdapter {

        private Context context;
        private List<Integer> data;

        public PageBgAdapter(Context context, List<Integer> data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.page_bg_item, null);
                viewHolder.imageViewBg = (ImageView) convertView.findViewById(R.id.iv_page);
                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    viewHolder.imageViewBg.setRotation(90);
                }
                viewHolder.abbCheckedFlag = (AutoBgButton) convertView.findViewById(R.id.iv_check);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.imageViewBg.setBackgroundResource(data.get(position));
            if (position == focusPosition) {
                viewHolder.abbCheckedFlag.setSelectedState(true);
            } else {
                viewHolder.abbCheckedFlag.setSelectedState(false);
            }

            viewHolder.imageViewBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    focusPosition = position;
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }

        private class ViewHolder {
            ImageView imageViewBg;
            AutoBgButton abbCheckedFlag;
        }
    }

    public void setOnBgChangeListener(OnBackgroundChangeListener onBgChangeListener) {
        this.onBgChangeListener = onBgChangeListener;
    }

    public interface OnBackgroundChangeListener {
        void onButtonClick(int viewId, int selectPageBgIndex);
    }

}