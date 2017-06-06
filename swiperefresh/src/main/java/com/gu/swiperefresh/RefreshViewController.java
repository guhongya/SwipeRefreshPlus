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

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import com.gu.swiperefresh.Utils.Size;


/**
 * Created by gu on 2016/12/19.
 * 下拉刷新控制类
 * 下拉刷新 view 使用 google SwipeRefreshLayout 中的 ProgressDrawable
 */

public class RefreshViewController implements IRefreshViewController {
    //默认circleimage大小
    static final int CIRCLE_DIAMETER = 40;
    // Default offset in dips from the top of the view to where the progress spinner should stop
    private static final int DEFAULT_CIRCLE_TARGET = 64;
    // Max amount of circle that can be filled by progress during swipe gesture,
    // where 1.0 is a full circle
    private static final float MAX_PROGRESS_ANGLE = .8f;
    private static final int MAX_ALPHA = 255;
    private static final int STARTING_PROGRESS_ALPHA = (int) (.3f * MAX_ALPHA);
    private static final int ALPHA_ANIMATION_DURATION = 300;
    private static final int SCALE_DOWN_DURATION = 150;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final int ANIMATE_TO_START_DURATION = 200;
    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;
    // Default background for the progress spinner
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    final DisplayMetrics metrics;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private int mFrom;
    private int mSpinnerOffsetEnd;
    // Whether this item is scaled up rather than clipped
    private boolean mScale;
    private float mStartingScale;
    private ProgressDrawable mProgress;
    private int mCurrentTargetOffsetTop;
    private int mOriginalOffsetTop;
    private float mTotalDragDistance = -1;
    private boolean isRefresh;
    private CircleImageView mCircleView;
    private Context mContext;
    private View mParent;
    //是否回调onPullDownToRefresh
    private boolean mNotify;
    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };
    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = 0;
            int endTarget = 0;
            endTarget = mSpinnerOffsetEnd;
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mCircleView.getTop();
            setTargetOffsetTopAndBottom(offset, false /* requires update */);
            mProgress.setArrowScale(1 - interpolatedTime);
        }
    };
    private int mCircleDiameter;
    private SwipeRefreshPlus.OnRefreshListener mListener;
    // refreshview下拉动画结束,开始刷新
    private Animation.AnimationListener mRefreshListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isRefresh) {
                // Make sure the progress view is fully visible
                mProgress.setAlpha(MAX_ALPHA);
                mProgress.start();
                if (mNotify && mListener != null) {
                    mListener.onPullDownToRefresh();
                }
                mCurrentTargetOffsetTop = mCircleView.getTop();
            } else {
                reset();
            }
        }
    };
    /***************************
     * 动画
     ******************************************/
    private Animation mScaleAnimation;
    private Animation mScaleDownAnimation;
    private Animation mAlphaStartAnimation;
    private Animation mAlphaMaxAnimation;
    private Animation mScaleDownToStartAnimation;

    public RefreshViewController(Context context, View parent) {
        this.mContext = context;
        this.mParent = parent;
        metrics = mContext.getResources().getDisplayMetrics();
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        mCircleDiameter = (int) (CIRCLE_DIAMETER * metrics.density);
        mOriginalOffsetTop = mCurrentTargetOffsetTop = -mCircleDiameter;
        mSpinnerOffsetEnd = (int) (DEFAULT_CIRCLE_TARGET * metrics.density);
        mTotalDragDistance = mSpinnerOffsetEnd;

    }

    //创建refresh view
    public View create() {
        mCircleView = new CircleImageView(mContext, CIRCLE_BG_LIGHT);
        mProgress = new ProgressDrawable(mContext, mParent);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mCircleView.setImageDrawable(mProgress);
        mCircleView.setVisibility(View.GONE);
        moveToStart(1.0f);
        return mCircleView;
    }

    //refresh 结束，资源清理
    public void reset() {
        mCircleView.clearAnimation();
        mProgress.stop();
        mCircleView.setVisibility(View.GONE);
        setColorViewAlpha(MAX_ALPHA);
        // Return the circle to its start position
        if (mScale) {
            setAnimationProgress(0 /* animation complete and view is hidden */);
        } else {
            setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop,
                    true /* requires update */);
        }
        mCurrentTargetOffsetTop = mCircleView.getTop();
    }

    /**
     * 设置progress colors
     *
     * @param colors
     */
    public void setProgressColors(@ColorInt int... colors) {
        mProgress.setColorSchemeColors(colors);
    }

    public void startProgress() {
        mProgress.setAlpha(STARTING_PROGRESS_ALPHA);
    }

    public Size getRefreshViewSize() {
        return new Size(mCircleDiameter, mCircleDiameter);
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    //下拉时，refresh动画
    public float showPullRefresh(float overscrollTop) {
        mProgress.showArrow(true);
        float originalDragPercent = overscrollTop / mTotalDragDistance;

        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
        float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
        float extraOS = Math.abs(overscrollTop) - mTotalDragDistance;
        float slingshotDist = mSpinnerOffsetEnd;
        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2) / slingshotDist);
        float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                (tensionSlingshotPercent / 4), 2)) * 2f;
        float extraMove = (slingshotDist) * tensionPercent * 2;

        int targetY = mOriginalOffsetTop + (int) ((slingshotDist * dragPercent) + extraMove);
        // where 1.0f is a full circle
        if (mCircleView.getVisibility() != View.VISIBLE) {
            mCircleView.setVisibility(View.VISIBLE);
        }
        if (!mScale) {
            ViewCompat.setScaleX(mCircleView, 1f);
            ViewCompat.setScaleY(mCircleView, 1f);
        }

        if (mScale) {
            setAnimationProgress(Math.min(1f, overscrollTop / mTotalDragDistance));
        }
        if (overscrollTop < mTotalDragDistance) {
            if (mProgress.getAlpha() > STARTING_PROGRESS_ALPHA
                    && !isAnimationRunning(mAlphaStartAnimation)) {
                // Animate the alpha
                startProgressAlphaStartAnimation();
            }
        } else {
            if (mProgress.getAlpha() < MAX_ALPHA && !isAnimationRunning(mAlphaMaxAnimation)) {
                // Animate the alpha
                startProgressAlphaMaxAnimation();
            }
        }
        float strokeStart = adjustedPercent * .8f;
        mProgress.setStartEndTrim(0f, Math.min(MAX_PROGRESS_ANGLE, strokeStart));
        mProgress.setArrowScale(Math.min(1f, adjustedPercent));

        float rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f;
        mProgress.setProgressRotation(rotation);
        setTargetOffsetTopAndBottom(targetY - mCurrentTargetOffsetTop, true /* requires update */);
        return 0;
    }

    public void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        // mCircleView.bringToFront();
        ViewCompat.offsetTopAndBottom(mCircleView, offset);
        mCurrentTargetOffsetTop = mCircleView.getTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            mParent.invalidate();
        }
    }

    public int getCurrentTargetOffsetTop() {
        return mCurrentTargetOffsetTop;
    }

    /**
     * 根据用户下拉距离，判断是否应该刷新
     *
     * @param overscrollTop
     */
    public float finishPullRefresh(float overscrollTop) {
        if (overscrollTop > mTotalDragDistance) {
            setRefreshing(true, true /* notify */);
        } else {
            // cancel refresh
            isRefresh = false;
            mProgress.setStartEndTrim(0f, 0f);
            Animation.AnimationListener listener = null;
            if (!mScale) {
                listener = new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (!mScale) {
                            startScaleDownAnimation(null);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                };
            }
            animateOffsetToStartPosition(mCurrentTargetOffsetTop, listener);
            mProgress.showArrow(false);
        }
        return 0;
    }

    public void setRefreshListener(SwipeRefreshPlus.OnRefreshListener scrollListener) {
        this.mListener = scrollListener;
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        if (refreshing && isRefresh != refreshing) {
            // scale and show
            isRefresh = refreshing;
            int endTarget = 0;

            setTargetOffsetTopAndBottom(endTarget - mCurrentTargetOffsetTop,
                    true /* requires update */);
            mNotify = false;
            startScaleUpAnimation(mRefreshListener);
        } else {
            setRefreshing(refreshing, false /* notify */);
        }
    }

    @Override
    public int getZIndex() {
        return ZIndex.TOP;
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (isRefresh != refreshing) {
            mNotify = notify;
            // ensureTarget();
            isRefresh = refreshing;
            if (isRefresh) {
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener);
            } else {
                startScaleDownAnimation(mRefreshListener);
            }
        }
    }

    private boolean isAnimationRunning(Animation animation) {
        return animation != null && animation.hasStarted() && !animation.hasEnded();
    }

    /**
     * Pre API 11, this does an alpha animation.
     *
     * @param progress
     */
    void setAnimationProgress(float progress) {
        if (isAlphaUsedForScale()) {
            setColorViewAlpha((int) (progress * MAX_ALPHA));
        } else {
            ViewCompat.setScaleX(mCircleView, progress);
            ViewCompat.setScaleY(mCircleView, progress);
        }
    }

    private void setColorViewAlpha(int targetAlpha) {
        mCircleView.getBackground().setAlpha(targetAlpha);
        mProgress.setAlpha(targetAlpha);
    }

    private void startProgressAlphaStartAnimation() {
        mAlphaStartAnimation = startAlphaAnimation(mProgress.getAlpha(), STARTING_PROGRESS_ALPHA);
    }

    private void startProgressAlphaMaxAnimation() {
        mAlphaMaxAnimation = startAlphaAnimation(mProgress.getAlpha(), MAX_ALPHA);
    }

    private Animation startAlphaAnimation(final int startingAlpha, final int endingAlpha) {
        // Pre API 11, alpha is used in place of scale. Don't also use it to
        // show the trigger point.
        if (mScale && isAlphaUsedForScale()) {
            return null;
        }
        Animation alpha = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                mProgress.setAlpha(
                        (int) (startingAlpha + ((endingAlpha - startingAlpha) * interpolatedTime)));
            }
        };
        alpha.setDuration(ALPHA_ANIMATION_DURATION);
        // Clear out the previous animation listeners.
        mCircleView.setAnimationListener(null);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(alpha);
        return alpha;
    }

    void moveToStart(float interpolatedTime) {
        int targetTop = 0;
        targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mCircleView.getTop();
        setTargetOffsetTopAndBottom(offset, false /* requires update */);
    }

    private void animateOffsetToStartPosition(int from, Animation.AnimationListener listener) {
        if (mScale) {
            // Scale the item back down
            startScaleDownReturnToStartAnimation(from, listener);
        } else {
            mFrom = from;
            mAnimateToStartPosition.reset();
            mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
            mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
            if (listener != null) {
                mCircleView.setAnimationListener(listener);
            }
            mCircleView.clearAnimation();
            mCircleView.startAnimation(mAnimateToStartPosition);
        }
    }

    private void startScaleDownReturnToStartAnimation(int from,
                                                      Animation.AnimationListener listener) {
        mFrom = from;
        if (isAlphaUsedForScale()) {
            mStartingScale = mProgress.getAlpha();
        } else {
            mStartingScale = ViewCompat.getScaleX(mCircleView);
        }
        mScaleDownToStartAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                float targetScale = (mStartingScale + (-mStartingScale * interpolatedTime));
                setAnimationProgress(targetScale);
                moveToStart(interpolatedTime);
            }
        };
        mScaleDownToStartAnimation.setDuration(SCALE_DOWN_DURATION);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleDownToStartAnimation);
    }

    void startScaleDownAnimation(Animation.AnimationListener listener) {
        mScaleDownAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(1 - interpolatedTime);
            }
        };
        mScaleDownAnimation.setDuration(SCALE_DOWN_DURATION);
        mCircleView.setAnimationListener(listener);
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleDownAnimation);
    }

    @SuppressLint("NewApi")
    private void startScaleUpAnimation(Animation.AnimationListener listener) {
        mCircleView.setVisibility(View.VISIBLE);
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            // Pre API 11, alpha is used in place of scale up to show the
            // progress circle appearing.
            // Don't adjust the alpha during appearance otherwise.
            mProgress.setAlpha(MAX_ALPHA);
        }
        mScaleAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(interpolatedTime);
            }
        };
        mScaleAnimation.setDuration(SCALE_DOWN_DURATION);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mScaleAnimation);
    }

    private void animateOffsetToCorrectPosition(int from, Animation.AnimationListener listener) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mCircleView.setAnimationListener(listener);
        }
        mCircleView.clearAnimation();
        mCircleView.startAnimation(mAnimateToCorrectPosition);
    }

    /**
     * Pre API 11, alpha is used to make the progress circle appear instead of scale.
     */
    private boolean isAlphaUsedForScale() {
        return android.os.Build.VERSION.SDK_INT < 11;
    }
}
