package com.hanvon.virtualpage.common;

import android.graphics.Typeface;
import android.util.Log;
import android.util.Xml;
import android.widget.AbsoluteLayout;
import android.widget.EditText;

import com.hanvon.virtualpage.pageeditor.tools.picture.NotePicture;
import com.hanvon.virtualpage.pageeditor.tools.text.NoteEditText;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * -------------------------------
 * Description:
 *
 * -------------------------------
 * Author:  hll
 * Date:    2016/2/24
 */
public class ElementXmlHelper {
    private static Document mDocument;
    private static Element mPageElement;
    private static ArrayList<Object> elementList;

    private static NodeList mElementNodeList;
    private static XmlSerializer mSerializer;
    private static StringWriter mWriter;

    public static void saveToXml(String path,
                                 ArrayList<ElementLayout> elementList)
            throws IllegalArgumentException, IllegalStateException, IOException {

        mSerializer = Xml.newSerializer();
        mWriter = new StringWriter();
        mSerializer.setOutput(mWriter);
        mSerializer.startDocument("UTF-8", true);
        mSerializer.startTag("", "Page");
        mSerializer.startTag("", "ElementList");

        if (elementList != null) {
            for (int i = 0; i < elementList.size(); i++) {
                appendElement(elementList.get(i));
            }
        }

        mSerializer.endTag("", "ElementList");
        mSerializer.endTag("", "Page");
        mSerializer.endDocument();
        try {
            File xmlFile = new File(path);
            FileOutputStream os = new FileOutputStream(xmlFile);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            osw.write(mWriter.toString());
            osw.close();
            os.close();
        } catch (FileNotFoundException e) {
            Log.v("FileNotFoundException", e.getMessage());

        } catch (IOException e) {
            Log.v("IOException", e.getMessage());
        }
    }

