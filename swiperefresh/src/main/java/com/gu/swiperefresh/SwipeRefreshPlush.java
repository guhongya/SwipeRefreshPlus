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
import android.support.v4.view.VelocityTrackerCompat;
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

import com.apkfuns.logutils.LogUtils;

import static android.support.v4.widget.ViewDragHelper.INVALID_POINTER;

/**
 * Created by gu on 2016/11/13.
 */

public class SwipeRefreshPlush extends ViewGroup implements NestedScrollingParent,
        NestedScrollingChild {
    private final NestedScrollingChildHelper nestedScrollingChildHelper;
    private final NestedScrollingParentHelper nestedScrollingParentHelper;
    private int REFRESH_MODE = 1;
    private OnScrollListener mListener;
    private View refreshView;
    private View loadMoreView;
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
    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;

    private int mTouchSlop;
    private boolean mNestedScrollInProgress;
    // Target is returning to its start offset because it was cancelled or a
    // refresh was triggered.
    private boolean mReturningToStart;

    private static final float DRAG_RATE = .5f;

    private boolean isLoad;

    private ScrollerCompat mScroller;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    //fling最小速度
    private int mMinimumVelocity;

    private int lastFlingY;

    private RefreshViewController refreshController;

   private LoadViewController loadViewController;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;

    private final DecelerateInterpolator mDecelerateInterpolator;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    //loadmore动画结束，调用回调函数
    private Animation.AnimationListener mLoadMoreListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isLoad) {
                LogUtils.d("load begin");
                loadViewController.showLoadAnimation();
                if (mListener != null) {
                    mListener.onLoadMore();
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    void reset() {
       refreshController.reset();
    }

    private void createProgressView() {
       this.refreshView=refreshController.create();
        this.loadMoreView=loadViewController.create();
        addView(loadMoreView);
        addView(refreshView);
    }

    public SwipeRefreshPlush(Context context) {
        this(context, null);
    }

    public SwipeRefreshPlush(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadViewController=new LoadViewController(context,this);
        refreshController=new RefreshViewController(context,this);
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
        LogUtils.d("layout");
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        int loadViewWidth = loadMoreView.getMeasuredWidth();
        int loadViewHeight = loadMoreView.getMeasuredHeight();
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
        final int childHeight = height - childTop - childBottom - loadViewController.getCurrentHeight();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        int circleWidth = refreshView.getMeasuredWidth();
        int circleHeight = refreshView.getMeasuredHeight();
        refreshView.layout((width / 2 - circleWidth / 2), refreshController.getmCurrentTargetOffsetTop(),
                (width / 2 + circleWidth / 2), refreshController.getmCurrentTargetOffsetTop() + circleHeight);
        loadMoreView.layout(width / 2 - loadViewWidth / 2, height - childBottom, width / 2 + loadViewWidth / 2, height + loadViewHeight - childBottom);

        // mScroller.startScroll(0,height,0,loadViewHeight);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // int oldY = mHeaderController.getScroll();
            int y = mScroller.getCurrY();
            int dy=y-lastFlingY;
            lastFlingY=y;
            //dispatchNestedScroll(0,0,0,dy,new int[2]);
            LogUtils.d("fling y" + y);
            LogUtils.d("target height"+mTarget.getMeasuredHeight());
           // scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
//            if (oldY != y) {
//                //moveBy(y - oldY);
//            }
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            //tryBounceBack();
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
        refreshView.measure(MeasureSpec.makeMeasureSpec(refreshController.getRefreshViewSize(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(refreshController.getRefreshViewSize(), MeasureSpec.EXACTLY));
        loadMoreView.measure(MeasureSpec.makeMeasureSpec(loadViewController.getLoadViewSize(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(loadViewController.getLoadViewSize(), MeasureSpec.EXACTLY));
        circleViewIndex = -1;
        // Get the index of the circleview.
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == refreshView) {
                circleViewIndex = index;
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        int pointerIndex = -1;
        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }

        if (!isEnabled() || mReturningToStart || canChildScrollUp()
                || refreshController.isRefresh() || mNestedScrollInProgress || isLoad) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;
                mActivePointerId = event.getPointerId(0);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = event.getY(pointerIndex);
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

                if (mIsBeingDragged) {
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    if (overscrollTop > 0) {
                        refreshController.showPullRefresh(overscrollTop);
                    } else {
                        mDownTotalUnconsumed += Math.abs(overscrollTop);
                        showLoadMoreView((int) Math.abs(overscrollTop));
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    //  Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragged) {
                    final float y = event.getY(pointerIndex);
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragged = false;
                    refreshController.finishPullRefresh(overscrollTop);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }
        return true;
    }


    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
            refreshController.startProgress();
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
        if (!isEnabled() || mReturningToStart || canChildScrollUp()
                || refreshController.isRefresh() || mNestedScrollInProgress || isLoad) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                refreshController.setTargetOffsetTopAndBottom(refreshController.getmCurrentTargetOffsetTop() - refreshView.getTop(), true);
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;

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
                    // Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
                    return false;
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
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                float initialVelocity = VelocityTrackerCompat.getYVelocity(velocityTracker,
                        mActivePointerId);
                LogUtils.d("velocity" + initialVelocity);
                if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                    flingWithNestedDispatch(0, initialVelocity);
                }
                releaseVelocityTracker();
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
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
        final boolean canFling = velocityY > mMinimumVelocity;
        if (!dispatchNestedPreFling(velocityX, velocityY)) {
            dispatchNestedFling(velocityX, velocityY, canFling);
            if (canFling) {
                fling(velocityY);
                return true;
            }
        }
        return false;
    }

    public void fling(float velocityY) {
        //mPullState = STATE_FLING;
        mScroller.abortAnimation();
        mScroller.computeScrollOffset();

        mScroller.fling(0, mScroller.getCurrY(), 0, (int) velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

//        mScroller.fling(0, mHeaderController.getScroll(), 0, velocityY, 0, 0,
//                mHeaderController.getMinScroll(), mHeaderController.getMaxScroll(),
//                0, 0);

        ViewCompat.postInvalidateOnAnimation(this);
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
        return REFRESH_MODE != SwipeRefreshMode.MODE_NONE && !refreshController.isRefresh()
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
            refreshController.finishPullRefresh(mUpTotalUnconsumed);
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
        LogUtils.d("unconsumed" + dy);
        if (dy < 0 && !canChildScrollUp()) {
            mUpTotalUnconsumed += Math.abs(dy);
            //moveSpinner(mUpTotalUnconsumed);
            refreshController.showPullRefresh(mUpTotalUnconsumed);
        } else if (dy > 0 && canChildScrollUp()) {
            mDownTotalUnconsumed += dy;
            //// TODO: 2016/12/2 显示加载更多
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
        if (dy > 0 && mUpTotalUnconsumed > 0) {
            if (dy > mUpTotalUnconsumed) {
                consumed[1] = dy - (int) mUpTotalUnconsumed;
                mUpTotalUnconsumed = 0;
            } else {
                mUpTotalUnconsumed -= dy;
                consumed[1] = dy;
            }
            //   moveSpinner(mUpTotalUnconsumed);
            refreshController.showPullRefresh(mUpTotalUnconsumed);
        } else if (dy < 0 && mDownTotalUnconsumed > 0) {
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

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        LogUtils.d("fling consumed" + consumed + velocityY);
        if (!consumed && velocityY > 0) {
            flingWithNestedDispatch(velocityX, velocityY);
            return true;
        }
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
//        LogUtils.d("onNestedPreFling");
       // return dispatchNestedPreFling(velocityX, velocityY);
           return flingWithNestedDispatch(velocityX,velocityY);

    }

    @Override
    public int getNestedScrollAxes() {
        return nestedScrollingParentHelper.getNestedScrollAxes();
    }
    /********************parent end************************************/


    //显示加载更多view
    private void animateShowLoadMore(Animation.AnimationListener listener) {
        mAnimationShowLoadMore.reset();
        mAnimationShowLoadMore.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimationShowLoadMore.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mAnimationShowLoadMore.setAnimationListener(listener);
        }
        loadMoreView.clearAnimation();
        loadMoreView.startAnimation(mAnimationShowLoadMore);
    }

    //加载更多动画
    private final Animation mAnimationShowLoadMore = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int offset = (int) ((loadMoreView.getHeight() - loadViewController.getCurrentHeight()) * interpolatedTime);
            loadViewController.move(offset);
            // float offset=-loadMoreView.getMeasuredHeight()*interpolatedTime;
            ViewCompat.offsetTopAndBottom(mTarget, -offset);
            ViewCompat.offsetTopAndBottom(loadMoreView, -offset);
//            mTarget.offsetTopAndBottom(-offset);
//            loadMoreView.offsetTopAndBottom(-offset);

           // mLoadProgress.setArrowScale(1 - interpolatedTime);
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



    private void showLoadMoreView(int height) {
        LogUtils.i("height:" + height + "slop" + mTouchSlop);
        if (!isLoad) {
            isLoad = true;
            if (loadMoreView.getVisibility() != VISIBLE)
                loadMoreView.setVisibility(VISIBLE);
            animateShowLoadMore(mLoadMoreListener);
        }
    }


    private void hideLoadMoreView(int height) {
        if (isLoad) {
            loadMoreView.offsetTopAndBottom(height);
            mTarget.offsetTopAndBottom(height);
            loadViewController.move(-height);
        }
        if (loadViewController.getCurrentHeight() <= 0) {
            loadViewController.reset();
            isLoad = false;
        }
    }

    public void setLoadMore(boolean show) {
        if (show) {
            showLoadMoreView(loadMoreView.getMeasuredHeight());
        } else {
            // hideLoadMoreView();
            hideLoadMoreView(loadMoreView.getMeasuredHeight());
        }
    }


    /*********基本设置************/
    /**
     * @param onScrollListener
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mListener = onScrollListener;
        loadViewController.setScrollListener(mListener);
        refreshController.setListener(mListener);
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
        this.refreshView = view;
    }

    public void setLoadMoreView(View view) {
        this.loadMoreView = view;
    }

    public void setColorSchemeResources(@ColorRes int... colorResIds) {
        final Context context = getContext();
        int[] colorRes = new int[colorResIds.length];
        for (int i = 0; i < colorResIds.length; i++) {
            colorRes[i] = ContextCompat.getColor(context, colorResIds[i]);
        }
        setColorSchemeColors(colorRes);
    }

    public void setColorSchemeColors(@ColorInt int... colors) {
        ensureTarget();
        refreshController.setProgressColors(colors);
        loadViewController.setProgressColors(colors);
    }

    public void setRefresh(boolean refresh) {
        ensureTarget();
        refreshController.setRefreshing(refresh, false);
    }



    /*********************************************************************/
    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(refreshView) && !child.equals(loadMoreView)) {
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




    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }
}
