package com.hanvon.virtualpage.pageeditor.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.common.EditorState;
import com.hanvon.virtualpage.pageeditor.activity.PageEditorActivity;
import com.hanvon.virtualpage.pageeditor.tools.stroke.NoteParams;
import com.larswerkman.holocolorpicker.HuBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.util.List;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/2/23
 * E_mail:  taozhi@hanwang.com.cn
 */
public class ColorOptionView extends LinearLayout implements View.OnClickListener, HuBar.OnColorChangedListener {

    private AutoBgButton currentFocusView;
    private TextColorListener mTextColorListener;
    private Context context;

    private PopupWindow popupWindow;
    private SaturationBar mSaturationBar;
    private AutoBgButton currentFocusSaturationView;
    private List<Integer> colorList;
    private String TAG = "ColorOptionView";
    private RecyclerView rvColorList;
    private ColorAdapter colorAdapter;
    private int mFocusPos;
    private HuBar mHuBar;
    private ValueBar mValueBar;
    private boolean isPPWShowing;
    private AutoBgButton abLight1;
    private AutoBgButton abLight2;
    private AutoBgButton abLight3;
    private AutoBgButton abLight4;
    private AutoBgButton abLight5;
    private AutoBgButton abLight6;

    public ColorOptionView(Context context) {
        this(context, null);
    }

