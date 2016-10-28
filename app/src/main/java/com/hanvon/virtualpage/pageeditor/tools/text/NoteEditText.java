package com.hanvon.virtualpage.pageeditor.tools.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.smartpad.SmartpadManager;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsoluteLayout;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.common.EditorState;
import com.hanvon.virtualpage.common.ElementLayout;
import com.hanvon.virtualpage.common.ElementType;
import com.hanvon.virtualpage.pageeditor.activity.PageEditorActivity;
import com.hanvon.virtualpage.pageeditor.tools.stroke.NoteParams;
import com.hanvon.virtualpage.pageeditor.view.TextFontItemView;

/**
 * -------------------------------
 * Description:
 * 文本对象，继承ElementLayout。
 * 提供方法：NoteEditText，添加边框，touch时间处理（文本的缩放，移动）
 * -------------------------------
 */
public class NoteEditText extends ElementLayout {
    private final int DEFAULT_WIDTH = 500;
    private final int DEFAULT_HEIGHT = 100;
    private final int DEFAULT_TEXT_SIZE = 15;
    private static InputMethodManager imm;
    private final int DEFAULT_BOUNDSIZE = 20;
    private boolean isBoundDown = false;
    private NoteEditTextContent mContentView;
    private Context mContext;
    private int fontTypeIndex;
    private Typeface typeFace;
    private boolean isBold;
    private boolean isItalic;

    public NoteEditText(Context context) {
        super(context);
        mContext = context;
//        initContextView();
        initInputMethodManager();
    }

    public NoteEditText(Context context, int boundSize) {
        super(context, boundSize);
        mContext = context;
//        initContextView();
        initInputMethodManager();
    }

