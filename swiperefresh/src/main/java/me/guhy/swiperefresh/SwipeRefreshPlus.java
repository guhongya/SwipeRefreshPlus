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
package me.guhy.swiperefresh;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.OverScroller;

import me.guhy.swiperefresh.Utils.Log;

import static android.support.v4.widget.ViewDragHelper.INVALID_POINTER;

/**
 * Created by gu on 2016/11/13.
 * 仿照 SwipeRefreshLayout 添加了滑动到底部时的事件回调
 * 可添加的子view 可以是 AbsListView 的子类，或者NestedScrollingChild的实现类
 * 当添加了子view 没有实现 NestedScrollingChild 或者子view 是 AbsListView的子类并且sdk《21 时，不会响应子view requestDisallowInterceptTouchEvent 的请求
 * 当targetview 内容不满一屏时，用户向上滑动屏幕 不会触发回调事件
 */

public class SwipeRefreshPlus extends ViewGroup implements NestedScrollingParent,
        NestedScrollingChild {
    private static final float DRAG_RATE = .5f;
    private final String TAG = "SwipeRefreshPlush";
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    int circleViewIndex = -1;
    private int REFRESH_MODE = 1;
    private OnRefreshListener mListener;
    private View mRefreshView;
    private View mLoadMoreView;
    private View mTarget;
    private View mScrollView;
    //上拉距离
    private float mUpTotalUnconsumed;
    //下拉距离
    private float mDownTotalUnconsumed;
    private float mInitialMotionY;
    private float mInitialDownY;
    private boolean mIsBeingDragUp;
    private boolean mIsBeingDragDown;
    private int mActivePointerId = INVALID_POINTER;
    private int mTouchSlop;
    private boolean mNestedScrollInProgress;
    // Target is returning to its start offset because it was cancelled or a
    // refresh was triggered.
    private boolean mReturningToStart;
    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    //fling最小速度
    private int mMinimumVelocity;

    private IRefreshViewController mRefreshController;
    private ILoadViewController mLoadViewController;

    private View mNoMoreView = null;
    //onInterceptTouchEvent或onTouch move时上一点
    private float mLastY;


    public SwipeRefreshPlus(Context context) {
        this(context, null);
    }

    public SwipeRefreshPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLoadViewController = new LoadViewController(context, this);
        mRefreshController = new RefreshViewController(context, this);
        //   mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mScroller = new OverScroller(getContext());
        createProgressView();
        setChildrenDrawingOrderEnabled(true);
    }

    /**
     * 设置滑动监听
     *
     * @param onRefreshListener 刷新回调
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.mListener = onRefreshListener;
        mLoadViewController.setRefreshListener(mListener);
        mRefreshController.setRefreshListener(mListener);
    }

    /**
     * 模式设置
     *
     * @param mode 模式
     */
    public void setScrollMode(@SwipeRefreshMode int mode) {
        this.REFRESH_MODE = mode;
    }

    /**
     * refresh progress color
     *
     * @param colorResIds 一组颜色Id
     */
    public void setRefreshColorResources(@ColorRes int... colorResIds) {
        final Context context = getContext();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = ContextCompat.getColor(context, colorResIds[i]);
        }
        setRefreshColors(colorRes);
    }

    /**
     * 设置progress 颜色
     *
     * @param colors 一组颜色
     */
    public void setRefreshColors(@ColorInt int... colors) {
        ensureTarget();
        if (mRefreshController instanceof RefreshViewController) {
            ((RefreshViewController) mRefreshController).setProgressColors(colors);
        }
    }

    /**
     * 设置loadmore 颜色
     *
     * @param colorResIds 一组颜色
     */
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
        if (mLoadViewController instanceof LoadViewController) {
            ((LoadViewController) mLoadViewController).setProgressColors(colors);
        }
    }

    /**
     * 是否显示refresh
     *
     * @param refresh 刷新标志
     */
    public void setRefresh(boolean refresh) {
        ensureTarget();
        mRefreshController.setRefreshing(refresh);
    }

    /**
     * @param show 是否显示loadmore
     */
    public void setLoadMore(boolean show) {
        final int height = mLoadViewController.getCurrentHeight();
        boolean oldStatus = mLoadViewController.isLoading();
        if (show) {
            mLoadViewController.setLoadMore(show);
        }
        if (oldStatus && !show) {
            mLoadViewController.stopAnimation();
            Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                @Override
                public boolean queueIdle() {
                    mLoadViewController.setLoadMore(false);
                    if (mScrollView instanceof AbsListView) {
                        if (Build.VERSION.SDK_INT > 18) {
                            ((AbsListView) mScrollView).scrollListBy(height);
                        } else {
                            mScrollView.scrollBy(0, height);
                        }
                    } else {
                        mScrollView.scrollBy(0, height);
                    }
                    return false;
                }
            });
        }
    }

    /**
     * 刷新结束
     */
    public void stopLoading() {
        mRefreshController.setRefreshing(false);
        setLoadMore(false);
    }

    /**
     * 设置没有更多提示view
     *
     * @param view         数据加载完毕没有更多数据时显示
     * @param layoutParams view 的LayoutParams
     */
    public void setNoMoreView(View view, LayoutParams layoutParams) {
        mNoMoreView = view;
        mNoMoreView.setLayoutParams(layoutParams);
        // mLoadMoreView.setVisibility(GONE);
    }

    /**
     * 显示没有更多提示
     *
     * @param show true:滑动到底部时显示没有更多，false:滑动到底部时显示加载更多
     */
    public void showNoMore(boolean show) {
        mLoadViewController.showNoMore(show);
        if (show && mNoMoreView != null) {
            mLoadMoreView.clearAnimation();
            detachViewFromParent(mLoadMoreView);
            mLoadMoreView = mNoMoreView;
            addView(mNoMoreView, mNoMoreView.getLayoutParams());
        } else if (!show) {
            detachViewFromParent(mLoadMoreView);
            mLoadMoreView = mLoadViewController.getDefaultView();
            addView(mLoadMoreView);
        }
    }

    public ILoadViewController getLoadViewController() {
        return mLoadViewController;
    }

    public void setLoadViewController(ILoadViewController controller) {
        this.mLoadViewController = controller;
        detachViewFromParent(mLoadMoreView);
        mLoadMoreView = mLoadViewController.create();
        // requestLayout();
        measureChild(mLoadMoreView);
        addView(mLoadMoreView);
        if (mListener != null) {
            mLoadViewController.setRefreshListener(mListener);
        }
        //forceLayout();
    }

    public IRefreshViewController getRefreshController() {
        return mRefreshController;
    }

    /**
     * @param controller refreshview 的控制类
     */
    public void setRefreshViewController(IRefreshViewController controller) {
        this.mRefreshController = controller;
        detachViewFromParent(mRefreshView);
        mRefreshView = mRefreshController.create();
        measureChild(mRefreshView);
        switch (mRefreshController.getZIndex()) {
            case ZIndex.TOP:
                addView(mRefreshView, getChildCount());
                break;
            case ZIndex.BOTTOM:
                addView(mRefreshView, 0);
                break;
            case ZIndex.NORMAL:
                addView(mRefreshView);
                break;
        }
        if (mListener != null) {
            mRefreshController.setRefreshListener(mListener);
        }
    }

    @Override
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        if (!enable) {
            reset();
        }
    }

    /**
     * 设置可滑动view
     *
     * @param target
     */
    public void setScrollView(View target) {
        this.mScrollView = target;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
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
        mRefreshView.layout((width / 2 - circleWidth / 2), mRefreshController.getCurrentTargetOffsetTop(),
                (width / 2 + circleWidth / 2), mRefreshController.getCurrentTargetOffsetTop() + circleHeight);
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
            if (!canChildScrollDown() && canLoadMore()) {
                //showLoadMoreView(mLoadMoreView.getHeight());
                int dis = mLoadViewController.finishPullRefresh(mScroller.getFinalY() - mScroller.getCurrY());
                scrollBy(0, dis);
                mScroller.abortAnimation();
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (circleViewIndex < 0 || mRefreshController.getZIndex() == ZIndex.NORMAL) {
            return i;
        } else if (mRefreshController.getZIndex() == ZIndex.TOP) {
            if (i == circleViewIndex) {
                return childCount - 1;
            } else if (i > circleViewIndex) {
                return i - 1;
            } else {
                return i;
            }
        } else {
            if (i > circleViewIndex) {
                return i + 1;
            } else if (i == circleViewIndex) {
                return 0;
            } else {
                return i;
            }
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
        measureChild(mRefreshView);
//        mRefreshView.measure(MeasureSpec.makeMeasureSpec(mRefreshController.getRefreshViewSize().getWidth(), MeasureSpec.EXACTLY),
//                MeasureSpec.makeMeasureSpec(mRefreshController.getRefreshViewSize().getHeight(), MeasureSpec.EXACTLY));
        measureChild(mLoadMoreView);
        circleViewIndex = -1;
        // Get the index of the circleview.
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mRefreshView) {
                circleViewIndex = index;
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        int pointerIndex;
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
                mIsBeingDragDown = false;
                mActivePointerId = event.getPointerId(0);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = event.getY(pointerIndex);
                mLastY = mInitialDownY;
                return false;
            case MotionEvent.ACTION_POINTER_DOWN:
                pointerIndex = MotionEventCompat.getActionIndex(event);
                if (pointerIndex < 0) {
                    return false;
                }
                mActivePointerId = event.getPointerId(pointerIndex);
                break;
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

                } else if (mIsBeingDragDown) {
                    int dy = (int) (y - mLastY);
                    Log.i(TAG, "lasty:" + mLastY);
                    Log.i(TAG, "dy:" + dy);
                    //消除抖动
                    if (dy >= 0.5) {
                        hideLoadMoreView(Math.abs(dy));
                    } else if (dy < -0.5) {
                        showLoadMoreView(Math.abs(dy));
                    }
                }
                mLastY = y;
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
                    if (overscrollTop > 0) mRefreshController.finishPullRefresh(overscrollTop);
                }
                if (mIsBeingDragDown) {
                    final float y = event.getY(pointerIndex);
                    final float overscrollBottom = (y - mInitialMotionY);
                    mIsBeingDragDown = false;
                    if (overscrollBottom < 0) {
                        int dis = mLoadViewController.finishPullRefresh(Math.abs(overscrollBottom));
                        scrollBy(0, dis);
                    }
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;

        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }
        if (!isEnabled() || (!canLoadMore() && !canRefresh()) || mReturningToStart
                || mRefreshController.isRefresh() || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mRefreshController.setTargetOffsetTopAndBottom(mRefreshController.getCurrentTargetOffsetTop() - mRefreshView.getTop(), true);
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragUp = false;
                mIsBeingDragDown = false;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                initVelocityTracker();
                mVelocityTracker.addMovement(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                }
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
                if (mVelocityTracker != null) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    float initialVelocity = velocityTracker.getYVelocity(mActivePointerId);
                    Log.d(TAG, "fling:" + initialVelocity);
                    if (Math.abs(initialVelocity) > mMinimumVelocity) {
                        flingWithNestedDispatch(0, -initialVelocity);
                    }
                    releaseVelocityTracker();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragUp = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragUp || mIsBeingDragDown;
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

    void reset() {
        mRefreshController.reset();
        mLoadViewController.reset();
    }

    private void createProgressView() {
        this.mRefreshView = mRefreshController.create();
        this.mLoadMoreView = mLoadViewController.create();
        addView(mLoadMoreView, mLoadMoreView.getLayoutParams());
        addView(mRefreshView);
    }

    private void initVelocityTracker() {
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

    private void measureChild(View view) {
        if (view == null)
            return;
        LayoutParams lp = view.getLayoutParams();
        int width, height;
        width = getMeasureSpec(lp.width, getMeasuredWidth());
        height = getMeasureSpec(lp.height, getMeasuredHeight());
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


    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragUp) {
            if (!canChildScrollUp()) {
                mInitialMotionY = mInitialDownY + mTouchSlop;
                mIsBeingDragUp = true;
                mRefreshController.startPulling();
            } else if (mLoadViewController.getCurrentHeight() > 0) {
                hideLoadMoreView((int) yDiff);
            }
        } else if (yDiff < -mTouchSlop && !mIsBeingDragDown && !canChildScrollDown() && canLoadMore()) {
            Log.d(TAG, yDiff + ":" + mTouchSlop);
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mLastY = mInitialDownY;
            mIsBeingDragDown = true;
        }
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

    private boolean fling(float velocityY) {
        if (velocityY <= 0) {
            if (mLoadViewController.getCurrentHeight() > 0) {
                hideLoadMoreView(mLoadViewController.getCurrentHeight());
            }
            mScroller.abortAnimation();
            return false;
        }

        mScroller.abortAnimation();
        mScroller.computeScrollOffset();
        if (canChildScrollUp() && canLoadMore()) {
            mScroller.fling(0, mScroller.getCurrY(), 0, (int) velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        ViewCompat.postInvalidateOnAnimation(this);
        return false;
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }


    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return REFRESH_MODE != SwipeRefreshMode.MODE_NONE && !mRefreshController.isRefresh()
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;

    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        // Dispatch up to the nested parent
        startNestedScroll(nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mUpTotalUnconsumed = 0;
        mDownTotalUnconsumed = 0;
        mNestedScrollInProgress = true;
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        mNestedScrollInProgress = false;
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        if (mUpTotalUnconsumed > 0) {
            mRefreshController.finishPullRefresh(mUpTotalUnconsumed);
            mUpTotalUnconsumed = 0;
        }
        if (mDownTotalUnconsumed > 0) {
            int dis = mLoadViewController.finishPullRefresh(mDownTotalUnconsumed);
            scrollBy(0, dis);
            mDownTotalUnconsumed = 0;
        }
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (mRefreshController.isRefresh())
            return;
        if (dy < 0 && !canChildScrollUp() && canRefresh()) {
            mUpTotalUnconsumed += Math.abs(dy);
            //moveSpinner(mUpTotalUnconsumed);
            mRefreshController.showPullRefresh(mUpTotalUnconsumed);
        } else if (dy > 0 && !canChildScrollDown() && canLoadMore()) {
            mDownTotalUnconsumed += dy;
            showLoadMoreView(dy);
        }
    }

    /**
     * parent 消耗的值
     *
     * @param target   target view
     * @param dx       x distance
     * @param dy       y方向的移动距离
     * @param consumed parent消耗的值
     */
    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
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
            } else if (dy < -1 && mLoadViewController.getCurrentHeight() > 0) {
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
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        if (!consumed) {
            flingWithNestedDispatch(velocityX, velocityY);
            return true;
        }
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        return flingWithNestedDispatch(velocityX, velocityY);

    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    /********************
     * parent end
     ************************************/

    /**
     * targrt view 是否能向上滑动
     *
     * @return target view 是否能向上滑动
     */
    private boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mScrollView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mScrollView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mScrollView.canScrollVertically(-1) || mScrollView.getScrollY() > 0;
            }
        } else {
            return mScrollView.canScrollVertically(-1);
        }
    }

    /**
     * target view 是否能向下滑动
     *
     * @return
     */
    private boolean canChildScrollDown() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mScrollView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mScrollView;
                int count = absListView.getChildCount();
                //return absListView.canScrollList(-1);
                int position = absListView.getLastVisiblePosition();
                return (count > position + 1) || absListView.getChildAt(position).getBottom() <= absListView.getPaddingBottom();
            } else {
                return mScrollView.canScrollVertically(1);
            }
        } else {
            return mScrollView.canScrollVertically(1);
        }
    }


    private void showLoadMoreView(int height) {
        if (mLoadMoreView.getVisibility() != VISIBLE)
            mLoadMoreView.setVisibility(VISIBLE);
        scrollBy(0, mLoadViewController.move(height));
    }


    private void hideLoadMoreView(int height) {
        if (mLoadViewController.getCurrentHeight() > 0) {
            int currentHeight = mLoadViewController.getCurrentHeight();
            if (height > currentHeight) {
                height = currentHeight;
            }
            scrollBy(0, mLoadViewController.move(-height));
        } else {
            mLoadViewController.reset();
        }
    }


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
            if (mScrollView == null) {
                mScrollView = mTarget;
            }
        }
    }

    private boolean canRefresh() {
        if (REFRESH_MODE == SwipeRefreshMode.MODE_BOTH || REFRESH_MODE == SwipeRefreshMode.MODE_REFRESH_ONLY)
            return true;
        else
            return false;
    }

    private boolean canLoadMore() {
        if ((REFRESH_MODE == SwipeRefreshMode.MODE_BOTH || REFRESH_MODE == SwipeRefreshMode.MODE_LOADMODE) && canChildScrollUp()) {
            return true;
        } else
            return false;
    }


    public interface OnRefreshListener {
        void onPullDownToRefresh();

        void onPullUpToRefresh();
    }
}
