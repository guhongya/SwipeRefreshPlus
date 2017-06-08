package com.gu.swiperefreshplush.extention;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.gu.swiperefresh.IRefreshViewController;
import com.gu.swiperefresh.SwipeRefreshPlus;
import com.gu.swiperefresh.Utils.Size;
import com.gu.swiperefresh.ZIndex;
import com.gu.swiperefreshplush.R;

/**
 * Created by GUHY on 2017/4/18.
 */

public class MRefreshViewController implements IRefreshViewController {
    private static final int DEFAULT_POSITION = 100;
    private static final int DEFAULT_PULL_UP_DURATION = 200;
    private static final int DEFAULT_PULL_DOWWN_DURATION = 300;
    private int mOriginOffset;
    private int mTargetPosition;
    private int mCurrentOffsetTop;
    private int mWidth;
    private int mHeight;
    private Context mContext;
    private View mParent;
    private View mTarget;
    private boolean refreshing;
    private boolean mNotify;

    private RefreshViewLayout mRefreshView;
    private SwipeRefreshPlus.OnRefreshListener mOnRefreshListener;

    private Animation mPullDownAnimation = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {


        }
    };
    private Animation mPullUpAnimation = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
           int offset= (int) (-mCurrentOffsetTop*interpolatedTime);
            mCurrentOffsetTop+=offset;
            mParent.scrollTo(0,mCurrentOffsetTop);
        }
    };

    private Animation.AnimationListener mPullDownListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };
    private Animation.AnimationListener mPullUpListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    public MRefreshViewController(Context context, View parent, View target) {
        mContext = context;
        mParent = parent;
        mTarget = target;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mTargetPosition = (int) (DEFAULT_POSITION * metrics.density);
        mCurrentOffsetTop = -mTargetPosition;
        mOriginOffset =mCurrentOffsetTop;
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;

    }

    @Override
    public void reset() {
        mCurrentOffsetTop = -mTargetPosition;
    }

    @Override
    public View create() {
        mRefreshView = new RefreshViewLayout(mContext);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTargetPosition);
        mRefreshView.setLayoutParams(params);
        mRefreshView.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
        mRefreshView.setDefaultThreshold(mTargetPosition);
        return mRefreshView;
    }

    @Override
    public int getZIndex() {
        return ZIndex.TOP;
    }

    @Override
    public int getCurrentTargetOffsetTop() {
        return mCurrentOffsetTop;
    }

    @Override
    public Size getRefreshViewSize() {
        return new Size(mHeight, mWidth);
    }

    @Override
    public boolean isRefresh() {
        return refreshing;
    }

    @Override
    public int showPullRefresh(float overscrollTop) {
        if(overscrollTop<mTargetPosition){
            mParent.scrollTo(0, (int) -overscrollTop);
        }else{
            mRefreshView.pullDown((int) overscrollTop,mWidth/2,(int) overscrollTop*2);
        }
        return 0;
    }

    @Override
    public float finishPullRefresh(float overscrollTop) {
        if(overscrollTop>mTargetPosition){
           pullUpAnimation();
        }else{
            pullUpAnimation();
        }
        return 0;
    }

    @Override
    public void startProgress() {
        setRefreshing(true,true);
    }

    @Override
    public void setTargetOffsetTopAndBottom(int i, boolean b) {
        ViewCompat.offsetTopAndBottom(mRefreshView, i);
        mCurrentOffsetTop = mRefreshView.getTop();
    }

    @Override
    public void setRefreshListener(SwipeRefreshPlus.OnRefreshListener mListener) {
        mOnRefreshListener = mListener;
    }

    @Override
    public void setRefreshing(boolean refresh) {
        if (refresh && refreshing != refresh) {
            // scale and show
            refreshing = refresh;
            int endTarget = 0;

            setTargetOffsetTopAndBottom(endTarget - mCurrentOffsetTop,
                    true /* requires update */);
            mNotify = false;
            pullDownAnimation();
        } else {
            setRefreshing(refreshing, false /* notify */);
        }
    }

    private void pullDownAnimation() {
        mPullDownAnimation.reset();
        mPullDownAnimation.setDuration(DEFAULT_PULL_DOWWN_DURATION);
        mPullDownAnimation.setAnimationListener(mPullDownListener);
        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mPullDownAnimation);
    }

    private void pullUpAnimation() {
        mPullUpAnimation.reset();
        mPullUpAnimation.setDuration(DEFAULT_PULL_UP_DURATION);
        mPullUpAnimation.setAnimationListener(mPullUpListener);
        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mPullUpAnimation);
    }

    private void setRefreshing(boolean refresh, final boolean notify) {
        if (refreshing != refresh) {
            mNotify = notify;
            // ensureTarget();
            refreshing = refresh;
            if (refreshing) {
                // animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener);
            } else {
                pullUpAnimation();
            }
        }
    }
}
