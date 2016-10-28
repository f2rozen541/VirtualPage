package com.hanvon.virtualpage.lib.beans;

import android.util.Log;
import android.util.Xml;

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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Document名字不能重复，保证唯一
 */
public class Document extends BaseInfo implements IDisposable {

    private List<Page> mPages;
    private int mCount;
    private String mAuthor;
    private String mBrief;  //简介""
    private boolean mlock;
    private String mPassword;
    private Page titlePage;  //原PenNote标题显示
    private String titlePath;    //原PenNote标题缩略图路径s
    private String mBackgroundFilename;//标题背景图资源ID
    //用于前一版本兼容的问题
    private boolean mIsOld = false;

    /** 标注当前条目是否被选中*/
    private boolean checkState = false;
    /** 标注当前条目是否获得焦点*/
    private boolean focusState = false;

    public Document() {
        this(new ArrayList<Page>(), 0, "", "", false, "", "", "");
    }

    protected Document(List<Page> pages, int count, String author,
                       String brief, boolean lock, String password, String titleImagePath,
                       String mBackgroundFilename) {
        super();
        mPages = pages;
        mCount = count;
        mAuthor = author;
        mBrief = brief;
        mlock = lock;
        mPassword = password;
        this.titlePath = titleImagePath;
        this.mBackgroundFilename = mBackgroundFilename;
    }

