package com.hanvon.virtualpage.lib.beans;

/**
 * Created by wangkun20 on 2016/10/11.
 */

public class PageRotation {
    //纸张正方向
    public int startRotation;
    //纸张反方向（正方向的180度旋转）
    public int endRotation;
    //纸张是否旋转了180度
    public boolean reverseRotation;
    public PageRotation(){
        startRotation = -1;
        endRotation = -1;
        reverseRotation = false;
    }

    public PageRotation(int start, int end, boolean reverse){
        startRotation = start;
        endRotation = end;
        reverseRotation = reverse;
    }
}
