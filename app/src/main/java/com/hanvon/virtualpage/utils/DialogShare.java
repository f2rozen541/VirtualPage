package com.hanvon.virtualpage.utils;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.UIConstants;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.hanvon.virtualpage.utils.FileManager;
import com.orhanobut.logger.Logger;

/**
 * Created by shihuijie on 16-4-8.
 */
public class DialogShare extends Dialog implements AdapterView.OnItemClickListener {

    private String TAG = "DialogShare";

    private LinearLayout mLayout;
    private GridView mGridView;
    private float mDensity;
    private String msgText = "分享了...";
    private Object mImgPath;
    private int mScreenOrientation;
    private List<ShareItem> mListData;

    Boolean isGallery = true;


    private Handler mHandler = new Handler();
    private Runnable work = new Runnable() {
        public void run() {
            int orient = getScreenOrientation();
            if (orient != mScreenOrientation) {
                if (orient == 0)
                    mGridView.setNumColumns(4);
                else {
                    mGridView.setNumColumns(4);
                }
                mScreenOrientation = orient;
                ((DialogShare.MyAdapter) mGridView.getAdapter()).notifyDataSetChanged();
            }
            mHandler.postDelayed(this, 1000L);
        }
    };

    public DialogShare(Context context) {
        super(context, R.style.shareDialogTheme);
    }

    public DialogShare(Context context, int theme, String msgText, final Object imgUri) {
        this(context);
        this.msgText = msgText;
        this.mImgPath = imgUri;
    }

    /**
     * 生成分享dialog
     * @param context
     * @param msgText msg内容
     * @param imgUri　图片uri 或者Bitmap(用来生成pdf)
     */
    public DialogShare(Context context, String msgText, final Object imgUri) {
        super(context, R.style.shareDialogTheme);
        this.msgText = msgText;
        this.mImgPath = imgUri;
    }

    void init(Context context) {
        DisplayMetrics dm;
        dm = context.getResources().getDisplayMetrics();
        this.mDensity = dm.density;
        this.mListData = new ArrayList<>();
        this.mListData.add(new ShareItem(context.getString(R.string.Share_Bluetooth), R.drawable.btn_share_bluetooth_selector,
                "", "com.android.bluetooth"));
        this.mListData.add(new ShareItem(context.getString(R.string.Share_Gmail), R.drawable.btn_share_gmail_selector,
            "", "com.google.android.gm"));
        this.mListData.add(new ShareItem(context.getString(R.string.Share_Wechat), R.drawable.btn_share_wechat_selector,
            "com.tencent.mm.ui.tools.ShareImgUI", "com.tencent.mm"));
        this.mListData.add(new ShareItem(context.getString(R.string.Share_QQ), R.drawable.btn_share_qq_selector,
                "com.tencent.mobileqq.activity.JumpActivity","com.tencent.mobileqq"));
        this.mListData.add(new ShareItem(context.getString(R.string.Share_Evernote), R.drawable.btn_share_evernote_selector,
                "", "com.evernote"));
        this.mListData.add(new ShareItem(context.getString(R.string.Share_Drive), R.drawable.btn_share_drive_selector,
                "com.google.android.apps.docs.app.NewMainProxyActivity", "com.google.android.apps.docs"));
        this.mListData.add(new ShareItem(context.getString(R.string.Share_Gallery), R.drawable.btn_share_gallery_selector,
                "","com.android.gallery3d"));
        this.mListData.add(new ShareItem(context.getString(R.string.Share_PDF), R.drawable.btn_share_pdf_selector,
                "", "com.hanvon.virtualpage"));
//        this.mListData.add(new ShareItem("其他", R.drawable.logo_other,
//                "",""));

        this.mLayout = new LinearLayout(context);
        this.mLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.leftMargin = ((int) (10.0F * this.mDensity));
//        params.rightMargin = ((int) (10.0F * this.mDensity));
        this.mLayout.setLayoutParams(params);
        this.mLayout.setBackgroundColor(Color.parseColor("#D9DEDF"));

        TextView textView = new TextView(context);
        textView.setText(context.getString(R.string.Share));
        textView.setTextSize(20.0F);
        int padding = (int) (10.0F * mDensity);
        textView.setPadding(padding,padding,padding,padding);
        ViewGroup.LayoutParams params1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params1);
        this.mLayout.addView(textView);

