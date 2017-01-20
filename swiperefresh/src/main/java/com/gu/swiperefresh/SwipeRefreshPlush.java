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
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;

import com.gu.swiperefresh.Utils.Log;

import static android.support.v4.widget.ViewDragHelper.INVALID_POINTER;

/**
 * Created by gu on 2016/11/13.
 */

public class SwipeRefreshPlush extends ViewGroup implements NestedScrollingParent,
        NestedScrollingChild {
    private final String TAG = "SwipeRefreshPlush";
    private final NestedScrollingChildHelper nestedScrollingChildHelper;
    private final NestedScrollingParentHelper nestedScrollingParentHelper;
    private int REFRESH_MODE = 1;
    private OnScrollListener mListener;
    private View mRefreshView;
    private View mLoadMoreView;
    private View mTarget;

    //上拉距离
    private float mUpTotalUnconsumed;
    //下拉距离
    private float mDownTotalUnconsumed;


    int circleViewIndex = -1;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];


    private float mInitialMotionY;
    private float mInitialDownY;
    private boolean mIsBeingDragUp;
    private boolean mIsBeingDragDowm;
    private int mActivePointerId = INVALID_POINTER;

    private int mTouchSlop;
    private boolean mNestedScrollInProgress;
    // Target is returning to its start offset because it was cancelled or a
    // refresh was triggered.
    private boolean mReturningToStart;

    private static final float DRAG_RATE = .5f;


    private ScrollerCompat mScroller;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    //fling最小速度
    private int mMinimumVelocity;


    private RefreshViewController mRefreshController;

    private LoadViewController mLoadViewController;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;

    private final DecelerateInterpolator mDecelerateInterpolator;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    private volatile boolean isLoadAnimation;

    private View mNoMoreView = null;
    private boolean mShowNoMore = false;
    //onInterceptTouchEvent或onTouch move时上一点
    private float mLastY;

    //loadmore动画结束，调用回调函数
    private Animation.AnimationListener mLoadMoreListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            isLoadAnimation = true;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!mShowNoMore)
                mLoadViewController.beginLoading();
            isLoadAnimation = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    void reset() {
        mRefreshController.reset();
        mLoadViewController.reset();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    private void createProgressView() {
        this.mRefreshView = mRefreshController.create();
        this.mLoadMoreView = mLoadViewController.create();
        addView(mLoadMoreView, mLoadMoreView.getLayoutParams());
        addView(mRefreshView);
    }

    public SwipeRefreshPlush(Context context) {
        this(context, null);
    }

    public SwipeRefreshPlush(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLoadViewController = new LoadViewController(context, this);
        mRefreshController = new RefreshViewController(context, this);
        //   mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        nestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        nestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mScroller = ScrollerCompat.create(getContext());
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        createProgressView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        Log.d(TAG, "onLayout: " + getChildCount());
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0)
            return;
        if (mTarget == null)
            ensureTarget();
        if (mTarget == null)
            return;
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childRight = getPaddingRight();
        final int childTop = getPaddingTop();
        final int childBottom = getPaddingBottom();
        final int childWidth = width - childLeft - childRight;
        //   final int childHeight=child.getMeasuredHeight();
        final int childHeight = height - childTop - childBottom;
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        final int circleWidth = mRefreshView.getMeasuredWidth();
        final int circleHeight = mRefreshView.getMeasuredHeight();
        final int loadViewWidth = mLoadMoreView.getMeasuredWidth();
        final int loadViewHeight = mLoadMoreView.getMeasuredHeight();
        mRefreshView.layout((width / 2 - circleWidth / 2), mRefreshController.getmCurrentTargetOffsetTop(),
                (width / 2 + circleWidth / 2), mRefreshController.getmCurrentTargetOffsetTop() + circleHeight);
        LayoutParams layoutParams = mLoadMoreView.getLayoutParams();
            if (layoutParams instanceof MarginLayoutParams) {
                final MarginLayoutParams lp = (MarginLayoutParams) mLoadMoreView.getLayoutParams();
                mLoadMoreView.layout(width / 2 - loadViewWidth / 2, height - childBottom + lp.topMargin, width / 2 + loadViewWidth / 2,
                        height + loadViewHeight + childBottom + lp.bottomMargin);
            } else {
                mLoadMoreView.layout(width / 2 - loadViewWidth / 2, height - childBottom, width / 2 + loadViewWidth / 2,
                        height + loadViewHeight + childBottom);
            }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (!canChildScrollDown()&&canLoadMore()) {
                showLoadMoreView(mLoadMoreView.getHeight());
                mScroller.abortAnimation();
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (circleViewIndex < 0) {
            return i;
        } else if (i == childCount - 1) {
            // Draw the selected child last
            return circleViewIndex;
        } else if (i >= circleViewIndex) {
            // Move the children after the selected child earlier one
            return i + 1;
        } else {
            // Keep the children before the selected child the same
            return i;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null)
            ensureTarget();
        if (mTarget == null)
            return;
        mTarget.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        mRefreshView.measure(MeasureSpec.makeMeasureSpec(mRefreshController.getRefreshViewSize().getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mRefreshController.getRefreshViewSize().getHeight(), MeasureSpec.EXACTLY));
        measureChild(mLoadMoreView);
        //measureChild(mNoMoreView);
        circleViewIndex = -1;
        // Get the index of the circleview.
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mRefreshView) {
                circleViewIndex = index;
                break;
            }
        }
    }

    private void measureChild(View view) {
        if (view == null)
            return;
        LayoutParams lp = view.getLayoutParams();
        int width, height;
        width = getMeasureSpec(lp.width, getWidth());
        height = getMeasureSpec(lp.height, mLoadViewController.getMaxHeight());
        view.measure(width, height);
    }

    private int getMeasureSpec(int size, int parentSize) {
        int result;
        if (size == LayoutParams.MATCH_PARENT)
            result = MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.EXACTLY);
        else if (size == LayoutParams.WRAP_CONTENT)
            result = MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.AT_MOST);
        else
            result = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        return result;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        int pointerIndex = -1;
        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart
                || mRefreshController.isRefresh() || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragUp = false;
                mIsBeingDragDowm=false;
                mActivePointerId = event.getPointerId(0);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = event.getY(pointerIndex);
                mLastY=mInitialDownY;
                return false;
            case MotionEvent.ACTION_CANCEL:
                return false;
            case MotionEvent.ACTION_MOVE: {
                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    // Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }

                final float y = event.getY(pointerIndex);
                startDragging(y);

                if (mIsBeingDragUp) {
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    if (overscrollTop > 0) {
                        mRefreshController.showPullRefresh(overscrollTop);
                    }

                }else if(mIsBeingDragDowm){
                    int dy=(int)(y-mLastY);
                    Log.i(TAG,"dy:"+dy);
                    //消除抖动
                        if (dy >= 0.5) {
                            hideLoadMoreView(Math.abs(dy));
                        } else if(dy<-0.5){
                            showLoadMoreView(dy);
                    }
                }
                mLastY=y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    //  Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragUp) {
                    final float y = event.getY(pointerIndex);
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragUp = false;
                    if (overscrollTop > 0)
                        mRefreshController.finishPullRefresh(overscrollTop);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }
        return true;
    }


    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragUp) {
            if (!canChildScrollUp() && canLoadMore()) {
                mInitialMotionY = mInitialDownY + mTouchSlop;
                mIsBeingDragUp = true;
                mRefreshController.startProgress();
            } else if (mLoadViewController.getCurrentHeight() > 0) {
                hideLoadMoreView((int) yDiff);
            }
        } else if (yDiff < -mTouchSlop && !mIsBeingDragDowm && !canChildScrollDown() && canLoadMore()) {
            Log.d(TAG, yDiff + ":" + mTouchSlop);
            mInitialMotionY = mInitialDownY - mTouchSlop;
            mIsBeingDragDowm = true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }
        if (!isEnabled() || (!canLoadMore() && !canRefresh()) || mReturningToStart || mRefreshController.isRefresh() || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mRefreshController.setTargetOffsetTopAndBottom(mRefreshController.getmCurrentTargetOffsetTop() - mRefreshView.getTop(), true);
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragUp = false;
                mIsBeingDragDowm=false;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                mVelocityTracker.addMovement(ev);
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                startDragging(y);
                break;

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;

            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                float initialVelocity = velocityTracker.getYVelocity(mActivePointerId);
                Log.d(TAG, "fling:" + initialVelocity);
                if (Math.abs(initialVelocity) > mMinimumVelocity) {
                    flingWithNestedDispatch(0, -initialVelocity);
                }
                releaseVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragUp = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragUp||mIsBeingDragDowm;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
            //mVelocityTracker.addMovement(ev);
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if ((android.os.Build.VERSION.SDK_INT < 21 && mTarget instanceof AbsListView)
                || (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    private boolean flingWithNestedDispatch(float velocityX, float velocityY) {
        final boolean canFling = Math.abs(velocityY) > mMinimumVelocity;
        if (!dispatchNestedPreFling(velocityX, velocityY)) {
            dispatchNestedFling(velocityX, velocityY, canFling);
            if (canFling) {
                return fling(velocityY);
            }
        }
        return false;
    }

    public boolean fling(float velocityY) {
        if (velocityY <= 0) {
            if (mLoadViewController.getCurrentHeight() > 0) {
                hideLoadMoreView(mLoadViewController.getCurrentHeight());
            }
            mScroller.abortAnimation();
            return false;
        }
        //mPullState = STATE_FLING;
        mScroller.abortAnimation();
        mScroller.computeScrollOffset();
        if (canChildScrollUp() && canLoadMore()) {
            mScroller.fling(0, mScroller.getCurrY(), 0, (int) velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        ViewCompat.postInvalidateOnAnimation(this);
        return false;
    }

    /***
     * nestedScrollingChild
     **/
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        nestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return nestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return nestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return nestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
    /********************child end*********************************************************/


    /**********************parent begin***************************************************************/
    /**
     * @param child
     * @param target
     * @param nestedScrollAxes 滚动标志
     * @return
     */
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return REFRESH_MODE != SwipeRefreshMode.MODE_NONE && !mRefreshController.isRefresh()
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;

    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        // Dispatch up to the nested parent
        startNestedScroll(nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mUpTotalUnconsumed = 0;
        mDownTotalUnconsumed = 0;
        mNestedScrollInProgress = true;
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollInProgress = false;
        nestedScrollingParentHelper.onStopNestedScroll(target);
        if (mUpTotalUnconsumed > 0) {
            mRefreshController.finishPullRefresh(mUpTotalUnconsumed);
            mUpTotalUnconsumed = 0;
        }
        if (mDownTotalUnconsumed > 0) {
            mDownTotalUnconsumed = 0;
        }
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (mRefreshController.isRefresh())
            return;
        if (dy < 0 && !canChildScrollUp() && canRefresh()) {
            mUpTotalUnconsumed += Math.abs(dy);
            //moveSpinner(mUpTotalUnconsumed);
            mRefreshController.showPullRefresh(mUpTotalUnconsumed);
        } else if (dy > 0 &&mLoadViewController.getCurrentHeight()==0&& !canChildScrollDown() && canLoadMore()) {
            mDownTotalUnconsumed += dy;
            showLoadMoreView(dy);
        }
    }

    /**
     * parent 消耗的值
     *
     * @param target
     * @param dx
     * @param dy       y方向的移动距离>0向上滑
     * @param consumed parent消耗的值
     */
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (REFRESH_MODE != SwipeRefreshMode.MODE_NONE) {
            if (dy > 0 && mUpTotalUnconsumed > 0) {
                if (dy > mUpTotalUnconsumed) {
                    consumed[1] = dy - (int) mUpTotalUnconsumed;
                    mUpTotalUnconsumed = 0;
                } else {
                    mUpTotalUnconsumed -= dy;
                    consumed[1] = dy;
                }
                mRefreshController.showPullRefresh(mUpTotalUnconsumed);
            } else if (dy < -1 && mLoadViewController.getCurrentHeight() > 0&&!isLoadAnimation) {
                if (dy + mDownTotalUnconsumed < 0) {
                    consumed[1] = dy + (int) mDownTotalUnconsumed;
                    mDownTotalUnconsumed = 0;
                } else {
                    mDownTotalUnconsumed += dy;
                    consumed[1] = dy;
                }
                hideLoadMoreView(Math.abs(dy));
            }
        }
        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (!consumed) {
            flingWithNestedDispatch(velocityX, velocityY);
            return true;
        }
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        // Log.d(TAG,velocityX+":"+velocityY);
        return flingWithNestedDispatch(velocityX, velocityY);

    }

    @Override
    public int getNestedScrollAxes() {
        return nestedScrollingParentHelper.getNestedScrollAxes();
    }

    /********************
     * parent end
     ************************************/


    //显示加载更多view
    private void animateShowLoadMore(Animation.AnimationListener listener) {
        mAnimationShowLoadMore.reset();
        mAnimationShowLoadMore.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimationShowLoadMore.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mAnimationShowLoadMore.setAnimationListener(listener);
        }
        mLoadMoreView.startAnimation(mAnimationShowLoadMore);
    }

    //加载更多动画
    private final Animation mAnimationShowLoadMore = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int offset = (int) ((mLoadViewController.getMaxHeight() - mLoadViewController.getCurrentHeight()) * interpolatedTime);
            scrollBy(0,  mLoadViewController.move(offset));
        }
    };

    /*******************************************************************/
    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    public boolean canChildScrollDown() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                int count = absListView.getChildCount();
                int position = absListView.getLastVisiblePosition();
                return (count > position + 1) || absListView.getChildAt(position).getBottom() <=absListView.getPaddingBottom();
            } else {
                return ViewCompat.canScrollVertically(mTarget, 1);
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, 1);
        }
    }


    private void showLoadMoreView(int height) {
        if (!isLoadAnimation&&mLoadViewController.getCurrentHeight()<=0) {
            isLoadAnimation=true;
            if (mLoadMoreView.getVisibility() != VISIBLE)
                mLoadMoreView.setVisibility(VISIBLE);
            mLoadMoreView.clearAnimation();
            animateShowLoadMore(mLoadMoreListener);
        }
    }


    private void hideLoadMoreView(int height) {
        if(isLoadAnimation)
            return;
        if (mLoadViewController.getCurrentHeight() > 0) {
            int currentHeight = mLoadViewController.getCurrentHeight();
            if (height > currentHeight) {
                height = currentHeight;
            }
            scrollBy(0,  mLoadViewController.move(-height));
        }
        if (mLoadViewController.getCurrentHeight() <= 0) {
            mLoadViewController.reset();
        }
    }


    /*********基本设置************/
    /**
     * @param onScrollListener
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mListener = onScrollListener;
        mLoadViewController.setScrollListener(mListener);
        mRefreshController.setListener(mListener);
    }

    /**
     * 模式设置
     * <p>
     * public static interface SwipeRefreshMode{
     * int MODE_BOTH=1;
     * int MODE_REFRESH_ONLY=2;
     * int MODE_LOADMODE=3;
     * int MODE_NONE=4;
     * }
     *
     * @param mode 模式
     */
    public void setScrollMode(int mode) {
        this.REFRESH_MODE = mode;
    }

    public void setRefreshView(View view) {
        this.mRefreshView = view;
    }


    public void setRefreshColorResources(@ColorRes int... colorResIds) {
        final Context context = getContext();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = ContextCompat.getColor(context, colorResIds[i]);
        }
        setRefreshColors(colorRes);
    }

    public void setRefreshColors(@ColorInt int... colors) {
        ensureTarget();
        mRefreshController.setProgressColors(colors);
    }

    public void setLoadMoreColorResources(@ColorRes int... colorResIds) {
        final Context context = getContext();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = ContextCompat.getColor(context, colorResIds[i]);
        }
        setLoadMoreColors(colorRes);
    }

    public void setLoadMoreColors(@ColorInt int... colors) {
        ensureTarget();
        mLoadViewController.setProgressColors(colors);
    }

    public void setRefresh(boolean refresh) {
        ensureTarget();
        mRefreshController.setRefreshing(refresh, false);
    }

    public void setLoadMore(boolean show) {
        if (show) {
            showLoadMoreView(mLoadViewController.getMaxHeight());
        } else {
            // hideLoadMoreView();
            hideLoadMoreView(mLoadViewController.getMaxHeight());
            mLoadViewController.stopLoad();
        }
    }

    public void setLoadMoreView(View view, LayoutParams layoutParams) {
        detachViewFromParent(mLoadMoreView);
        this.mLoadMoreView = view;
        addView(mLoadMoreView, layoutParams);
        mLoadViewController.changeDefaultView(mLoadMoreView);
    }

    public void setNoMoreView(View view, LayoutParams layoutParams) {
        mNoMoreView = view;
        mNoMoreView.setLayoutParams(layoutParams);
        // mLoadMoreView.setVisibility(GONE);
    }

    public void showNoMore(boolean show) {
        mShowNoMore = show;
        if (show && mNoMoreView != null) {
            mLoadViewController.clearState();
            mLoadMoreView.clearAnimation();
            detachViewFromParent(mLoadMoreView);
            mLoadMoreView = mNoMoreView;
            addView(mNoMoreView, mNoMoreView.getLayoutParams());
        } else if(!show) {
            detachViewFromParent(mLoadMoreView);
            mLoadMoreView = mLoadViewController.getDefaultView();
            addView(mLoadMoreView);
        }
    }
    @Override
    public void setEnabled(boolean enable){
        super.setEnabled(enable);
        if(!enable){
            reset();
        }
    }
    /*********************************************************************/
    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mRefreshView) && !child.equals(mLoadMoreView)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    public static interface SwipeRefreshMode {
        int MODE_BOTH = 1;//刷新和下拉加载更多模式
        int MODE_REFRESH_ONLY = 2;//刷新
        int MODE_LOADMODE = 3;//加载更多
        int MODE_NONE = 4;//即不能加载更多也不能下拉刷新
    }

    public interface OnScrollListener {
        void onRefresh();

        void onLoadMore();
    }

    private boolean canRefresh() {
        if (REFRESH_MODE == SwipeRefreshMode.MODE_BOTH || REFRESH_MODE == SwipeRefreshMode.MODE_REFRESH_ONLY)
            return true;
        else
            return false;
    }

    private boolean canLoadMore() {
        if (REFRESH_MODE == SwipeRefreshMode.MODE_BOTH || REFRESH_MODE == SwipeRefreshMode.MODE_LOADMODE) {
            return true;
        } else
            return false;
    }
}
