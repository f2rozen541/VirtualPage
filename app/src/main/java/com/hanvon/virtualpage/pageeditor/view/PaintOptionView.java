package com.hanvon.virtualpage.pageeditor.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.common.EditorState;
import com.hanvon.virtualpage.common.ElementType;
import com.hanvon.virtualpage.pageeditor.tools.stroke.NoteParams;
import com.hanvon.virtualpage.pageeditor.tools.stroke.params.StrokeStyleType;
import com.hanvon.virtualpage.pageeditor.tools.stroke.params.StrokeWidthType;
import com.hanvon.virtualpage.utils.AnimationUtils;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/2/23
 * E_mail:  taozhi@hanwang.com.cn
 */
public class PaintOptionView extends RelativeLayout implements View.OnClickListener {

    private RelativeLayout parentView;
    private FrameLayout fl_brush;
    private FrameLayout fl_pencil;
    private FrameLayout fl_pen;
    private FrameLayout fl_eraser;
    private AutoBgButton ib_small;
    private AutoBgButton ib_middle;
    private AutoBgButton ib_big;

    private FrameLayout mCurrentPaintView;
    private AutoBgButton mCurrentWidthView;

    private TranslateAnimation mAnimationUp;
    private TranslateAnimation mAnimationDown;
//    private TranslateAnimation mAnimationStart;

//    private List<FrameLayout> mPaintList;


    public PaintOptionView(Context context) {
        this(context, null);
    }

    public PaintOptionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaintOptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parentView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.paint_option_layout, this);
        initViews();
        initData();
    }

    private void initData() {
//        mCurrentWidthView = ib_middle;
//        mCurrentWidthView.setSelectedState(true);

        mAnimationUp = AnimationUtils.getTranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -0.29f,
                100, null);
        mAnimationDown = AnimationUtils.getTranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -0.29f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                100, null);
//        mCurrentPaintView = fl_pen; // 设置当前选中的控件为brush
        setCurrentPaintStyle(EditorState.getInstance().getStrokeType());
        setCurrentStrokeWidth(EditorState.getInstance().getStrokeWidth());

    }


    private void clearViewsAnimation(){
        fl_brush.clearAnimation();
        fl_pen.clearAnimation();
        fl_pencil.clearAnimation();
        fl_eraser.clearAnimation();
    }

    public void setCurrentPaintStyle (int paintStyle) {
        switch (paintStyle) {
            case EditorState.PAINT_BRUSH:
                mCurrentPaintView = fl_brush;
                break;
            case EditorState.PAINT_PENCIL:
                mCurrentPaintView = fl_pencil;
                break;
            case EditorState.PAINT_PEN:
                mCurrentPaintView = fl_pen;
                break;
            case EditorState.PAINT_ERASER:
                mCurrentPaintView = fl_eraser;
                break;
        }
    }

    public void setCurrentStrokeWidth (int strokeWidth) {
        switch (strokeWidth) {
            case EditorState.STROKE_THIN:
                updateFocusItem(ib_small);
                break;
            case EditorState.STROKE_MEDIUM:
                updateFocusItem(ib_middle);
                break;
            case EditorState.STROKE_THICK:
                updateFocusItem(ib_big);
                break;
            default:
                break;
        }
    }

    private void initViews() {
        fl_brush = (FrameLayout) parentView.findViewById(R.id.fl_brush);
        fl_pencil = (FrameLayout) parentView.findViewById(R.id.fl_pencil);
        fl_pen = (FrameLayout) parentView.findViewById(R.id.fl_pen);
        fl_eraser = (FrameLayout) parentView.findViewById(R.id.fl_eraser);
        ib_small = (AutoBgButton) parentView.findViewById(R.id.ib_small);
        ib_middle = (AutoBgButton) parentView.findViewById(R.id.ib_middle);
        ib_big = (AutoBgButton) parentView.findViewById(R.id.ib_big);


        fl_brush.setOnClickListener(this);
        fl_pencil.setOnClickListener(this);
        fl_pen.setOnClickListener(this);
        fl_eraser.setOnClickListener(this);
        ib_small.setOnClickListener(this);
        ib_middle.setOnClickListener(this);
        ib_big.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        // TODO 这里需要一些动画来显示当前选中的笔形 2016年2月21日 13:40:50
        switch (v.getId()){
            case R.id.fl_brush:
                setCurrentPaint(fl_brush);
                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.STROKE);
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeStyleType(StrokeStyleType.BRUSH);
                EditorState.getInstance().setStrokeType(EditorState.PAINT_BRUSH);
                break;
            case R.id.fl_pencil:
                setCurrentPaint(fl_pencil);
                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.STROKE);
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeStyleType(StrokeStyleType.PENCIL);
                EditorState.getInstance().setStrokeType(EditorState.PAINT_PENCIL);
                break;
            case R.id.fl_pen:
                setCurrentPaint(fl_pen);
                EditorState.getInstance().setStrokeType(EditorState.PAINT_PEN);
                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.STROKE);
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeStyleType(StrokeStyleType.PEN);
                break;
            case R.id.fl_eraser:
                setCurrentPaint(fl_eraser);
                EditorState.getInstance().setStrokeType(EditorState.PAINT_ERASER);
                NoteParams.getCurrentPenNoteParams().setCurrentElementType(ElementType.ERASER);
                break;
            case R.id.ib_small:
                updateFocusItem(v);
                EditorState.getInstance().setStrokeWidth(EditorState.STROKE_THIN);
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeWidthType(StrokeWidthType.THIN);
                break;
            case R.id.ib_middle:
                updateFocusItem(v);
                EditorState.getInstance().setStrokeWidth(EditorState.STROKE_MEDIUM);
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeWidthType(StrokeWidthType.MEDIUM);
                break;
            case R.id.ib_big:
                updateFocusItem(v);
                EditorState.getInstance().setStrokeWidth(EditorState.STROKE_THICK);
                NoteParams.getCurrentPenNoteParams().setCurrentStrokeWidthType(StrokeWidthType.WIDE);
                break;
        }
    }

    public boolean isEraserUp(){
        return mCurrentPaintView == fl_eraser;
    }

    /**
     * 更新选中的宽度控件
     * @param view
     */
    private void updateFocusItem(View view) {
        if(mCurrentWidthView == view){
            return;
        }
        if (mCurrentWidthView == null) {
            mCurrentWidthView = (AutoBgButton) view;
            mCurrentWidthView.setSelectedState(true);
        } else {
            mCurrentWidthView.setSelectedState(false);
            mCurrentWidthView = (AutoBgButton) view;
            mCurrentWidthView.setSelectedState(true);
        }
    }

    private void setCurrentPaint(FrameLayout paintView) {
        if(mCurrentPaintView == paintView){
            return;
        }
        clearViewsAnimation();
        mCurrentPaintView.startAnimation(mAnimationDown);
        paintView.startAnimation(mAnimationUp);
        mCurrentPaintView = paintView;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int y = (int) (fl_brush.getHeight() * 0.3);
        fl_brush.offsetTopAndBottom(y);
        fl_pencil.offsetTopAndBottom(y);
        fl_pen.offsetTopAndBottom(y);
        fl_eraser.offsetTopAndBottom(y);
        mCurrentPaintView.startAnimation(AnimationUtils.getTranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -0.29f,
                0, null));
    }
}