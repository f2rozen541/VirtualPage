/*
 * Copyright 2012 Lars Werkman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.larswerkman.holocolorpicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hanvon.virtualpage.R;


public class HuBar extends View {

    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_PARENT = "parent";
    private static final String STATE_COLOR = "color";
    private static final String STATE_VALUE = "value";
    private static final String STATE_ORIENTATION = "orientation";

    /**
     * Constants used to identify orientation.
     */
    private static final boolean ORIENTATION_HORIZONTAL = true;
    private static final boolean ORIENTATION_VERTICAL = false;

    /**
     * Default orientation of the bar.
     */
    private static final boolean ORIENTATION_DEFAULT = ORIENTATION_HORIZONTAL;

    /**
     * The thickness of the bar.
     */
    private int mBarThickness;

    /**
     * The length of the bar.
     */
    private int mBarLength;
    private int mPreferredBarLength;

    /**
     * The radius of the pointer.
     */
//    private int mBarPointerRadius;

    /**
     * The radius of the halo of the pointer.
     */
    private int mBarPointerHaloRadius;

    /**
     * The position of the pointer on the bar.
     */
    private int mBarPointerPosition;

    /**
     * Colors to construct the color bar using {@link android.graphics.LinearGradient}.
     */
    private static final int[] COLORS = new int[] { 0xFFFF0000, 0xFFFFFF00,
            0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000 };

    /**
     * {@code Paint} instance used to draw the bar.
     */
    private Paint mBarPaint;

    /**
     * {@code Paint} instance used to draw the pointer.
     */
    private Paint mBarPointerPaint;

    /**
     * {@code Paint} instance used to draw the halo of the pointer.
     */
    private Paint mBarPointerHaloPaint;

    /**
     * The rectangle enclosing the bar.
     */
    private RectF mBarRect = new RectF();

    /**
     * {@code Shader} instance used to fill the shader of the paint.
     */
    private Shader shader;

    /**
     * {@code true} if the user clicked on the pointer to start the move mode. <br>
     * {@code false} once the user stops touching the screen.
     *
     * @see #onTouchEvent(android.view.MotionEvent)
     */
    private boolean mIsMovingPointer;

    /**
     * The ARGB value of the currently selected color.
     */
    private int mColor;

    /**
     * An array of floats that can be build into a {@code Color} <br>
     * Where we can extract the color from.
     */
    private float[] mHSVColor = new float[3];

    /**
     * Factor used to calculate the position to the Opacity on the bar.
     */
    private float mPosToSatFactor;

    /**
     * Factor used to calculate the Opacity to the postion on the bar.
     */
    private float mSatToPosFactor;


    /**
     * Used to toggle orientation between vertical and horizontal.
     */
    private boolean mOrientation;

    /**
     * {@code onColorChangedListener} instance of the onColorChangedListener
     */
    private OnColorChangedListener onColorChangedListener;

    /**
     * Value of the latest entry of the onValueChangedListener.
     */
//    private int oldChangedListenerValue;
    private int oldChangedListenerColor;

    private String TAG = "HuBar";
    /**
     * {@code ValueBar} instance used to control the Value bar.
     */
    private com.larswerkman.holocolorpicker.ValueBar mValueBar = null;

    /**
     * {@code SVBar} instance used to control the Saturation/Value bar.
     */
    private com.larswerkman.holocolorpicker.SVBar mSVbar = null;

    /**
     * {@code OpacityBar} instance used to control the Opacity bar.
     */
    private com.larswerkman.holocolorpicker.OpacityBar mOpacityBar = null;
    /**
     * {@code SaturationBar} instance used to control the Saturation bar.
     */
    private com.larswerkman.holocolorpicker.SaturationBar mSaturationBar = null;
    /**
     * 当前显示的hu值
     */
    private int mcurrentValue;

    /**
     * An interface that is called whenever the color is changed. Currently it
     * is always called when the color is changes.
     *
     * @author lars
     *
     */
    public interface OnColorChangedListener {
        public void onColorChanged(int color);
    }

    /**
     * Set a onColorChangedListener
     *
     * @param listener {@code OnColorChangedListener}
     */
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.onColorChangedListener = listener;
    }


    public HuBar(Context context) {
        super(context);
        init(null, 0);
    }

    public HuBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HuBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.ColorBars, defStyle, 0);
        final Resources b = getContext().getResources();

        mBarThickness = a.getDimensionPixelSize(
                R.styleable.ColorBars_bar_thickness,
                b.getDimensionPixelSize(R.dimen.bar_thickness));
        mBarLength = a.getDimensionPixelSize(R.styleable.ColorBars_bar_length,
                b.getDimensionPixelSize(R.dimen.bar_length));
        mPreferredBarLength = mBarLength;
