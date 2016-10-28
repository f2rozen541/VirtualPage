package com.hanvon.virtualpage.pageeditor.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Document;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.beans.Workspace;
import com.hanvon.virtualpage.common.RotateTransformation;

import java.util.List;

public class Main2Activity extends Activity {
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);
        mViewPager = (ViewPager)findViewById(R.id.mViewPager);
        List<Page> pages = Workspace.getInstance().getCurrentDocument().getPages();
        mViewPager.setAdapter(new MyPagerAdapter(this, pages));
    }
}

class MyPagerAdapter extends PagerAdapter {
    protected LayoutInflater mInflater;
    protected List<Page> mDataList;
    protected Context mContext;

    public MyPagerAdapter(Context context, List<Page> dataList){
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDataList = dataList;
    }

    @Override
    public int getCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = getView(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    public View getView(final int position){
        View view = mInflater.inflate(R.layout.item_pager, null);
        ImageView iv_drawPager = (ImageView)view.findViewById(R.id.iv_drawPager);
        Page pageInfo = mDataList.get(position);
        Glide.with(BaseApplication.getContext())
                .load(pageInfo.getThumbnailFilePath())
                .transform(new RotateTransformation(mContext, 90))
                .into(iv_drawPager);
        TextView testText = (TextView)view.findViewById(R.id.testText);
        testText.setText("Page" + position);
        Button bt_edit = (Button)view.findViewById(R.id.bt_edit);
        bt_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Document mNoteInfo = Workspace.getInstance().getCurrentDocument();
                Workspace.getInstance().setCurrentDocument(mNoteInfo);
                Page curPage = Workspace.getInstance().getCurrentDocument().getPage(position);
                curPage.setOwner(mNoteInfo);
                Workspace.getInstance().setCurrentPage(curPage);
                Intent intent = new Intent(mContext, PageEditorActivity.class);
                mContext.startActivity(intent);
            }
        });
        return view;
    }
}
