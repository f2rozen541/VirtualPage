package com.hanvon.virtualpage.common;

import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.os.Build;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 *      保存当前的编辑窗体的编辑状态的单例类，这里保存PageEditor界面的编辑器的状态，再重新进入时，这里的状态会被初始化
 * @Author: TaoZhi
 * @Date: 2016/5/25
 * @E_mail: taozhi@hanwang.com.cn
 */
public class EditorState {
    private static EditorState sInstance = null;

    /** 顶部栏功能区选项 */
    public static final int FUNCTION_PAINT = 0;
    public static final int FUNCTION_TEXT = 1;
    public static final int FUNCTION_PHOTO = 2;
    public static final int FUNCTION_ROTATE = 3;
    public static final int FUNCTION_FRAME = 4;

    /** **/
    public static final int PANEL_PAINT = 0;
    public static final int PANEL_TEXT = 1;
    public static final int PANEL_GONE = 2;

    /** 笔型选项*/
    public static final int PAINT_BRUSH = 0;
    public static final int PAINT_PENCIL = 1;
    public static final int PAINT_PEN = 2;
    public static final int PAINT_ERASER = 3;

    /** 粗细选项*/
    public static final int STROKE_THIN = 0;
    public static final int STROKE_MEDIUM = 1;
    public static final int STROKE_THICK = 2;

    /** 字体选项*/
    public static final int FONT_ARIAL = 0;
    public static final int FONT_ARIAL_BLACK = 1;
    public static final int FONT_ROBOTO = 2;
    public static final int FONT_ROBOTO_CONDENSED = 3;
//    public static final int FONT_SONGTI = 4;
//    public static final int FONT_KAISHU = 5;
//    public static final int FONT_WRYH = 6;

    private static final String FONT_ARIAL_NAME = "Arial";
    private static final String FONT_ARIAL_BLACK_NAME = "Arial black";
//    private static final String FONT_ROBOTO_NAME = "Roboto";
    private static final String FONT_ROBOTO_NAME = "Serif";
    private static final String FONT_ROBOTO_CONDENSED_NAME = "Roboto condensed";
//    private static final String FONT_SONGTI_NAME = "宋体";
//    private static final String FONT_KAISHU_NAME = "楷书";
//    private static final String FONT_WRYH_NAME = "微软雅黑";

    private float canvasRate = 1;
    private float canvasTranslateX = 0;
    private float canvasTranslateY = 0;


    /** */
    private EditorState() {
        resetDefaultValues();
    }
    public static EditorState getInstance() {
        if (sInstance == null) {
            synchronized (EditorState.class) {
                if (null == sInstance) {
                    sInstance = new EditorState();
                }
            }
        }
        return sInstance;
    }

    public void resetDefaultValues() {
        initColorPicker();
        initFontStyleArray();
        topViewState = FUNCTION_PAINT;
        bottomViewState = PANEL_PAINT;
        strokeType = PAINT_PEN;
        strokeWidth = STROKE_MEDIUM;
        colorIndex = 0;
        strokeColor = colorList.get(colorIndex);
        textFontIndex = 0;
        fontStyleName = fontsList.get(textFontIndex);
        isBold = false;
        isItalic = false;
        isUnderLine = false;
        textSize = 40;
        textColor = colorList.get(colorIndex);
        shownListState = false;
//        savingFlag = false;
    }

    private void initFontStyleArray() {
        if (fontsList == null) {
            fontsList = new ArrayList<>();
        } else {
            fontsList.clear();
        }
        fontsList.add(FONT_ARIAL_NAME);
        fontsList.add(FONT_ARIAL_BLACK_NAME);
        fontsList.add(FONT_ROBOTO_NAME);
        fontsList.add(FONT_ROBOTO_CONDENSED_NAME);
//            fontsList.add(FONT_SONGTI);
//        fontsList.add(FONT_KAISHU_NAME);
//            fontsList.add(FONT_WRYH);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initColorPicker() {
        if (colorList == null) {
            colorList = new ArrayList<>();
        } else {
            colorList.clear();
        }
        colorList.add(BaseApplication.getContext().getColor(R.color.paintColorBlack));
        colorList.add(BaseApplication.getContext().getColor(R.color.paintColorGray));
        colorList.add(BaseApplication.getContext().getColor(R.color.paintColorBlue));
        colorList.add(BaseApplication.getContext().getColor(R.color.paintColorYellow));
        colorList.add(BaseApplication.getContext().getColor(R.color.paintColorOrange));
        colorList.add(BaseApplication.getContext().getColor(R.color.paintColorRed));
    }

