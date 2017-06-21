package com.gu.swiperefreshplush.extention;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.apkfuns.logutils.LogUtils;
import com.gu.swiperefreshplush.R;


/**
 * Created by GUHY on 2017/6/8.
 */

public class RefreshViewLayout extends FrameLayout {
    private int mBackgroundColor = 0xff0000;
    private int mDefaultThreshold = 150;
    private static final int progressViewDiment=40;
    private Paint mPaint;
    private Path mPath;
    private Paint mBkgPaint;
    private ValueAnimator dampAnimator;
    private int mTotalOffset;
    private int mResertFrom;

    private ProgressView mProgressView;


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
        postInvalidate();
    }
    public void start(){
        mProgressView.start();
    }
    public void animatorToCurrentPosition(Animator.AnimatorListener listener) {
        dampAnimator.removeAllListeners();
        mResertFrom=mTotalOffset;
        if(mTotalOffset>0){
            if(dampAnimator.isRunning()){
                dampAnimator.cancel();
            }
            dampAnimator.addListener(listener);
            dampAnimator.start();
        }else{
            reset();
            listener.onAnimationEnd(dampAnimator);
        }
       // mPath.animatorToCurrentPosition();
       // postInvalidate(0, mDefaultThreshold, getWidth(), getHeight());
    }
    public void reset(){
        mPath.reset();
        mPath.moveTo(0,0);
        mPath.lineTo(0,mDefaultThreshold);
        mPath.lineTo(getWidth(),mDefaultThreshold);
        mPath.lineTo(getWidth(),0);
        postInvalidate();
        mProgressView.stop();
       // postInvalidate(0, mDefaultThreshold, getWidth(), getHeight());

    }
    private void initAnimation(){
        dampAnimator=ValueAnimator.ofFloat(0,11);
        dampAnimator.setDuration(500);
        dampAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        dampAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
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
       super.onLayout(changed, left, top, right, bottom);
        int childLeft=(right-left-mProgressView.getMeasuredWidth())/2;
        int childTop=(mDefaultThreshold-mProgressView.getMeasuredHeight())/2;
        mProgressView.layout(childLeft,childTop,childLeft+mProgressView.getMeasuredWidth(),childTop+mProgressView.getMeasuredHeight());
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
