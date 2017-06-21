package com.gu.swiperefreshplush.extention;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.view.View;

/**
 * Created by GUHY on 2017/6/21.
 */

public class ProgressView extends View {
    private static final int DEFAULT_DURATION=1500;

    private Paint mPaint;
    private float mGap;
    private Paint mStrokePaint;
    private ValueAnimator mRotateAnimator;
    private RectF mRectf;
    private float mSwipeAngle=0;
    private ValueAnimator mAlphaAnimatoe;
    private ValueAnimator.AnimatorUpdateListener mRotateListener=new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value= (float) animation.getAnimatedValue();
            mSwipeAngle=360*value;
            invalidate();
        }
    };
    public ProgressView(Context context) {
        super(context);
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mStrokePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeCap(Paint.Cap.SQUARE);
        mRotateAnimator=ValueAnimator.ofFloat(0,1.0f);
        mRotateAnimator.setDuration(DEFAULT_DURATION);
        mRotateAnimator.setRepeatMode(ValueAnimator.RESTART);
        mRotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRotateAnimator.addUpdateListener(mRotateListener);
    }

    public void setGap(float gap){
        mStrokePaint.setStrokeWidth(gap);

    }
    public void setColor(@ColorInt int color){
        mPaint.setColor(color);
        mStrokePaint.setColor(color);
    }
    public void disappear(boolean disappear){
        if(disappear){}
        else{

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRectf=new RectF(0+10,0+10,getMeasuredWidth()-10,getMeasuredHeight()-10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getWidth()/2,getHeight()/2, getWidth()/2*0.7f,mPaint);
        canvas.drawArc(mRectf,90,mSwipeAngle+5,false,mStrokePaint);
    }

    public void start(){
        if(mRotateAnimator.isRunning()){
            mRotateAnimator.cancel();
        }
        mRotateAnimator.start();
    }

    public void stop(){
        mRotateAnimator.cancel();
        mSwipeAngle=0;
        invalidate();
    }
}