    private void initContextView() {
        TextFontItemView textFontItemView = new TextFontItemView(mContext);
        fontTypeIndex = EditorState.getInstance().getTextFontIndex();
        textFontItemView.setTextFontByType(fontTypeIndex);
        Typeface textTypeface = textFontItemView.getTextTypeface();
        int fontStyle = 0;
        isBold = EditorState.getInstance().isBold();
        isItalic = EditorState.getInstance().isItalic();
        if (isBold) {
            fontStyle += Typeface.BOLD;
        }
        if (isItalic) {
            fontStyle += Typeface.ITALIC;
        }
        typeFace = Typeface.create(textTypeface, fontStyle);
        mContentView.setTypeface(typeFace);
        mContentView.getNoteEditText().textUnderLineChanged(EditorState.getInstance().isUnderLine());
        mContentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, EditorState.getInstance().getTextSize());
        mContentView.setTextColor(EditorState.getInstance().getTextColor());
    }

    private void initInputMethodManager() {
        if (imm == null)
            imm = (InputMethodManager) (getContext())
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void addBounds() {
        super.addBounds();
        if (mBoundType == 0) {
            for (int i = 0; i < mBorderList.length; i++) {
                mBorderList[i] = new NoteEditTextBorder_Default(getContext(), i);
                this.addView(mBorderList[i]);
            }
            for (int i = 0; i < mCornerList.length; i++) {
                mCornerList[i] = new NoteEditTextCorner_Default(getContext(), i);
                this.addView(mCornerList[i]);
            }
        }
    }

    @Override
    public void addContentView() {
        mContentView = new NoteEditTextContent(getContext());
        RelativeLayout.LayoutParams rlLPcontent = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rlLPcontent.setMargins(mBoundSize, mBoundSize, mBoundSize, mBoundSize);
        mContentView.setLayoutParams(rlLPcontent);
        mContentView.setBackgroundColor(Color.TRANSPARENT);
        mContentView.setFocusableInTouchMode(true);
        initContextView();
        mContentView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    removeBounds();
                    if (imm.isActive(mContentView)) {
                        imm.hideSoftInputFromWindow(
                                mContentView.getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    if (v instanceof EditText) {
                        String text = ((EditText) v).getText().toString();
                        ((EditText) v).setText(text.trim());
                    }
                } else {
                    BaseApplication.getApplication().setPenKeyState(SmartpadManager.PEN_KEY_LED_STATE_OFF);
                    setBoundType(0);
                    addBounds();
                    //imm.showSoftInput(mContentView, 0);

                    if (NoteParams.getCurrentPenNoteParams().getCurrentElementType() == ElementType.TEXT
                            && EditorState.getInstance().getTopViewState() == EditorState.FUNCTION_TEXT) {
                        if(mContentView != null && !getContentViewText().equals("")){
//                            if (Typeface.DEFAULT.equals(typeFace)) {
//                                mContentView.setTypeface(Typeface.SERIF);
//                            } else {
//                                mContentView.setTypeface(Typeface.DEFAULT);
//                            }
                            EditorState.getInstance().setIsUnderLine(mContentView.getPaint().isUnderlineText());
                        //                        fontTypeIndex = EditorState.getInstance().getTextFontIndex();
                            mContentView.getNoteEditText().setContentViewTextTypeface(typeFace);
                            EditorState.getInstance().setTextFontIndex(getFontTypeIndex());
                            EditorState.getInstance().setIsBold(typeFace.isBold());
                            EditorState.getInstance().setIsItalic(typeFace.isItalic());
                            EditorState.getInstance().setTextSize((int) mContentView.getTextSize());
                            PageEditorActivity.bottomView.refreshTextOptionViewState();
                        }
                    } else {
                        v.clearFocus();
                    }
                }
            }
        });

        addView(mContentView);
        mContentView.setGravity(ALIGN_PARENT_TOP);

        Object tag = this.getTag();
        if (tag instanceof EditTextParams) {
            EditTextParams params = (EditTextParams) tag;
            fontTypeIndex = params.getFontIndex();
            Typeface typeface = EditorState.getInstance().getTypeFaceByTypeIndex(fontTypeIndex);
            int fontStyle = 0;
            isBold = params.isBold();
            isItalic = params.isItalic();
            if (isBold) {
                fontStyle += Typeface.BOLD;
            }
            if (isItalic) {
                fontStyle += Typeface.ITALIC;
            }
            typeFace = Typeface.create(typeface, fontStyle);
            mContentView.setTypeface(typeFace);
            mContentView.getPaint().setUnderlineText(params.isUnderline());
            mContentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, params.getTextSize());
            mContentView.setTextColor(params.getTextColor());
            mContentView.setText(params.getText());
        }
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        AbsoluteLayout.LayoutParams param = (AbsoluteLayout.LayoutParams) params;

        if (param.width < DEFAULT_WIDTH) {
            param.width = DEFAULT_WIDTH;
        }
        int textHeight;
        if (mContentView != null) {
            textHeight = (int) (mContentView.getTextSize() * 1.5);
        } else {
            textHeight = (int) (DEFAULT_TEXT_SIZE * 1.5);
        }

        if (param.height < DEFAULT_HEIGHT + mBoundSize * 3 + textHeight)
            param.height = DEFAULT_HEIGHT + mBoundSize * 3 + textHeight;

        super.setLayoutParams(param);
    }

    public class NoteEditTextContent extends EditText {
        public NoteEditTextContent(Context context) {
            super(context);
        }

        public NoteEditText getNoteEditText() {
            return NoteEditText.this;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (EditorState.getInstance().getStrokeType() == EditorState.PAINT_ERASER
                    || NoteParams.getCurrentPenNoteParams().getCurrentElementType() == ElementType.STROKE) {
                return false;
            }
            return super.onTouchEvent(event);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }
    }

    @Override
    public void onRemovedFromParent() {
        if (imm.isActive(mContentView))
            imm.hideSoftInputFromWindow(mContentView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //change size
    public void textSizeChanged(int value) {
        mContentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, value);
        NoteEditText.this.setLayoutParams(getLayoutParams());
    }

    //change typeFace
    public void textTypefaceChanged(int typeFace) {
        mContentView.setTypeface(Typeface.DEFAULT, typeFace);
    }

    //change color
    public void textColorChanged(int value) {
        mContentView.setTextColor(value);
    }

    //change underLine
    public void textUnderLineChanged(boolean flag) {
        mContentView.getPaint().setUnderlineText(flag);
//        if(flag){
//            mContentView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
//            mContentView.getPaint().setAntiAlias(true);
//        } else{
//            mContentView.getPaint().setFlags(0);
//        }
//        mContentView.postInvalidate();

    }

    public class NoteEditTextCorner_Default extends ElementCorner {
        public NoteEditTextCorner_Default(Context context, int cornerType) {
            super(context, cornerType);
        }

        //draw corner
        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint defaultPaint = new Paint();
            defaultPaint.setStyle(Paint.Style.STROKE);
            if (!isBoundDown) {
                defaultPaint.setColor(Color.BLACK);
            } else {
                defaultPaint.setColor(Color.BLUE);
            }

            if (cornerType == 0) {
                canvas.drawRect(mBoundSize - DEFAULT_BOUNDSIZE, mBoundSize
                                - DEFAULT_BOUNDSIZE, mBoundSize - 1, mBoundSize - 1,
                        defaultPaint);
            } else if (cornerType == 1) {
                canvas.drawRect(0, mBoundSize - DEFAULT_BOUNDSIZE,
                        DEFAULT_BOUNDSIZE - 1, mBoundSize - 1, defaultPaint);
            } else if (cornerType == 2) {
                canvas.drawRect(0, 0, DEFAULT_BOUNDSIZE - 1,
                        DEFAULT_BOUNDSIZE - 1, defaultPaint);
            } else if (cornerType == 3) {
                canvas.drawRect(mBoundSize - DEFAULT_BOUNDSIZE, 0,
                        mBoundSize - 1, DEFAULT_BOUNDSIZE - 1, defaultPaint);
            }
        }

        int tmpX, tmpY;

        //drag four corners
        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (EditorState.getInstance().getStrokeType() == EditorState.PAINT_ERASER) {
                return false;
            }

            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            AbsoluteLayout.LayoutParams param = (AbsoluteLayout.LayoutParams) NoteEditText.this
                    .getLayoutParams();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    requestFocus();
                    if (imm.isActive(mContentView)) {
                        imm.hideSoftInputFromWindow(mContentView.getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }

                    tmpX = x;
                    tmpY = y;
                    isBoundDown = true;
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (cornerType == 0) {//drag left top
                        if (PageEditorActivity.canvasRotation == 0) {
                            if (param.width - DEFAULT_WIDTH < x - tmpX) {
                                x = param.width - DEFAULT_WIDTH + tmpX;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < y - tmpY) {
                                y = param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT + tmpY;
                            }

                            param.x += x - tmpX;
                            param.y += y - tmpY;
                            param.width -= x - tmpX;
                            param.height -= y - tmpY;
                        } else if (PageEditorActivity.canvasRotation == 90) {
                            if (param.width - DEFAULT_WIDTH < y - tmpY) {
                                y = param.width - DEFAULT_WIDTH + tmpY;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < tmpX - x) {
                                x = -param.height + mBoundSize * 3
                                        + (int) (mContentView.getTextSize() * 1.5)
                                        + DEFAULT_HEIGHT + tmpX;
                            }

                            param.x += y - tmpY;
                            param.y -= x - tmpX;
                            param.width -= y - tmpY;
                            param.height += x - tmpX;
                        } else if (PageEditorActivity.canvasRotation == 180) {
                            if (param.width - DEFAULT_WIDTH < tmpX - x) {
                                x = -param.width + DEFAULT_WIDTH + tmpX;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < tmpY - y) {
                                y = -param.height + mBoundSize * 3
                                        + (int) (mContentView.getTextSize() * 1.5)
                                        + DEFAULT_HEIGHT + tmpY;
                            }

                            param.x -= x - tmpX;
                            param.y -= y - tmpY;
                            param.width += x - tmpX;
                            param.height += y - tmpY;
                        } else if (PageEditorActivity.canvasRotation == 270) {
                            if (param.width - DEFAULT_WIDTH < tmpY - y) {
                                y = -param.width + DEFAULT_WIDTH + tmpY;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < x - tmpX) {
                                x = param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT + tmpX;
                            }

                            param.x -= y - tmpY;
                            param.y += x - tmpX;
                            param.width += y - tmpY;
                            param.height -= x - tmpX;
                        }
                    } else if (cornerType == 1) {//drag right top
                        if (PageEditorActivity.canvasRotation == 0) {
                            if (param.width - DEFAULT_WIDTH < tmpX - x) {
                                x = tmpX - param.width + DEFAULT_WIDTH;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < y - tmpY) {
                                y = param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT + tmpY;
                            }

                            param.y += y - tmpY;
                            param.width += x - tmpX;
                            param.height -= y - tmpY;
                        } else if (PageEditorActivity.canvasRotation == 90) {
                            if (param.width - DEFAULT_WIDTH < tmpY - y) {
                                y = tmpY - param.width + DEFAULT_WIDTH;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < tmpX - x) {
                                x = -param.height + mBoundSize * 3
                                        + (int) (mContentView.getTextSize() * 1.5)
                                        + DEFAULT_HEIGHT + tmpX;
                            }

                            param.y -= x - tmpX;
                            param.width += y - tmpY;
                            param.height += x - tmpX;
                        } else if (PageEditorActivity.canvasRotation == 180) {
                            if (param.width - DEFAULT_WIDTH < x - tmpX) {
                                x = tmpX + param.width - DEFAULT_WIDTH;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < tmpY - y) {
                                y = -param.height + mBoundSize * 3
                                        + (int) (mContentView.getTextSize() * 1.5)
                                        + DEFAULT_HEIGHT + tmpY;
                            }

                            param.y -= y - tmpY;
                            param.width -= x - tmpX;
                            param.height += y - tmpY;
                        } else if (PageEditorActivity.canvasRotation == 270) {
                            if (param.width - DEFAULT_WIDTH < y - tmpY) {
                                y = tmpY + param.width - DEFAULT_WIDTH;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < x - tmpX) {
                                x = param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT + tmpX;
                            }

                            param.y += x - tmpX;
                            param.width -= y - tmpY;
                            param.height -= x - tmpX;
                        }
                    } else if (cornerType == 2) {//drag right bottom
                        if (PageEditorActivity.canvasRotation == 0) {
                            if (param.width - DEFAULT_WIDTH < tmpX - x)
                                x = tmpX - param.width + DEFAULT_WIDTH;
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < tmpY - y) {
                                y = tmpY - param.height + mBoundSize * 3
                                        + (int) (mContentView.getTextSize() * 1.5)
                                        + DEFAULT_HEIGHT;
                            }

                            param.width += x - tmpX;
                            param.height += y - tmpY;
                        } else if (PageEditorActivity.canvasRotation == 90) {
                            if (param.width - DEFAULT_WIDTH < tmpY - y) {
                                y = tmpY - param.width + DEFAULT_WIDTH;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < x - tmpX) {
                                x = tmpX + param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT;
                            }

                            param.width += y - tmpY;
                            param.height -= x - tmpX;
                        } else if (PageEditorActivity.canvasRotation == 180) {
                            if (param.width - DEFAULT_WIDTH < x - tmpX)
                                x = tmpX + param.width - DEFAULT_WIDTH;
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < y - tmpY) {
                                y = tmpY + param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT;
                            }

                            param.width -= x - tmpX;
                            param.height -= y - tmpY;
                        } else if (PageEditorActivity.canvasRotation == 270) {
                            if (param.width - DEFAULT_WIDTH < y - tmpY) {
                                y = tmpY + param.width - DEFAULT_WIDTH;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < tmpX - x) {
                                x = tmpX - param.height + mBoundSize * 3
                                        + (int) (mContentView.getTextSize() * 1.5)
                                        + DEFAULT_HEIGHT;
                            }

                            param.width -= y - tmpY;
                            param.height += x - tmpX;
                        }
                    } else if (cornerType == 3) {//drag left bottom
                        if (PageEditorActivity.canvasRotation == 0) {
                            if (param.width - DEFAULT_WIDTH < x - tmpX) {
                                x = param.width - DEFAULT_WIDTH + tmpX;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < tmpY - y) {
                                y = tmpY - param.height + mBoundSize * 3
                                        + (int) (mContentView.getTextSize() * 1.5)
                                        + DEFAULT_HEIGHT;
                            }

                            param.x += x - tmpX;
                            param.width -= x - tmpX;
                            param.height += y - tmpY;
                        } else if (PageEditorActivity.canvasRotation == 90) {
                            if (param.width - DEFAULT_WIDTH < y - tmpY) {
                                y = param.width - DEFAULT_WIDTH + tmpY;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < x - tmpX) {
                                x = tmpX + param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT;
                            }

                            param.x += y - tmpY;
                            param.width -= y - tmpY;
                            param.height -= x - tmpX;
                        } else if (PageEditorActivity.canvasRotation == 180) {
                            if (param.width - DEFAULT_WIDTH < tmpX - x) {
                                x = -param.width + DEFAULT_WIDTH + tmpX;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < y - tmpY) {
                                y = tmpY + param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT;
                            }

                            param.x -= x - tmpX;
                            param.width += x - tmpX;
                            param.height -= y - tmpY;
                        } else if (PageEditorActivity.canvasRotation == 270) {
                            if (param.width - DEFAULT_WIDTH < tmpY - y) {
                                y = -param.width + DEFAULT_WIDTH + tmpY;
                            }
                            if (param.height - mBoundSize * 3
                                    - (int) (mContentView.getTextSize() * 1.5)
                                    - DEFAULT_HEIGHT < tmpX - x) {
                                x = tmpX - param.height + mBoundSize * 3
                                        + (int) (mContentView.getTextSize() * 1.5)
                                        + DEFAULT_HEIGHT;
                            }

                            param.x -= y - tmpY;
                            param.width += y - tmpY;
                            param.height += x - tmpX;
                        }
                    }
                    NoteEditText.this.setLayoutParams(param);
                    invalidate();
                    tmpX = x;
                    tmpY = y;

                    break;
                case MotionEvent.ACTION_UP:
                    //mContentView.setHint("please_input_word");
                    imm.showSoftInput(mContentView, 0);
                    isBoundDown = false;
                    invalidate();

                    Rect rect = new Rect();
                    rect.left = param.x;
                    rect.top = param.y;
                    rect.right = param.x + param.width;
                    rect.bottom = param.y + param.height;
                    mUndoLayoutParamsList.add(rect);
