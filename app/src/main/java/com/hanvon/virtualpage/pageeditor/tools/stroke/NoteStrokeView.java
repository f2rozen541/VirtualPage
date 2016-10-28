package com.hanvon.virtualpage.pageeditor.tools.stroke;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.hanvon.core.Stroke;
import com.hanvon.virtualpage.beans.UIConstants;
import com.hanvon.virtualpage.common.ElementType;
import com.hanvon.virtualpage.pageeditor.tools.stroke.params.StrokeParams;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * -------------------------------
 * Description:
 * -------------------------------
 */
public class NoteStrokeView extends StrokeView implements Observer {

    private ElementType currentElementType = ElementType.STROKE;
    private List<Stroke> strokeListData;
    private int currentBackgroundID = -1;
    private boolean isNewData = false;

    public NoteStrokeView(Context context, int drawableID) {
        super(context);
        setFocusableInTouchMode(true);
    }

    public NoteStrokeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusableInTouchMode(true);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof ElementType) {
            currentElementType = (ElementType) data;

            if (currentElementType == ElementType.STROKE) {
                setWriteMode(true);
            } else {
                setWriteMode(false);
            }
            requestFocus();
        }
        if (data instanceof StrokeParams) {
            setStyleType(((StrokeParams) data).getStrokeStyleType());
            setWidthType(((StrokeParams) data).getStrokeWidthType());
            setColorType(((StrokeParams) data).getStrokeColor());
        }
    }

    /**
     * 修改画布背景
     * @param bitmap 将数据Bitmap绘制到背景上
     */
    @Override
    protected void fillBackground(Bitmap bitmap) {
        Drawable drawable;
        try {
            drawable = this.getResources().getDrawable(currentBackgroundID);
        } catch (Exception we) {
            drawable = this.getResources().getDrawable(UIConstants.ARRAY_PAGE_BG_REPEAT[0]);
        }
//        BitmapUtil.drawableToBitmap(drawable, bitmap, getBitmapWidth(), getBitmapHeight());
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, getBitmapWidth(), getBitmapHeight());
        drawable.draw(canvas);
    }

    public int getBgDrawableID() {
        return currentBackgroundID;
    }

    public void setBgDrawableID(int bgID) {
        currentBackgroundID = bgID;
        super.changeBackGroundBitmap();
    }
    @Override
    public void setStrokeList(List<Stroke> strokes) {
        super.setStrokeList(strokes);

        StrokeParams strokeParams = new StrokeParams();
        strokeParams.setStrokeColor(getColorType());
        strokeParams.setStrokeWidthType(getWidthType());
        strokeParams.setStrokeStyleType(getStyleType());
        NoteParams.getCurrentPenNoteParams().setCurrentStrokeParams(strokeParams);
    }

    public void setStrokeListData(List<Stroke> strokes){
        this.strokeListData = strokes;
        isNewData = true;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return super.onTouchEvent(event);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(strokeListData != null && isNewData){
            setStrokeList(strokeListData);
            isNewData = false;
        }
    }

    @Override
    public void releaseStrokeViewResource() {
        if (strokeListData != null) {
            strokeListData.clear();
            strokeListData = null;
        }
        super.releaseStrokeViewResource();
    }
}

