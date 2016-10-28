package com.hanvon.virtualpage.pageeditor.tools.text;

import android.graphics.Typeface;

/**
 * -------------------------------
 * Description:
 * 文本参数。提供设置及获取文本内容的字体，颜色，字号，下划线，粗斜体。
 * -------------------------------
 */
public class EditTextParams {
    private Typeface typeface;
    private int fontIndex;
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderline;
    private float textSize;
    private int textColor;
    private String text;

//    public EditTextParams(int typeface, float textSize, int textColor, String text) {
//        this.typeface = typeface;
//        this.textSize = textSize;
//        this.textColor = textColor;
//        this.text = text;
//    }

    public EditTextParams(Typeface typeface, boolean isBold, boolean isItalic, boolean isUnderline, float textSize, int textColor, String text) {
        this.typeface = typeface;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderline = isUnderline;
        this.textSize = textSize;
        this.textColor = textColor;
        this.text = text;
    }

    public EditTextParams(int fontIndex, boolean isBold, boolean isItalic, boolean isUnderline, float textSize, int textColor, String text) {
        this.fontIndex = fontIndex;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderline = isUnderline;
        this.textSize = textSize;
        this.textColor = textColor;
        this.text = text;
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public boolean isUnderline() {
        return isUnderline;
    }

    public void setIsUnderline(boolean isUnderline) {
        this.isUnderline = isUnderline;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public void setIsItalic(boolean isItalic) {
        this.isItalic = isItalic;
    }

    public boolean isBold() {
        return isBold;
    }

    public void setIsBold(boolean isBold) {
        this.isBold = isBold;
    }

    public int getFontIndex() {
        return fontIndex;
    }

    public void setFontIndex(int fontIndex) {
        this.fontIndex = fontIndex;
    }


}

