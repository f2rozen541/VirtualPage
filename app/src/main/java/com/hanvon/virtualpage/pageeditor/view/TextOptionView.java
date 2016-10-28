package com.hanvon.virtualpage.pageeditor.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.common.EditorState;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/2/25
 * E_mail:  taozhi@hanwang.com.cn
 */
public class TextOptionView extends RelativeLayout implements View.OnClickListener {
    private RelativeLayout parentView;
    private AutoBgButton abb_bold;
    private AutoBgButton abb_italic;
    private AutoBgButton abb_underline;
    private TextStyleListener mTextStyleListener;
    private boolean isBold = false;
    private boolean isItalic = false;
    private boolean isUnderLine = false;
    private Typeface mTypeface;

    private int fontStyle = 0;
    private TextFontView textFontView;

    public TextOptionView(Context context) {
        this(context, null);
    }

    public TextOptionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextOptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parentView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.text_option_layout, this);
        initViews();
    }

    private void initViews() {
        abb_bold = (AutoBgButton)(findViewById(R.id.abb_bold));
        abb_italic = (AutoBgButton)(findViewById(R.id.abb_italic));
        abb_underline = (AutoBgButton)(findViewById(R.id.abb_underline));
        textFontView = (TextFontView) findViewById(R.id.textFontView);

        refreshViews();

        abb_bold.setOnClickListener(this);
        abb_italic.setOnClickListener(this);
        abb_underline.setOnClickListener(this);

        textFontView.setOnFontItemClickListener(new TextFontView.OnFontItemClickListener() {

            @Override
            public void onItemSelect(int position, Typeface typeface) {
                EditorState.getInstance().setTextFontIndex(position);
                mTypeface = typeface;
                isBold = mTypeface.isBold();
                abb_bold.setSelectedState(isBold);
                isItalic = mTypeface.isItalic();
                abb_italic.setSelectedState(isItalic);
                checkFontStyles();
            }
        });
    }

    public void refreshViews() {
        isBold = EditorState.getInstance().isBold();
        isItalic = EditorState.getInstance().isItalic();
        isUnderLine = EditorState.getInstance().isUnderLine();

        abb_bold.setSelectedState(isBold);
        abb_italic.setSelectedState(isItalic);
        abb_underline.setSelectedState(isUnderLine);

        textFontView.setCurrentFontName(EditorState.getInstance().getTextFontIndex());
    }

//    public void hideFontOptionPPW(){
//        textFontView.hidePopupWindow();
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.abb_bold:
                isBold = !isBold;
                abb_bold.setSelectedState(isBold);
                break;
            case R.id.abb_italic:
                isItalic = !isItalic;
                abb_italic.setSelectedState(isItalic);
                break;
            case R.id.abb_underline:
                isUnderLine = !isUnderLine;
                abb_underline.setSelectedState(isUnderLine);
                break;
            default:
                break;
        }
        checkFontStyles();

    }



    private void checkFontStyles(){
        fontStyle = 0;
        if(isBold){
            fontStyle += Typeface.BOLD;
        }
        if(isItalic){
            fontStyle += Typeface.ITALIC;
        }
        mTypeface = Typeface.create(mTypeface, fontStyle);
        if(mTextStyleListener != null){
//            mTextStyleListener.TextFontChanged(mTypeface);
            mTextStyleListener.TextStyleChanged(mTypeface, isUnderLine);
        }
        EditorState.getInstance().setIsBold(isBold);
        EditorState.getInstance().setIsItalic(isItalic);
        EditorState.getInstance().setIsUnderLine(isUnderLine);

    }

    public interface TextStyleListener {
//        void TextTypefaceChanged(int typeFace);
//        void TextFontChanged(Typeface typeface);
//        void TextUnderLineChanged(boolean flag);
        void TextStyleChanged(Typeface typeface, boolean isUnderLine);
    }

    public void setTextStyleListener(TextStyleListener textStyleListener) {
        this.mTextStyleListener = textStyleListener;
    }
}