//        mBarPointerRadius = a.getDimensionPixelSize(
//                R.styleable.ColorBars_bar_pointer_radius,
//                b.getDimensionPixelSize(R.dimen.bar_pointer_radius));
        mBarPointerHaloRadius = a.getDimensionPixelSize(
                R.styleable.ColorBars_bar_pointer_halo_radius,
                b.getDimensionPixelSize(R.dimen.bar_pointer_halo_radius));
        mOrientation = a.getBoolean(
                R.styleable.ColorBars_bar_orientation_horizontal, ORIENTATION_DEFAULT);

        a.recycle();

        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPaint.setShader(shader);

        mBarPointerPosition = mBarPointerHaloRadius;

        mBarPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPointerHaloPaint.setColor(0xFFF5F5F5);

        mBarPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarPointerPaint.setColor(0xffff0000);
        mBarPointerPaint.setTextSize(24);

        mPosToSatFactor = 1 / ((float) mBarLength);
        mSatToPosFactor = ((float) mBarLength) / 1;
        mcurrentValue = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int intrinsicSize = mPreferredBarLength + (mBarPointerHaloRadius * 2);

        // Variable orientation
        int measureSpec;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            measureSpec = widthMeasureSpec;
        }
        else {
            measureSpec = heightMeasureSpec;
        }
        int lengthMode = MeasureSpec.getMode(measureSpec);
        int lengthSize = MeasureSpec.getSize(measureSpec);

        int length;
        if (lengthMode == MeasureSpec.EXACTLY) {
            length = lengthSize;
        }
        else if (lengthMode == MeasureSpec.AT_MOST) {
            length = Math.min(intrinsicSize, lengthSize);
        }
        else {
            length = intrinsicSize;
        }

        int barPointerHaloRadiusx2 = mBarPointerHaloRadius * 2;
        mBarLength = length - barPointerHaloRadiusx2;
        if(mOrientation == ORIENTATION_VERTICAL) {
            setMeasuredDimension(barPointerHaloRadiusx2, (mBarLength + barPointerHaloRadiusx2));
        }
        else {
            setMeasuredDimension((mBarLength + barPointerHaloRadiusx2), barPointerHaloRadiusx2);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Fill the rectangle instance based on orientation
        int x1, y1;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            x1 = (mBarLength + mBarPointerHaloRadius);
            y1 = mBarThickness;
            mBarLength = w - (mBarPointerHaloRadius * 2);
            mBarRect.set(mBarPointerHaloRadius,
                    (mBarPointerHaloRadius - (mBarThickness / 2)),
                    (mBarLength + (mBarPointerHaloRadius)),
                    (mBarPointerHaloRadius + (mBarThickness / 2)));
        }
        else {
            x1 = mBarThickness;
            y1 = (mBarLength + mBarPointerHaloRadius);
            mBarLength = h - (mBarPointerHaloRadius * 2);
            mBarRect.set((mBarPointerHaloRadius - (mBarThickness / 2)),
                    mBarPointerHaloRadius,
                    (mBarPointerHaloRadius + (mBarThickness / 2)),
                    (mBarLength + (mBarPointerHaloRadius)));
        }

        // Update variables that depend of mBarLength.
        if (!isInEditMode()) {
            shader = new LinearGradient(mBarPointerHaloRadius, 0,
                    x1, y1,COLORS,
                    null, Shader.TileMode.CLAMP);
        } else {
            shader = new LinearGradient(mBarPointerHaloRadius, 0,
                    x1, y1,COLORS, null,
                    Shader.TileMode.CLAMP);
            Color.colorToHSV(0xff81ff00, mHSVColor);
        }

        mBarPaint.setShader(shader);
        mPosToSatFactor = 1 / ((float) mBarLength);
        mSatToPosFactor = ((float) mBarLength) / 1;

        float[] hsvColor = new float[3];
        Color.colorToHSV(mColor, hsvColor);

        if (!isInEditMode()) {
            mBarPointerPosition = Math.round((mBarLength - (mSatToPosFactor * hsvColor[2])) + mBarPointerHaloRadius);
        } else {
            mBarPointerPosition = mBarPointerHaloRadius;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the bar.
//        canvas.drawRect(mBarRect, mBarPaint);
        canvas.drawRoundRect(mBarRect,20,20,mBarPaint);
        // Calculate the center of the pointer.
        int cX, cY;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            cX = mBarPointerPosition;
            cY = mBarPointerHaloRadius;
        }
        else {
            cX = mBarPointerHaloRadius;
            cY = mBarPointerPosition;
        }

