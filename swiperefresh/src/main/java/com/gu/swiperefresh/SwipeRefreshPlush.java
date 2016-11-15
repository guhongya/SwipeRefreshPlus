package com.gu.swiperefresh;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by gu on 2016/11/13.
 */

public class SwipeRefreshPlush extends ViewGroup implements NestedScrollingParent,
        NestedScrollingChild {
    private final NestedScrollingChildHelper nestedScrollingChildHelper;
    private final NestedScrollingParentHelper nestedScrollingParentHelper;
    private int REFRESH_MODE=1;
    private OnScrollListener onScrollListener;
    private View refreshView;
    private View loadMoreView;
    private View mTarget;
    ProgressDrawable mProgress;
    private boolean isRefresh;


    public SwipeRefreshPlush(Context context) {
        this(context,null);
    }

    public SwipeRefreshPlush(Context context, AttributeSet attrs) {
        super(context, attrs);
        nestedScrollingChildHelper=new NestedScrollingChildHelper(this);
        nestedScrollingParentHelper=new NestedScrollingParentHelper(this);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        final int width=getMeasuredWidth();
        final int height=getMeasuredHeight();
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
        final int childHeight=height-childTop-childBottom;
        child.layout(childLeft,childTop,childLeft+childWidth,childTop+childHeight);

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
    }

    @Override
    public void onStopNestedScroll(View target) {

    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
      return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return 0;
    }
   /********************parent end************************************/
   /*********基本设置************/
    /**
     *
     * @param onScrollListener
     */
   public void setOnScrollListener(OnScrollListener onScrollListener){
       this.onScrollListener=onScrollListener;
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

}
