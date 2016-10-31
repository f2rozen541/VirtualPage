package com.hanvon.virtualpage.pageeditor.tools.stroke;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.common.EditorState;
import com.hanvon.virtualpage.common.ElementDirector;
import com.hanvon.virtualpage.common.ElementLayout;
import com.hanvon.virtualpage.common.ElementType;
import com.hanvon.virtualpage.common.NoteEditBuilder;
import com.hanvon.virtualpage.common.NotePictureBuilder;
import com.hanvon.virtualpage.pageeditor.activity.PageEditorActivity;
import com.hanvon.virtualpage.pageeditor.tools.picture.NotePicture;
import com.hanvon.virtualpage.pageeditor.tools.text.NoteEditText;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * -------------------------------
 * Description:
 * 构建画布，继承AbsoluteLayout。
 * 提供方法：对画布上的对象元素进行操作及touch时间的处理。
 *	添加文本；添加图片；获取对象；擦除对象；撤销恢复对象；复制删除粘贴对象
 * -------------------------------
 */
@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class CanvasLayout extends AbsoluteLayout implements Observer {

    private String TAG = "CanvasLayout";
    private ElementType mCurrentElementType = ElementType.STROKE;
    private RemoveElementsListener mRemoveListener = null;
    List<Object> childViews;
    private int touchStartX;
    private int touchStartY;
    private int touchEndX;
    private int touchEndY;
    private Rect currentRect = new Rect();
    private View currentView = null;
    private int mTmpX = 0;
    private int mTmpY = 0;

    private int mPictureStartX = 100;
    private int mPictureStartY = 100;

    public interface RemoveElementsListener {
        void onRemoveElementByPoint(int x, int y);
    }

    public void setRemoveListener(RemoveElementsListener removeListener) {
        this.mRemoveListener = removeListener;
    }

    public CanvasLayout(Context context) {
        super(context);
        init();
        setWillNotDraw(false);
    }

    public CanvasLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        setWillNotDraw(false);
    }

    public CanvasLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        setWillNotDraw(false);
    }

    public void init() {
        childViews = new ArrayList<>();
    }

    public void removeAllElements() {
        int childCount = getChildCount();
        List<Object> elementList = new ArrayList<Object>();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof ElementLayout) {
                elementList.add((ElementLayout) child);
            }
        }
        for (int i = 0; i < elementList.size(); i++) {
            removeView((View) elementList.get(i));
        }
    }

    public void deleteEmptyEditText(){
        List<NoteEditText> editTextList = new ArrayList<NoteEditText>();
        for(int i = 0; i < getChildCount(); i++){
            View child = getChildAt(i);
            if(child instanceof NoteEditText && ((NoteEditText) child).getContentViewText()!= null && ((NoteEditText) child).getContentViewText().equals("")){
                editTextList.add((NoteEditText)child);
            }
        }
        for(int i = 0; i < editTextList.size(); i++){
            removeView(editTextList.get(i));
        }
    }

    @Override
    public void removeView(View child) {
        childViews.remove(child);
        if (child instanceof ElementLayout) {
            ((ElementLayout) child).onRemovedFromParent();
        }
        super.removeView(child);
    }

    public List<ElementLayout> removeElementsByPoint(int x, int y) {
        List<ElementLayout> removedList = new ArrayList<ElementLayout>();

        int childCount = getChildCount();
        List<ElementLayout> elementList = new ArrayList<ElementLayout>();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof ElementLayout) {
                child.clearFocus();
                Rect outRect = new Rect();
                child.getHitRect(outRect);
                if (outRect.contains(x, y))
                    elementList.add((ElementLayout) child);
            }
        }

        removedList = elementList;

        for (int i = 0; i < elementList.size(); i++) {
            NoteParams.getCurrentPenNoteParams().deleteObserver(
                    elementList.get(i));
            removeView(elementList.get(i));
        }

        return removedList;
    }

    private int getPictureNum(){
        int num = 0;
        for(int i = 0; i < getChildCount(); i++){
            View child = getChildAt(i);
            if (child instanceof NotePicture) {
                num++;
            }
        }
        return num;
    }

    private int mX, mY;


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (!currentRect.contains(x, y)) {
            Rect rect = new Rect();
            Iterator iterator = childViews.iterator();
            while (iterator.hasNext()) {
                currentView = ((View) iterator.next());
                currentView.getHitRect(rect);
                if (rect.contains(x, y)) {
                    currentRect = rect;
                }
            }
        }
        return super.onInterceptHoverEvent(event);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER || mCurrentElementType == ElementType.ERASER) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(EditorState.getInstance().getCanvasRate() != 1
                            || EditorState.getInstance().getCanvasTranslateX() != 0
                            || EditorState.getInstance().getCanvasTranslateY() != 0){
                        return true;
                    }

                    PageEditorActivity.mSelectedStrokeList.clear();
                    PageEditorActivity.mSelectedElementList.clear();

                    if (mRemoveListener != null) {
                        mRemoveListener.onRemoveElementByPoint(x, y);
                    }

                    PageEditorActivity.mSelectedElementList.addAll(removeElementsByPoint(x, y));
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(EditorState.getInstance().getCanvasRate() != 1
                            || EditorState.getInstance().getCanvasTranslateX() != 0
                            || EditorState.getInstance().getCanvasTranslateY() != 0){
                        return true;
                    }

                    if (mRemoveListener != null) {
                        mRemoveListener.onRemoveElementByPoint(x, y);
                    }
                    PageEditorActivity.mSelectedElementList.addAll(removeElementsByPoint(x, y));
                    break;
                case MotionEvent.ACTION_UP:
                    if(EditorState.getInstance().getCanvasRate() != 1
                            || EditorState.getInstance().getCanvasTranslateX() != 0
                            || EditorState.getInstance().getCanvasTranslateY() != 0){
                        return true;
                    }

                    if (mRemoveListener != null) {
                        mRemoveListener.onRemoveElementByPoint(-100, -100);
                    }

                    PageEditorActivity.mSelectedElementList.addAll(removeElementsByPoint(-100, -100));

