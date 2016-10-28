package com.hanvon.virtualpage.lib.beans;


import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.util.Xml;

import com.hanvon.core.Stroke;
import com.hanvon.virtualpage.lib.utils.BitmapUtil;
import com.hanvon.virtualpage.lib.utils.FileManager;
import com.hanvon.virtualpage.lib.utils.TimeHelper;

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
    private String mContent; //""
    private String mBackgroundFilename;//设置背景图片资源ID
    private boolean mMark;
    private Resolution mResolution; //屏幕尺寸
    private PageRotation mPageRotation; //纸张方向
    private List<Stroke> mStrokeList;
    private List<Object> mElementList;
    private Bitmap mThumbnail;//绘制完成图片
    private Document mOwner;

    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    private boolean mIsOld = false;

    public Page() {
        this("", "", false, new Resolution(), new PageRotation(), new ArrayList<Stroke>(),
                new ArrayList<Object>());
    }

    public Page(String createdUpdatedTime) {
        this();
        this.setCreatedTime(createdUpdatedTime);
        this.setUpdatedTime(createdUpdatedTime);
    }

    protected Page(String content, String backgroundFilename, boolean mark,
                   Resolution resolution, PageRotation pageRotation,List<Stroke> strokeList,
                   List<Object> elementList) {
        super();
        super.setPath(TimeHelper.getCurrentDateTime());
        mContent = content;
        mBackgroundFilename = backgroundFilename;
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

    public void setBackgroundFilename(String filename) {
        mBackgroundFilename = filename;
    }

    public String getBackgroundFilename() {
        return mBackgroundFilename;
    }

    public String getInfoesFilePath() {
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
        if (mOwner != null)
            return mOwner.getStoragePath() + this.getPath();
        else
            return null;
    }

    public void setMark(boolean flag) {
        mMark = flag;
    }

    public boolean getMark() {
        return mMark;
    }

//    public void setResolution(Resolution resolution) {
//        mResolution = resolution;
//    }
//
//    public Resolution getResolution() {
//        return mResolution;
//    }

    //cuishuo add for set page rotation
    public void setPageRotation(PageRotation pagerotation) {
        mPageRotation = pagerotation;
    }

    public PageRotation getPageRotation() {
        return mPageRotation;
    }
    //end by cuishuo

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
    }

    public Bitmap getThumbnail() {

        if (mThumbnail == null) {
            try {
//                mThumbnail = BitmapUtil.decodeFileBySize(getThumbnailFilePath(), 600, 800);
                mThumbnail = BitmapUtil.decodeFileBySize(getThumbnailFilePath(), 373, 600);
            } catch (Exception e){
                System.out.print(e.toString());
            }
        }

        return mThumbnail;
    }

    public void setOwner(Document document) {
        mOwner = document;
    }

    public Document getOwner() {
        return mOwner;
    }

    public boolean hasLoaded() {
        if (mStrokeList.size() > 0 || mElementList.size() > 0)
            return true;
        else
            return false;
    }

    public boolean open() {
        return open(getInfoesFilePath());
    }

    /**
     * Initialization a page_bg.
     */
    public boolean open(String path) {
        if (hasLoaded())
            return true;
        try {
            if (path != null) {
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputStream in = new FileInputStream(path);
                org.w3c.dom.Document document = docBuilder.parse(in);
                Element documentElement = document.getDocumentElement();
                //cuishuo add for get page rotation info for current page
                openPageRotation(documentElement.getElementsByTagName("PageRotation"));

//                openResolution(documentElement
//                        .getElementsByTagName("Resolution"));
//                openElementList(
//                        documentElement.getElementsByTagName("ElementList")
//                                .item(0).getChildNodes(),
//                        PageEditorActivity.appInstance);
                openStrokeList(
                        documentElement
                        .getElementsByTagName("StrokeList")
                        .item(0)
                        .getChildNodes());
                return true;
            } else
                return false;
        } catch (Exception ex) {
            return false;
        }
    }

//    private void openResolution(NodeList nodelist) {
//        NamedNodeMap resolutionAttributeMap = nodelist.item(0).getAttributes();
//        if (resolutionAttributeMap != null) {
//            int width, height;
//            width = Integer.parseInt(resolutionAttributeMap.getNamedItem(
//                    "width").getNodeValue());
//            height = Integer.parseInt(resolutionAttributeMap.getNamedItem(
//                    "height").getNodeValue());
//            mResolution = new Resolution(width, height);
//        }
//    }

    //cuishuo add for get page rotation info for current page
    private void openPageRotation(NodeList nodelist) {
        if(nodelist != null && nodelist.getLength() > 0) {
            NamedNodeMap rotationAttributeMap = nodelist.item(0).getAttributes();
            if (rotationAttributeMap != null) {
                int start, end;
                boolean reverse;
                start = Integer.parseInt(rotationAttributeMap.getNamedItem("start").getNodeValue());
                end = Integer.parseInt(rotationAttributeMap.getNamedItem("end").getNodeValue());
                reverse = Boolean.parseBoolean(rotationAttributeMap.getNamedItem("reverse").getNodeValue());
                mPageRotation = new PageRotation(start, end, reverse);
            }
        }
    }
    //end by cuishuo

