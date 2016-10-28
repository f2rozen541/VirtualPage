package com.hanvon.virtualpage.pageeditor.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.common.EditorState;
import com.hanvon.virtualpage.pageeditor.activity.PageEditorActivity;

import java.util.List;

/**
 * -------------------------------
 * Description:
 * <p/> 字体选择控件
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/3/1
 * E_mail:  taozhi@hanwang.com.cn
 */
public class TextFontView extends RelativeLayout implements View.OnClickListener {

    private RelativeLayout parentView;
    private TextView tv_font;
    private AutoBgButton btn_switcher;
    private boolean isWindowOpen;
    private PopupWindow popupWindow;
    private int mPopWindWidth;
    private int mPopWindHeight;
    private Context mContext;
    private OnFontItemClickListener mListener;
    private int mFocusItemIndex;

    public TextFontView(Context context) {
        this(context, null);
    }

    public TextFontView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextFontView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        parentView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.text_font_layout, this);
        initViews();
        initData();
    }

    private void initData() {
        isWindowOpen = false;
        mPopWindWidth = 320;
        mPopWindHeight = EditorState.getInstance().getFontsList().size() * 80 + 30;
        mFocusItemIndex = EditorState.getInstance().getTextFontIndex();
    }

    private void initViews() {
        tv_font = (TextView) parentView.findViewById(R.id.tv_font_text);
        btn_switcher = (AutoBgButton) parentView.findViewById(R.id.abb_font_switcher);
        tv_font.setOnClickListener(this);
        btn_switcher.setOnClickListener(this);
    }

    public void setPopWindowSize(int width, int height){
        mPopWindWidth = width;
        mPopWindHeight = height;
    }

    public void setCurrentFontName(int fontTypeIndex) {
        TextFontItemView textFontItemView = new TextFontItemView(mContext);
        textFontItemView.setTextFontByType(fontTypeIndex);
        textFontItemView.setViewToDesignatedFont(tv_font);
        tv_font.setText(EditorState.getInstance().getFontStyleName());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_font_text:
            case R.id.abb_font_switcher:
                if (PageEditorActivity.getInstance() != null) {
                    PageEditorActivity.getInstance().setPageListState(false);
                }
                if(isWindowOpen) { // 如果打开状态，就关闭
                    hidePopupWindow();
                }else{ // 否则就打开
                    openPopupWindow();
                }
                break;

            default :
                break;
        }
    }

    public void openPopupWindow() {
        if (popupWindow == null) {
            ListView fontList = new ListView(getContext());
            fontList.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            fontList.setBackgroundResource(R.drawable.font_menu);
            fontList.setItemsCanFocus(true);
            final List<String> dataList = EditorState.getInstance().getFontsList();
//            mPopWindHeight = 80 * dataList.size();
            fontList.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return dataList.size();
                }

                @Override
                public Object getItem(int position) {
                    return dataList.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    final TextFontItemView fontItemView = new TextFontItemView(mContext);
                    fontItemView.setText(dataList.get(position));
                    fontItemView.setTextFontByType(position);
                    fontItemView.setSelectState(false);
                    if (position % 2 == 0) {
                        fontItemView.setBgColor(0xFFFFFF);
                    }
                    if (mFocusItemIndex == position) {
                        fontItemView.setSelectState(true);
                    }
                    fontItemView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tv_font.setText(dataList.get(position));
                            fontItemView.setViewToDesignatedFont(tv_font);
                            if(mListener != null){
                                mListener.onItemSelect(position, fontItemView.getTextTypeface());
                            }
                            mFocusItemIndex = position;
                            EditorState.getInstance().setTextFontIndex(position);
                            hidePopupWindow();
                        }
                    });
                    return fontItemView;
                }
            });
//            fontList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    TextFontItemView textFontItemView = (TextFontItemView) view;
//                    tv_font.setText(dataList.get(position));
//                    ((TextFontItemView) view).setViewToDesignatedFont(tv_font);
//                    if(mListener != null){
//                        mListener.onItemSelect(position, textFontItemView.getTextTypeface());
//                    }
//                    mFocusItemIndex = position;
//                    EditorState.getInstance().setTextFontIndex(position);
//                    hidePopupWindow();
//                }
//            });
            fontList.setDividerHeight(0);
            FrameLayout layout = new FrameLayout(getContext());
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            layout.addView(fontList, layoutParams);
            popupWindow = new PopupWindow(layout, mPopWindWidth, mPopWindHeight);
            popupWindow.setTouchable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    btn_switcher.setSelectedState(false);// 修改按钮显示的背景
                }
            });
        }
        int []location = new int[2];
        parentView.getLocationOnScreen(location);

        int popupWindow_y = location[1] - 24 - mPopWindHeight;
        int viewDeviation = (mPopWindWidth - parentView.getMeasuredWidth()) / 2; // 两个视图之间差值的一半
        int popupWindow_x = location[0] - viewDeviation;

        popupWindow.showAtLocation(parentView, Gravity.NO_GRAVITY, popupWindow_x, popupWindow_y);
        isWindowOpen = true;
        btn_switcher.setSelectedState(isWindowOpen);// 修改按钮显示的背景
    }

    private void hidePopupWindow(){
        if(popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
        }
        isWindowOpen = false;
    }

    /**
     * 字体条目被点击时候的回调监听接口
     */
    public interface OnFontItemClickListener{
        /**
         * 当条目选中时，传递当前的条目位置和Typeface
         * @param position 当前字体条目位置
         * @param typeface 当前字体条目的Typeface
         */
        void onItemSelect(int position, Typeface typeface);

    }

    public void setOnFontItemClickListener(OnFontItemClickListener listener){
        this.mListener = listener;
    }

}