        this.mGridView = new GridView(context);
        this.mGridView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.mGridView.setGravity(17);
        this.mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
//        this.mGridView.setHorizontalSpacing((int) (10.0F * this.mDensity));
//        this.mGridView.setVerticalSpacing((int) (10.0F * this.mDensity));
//        this.mGridView.setStretchMode(GridView.STRETCH_SPACING);
//        this.mGridView.setColumnWidth((int) (80.0F * this.mDensity));
        this.mGridView.setHorizontalScrollBarEnabled(false);
        this.mGridView.setVerticalScrollBarEnabled(false);
        this.mLayout.addView(this.mGridView);

        View view = new View(context);
        view.setPadding(padding, padding, padding, padding);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50));
        this.mLayout.addView(view);


        if (isAvailable(getContext(), "com.android.gallery3d"))
        {
            isGallery = true;
        }
        else
        {
            isGallery = false;
        }

    }

    public List<ComponentName> queryPackage() {
        List<ComponentName> cns = new ArrayList<ComponentName>();
        Intent i = new Intent("android.intent.action.SEND");
        i.setType("image/*");
        List<ResolveInfo> resolveInfo = getContext().getPackageManager().queryIntentActivities(i, 0);
        for (ResolveInfo info : resolveInfo) {
            ActivityInfo ac = info.activityInfo;
            ComponentName cn = new ComponentName(ac.packageName, ac.name);
            cns.add(cn);
        }
        return cns;
    }

    public boolean isAvailable(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (((PackageInfo) pinfo.get(i)).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getContext();
        init(context);
        setContentView(this.mLayout);

        getWindow().setGravity(80);

        if (getScreenOrientation() == 0) {
            this.mScreenOrientation = 0;
            this.mGridView.setNumColumns(4);
        } else {
            this.mScreenOrientation = 1;
            this.mGridView.setNumColumns(4);
        }
        this.mGridView.setAdapter(new MyAdapter());
        this.mGridView.setOnItemClickListener(this);

        this.mHandler.postDelayed(this.work, 1000L);

        setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                mHandler.removeCallbacks(work);
            }
        });
    }

    public void show() {
        super.show();
    }

    public int getScreenOrientation() {
        int landscape = 0;
        int portrait = 1;
        Point pt = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getSize(pt);
        int width = pt.x;
        int height = pt.y;
        return width > height ? portrait : landscape;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ShareItem share = (ShareItem) this.mListData.get(position);
        shareMsg(getContext(), "Share", this.msgText, this.mImgPath, share);
    }
    private String createPdf(Object bitmap) {
        File pdfFolder = new File(Environment.getExternalStorageDirectory()+ "/hanvon/pdffiles");
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }
        String relativePath = "/hanvon/pdffiles/"+ TimeHelper.getCurrentDateTime() + ".pdf";
        String myFilePath = Environment.getExternalStorageDirectory() + relativePath;
        File myFile = new File(myFilePath);
        try {
            OutputStream output = new FileOutputStream(myFile);
            //start modified by cuijc3
            //Rectangle rectPageSize = new Rectangle(PageSize.B5);// A4纸张
            Rectangle rectPageSize = new Rectangle(PageSize.B2);//TUDO need to use appropriate one
            //end by cuijc3
            com.itextpdf.text.Document document = new  com.itextpdf.text.Document(rectPageSize, 40, 40, 40, 40);
            PdfWriter.getInstance(document, output);
            document.open();
            if (bitmap instanceof Bitmap && bitmap != null){
                Image image = Image.getInstance(BitmapUtil.Bitmap2Bytes((Bitmap)bitmap));
                document.add(image);
            }else{
                Log.d(TAG, "createPdf: there is something wrong with the imageData");
            }
            //document.add(new Paragraph("this is a pdf "));
            document.close();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return relativePath;
    }



    private String createPNG(String filename) {
        File pngFolder = new File(Environment.getExternalStorageDirectory()+ "/hanvon/pngfiles");
        if (!pngFolder.exists()) {
            pngFolder.mkdirs();
        }
        String relativePath = "/hanvon/pngfiles/"+ TimeHelper.getCurrentDateTime() + ".png";
        String myFilePath = Environment.getExternalStorageDirectory() + relativePath;
        try {
            FileManager.copyFile(filename, myFilePath);
        }
        catch (IOException e)
        {
            System.out.println("copy fle error");
        }
        return relativePath;
    }



    public void ShareSaveAsPDF(Context context, String msgText, final Object imgUri)
    {
        ShareItem share = new ShareItem(context.getString(R.string.Share_PDF), R.drawable.btn_share_pdf_selector,
                "", "com.hanvon.virtualpage");

        new saveAsPdfTask().execute(share, BitmapFactory.decodeFile(imgUri.toString()));
    }




    /**
     *
     * @param context
     * @param msgTitle  消息头
     * @param msgText
     * @param imgPath 要分享的bitmap(用于生成pdf) 或图片路径uri
     * @param share
     */
    private void shareMsg(Context context, String msgTitle, String msgText,
                          Object imgPath, ShareItem share) {

        if (!share.packageName.equals("com.android.gallery3d") && !share.packageName.equals("com.google.android.apps.photos")) {
            if (!share.packageName.isEmpty() && !isAvailable(getContext(), share.packageName)) {
                Toast.makeText(getContext(), "您没有安装该客户端" + share.title, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (share.packageName.equals("com.android.gallery3d") || share.packageName.equals("com.google.android.apps.photos")){
            //保存为png图片
            //new saveAsPdfTask().execute(share,"/hanvon/fff.png");
            if (imgPath instanceof String)
            {
                new saveAsPdfTask().execute(share, imgPath.toString());
            }
        }else if (share.packageName.equals("com.android.bluetooth")){
            //使用蓝牙分享图片
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.setPackage("com.android.bluetooth");
            //intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + "/sdcard/hanvon/fff.png"));
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imgPath.toString()));
            context.startActivity(intent);
            DialogShare.this.dismiss();
        }else if (share.packageName.equals("com.hanvon.virtualpage")) {
            //保存为pdf文件
            //new saveAsPdfTask().execute(share, BitmapFactory.decodeResource(context.getResources(),R.drawable.page));
            if (imgPath instanceof String)
            {
                new saveAsPdfTask().execute(share, BitmapFactory.decodeFile(imgPath.toString()));
            }
        }else{
            if (imgPath instanceof String){
                Intent intent = new Intent("android.intent.action.SEND");
                if ((imgPath == null) || (imgPath.equals(""))) {
                    intent.setType("text/plain");
                } else {

                    try {
                        FileManager.copyFile(imgPath.toString(), "/sdcard/hanvon/share.png");
                    }
                    catch (IOException e)
                    {
                        System.out.println("copy fle error");
                    }

                    File f = new File("/sdcard/hanvon/share.png");
                    if ((f != null) && (f.exists()) && (f.isFile())) {
                        intent.setType("image/png");
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                    }
                }

                intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
                intent.putExtra(Intent.EXTRA_TEXT, msgText);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(!share.packageName.isEmpty()) {
                    if (!share.activityName.isEmpty()){
                        intent.setComponent(new ComponentName(share.packageName,share.activityName));
                    }else{
                        intent.setPackage(share.packageName);
                    }
                    context.startActivity(intent);
                }
                else {
                    context.startActivity(Intent.createChooser(intent, msgTitle));
                }
            DialogShare.this.dismiss();
            }
        }
    }

    private File getFileCache() {
        File cache = null;

        if (Environment.getExternalStorageState().equals("mounted"))
            cache = new File(Environment.getExternalStorageDirectory() + "/." + getContext().getPackageName());
        else {
            cache = new File(getContext().getCacheDir().getAbsolutePath() + "/." + getContext().getPackageName());
        }
        if ((cache != null) && (!cache.exists())) {
            cache.mkdirs();
        }
        return cache;
    }

    public String getImagePath(String imageUrl, File cache) throws Exception {
        String name = imageUrl.hashCode() + imageUrl.substring(imageUrl.lastIndexOf("."));
        File file = new File(cache, name);

        if (file.exists()) {
            return file.getAbsolutePath();
        }

        URL url = new URL(imageUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            is.close();
            fos.close();

            return file.getAbsolutePath();
        }

        return null;
    }

    private final class MyAdapter extends BaseAdapter {
        private static final int image_id = 256;
        private static final int tv_id = 512;

        public MyAdapter() {
        }

        public int getCount() {
            return mListData.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0L;
        }

        private View getItemView() {
            LinearLayout item = new LinearLayout(getContext());
            item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            item.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (10.0F * mDensity);
            item.setPadding(padding, padding, padding, padding);
            item.setGravity(Gravity.CENTER_HORIZONTAL);

            ImageView iv = new ImageView(getContext());
            item.addView(iv);
//            iv.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            iv.setLayoutParams(new LinearLayout.LayoutParams(103,103));
            iv.setId(image_id);

            TextView tv = new TextView(getContext());
            item.addView(tv);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = ((int) (5.0F * mDensity));
            tv.setLayoutParams(layoutParams);
            tv.setTextColor(Color.parseColor("#212121"));
            tv.setTextSize(16.0F);
            tv.setId(tv_id);

            return item;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getItemView();
            }
            ImageView iv = (ImageView) convertView.findViewById(image_id);
            TextView tv = (TextView) convertView.findViewById(tv_id);
            DialogShare.ShareItem item = mListData.get(position);
            iv.setImageResource(item.logo);
            tv.setText(item.title);
            return convertView;
        }
    }

    private class ShareItem {
        String title;
        int logo;
        String activityName;
        String packageName;

        public ShareItem(String title, int logo, String activityName, String packageName) {
            this.title = title;
            this.logo = logo;
            this.activityName = activityName;
            this.packageName = packageName;
        }
    }

    private class saveAsPdfTask extends AsyncTask {

        ShareItem share;
        Object imagePath;
        String pdfFilePath;
        String galleryFilePath;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            share = (ShareItem)params[0];
            imagePath = params[1];
            if (imagePath instanceof Bitmap){
                pdfFilePath = createPdf(params[1]);
            }else if (imagePath instanceof String){
                galleryFilePath = createPNG(imagePath.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.save_dialog_layout,null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(view);
            final AlertDialog saveDialog = builder.create();
            (view.findViewById(R.id.tv_view)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveDialog.dismiss();
                    DialogShare.this.dismiss();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addCategory("android.intent.category.DEFAULT");
                    if (imagePath instanceof Bitmap) {
                        i.setDataAndType(Uri.parse("file://" + "/sdcard/" + pdfFilePath), "application/pdf");
                    } else if (imagePath instanceof String) {

                        if (isGallery)
                        {
                            i.setPackage("com.android.gallery3d");
                        }
                        else
                        {
                            i.setPackage("com.google.android.apps.photos");
                        }

                        //i.setPackage(share.packageName);
                        //i.setDataAndType(Uri.parse("file://" + "/sdcard/" + imagePath), "image/*");
                        //i.setDataAndType(Uri.parse("file://" + "/sdcard/hanvon/fff.png"), "image/*");
                        i.setDataAndType(Uri.parse("file://" + "/sdcard/" + galleryFilePath), "image/*");
                    }

                    try
                    {
                        getContext().startActivity(i);
                    }
                    catch (android.content.ActivityNotFoundException ex)
                    {
                        Logger.i("Can't found activity");
                    }
                }
            });
            (view.findViewById(R.id.tv_ok)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveDialog.dismiss();
                    DialogShare.this.dismiss();
                }
            });
            saveDialog.show();
            super.onPostExecute(o);
        }
    }
}
