package com.gu.swiperefreshplus.extention;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.gu.swiperefreshplus.R;


/**
 * Created by GUHY on 2017/6/8.
 */

public class RefreshViewLayout extends FrameLayout {
    private int mBackgroundColor = 0xff0000;
    private int mDefaultThreshold = 150;
    private static final int progressViewDiment=40;
    private static final int DEFAULT_DAMP_DURATION=500;
    private static final int DEFAULT_OFFSET_DUTION=200;
    private float mAlphaThreshold;
    private Paint mPaint;
    private Path mPath;
    private Paint mBkgPaint;
    private ValueAnimator mDampAnimator;
    private ValueAnimator mOffsetAnimator;
    private int mTotalOffset;
    private int mResertFrom;

    private ProgressView mProgressView;

    private boolean isfling=false;

    public RefreshViewLayout(@NonNull Context context) {
        this(context, null);
    }

    public RefreshViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mBkgPaint=new Paint();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPath = new Path();
        initAnimation();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int dimnet= (int) (metrics.density*progressViewDiment);
        mAlphaThreshold=dimnet;
        mProgressView=new ProgressView(context);
        mProgressView.setColor(context.getResources().getColor(R.color.white));
        mProgressView.setGap(2*metrics.density);
        mProgressView.setLayoutParams(new LayoutParams(dimnet,dimnet));
        addView(mProgressView);
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        mBackgroundColor = color;
        mBkgPaint.setColor(mBackgroundColor);
        mPaint.setColor(color);

    }

    public void setDefaultThreshold(int height) {
        mDefaultThreshold = height;
    }

    public void pullDown(int offset) {
        mTotalOffset=offset;
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(0,mDefaultThreshold);
        mPath.quadTo(getWidth() / 2, mDefaultThreshold+offset * 1.6f, getWidth(), mDefaultThreshold);
        mPath.lineTo(getWidth(),0);
        if(!isfling) {
            int bottom = (int) (mDefaultThreshold + offset * 0.8);
            ViewCompat.offsetTopAndBottom(mProgressView, bottom-mProgressView.getHeight()-mProgressView.getTop());
            float alpha=((float) (bottom-mDefaultThreshold))/mAlphaThreshold;
            mProgressView.setAlpha(alpha);

        }
        postInvalidate();
    }
    public void start(){
        mProgressView.setAlpha(1f);
        mProgressView.start();
    }
    public void animatorToCurrentPosition(Animator.AnimatorListener listener) {
        isfling=true;

        //ViewCompat.offsetTopAndBottom(mProgressView,offset-mProgressView.getTop());
        mDampAnimator.removeAllListeners();
        if(mOffsetAnimator!=null&&mOffsetAnimator.isRunning()) {
            mOffsetAnimator.cancel();
        }
        mResertFrom=mTotalOffset;
        if(mTotalOffset>0){
            if(mDampAnimator.isRunning()) {
                mDampAnimator.cancel();
            }
            mDampAnimator.start();
            int offset=(mDefaultThreshold-mProgressView.getMeasuredHeight())/2;
            mOffsetAnimator=ValueAnimator.ofInt(0,offset);
            mOffsetAnimator.setDuration(DEFAULT_OFFSET_DUTION);
            mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value= (int) animation.getAnimatedValue();
                    ViewCompat.offsetTopAndBottom(mProgressView,value-mProgressView.getTop());
                }
            });
            mOffsetAnimator.removeAllListeners();
            mOffsetAnimator.addListener(listener);
            mOffsetAnimator.start();
        }else{
            reset();
            listener.onAnimationEnd(mOffsetAnimator);
        }
    }
    public void reset(){
        mPath.reset();
        mPath.moveTo(0,0);
        mPath.lineTo(0,mDefaultThreshold);
        mPath.lineTo(getWidth(),mDefaultThreshold);
        mPath.lineTo(getWidth(),0);
        postInvalidate();
        isfling=false;
        mProgressView.stop();
        mProgressView.disappear(true);
       // postInvalidate(0, mDefaultThreshold, getWidth(), getHeight());

    }
    private void initAnimation(){
        mDampAnimator =ValueAnimator.ofFloat(0,11);
        mDampAnimator.setDuration(DEFAULT_DAMP_DURATION);
        mDampAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mDampAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float value= (float) animation.getAnimatedValue();
                double damp=(Math.cos(value))/Math.pow(1.2,value);
                double offset=mResertFrom*damp;
                pullDown((int) offset);
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
      // super.onLayout(changed, left, top, right, bottom);
        int childLeft=(right-left-mProgressView.getMeasuredWidth())/2;
        mProgressView.layout(childLeft,0,childLeft+mProgressView.getMeasuredWidth(),mProgressView.getMeasuredHeight());
        mProgressView.setAlpha(0);
        mPath.moveTo(0,0);
        mPath.lineTo(0,mDefaultThreshold);
        mPath.lineTo(right,mDefaultThreshold);
        mPath.lineTo(right,0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
    }

}