    private String getDocumentXMLPath() {
        return getStoragePath() + UIConstants.DocumentFilename;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public int getCount() {
        if (mPages.size() != 0) {
            mCount = mPages.size();
        }
        return mCount;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setBrief(String brief) {
        mBrief = brief;
    }

    public String getBrief() {
        return mBrief;
    }

    public void setLock(boolean lock) {
        mlock = lock;
    }

    public boolean getLock() {
        return mlock;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getPassword() {
        return mPassword;
    }

    public int getPageId(Page page) {
        return mPages.indexOf(page) + 1;
    }

    public Page getPageById(int id) {
        if (id > 0 && id <= mPages.size() + 1)
            return mPages.get(id - 1);
        else
            return null;
    }

    public Page getPage(int location) {
        return mPages.get(location);
    }

    public Page getPreviousPage(Page page) {
        int location = mPages.indexOf(page);
        if (location > 0)
            return mPages.get(location - 1);
        else
            return null;
    }

    public Page getNextPage(Page page) {
        int location = mPages.indexOf(page);
        if (0 <= location && location < mPages.size() - 1)
            return mPages.get(location + 1);
        else
            return null;
    }

    public Page getFirstPage() {
        if (mPages.size() > 0)
            return mPages.get(0);
        else
            return null;
    }

    public Page getLastPage() {
        if (mPages.size() > 0)
            return mPages.get(mPages.size() - 1);
        else
            return null;
    }

    public List<Page> getPages() {
        return mPages;
    }

    public void setPages(List<Page> pages) {
        mPages = pages;
    }

    public List<Page> getPagesOrderByTitle() {
        List<Page> result = new ArrayList<Page>();
        result.addAll(mPages);
        Collections.sort(result, new PageTitleComparator());
        return result;
    }

    public List<Page> getPagesOrderByTime() {
        List<Page> result = new ArrayList<Page>();
        result.addAll(mPages);
        Collections.sort(result, new PageTimeComparator());
        return result;
    }

    public int size() {
        return mPages.size();
    }

    public boolean exist(Page item) {
        if (item != null)
            return mPages.contains(item);
        else
            return false;
    }

    public void add(Page item) {
        item.setOwner(this);
        mPages.add(item);
    }

    public boolean delete(Page item) {
//        if (Workspace.getInstance().getCurrentPage() != null
//                && Workspace.getInstance().getCurrentPage().equals(item))
//            Workspace.getInstance().setCurrentPage(null);
        mPages.remove(item);
        try {
            FileManager.removeFile(new File(item.getStoragePath()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            item.dispose();
        }
    }

    public void clear() {
        mPages.clear();
    }

    public int indexOf(Page item) {
        return mPages.indexOf(item);
    }

    @Override
    public String getStoragePath() {
        return FileManager.getSDCardPath() + this.getPath();
    }

    public boolean open() {
        return open(getDocumentXMLPath());
    }

    /**
     * Initialization base information of all pages.
     */
    public boolean open(String path) {
        org.w3c.dom.Document document;
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            InputStream in = new FileInputStream(path);
            document = docBuilder.parse(in);
        } catch (Exception ex) {
            return false;
        }
        Element documentElement = document.getDocumentElement();
        NodeList pageList = documentElement.getElementsByTagName("PageList")
                .item(0).getChildNodes();
        NamedNodeMap namedNodeMap = null;
        Page item = null;
        int length = pageList.getLength();
        mPages.clear();
        for (int index = 0; index < length; index++) {
            if (Node.ELEMENT_NODE == pageList.item(index).getNodeType()
                    && pageList.item(index).getNodeName().equals("Page")) {
                namedNodeMap = pageList.item(index).getAttributes();
                if (null != namedNodeMap) {
                    item = new Page();
                    if (mIsOld)
                        item.setIsOld(mIsOld);
                    item.createByNamedNodeMap(namedNodeMap, item);
                    if (mIsOld)
                        item.setIsOld(!mIsOld);
                    add(item);
                }
            }
        }
        return true;
    }

    public boolean save() {
        try {
            return save(getDocumentXMLPath());
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Save base information of all pages of a document in pages.xml.
     */
    public boolean save(String path) throws IllegalArgumentException,
            IllegalStateException, IOException {
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(writer);
        serializer.startDocument("UTF-8", true);
        serializer.startTag("", "Document");
        serializer.startTag("", "PageList");
        for (int index = 0; index < mPages.size(); index++)
            mPages.get(index).fillSerializer(serializer);
        serializer.endTag("", "PageList");
        serializer.endTag("", "Document");
        serializer.endDocument();
        try {
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
        }
    }

    @Override
    protected void onCreateByNamedNodeMap(NamedNodeMap namedNodeMap, BaseInfo item) {
        super.onCreateByNamedNodeMap(namedNodeMap, item);
        ((Document) item).setCount(Integer.parseInt(namedNodeMap.getNamedItem("Count").getNodeValue()));
        ((Document) item).setAuthor(String.valueOf(namedNodeMap.getNamedItem("Author").getNodeValue()));
        ((Document) item).setBrief(String.valueOf(namedNodeMap.getNamedItem("Brief").getNodeValue()));
        ((Document) item).setLock(Boolean.parseBoolean(namedNodeMap.getNamedItem("Lock").getNodeValue()));
        ((Document) item).setPassword(String.valueOf(namedNodeMap.getNamedItem("Password").getNodeValue()));
        Node titlePathNode = namedNodeMap.getNamedItem("TitlePath");

        Node BackgroundFilenameNode = namedNodeMap.getNamedItem("BackgroundFilename");
        if (BackgroundFilenameNode != null) {
            String path = String.valueOf(BackgroundFilenameNode.getNodeValue());
            ((Document) item).setBackgroundFilename(path);
            if (path.equals(""))
                // TODO: 16-3-3 决定名称
                ((Document) item).setBackgroundFilename("起个什么名字好呢");
//				((Document) item).setBackgroundFilename(String.valueOf(UIConstants.BOOK_Wrappage_RESOURCES[0]));
        } else
            // TODO: 16-3-3 决定名称
            ((Document) item).setBackgroundFilename("起个什么名字好呢");
//			((Document) item).setBackgroundFilename(String.valueOf(UIConstants.BOOK_Wrappage_RESOURCES[0]));
        {
            if (titlePathNode != null) {
                ((Document) item).setTitlePath(String.valueOf(titlePathNode.getNodeValue()));
            } else {
                //TODO: 16-3-3  生成背景图片
//            Bitmap bmp = BitmapFactory.decodeResource(Workspace.getInstance().getResources(), UIConstants.BOOK_COVER_RESOURCES[0]);
                // 不需要再保存封面，去除log中的异常 2016年6月6日 15:06:08
//                try {
//                    Bitmap bmp = BitmapFactory.decodeFile("");
//                    String path = this.getThumbnailFilePath();
//                    FileManager.saveBitmapToFile(bmp, path);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                String currentDateTime = TimeHelper.getCurrentDateTime();
                this.setTitlePath(TimeHelper.getCurrentDateTime());
                Page operatedPage = new Page(currentDateTime);
                operatedPage.setOwner(this);
                this.setTitlePage(operatedPage);

                //重新将旧版本的每一页的背景对应成新的背景ID
                mIsOld = true;
                this.open();
                this.save();
                mIsOld = false;
            }
        }
    }

    @Override
    protected void onFillSerializer(XmlSerializer serializer)
            throws IOException {
        serializer.startTag("", "Document");
        super.onFillSerializer(serializer);
        serializer.attribute("", "Count", String.valueOf(getCount()));
        serializer.attribute("", "Author", mAuthor);
        serializer.attribute("", "Brief", mBrief);
        serializer.attribute("", "Lock", String.valueOf(mlock));
        serializer.attribute("", "Password", mPassword);
        serializer.attribute("", "TitlePath", titlePath);
        serializer.attribute("", "BackgroundFilename", mBackgroundFilename);
        serializer.endTag("", "Document");
    }

    @Override
    public void dispose() {
        if (mPages != null) {
            int size = mPages.size();
            for (int index = 0; index < size; index++)
                mPages.get(index).dispose();
        }
    }

    public Page getTitlePage() {
        return titlePage;
    }

    public void setTitlePage(Page titlePage) {
        this.titlePage = titlePage;
    }

    public String getTitlePath() {
        return titlePath;
    }

    public void setTitlePath(String titleImagePath) {
        this.titlePath = titleImagePath;
    }

    public String getBackgroundFilename() {
        return mBackgroundFilename;
    }

    public void setBackgroundFilename(String mBackgroundFilename) {
        this.mBackgroundFilename = mBackgroundFilename;
    }

    public boolean isCheckState() {
        return checkState;
    }

    public void setCheckState(boolean checkState) {
        this.checkState = checkState;
    }

    public boolean isFocusState() {
        return focusState;
    }

    public void setFocusState(boolean focusState) {
        this.focusState = focusState;
    }
}