//                    PageEditorActivity.addRecord(NoteEditText.this);
                    if (PageEditorActivity.getInstance() != null) {
                        PageEditorActivity.getInstance().addRecord(NoteEditText.this);
                    }
                    break;
            }
            return true;
        }
    }

    public class NoteEditTextBorder_Default extends ElementBorder {
        public NoteEditTextBorder_Default(Context context, int borderType) {
            super(context, borderType);
        }

        @Override
        public void onDraw(Canvas canvas) {//draw line
            super.onDraw(canvas);

            Paint linePaint = new Paint();
            linePaint.setStyle(Paint.Style.STROKE);

            if (!isBoundDown) {
                linePaint.setColor(Color.BLACK);
            } else {
                linePaint.setColor(Color.BLUE);
            }

            Paint defaultPaint = new Paint();
            defaultPaint.setStyle(Paint.Style.STROKE);

            if (!isBoundDown) {
                defaultPaint.setColor(Color.BLACK);
            } else {
                defaultPaint.setColor(Color.BLUE);
            }

            Path pathA = new Path();
            Path pathB = new Path();

            PathEffect effects = new DashPathEffect(new float[]{5, 3}, 1);

            if (mBorderType == 0) {// top border

                pathA.moveTo(0, mBoundSize - DEFAULT_BOUNDSIZE / 2);
                pathA.lineTo(getWidth() / 2 - DEFAULT_BOUNDSIZE / 2, mBoundSize
                        - DEFAULT_BOUNDSIZE / 2);

                canvas.drawRect(getWidth() / 2 - DEFAULT_BOUNDSIZE / 2,
                        mBoundSize - DEFAULT_BOUNDSIZE, getWidth() / 2
                                + DEFAULT_BOUNDSIZE / 2 - 1, mBoundSize - 1,
                        defaultPaint);

                pathB.moveTo(getWidth() / 2 + DEFAULT_BOUNDSIZE / 2 - 1,
                        mBoundSize - DEFAULT_BOUNDSIZE / 2);
                pathB.lineTo(getWidth(), mBoundSize - DEFAULT_BOUNDSIZE / 2);
            } else if (mBorderType == 1) {// right border

                pathA.moveTo(DEFAULT_BOUNDSIZE / 2, 0);
                pathA.lineTo(DEFAULT_BOUNDSIZE / 2, getHeight() / 2
                        - DEFAULT_BOUNDSIZE / 2);

                canvas.drawRect(0, getHeight() / 2 - DEFAULT_BOUNDSIZE / 2,
                        DEFAULT_BOUNDSIZE - 1, getHeight() / 2
                                + DEFAULT_BOUNDSIZE / 2 - 1, defaultPaint);

                pathB.moveTo(DEFAULT_BOUNDSIZE / 2, getHeight() / 2
                        + DEFAULT_BOUNDSIZE / 2 - 1);
                pathB.lineTo(DEFAULT_BOUNDSIZE / 2, getHeight());
            } else if (mBorderType == 2) {// bottom border

                pathA.moveTo(0, DEFAULT_BOUNDSIZE / 2);
                pathA.lineTo(getWidth() / 2 - DEFAULT_BOUNDSIZE / 2,
                        DEFAULT_BOUNDSIZE / 2);

                canvas.drawRect(getWidth() / 2 - DEFAULT_BOUNDSIZE / 2, 0,
                        getWidth() / 2 + DEFAULT_BOUNDSIZE / 2 - 1,
                        DEFAULT_BOUNDSIZE - 1, defaultPaint);

                pathB.moveTo(getWidth() / 2 + DEFAULT_BOUNDSIZE / 2 - 1,
                        DEFAULT_BOUNDSIZE / 2);
                pathB.lineTo(getWidth(), DEFAULT_BOUNDSIZE / 2);
            } else if (mBorderType == 3) {// left border

                pathA.moveTo(mBoundSize - DEFAULT_BOUNDSIZE / 2, 0);
                pathA.lineTo(mBoundSize - DEFAULT_BOUNDSIZE / 2, getHeight() / 2
                        - DEFAULT_BOUNDSIZE / 2);

                canvas.drawRect(mBoundSize - DEFAULT_BOUNDSIZE, getHeight() / 2
                        - DEFAULT_BOUNDSIZE / 2, mBoundSize - 1, getHeight() / 2
                        + DEFAULT_BOUNDSIZE / 2 - 1, defaultPaint);

                pathB.moveTo(mBoundSize - DEFAULT_BOUNDSIZE / 2, getHeight() / 2
                        + DEFAULT_BOUNDSIZE / 2 - 1);
                pathB.lineTo(mBoundSize - DEFAULT_BOUNDSIZE / 2, getHeight());
            }
            linePaint.setPathEffect(effects);
            canvas.drawPath(pathA, linePaint);
            canvas.drawPath(pathB, linePaint);
        }

        int tmpX, tmpY;
        boolean dragFlag;

        @Override
        public boolean onTouchEvent(MotionEvent event) {//drag line

            if (EditorState.getInstance().getStrokeType() == EditorState.PAINT_ERASER) {
                return false;
            }

            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            AbsoluteLayout.LayoutParams param = (AbsoluteLayout.LayoutParams) NoteEditText.this
                    .getLayoutParams();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    requestFocus();

                    if (imm.isActive(mContentView)) {
                        imm.hideSoftInputFromWindow(mContentView.getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }

                    tmpX = x;
                    tmpY = y;
                    int borderX = (int) event.getX();
                    int borderY = (int) event.getY();
                    if ((mBorderType == 0
                            && borderX >= getWidth() / 2 - mBoundSize / 2 - 10
                            && borderX <= getWidth() / 2 + mBoundSize / 2 + 10)
                            ||
                            (mBorderType == 1
                                    && borderY >= getHeight() / 2 - mBoundSize / 2 - 10
                                    && borderY <= getHeight() / 2 + mBoundSize / 2 + 10)
                            ||
                            (mBorderType == 2
                                    && borderX >= getWidth() / 2 - mBoundSize / 2 - 10
                                    && borderX <= getWidth() / 2 + mBoundSize / 2 + 10)
                            ||
                            (mBorderType == 3
                                    && borderY >= getHeight() / 2 - mBoundSize / 2 - 10
                                    && borderY <= getHeight() / 2 + mBoundSize / 2 + 10)) {
                        dragFlag = true;
                    } else {
                        dragFlag = false;
                    }
                    isBoundDown = true;
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:

                    if (dragFlag) {
                        if (mBorderType == 0) {
                            if (PageEditorActivity.canvasRotation == 0) {
                                if (param.width - DEFAULT_WIDTH < x - tmpX) {
                                    x = param.width - DEFAULT_WIDTH + tmpX;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < y - tmpY) {
                                    y = param.height - mBoundSize * 3
                                            - (int) (mContentView.getTextSize() * 1.5)
                                            - DEFAULT_HEIGHT + tmpY;
                                }

                                param.y += y - tmpY;
                                param.height -= y - tmpY;
                            } else if (PageEditorActivity.canvasRotation == 90) {
                                if (param.width - DEFAULT_WIDTH < tmpY - y) {
                                    y = -param.width + DEFAULT_WIDTH + tmpY;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < tmpX - x) {
                                    x = -param.height + mBoundSize * 3
                                            + (int) (mContentView.getTextSize() * 1.5)
                                            + DEFAULT_HEIGHT + tmpX;
                                }

                                param.y -= x - tmpX;
                                param.height += x - tmpX;
                            } else if (PageEditorActivity.canvasRotation == 180) {
                                if (param.width - DEFAULT_WIDTH < tmpX - x) {
                                    x = -param.width + DEFAULT_WIDTH + tmpX;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < tmpY - y) {
                                    y = -param.height + mBoundSize * 3
                                            + (int) (mContentView.getTextSize() * 1.5)
                                            + DEFAULT_HEIGHT + tmpY;
                                }

                                param.y -= y - tmpY;
                                param.height += y - tmpY;
                            } else if (PageEditorActivity.canvasRotation == 270) {
                                if (param.width - DEFAULT_WIDTH < y - tmpY) {
                                    y = param.width - DEFAULT_WIDTH + tmpY;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < x - tmpX) {
                                    x = param.height - mBoundSize * 3
                                            - (int) (mContentView.getTextSize() * 1.5)
                                            - DEFAULT_HEIGHT + tmpX;
                                }

                                param.y += x - tmpX;
                                param.height -= x - tmpX;
                            }
                        } else if (mBorderType == 1) {
                            if (PageEditorActivity.canvasRotation == 0) {
                                if (param.width - DEFAULT_WIDTH < tmpX - x) {
                                    x = tmpX - param.width + DEFAULT_WIDTH;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < y - tmpY) {
                                    y = param.height - mBoundSize * 3
                                            - (int) (mContentView.getTextSize() * 1.5)
                                            - DEFAULT_HEIGHT + tmpY;
                                }

                                param.width += x - tmpX;
                            } else if (PageEditorActivity.canvasRotation == 90) {
                                if (param.width - DEFAULT_WIDTH < tmpY - y) {
                                    y = -param.width + DEFAULT_WIDTH + tmpY;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < tmpX - x) {
                                    x = -param.height + mBoundSize * 3
                                            + (int) (mContentView.getTextSize() * 1.5)
                                            + DEFAULT_HEIGHT + tmpX;
                                }

                                param.width += y - tmpY;
                            } else if (PageEditorActivity.canvasRotation == 180) {
                                if (param.width - DEFAULT_WIDTH < x - tmpX) {
                                    x = tmpX + param.width - DEFAULT_WIDTH;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < tmpY - y) {
                                    y = -param.height + mBoundSize * 3
                                            + (int) (mContentView.getTextSize() * 1.5)
                                            + DEFAULT_HEIGHT + tmpY;
                                }

                                param.width -= x - tmpX;
                            } else if (PageEditorActivity.canvasRotation == 270) {
                                if (param.width - DEFAULT_WIDTH < y - tmpY) {
                                    y = param.width - DEFAULT_WIDTH + tmpY;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < x - tmpX) {
                                    x = param.height - mBoundSize * 3
                                            - (int) (mContentView.getTextSize() * 1.5)
                                            - DEFAULT_HEIGHT + tmpX;
                                }

                                param.width -= y - tmpY;
                            }
                        } else if (mBorderType == 2) {
                            if (PageEditorActivity.canvasRotation == 0) {
                                if (param.width - DEFAULT_WIDTH < tmpX - x)
                                    x = tmpX - param.width + DEFAULT_WIDTH;
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < tmpY - y) {
                                    y = tmpY - param.height + mBoundSize * 3
                                            + (int) (mContentView.getTextSize() * 1.5)
                                            + DEFAULT_HEIGHT;
                                }

                                param.height += y - tmpY;
                            } else if (PageEditorActivity.canvasRotation == 90) {
                                if (param.width - DEFAULT_WIDTH < tmpY - y)
                                    y = tmpY - param.width + DEFAULT_WIDTH;
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < x - tmpX) {
                                    x = tmpX + param.height - mBoundSize * 3
                                            - (int) (mContentView.getTextSize() * 1.5)
                                            - DEFAULT_HEIGHT;
                                }

                                param.height -= x - tmpX;
                            } else if (PageEditorActivity.canvasRotation == 180) {
                                if (param.width - DEFAULT_WIDTH < x - tmpX)
                                    x = tmpX + param.width - DEFAULT_WIDTH;
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < y - tmpY) {
                                    y = tmpY + param.height - mBoundSize * 3
                                            - (int) (mContentView.getTextSize() * 1.5)
                                            - DEFAULT_HEIGHT;
                                }

                                param.height -= y - tmpY;
                            } else if (PageEditorActivity.canvasRotation == 270) {
                                if (param.width - DEFAULT_WIDTH < y - tmpY)
                                    y = tmpY + param.width - DEFAULT_WIDTH;
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < tmpX - x) {
                                    x = tmpX - param.height + mBoundSize * 3
                                            + (int) (mContentView.getTextSize() * 1.5)
                                            + DEFAULT_HEIGHT;
                                }

                                param.height += x - tmpX;
                            }
                        } else if (mBorderType == 3) {
                            if (PageEditorActivity.canvasRotation == 0) {
                                if (param.width - DEFAULT_WIDTH < x - tmpX) {
                                    x = param.width - DEFAULT_WIDTH + tmpX;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < tmpY - y) {
                                    y = tmpY - param.height + mBoundSize * 3
                                            + (int) (mContentView.getTextSize() * 1.5)
                                            + DEFAULT_HEIGHT;
                                }

                                param.x += x - tmpX;
                                param.width -= x - tmpX;
                            } else if (PageEditorActivity.canvasRotation == 90) {
                                if (param.width - DEFAULT_WIDTH < y - tmpY) {
                                    y = param.width - DEFAULT_WIDTH + tmpY;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < x - tmpX) {
                                    x = tmpX + param.height - mBoundSize * 3
                                            - (int) (mContentView.getTextSize() * 1.5)
                                            - DEFAULT_HEIGHT;
                                }

                                param.x += y - tmpY;
                                param.width -= y - tmpY;
                            } else if (PageEditorActivity.canvasRotation == 180) {
                                if (param.width - DEFAULT_WIDTH < tmpX - x) {
                                    x = -param.width + DEFAULT_WIDTH + tmpX;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < y - tmpY) {
                                    y = tmpY + param.height - mBoundSize * 3
                                            - (int) (mContentView.getTextSize() * 1.5)
                                            - DEFAULT_HEIGHT;
                                }

                                param.x -= x - tmpX;
                                param.width += x - tmpX;
                            } else if (PageEditorActivity.canvasRotation == 270) {
                                if (param.width - DEFAULT_WIDTH < tmpY - y) {
                                    y = -param.width + DEFAULT_WIDTH + tmpY;
                                }
                                if (param.height - mBoundSize * 3
                                        - (int) (mContentView.getTextSize() * 1.5)
                                        - DEFAULT_HEIGHT < tmpX - x) {
                                    x = tmpX - param.height + mBoundSize * 3
                                            + (int) (mContentView.getTextSize() * 1.5)
                                            + DEFAULT_HEIGHT;
                                }

                                param.x -= y - tmpY;
                                param.width += y - tmpY;
                            }
                        }
                    } else {
                        if (PageEditorActivity.canvasRotation == 0) {
                            param.x += x - tmpX;
                            param.y += y - tmpY;
                        } else if (PageEditorActivity.canvasRotation == 90) {
                            param.x += y - tmpY;
                            param.y -= x - tmpX;
                        } else if (PageEditorActivity.canvasRotation == 180) {
                            param.x -= x - tmpX;
                            param.y -= y - tmpY;
                        } else if (PageEditorActivity.canvasRotation == 270) {
                            param.x -= y - tmpY;
                            param.y += x - tmpX;
                        }
                    }
                    tmpX = x;
                    tmpY = y;
                    NoteEditText.this.setLayoutParams(param);
                    invalidate();

                    break;
                case MotionEvent.ACTION_UP:
                    //mContentView.setHint("please_input_word");
                    imm.showSoftInput(mContentView, 0);
                    isBoundDown = false;
                    invalidate();

                    Rect rect = new Rect();
                    rect.left = param.x;
                    rect.top = param.y;
                    rect.right = param.x + param.width;
                    rect.bottom = param.y + param.height;
                    mUndoLayoutParamsList.add(rect);
