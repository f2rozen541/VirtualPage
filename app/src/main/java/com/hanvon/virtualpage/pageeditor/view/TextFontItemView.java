package com.hanvon.virtualpage.pageeditor.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.common.EditorState;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/3/10
 * E_mail:  taozhi@hanwang.com.cn
 */
public class TextFontItemView extends FrameLayout {

    private View parentView;
    private FrameLayout fl_container;
    private TextView tv_content;


//    public static final int FONT_ARIAL = 0;
//    public static final int FONT_ARIAL_BLACK = 1;
//    public static final int FONT_ROBOTO = 2;
//    public static final int FONT_ROBOTO_CONDENSED = 3;
//    public static final int FONT_SONGTI = 4;
//    public static final int FONT_KAISHU = 5;
//    public static final int FONT_WRYH = 6;
//    public static final int FONT_DEFAULT = 7;

    public static final String KAISHU_PATH = "font/kaiti.ttf";


    private Context mContext;
    private AutoBgButton abbFontSelectFlag;

    public TextFontItemView(Context context) {
        this(context, null);
    }

    public TextFontItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextFontItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        parentView = LayoutInflater.from(context).inflate(R.layout.text_font_item_layout, this);
        initViews();

    }

    private void initViews() {
        fl_container = (FrameLayout) parentView.findViewById(R.id.fl_container);
        tv_content = (TextView) parentView.findViewById(R.id.tv_content);
        abbFontSelectFlag = (AutoBgButton) parentView.findViewById(R.id.abb_font_select_flag);
    }

    public void setText(String text){
        tv_content.setText(text);
    }

    public void setTextFontByType(int type){
        Typeface typeface = null;
        switch (type){
            case EditorState.FONT_ARIAL:
                typeface = Typeface.create("sans-serif", Typeface.NORMAL);
                break;
            case EditorState.FONT_ARIAL_BLACK:
                typeface = Typeface.create("sans-serif-black", Typeface.NORMAL);
                break;
            case EditorState.FONT_ROBOTO:
                typeface = Typeface.create("monospace", Typeface.NORMAL);
                break;
            case EditorState.FONT_ROBOTO_CONDENSED:
                typeface = Typeface.create("monospace", Typeface.BOLD);
                break;
//            case EditorState.FONT_SONGTI:
//                typeface = Typeface.create("cursive", Typeface.NORMAL);
//                break;
//            case EditorState.FONT_KAISHU:
//                typeface = Typeface.createFromAsset(mContext.getAssets(), KAISHU_PATH);
//                break;
//            case EditorState.FONT_WRYH:
//                typeface = Typeface.create("compact", Typeface.NORMAL);
//                break;
            default:
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
                break;

        }
        tv_content.setTypeface(typeface);
    }

    /**
     * 设置背景色
     * @param color 颜色值
     */
    public void setBgColor(int color){
        fl_container.setBackgroundColor(color);
    }

    /**
     * 设置当前的选中状态
     * @param isSelected 是否选中
     */
    public void setSelectState(boolean isSelected) {
        abbFontSelectFlag.setSelectedState(isSelected);
    }

    /**
     * 将指定的view的字体转换成与当前条目显示的字体
     * @param view 想要进行转换的view
     * @return 经过字体转换之后的view
     */
    public View setViewToDesignatedFont(View view){

        if(view instanceof TextView || view instanceof EditText){
            Typeface typeface = tv_content.getTypeface();
            ((TextView)view).setTypeface(typeface);
        }
        return view;
    }

    /**
     * 获取当前Item的Typeface
     * @return 当前item的Typeface
     */
    public Typeface getTextTypeface() {
        return tv_content.getTypeface();
    }

    /**
     * 通过给出字体名，设置字体格式
     * @param fontName 字体名
     */
    public void setTextFontByName(String fontName){
        Typeface typeface = Typeface.create(fontName, Typeface.NORMAL);
        tv_content.setTypeface(typeface);
    }

    /**
     * 通过给出的位于assets目录下的**.ttf文件所在的路径，设置字体格式
     * @param fontSrcPath .ttf文件路径
     */
    public void setTextFontByPath(String fontSrcPath){
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), fontSrcPath);
//        Typeface typeface = Typeface.create("sans-serif", Typeface.NORMAL);
        tv_content.setTypeface(typeface);
    }
}
