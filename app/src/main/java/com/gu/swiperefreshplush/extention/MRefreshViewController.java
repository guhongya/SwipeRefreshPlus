package com.gu.swiperefreshplush.extention;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.apkfuns.logutils.LogUtils;
import com.gu.swiperefresh.IRefreshViewController;
import com.gu.swiperefresh.SwipeRefreshPlus;
import com.gu.swiperefresh.ZIndex;

/**
 * Created by GUHY on 2017/4/18.
 */

public class MRefreshViewController implements IRefreshViewController {
    private static final int DEFAULT_POSITION = 100;
    private static final int DEFAULT_PULL_UP_DURATION = 300;
    private static final int DEFAULT_PULL_DOWWN_DURATION = 500;
    private int mOriginOffset;
    private int mTargetPosition;
    private int mCurrentOffsetTop;
    private Context mContext;
    private View mParent;
    private boolean refreshing;
    private boolean mNotify;
    private float mTotalDragDiatance;

    private RefreshViewLayout mRefreshViewLayout;
    private SwipeRefreshPlus.OnRefreshListener mOnRefreshListener;

    private ValueAnimator mPullDownAnimation;
    private ValueAnimator mPullUpAnimation;
    private int mBackgroundColor= Color.WHITE;

    private Animator.AnimatorListener mPullUpListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            reset();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };
    private Animator.AnimatorListener mPullDownListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mRefreshViewLayout.reset();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };


    public MRefreshViewController(Context context, View parent) {
        mContext = context;
        mParent = parent;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mTargetPosition = (int) (DEFAULT_POSITION * metrics.density);
        mCurrentOffsetTop = -mTargetPosition;
        mOriginOffset = mCurrentOffsetTop;
        innitAnimation();

    }

    @Override
    public void reset() {
        mRefreshViewLayout.reset();
        mCurrentOffsetTop = -mTargetPosition;
    }

    @Override
    public View create() {
        mRefreshViewLayout = new RefreshViewLayout(mContext);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mRefreshViewLayout.setLayoutParams(params);
        mRefreshViewLayout.setBackgroundColor(mBackgroundColor);
        mRefreshViewLayout.setDefaultThreshold(mTargetPosition);
        return mRefreshViewLayout;
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
    public boolean isRefresh() {
        return refreshing;
    }

    @Override
    public void showPullRefresh(float overscrollTop) {
        mTotalDragDiatance = overscrollTop;
        if (overscrollTop <= mTargetPosition) {
            mParent.scrollTo(0, (int) -overscrollTop);
            mCurrentOffsetTop = (int) (mOriginOffset + overscrollTop);
        } else {
            mRefreshViewLayout.pullDown((int) overscrollTop - mTargetPosition);
        }
    }

    @Override
    public void finishPullRefresh(float overscrollTop) {
        mTotalDragDiatance = overscrollTop;
        if (overscrollTop > mTargetPosition) {
            setRefreshing(true, true);
        } else {
            pullUpAnimation();
        }
    }

    @Override
    public void startPulling() {
        //setRefreshing(true,true);
    }

    @Override
    public void setTargetOffsetTopAndBottom(int i, boolean b) {
        LogUtils.d(i);
        //if(mPullDownAnimation.isRunning())mPullDownAnimation.cancel();
        if(mPullUpAnimation.isRunning())mPullUpAnimation.cancel();
        ViewCompat.offsetTopAndBottom(mParent, i);
        mCurrentOffsetTop = mRefreshViewLayout.getTop();
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
            setRefreshing(refresh, false /* notify */);
        }
    }

    public void setBackgroundColor(@ColorInt int color){
        mBackgroundColor=color;
        if(mRefreshViewLayout !=null) mRefreshViewLayout.setBackgroundColor(color);
    }

    private void innitAnimation() {
        mPullDownAnimation = new ValueAnimator();
        mPullDownAnimation
                .setDuration(DEFAULT_PULL_DOWWN_DURATION)
                .addListener(mPullDownListener);
        mPullDownAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mParent.scrollTo(0, (int) animation.getAnimatedValue());
                mCurrentOffsetTop = mRefreshViewLayout.getTop();
            }
        });

        mPullUpAnimation = new ValueAnimator();
        mPullUpAnimation.setDuration(DEFAULT_PULL_UP_DURATION);
        mPullUpAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mParent.scrollTo(0, (int) animation.getAnimatedValue());
                mCurrentOffsetTop = mRefreshViewLayout.getTop();
            }
        });
        mPullUpAnimation.addListener(mPullUpListener);
    }

    private void pullDownAnimation() {
        if (mPullDownAnimation.isRunning()) {
           // mPullDownAnimation.cancel();
            return;
        }
        mPullDownAnimation.setIntValues(mParent.getTop(), mTargetPosition);
        mPullDownAnimation.start();
    }

    private void pullUpAnimation() {
        LogUtils.d("pull up");
        if (mPullUpAnimation.isRunning()) {
            //mPullUpAnimation.cancel();
            return;
        }
        mPullUpAnimation.setIntValues(-mParent.getTop(), 0);
        mPullUpAnimation.start();

    }

    private void setRefreshing(boolean refresh, final boolean notify) {
        LogUtils.d(refresh);
        if (refreshing != refresh) {
            refreshing = refresh;
            mNotify = notify;
            if (refreshing) {
                animateOffsetToCorrectPosition();
            } else {
                mRefreshViewLayout.animatorToCurrentPosition(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pullUpAnimation();

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        }
    }

    private void animateOffsetToCorrectPosition() {
        LogUtils.d("animate");
        if (mTotalDragDiatance >= mTargetPosition) {
            mRefreshViewLayout.animatorToCurrentPosition(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mNotify && mOnRefreshListener != null) {
                        mOnRefreshListener.onPullDownToRefresh();
                        mRefreshViewLayout.start();
                    }

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else {
            pullDownAnimation();
        }
    }

}
