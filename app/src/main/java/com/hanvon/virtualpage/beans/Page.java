package com.hanvon.virtualpage.beans;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.util.Xml;
import android.widget.AbsoluteLayout;
import android.widget.EditText;

import com.hanvon.core.Stroke;
import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.common.ElementDirector;
import com.hanvon.virtualpage.common.NoteEditBuilder;
import com.hanvon.virtualpage.common.NotePictureBuilder;
import com.hanvon.virtualpage.pageeditor.tools.picture.NotePicture;
import com.hanvon.virtualpage.pageeditor.tools.text.EditTextParams;
import com.hanvon.virtualpage.pageeditor.tools.text.NoteEditText;
import com.hanvon.virtualpage.utils.FileManager;
import com.hanvon.virtualpage.utils.LogUtil;
import com.hanvon.virtualpage.utils.TimeHelper;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Page extends BaseInfo implements IDisposable, Serializable {

    private final static float pressureMultiple = 10000.0f;
    private final static short offset = 100;
    private String mContent; // 内容字段（未用到）
    private String mBackgroundResIndex;//设置背景图片资源ID索引值
    private boolean mMark; // 标记位（未用到）
    private Resolution mResolution; //屏幕尺寸（未用到）
    private PageRotation mPageRotation; //纸张方向
    private List<Stroke> mStrokeList; // 笔迹数据对象列表
    private List<Object> mElementList; // 其他元素数据对象列表
    private Bitmap mThumbnail; // 绘制完成图片，（废弃了这种保存Bitmap对象的方式）（未用到）
    private Document mOwner; // 所属的Note对象
    private String mStoragePath; // 新增了一个字段，来保存文件路径，这个也会兼容setOwner()方法


    private boolean mIsOld = false; //（未用到）

    public Page() {
        this("", "", false, new Resolution(), new PageRotation(), new ArrayList<Stroke>(), new ArrayList<>());
    }

    public Page(String createdUpdatedTime) {
        this();
        this.setCreatedTime(createdUpdatedTime);
        this.setUpdatedTime(createdUpdatedTime);
    }

    protected Page(String content,
                   String BackgroundResIndex,
                   boolean mark,
                   Resolution resolution,
                   PageRotation pageRotation,
                   List<Stroke> strokeList,
                   List<Object> elementList) {
        super();
        super.setPath(TimeHelper.getCurrentDateTime());
        mContent = content;
        mBackgroundResIndex = BackgroundResIndex;
        mMark = mark;
        mResolution = resolution;
        mPageRotation = pageRotation;
        mStrokeList = strokeList;
        mElementList = elementList;
        mOwner = null;
    }

    public void setIsOld(boolean isOld) {
        mIsOld = isOld;
    }

    public int getId() {
        if (mOwner != null)
            return mOwner.getPageId(this);
        else
            return 0;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public String getContent() {
        return mContent;
    }

    /**
     * 设置当前Page的背景图片，有六种选择；跟缩略图不一样的是，这个是Page的背景图片
     *
     * @param resIdIndex 背景图片的资源ID（字符串类型）
     */
    public void setBackgroundResIndex(String resIdIndex) {
        mBackgroundResIndex = resIdIndex;
    }

    public String getBackgroundResIndex() {
        return mBackgroundResIndex;
    }

    public String getPageXmlPath() {
        String path = getStoragePath();
        if (path != null)
            return path + "/" + UIConstants.PageFilename;
        else
            return null;
    }

    public String getIdxFilePath() {
        String path = getStoragePath();
        if (path != null)
            return path + "/" + UIConstants.IdxFilename;
        else
            return null;
    }

    @Override
    public String getStoragePath() {
        if (mStoragePath == null) {
            if (mOwner == null) {
                LogUtil.e("当前的Page的Owner没有设置");
                mStoragePath = null;
            } else {
                mStoragePath = mOwner.getStoragePath() + this.getPath();
            }
        }
        return mStoragePath;
    }

    public void setStoragePath(String storagePath) {
        mStoragePath = storagePath;
    }

    public void setMark(boolean flag) {
        mMark = flag;
    }

    public boolean getMark() {
        return mMark;
    }

    public void setResolution(Resolution resolution) {
        mResolution = resolution;
    }

    public Resolution getResolution() {
        return mResolution;
    }

    //begin by cuishuo1
    //设置纸张方向，用于保存
    public void setPageRotation(PageRotation pagerotation) {
        mPageRotation = pagerotation;
    }

    public PageRotation getPageRotation() {
        return mPageRotation;
    }
    //end by cuishuo1

    public void setStrokeList(List<Stroke> strokeList) {
        mStrokeList = strokeList;
    }

    public List<Stroke> getStrokeList() {
        List<Stroke> result = new ArrayList<Stroke>(mStrokeList);
        return result;
    }

    public void setElementList(List<Object> elementList) {
        mElementList = elementList;
    }

    public List<Object> getElementList() {
        return mElementList;
    }

    public void setThumbnail(Bitmap bitmap) {
        mThumbnail = bitmap;
//        LruCacheUtils.getInstance().addBitmapToMemoryCache(getUpdatedTime(), mThumbnail);
    }

    /**
     * 即时保存Bitmap为文件
     * @param bitmap 需要保存的Bitmap
     * @return 成功返回true，失败返回false
     */
    public boolean saveThumbnailImmediately(Bitmap bitmap) {
        try {
            LogUtil.v("bmp", "++++++++++++++++++++++++++++++++++saveThumbnailImmediately--->开始+++++++++++++++++++++++++++++++++");
            if (bitmap == null || bitmap.isRecycled()) {
                LogUtil.e("即时保存缩略图失败：当前的Bitmap不可用！");
                return false;
            }
//            LruCacheUtils.getInstance().addBitmapToMemoryCache(getUpdatedTime(), bitmap);
            FileManager.removeFileType(getStoragePath(), "png"); // 保存之前，删除其他png文件
            FileManager.saveBitmapToFile(bitmap, getThumbnailFilePath());
            LogUtil.v("bmp", "++++++++++++++++++++++++++++++++++saveThumbnailImmediately--->完成+++++++++++++++++++++++++++++++++");
            return true;
        } catch (IOException ex) {
            return false;
        }
        finally {
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }
    }

    /**
     * 即时保存Bitmap为文件
     *
     * @param bitmap 需要保存的Bitmap
     * @return 成功返回true，失败返回false
     */
    public boolean savePdfImageImmediately(Bitmap bitmap) {
        try {
            LogUtil.e("cuijc3", "++++++++++++++++++++++++++++++++++savePdfImageImmediately--->开始+++++++++++++++++++++++++++++++++");
            if (bitmap == null || bitmap.isRecycled()) {
                LogUtil.e("cuijc3", "即时保存缩略图失败：当前的Bitmap不可用！");
                return false;
            }
//            LruCacheUtils.getInstance().addBitmapToMemoryCache(getUpdatedTime(), bitmap);
            File file = new File(getStoragePath() + "/" + "pdf");
            if (file.exists()) {
                file.delete();
            }
            file.mkdir();
            FileManager.removeFileType(getStoragePath() + "/" + "pdf", "png"); // 保存之前，删除其他png文件
            FileManager.saveBitmapToFile(bitmap, getPdfOriginalImageFilePath());
            LogUtil.e("cuijc3", "++++++++++++++++++++++++++++++++++savePdfImageImmediately--->完成+++++++++++++++++++++++++++++++++");
            return true;
        } catch (IOException ex) {
            LogUtil.i("cuijc3", "ex.toString:" + ex.toString());

            return false;
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }
    }

    public void setOwner(Document document) {
        mOwner = document;
        mStoragePath = null; // 可能转移到了新的Document中，所以需要重新生成存储路径
    }

    public Document getOwner() {
        return mOwner;
    }

    public boolean hasLoaded() {
        if ((mStrokeList != null && mStrokeList.size() > 0)
                || (mElementList != null &&mElementList.size() > 0)) {
            return true;
        } else {
            return false;
        }
    }


    public boolean open() {
        return open(getPageXmlPath());
    }

    /**
     * Initialization a page_bg.
     */
    public boolean open(String path) {
        if (hasLoaded())
            return true;
        try {
            if (path != null) {
                // 按照当前路径打开之后，顺便更新当前的mPath，不然可能找不到了 by tz 2016年5月16日 15:48:54
                String[] str = path.split(File.separator);
                int length = str.length;
                if (str[length - 1].endsWith(".xml")) { // 获取mPath
                    setPath(str[str.length - 2]);
                }

                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputStream in = new FileInputStream(path);
                org.w3c.dom.Document document = docBuilder.parse(in);
                Element documentElement = document.getDocumentElement();
//                openResolution(documentElement.getElementsByTagName("Resolution"));
                openPageRotation(documentElement.getElementsByTagName("PageRotation"));
                NodeList elementList = documentElement.getElementsByTagName("ElementList");
                if (elementList != null && elementList.getLength() > 0) {
//                    openElementList(elementList.item(0).getChildNodes(), BaseApplication.getContext());
                    openElementList(elementList.item(0).getChildNodes(), BaseApplication.getApplication().getTopActivity());
                }
                NodeList strokeList = documentElement.getElementsByTagName("StrokeList");
                if (strokeList != null && strokeList.getLength() > 0) {
                    openStrokeList(strokeList.item(0).getChildNodes());
                }
//                openStrokeList(strokeList.item(0).getChildNodes());
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private void openResolution(NodeList nodelist) {
        NamedNodeMap resolutionAttributeMap = nodelist.item(0).getAttributes();
        if (resolutionAttributeMap != null) {
            int width, height;
            width = Integer.parseInt(resolutionAttributeMap.getNamedItem("width").getNodeValue());
            height = Integer.parseInt(resolutionAttributeMap.getNamedItem("height").getNodeValue());
            mResolution = new Resolution(width, height);
        }
    }

    //begin by cuishuo1
    //从page.xml中获取纸张方向
    private void openPageRotation(NodeList nodelist) {
        if(nodelist != null && nodelist.getLength() > 0) {
            NamedNodeMap rotationAttributeMap = nodelist.item(0).getAttributes();
            if (rotationAttributeMap != null) {
                int start, end;
                boolean reverse;
                start = Integer.parseInt(rotationAttributeMap.getNamedItem("start").getNodeValue());
                end = Integer.parseInt(rotationAttributeMap.getNamedItem("end").getNodeValue());
                reverse = Boolean.parseBoolean(rotationAttributeMap.getNamedItem("reverse").getNodeValue());
                LogUtil.i("openPageRotation start = "+start);
                LogUtil.i("openPageRotation end = "+end);
                LogUtil.i("openPageRotation reverse = "+reverse);
                mPageRotation = new PageRotation(start, end, reverse);
            }
        }
    }
    //end by cuishuo1

    private void openElementList(NodeList nodelist, Context context) {
        mElementList.clear();
        int x, y, width, height, textColor, textFontIndex, textItalic, textBold, textUnderline;
        //float angle;
        float textSize;
        String source, text, picPath;
        AbsoluteLayout.LayoutParams params;
        for (int i = 0; i < nodelist.getLength(); i++) {
            if (nodelist.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (nodelist.item(i).getNodeName().equals("NoteEditText")) {
                    NamedNodeMap editTextAttributeMap = nodelist.item(i)
                            .getAttributes();
                    if (editTextAttributeMap != null) {
                        x = Integer.parseInt(editTextAttributeMap.getNamedItem("x").getNodeValue());
                        y = Integer.parseInt(editTextAttributeMap.getNamedItem("y").getNodeValue());
                        width = Integer.parseInt(editTextAttributeMap.getNamedItem("width").getNodeValue());
                        height = Integer.parseInt(editTextAttributeMap.getNamedItem("height").getNodeValue());
                        textSize = Float.parseFloat(editTextAttributeMap.getNamedItem("textSize").getNodeValue());
                        textColor = Integer.parseInt(editTextAttributeMap.getNamedItem("textColor").getNodeValue());
                        textFontIndex = Integer.parseInt(editTextAttributeMap.getNamedItem("textFontIndex").getNodeValue());
                        textItalic = Integer.parseInt(editTextAttributeMap.getNamedItem("isItalic").getNodeValue());
                        textBold = Integer.parseInt(editTextAttributeMap.getNamedItem("isBold").getNodeValue());
                        textUnderline = Integer.parseInt(editTextAttributeMap.getNamedItem("isUnderline").getNodeValue());
                        text = editTextAttributeMap.getNamedItem("text").getNodeValue();
                        params = new AbsoluteLayout.LayoutParams(width, height, x, y);
                        EditTextParams textParams = new EditTextParams(
                                textFontIndex,
                                textBold == 1,
                                textItalic == 1,
                                textUnderline == 1,
                                textSize,
                                textColor,
                                text
                        );
//                        EditTextParams textParams = new EditTextParams(typeface, textSize, textColor, text);
                        NoteEditBuilder builder = new NoteEditBuilder(context, 30);
                        new ElementDirector().Construct(builder, params, textParams);
                        mElementList.add(builder.getResult());
                    }
                }
                if (nodelist.item(i).getNodeName().equals("NotePicture")) {
                    NamedNodeMap pictureAttributeMap = nodelist.item(i).getAttributes();
                    if (pictureAttributeMap != null) {
                        x = Integer.parseInt(pictureAttributeMap.getNamedItem("x").getNodeValue());
                        y = Integer.parseInt(pictureAttributeMap.getNamedItem("y").getNodeValue());
                        width = Integer.parseInt(pictureAttributeMap.getNamedItem("width").getNodeValue());
                        height = Integer.parseInt(pictureAttributeMap.getNamedItem("height").getNodeValue());
                        //angle = Float.parseFloat(pictureAttributeMap.getNamedItem("angle").getNodeValue());
                        source = pictureAttributeMap.getNamedItem("source").getNodeValue();
                        params = new AbsoluteLayout.LayoutParams(width, height, x, y);

                        //shiyu modify
                        //picPath = Workspace.getInstance().getCurrentPagePath() + "/" + source;
                        picPath = source;


                        //NotePictureBuilder builder = new NotePictureBuilder(context,angle);
                        NotePictureBuilder builder = new NotePictureBuilder(context, width, height, 30);
                        new ElementDirector().Construct(builder, params, picPath);
                        //mElementList.add(builder.getResult(true));
                        mElementList.add(builder.getResult());
                    }
                }
            }
        }
    }

    private void openStrokeList(NodeList nodelist) {
        mStrokeList.clear();
        for (int index = 0; index < nodelist.getLength(); index++) {
            if (nodelist.item(index).getNodeType() == Node.ELEMENT_NODE) {
                int styleType = 1, widthType = 1, colorType = 0;
                if (nodelist.item(index).getNodeName().equals("stroke")) {
                    // stroke's style width and color
                    NamedNodeMap strokeAttributeMap = nodelist.item(index).getAttributes();
                    if (strokeAttributeMap != null) {
                        styleType = Integer.parseInt(strokeAttributeMap.getNamedItem("style").getNodeValue());
                        widthType = Integer.parseInt(strokeAttributeMap.getNamedItem("width").getNodeValue());
                        colorType = Integer.parseInt(strokeAttributeMap.getNamedItem("color").getNodeValue());
                    }
                    // Points
                    NodeList pointNodeList = nodelist.item(index).getChildNodes();
                    List<Point> mPoints = new ArrayList<Point>();
                    List<Float> mPrs = new ArrayList<Float>();
                    for (int j = 0; j < pointNodeList.getLength(); j++) {
                        if (pointNodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            if (pointNodeList.item(j).getNodeName().equals("Points")) {
                                NamedNodeMap pointAttributeMap = pointNodeList.item(j).getAttributes();
                                if (pointAttributeMap != null) {
                                    Node points = pointAttributeMap.getNamedItem("value");
                                    FromString(points.getNodeValue(), mPoints, mPrs);
                                }
                            }
                        }
                    }
                    Stroke stroke = new Stroke(styleType, colorType, widthType, true, mPoints, mPrs);
                    mStrokeList.add(stroke);
                }
            }
        }
    }

    // Change the string of stroke to points and pressures.
    private void FromString(String info, List<Point> points, List<Float> pressures) {
        if (info != null) {
            String[] locationInfoes = info.split(";");
            for (int index = 0; index < locationInfoes.length; index++) {
                String[] infoes = locationInfoes[index].split(",");
                if (infoes.length == 3) {
                    points.add(new Point(Integer.parseInt(infoes[0]), Integer.parseInt(infoes[1])));
                    pressures.add(Integer.parseInt(infoes[2]) / pressureMultiple);
                }
            }
        }
    }

    public boolean saveOfNumberChange() {
        // TODO: 16-3-3 what to do
//        LogUtil.i("saver", "保存数据：mElementList.size(2)====>" + mElementList.size());
//        LogUtil.i("saver", "保存数据：getPageXmlPath(2)====>" + getPageXmlPath());
//        return saveThumbnail(null) && savePageXml(getPageXmlPath());
        // 保存缩略图的操作已经在saveThumbImmediately(Bitmap bitmap)方法中完成 by tz 2016年5月17日 14:46:20
        return savePageXml(getPageXmlPath());
    }

    /**
     * Save Page.xml and Thumbnail.
     */
    public boolean save() {
//        LogUtil.i("saver", "保存数据：mElementList.size(1)====>" + mElementList.size());
//        LogUtil.i("saver", "保存数据：getPageXmlPath(1)====>" + getPageXmlPath());
//        return saveThumbnail(null) && savePageXml(getPageXmlPath());
        // 保存缩略图的操作已经在saveThumbImmediately(Bitmap bitmap)方法中完成
        return savePageXml(getPageXmlPath());
    }


    short[] mPoints = null;
    int mCurrentIndex = 0;

    /**
     * Save all information of a page_bg that includes Page.xml and others files.
     */
    private boolean savePageXml(String path) {
        if (path != null) {
            try {
                StringWriter writer = new StringWriter();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(writer);
                serializer.startDocument("UTF-8", true);
                serializer.startTag("", "Page");
                saveResolution(serializer);
                savePageRotation(serializer);

                serializer.startTag("", "ElementList");
                for (int index = 0; index < mElementList.size(); index++) {
                    saveElement(serializer, mElementList.get(index));
                }
                serializer.endTag("", "ElementList");

                serializer.startTag("", "StrokeList");
                mCurrentIndex = 0;
                int sum = 0;
                for (int index = 0; index < mStrokeList.size(); index++) {
                    sum += mStrokeList.get(index).getHWPath().getPoints().size() * 2;
                }
                if (sum != 0){
                    mPoints = new short[sum];
                }
                for (int index = 0; index < mStrokeList.size(); index++) {
                    saveStroke(serializer, mStrokeList.get(index));
                }

                serializer.endTag("", "StrokeList");
                serializer.endTag("", "Page");
                serializer.endDocument();
                File xmlFile = new File(path);
                if (xmlFile.exists()) {
                    xmlFile.delete();
                }
                xmlFile.createNewFile();
                FileOutputStream os = new FileOutputStream(xmlFile);
                OutputStreamWriter osw = new OutputStreamWriter(os);
                osw.write(writer.toString());
                osw.close();
                os.close();

                // 需要更新Owner的更新时间
                if (mOwner != null) {
                    mOwner.setUpdatedTime(getUpdatedTime());
                    Manifest.getInstance().save();
                }
                return true;
            } catch (FileNotFoundException e) {
                Log.v("FileNotFoundException", e.getMessage());
                return false;
            } catch (IOException e) {
                Log.v("IOException", e.getMessage());
                return false;
            } catch (IllegalArgumentException e) {
                Log.v("IllegalArgsException", e.getMessage());
                return false;
            } catch (IllegalStateException e) {
                Log.v("IllegalStateException", e.getMessage());
                return false;
            } catch (Exception e) {
                Log.v("Exception", e.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    private void saveResolution(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag("", "Resolution");
        serializer.attribute("", "width", String.valueOf(mResolution.Width));
        serializer.attribute("", "height", String.valueOf(mResolution.Height));
        serializer.endTag("", "Resolution");
    }

    //begin by cuishuo1
    //保存纸张方向属性到page.xml
    private void savePageRotation(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        LogUtil.i("savePageRotation start = "+mPageRotation.startRotation);
        LogUtil.i("savePageRotation end = "+mPageRotation.endRotation);
        LogUtil.i("savePageRotation reverse = "+mPageRotation.reverseRotation);
        serializer.startTag("", "PageRotation");
        serializer.attribute("", "start", String.valueOf(mPageRotation.startRotation));
        serializer.attribute("", "end", String.valueOf(mPageRotation.endRotation));
        serializer.attribute("", "reverse", String.valueOf(mPageRotation.reverseRotation));
        serializer.endTag("", "PageRotation");
    }
    //end by cuishuo1

    private void saveElement(XmlSerializer serializer, Object item)
            throws IllegalArgumentException, IllegalStateException, IOException {
        if (item instanceof NoteEditText) {
            NoteEditText noteEditText = (NoteEditText) item;
            serializer.startTag("", "NoteEditText");
            serializer.attribute("", "x",
                    String.valueOf(((AbsoluteLayout.LayoutParams) noteEditText.getLayoutParams()).x));
            serializer.attribute("", "y",
                    String.valueOf(((AbsoluteLayout.LayoutParams) noteEditText.getLayoutParams()).y));
            serializer.attribute("", "width",
                    String.valueOf(((AbsoluteLayout.LayoutParams) noteEditText.getLayoutParams()).width));
            serializer.attribute("", "height",
                    String.valueOf(((AbsoluteLayout.LayoutParams) noteEditText.getLayoutParams()).height));

            float textSize = ((EditText) noteEditText.getContentView()).getTextSize();
            serializer.attribute("", "textSize", String.valueOf(textSize));

            serializer.attribute("", "textColor",
                    String.valueOf(((EditText) noteEditText.getContentView()).getCurrentTextColor()));

            serializer.attribute("", "textFontIndex", String.valueOf(noteEditText.getFontTypeIndex()));

            serializer.attribute("", "isItalic", noteEditText.getIsItalic() == true ? "1" : "0");

            serializer.attribute("", "isBold", noteEditText.getIsBold() == true ? "1" : "0");

            serializer.attribute("", "isUnderline", ((EditText) noteEditText.getContentView()).getPaint().isUnderlineText() ? "1" : "0");

            serializer.attribute("", "text", String.valueOf(((EditText) noteEditText.getContentView()).getText()));

            serializer.endTag("", "NoteEditText");
        } else if (item instanceof NotePicture) {
            serializer.startTag("", "NotePicture");
            serializer.attribute("", "x",
                    String.valueOf(((AbsoluteLayout.LayoutParams) ((NotePicture) item).getLayoutParams()).x));
            serializer.attribute("", "y",
                    String.valueOf(((AbsoluteLayout.LayoutParams) ((NotePicture) item).getLayoutParams()).y));
            serializer.attribute("", "width",
                    String.valueOf(((AbsoluteLayout.LayoutParams) ((NotePicture) item).getLayoutParams()).width));
            serializer.attribute("", "height",
                    String.valueOf(((AbsoluteLayout.LayoutParams) ((NotePicture) item).getLayoutParams()).height));
            //serializer.attribute("", "angle", String.valueOf(((NotePicture) item).getCurrentAngle()));
            String contentParam = ((NotePicture) (item)).getTag().toString();

            //shiyu modify
            //String source = contentParam.substring(
            //        contentParam.lastIndexOf("/") + 1, contentParam.length());
            String source = contentParam;


            serializer.attribute("", "source", source);
            serializer.endTag("", "NotePicture");
        }
    }

    private void saveStroke(XmlSerializer serializer, Stroke stroke)
            throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag("", "stroke");
        serializer.attribute("", "style", String.valueOf(stroke.getHWPen().getStyle()));
        serializer.attribute("", "width", String.valueOf(stroke.getHWPen().getWidth()));
        serializer.attribute("", "color", String.valueOf(stroke.getHWPen().getTrueColor()));
        List<Point> points = stroke.getHWPath().getPoints();
        List<Float> prs = stroke.getHWPath().getPrs();
        if (points != null) {
            serializer.startTag("", "Points");
            serializer.attribute("", "value", ToString(points, prs));
            serializer.endTag("", "Points");
        }
        serializer.endTag("", "stroke");
    }

    // Change the stroke info to string. Like "x,y,p;x,y,p;......"
    private String ToString(List<Point> points, List<Float> pressures) {
        StringBuffer result = new StringBuffer(2000);
        if (points != null) {
            int x, y;
            for (int index = 0; index < points.size(); index++) {
                x = points.get(index).x;
                y = points.get(index).y;
                if (mPoints != null) {
                    if (index == points.size() - 1) {

                        mPoints[mCurrentIndex++] = -1;
                        mPoints[mCurrentIndex++] = 0;
                    } else {

                        mPoints[mCurrentIndex++] = (short) (x + Page.offset);
                        mPoints[mCurrentIndex++] = (short) (y + Page.offset);
                    }
                }
                result.append(String.valueOf(x) + ",");
                result.append(String.valueOf(y) + ",");
                result.append(String.valueOf((int) (pressures.get(index) * pressureMultiple)) + ";");
            }
            return result.toString();
        } else {
            return null;
        }
    }

    @Override
    protected void onCreateByNamedNodeMap(NamedNodeMap namedNodeMap, BaseInfo item) {
        super.onCreateByNamedNodeMap(namedNodeMap, item);
        ((Page) item).setContent(String.valueOf(namedNodeMap.getNamedItem("Content").getNodeValue()));
        String backgroundFilename = String.valueOf(namedNodeMap.getNamedItem("BackgroundFilename").getNodeValue());
        if (mIsOld) {
            backgroundFilename = backgroundConverter(backgroundFilename);
        }

        ((Page) item).setBackgroundResIndex(backgroundFilename);
        ((Page) item).setMark(Boolean.parseBoolean(namedNodeMap.getNamedItem("Mark").getNodeValue()));
    }

    private String backgroundConverter(String old) {
        return String.valueOf(UIConstants.DEFAULT_PAGE_BG_INDEX);
    }

    @Override
    protected void onFillSerializer(XmlSerializer serializer)
            throws IOException {
        serializer.startTag("", "Page");
        super.onFillSerializer(serializer);
        serializer.attribute("", "Content", mContent);
        serializer.attribute("", "BackgroundFilename", mBackgroundResIndex);
        serializer.attribute("", "Mark", String.valueOf(mMark));
        serializer.endTag("", "Page");
    }

    @Override
    public void dispose() {
        clearData();
        mOwner = null;
    }

    public void clearData() {
        mPageRotation = null;
        mStrokeList.clear();
        mElementList.clear();
        if (mThumbnail != null) {
            mThumbnail.recycle();
            mThumbnail = null;
        }
    }
}

class PageTitleComparator implements Comparator<Page> {
    @Override
    public int compare(Page page1, Page page2) {
        return page1.getTitle().compareTo(page2.getTitle());
    }
}

class PageTimeComparator implements Comparator<Page> {
    @Override
    public int compare(Page page1, Page page2) {
        return page1.getCreatedTime().compareTo(page2.getCreatedTime());
    }
}
