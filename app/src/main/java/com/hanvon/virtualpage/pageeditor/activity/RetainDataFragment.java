package com.hanvon.virtualpage.pageeditor.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.app.Fragment;

import com.hanvon.core.Stroke;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RetainDataFragment extends Fragment {

    private Boolean canPaste;
    private Rect rect;
    private LinkedList<Stroke> copyStrokeList;
    private List<Object> copyElementList;

    public RetainDataFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setCanPaste(Boolean canPaste){
        this.canPaste = canPaste;
    }

    public void setRect(Rect rect){
        this.rect = rect;
    }

    public void setCopyStrokeList(LinkedList<Stroke> copyStrokeList){
        this.copyStrokeList = copyStrokeList;
    }

    public void setCopyElementList(List<Object> copyElementList){
        this.copyElementList = copyElementList;
    }

    public Boolean getCanPaste(){
        return canPaste;
    }

    public Rect getRect(){
        return rect;
    }

    public LinkedList<Stroke> getCopyStrokeList(){
        return copyStrokeList;
    }

    public List<Object> getCopyElementList(){
        return copyElementList;
    }

}
