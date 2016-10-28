package com.hanvon.virtualpage.pageeditor.widget;


import com.hanvon.virtualpage.beans.Page;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by shihuijie on 16-3-20.
 */
public class PageSaveTask implements Runnable {

    private final static int MaxCacheSize = 5;
    private static boolean finish = false;
    private static PageSaveTask Instance;

    private BlockingQueue<Page> mPageQueue;
    private BlockingQueue<Page> mPageCacheQueue;
    private boolean mChangeNumber = false;



    private PageSaveTask(){
        mPageQueue = new LinkedBlockingQueue<Page>();
        mPageCacheQueue = new LinkedBlockingQueue<Page>();
    }

    public static PageSaveTask getInstance(){
        if(Instance == null) {
            synchronized (PageSaveTask.class) {
                if (Instance == null) {
                    Instance = new PageSaveTask();
                }

            }
        }
        return Instance;
    }

    public boolean offer(Page page, boolean changeNumber){
        mChangeNumber = changeNumber;
        return mPageQueue.offer(page);
    }

    public void finish(){
        finish = true;
    }

    @Override
    public void run() {
        Page page;
        while(true){

//            LogUtil.e("PageSaveTask=============>run");

            try {
                page = mPageQueue.take();
                if (mChangeNumber) {
                    page.saveOfNumberChange();
                } else {
                    page.save();
                }

                if (page.getOwner() != null) {
//                LogUtil.e("bmp", "PageSaveTask=============>保存Page到Document");
                    page.getOwner().save();
                }

                if (!mPageCacheQueue.contains(page)) {
                    mPageCacheQueue.offer(page);
                    clearRedundancy();
                }
            }
            catch (InterruptedException ex) {
//                LogUtil.e("PageSaveTask  mPageQueue.take() exception and contine");
                continue;
            } catch (Exception ex) {
                continue;
            }
            if(finish){
                finish = false;
                break;
            }
            Thread.yield();
        }
    }

    private void clearRedundancy(){
        while(mPageCacheQueue.size() > MaxCacheSize){
            Page page = mPageCacheQueue.remove();
            page.clearData();
        }
    }

    public boolean noTask(){
        if(mPageQueue.size() == 0)
            return true;
        else
            return false;
    }
}
