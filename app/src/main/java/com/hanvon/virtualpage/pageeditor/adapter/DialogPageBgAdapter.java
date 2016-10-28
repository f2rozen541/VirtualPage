package com.hanvon.virtualpage.pageeditor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.pageeditor.view.AutoBgButton;

import java.util.List;

/**
 * @Description:
 * @Author: TaoZhi
 * @Date: 2016/5/9
 * @E_mail: taozhi@hanwang.com.cn
 */
public class DialogPageBgAdapter extends BaseAdapter {

    private Context context;
    private List<Integer> data;
    private int mFocusPosition;

    public DialogPageBgAdapter(Context context, List<Integer> data) {
        this(context, data, 0);
    }

    public DialogPageBgAdapter(Context context, List<Integer> data, int focusPosition) {
        this.context = context;
        this.data = data;
        this.mFocusPosition = focusPosition;
    }

    public void setFocusPosition(int position) {
        this.mFocusPosition = position;
    }

    public int getFocusPosition() {
        return mFocusPosition;
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
            viewHolder.abbCheckedFlag = (AutoBgButton) convertView.findViewById(R.id.iv_check);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageViewBg.setBackgroundResource(data.get(position));
        if (position == mFocusPosition) {
            viewHolder.abbCheckedFlag.setSelectedState(true);
        } else {
            viewHolder.abbCheckedFlag.setSelectedState(false);
        }

        viewHolder.imageViewBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFocusPosition = position;
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
