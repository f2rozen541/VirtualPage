package com.hanvon.virtualpage.pageeditor.tools.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.utils.BitmapUtil;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * -------------------------------
 * Description:
 * ThumbnailAdapter: the adapter of showing thumbnails
 * -------------------------------
 * Author:  hll
 * Date:    2016/2/24
 */
@SuppressWarnings("rawtypes")
public class ThumbnailAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private int mWidth;
    private int mHeight;
    private List mData;

    public ThumbnailAdapter(Context c, List data, int width, int height) {
        mContext = c;
        mData = data;
        mInflater = (LayoutInflater) c
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWidth = width;
        mHeight = height;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.thumbnail_list_item, null);
            holder.IvThumbnail = (ImageView) convertView
                    .findViewById(R.id.thumbnail_list_item);
            holder.IvThumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
            holder.IvThumbnail.setLayoutParams(new LinearLayout.LayoutParams(
                    mWidth, mHeight));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Object data = mData.get(position);
        WeakReference<Bitmap> bmp = null;
        if (data instanceof Integer) {
            bmp = new WeakReference<Bitmap>(BitmapUtil.decodeResourceBySize(
                    mContext.getResources(), (Integer) data, mWidth, mHeight));
        }
        holder.IvThumbnail.setImageBitmap(bmp.get());
        bmp = null;
        return convertView;
    }

    private static class ViewHolder {
        ImageView IvThumbnail;
    }
}

