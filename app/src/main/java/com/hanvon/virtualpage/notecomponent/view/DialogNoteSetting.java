package com.hanvon.virtualpage.notecomponent.view;

import android.content.Context;
import android.hardware.smartpad.SmartpadManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.base.BaseDialog;
import com.hanvon.virtualpage.beans.Document;
import com.hanvon.virtualpage.beans.Manifest;
import com.hanvon.virtualpage.beans.UIConstants;
import com.hanvon.virtualpage.beans.Workspace;
import com.hanvon.virtualpage.pageeditor.view.AutoBgButton;
import com.hanvon.virtualpage.utils.TimeHelper;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/7/6
 * E_mail:  taozhi@hanwang.com.cn
 */
public class DialogNoteSetting extends BaseDialog {
    private Context mContext;
    private View parentView;
    private EditText edtNoteName;
    private GridView gvCoverSelector;
    private TextView tvCancel;
    private TextView tvOk;
    private Document mDocument;
    private CoversAdapter coversAdapter;
    private OnDialogOperationListener mListener;
//    private InputMethodManager service;

    public DialogNoteSetting(Context context) {
        super(context);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mContext = context;
//        service = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    @Override
    public View getContentLayoutView() {
        initViews();
        initData();
        return parentView;
    }


    private void initViews() {
        parentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_note_edit_layout, null);
        edtNoteName = (EditText) parentView.findViewById(R.id.et_note_name);
        gvCoverSelector = (GridView) parentView.findViewById(R.id.gv_cover_selector);
        tvCancel = (TextView) parentView.findViewById(R.id.tv_cancel);
        tvOk = (TextView) parentView.findViewById(R.id.tv_ok);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancelClick();
                }
                dismiss();
            }
        });
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkStrValidation(edtNoteName.getText().toString())) {
                    Toast.makeText(mContext, R.string.RenameWarningInfo, Toast.LENGTH_SHORT).show();
                    return;
                }
                mDocument.setBackgroundResIndex(String.valueOf(coversAdapter.getSelectPosition()));
                mDocument.setUpdatedTime(TimeHelper.getCurrentDateTime());
                mDocument.setTitle(edtNoteName.getText().toString());
                mDocument.save();
                Manifest.getInstance().save();
                if (mListener != null) {
                    mListener.onConfirmClick();
                }
                dismiss();
            }
        });
//        ViewTreeObserver vto = parentView.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//
//            @Override
//            public void onGlobalLayout() {
//                if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    if (service.isActive(edtNoteName)) {
//                        ViewGroup.LayoutParams layoutParams = gvCoverSelector.getLayoutParams();
//                        layoutParams.height = 200;
//                    } else {
//                        ViewGroup.LayoutParams layoutParams = gvCoverSelector.getLayoutParams();
//                        layoutParams.height = 400;
//                    }
//                }
//            }
//        });
    }


    private void initData() {
        // 获取当前需要修改的note对象
        mDocument = Workspace.getInstance().getCurrentDocument();
        // 将Note对象数据传递到适配器中
        coversAdapter = new CoversAdapter(mDocument, UIConstants.ARRAY_NOTE_COVER_COLOR);
        // 加载编辑窗口中的布局文件
        edtNoteName.setText(mDocument.getTitle());
        edtNoteName.selectAll();
        if (Manifest.getInstance().get(0) == mDocument) {
            edtNoteName.setSelection(0);
            edtNoteName.setEnabled(false);
        }
        gvCoverSelector.setAdapter(coversAdapter);
        // Start: by wangkun20 to set the keyboard enable and disable the pen write mode
        BaseApplication.getApplication().setPenKeyState(SmartpadManager.PEN_KEY_LED_STATE_OFF);

    }

    public interface OnDialogOperationListener {
        void onConfirmClick();
        void onCancelClick();
    }

    public void setOnDialogOperationListener(OnDialogOperationListener listener) {
        mListener = listener;
    }

    private boolean checkStrValidation(String str) {
        if (TextUtils.isEmpty(str.trim())) {
            return false;
        }
        return true;
    }

    /**
     * Note编辑框中可选Cover的数据适配器
     */
    private class CoversAdapter extends BaseAdapter {
        private int selectPosition;
        private int[] resIds;
        private Document bean;

        public CoversAdapter(Document doc, int[] resIds) {
            this.bean = doc;
            this.resIds = resIds;
//            selectPosition = coversList.indexOf(Integer.parseInt(bean.getBackgroundResIndex()));
            selectPosition = UIConstants.getAvailableCoverIndex(bean.getBackgroundResIndex());
        }

        /**
         * 设置新的选中项，一旦设置后，需要刷新显示
         *
         * @param index 新的选中项
         */
        public void setSelectPosition(int index) {
            if (index == selectPosition) {
                return;
            }
            selectPosition = index;
            notifyDataSetChanged();
        }

        public int getSelectPosition() {
            return selectPosition;
        }

        @Override
        public int getCount() {
            return resIds.length;
        }

        @Override
        public Object getItem(int position) {
            return resIds[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View dialog_editor = LayoutInflater.from(mContext).inflate(R.layout.note_editor_dialog_cover_item, parent, false);
            ImageView iv_note_cover = (ImageView) dialog_editor.findViewById(R.id.iv_note_cover);
            AutoBgButton abb_cover_selected_flag = (AutoBgButton) dialog_editor.findViewById(R.id.abb_cover_selected_flag);
            iv_note_cover.setBackgroundResource(resIds[position]);
            if (position == selectPosition) {
                abb_cover_selected_flag.setSelectedState(true);
            } else {
                abb_cover_selected_flag.setSelectedState(false);
            }
            dialog_editor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    setSelectPosition(position);
                }
            });
            return dialog_editor;
        }
    }
}
