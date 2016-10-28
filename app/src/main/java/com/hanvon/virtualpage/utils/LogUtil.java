package com.hanvon.virtualpage.utils;

/**
 * 程序调试日志类，使用该类中的方法集中控制日志打印
 * @author TZ
 *
 */
public class LogUtil {
	
	public static boolean isDebug = true;
//	public static boolean isDebug = false;
	
	public static String TAG = "tztest";
	
	public static void i(String tag, String msg){
		if(isDebug)
			android.util.Log.i(tag, msg);
	}
	public static void d(String tag, String msg){
		if(isDebug)
			android.util.Log.d(tag, msg);
	}
	public static void e(String tag, String info){
		if(isDebug)
			android.util.Log.e(tag, info);
	}
	public static void v(String tag, String info){
		if(isDebug)
			android.util.Log.v(tag, info);	
	}
	public static void w(String tag, String info){
		if(isDebug)
			android.util.Log.w(tag, info);
	}
	
	/** 署名默认TAG的打印信息*/
	public static void i(String msg){
		if(isDebug)
			android.util.Log.i(TAG, msg);
	}
	/** 署名默认TAG的打印信息*/
	public static void e(String info){
		if(isDebug)
			android.util.Log.e(TAG, info);
	}
	/** 署名默认TAG的打印信息*/
	public static void v(String info){
		if(isDebug)
			android.util.Log.v(TAG, info);
	}
	/** 署名默认TAG的打印信息*/
	public static void w(String info){
		if(isDebug)
			android.util.Log.w(TAG, info);
	}
	public static void d(String info){
		if(isDebug)
			android.util.Log.d(TAG, info);
	}
	


}
