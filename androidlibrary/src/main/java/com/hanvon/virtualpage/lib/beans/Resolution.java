package com.hanvon.virtualpage.lib.beans;

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
