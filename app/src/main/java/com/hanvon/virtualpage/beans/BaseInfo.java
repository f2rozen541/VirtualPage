package com.hanvon.virtualpage.beans;


import org.w3c.dom.NamedNodeMap;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * @Description: Page和Note的基类，保存通用的必要字段
 * @Author: TaoZhi
 * @Date: 2016/3/31
 * @E_mail: taozhi@hanwang.com.cn
 */
public abstract class BaseInfo {
    /**
     * 文件名，如：document：NoteSaver,Note_20161005165603
     *            page:""
     */
    private String mTitle;
    /**
     * 文件路径，如：20161005172835
     */
    private String mPath;
    private String mCreatedTime;
    private String mUpdatedTime;

    protected BaseInfo() {
        this("", "", "", "");
    }

    protected BaseInfo(String title, String path, String createdTime,
                       String updatedTime) {
        mTitle = title;
        mPath = path;
        mCreatedTime = createdTime;
        mUpdatedTime = updatedTime;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getPath() {
        return mPath;
    }

    /**
     * 获取缩略图文件的路径，这里用最后更新时间作为文件名来保存，是为了保证重新加载图片的时候不会去用缓存中的数据
     * @return
     */
    public String getThumbnailFilePath() {
        String path = getStoragePath();
        if (path != null)
//            return path + "/" + UIConstants.ThumbnailFilename;
            return path + "/" + getUpdatedTime() + UIConstants.ThumbnailFileType;
        else
            return null;
    }
    /**
     * 获取缩略图pdf文件的路径，这里用最后更新时间作为文件名来保存，是为了保证重新加载图片的时候不会去用缓存中的数据
     * @return
     */
    public String getPdfOriginalImageFilePath(){
        String path = getStoragePath();
        if (path != null)
//            return path + "/" + UIConstants.ThumbnailFilename;
            return path + "/" + "pdf/" + getUpdatedTime() + UIConstants.ThumbnailFileType;
        else
            return null;
    }

    public abstract String getStoragePath();

    public void setCreatedTime(String time) {
        mCreatedTime = time;
    }

    public String getCreatedTime() {
        return mCreatedTime;
    }

    public void setUpdatedTime(String time) {
        mUpdatedTime = time;
    }

    public String getUpdatedTime() {
        return mUpdatedTime;
    }

    public void createByNamedNodeMap(NamedNodeMap namedNodeMap, BaseInfo item) {
        onCreateByNamedNodeMap(namedNodeMap, item);
    }

    protected void onCreateByNamedNodeMap(NamedNodeMap namedNodeMap, BaseInfo item) {
        item.setTitle(String.valueOf(namedNodeMap.getNamedItem("Title").getNodeValue()));
        item.setPath(String.valueOf(namedNodeMap.getNamedItem("Path").getNodeValue()));
        item.setCreatedTime(String.valueOf(namedNodeMap.getNamedItem("CreatedTime").getNodeValue()));
        item.setUpdatedTime(String.valueOf(namedNodeMap.getNamedItem("UpdatedTime").getNodeValue()));
    }

    public void fillSerializer(XmlSerializer serializer) throws IOException {
        onFillSerializer(serializer);
    }

    protected void onFillSerializer(XmlSerializer serializer) throws IOException {
        serializer.attribute("", "Title", mTitle);
        serializer.attribute("", "Path", mPath);
        serializer.attribute("", "CreatedTime", mCreatedTime);
        serializer.attribute("", "UpdatedTime", mUpdatedTime);
    }
}

interface IDisposable {
    void dispose();
}
