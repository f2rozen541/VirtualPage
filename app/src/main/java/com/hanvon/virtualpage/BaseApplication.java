package com.hanvon.virtualpage;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.smartpad.SmartpadManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hanvon.virtualpage.beans.UIConstants;
import com.hanvon.virtualpage.common.HelpActivity;
import com.hanvon.virtualpage.utils.LogUtil;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Stack;
import java.util.StringTokenizer;

public class BaseApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "BaseApplication";
    public static final String KILL_SELF = "COM.HANVON.KILLAPP";
    private static BaseApplication mInstance;
    /** 主线程ID */
    private static int mMainThreadId = -1;
    /** 主线程ID */
    private static Thread mMainThread;
    /** 主线程Handler */
    private static Handler mMainThreadHandler;
    /** 主线程Looper */
    private static Looper mMainLooper;

    private static Context mContext;
    private static SharedPreferences sp;
    private static boolean isFirstLoad;
    private static boolean hasPermission;

    private static Stack<Activity> mActivityStack;

    /** 用来管理硬键盘的实例对象，这里修改了V23包中的Context.class文件，所以需要使用特定的支持包*/
    private SmartpadManager mSmartpadManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mMainThreadId = android.os.Process.myTid();
        mMainThread = Thread.currentThread();
        mMainThreadHandler = new Handler();
        mMainLooper = getMainLooper();
        mInstance = this;
        Logger.init("taozhi");
        getSharePreferenceData();
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMem = maxMemory / (1024 * 1024);
        Logger.i("BaseApplication的onCreate入口cacheMem" + cacheMem);
        LogUtil.e("============================================================");
        LogUtil.e("===============BaseApplication的onCreate入口=================");
        LogUtil.e("============================================================");
        this.registerActivityLifecycleCallbacks(this);