    private static void appendElement(Object layout)
            throws IllegalArgumentException, IllegalStateException, IOException {
        if (layout instanceof NoteEditText) {
            mSerializer.startTag("", "NoteEditText");
            mSerializer.attribute("", "x", String
                    .valueOf(((AbsoluteLayout.LayoutParams) ((NoteEditText)layout)
                            .getLayoutParams()).x));
            mSerializer.attribute("", "y", String
                    .valueOf(((AbsoluteLayout.LayoutParams) ((NoteEditText)layout)
                            .getLayoutParams()).y));
            mSerializer.attribute("", "width", String
                    .valueOf(((AbsoluteLayout.LayoutParams) ((NoteEditText)layout)
                            .getLayoutParams()).width));
            mSerializer.attribute("", "height", String
                    .valueOf(((AbsoluteLayout.LayoutParams) ((NoteEditText)layout)
                            .getLayoutParams()).height));
            mSerializer.attribute("", "textSize",
                    String.valueOf(((EditText) ((NoteEditText)layout).getContentView())
                            .getTextSize()));
            mSerializer.attribute("", "textColor", String
                    .valueOf(((EditText) ((NoteEditText)layout).getContentView())
                            .getCurrentTextColor()));
            Typeface tf = ((EditText) ((NoteEditText)layout).getContentView()).getTypeface();
            mSerializer.attribute("", "isItalic",
                    tf != null && tf.isItalic() ? "1" : "0");
            mSerializer.attribute("", "text", String.valueOf(((EditText) ((NoteEditText)layout)
                    .getContentView()).getText()));
            mSerializer.endTag("", "NoteEditText");
        } else if (layout instanceof NotePicture) {
            mSerializer.startTag("", "NotePicture");
            mSerializer.attribute("", "x", String
                    .valueOf(((AbsoluteLayout.LayoutParams) (((NotePicture) layout))
                            .getLayoutParams()).x));
            mSerializer.attribute("", "y", String
                    .valueOf(((AbsoluteLayout.LayoutParams) ((NotePicture)layout)
                            .getLayoutParams()).y));
            mSerializer.attribute("", "width", String
                    .valueOf(((AbsoluteLayout.LayoutParams) ((NotePicture)layout)
                            .getLayoutParams()).width));
            mSerializer.attribute("", "height", String
                    .valueOf(((AbsoluteLayout.LayoutParams) ((NotePicture)layout)
                            .getLayoutParams()).height));
            //mSerializer.attribute("", "angle", String.valueOf(((NotePicture) layout).getCurrentAngle()));
            String contentParam = (((NotePicture)layout)).getTag().toString();
            
            //shiyu modify
            //String source = contentParam.substring(
            //        contentParam.lastIndexOf("/") + 1, contentParam.length());
            String source = contentParam;


            mSerializer.attribute("", "source", source);
            mSerializer.endTag("", "NotePicture");
        }
    }

//    public static ArrayList<Object> readFromXml(String path,
//                                                       Context context) throws SAXException, IOException,
//            ParserConfigurationException {
//        if (elementList != null)
//            elementList.clear();
//        elementList = new ArrayList<Object>();
//
//        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
//                .newInstance();
//        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
//        InputStream in = new FileInputStream(path);
//        mDocument = docBuilder.parse(in);
//        mPageElement = mDocument.getDocumentElement();
//
//        mElementNodeList = mPageElement.getElementsByTagName("ElementList")
//                .item(0).getChildNodes();
//
//        for (int i = 0; i < mElementNodeList.getLength(); i++) {
//            if (mElementNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
//                if (mElementNodeList.item(i).getNodeName()
//                        .equals("NoteEditText")) {
//
//                    NamedNodeMap editTextAttributeMap = mElementNodeList
//                            .item(i).getAttributes();
//                    if (editTextAttributeMap != null) {
//                        Node editTextX = editTextAttributeMap.item(0); // x
//                        Node editTextY = editTextAttributeMap.item(1); // y
//                        Node editTextWidth = editTextAttributeMap.item(2); // width
//                        Node editTextHeight = editTextAttributeMap.item(3); // height
//                        Node editTextSize = editTextAttributeMap.item(4); // textSize
//                        Node editTextColor = editTextAttributeMap.item(5); // textColor
//                        Node editTextStyle = editTextAttributeMap.item(6); // textStyle
//                        Node editTextText = editTextAttributeMap.item(7); // text
//
//                        int x = Integer.parseInt(editTextX.getNodeValue());
//                        int y = Integer.parseInt(editTextY.getNodeValue());
//                        int width = Integer.parseInt(editTextWidth
//                                .getNodeValue());
//                        int height = Integer.parseInt(editTextHeight
//                                .getNodeValue());
//                        float textSize = Float.parseFloat(editTextSize
//                                .getNodeValue());
//                        int textColor = Integer.parseInt(editTextColor
//                                .getNodeValue());
//                        int textStyle = Integer.parseInt(editTextStyle
//                                .getNodeValue());
//                        String text = editTextText.getNodeValue();
//
//                        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width, height,
//                                x, y);
//                        EditTextParams textParams = new EditTextParams(
//                                textStyle, textSize, textColor, text);
//
//                        NoteEditBuilder builder = new NoteEditBuilder(context,
//                                30);
//                        new ElementDirector().Construct(builder, params,
//                                textParams);
//                        ElementLayout editText = builder.getResult();
//                        elementList.add(editText);
//                    }
//                }
//                if (mElementNodeList.item(i).getNodeName()
//                        .equals("NotePicture")) {
//
//                    NamedNodeMap pictureAttributeMap = mElementNodeList.item(i)
//                            .getAttributes();
//                    if (pictureAttributeMap != null) {
//                        Node pictureX = pictureAttributeMap.item(0); // x
//                        Node pictureY = pictureAttributeMap.item(1); // y
//                        Node pictureWidth = pictureAttributeMap.item(2); // width
//                        Node pictureHeight = pictureAttributeMap.item(3); // height
//                        Node pictureAngle = pictureAttributeMap.item(4); // angle
//                        Node pictureSource = pictureAttributeMap.item(5); // source
//
//                        int x = Integer.parseInt(pictureX.getNodeValue());
//                        int y = Integer.parseInt(pictureY.getNodeValue());
//                        int width = Integer.parseInt(pictureWidth
//                                .getNodeValue());
//                        int height = Integer.parseInt(pictureHeight
//                                .getNodeValue());
//                        float angle = Float.parseFloat(pictureAngle.getNodeValue());
//                        String source = pictureSource.getNodeValue();
//
//                        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width, height,
//                                x, y);
//
//                        String picPath = path.substring(0,
//                                path.lastIndexOf("/") + 1);
//
//                        NotePictureBuilder builder = new NotePictureBuilder(
//                                context, 30);
//
//                        //shiyu modify
//                        //new ElementDirector().Construct(builder, params,
//                        //        picPath + source);
//
//                        new ElementDirector().Construct(builder, params,
//                                source);
//
//
//                        View picture = builder.getResult();
//                        elementList.add(picture);
//                    }
//                }
//            }
//        }
//        return elementList;
//    }
}

