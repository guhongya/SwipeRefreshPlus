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

import com.gu.swiperefresh.Utils.Size;

/**
 * Created by Guhy on 2016/12/7.
 *加载更多和没有更多时的view 控制类
 * 加载更多提示 VIEW 使用SwipeRefreshLayout 中的ProgressDrawable 默认颜色为app的前景色
 * 没有更多提示没有默认的view样式，需要使用者手动设置
 */

public class LoadViewController {
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
    private View parent;
    private CircleImageView mCircleImageView;
    private ProgressDrawable mProgress;
    private int mDefaultProgressColor;
    private int currentHeight;
    //loadview 大小
    private int mCircleDiameter;
    private boolean isLoading;
    private SwipeRefreshPlush.OnRefreshListener mListener;
    private int mMargin = 5;
    private int mMaxHeigth;
    //加载更多动画
    private final Animation mAnimationShowLoadMore = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int offset = (int) ((getMaxHeight() - getCurrentHeight()) * interpolatedTime);
            parent.scrollBy(0, move(offset));
        }
    };
    private boolean isDefault = true;
    private View defaultView;
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
        this.parent = parent;
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
        mDefaultProgressColor = typedArray.getColor(0, CIRCLE_BG_LIGHT);
        metrics = mContext.getResources().getDisplayMetrics();
        mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
        mMargin = (int) (mMargin * metrics.density);
        mMaxHeigth = mMargin * 2 + mCircleDiameter;
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
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
        ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(mCircleDiameter, mCircleDiameter);
        marginLayoutParams.setMargins(0, mMargin, 0, mMargin);
        mCircleImageView.setLayoutParams(marginLayoutParams);
        defaultView = mCircleImageView;
        return mCircleImageView;
    }

    protected void setScrollListener(SwipeRefreshPlush.OnRefreshListener onRefreshListener) {
        this.mListener = onRefreshListener;
    }

    protected int getCurrentHeight() {
        return currentHeight;
    }

    protected void clearState() {
        mProgress.stop();
        isLoading = false;
    }

    protected void changeDefaultView(View loadView) {
        this.defaultView = loadView;
        this.isDefault = false;
    }

    protected View getDefaultView() {
        return defaultView;
    }

    /**
     * 实际移动距离
     *
     * @param scrollDistance
     * @return
     */
    protected int move(int scrollDistance) {
        currentHeight += scrollDistance;
        if (currentHeight > mMaxHeigth) {
            int result = scrollDistance - (currentHeight - mMaxHeigth);
            currentHeight = mMaxHeigth;
            return result;
        } else if (currentHeight < 0) {
            int result = scrollDistance - currentHeight;
            currentHeight = 0;
            return result;
        }
        return scrollDistance;
    }

    protected int getMaxHeight() {
        return mMaxHeigth;
    }

    protected void beginLoading() {
        if (isDefault) {
            mProgress.setAlpha(MAX_ALPHA);
            mProgress.start();
        }
        if (!isLoading&&mListener!=null) {
            isLoading = true;
            mListener.onPullUpToRefresh();
        }
    }

    protected void reset() {
        if (mProgress.isRunning())
            mProgress.stop();
        currentHeight = 0;
    }

    protected void stopLoad() {
        isLoading = false;
        reset();
    }

    protected void showLoadMore() {
        if (isLoadAnimation) return;
        animateShowLoadMore(mLoadMoreListener);
    }

    protected boolean canMove() {
        return !isLoadAnimation;
    }

    protected void setProgressColors(@ColorInt int... colors) {
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
        parent.startAnimation(mAnimationShowLoadMore);
    }

    protected void showNoMore(boolean show) {
        mShowNoMore = show;
    }
}
