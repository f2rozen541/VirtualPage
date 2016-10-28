package com.hanvon.virtualpage.utils;

import java.util.Locale;

/**
 * Created by cuijingchuan on 2016/10/11.
 */
public class CommonUtils {

    public static boolean isRtl(){
        return Locale.getDefault().getLanguage().equals("iw") || Locale.getDefault().getLanguage().equals("ar");
    }

}