    public ColorOptionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorOptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initViews();
        isPPWShowing = false;
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void initViews() {

        colorList = EditorState.getInstance().getColorList();
        mFocusPos = EditorState.getInstance().getColorIndex();

        rvColorList = new RecyclerView(context);
        rvColorList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        rvColorList.setOverScrollMode(OVER_SCROLL_NEVER);
        colorAdapter = new ColorAdapter();
        rvColorList.setAdapter(colorAdapter);
        rvColorList.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View child = rvColorList.getChildAt(0);
                    int position = rvColorList.getChildAdapterPosition(child);
                    int state;
                    if (position > 1) { // 滑动到第二栏
                        rvColorList.smoothScrollToPosition(colorAdapter.getItemCount() - 1);
                        state = 1;
                    } else { // 滑动到第一栏
                        rvColorList.smoothScrollToPosition(0);
                        state = 0;
                    }
                    if (mTextColorListener != null) {
                        mTextColorListener.onColorSectionChanged(state);
                    }
                }
            }
        });

        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        this.addView(rvColorList, layoutParams);
    }

    public void refreshViews() {
        colorList = EditorState.getInstance().getColorList();
        mFocusPos = EditorState.getInstance().getColorIndex();
        colorAdapter.notifyDataSetChanged();
    }

    public class ColorAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            AutoBgButton autoBgButton = (AutoBgButton) LayoutInflater.from(context).inflate(R.layout.color_picker_item, parent, false);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) autoBgButton.getLayoutParams();
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layoutParams.leftMargin = 18;
                layoutParams.rightMargin = 18;
            } else {
                layoutParams.leftMargin = 12;
                layoutParams.rightMargin = 12;
            }
            autoBgButton.setLayoutParams(layoutParams);
            return new ColorOptHolder(autoBgButton);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ColorOptHolder) {
                final AutoBgButton optItem = ((ColorOptHolder) holder).colorOptItem;
                if (mFocusPos == position) {
                    optItem.setSelectedState(true);
                } else {
                    optItem.setSelectedState(false);
                }
                optItem.setBgColor(colorList.get(position));
                optItem.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mFocusPos == position) {
                            showColorPickerPopu(v);
                        } else {
                            isPPWShowing = false;
                        }
                        mFocusPos = position;
                        NoteParams.getCurrentPenNoteParams().setCurrentStrokeColorType(optItem.getBgColor());
                        EditorState.getInstance().setColorIndex(position);
                        if (mTextColorListener != null) {
                            mTextColorListener.TextColorChanged(optItem.getBgColor());
                        }
                        notifyDataSetChanged();
                    }
                });
                optItem.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        isPPWShowing = false;
                        mFocusPos = position;
                        NoteParams.getCurrentPenNoteParams().setCurrentStrokeColorType(optItem.getBgColor());
                        EditorState.getInstance().setColorIndex(position);
                        if (mTextColorListener != null) {
                            mTextColorListener.TextColorChanged(optItem.getBgColor());
                        }
                        showColorPickerPopu(v);
                        notifyDataSetChanged();
                        return true;
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return colorList.size();
        }
    }

    public class ColorOptHolder extends RecyclerView.ViewHolder {

        AutoBgButton colorOptItem;

        public ColorOptHolder(View itemView) {
            super(itemView);
            colorOptItem = (AutoBgButton) itemView;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_light1:
//                mSaturationBar.setSaturationValue(1f);
                updateFocusSaturationView(v);
                break;
            case R.id.iv_light2:
//                mSaturationBar.setSaturationValue(0.8f);
                updateFocusSaturationView(v);
                break;
            case R.id.iv_light3:
//                mSaturationBar.setSaturationValue(0.5f);
                updateFocusSaturationView(v);
                break;
            case R.id.iv_light4:
//                mSaturationBar.setSaturationValue(0.4f);
                updateFocusSaturationView(v);
                break;
            case R.id.iv_light5:
//                mSaturationBar.setSaturationValue(0.3f);
                updateFocusSaturationView(v);
                break;
            case R.id.iv_light6:
//                mSaturationBar.setSaturationValue(0.2f);
                updateFocusSaturationView(v);
                break;
        }
    }

    private void showColorPickerPopu(View view) {

        if (isPPWShowing == true) {
            isPPWShowing = false;
            return;
        } else {
            View contentView = LayoutInflater.from(context).inflate(R.layout.colorpicker_popwindow, null);

            abLight1 = (AutoBgButton) contentView.findViewById(R.id.iv_light1);
            abLight2 = (AutoBgButton) contentView.findViewById(R.id.iv_light2);
            abLight3 = (AutoBgButton) contentView.findViewById(R.id.iv_light3);
            abLight4 = (AutoBgButton) contentView.findViewById(R.id.iv_light4);
            abLight5 = (AutoBgButton) contentView.findViewById(R.id.iv_light5);
            abLight6 = (AutoBgButton) contentView.findViewById(R.id.iv_light6);

            abLight1.setOnClickListener(this);
            abLight2.setOnClickListener(this);
            abLight3.setOnClickListener(this);
            abLight4.setOnClickListener(this);
            abLight5.setOnClickListener(this);
            abLight6.setOnClickListener(this);

//            currentFocusSaturationView = abLight1;
//            currentFocusSaturationView.setSelectedState(true);
            mHuBar = (HuBar) contentView.findViewById(R.id.hubar);
            mSaturationBar = (SaturationBar) contentView.findViewById(R.id.saturationbar);
            mValueBar = (ValueBar) contentView.findViewById(R.id.valuebar);

            final int currentColor = EditorState.getInstance().getColorList().get(EditorState.getInstance().getColorIndex());
            mHuBar.addSaturationBar(mSaturationBar);
            mHuBar.addValueBar(mValueBar);
            mHuBar.setOnColorChangedListener(ColorOptionView.this);
            mHuBar.setColor(currentColor);

//            currentFocusSaturationView = abLight1;
//            currentFocusSaturationView.setSelectedState(true);

            setGradientColorBar(Color.BLACK);

            popupWindow = new PopupWindow(contentView, 474, 366, true);
            popupWindow.setFocusable(false);
            popupWindow.setTouchable(true);
            popupWindow.setOutsideTouchable(true);

            // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
            // 我觉得这里是API的一个bug
            popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.color_menu));

            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int[] parentLocation = new int[2];
            rvColorList.getLocationOnScreen(parentLocation);
            popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0] - 24, parentLocation[1] - popupWindow.getHeight() - 24);
            BaseApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mHuBar != null) {
                        mHuBar.setInitColor(currentColor);
                    }
                }
            }, 10);

