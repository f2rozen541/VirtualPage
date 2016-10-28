package com.hanvon.virtualpage.pageeditor.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.common.EditorState;
import com.hanvon.virtualpage.pageeditor.activity.PageEditorActivity;
import com.hanvon.virtualpage.pageeditor.tools.stroke.CanvasLayout;
import com.hanvon.virtualpage.pageeditor.tools.text.NoteEditText;

/**
 * -------------------------------
 * Description:
 * 页面编辑底部显示控件
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/2/23
 * E_mail:  taozhi@hanwang.com.cn
 */
public class PageEditorBottomView extends LinearLayout {
    public static final int FUNCTION_PAINT = 0;
    public static final int FUNCTION_TEXT = 1;
    private LinearLayout parentView;
    private ImageView ib_sidebar;
    private FrameLayout fl_function_area;
    private FrameLayout fl_color_area;

    private PaintOptionView mPaintView;
    private TextOptionView mTextOptionView;
    private ColorOptionView mColorOptionView;
    private Context mContext;
    public CanvasLayout mCanvasLayout;
    private boolean showList = false;
    private OnShowClickListener mListener;
    private NumberSlideBar numberSlideBar;
    private AutoBgButton abbColorIndicator1;
    private AutoBgButton abbColorIndicator2;

    public void setCanvasLayout(CanvasLayout mCanvasLayout) {
        this.mCanvasLayout = mCanvasLayout;
    }

    public PageEditorBottomView(Context context) {
        this(context, null);
    }

    public PageEditorBottomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageEditorBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        parentView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.page_editor_bottom_layout, this);
        initViews();
    }

    private void initViews() {
        ib_sidebar = (ImageView) parentView.findViewById(R.id.ib_sidebar);
        fl_function_area = (FrameLayout) parentView.findViewById(R.id.fl_function_area);
        fl_color_area = (FrameLayout) parentView.findViewById(R.id.fl_color_area);
        abbColorIndicator1 = (AutoBgButton) parentView.findViewById(R.id.abb_color_indicator_1);
        abbColorIndicator2 = (AutoBgButton) parentView.findViewById(R.id.abb_color_indicator_2);

        ib_sidebar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setPageListState(!showList);
                if (mListener != null) {
                    mListener.onShowClick(showList);
                }
            }
        });

        mPaintView = new PaintOptionView(mContext);
        mTextOptionView = new TextOptionView(mContext);

        mTextOptionView.setTextStyleListener(new TextOptionView.TextStyleListener() {
            @Override
            public void TextStyleChanged(Typeface typeface, boolean isUnderLine) {
                View view = mCanvasLayout.findFocus();

                if (view instanceof NoteEditText.NoteEditTextContent) {
                    NoteEditText.NoteEditTextContent content = (NoteEditText.NoteEditTextContent) view;
                    NoteEditText noteEditText = content.getNoteEditText();
                    /* 这里下划线属性的修改只有在Typeface变化时才会生效，所以这里无论如何需要设置一个与当前不同的Typeface，
                    然后重新设定Typeface属性，目前不确定这种现象的解决方法，暂时采取这种方法来实现效果 by tz 2016年6月20日 19:54:52*/
                    Typeface tf = noteEditText.getContentViewTextTypeface();
                    if (Typeface.DEFAULT.equals(tf)) {
                        noteEditText.setContentViewTextTypeface(Typeface.SERIF);
                    } else {
                        noteEditText.setContentViewTextTypeface(Typeface.DEFAULT);
                    }
                    noteEditText.textUnderLineChanged(isUnderLine);
                    noteEditText.setContentViewTextTypeface(typeface);
                    noteEditText.setFontTypeIndex(EditorState.getInstance().getTextFontIndex());
                }
                EditorState.getInstance().setSavingFlag(true);
            }

//            @Override
//            public void TextTypefaceChanged(int typeFace) {
//                View view = mCanvasLayout.findFocus();
//
//                if (view instanceof NoteEditText.NoteEditTextContent) {
//                    NoteEditText.NoteEditTextContent content = (NoteEditText.NoteEditTextContent) view;
//                    NoteEditText noteEditText = content.getNoteEditText();
//                    noteEditText.textTypefaceChanged(typeFace);
//                }
//            }

//            @Override
//            public void TextFontChanged(Typeface typeface) {
//                View view = mCanvasLayout.findFocus();
//
//                if (view instanceof NoteEditText.NoteEditTextContent) {
//                    NoteEditText.NoteEditTextContent content = (NoteEditText.NoteEditTextContent) view;
//                    NoteEditText noteEditText = content.getNoteEditText();
//                    noteEditText.setContentViewTextTypeface(typeface);
//                }
//            }
//
//            @Override
//            public void TextUnderLineChanged(boolean flag) {
//                View view = mCanvasLayout.findFocus();
//
//                if (view instanceof NoteEditText.NoteEditTextContent) {
//                    NoteEditText.NoteEditTextContent content = (NoteEditText.NoteEditTextContent) view;
//                    NoteEditText noteEditText = content.getNoteEditText();
//                    noteEditText.textUnderLineChanged(flag);
//                }
//            }
        });

        fl_function_area.addView(mPaintView);
