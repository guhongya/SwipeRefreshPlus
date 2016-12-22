/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gu.swiperefresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.gu.swiperefresh.Utils.Size;

/**
 * Created by Guhy on 2016/12/7.
 */

public class LoadViewController {
    private Context mContext;

    private View parent;

    private CircleImageView mCircleImageView;

    private ProgressDrawable mProgress;

    // Max amount of circle that can be filled by progress during swipe gesture,
    // where 1.0 is a full circle
    private static final float MAX_PROGRESS_ANGLE = .8f;
    private static final int MAX_ALPHA = 255;
    //默认circleimage大小
    static final int CIRCLE_DIAMETER = 40;
    // Default background for the progress spinner
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    private int mDefaultProgressColor;
    private int currentHeight;

    //loadview 大小
    private int mCircleDiameter;

    final DisplayMetrics metrics;

    private boolean isLoading;

    private SwipeRefreshPlush.OnScrollListener mListener;

    private int mMargin =5;
    private int mMaxHeigth;

    private int mViewWidth,mViewHeight;

    private boolean isDefault=true;

    private View defaultView;

    public LoadViewController(Context context, View parent) {
        this.mContext = context;
        this.parent = parent;
        TypedArray typedArray=context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
        mDefaultProgressColor =typedArray.getColor(0,CIRCLE_BG_LIGHT);
        metrics = mContext.getResources().getDisplayMetrics();
        mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
        mMargin =(int)(mMargin *metrics.density);
        mMaxHeigth=mMargin*2+mCircleDiameter;
        mViewHeight=mCircleDiameter;
        mViewWidth=mCircleDiameter;
        typedArray.recycle();
    }

    protected View create() {
        mCircleImageView = new CircleImageView(mContext, CIRCLE_BG_LIGHT);
       // mCircleImageView.set(mMargin,mMargin,mMargin,mMargin);
        mProgress = new ProgressDrawable(mContext, parent);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        //  mLoadProgress.setAlpha(MAX_ALPHA);
        mProgress.setRotation(MAX_PROGRESS_ANGLE);
        mProgress.setColorSchemeColors(new int[]{mDefaultProgressColor});
        mCircleImageView.setImageDrawable(mProgress);
        //mCircleImageView.setVisibility(View.GONE);
        ViewGroup.MarginLayoutParams marginLayoutParams= new ViewGroup.MarginLayoutParams(mCircleDiameter,mCircleDiameter);
        marginLayoutParams.setMargins(0,mMargin,0,mMargin);
        mCircleImageView.setLayoutParams(marginLayoutParams);
        defaultView=mCircleImageView;
        return mCircleImageView;
    }

    protected void setScrollListener(SwipeRefreshPlush.OnScrollListener onScrollListener) {
        this.mListener = onScrollListener;
    }

    protected Size getLoadViewSize() {
        return new Size(mViewWidth,mViewHeight);
    }

    protected int getCurrentHeight() {
        return currentHeight;
    }
   protected void clearState(){
       mProgress.stop();
       isLoading=false;
   }

    protected void changeDefaultView(View loadView){
        this.defaultView=loadView;
        this.isDefault=false;
    }
    protected View getDefaultView(){
        return defaultView;
    }
    protected void move(int distance) {
        currentHeight+=distance;
       if(currentHeight>mMaxHeigth)
            currentHeight=mMaxHeigth;
        else if(currentHeight<0)
            currentHeight=0;
    }

    protected int getMaxHeight(){
        return mMaxHeigth;
    }

    protected void beginLoading() {
        if(isDefault) {
            mProgress.setAlpha(MAX_ALPHA);
            mProgress.start();
        }
        if(!isLoading) {
            isLoading = true;
            mListener.onLoadMore();
        }
    }

    protected void reset() {
        if(mProgress.isRunning())
            mProgress.stop();
        currentHeight = 0;
    }
    protected void stopLoad(){
        isLoading = false;
        reset();
    }

    protected boolean isLoading() {
        return isLoading;
    }

    protected void setProgressColors(@ColorInt int... colors) {
        mProgress.setColorSchemeColors(colors);
    }

}