//                    PageEditorActivity.addRecord(NoteEditText.this);
                    if (PageEditorActivity.getInstance() != null) {
                        PageEditorActivity.getInstance().addRecord(NoteEditText.this);
                    }
                    break;
            }
            return true;
        }
    }

    @Override
    public View getContentView() {
        return mContentView;
    }

    public void setContentViewText(String text) {
        mContentView.setText(text);
    }

    public String getContentViewText() {
        return mContentView.getText().toString();
    }

    public void setContentViewTextSize(float size) {
        mContentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public float getContentViewTextSize() {
        return mContentView.getTextSize();
    }

    public void setContentViewTextColor(int color) {
        mContentView.setTextColor(color);
    }

    public int getContentViewTextColor() {
        return mContentView.getCurrentTextColor();
    }

    public void setContentViewTextTypeface(Typeface typeface) {
        this.typeFace = typeface;
        mContentView.setTypeface(typeface);
        isBold = typeface.isBold();
        isItalic = typeface.isItalic();
    }

    public Typeface getContentViewTextTypeface() {
//        return mContentView.getTypeface();
        return typeFace;
    }

    public void setContentViewTextUnderLine(int flag) {
        mContentView.getPaint().setFlags(flag);
    }

    public int getContentViewTextUnderLine() {
        return mContentView.getPaint().getFlags();
    }

    public int getFontTypeIndex() {
        return fontTypeIndex;
    }

    public void setFontTypeIndex(int fontTypeIndex) {
        this.fontTypeIndex = fontTypeIndex;
    }

    public boolean getIsItalic() {
        return isItalic;
    }

    public void setIsItalic(boolean isItalic) {
        this.isItalic = isItalic;
    }

    public boolean getIsBold() {
        return isBold;
    }

    public void setIsBold(boolean isBold) {
        this.isBold = isBold;
    }
}