//    private void openElementList(NodeList nodelist, Context context) {
//        mElementList.clear();
//        int x, y, width, height, textColor, textStyle;
//        float angle;
//        float textSize;
//        String source, text, picPath;
//        AbsoluteLayout.LayoutParams params;
//        for (int i = 0; i < nodelist.getLength(); i++) {
//            if (nodelist.item(i).getNodeType() == Node.ELEMENT_NODE) {
//                if (nodelist.item(i).getNodeName().equals("NoteEditText")) {
//                    NamedNodeMap editTextAttributeMap = nodelist.item(i)
//                            .getAttributes();
//                    if (editTextAttributeMap != null) {
//                        x = Integer.parseInt(editTextAttributeMap.getNamedItem(
//                                "x").getNodeValue());
//                        y = Integer.parseInt(editTextAttributeMap.getNamedItem(
//                                "y").getNodeValue());
//                        width = Integer.parseInt(editTextAttributeMap
//                                .getNamedItem("width").getNodeValue());
//                        height = Integer.parseInt(editTextAttributeMap
//                                .getNamedItem("height").getNodeValue());
//                        textSize = Float.parseFloat(editTextAttributeMap
//                                .getNamedItem("textSize").getNodeValue());
//                        textColor = Integer.parseInt(editTextAttributeMap
//                                .getNamedItem("textColor").getNodeValue());
//                        textStyle = Integer.parseInt(editTextAttributeMap
//                                .getNamedItem("isItalic").getNodeValue());
//                        text = editTextAttributeMap.getNamedItem("text")
//                                .getNodeValue();
//                        params = new AbsoluteLayout.LayoutParams(width, height, x, y);
//                        EditTextParams textParams = new EditTextParams(
//                                textStyle, textSize, textColor, text);
//                        NoteEditBuilder builder = new NoteEditBuilder(context,
//                                30);
//                        new ElementDirector().Construct(builder, params,
//                                textParams);
//                        mElementList.add(builder.getResult());
//                    }
//                }
//                if (nodelist.item(i).getNodeName().equals("NotePicture")) {
//                    NamedNodeMap pictureAttributeMap = nodelist.item(i)
//                            .getAttributes();
//                    if (pictureAttributeMap != null) {
//                        x = Integer.parseInt(pictureAttributeMap.getNamedItem(
//                                "x").getNodeValue());
//                        y = Integer.parseInt(pictureAttributeMap.getNamedItem(
//                                "y").getNodeValue());
//                        width = Integer.parseInt(pictureAttributeMap
//                                .getNamedItem("width").getNodeValue());
//                        height = Integer.parseInt(pictureAttributeMap
//                                .getNamedItem("height").getNodeValue());
//                        angle = Float.parseFloat(pictureAttributeMap.getNamedItem("angle").getNodeValue());
//                        source = pictureAttributeMap.getNamedItem("source")
//                                .getNodeValue();
//                        params = new AbsoluteLayout.LayoutParams(width, height, x, y);
//                        picPath = Workspace.getInstance().getCurrentPagePath()
//                                + "/" + source;
//                        NotePictureBuilder builder = new NotePictureBuilder(context,angle);
//                        new ElementDirector().Construct(builder, params,
//                                picPath);
//                        mElementList.add(builder.getResult(true));
//                    }
//                }
//            }
//        }
//    }

    private void openStrokeList(NodeList nodelist) {
        mStrokeList.clear();
        for (int index = 0; index < nodelist.getLength(); index++) {
            if (nodelist.item(index).getNodeType() == Node.ELEMENT_NODE) {
                int styleType = 1, widthType = 1, colorType = 0;
                if (nodelist.item(index).getNodeName().equals("stroke")) {
                    // stroke's style width and color
                    NamedNodeMap strokeAttributeMap = nodelist.item(index)
                            .getAttributes();
                    if (strokeAttributeMap != null) {
                        styleType = Integer.parseInt(strokeAttributeMap
                                .getNamedItem("style").getNodeValue());
                        widthType = Integer.parseInt(strokeAttributeMap
                                .getNamedItem("width").getNodeValue());
                        colorType = Integer.parseInt(strokeAttributeMap
                                .getNamedItem("color").getNodeValue());
                    }
                    // Points
                    NodeList pointNodeList = nodelist.item(index)
                            .getChildNodes();
                    List<Point> mPoints = new ArrayList<Point>();
                    List<Float> mPrs = new ArrayList<Float>();
                    for (int j = 0; j < pointNodeList.getLength(); j++) {
                        if (pointNodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            if (pointNodeList.item(j).getNodeName()
                                    .equals("Points")) {
                                NamedNodeMap pointAttributeMap = pointNodeList
                                        .item(j).getAttributes();
                                if (pointAttributeMap != null) {
                                    Node points = pointAttributeMap
                                            .getNamedItem("value");
                                    FromString(points.getNodeValue(), mPoints,
                                            mPrs);
                                }
                            }
                        }
                    }
                    Stroke stroke = new Stroke(styleType, colorType, widthType,
                            true, mPoints, mPrs);
                    mStrokeList.add(stroke);
                }
            }
        }
    }

    // Change the string of stroke to points and pressures.
    private void FromString(String info, List<Point> points,
                            List<Float> pressures) {
        if (info != null) {
            String[] locationInfoes = info.split(";");
            for (int index = 0; index < locationInfoes.length; index++) {
                String[] infoes = locationInfoes[index].split(",");
                if (infoes.length == 3) {
                    points.add(new Point(Integer.parseInt(infoes[0]), Integer
                            .parseInt(infoes[1])));
                    pressures.add(Integer.parseInt(infoes[2])
                            / pressureMultiple);
                }
            }
        }
    }

    public boolean saveOfNumberChange() {
        // TODO: 16-3-3 what to do
        boolean temp = saveThumbnail(null) && save(getInfoesFilePath());
        return temp;
    }

    /**
     * Save Page.xml and Thumbnail.
     */
    public boolean save() {
        return saveThumbnail(null) && save(getInfoesFilePath());
    }

    /**
     * Save the thumbnail
     *
     * @param path If path is null,the save path is default. If path is not null
     *             the thumbnail will be saved at that path.
     */
    public boolean saveThumbnail(String path) {
        try {
            if (path == null) {
                FileManager.saveBitmapToFile(mThumbnail, getThumbnailFilePath());
            }  else {
                FileManager.saveBitmapToFile(mThumbnail, path + UIConstants.ThumbnailFilename);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }finally {
            if(mThumbnail != null){
                mThumbnail = null;
            }
        }
    }

    short[] mPoints = null;
    int mCurrentIndex = 0;

    /**
     * Save all information of a page_bg that includes Page.xml and others files.
     */
    public boolean save(String path) {
        if (path != null) {
            try {
                StringWriter writer = new StringWriter();
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(writer);
                serializer.startDocument("UTF-8", true);
                serializer.startTag("", "Page");
//                saveResolution(serializer);
                //cuishuo add for save current page rotation info
                savePageRotation(serializer);
//                serializer.startTag("", "ElementList");
//                for (int index = 0; index < mElementList.size(); index++)
//                    saveElement(serializer, mElementList.get(index));
//                serializer.endTag("", "ElementList");
                serializer.startTag("", "StrokeList");

                mCurrentIndex = 0;
                int sum = 0;

                for (int index = 0; index < mStrokeList.size(); index++)
                    sum += mStrokeList.get(index).getHWPath().getPoints()
                            .size() * 2;

                if (sum != 0)
                    mPoints = new short[sum];

                for (int index = 0; index < mStrokeList.size(); index++)
                    saveStroke(serializer, mStrokeList.get(index));

//				if (sum != 0) {
//					String idxpath = getIdxFilePath();
//					if (idxpath != null) {
//						File idxFile = new File(idxpath);
//						if (idxFile.exists())
//							idxFile.delete();
////						FreeStylus.initDocEngine(idxpath);
////						FreeStylus.analysisStroke(mPoints);
////						FreeStylus.exitDocEngine();
//						mCurrentIndex = 0;
//					}
//				}

                serializer.endTag("", "StrokeList");
                serializer.endTag("", "Page");
                serializer.endDocument();
                File xmlFile = new File(path);
                if (xmlFile.exists())
                    xmlFile.delete();
                xmlFile.createNewFile();
                FileOutputStream os = new FileOutputStream(xmlFile);
                OutputStreamWriter osw = new OutputStreamWriter(os);
                osw.write(writer.toString());
                osw.close();
                os.close();
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
            }
        } else
            return false;
    }

//    private void saveResolution(XmlSerializer serializer)
//            throws IllegalArgumentException, IllegalStateException, IOException {
//        serializer.startTag("", "Resolution");
//        serializer.attribute("", "width", String.valueOf(mResolution.Width));
//        serializer.attribute("", "height", String.valueOf(mResolution.Height));
//        serializer.endTag("", "Resolution");
//    }

    //cuishuo add for save current page rotation info
    private void savePageRotation(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag("", "PageRotation");
        serializer.attribute("", "start", String.valueOf(mPageRotation.startRotation));
        serializer.attribute("", "end", String.valueOf(mPageRotation.endRotation));
        serializer.attribute("", "reverse", String.valueOf(mPageRotation.reverseRotation));
        serializer.endTag("", "PageRotation");
    }
    //end by cuishuo


    private void saveStroke(XmlSerializer serializer, Stroke stroke)
            throws IllegalArgumentException, IllegalStateException, IOException {
        serializer.startTag("", "stroke");
        serializer.attribute("", "style",
                String.valueOf(stroke.getHWPen().getStyle()));
        serializer.attribute("", "width",
                String.valueOf(stroke.getHWPen().getWidth()));
        serializer.attribute("", "color",
                String.valueOf(stroke.getHWPen().getTrueColor()));
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
                        // 绗旇抗缁撳熬鏍囪瘑
                        mPoints[mCurrentIndex++] = -1;
                        mPoints[mCurrentIndex++] = 0;
                    } else {
                        // 骞崇Щ鍧愭爣锛岄槻姝㈠潗鏍囦骇鐢熻礋鍊硷紝鍏ㄦ枃绗旇抗鎼滅储鍧愭爣鏄礋鍊奸兘鏄潪娉曠殑锛岄櫎浜嗙粨灏�
                        mPoints[mCurrentIndex++] = (short) (x + Page.offset);
                        mPoints[mCurrentIndex++] = (short) (y + Page.offset);
                    }
                }
                result.append(String.valueOf(x) + ",");
                result.append(String.valueOf(y) + ",");
                result.append(String.valueOf((int) (pressures.get(index) * pressureMultiple))
                        + ";");
            }
            return result.toString();
        } else {
            return null;
        }
    }

    @Override
    protected void onCreateByNamedNodeMap(NamedNodeMap namedNodeMap,
                                          BaseInfo item) {
        super.onCreateByNamedNodeMap(namedNodeMap, item);
        ((Page) item).setContent(String.valueOf(namedNodeMap.getNamedItem(
                "Content").getNodeValue()));

        String backgroundFilename = String.valueOf(namedNodeMap.getNamedItem("BackgroundFilename").getNodeValue());
        if (mIsOld)
            backgroundFilename = backgroundConverter(backgroundFilename);

        ((Page) item).setBackgroundFilename(backgroundFilename);
        ((Page) item).setMark(Boolean.parseBoolean(namedNodeMap.getNamedItem(
                "Mark").getNodeValue()));
    }

    private String backgroundConverter(String old) {
        // TODO: 16-3-3  unkown function
//		int oldId = Integer.parseInt(old);
//		switch(oldId)
//		{
//		case 2130837505:
//			return String.valueOf(2130837505);
//		case 2130837506:
//			return String.valueOf(2130837506);
//		case 2130837507:
//			return String.valueOf(2130837507);
//		case 2130837508:
//			return String.valueOf(2130837508);
//		case 2130837509:
//			return String.valueOf(2130837527);
//		case 2130837510:
//			return String.valueOf(2130837510);
//		case 2130837511:
//			return String.valueOf(2130837511);
//		case 2130837512:
//			return String.valueOf(2130837512);
//		case 2130837513:
//			return String.valueOf(2130837513);
//		case 2130837514:
//			return String.valueOf(2130837514);
//		case 2130837515:
//			return String.valueOf(2130837509);
//		}

        return String.valueOf(2130837509);
    }

    @Override
    protected void onFillSerializer(XmlSerializer serializer)
            throws IOException {
        serializer.startTag("", "Page");
        super.onFillSerializer(serializer);
        serializer.attribute("", "Content", mContent);
        serializer.attribute("", "BackgroundFilename", mBackgroundFilename);
        serializer.attribute("", "Mark", String.valueOf(mMark));
        serializer.endTag("", "Page");
    }

    @Override
    public void dispose() {
        clearData();
        mOwner = null;
    }

    public void clearData() {
        mPageRotation = null; //cuishuo add for reset page rotation info
        mStrokeList.clear();
        mElementList.clear();
        if (mThumbnail != null) {
            // TODO: 2016/4/18 注释了这里，解决了切屏时的Bitmap导致的问题
//            mThumbnail.recycle();
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