//                    PageEditorActivity.addRecord(PageEditorActivity.ACTION_DELETE);//undo redo
                    if (PageEditorActivity.getInstance() != null) {
                        PageEditorActivity.getInstance().addRecord(PageEditorActivity.ACTION_DELETE);//undo redo
                        PageEditorActivity.mUndoSelectedRecords.add(PageEditorActivity.listAddList(PageEditorActivity.mSelectedElementList, PageEditorActivity.mSelectedStrokeList));
                        PageEditorActivity.mRedoSelectedRecords.clear();
                    }

                    break;
            }
        } else if (mCurrentElementType == ElementType.TEXT) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = x;
                    touchStartY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchEndX = x;
                    touchEndY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    View view = findFocus();
                    if(view != null && (view instanceof EditText || view instanceof ImageView)){
                        view.clearFocus();
                        return true;
                    }

                    AbsoluteLayout.LayoutParams orgLP = new AbsoluteLayout.LayoutParams(
                            touchEndX - touchStartX, touchEndY - touchStartY, touchStartX, touchStartY);

//                    NoteEditBuilder builder = new NoteEditBuilder(BaseApplication.getContext(), 30);
                    NoteEditBuilder builder = new NoteEditBuilder(getContext(), 30);
                    new ElementDirector().Construct(builder, orgLP, null);
                    ElementLayout elementView = builder.getResult();

                    Log.e("wangkun", "onTouchEvent: rotation" + getRotation());
                    elementView.setRotation(-getRotation());

                    addView(elementView);

                    elementView.getContentView().requestFocus();

                    NoteParams.getCurrentPenNoteParams()
                            .addObserver(elementView);
                    childViews.add(elementView);
                    elementView.mUndoLayoutParamsList.add(new Rect(orgLP.x, orgLP.y, orgLP.width, orgLP.height));
