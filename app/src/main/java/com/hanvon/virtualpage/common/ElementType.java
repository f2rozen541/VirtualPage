package com.hanvon.virtualpage.common;

/**
 * -------------------------------
 * Description:
 * 对象元素：笔迹；文本；图片；橡皮
 * -------------------------------
 */
public enum ElementType {
    STROKE(1), TEXT(2), PICTURE(3), ERASER(4);

    private final int mValue;

    ElementType(int mValue) {
        this.mValue = mValue;
    }
}

