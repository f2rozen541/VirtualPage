package com.hanvon.virtualpage.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Document;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.beans.Workspace;
import com.hanvon.virtualpage.notecomponent.widget.NoteManagerView;
import com.hanvon.virtualpage.utils.InformationParser;
import com.hanvon.virtualpage.utils.LogUtil;
import com.hanvon.virtualpage.utils.PermissionsActivity;
import com.hanvon.virtualpage.utils.PermissionsChecker;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public Context mContext;
    private NoteManagerView noteManagerView;
    public List<Page> pages;
    private RelativeLayout.LayoutParams lpContent;
    private FrameLayout viewsContainer;


    private List<Document> notesInfo;
    private Document mCurrentDoc;

    private PermissionsChecker mPermissionsChecker;
    private static final int REQUEST_CODE = 11;
    private static final String[] NECESSARY_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mPermissionsChecker = new PermissionsChecker(mContext);
        initWidgetViews();
    }

    /**
     * 初始化控件对象
     */
    private void initWidgetViews() {
        lpContent = new RelativeLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        viewsContainer = (FrameLayout) findViewById(R.id.fl_container);
        if (noteManagerView == null) {
            noteManagerView = new NoteManagerView(this);
        }
        viewsContainer.addView(noteManagerView, lpContent);
    }


    /**
     * 加载控件数据。每次加载数据都需要先检查权限
     */
    private void loadWidgetData() {
        if (mPermissionsChecker.lacksPermissions(NECESSARY_PERMISSIONS)) {
            startPermissionCheckerActivity();
        } else {
            BaseApplication.setHasPermission(true);
            InformationParser.initializedManifest(mContext, com.hanvon.virtualpage.beans.Manifest.getInstance());
            notesInfo = com.hanvon.virtualpage.beans.Manifest.getInstance().getDocuments();
            Collections.sort(notesInfo, new InformationParser.NoteListComparator());
            if (notesInfo.size() == 0) {
                InformationParser.newDefaultDocument(mContext, com.hanvon.virtualpage.beans.Manifest.getInstance());
            }
            Document document = Workspace.getInstance().getCurrentDocument();
            int indexOf = notesInfo.indexOf(document) + 1;
            if (indexOf < 1) {
                indexOf = 1;
            }
            noteManagerView.setFocusViewPosition(indexOf);
            noteManagerView.setData(notesInfo);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWidgetData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 每次界面消失前都会保存当前数据
        if (BaseApplication.hasPermission()) {
            com.hanvon.virtualpage.beans.Manifest.getInstance().save();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            BaseApplication.getApplication().AppExit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startPermissionCheckerActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, NECESSARY_PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
                    LogUtil.i("loadData", "权限不够，直接关闭");
                    BaseApplication.setHasPermission(false);
                    BaseApplication.getApplication().AppExit();
                } else {
                    BaseApplication.setHasPermission(true);
                }
                break;
            default:
                break;
        }
    }
}