//                    PageEditorActivity.addRecord(elementView);
                    if (PageEditorActivity.getInstance() != null) {
                        PageEditorActivity.getInstance().addRecord(elementView);
                    }
                    break;
            }
            return true;
        }
        return true;
    }

    public void addNotePicture(String contentParam, int width, int height) {
        if(getPictureNum() >= 8) {//the max value of picture is 8
            Toast.makeText(BaseApplication.getContext(), R.string.LimitPictureAmount, Toast.LENGTH_LONG).show();
            return;
        }

        AbsoluteLayout.LayoutParams tvBg = new AbsoluteLayout.LayoutParams(width, height, mPictureStartX, mPictureStartY);
        NotePictureBuilder builder = new NotePictureBuilder(BaseApplication.getContext(), width, height, 30);
        new ElementDirector().Construct(builder, tvBg, contentParam);
        ElementLayout elementView = builder.getResult();
        addView(elementView);
        elementView.getContentView().requestFocus();
        NoteParams.getCurrentPenNoteParams().addObserver(elementView);
        childViews.add(elementView);
        elementView.mUndoLayoutParamsList.add(new Rect(tvBg.x, tvBg.y, tvBg.x + tvBg.width, tvBg.y + tvBg.height));
//        PageEditorActivity.addRecord(elementView);
        if (PageEditorActivity.getInstance() != null) {
            PageEditorActivity.getInstance().addRecord(elementView);
        }
        setPictureStartValue(width);
    }

    private void setPictureStartValue(int width){
        if(PageEditorActivity.canvasRotation == 0 || PageEditorActivity.canvasRotation == 180){
            mPictureStartX += 200;
            if(mPictureStartX > PageEditorActivity.screenWidth - width){
                mPictureStartX = 200;
                mPictureStartY += 200;
            }
        } else if(PageEditorActivity.canvasRotation == 90 || PageEditorActivity.canvasRotation == 270){
            mPictureStartX += 200;
            if(mPictureStartX > PageEditorActivity.screenHeight - width){
                mPictureStartX = 200;
                mPictureStartY += 200;
            }
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof ElementType) {
            mCurrentElementType = (ElementType) data;
        }
    }

    public void clearCanvas() {
        removeAllElements();
        mPictureStartX = 100;
        mPictureStartY = 100;
    }

    public void setElementList(List<Object> elements) {
        removeAllElements();
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i) instanceof ElementLayout) {
                ElementLayout elementLayout = (ElementLayout)elements.get(i);
                if(elementLayout.getParent() != null){
                    ViewGroup view = (ViewGroup)elementLayout.getParent();
                    view.removeView(elementLayout);
                }
                this.addView(elementLayout);
                this.childViews.add(elementLayout);
                NoteParams.getCurrentPenNoteParams().addObserver(elementLayout);
            }
        }
    }

    public List<Object> getElementList() {
        List<Object> result = new ArrayList<Object>();
        View item;
        for (int index = 0, count = this.getChildCount(); index < count; index++) {
            item = this.getChildAt(index);
            if (item instanceof ElementLayout)
                result.add(item);
        }
        return result;
    }

    public void undo(ElementLayout undo) {
        if (undo.mUndoLayoutParamsList.size() <= 0) {
            return;
        }

        undo.mRedoLayoutParamsList.add(undo.mUndoLayoutParamsList.getLast());
        undo.mUndoLayoutParamsList.removeLast();

        if (undo.mUndoLayoutParamsList.size() == 0) {
            removeView(undo);
            //childViews.remove(undo);
        } else {
            Rect rect = undo.mUndoLayoutParamsList.getLast();
            AbsoluteLayout.LayoutParams param = new AbsoluteLayout.LayoutParams(
                    rect.width(), rect.height(), rect.left, rect.top);
            undo.setLayoutParams(param);
            invalidate();
        }
    }

    public void undo(Object action, ElementLayout undo) {
        if (action.equals(2)) {//cut
            if(!childViews.contains(undo)){
                addView(undo);
                NoteParams.getCurrentPenNoteParams().addObserver(undo);
                childViews.add(undo);
            }
        }
        if (action.equals(3)) {//delete
            if(!childViews.contains(undo)){
                addView(undo);
                NoteParams.getCurrentPenNoteParams().addObserver(undo);
                childViews.add(undo);
            }
        }
        if (action.equals(4)) {//paste
            removeView(undo);
            //childViews.remove(undo);
        }
    }

    public void redo(ElementLayout redo) {
        if (redo.mRedoLayoutParamsList.size() <= 0) {
            return;
        }

        redo.mUndoLayoutParamsList.add(redo.mRedoLayoutParamsList.getLast());
        redo.mRedoLayoutParamsList.removeLast();

        if (redo.mUndoLayoutParamsList.size() == 1) {
            if(!childViews.contains(redo)){
                addView(redo);
                NoteParams.getCurrentPenNoteParams().addObserver(redo);
                childViews.add(redo);
            }
        } else {
            Rect rect = redo.mUndoLayoutParamsList.getLast();
            AbsoluteLayout.LayoutParams param = new AbsoluteLayout.LayoutParams(
                    rect.width(), rect.height(), rect.left, rect.top);
            redo.setLayoutParams(param);
            invalidate();
        }
    }

    public void redo(Object action, ElementLayout redo) {
        if (action.equals(2)) {//cut
            removeView(redo);
            //childViews.remove(redo);
        }
        if (action.equals(3)) {//delete
            removeView(redo);
            //childViews.remove(redo);
        }
        if (action.equals(4)) {//paste
            if(!childViews.contains(redo)){
                addView(redo);
                NoteParams.getCurrentPenNoteParams().addObserver(redo);
                childViews.add(redo);
            }
        }
    }

    public List<Object> getSelectedElementList(Rect selectRect) {
        List<Object> elementList = new ArrayList<Object>();

        if (selectRect != null) {
            for (int i = 0; i < getElementList().size(); i++) {
                ElementLayout elementLayout = (ElementLayout) getElementList().get(i);
                AbsoluteLayout.LayoutParams param = (AbsoluteLayout.LayoutParams) elementLayout.getLayoutParams();
                if (param.x >= selectRect.left
                        && param.y >= selectRect.top
                        && (param.x + param.width) <= selectRect.right
                        && (param.y + param.height) <= selectRect.bottom) {
                    elementList.add(elementLayout);
                }
            }
        }

        return elementList;
    }

    public void removeSelectedElementList(List<Object> list) {
        List<Object> elementList = list;
        if (elementList != null) {
            for (int i = 0; i < elementList.size(); i++) {
                removeView((View) elementList.get(i));
                //childViews.remove(elementList.get(i));
            }
        }
    }

    public List<Object> pasteSelectedElementList(Point pasteStartPoint, Rect selectedRect, List<Object> srcElementList) {
        List<Object> resultElementList = new ArrayList<>();

        if (pasteStartPoint == null || selectedRect == null || srcElementList == null) {
            return resultElementList;
        }

        if(pasteStartPoint.x + selectedRect.width() > this.getWidth()){
            pasteStartPoint.x = this.getWidth() - selectedRect.width();
        }

        if(pasteStartPoint.y + selectedRect.height() > this.getHeight()){
            pasteStartPoint.y = this.getHeight() - selectedRect.height();
        }

        int differenceX = pasteStartPoint.x - selectedRect.left;
        int differenceY = pasteStartPoint.y - selectedRect.top;

        for (int i = 0; i < srcElementList.size(); i++) {
            ElementLayout srcElementLayout = (ElementLayout) srcElementList.get(i);
            AbsoluteLayout.LayoutParams srcParam = (AbsoluteLayout.LayoutParams) srcElementLayout.getLayoutParams();

            if (srcElementLayout instanceof NoteEditText) {
//                NoteEditBuilder editBuilder = new NoteEditBuilder(BaseApplication.getContext(), 30);
                NoteEditBuilder editBuilder = new NoteEditBuilder(getContext(), 30);
                AbsoluteLayout.LayoutParams param = new AbsoluteLayout.LayoutParams(srcParam.width,
                        srcParam.height,
                        srcParam.x + differenceX,
                        srcParam.y + differenceY);
                new ElementDirector().Construct(editBuilder, param, null);
                NoteEditText editText = (NoteEditText) editBuilder.getResult();
                editText.setLayoutParams(param);
                editText.setContentViewText(((NoteEditText) srcElementLayout).getContentViewText());
                editText.setContentViewTextColor(((NoteEditText) srcElementLayout).getContentViewTextColor());
                editText.setContentViewTextSize(((NoteEditText) srcElementLayout).getContentViewTextSize());
                editText.setContentViewTextTypeface(((NoteEditText) srcElementLayout).getContentViewTextTypeface());
                editText.setContentViewTextUnderLine(((NoteEditText) srcElementLayout).getContentViewTextUnderLine());
                addView(editText);
                editText.getContentView().requestFocus();
                NoteParams.getCurrentPenNoteParams().addObserver(editText);
                childViews.add(editText);
                editText.mUndoLayoutParamsList.add(new Rect(param.x, param.y, param.x + param.width, param.y + param.height));
                if (PageEditorActivity.getInstance() != null) {
                    PageEditorActivity.getInstance().addRecord(editText);
                }
                invalidate();

                resultElementList.add(editText);
            }

            if (srcElementLayout instanceof NotePicture) {
                NotePictureBuilder pictureBuilder = new NotePictureBuilder(BaseApplication.getContext(), srcParam.width,
                        srcParam.height, 30);
                AbsoluteLayout.LayoutParams param = new AbsoluteLayout.LayoutParams(srcParam.width,
                        srcParam.height,
                        srcParam.x + differenceX,
                        srcParam.y + differenceY);
                new ElementDirector().Construct(pictureBuilder, param, srcElementLayout.getTag());
                NotePicture picture = (NotePicture) pictureBuilder.getResult();
                picture.setLayoutParams(param);
                addView(picture);
                picture.getContentView().requestFocus();
                NoteParams.getCurrentPenNoteParams().addObserver(picture);
                childViews.add(picture);
                picture.mUndoLayoutParamsList.add(new Rect(param.x, param.y, param.x + param.width, param.y + param.height));
                if (PageEditorActivity.getInstance() != null) {
                    PageEditorActivity.getInstance().addRecord(picture);
                }
                invalidate();

                resultElementList.add(picture);
            }
        }
        return resultElementList;
    }
}
