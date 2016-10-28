package com.hanvon.virtualpage.beans;

/**
 * @Description: 保存屏幕信息类，目前项目尺寸单一，所以暂时无用
 * @Author: TaoZhi
 * @Date: 2016/3/31
 * @E_mail: taozhi@hanwang.com.cn
 */
public class Resolution {
    public int Width;
    public int Height;
    public Resolution(){
        Width = 0;
        Height = 0;
    }
    public Resolution(int width, int height){
        Width = width;
        Height = height;
    }

    @Override
    public Resolution clone(){
        return new Resolution(Width,Height);
    }

    public boolean compare(Resolution resolution){
        return Width == resolution.Width && Height == resolution.Height;
    }
}
