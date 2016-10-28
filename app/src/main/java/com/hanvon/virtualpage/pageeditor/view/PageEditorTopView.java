package com.hanvon.virtualpage.pageeditor.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.common.EditorState;
import com.hanvon.virtualpage.utils.CommonUtils;
/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/2/23
 * E_mail:  taozhi@hanwang.com.cn
 */
public class PageEditorTopView extends LinearLayout implements View.OnClickListener {

    private LinearLayout parentView;
    private ImageButton ib_back;
    private ImageButton ib_new_note;
    private AutoBgButton ib_paint;
    private AutoBgButton ib_text;
    private ImageButton ib_photo;
    private ImageButton ib_rotate;
    private AutoBgButton ib_frame;
    private ImageButton ib_last;
    private ImageButton ib_next;
    private ImageButton ib_setting;
    private ImageButton ib_share;

    private AutoBgButton currentFocusView;

    private int mFocusView = 0;
    public static final int FUNCTION_PEN = 0;
    public static final int FUNCTION_TEXT = 1;
    public static final int FUNCTION_PHOTO = 2;
    public static final int FUNCTION_ROTATE = 3;
    public static final int FUNCTION_FRAME = 4;
    private OnButtonClickListener mListener;

    public PageEditorTopView(Context context) {
        this(context, null);
    }

    public PageEditorTopView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageEditorTopView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parentView = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.page_editor_top_layout, this);
        initViews();
        initDates();
//        int scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private void initDates() {
//        currentFocusView = ib_paint;
        setCurrentState(EditorState.getInstance().getTopViewState());
//        currentFocusView.setSelectedState(true);
    }

    private void initViews() {
        ib_back = (ImageButton) parentView.findViewById(R.id.ib_back);
        ib_new_note = (ImageButton) parentView.findViewById(R.id.ib_note_new_note);
        ib_paint = (AutoBgButton) parentView.findViewById(R.id.ib_paint);
        ib_text = (AutoBgButton) parentView.findViewById(R.id.ib_text);
        ib_photo = (ImageButton) parentView.findViewById(R.id.ib_photo);
        ib_rotate = (ImageButton) parentView.findViewById(R.id.ib_rotate);
        ib_frame = (AutoBgButton) parentView.findViewById(R.id.ib_frame);
        ib_last = (ImageButton) parentView.findViewById(R.id.ib_last);
        ib_next = (ImageButton) parentView.findViewById(R.id.ib_next);
        ib_share = (ImageButton) parentView.findViewById(R.id.ib_share);
        ib_setting = (ImageButton) parentView.findViewById(R.id.ib_note_setting);
        if(CommonUtils.isRtl())ib_back.setBackgroundResource(R.drawable.back_arab);


        ib_back.setOnClickListener(this);
        ib_new_note.setOnClickListener(this);
        ib_paint.setOnClickListener(this);
        ib_text.setOnClickListener(this);
        ib_photo.setOnClickListener(this);
        ib_rotate.setOnClickListener(this);
        ib_frame.setOnClickListener(this);
        ib_last.setOnClickListener(this);
        ib_next.setOnClickListener(this);
        ib_share.setOnClickListener(this);
        ib_setting.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ib_back:
                if(mListener != null){
                    mListener.onBackClick();
                }
                break;
            case R.id.ib_note_new_note:
                if(mListener != null){
                    mListener.onNewNoteClick();
                }
                break;
            case R.id.ib_paint:
                updateFocusView(v);
                EditorState.getInstance().setTopViewState(EditorState.FUNCTION_PAINT);
                if(mListener != null){
                    mListener.onPaintClick();
                }
                break;
            case R.id.ib_text:
                updateFocusView(v);
                EditorState.getInstance().setTopViewState(EditorState.FUNCTION_TEXT);
                if(mListener != null){
                    mListener.onTextClick();
                }
                break;
            case R.id.ib_photo:
                if(mListener != null){
                    mListener.onPhotoClick();
                }
                break;
            case R.id.ib_rotate:
                if(mListener != null){
                    mListener.onRotateClick();
                }
                break;
            case R.id.ib_frame:
                updateFocusView(v);
                EditorState.getInstance().setTopViewState(EditorState.FUNCTION_FRAME);
                if(mListener != null){
                    mListener.onFrameClick();
                }
                break;
            case R.id.ib_last:
                if(mListener != null){
                    mListener.onLastClick();
                }
                break;
            case R.id.ib_next:
                if(mListener != null){
                    mListener.onNextClick();
                }
                break;
            case R.id.ib_share:
                if(mListener != null){
                    mListener.onShareClick();
                }
                break;
            case R.id.ib_note_setting:
                if(mListener != null){
                    mListener.onSettingClick();
                }
                break;
        }
    }

    public void setCurrentState(int focusPosition) {
        switch (focusPosition) {
            case FUNCTION_PEN:
                updateFocusView(ib_paint);
                break;
            case FUNCTION_TEXT:
                updateFocusView(ib_text);
                break;
            case FUNCTION_PHOTO:
                updateFocusView(ib_photo);
                break;
            case FUNCTION_ROTATE:
                updateFocusView(ib_rotate);
                break;
            case FUNCTION_FRAME:
                updateFocusView(ib_frame);
                break;
            default:
                break;

        }
    }

    private void updateFocusView(View v) {
        if(currentFocusView == v){
            return;
        }
        if (currentFocusView == null) {
            currentFocusView = (AutoBgButton) v;
            currentFocusView.setSelectedState(true);
        } else {
            currentFocusView.setSelectedState(false);
            currentFocusView = (AutoBgButton) v;
            currentFocusView.setSelectedState(true);
        }
    }


    /**
     * 按钮点击在UI改变上的回调接口
     */
    public interface OnButtonClickListener{
        void onBackClick();
        void onNewNoteClick();
        void onPaintClick();
        void onTextClick();
        void onPhotoClick();
        void onRotateClick();
        void onFrameClick();
        void onLastClick();
        void onNextClick();

        void onShareClick();

        void onSettingClick();
    }

    public void setOnButtonClickListener(OnButtonClickListener listener){
        this.mListener = listener;
    }
}
