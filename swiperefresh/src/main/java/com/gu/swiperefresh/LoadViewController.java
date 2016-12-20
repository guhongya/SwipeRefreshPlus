package com.gu.swiperefresh;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

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

    private int currentHeight;

    //loadview 大小
    private int mCircleDiameter;

    final DisplayMetrics metrics;

    private boolean isLoading;

    private SwipeRefreshPlush.OnScrollListener mListener;

    public LoadViewController(Context context, View parent) {
        this.mContext = context;
        this.parent = parent;

        metrics = mContext.getResources().getDisplayMetrics();
        mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
    }

    protected View create() {
        mCircleImageView = new CircleImageView(mContext, CIRCLE_BG_LIGHT);
        mProgress = new ProgressDrawable(mContext, parent);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        //  mLoadProgress.setAlpha(MAX_ALPHA);
        mProgress.setRotation(MAX_PROGRESS_ANGLE);
        mCircleImageView.setImageDrawable(mProgress);
        mCircleImageView.setVisibility(View.GONE);
        return mCircleImageView;
    }

    protected void setScrollListener(SwipeRefreshPlush.OnScrollListener onScrollListener) {
        this.mListener = onScrollListener;
    }

    protected Size getLoadViewSize() {
        return new Size(mCircleDiameter,mCircleDiameter);
    }

    protected int getCurrentHeight() {
        return currentHeight;
    }

    protected void setCurrentHeight(int height) {
        currentHeight = height;
    }

    protected void move(int distance) {
        currentHeight += distance;
    }

    protected void showLoadAnimation() {
        mProgress.setAlpha(MAX_ALPHA);
        mProgress.start();
        if(!isLoading) {
            isLoading = true;
            mListener.onLoadMore();
        }
    }

    protected void reset() {
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
