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
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

/**
 * Created by Guhy on 2016/12/7.
 * 加载更多和没有更多时的view 控制类
 * 加载更多提示 VIEW 使用SwipeRefreshLayout 中的ProgressDrawable 默认颜色为app的前景色
 * 没有更多提示没有默认的view样式，需要使用者手动设置
 */

public class LoadViewController implements ILoadViewController {
    //默认circleimage大小
    static final int CIRCLE_DIAMETER = 40;
    // Max amount of circle that can be filled by progress during swipe gesture,
    // where 1.0 is a full circle
    private static final float MAX_PROGRESS_ANGLE = .8f;
    private static final int MAX_ALPHA = 255;
    // Default background for the progress spinner
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    final DisplayMetrics metrics;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private Context mContext;
    private View mParent;
    private CircleImageView mCircleImageView;
    private ProgressDrawable mProgress;
    private int mDefaultProgressColor;
    private int mCurrentHeight;

    private boolean isLoading;
    //loadview 大小
    private int mCircleDiameter;
    private int mMargin = 5;
    private int mMaxHeigth;
    //加载更多动画
    private final Animation mAnimationShowLoadMore = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int offset = (int) ((getDefaultHeight() - getCurrentHeight()) * interpolatedTime);
            mParent.scrollBy(0, move(offset));
        }
    };
    private SwipeRefreshPlus.OnRefreshListener mListener;
    //动画是否在加载
    private volatile boolean isLoadAnimation;
    //是否显示没有更多view
    private boolean mShowNoMore = false;
    //loadmore动画结束，调用回调函数
    private Animation.AnimationListener mLoadMoreListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            isLoadAnimation = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!mShowNoMore) beginLoading();
            isLoadAnimation = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    public LoadViewController(Context context, View parent) {
        this.mContext = context;
        this.mParent = parent;
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
        try {
            mDefaultProgressColor = typedArray.getColor(0, CIRCLE_BG_LIGHT);
        } catch (Exception e) {
            mDefaultProgressColor = CIRCLE_BG_LIGHT;
        }
        metrics = mContext.getResources().getDisplayMetrics();
        mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
        mMargin = (int) (mMargin * metrics.density);
        mMaxHeigth = mMargin * 2 + mCircleDiameter;
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        typedArray.recycle();
    }

    @Override
    public View create() {
        mCircleImageView = new CircleImageView(mContext, CIRCLE_BG_LIGHT);
        mProgress = new ProgressDrawable(mContext, mParent);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mProgress.setRotation(MAX_PROGRESS_ANGLE);
        mProgress.setColorSchemeColors(new int[]{mDefaultProgressColor});
        mCircleImageView.setImageDrawable(mProgress);
        ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(mCircleDiameter, mCircleDiameter);
        marginLayoutParams.setMargins(0, mMargin, 0, mMargin);
        mCircleImageView.setLayoutParams(marginLayoutParams);
        return mCircleImageView;
    }

    @Override
    public void setRefreshListener(SwipeRefreshPlus.OnRefreshListener onRefreshListener) {
        this.mListener = onRefreshListener;
    }

    @Override
    public int getCurrentHeight() {
        return mCurrentHeight;
    }

    @Override
    public View getDefaultView() {
        return mCircleImageView;
    }

    @Override
    public void setLoadMore(boolean loading) {
        isLoading = loading;
        if (loading) {
            animateShowLoadMore(mLoadMoreListener);
        } else {
            animateHideLoadMore(null);
        }
    }

    @Override
    public boolean isLoading() {
        return isLoading;
    }

    /**
     * 实际移动距离
     *
     * @param scrollDistance
     * @return
     */
    @Override
    public int move(int scrollDistance) {
        mCurrentHeight += scrollDistance;
        if (mCurrentHeight > mMaxHeigth) {
            int result = scrollDistance - (mCurrentHeight - mMaxHeigth);
            mCurrentHeight = mMaxHeigth;
            return result;
        } else if (mCurrentHeight < 0) {
            int result = scrollDistance - mCurrentHeight;
            mCurrentHeight = 0;
            return result;
        }
        return scrollDistance;
    }

    @Override
    public int getDefaultHeight() {
        return mMaxHeigth;
    }

    protected void beginLoading() {
        mProgress.setAlpha(MAX_ALPHA);
        mProgress.start();
        if (!isLoading && mListener != null) {
            isLoading = true;
            mListener.onPullUpToRefresh();
        }
    }

    @Override
    public void reset() {
        isLoading = false;
        if (mProgress.isRunning())
            mProgress.stop();
        mCurrentHeight = 0;
    }

    @Override
    public int finishPullRefresh(float totalDistance) {
        if (isLoadAnimation) return 0;
        //beginLoading();
        animateShowLoadMore(mLoadMoreListener);
        return 0;
    }


    public void setProgressColors(@ColorInt int... colors) {
        mProgress.setColorSchemeColors(colors);
    }

    //显示加载更多view
    private void animateShowLoadMore(Animation.AnimationListener listener) {
        mAnimationShowLoadMore.reset();
        mAnimationShowLoadMore.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimationShowLoadMore.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mAnimationShowLoadMore.setAnimationListener(listener);
        }
        mParent.clearAnimation();
        mParent.startAnimation(mAnimationShowLoadMore);
    }

    private void animateHideLoadMore(Animation.AnimationListener listener) {
        mParent.clearAnimation();
        mParent.scrollBy(0, -getCurrentHeight());
        reset();
    }

    public void showNoMore(boolean show) {
        mShowNoMore = show;
        isLoading = false;
        if (mProgress.isRunning())
            mProgress.stop();
    }
}
