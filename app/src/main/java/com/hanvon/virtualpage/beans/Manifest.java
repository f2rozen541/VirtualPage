package com.hanvon.virtualpage.beans;

import android.util.Log;
import android.util.Xml;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @Description: 管理所有的Note数据的单例类，负责读写系统目录下的Manifest.xml文件
 * @Author: TaoZhi
 * @Date: 2016/3/31
 * @E_mail: taozhi@hanwang.com.cn
 */
public class Manifest {


//    private static final String sPath = UIConstants.Manifest_PATH + "/" + UIConstants.ManifestFilename;
    private static final String sPath = BaseApplication.getApplication().getFilesDir().getAbsolutePath() + "/" + UIConstants.ManifestFilename;
    private static Manifest sInstance;
    private List<Document> mDocuments;


    private Manifest() {
        mDocuments = new ArrayList<>();
//        Toast.makeText(BaseApplication.getContext(), "保存路径：" + sPath, Toast.LENGTH_SHORT).show();
        LogUtil.e("sPath", "保存路径为：" + sPath);
        if (new File(sPath).exists()) {
            open(getManifestXMLPath());
        }
    }


    public static Manifest getInstance() {
        if (null == sInstance) {
            sInstance = new Manifest();
        }
        return sInstance;
    }


    private String getManifestXMLPath() {
        return sPath;
    }


    public Document get(int location) {
        if (mDocuments.size() != 0) {
            return mDocuments.get(location);
        } else {
            return null;
        }
    }

    public List<Document> getDocuments() {
        return mDocuments;
    }

    public int size() {
        return mDocuments.size();
    }

    public boolean contains(Document item) {
        return mDocuments.contains(item);
    }
    public boolean contains(String title) {
        for (int index = 0; index < mDocuments.size(); index++) {
            if (title.equalsIgnoreCase(mDocuments.get(index).getTitle())) {
                return true;
            }
        }
        return false;
    }

    public void add(Document item) throws VirtualPageException {
        if (!(contains(item) || contains(item.getTitle()))) {
            if (!item.getTitle().equals(""))
                mDocuments.add(item);
            else
                throw new VirtualPageException(Workspace.getInstance()
                        .getResources()
                        .getString(R.string.attribute_empty_title_forbidden));
        } else
            throw new VirtualPageException(Workspace.getInstance().getResources()
                    .getString(R.string.title_already_exist));
    }
    public boolean delete(Document item) {
        if (Workspace.getInstance().getCurrentDocument() != null && Workspace.getInstance().getCurrentDocument().equals(item))
            Workspace.getInstance().setCurrentDocument(null);
        mDocuments.remove(item);
        try {
            FileManager.removeFile(new File(FileManager.getSDCardPath()
                    + item.getPath()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void clear() {
        mDocuments.clear();
    }

    public int indexOf(Document item) {
        return mDocuments.indexOf(item);
    }

    /**
     * get the max default number that used for naming new document.
     */
    public String getMaxDefault() {
        if (mDocuments.size() == 0) {
            return "Saver";
        } else {
            return "_" + TimeHelper.getCurrentDateTime();
        }
    }

    /**
     * 获取默认的命名：根据当前Note列表中包含命名头个数，为新的Note设置默认名称
     * @param headName 命名头（如："Untitled Note"）
     * @return 返回命名头+编号 （如："Untitled Note-2"）
     */
    public String getUntitledName(String headName) {
        String untitledName;
        List<String> nameList = new ArrayList<>();
        for (Document document : mDocuments) {
            nameList.add(document.getTitle());
        }
        int index = 1;
        while (true) {
            untitledName = headName + " " + index;
            if (nameList.contains(untitledName)) {
                index++;
            } else {
                break;
            }
        }
        nameList.clear();
        return untitledName;
    }

    private int checkAvailableIndex(String head, int index) {
        String name = head + " " + index;
        for (Document doc : mDocuments) {
            if (doc.getTitle().equals(name)) {
                index++;
//                LogUtil.e("NewNote", name + "===>已存在，索引增加===>" + index);
                index = checkAvailableIndex(head, index);
                break;
            }
        }
        return index;
    }


    public void save() {
        try {
            save(getManifestXMLPath());
        } catch (Exception ex) {

        }
    }

    /**
     * Save base information of all documents of a Manifest in documents.xml.
     */
    private void save(String path) throws IllegalArgumentException, IllegalStateException, IOException {
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(writer);
        serializer.startDocument("UTF-8", true);
        serializer.startTag("", "Manifest");
        serializer.startTag("", "DocumentList");
        for (int index = 0; index < mDocuments.size(); index++) {
            mDocuments.get(index).fillSerializer(serializer);
        }
        serializer.endTag("", "DocumentList");
        serializer.endTag("", "Manifest");
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
        } catch (FileNotFoundException e) {
            Log.v("FileNotFoundException", e.getMessage());
        } catch (IOException e) {
            Log.v("IOException", e.getMessage());
        }
    }

    /**
     * Initialization the base information of all documents.
     */
    private boolean open(String path) {
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream in = new FileInputStream(path);
            org.w3c.dom.Document document = docBuilder.parse(in);
            Element manifestElement = document.getDocumentElement();
            NodeList documentList = manifestElement
                    .getElementsByTagName("DocumentList").item(0)
                    .getChildNodes();
            NamedNodeMap namedNodeMap;
            Document item;
            clear();
            for (int index = 0; index < documentList.getLength(); index++) {
                if (Node.ELEMENT_NODE == documentList.item(index).getNodeType()
                        && documentList.item(index).getNodeName()
                        .equals("Document")) {
                    namedNodeMap = documentList.item(index).getAttributes();
                    if (null != namedNodeMap) {
                        item = new Document();
                        item.createByNamedNodeMap(namedNodeMap, item);
                        add(item);
                    }
                }
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