//        mPaintView.setVisibility(INVISIBLE);
        fl_function_area.addView(mTextOptionView);
//        mTextOptionView.setVisibility(INVISIBLE);

        setFunctionAreaView(EditorState.getInstance().getBottomViewState()); // 默认显示画笔选择区

        numberSlideBar = (NumberSlideBar) findViewById(R.id.slide_bar);
        numberSlideBar.setTextSizeBoundary(20, 70);
//        LogUtil.d("TextSize", "TextSize初始化值为========>" + EditorState.getInstance().getTextSize());
        numberSlideBar.setProgress(EditorState.getInstance().getTextSize());
        numberSlideBar.setProgressChangeListener(new NumberSlideBar.OnProgressChangeListener() {
            @Override
            public void ProgressChanged(int value) {
                View view = mCanvasLayout.findFocus();
//                LogUtil.e("TextSize", "TextSize设定为========>" + value);
                EditorState.getInstance().setTextSize(value);

                if (view instanceof NoteEditText.NoteEditTextContent) {
                    NoteEditText.NoteEditTextContent content = (NoteEditText.NoteEditTextContent) view;
                    NoteEditText noteEditText = content.getNoteEditText();
                    noteEditText.textSizeChanged(value);
                }
                EditorState.getInstance().setSavingFlag(true);
            }
        });

        mColorOptionView = new ColorOptionView(mContext);
        mColorOptionView.setTextColorListener(new ColorOptionView.TextColorListener() {
            @Override
            public void TextColorChanged(int value) {
                View view = mCanvasLayout.findFocus();

                if (view instanceof NoteEditText.NoteEditTextContent) {
                    NoteEditText.NoteEditTextContent content = (NoteEditText.NoteEditTextContent) view;
                    NoteEditText noteEditText = content.getNoteEditText();
                    noteEditText.textColorChanged(value);
                }
                EditorState.getInstance().setSavingFlag(true);
            }

            @Override
            public void onColorSectionChanged(int state) {
                abbColorIndicator1.setSelectedState(state == 0);
                abbColorIndicator2.setSelectedState(state == 1);
            }

        });
        FrameLayout.LayoutParams lpColorView = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fl_color_area.addView(mColorOptionView, lpColorView);
    }


    public void refreshTextOptionViewState() {
        mTextOptionView.refreshViews();
        numberSlideBar.setProgress(EditorState.getInstance().getTextSize());
    }


    public boolean getPageListState() {
        return showList;
    }

    public void setPageListState(boolean isShown) {
        showList = isShown;
        if (showList) {
            ib_sidebar.setBackgroundResource(R.drawable.btn_list_hide_selector);
        } else {
            ib_sidebar.setBackgroundResource(R.drawable.btn_list_show_selector);
        }
        EditorState.getInstance().setShownListState(showList);
    }

    public interface OnShowClickListener {
        void onShowClick(boolean isShow);
    }

    public void setOnShowClickListener(OnShowClickListener listener) {
        this.mListener = listener;
    }

    public PaintOptionView getPaintView() {
        return mPaintView;
    }

    /**
     * 设置当前功能去显示的视图
     *
     * @param viewType
     */
    public void setFunctionAreaView(int viewType) {
        switch (viewType) {
            case EditorState.PANEL_PAINT:
                mPaintView.setVisibility(VISIBLE);
                mTextOptionView.setVisibility(INVISIBLE);
                break;
            case EditorState.PANEL_TEXT:
                mPaintView.setVisibility(INVISIBLE);
                mTextOptionView.setVisibility(VISIBLE);
                break;
            default:
                break;
        }

    }

    public void dismissPopWin() {
        mColorOptionView.dismissColorPickerPopu();
        if (PageEditorActivity.sActivity != null) {
            BaseApplication.setSystemUiVisibility(PageEditorActivity.sActivity, true);
        }
    }
}
