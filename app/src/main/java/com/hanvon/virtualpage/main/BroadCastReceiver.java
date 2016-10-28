package com.hanvon.virtualpage.main;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.utils.LogUtil;

public class BroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			LogUtil.i("broadcastreceive:", "Floating Window Status change onReceive=========");

			if (action.equals("com.havon.portableink.status.SHOW")) {

				System.out.println("com.havon.portableink.status.SHOW============");

//				BaseApplication.getApplication().AppExit();
				BaseApplication.killMyself();
			}
		}
	}

	
