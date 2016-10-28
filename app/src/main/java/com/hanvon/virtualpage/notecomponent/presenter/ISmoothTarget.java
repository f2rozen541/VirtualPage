package com.hanvon.virtualpage.notecomponent.presenter;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/7/8
 * E_mail:  taozhi@hanwang.com.cn
 */
public interface ISmoothTarget {

    float getPercent();

    void setPercent(float percent);

    /**
     * recommend call {@link SmoothHandler#loopSmooth(float)}
     *
     * @param percent the aim percent
     */
    void setSmoothPercent(float percent);

    /**
     * If the provider percent(the aim percent) more than {@link SmoothHandler#minInternalPercent}, it will
     * be split to the several {@link SmoothHandler#smoothInternalPercent}.
     *
     * @param percent        The aim percent.
     * @param durationMillis Temporary duration for {@code percent}. If lesson than 0, it will be
     *                       ignored.
     */
    void setSmoothPercent(float percent, long durationMillis);
}