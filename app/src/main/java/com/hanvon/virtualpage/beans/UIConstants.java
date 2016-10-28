package com.hanvon.virtualpage.beans;


import com.hanvon.virtualpage.R;

/**
 * @Description: 常量信息类，有字符串、图片资源ID等等常量信息
 * @Author: TaoZhi
 * @Date: 2016/3/31
 * @E_mail: taozhi@hanwang.com.cn
 */
public class UIConstants {

    public final static String DefaultNoteFileName = "NoteSaver";
    public final static String ManifestFilename = "Manifest.xml";
    public final static String DocumentFilename = "Document.xml";
    public final static String PageFilename = "Page.xml";
    public final static String IdxFilename = "dic.idx";
    public final static String DefaultPageKey = "defaultPage";
    public final static String ThumbnailFilename = "Thumbnail.png";
    public final static String ThumbnailFileType = ".png";
    public final static String Manifest_PATH = "/data/data/com.hanvon.virtualpage";
    public final static String ROOT_PATH = "/hanvon/com.hanvon.virtualpage";
    public final static String XMLFOLDER_PATH = "/hanvon/com.hanvon.virtualpage/xml";
    public final static String DB_PATH = "/hanvon/com.hanvon.virtualpage/db";
    public final static String SP_FILE_NAME = "NoteSaver";
    public static final String SP_IS_FIRST_LOAD = "isFirstLoad";
//    public static final String SP_HAS_OLD_DATA = "hasOldData";
//    public static final String SP_FOCUS_INDEX = "focusIndex";

    public static final String HAS_GRANTED_PERMISSION = "hasGrantedPermission";
    public static final String LOCATE_PAGE_POSITION = "locatePagePosition";

    public static final int DEFAULT_COVER_INDEX = 4; // 默认封皮背景索引值
    public static final int DEFAULT_PAGE_BG_INDEX = 0; // 默认封皮背景索引值

    public final static int PICTURE_WIDTH = 150;
    public final static int PICTURE_HEIGHT = 150;

    public final static int REQUESTCODE_PICTURE_ACTIVITY = 1;
    public static final int TAKE_PHOTO = 0;
    public static final int CHOSE_IMAGE = 2;

    public static final int JUMP_TO_LATEST = 0;
    public static final int JUMP_TO_END = 1;

    public static int[] ARRAY_NOTE_COVER = {
            R.drawable.note_cover1, R.drawable.note_cover2, R.drawable.note_cover3,
            R.drawable.note_cover4, R.drawable.note_cover5, R.drawable.note_cover6,
    };

    public static int[] ARRAY_NOTE_COVER_COLOR = {
            R.drawable.note_cover_1_bg, R.drawable.note_cover_2_bg, R.drawable.note_cover_3_bg,
            R.drawable.note_cover_4_bg, R.drawable.note_cover_5_bg, R.drawable.note_cover_6_bg,
    };

    public static int[] ARRAY_NOTE_COVER_TEXT_COLOR = {
            R.color.NoteCoverTextColor1, R.color.NoteCoverTextColor2, R.color.NoteCoverTextColor3,
            R.color.NoteCoverTextColor4, R.color.NoteCoverTextColor5, R.color.NoteCoverTextColor6,
    };

    public static int[] ARRAY_NOTE_OPEN_BG = {
            R.drawable.page_cover1_open_bg, R.drawable.page_cover2_open_bg, R.drawable.page_cover3_open_bg,
            R.drawable.page_cover4_open_bg, R.drawable.page_cover5_open_bg, R.drawable.page_cover6_open_bg,
    };

    public static int[] ARRAY_NOTE_OPEN_COVER = {
            R.drawable.note_cover1, R.drawable.note_cover2, R.drawable.note_cover3,
            R.drawable.note_cover4, R.drawable.note_cover5, R.drawable.note_cover6,
    };
//    public static int[] ARRAY_NOTE_OPEN_COVER = {
//            R.drawable.page_cover1, R.drawable.page_cover2, R.drawable.page_cover3,
//            R.drawable.page_cover4, R.drawable.page_cover5, R.drawable.page_cover6,
//    };

//    public static int[] ARRAY_PAGE_BG = {
//            R.drawable.page_bg_default, R.drawable.page_bg_1, R.drawable.page_bg_2,
//            R.drawable.page_bg_4, R.drawable.page_bg_5, R.drawable.page_bg_3,
//    };

    public static int[] ARRAY_PAGE_BG_REPEAT = {
            R.drawable.page_bg_repeat_default, R.drawable.page_bg_repeat_1, R.drawable.page_bg_repeat_2,
            R.drawable.page_bg_repeat_3, R.drawable.page_bg_repeat_4, R.drawable.page_bg_repeat_5,
    };

    /**
     * 根据Page背景索引值获取背景资源ID
     * @param resIdIndex String类型背景资源索引值
     * @return 可用的背景值
     */
    public static int getAvailableBgResIndex(String resIdIndex) {
        int resIndex = -1;
        try {
            resIndex = Integer.parseInt(resIdIndex);
        } catch (Exception ex) {
            resIndex = UIConstants.DEFAULT_PAGE_BG_INDEX;
        } finally {
            if (resIndex < 0 || resIndex > UIConstants.ARRAY_PAGE_BG_REPEAT.length) {
                resIndex = UIConstants.DEFAULT_PAGE_BG_INDEX;
            }
            return resIndex;
        }

    }

//    public static int getAvailableCoverIndex(String resIdIndex) {

    /**
     * 校验封皮索引值
     * @param resIdIndex
     * @return
     */
    public static int getAvailableCoverIndex(String resIdIndex) {
        int resBgIndex = -1;
        try {//防止转换类型错误
            resBgIndex = Integer.parseInt(resIdIndex);
            if (resBgIndex < 0 || resBgIndex >= UIConstants.ARRAY_NOTE_COVER.length) {
                resBgIndex = UIConstants.DEFAULT_COVER_INDEX;
            }
        } catch (Exception e) {
            resBgIndex = UIConstants.DEFAULT_COVER_INDEX;
        } finally {
            return resBgIndex;
        }
    }


}