//        Glide.with(this).onTrimMemory(TRIM_MEMORY_RUNNING_LOW);
    }

    /**
     * 设置硬键盘的显示状态
     * @param state true为显示，false为隐藏
     */
    public void setPenKeyState(int state) {
        Log.i("***", "state="+state);
        if (mSmartpadManager == null) {
            mSmartpadManager = (SmartpadManager)mContext.getSystemService(Context.SMARTPAD_SERVICE);
        }
        if (mSmartpadManager == null) {
            return;
        } else {
            mSmartpadManager.setPenKeyState(state);
        }
    }

    /**
     * 提供全局调用显示帮助界面
     */
    public static void showHelpActivity() {
        Intent intent = new Intent(BaseApplication.getApplication(), HelpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    public static SharedPreferences getAppSharePreferences() {
        return sp;
    }

    public static void getSharePreferenceData() {
        if (sp == null) {
            sp = mContext.getSharedPreferences(UIConstants.SP_FILE_NAME, MODE_PRIVATE);
        }
        isFirstLoad = sp.getBoolean(UIConstants.SP_IS_FIRST_LOAD, true);
        hasPermission = sp.getBoolean(UIConstants.HAS_GRANTED_PERMISSION, false);
    }

    /**
     * 获取当前权限状态
     * @return true当前权限已获取，false当前权限不够
     */
    public static boolean hasPermission() {
//		return sp.getBoolean(UIConstants.HAS_GRANTED_PERMISSION, false);
        return hasPermission;
    }

    /**
     * 设置当前的权限获取状态
     * @param granted
     */
    public static void setHasPermission(boolean granted) {
        hasPermission = granted;
        sp.edit().putBoolean(UIConstants.HAS_GRANTED_PERMISSION, granted).commit();
//		if (granted == false) {
//			killProcess(mContext);
//		}
    }

    /**
     * 是否为第一次启动程序
     * @return
     */
    public static boolean isFirstLoad() {
//		return sp.getBoolean(UIConstants.SP_IS_FIRST_LOAD, true);
        return isFirstLoad;
    }

    /**
     * 设置是否为第一次启动
     * @param isFirst
     */
    public static void setFirstLoad(boolean isFirst) {
        isFirstLoad = isFirst;
        sp.edit().putBoolean(UIConstants.SP_IS_FIRST_LOAD, isFirst).commit();
    }

    public static Context getContext() {
        return mContext;
    }

    /** 获取BaseApplication对象实例 */
    public static BaseApplication getApplication() {
        return mInstance;
    }

    /** 获取WindowManager实例 */
    public static WindowManager getWindowManager() {
        return (WindowManager)getApplication().getSystemService(WINDOW_SERVICE);
    }
    /** 获取主线程ID */
    public static int getMainThreadId() {
        return mMainThreadId;
    }

    /** 获取主线程 */
    public static Thread getMainThread() {
        return mMainThread;
    }

    /** 获取主线程的handler */
    public static Handler getMainThreadHandler() {
        return mMainThreadHandler;
    }

    /** 获取主线程的looper */
    public static Looper getMainThreadLooper() {
        return mMainLooper;
    }

    /**
     * 杀死进程
     * @param mAct
     */
    private static void killProcess(Context mAct) {

        String packageName = mAct.getPackageName();
        String processId = "";
        try {
            Runtime r = Runtime.getRuntime();
            Process p = r.exec("ps");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String inline;
            while ((inline = br.readLine()) != null) {
                if (inline.contains(packageName)) {
                    break;
                }
            }
            br.close();
            StringTokenizer processInfoTokenizer = new StringTokenizer(inline);
            int count = 0;
            while (processInfoTokenizer.hasMoreTokens()) {
                count++;
                processId = processInfoTokenizer.nextToken();
                if (count == 2) {
                    break;
                }
            }
            Log.e(TAG, "kill process : " + processId);
            r.exec("kill -15 " + processId);
        } catch (IOException ex) {
            Log.e(TAG, "" + ex.getStackTrace());
        }
    }

    /***
     * when this App start, the PortableInk should be paused!
     */
    private void killPortableInk() {
        LogUtil.e("killPortableInk");
        Intent intent = new Intent();
        ComponentName component = new ComponentName("com.hanvon.portableink", "com.hanvon.portableink.FxService");
        intent.setComponent(component);
        intent.setAction("android.intent.action.REQUEST_PAUSE");
        intent.putExtra("ShowOrHide", 2);
        mContext.startService(intent);
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated() returned: " + activity.toString());
        addToActivityStack(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(TAG, "onActivityStarted() returned: " + activity.toString());
        LogUtil.e("收到消息：activity===>" + activity.getIntent().getAction() + "===>" + activity.toString());
        if (KILL_SELF.equals(activity.getIntent().getAction())) {
//            AppExit();
            killMyself();
        } else {
            killPortableInk();
        }
    }


    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(TAG, "onActivityResumed() returned: " + activity.toString());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(TAG, "onActivityPaused() returned: " + activity.toString());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(TAG, "onActivityStopped() returned: " + activity.toString());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.d(TAG, "onActivitySaveInstanceState() returned: " + activity.toString());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(TAG, "onActivityDestroyed() returned: " + activity.toString());
        removeActivityFromStack(activity);
    }

    /**
     * 结束当前进程
     */
    public static void killMyself() {
        /*
        Intent MyIntent = new Intent(Intent.ACTION_MAIN);
        MyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyIntent.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(MyIntent);
        */
        Activity top = getTopActivity();
        if (top != null) {
            top.moveTaskToBack(true);
        }

//        android.os.Process.killProcess(mMainThreadId);
//		android.os.Process.killProcess(android.os.Process.myPid());
    }

    /** 添加Activity到自定义栈中 */
    public void addToActivityStack(Activity activity) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(activity);
        LogUtil.v("clearCanvas", "mActivityStack.size()==>" + mActivityStack.size() + "===>addToActivityStack() called with: " + "activity = [" + activity + "]");
    }

    /** 从自定义栈中移除Activity*/
    public void removeActivityFromStack(Activity activity) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.remove(activity);
//		activity = null;
        LogUtil.i("clearCanvas", "mActivityStack.size()==>" + mActivityStack.size() + "===>removeActivityFromStack() called with: " + "activity = [" + activity + "]");
    }

    /** 获取栈顶Activity */
    public static Activity getTopActivity() {
        if (mActivityStack != null) {
            return mActivityStack.peek();
        } else {
            return null;
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = mActivityStack.size(); i < size; i++) {
            if (null != mActivityStack.get(i)) {
                mActivityStack.get(i).finish();
            }
        }
        mActivityStack.clear();
    }

    /**
     * 退出应用程序
     */
    public void AppExit() {
        try {
            finishAllActivity();
            android.os.Process.killProcess(mMainThreadId);
        } catch (Exception e) {
            LogUtil.e("退出异常：" + e.toString());
        }
    }
    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : mActivityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }
    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            mActivityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    /**
     * 关闭Android导航栏，实现全屏
     * @param activity 需要全屏显示的Activity
     * @param enterFullscreen 是否设置为全屏
     */
    public static void setSystemUiVisibility(Activity activity, boolean enterFullscreen) {
        if (activity == null) {
            return;
        }
        View decor = activity.getWindow().getDecorView();
        if (enterFullscreen) {
            activity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        int systemUiVisibility = decor.getSystemUiVisibility();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (enterFullscreen) {
            systemUiVisibility |= flags;
        } else {
            systemUiVisibility &= ~flags;
        }
        decor.setSystemUiVisibility(systemUiVisibility);
    }

    /**
     * 关闭Android导航栏，实现全屏。此方法可以将Activity或者弹出的Dialog设置为全屏，实现风格统一
     * @param object 需要设置为全屏的对象，只能为Dialog或者Activity子类对象
     * @param enterFullscreen 是否设置为全屏
     */
    public static void setSystemUiVisibility1(Object object, boolean enterFullscreen) {
        if (object == null) {
            return;
        }
        Window window = null;
        if (object instanceof Dialog) {
            window = ((Dialog) object).getWindow();
        } else if (object instanceof Activity) {
            window = ((Activity) object).getWindow();
        }
//        else if (object instanceof PopupWindow) {
//            PopupWindow ppw = (PopupWindow) object;
//        }

        if (enterFullscreen) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        View decor = window.getDecorView();
        int systemUiVisibility = decor.getSystemUiVisibility();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (enterFullscreen) {
            systemUiVisibility |= flags;
        } else {
            systemUiVisibility &= ~flags;
        }
        decor.setSystemUiVisibility(systemUiVisibility);
    }

    /**
     * 判断当前Activity是否为全屏
     * @param activity 需要判断的Activity
     * @return
     */
    public static boolean isFullScreenActivity(Activity activity) {
        View decor = activity.getWindow().getDecorView();
        int systemUiVisibility = decor.getSystemUiVisibility();
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        return (systemUiVisibility & flags) == flags;
    }

    /**
     * 将dialog以沉浸式方式show出来，没有StatusBar显示与隐藏的过渡
     * @param dialog 将被显示出来的Dialog
     * @param activity Dialog将根据此Activity的SystemUiVisibility属性进行显示
     */
    public static void showImmersiveDialog(Dialog dialog, Activity activity) {
        if (dialog == null || activity == null) {
            return;
        }

        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();
        dialog.getWindow().getDecorView().setSystemUiVisibility(activity.getWindow().getDecorView().getSystemUiVisibility());
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
    }


    /**
     * 让Gallery上能马上看到该图片
     */
    public static void scanPhoto(String imgFileName) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(imgFileName);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }

    /**
     * 判断当前语言是否为指定语言类型
     * @param context
     * @param locateStr 指定语言类型代码，如：“en”
     * @return
     */
    public static boolean specifiedLocate(Context context, @NonNull String locateStr) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith(locateStr)) {
            return true;
        } else {
            return false;
        }
    }

//    public static boolean isBackground(Context context) {
//        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
//        for (RunningAppProcessInfo appProcess : appProcesses) {
//            if (appProcess.processName.equals(context.getPackageName())) {
//                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
//                    Log.i("后台", appProcess.processName);
//                    return true;
//                }else{
//                    Log.i("前台", appProcess.processName);
//                    return false;
//                }
//            }
//        }
//        return false;
//    }

}