//        Logger.e("执行画的方法===>%d", mBarPointerPosition);
        // Draw the pointer halo.
        canvas.drawCircle(cX, cY, mBarPointerHaloRadius, mBarPointerHaloPaint);
        // Draw the pointer.
//        canvas.drawCircle(cX, cY, mBarPointerRadius, mBarPointerPaint);
        Paint.FontMetricsInt fontMetrics = mBarPointerPaint.getFontMetricsInt();
        int baseline = (2 * mBarPointerHaloRadius - fontMetrics.bottom - fontMetrics.top) / 2;
        mBarPointerPaint.setTextAlign(Paint.Align.CENTER);
        mBarPointerPaint.setColor(0xFF585858);
        canvas.drawText(String.valueOf(mcurrentValue), cX, baseline, mBarPointerPaint);

    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);

        // Convert coordinates to our internal coordinate system
        float dimen;
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            dimen = event.getX();
        }
        else {
            dimen = event.getY();
        }

        int intValue = (int) dimen; // 替代了所有的Math.round(dimen)

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsMovingPointer = true;
                // Check whether the user pressed on (or near) the pointer
                if (dimen >= (mBarPointerHaloRadius) && dimen <= (mBarPointerHaloRadius + mBarLength)) {
                    mBarPointerPosition = intValue;
                    calculateColor(intValue);
                    mBarPointerPaint.setColor(mColor);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsMovingPointer) {
                    // Move the the pointer on the bar.
                    if (dimen >= mBarPointerHaloRadius && dimen <= (mBarPointerHaloRadius + mBarLength)) {
                        mBarPointerPosition = intValue;
                        calculateColor(intValue);
                        mBarPointerPaint.setColor(mColor);

                        setOtherBarsListener();
                        invalidate();
                    } else if (dimen < mBarPointerHaloRadius) {
                        mBarPointerPosition = mBarPointerHaloRadius;
                        calculateColor(intValue);
                        mBarPointerPaint.setColor(mColor);
                        setOtherBarsListener();
                        invalidate();
                    } else if (dimen > (mBarPointerHaloRadius + mBarLength)) {
                        mBarPointerPosition = mBarPointerHaloRadius + mBarLength;
                        calculateColor(intValue);
                        mBarPointerPaint.setColor(mColor);
                        setOtherBarsListener();
                        invalidate();
                    }
                }
//                if(onValueChangedListener != null && oldChangedListenerValue != mColor){
//                    onValueChangedListener.onValueChanged(mColor);
//                    oldChangedListenerValue = mColor;
//                }
                break;
            case MotionEvent.ACTION_UP:
                float[] hsv = new float[3];
                Color.colorToHSV(mColor, hsv);