//            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//                @Override
//                public void onDismiss() {
//                    mHuBar = null;
//                    mSaturationBar = null;
//                    mValueBar = null;
//                }
//            });
            isPPWShowing = true;
            if (PageEditorActivity.getInstance() != null) {
                PageEditorActivity.getInstance().setPageListState(false);
            }

        }
    }

    /**
     * 根据当前颜色刷新渐变色栏的颜色值
     * @param currentColor 当前的颜色值
     */
    private void setGradientColorBar(int currentColor) {
        float[] hsv = new float[3];
        float[] percent = new float[] {
                1.0f, 0.8f, 0.6f, 0.4f, 0.2f, 0.0f
        };
//        int currentColor = EditorState.getInstance().getColorList().get(EditorState.getInstance().getColorIndex());
        Color.colorToHSV(currentColor, hsv);
//        float saturation = hsv[1] + 0.00001f;
//        float value = hsv[2] + 0.00001f;
        float value = 1f;

//        if (isPPWShowing == true) {
            if (abLight1 != null) {
//                hsv[1] = saturation * percent[0];
                hsv[2] = value * percent[5];
                abLight1.setBgColor(Color.HSVToColor(hsv));
            }
            if (abLight2 != null) {
//                hsv[1] = saturation * percent[1];
                hsv[2] = value * percent[4];
                abLight2.setBgColor(Color.HSVToColor(hsv));
            }
            if (abLight3 != null) {
//                hsv[1] = saturation * percent[2];
                hsv[2] = value * percent[3];
                abLight3.setBgColor(Color.HSVToColor(hsv));
            }
            if (abLight4 != null) {
//                hsv[1] = saturation * percent[3];
                hsv[2] = value * percent[2];
                abLight4.setBgColor(Color.HSVToColor(hsv));
            }
            if (abLight5 != null) {
//                hsv[1] = saturation * percent[4];
                hsv[2] = value * percent[1];
                abLight5.setBgColor(Color.HSVToColor(hsv));
            }
            if (abLight6 != null) {
//                hsv[1] = saturation * percent[5];
                hsv[2] = value * percent[0];
                abLight6.setBgColor(Color.HSVToColor(hsv));
            }
//        }
    }


    private void updateFocusSaturationView(View v) {
        if (currentFocusSaturationView == v) {
            return;
        }
        if (currentFocusSaturationView == null) {
            currentFocusSaturationView = (AutoBgButton) v;
        }
        currentFocusSaturationView.setSelectedState(false);
        currentFocusSaturationView = (AutoBgButton) v;
        currentFocusSaturationView.setSelectedState(true);
        final int currentColor = currentFocusSaturationView.getBgColor();

        colorList.set(mFocusPos, currentColor);
        colorAdapter.notifyDataSetChanged();
        EditorState.getInstance().setColorAtPosition(mFocusPos, currentColor);
        EditorState.getInstance().setColorIndex(mFocusPos);
        NoteParams.getCurrentPenNoteParams().setCurrentStrokeColorType(currentColor);

        BaseApplication.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mHuBar != null) {
                    mHuBar.setInitColor(currentColor);
                }
            }
        }, 10);

        if (mTextColorListener != null) {
            mTextColorListener.TextColorChanged(currentColor);
        }
    }


    public void dismissColorPickerPopu() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }


    public interface TextColorListener {
        void TextColorChanged(int value);

        void onColorSectionChanged(int state);
    }

    public void setTextColorListener(TextColorListener textColorListener) {
        this.mTextColorListener = textColorListener;
    }

    @Override
    public void onColorChanged(int color) {
        Log.d(TAG, "onColorChanged: " + color);
//        setGradientColorBar(color);
        colorList.set(mFocusPos, color);
        colorAdapter.notifyDataSetChanged();
        EditorState.getInstance().setColorAtPosition(mFocusPos, color);
        EditorState.getInstance().setColorIndex(mFocusPos);
        NoteParams.getCurrentPenNoteParams().setCurrentStrokeColorType(color);
        if (mTextColorListener != null) {
            mTextColorListener.TextColorChanged(color);
        }
    }
}