    public void setColorAtPosition(int position, int colorValue) {
        colorList.set(position, colorValue);
    }

    public Typeface getTypeFaceByTypeIndex(int type){
        Typeface typeface = null;
        switch (type){
            case EditorState.FONT_ARIAL:
                typeface = Typeface.create("sans-serif", Typeface.NORMAL);
                break;
            case EditorState.FONT_ARIAL_BLACK:
                typeface = Typeface.create("sans-serif-black", Typeface.NORMAL);
                break;
            case EditorState.FONT_ROBOTO:
//                typeface = Typeface.create("monospace", Typeface.NORMAL);
                typeface = Typeface.create("serif", Typeface.NORMAL);
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
        return typeface;
    }


    private int topViewState;
    private int bottomViewState;
    private int textFontIndex;
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderLine;
    private int textSize;
    private int textColor;
    private int colorIndex;

    private int strokeType;
    private int strokeWidth;
    private int strokeColor;

    private List<Integer> colorList;
    private List<String> fontsList;
    private String fontStyleName;

    private boolean shownListState;
    private boolean savingFlag = false;


    /////////////////////////////////////////////////////////////
    //////////////以下都是bean的Getter和Setter方法/////////////////
    /////////////////////////////////////////////////////////////

    public boolean isSavingFlag() {
        return savingFlag;
    }

    public void setSavingFlag(boolean savingFlag) {
        this.savingFlag = savingFlag;
    }

    public boolean isShownListState() {
        return shownListState;
    }

    public void setShownListState(boolean shownListState) {
        this.shownListState = shownListState;
    }

    public List<String> getFontsList() {
        return fontsList;
    }

    public void setFontsList(List<String> fontsList) {
        this.fontsList = fontsList;
    }

    public String getFontStyleName() {
        return fontStyleName;
    }

    public void setFontStyleName(String fontStyleName) {
        this.fontStyleName = fontStyleName;
    }


    public List<Integer> getColorList() {
        return colorList;
    }

    public void setColorList(List<Integer> colorList) {
        this.colorList = colorList;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
        strokeColor = colorList.get(colorIndex);
        textColor = colorList.get(colorIndex);
    }

    public int getTopViewState() {
        return topViewState;
    }

    public void setTopViewState(int topViewState) {
        this.topViewState = topViewState;
    }

    public int getBottomViewState() {
        return bottomViewState;
    }

    public void setBottomViewState(int bottomViewState) {
        this.bottomViewState = bottomViewState;
    }

    public int getStrokeType() {
        return strokeType;
    }

    public void setStrokeType(int strokeType) {
        this.strokeType = strokeType;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getTextFontIndex() {
        return textFontIndex;
    }

    public void setTextFontIndex(int textFontIndex) {
        this.textFontIndex = textFontIndex;
        fontStyleName = fontsList.get(textFontIndex); // 同时更新名字
    }

    public boolean isBold() {
        return isBold;
    }

    public void setIsBold(boolean isBold) {
        this.isBold = isBold;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public void setIsItalic(boolean isItalic) {
        this.isItalic = isItalic;
    }

    public boolean isUnderLine() {
        return isUnderLine;
    }

    public void setIsUnderLine(boolean isUnderLine) {
        this.isUnderLine = isUnderLine;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public float getCanvasRate(){
        return canvasRate;
    }

    public void setCanvasRate(float rate){
        canvasRate = rate;
    }

    public float getCanvasTranslateX(){
        return canvasTranslateX;
    }

    public void setCanvasTranslateX(float translateX){
        canvasTranslateX = translateX;
    }

    public float getCanvasTranslateY(){
        return canvasTranslateY;
    }

    public void setCanvasTranslateY(float translateY){
        canvasTranslateY = translateY;
    }
}