//                Logger.i("MotionEvent.ACTION_UP:HSV{"+hsv[0]+","+hsv[1]+","+hsv[2]+"}===>Color:" + mColor);
                mIsMovingPointer = false;
                break;
        }
        return true;
    }

    private void setOtherBarsListener() {
        if (mOpacityBar != null) {
            mOpacityBar.setColor(mColor);
        }

        if (mValueBar != null) {
            mValueBar.setColor(mColor);
        }

        if (mSaturationBar != null) {
            mSaturationBar.setColor(mColor);
        }

        if (mSVbar != null) {
            mSVbar.setColor(mColor);
        }
    }

    public void setOtherBarsColor(int color) {

        // check of the instance isn't null
        if (mOpacityBar != null) {
            // set the value of the opacity
            mOpacityBar.setColor(mColor);
            mOpacityBar.setOpacity(Color.alpha(color));
        }

        // check if the instance isn't null
        if (mSVbar != null) {
            // the array mHSVColor will be filled with the HSV values of the color.
            Color.colorToHSV(color, mHSVColor);
            mSVbar.setColor(mColor);

            // because of the design of the Saturation/Value bar,
            // we can only use Saturation or Value every time.
            // Here will be checked which we shall use.
            if (mHSVColor[1] < mHSVColor[2]) {
                mSVbar.setSaturation(mHSVColor[1]);
            } else if(mHSVColor[1] > mHSVColor[2]){
                mSVbar.setValue(mHSVColor[2]);
            }
        }

        if (mSaturationBar != null) {
            Color.colorToHSV(color, mHSVColor);
            mSaturationBar.setColor(mColor);
            mSaturationBar.setSaturation(mHSVColor[1]);
        }

        if (mValueBar != null && mSaturationBar == null) {
            Color.colorToHSV(color, mHSVColor);
            mValueBar.setColor(mColor);
            mValueBar.setValue(mHSVColor[2]);
        } else if (mValueBar != null) {
            Color.colorToHSV(color, mHSVColor);
//            mValueBar.setValue(mHSVColor[2]);
            mValueBar.setValue(1 - mHSVColor[2]); // 这里的值好像是反着的，做一下修改，2016年7月7日 17:29:15 by tz
        }
    }
    /**
     * Set the bar color. <br>
     * <br>
     * Its discouraged to use this method.
     *
     * @param color
     */
    public void setColor(int color) {
        mColor = color;
        mBarPointerPaint.setColor(mColor);
        if (onColorChangedListener != null && color != oldChangedListenerColor ) {
            onColorChangedListener.onColorChanged(color);
            oldChangedListenerColor  = color;
        }
        invalidate();
    }
    public void setInitColor(int color) {
        mColor = color;
        int x1, y1;
        if(mOrientation == ORIENTATION_HORIZONTAL) {
            x1 = (mBarLength + mBarPointerHaloRadius);
            y1 = mBarThickness;
        }
        else {
            x1 = mBarThickness;
            y1 = (mBarLength + mBarPointerHaloRadius);
        }
        shader = new LinearGradient(mBarPointerHaloRadius, 0,
                x1, y1, COLORS, null, Shader.TileMode.CLAMP);
        mBarPaint.setShader(shader);

        Color.colorToHSV(mColor, mHSVColor);
        mcurrentValue = (int) mHSVColor[0];
        mBarPointerPosition = mBarPointerHaloRadius + mcurrentValue;
//        calculateColor(mBarPointerPosition);
        mBarPointerPaint.setColor(mColor);
        setOtherBarsColor(mColor);
        if (onColorChangedListener != null && color != oldChangedListenerColor ) {
            onColorChangedListener.onColorChanged(color);
            oldChangedListenerColor  = color;
        }
        invalidate();
    }

    public void setInitColorAndUpdate(int color) {
        Color.colorToHSV(color, mHSVColor);
        int hValue = Math.round(mHSVColor[0]);
        mBarPointerPosition = mBarPointerPosition + hValue;
        mBarPointerPosition = mBarPointerHaloRadius;
        int x1, y1;
        if(mOrientation == ORIENTATION_HORIZONTAL) {
            x1 = (mBarLength + mBarPointerHaloRadius);
            y1 = mBarThickness;
        }
        else {
            x1 = mBarThickness;
            y1 = (mBarLength + mBarPointerHaloRadius);
        }

        shader = new LinearGradient(mBarPointerHaloRadius, 0,
                x1, y1, COLORS, null, Shader.TileMode.CLAMP);
        mBarPaint.setShader(shader);
        calculateColor(mBarPointerPosition);
        mBarPointerPaint.setColor(mColor);
        invalidate();
    }


    /**
     * Set the pointer on the bar. With the opacity value.
     *
     * @param value float between 0 and 1
     */
    public void setValue(float value) {
        mBarPointerPosition = (int) ((mBarLength - (mSatToPosFactor * value)) + mBarPointerHaloRadius);
        calculateColor(mBarPointerPosition);
        mBarPointerPaint.setColor(mColor);
//        if (mPicker != null) {
//            mPicker.setNewCenterColor(mColor);
//            mPicker.changeOpacityBarColor(mColor);
//        }
        invalidate();
    }

    /**
     * Calculate the color selected by the pointer on the bar.
     *
     * @param coord Coordinate of the pointer.
     */
    private void calculateColor(int coord) {
        coord = coord - mBarPointerHaloRadius;
        if (coord < 0) {
            coord = 0;
        } else if (coord > mBarLength) {
            coord = mBarLength;
        }
        float unit;
        unit = mPosToSatFactor * coord;
//        Log.d(TAG, "calculateColor: coord" + coord + "\tunit" + unit + "\tmBarPointerHaloRadius" + mBarPointerHaloRadius);
        if (unit <= 0) {
            mcurrentValue = 0;
            mColor = COLORS[0];
        }else if (unit >= 1) {
            mcurrentValue = 360;
            mColor = COLORS[COLORS.length - 1];
        }else{
            mcurrentValue = (int) (360 * unit);
            float p = unit * (COLORS.length - 1);
            int i = (int) p;
            p -= i;

            int c0 = COLORS[i];
            int c1 = COLORS[i + 1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);

            mColor = Color.argb(a, r, g, b);
        }
    }

    private int ave(int s, int d, float p) {
        return s + Math.round(p * (d - s));
    }
    /**
     * Get the currently selected color.
     *
     * @return The ARGB value of the currently selected color.
     */
    public int getColor() {
        return mColor;
    }


    /**
     * Add a Saturation/Value bar to the color wheel.
     *
     * @param bar The instance of the Saturation/Value bar.
     */
    public void addSVBar(com.larswerkman.holocolorpicker.SVBar bar) {
        mSVbar = bar;
        // Give an instance of the color picker to the Saturation/Value bar.
        mSVbar.setHuBar(this);
        mSVbar.setColor(mColor);
    }

    /**
     * Add a Opacity bar to the color wheel.
     *
     * @param bar The instance of the Opacity bar.
     */
    public void addOpacityBar(com.larswerkman.holocolorpicker.OpacityBar bar) {
        mOpacityBar = bar;
        // Give an instance of the color picker to the Opacity bar.
        mOpacityBar.setHuBar(this);
        mOpacityBar.setColor(mColor);
    }

    public void addSaturationBar(com.larswerkman.holocolorpicker.SaturationBar bar) {
        mSaturationBar = bar;
        mSaturationBar.setHuBar(this);
        mSaturationBar.setColor(mColor);
    }

    public void addValueBar(com.larswerkman.holocolorpicker.ValueBar bar) {
        mValueBar = bar;
        mValueBar.setHuBar(this);
        mValueBar.setColor(mColor);
    }

    /**
     * Used to change the color of the {@code OpacityBar} used by the
     * {@code SVBar} if there is an change in color.
     *
     * @param color int of the color used to change the opacity bar color.
     */
    public void changeOpacityBarColor(int color) {
        if (mOpacityBar != null) {
            mOpacityBar.setColor(color);
        }
    }

    /**
     * Used to change the color of the {@code SaturationBar}.
     *
     * @param color
     *            int of the color used to change the opacity bar color.
     */
    public void changeSaturationBarColor(int color) {
        if (mSaturationBar != null) {
            mSaturationBar.setColor(color);
        }
    }

    /**
     * Used to change the color of the {@code ValueBar}.
     *
     * @param color int of the color used to change the opacity bar color.
     */
    public void changeValueBarColor(int color) {
        if (mValueBar != null) {
            mValueBar.setColor(color);
        }
    }

    /**
     * Checks if there is an {@code OpacityBar} connected.
     *
     * @return true or false.
     */
    public boolean hasOpacityBar(){
        return mOpacityBar != null;
    }

    /**
     * Checks if there is a {@code ValueBar} connected.
     *
     * @return true or false.
     */
    public boolean hasValueBar(){
        return mValueBar != null;
    }

    /**
     * Checks if there is a {@code SaturationBar} connected.
     *
     * @return true or false.
     */
    public boolean hasSaturationBar(){
        return mSaturationBar != null;
    }

    /**
     * Checks if there is a {@code SVBar} connected.
     *
     * @return true or false.
     */
    public boolean hasSVBar(){
        return mSVbar != null;
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable(STATE_PARENT, superState);
        state.putFloatArray(STATE_COLOR, mHSVColor);

        float[] hsvColor = new float[3];
        Color.colorToHSV(mColor, hsvColor);
        state.putFloat(STATE_VALUE, hsvColor[0]);

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable(STATE_PARENT);
        super.onRestoreInstanceState(superState);

        setInitColor(Color.HSVToColor(savedState.getFloatArray(STATE_COLOR)));
        setValue(savedState.getFloat(STATE_VALUE));
    }
}
