package com.gu.swiperefreshplush.extention;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.apkfuns.logutils.LogUtils;


/**
 * Created by GUHY on 2017/6/8.
 */

public class RefreshViewLayout extends FrameLayout {
    private int mBackgroundColor = 0xff0000;
    private int mDefaultThreshold = 150;
    private Paint mPaint;
    private Path mPath;
    private Paint mBkgPaint;
    private ValueAnimator dampAnimator;
    private int mTotalOffset;
    private Animator.AnimatorListener mDampListener=new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            reset();
            mTotalOffset=0;
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

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

    public void reset(Animator.AnimatorListener listener) {
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
       // mPath.reset();
       // postInvalidate(0, mDefaultThreshold, getWidth(), getHeight());
    }
    private void reset(){
        mPath.reset();
        mPath.moveTo(0,0);
        mPath.lineTo(0,mDefaultThreshold);
        mPath.lineTo(getWidth(),mDefaultThreshold);
        mPath.lineTo(getWidth(),0);
        postInvalidate();
       // postInvalidate(0, mDefaultThreshold, getWidth(), getHeight());

    }
    private void initAnimation(){
        dampAnimator=ValueAnimator.ofFloat(0,10);
        dampAnimator.setDuration(1000);
        dampAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        dampAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float value= (float) animation.getAnimatedValue();
                double damp=(Math.cos(value))/(Math.pow(1.2,value)+1);
                double offset=(mTotalOffset-mDefaultThreshold)*damp;
                pullDown((int) offset);
            }
        });
        dampAnimator.addListener(mDampListener);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
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
