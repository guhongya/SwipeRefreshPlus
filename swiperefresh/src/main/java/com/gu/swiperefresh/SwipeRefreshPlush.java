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
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.apkfuns.logutils.LogUtils;

import static android.support.v4.widget.ViewDragHelper.INVALID_POINTER;

/**
 * Created by gu on 2016/11/13.
 */

public class SwipeRefreshPlush extends ViewGroup implements NestedScrollingParent,
        NestedScrollingChild {
    private final NestedScrollingChildHelper nestedScrollingChildHelper;
    private final NestedScrollingParentHelper nestedScrollingParentHelper;
    private int REFRESH_MODE=1;
    private OnScrollListener mListener;
    private View refreshView;
    private View loadMoreView;
    private View mTarget;
    ProgressDrawable mProgress;
    private boolean isRefresh;
    //下拉距离
    private float mUpTotalUnconsumed;
    //下拉距离
    private float mDownTotalUnconsumed;
    private CircleImageView mCircleView;
    //默认circleimage大小
    static final int CIRCLE_DIAMETER = 40;
    // Default background for the progress spinner
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    int circleViewIndex=-1;
    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    private int mCircleDiameter;

    final DisplayMetrics metrics = getResources().getDisplayMetrics();
    int mCurrentTargetOffsetTop;
    int mOriginalOffsetTop;

    // Default offset in dips from the top of the view to where the progress spinner should stop
    private static final int DEFAULT_CIRCLE_TARGET = 64;
    private float mTotalDragDistance = -1;
    int mSpinnerOffsetEnd;

    // Max amount of circle that can be filled by progress during swipe gesture,
    // where 1.0 is a full circle
    private static final float MAX_PROGRESS_ANGLE = .8f;
    private static final int MAX_ALPHA = 255;
    // Whether this item is scaled up rather than clipped
    boolean mScale;
    private static final int STARTING_PROGRESS_ALPHA = (int) (.3f * MAX_ALPHA);

    private static final int ALPHA_ANIMATION_DURATION = 300;

    private static final int SCALE_DOWN_DURATION = 150;

    protected int mFrom;

    float mStartingScale;

    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    private final DecelerateInterpolator mDecelerateInterpolator;

    private static final int ANIMATE_TO_START_DURATION = 200;

    private static final int ANIMATE_TO_TRIGGER_DURATION = 200;

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

    private ProgressDrawable mLoadProgress;
    private ImageView defaultLoadView;
    private int defaultLoadSize=30;
    private int mLoadDiameter;
    private boolean isLoad;

    private ScrollerCompat mScroller;
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;

   // SwipeRefreshLayout
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
                    if (mListener != null) {
                        mListener.onRefresh();
                    }

                mCurrentTargetOffsetTop = mCircleView.getTop();
            } else {
                reset();
            }
        }
    };

    void reset() {
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

    private void createProgressView() {
        mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT);
        mProgress = new ProgressDrawable(getContext(), this);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mCircleView.setImageDrawable(mProgress);
        mCircleView.setVisibility(View.GONE);
        addView(mCircleView);
        this.refreshView=mCircleView;
    }
    private void createLoadView(){
        defaultLoadView=new ImageView(getContext());
        mLoadProgress=new ProgressDrawable(getContext(),this);
        mLoadProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mLoadProgress.setAlpha(MAX_ALPHA);
        mLoadProgress.setRotation(MAX_PROGRESS_ANGLE);
        defaultLoadView.setImageDrawable(mLoadProgress);
        defaultLoadView.setVisibility(GONE);
        addView(defaultLoadView);
        this.loadMoreView=defaultLoadView;
    }
    public SwipeRefreshPlush(Context context) {
        this(context,null);
    }

    public SwipeRefreshPlush(Context context, AttributeSet attrs) {
        super(context, attrs);
     //   mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        nestedScrollingChildHelper=new NestedScrollingChildHelper(this);
        nestedScrollingParentHelper=new NestedScrollingParentHelper(this);
        mCircleDiameter=(int) (CIRCLE_DIAMETER * metrics.density);
        mLoadDiameter=(int)(defaultLoadSize*metrics.density);
        mOriginalOffsetTop = mCurrentTargetOffsetTop = -mCircleDiameter;
        mScroller=ScrollerCompat.create(getContext());
        createProgressView();
        createLoadView();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        mSpinnerOffsetEnd = (int) (DEFAULT_CIRCLE_TARGET * metrics.density);
        mTotalDragDistance = mSpinnerOffsetEnd;
        moveToStart(1.0f);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        LogUtils.d("layout");
        final int width=getMeasuredWidth();
        final int height=getMeasuredHeight();
        int loadViewWidth=defaultLoadView.getMeasuredWidth();
        int loadViewHeight=defaultLoadView.getMeasuredHeight();
        if(getChildCount()==0)
            return;
        if(mTarget==null)
            ensureTarget();
        if(mTarget==null)
            return;
        final View child=mTarget;
        final int childLeft=getPaddingLeft();
        final int childRight=getPaddingRight();
        final int childTop=getPaddingTop();
        final int childBottom=getPaddingBottom();
        final int childWidth=width-childLeft-childRight;
        final int childHeight=height-childTop-childBottom-loadViewHeight;
        child.layout(childLeft,childTop,childLeft+childWidth,childTop+childHeight);
        int circleWidth = mCircleView.getMeasuredWidth();
        int circleHeight = mCircleView.getMeasuredHeight();
        mCircleView.layout((width / 2 - circleWidth / 2), mCurrentTargetOffsetTop,
                (width / 2 + circleWidth / 2), mCurrentTargetOffsetTop + circleHeight);
        defaultLoadView.layout(width/2-loadViewWidth/2,height-loadViewHeight-childBottom,width/2+loadViewWidth/2,height-childBottom);

       // mScroller.startScroll(0,height,0,loadViewHeight);
    }
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
           // int oldY = mHeaderController.getScroll();
            int y = mScroller.getCurrY();
            LogUtils.d("fling y"+y);
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
        if(mTarget==null)
            ensureTarget();
        if(mTarget==null)
            return;
        mTarget.measure(MeasureSpec.makeMeasureSpec(
                getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        mCircleView.measure(MeasureSpec.makeMeasureSpec(mCircleDiameter, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mCircleDiameter, MeasureSpec.EXACTLY));
        if(defaultLoadView.getVisibility()==VISIBLE)
        defaultLoadView.measure(MeasureSpec.makeMeasureSpec(mLoadDiameter,MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mLoadDiameter,MeasureSpec.EXACTLY));
        circleViewIndex = -1;
        // Get the index of the circleview.
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mCircleView) {
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
                || isRefresh || mNestedScrollInProgress||isLoad) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }
        switch (action){
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
            case MotionEvent.ACTION_MOVE:
            {
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
                        showPullRefresh(overscrollTop);
                    } else {
                        mDownTotalUnconsumed+=Math.abs(overscrollTop);
                        showLoadMoreView();
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:{
                pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                  //  Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false;
                }

                if (mIsBeingDragged) {
                    final float y = event.getY(pointerIndex);
                    final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
                    mIsBeingDragged = false;
                    finishPullRefresh(overscrollTop);
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
            mProgress.setAlpha(STARTING_PROGRESS_ALPHA);
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
                || isRefresh || mNestedScrollInProgress||isLoad) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCircleView.getTop(), true);
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
                float initialVelocity =  VelocityTrackerCompat.getYVelocity(velocityTracker,
                        mActivePointerId);
                LogUtils.d("velocity"+initialVelocity);
                if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                    flingWithNestedDispatch(0,initialVelocity);
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
    private boolean flingWithNestedDispatch(float velocityX,float velocityY) {
        final boolean canFling = velocityY>0;
        if (!dispatchNestedPreFling(velocityX, velocityY)) {
            dispatchNestedFling(velocityX, velocityY, canFling);
            if (canFling) {
                 fling(velocityY);
            }
        }
        return canFling;
    }
    public void fling(float velocityY) {
        //mPullState = STATE_FLING;
        mScroller.abortAnimation();
        mScroller.computeScrollOffset();
//        mScroller.fling(0, mHeaderController.getScroll(), 0, velocityY, 0, 0,
//                mHeaderController.getMinScroll(), mHeaderController.getMaxScroll(),
//                0, 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }
    /***nestedScrollingChild**/
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
        return nestedScrollingChildHelper.hasNestedScrollingParent()                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               ;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed,dyConsumed,dxUnconsumed,dyUnconsumed,offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx,dy,consumed,offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX,velocityY,consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX,velocityY);
    }
    /********************child end*********************************************************/


    /**********************parent begin***************************************************************/
    /**
     *
     * @param child
     * @param target
     * @param nestedScrollAxes 滚动标志
     * @return
     */
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return REFRESH_MODE!=SwipeRefreshMode.MODE_NONE&&!isRefresh
                && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;

    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        // Dispatch up to the nested parent
        startNestedScroll(nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mUpTotalUnconsumed = 0;
        mDownTotalUnconsumed=0;
        mNestedScrollInProgress = true;
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollInProgress = false;
        nestedScrollingParentHelper.onStopNestedScroll(target);
        if (mUpTotalUnconsumed > 0) {
            finishPullRefresh(mUpTotalUnconsumed);
            mUpTotalUnconsumed = 0;
        }
        if(mDownTotalUnconsumed>0){
            mDownTotalUnconsumed=0;
        }
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        LogUtils.d("unconsumed"+dy);
        if (dy < 0 && !canChildScrollUp()) {
            mUpTotalUnconsumed += Math.abs(dy);
            //moveSpinner(mUpTotalUnconsumed);
            showPullRefresh(mUpTotalUnconsumed);
        }else if(dy>0&&canChildScrollUp()){
            mDownTotalUnconsumed+=dy;
            //// TODO: 2016/12/2 显示加载更多
            showLoadMoreView();
        }
    }

    /**
     *parent 消耗的值
     * @param target
     * @param dx
     * @param dy
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
            showPullRefresh(mUpTotalUnconsumed);
        }else if(dy<0&&mDownTotalUnconsumed>0){
            if(dy+mDownTotalUnconsumed<0){
                consumed[1]=dy+(int)mDownTotalUnconsumed;
                mDownTotalUnconsumed=0;
                mLoadProgress.stop();
                loadMoreView.setVisibility(GONE);
            }else {
                mDownTotalUnconsumed+=dy;
                consumed[1]=dy;
            }
        }
        if(mDownTotalUnconsumed==0){
            mLoadProgress.stop();
            loadMoreView.setVisibility(GONE);
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        LogUtils.d("fling consumed"+consumed+velocityY);
        if(!consumed&&velocityY>0){
            flingWithNestedDispatch(velocityX,velocityY);
            return true;
        }
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
//        LogUtils.d("onNestedPreFling");
       return dispatchNestedPreFling(velocityX, velocityY);
      //  return flingWithNestedDispatch(velocityX,velocityY);

    }

    @Override
    public int getNestedScrollAxes() {
        return nestedScrollingParentHelper.getNestedScrollAxes();
    }
   /********************parent end************************************/
   /***************************动画******************************************/
   private Animation mScaleAnimation;

    private Animation mScaleDownAnimation;

    private Animation mAlphaStartAnimation;

    private Animation mAlphaMaxAnimation;

    private Animation mScaleDownToStartAnimation;
    /**
     * Pre API 11, this does an alpha animation.
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
    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };
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
                float targetScale = (mStartingScale + (-mStartingScale  * interpolatedTime));
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
    private void showPullRefresh(float overscrollTop){
        LogUtils.e("pull refresh");
        mProgress.showArrow(true);
        float originalDragPercent = overscrollTop / mTotalDragDistance;

        float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
        float adjustedPercent = (float) Math.max(dragPercent - .4, 0) * 5 / 3;
        float extraOS = Math.abs(overscrollTop) - mTotalDragDistance;
        float slingshotDist =  mSpinnerOffsetEnd;
        float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2)
                / slingshotDist);
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
    }
    private void finishPullRefresh(float overscrollTop) {
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
    }
    private void showLoadMoreView(){
        if(loadMoreView.getVisibility()!=View.VISIBLE) {
            loadMoreView.setVisibility(View.VISIBLE);
            isLoad=true;
            mLoadProgress.start();
            mListener.onLoadMore();
        }
    }
    private void hideLoadMoreView(){
        if(loadMoreView.getVisibility()!=View.GONE) {
            loadMoreView.setVisibility(View.GONE);
            mLoadProgress.stop();
            isLoad=false;
        }
    }
    public void setLoadMore(boolean show){
        if(show){
            showLoadMoreView();
        }else{
            hideLoadMoreView();
        }
    }

    void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        mCircleView.bringToFront();
        ViewCompat.offsetTopAndBottom(mCircleView, offset);
        mCurrentTargetOffsetTop = mCircleView.getTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }
   /*********基本设置************/
    /**
     *
     * @param onScrollListener
     */
   public void setOnScrollListener(OnScrollListener onScrollListener){
       this.mListener=onScrollListener;
   }

    /**
     * 模式设置
     *
     *  public static interface SwipeRefreshMode{
     int MODE_BOTH=1;
     int MODE_REFRESH_ONLY=2;
     int MODE_LOADMODE=3;
     int MODE_NONE=4;
     }
     * @param mode 模式
     */
    public void setScrollMode(int mode){
        this.REFRESH_MODE=mode;
    }
    public void setRefreshView(View view){
        this.refreshView=view;
    }
    public void setLoadMoreView(View view){
        this.loadMoreView=view;
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
        mProgress.setColorSchemeColors(colors);
    }
    public void setRefresh(boolean refresh){
        setRefreshing(refresh,false);
    }
    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (isRefresh != refreshing) {
          //  mNotify = notify;
            ensureTarget();
            isRefresh = refreshing;
            if (isRefresh) {
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener);
            } else {
                startScaleDownAnimation(mRefreshListener);
            }
        }
    }
    /*********************************************************************/
    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(refreshView)&&!child.equals(loadMoreView)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }
    public static interface SwipeRefreshMode{
       int MODE_BOTH=1;//刷新和下拉加载更多模式
       int MODE_REFRESH_ONLY=2;//刷新
       int MODE_LOADMODE=3;//加载更多
       int MODE_NONE=4;//即不能加载更多也不能下拉刷新
   }

    public interface OnScrollListener{
        void onRefresh();
        void onLoadMore();
    }
    /**
     * Pre API 11, alpha is used to make the progress circle appear instead of scale.
     */
    private boolean isAlphaUsedForScale() {
        return android.os.Build.VERSION.SDK_INT < 11;
    }
    private boolean isAnimationRunning(Animation animation) {
        return animation != null && animation.hasStarted() && !animation.hasEnded();
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
