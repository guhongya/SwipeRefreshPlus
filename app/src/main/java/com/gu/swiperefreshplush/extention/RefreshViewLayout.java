package com.gu.swiperefreshplush.extention;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.apkfuns.logutils.LogUtils;


/**
 * Created by GUHY on 2017/6/8.
 */

public class RefreshViewLayout extends FrameLayout {
    private int mBackgroundColor=0x000000;
    private int mDefaultThreshold=150;
    private Paint mPaint;
    private Path mPath;
    public RefreshViewLayout(@NonNull Context context) {
       this(context,null);
    }

    public RefreshViewLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public RefreshViewLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        mBackgroundColor=color;
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mPath=new Path();
        mPath.moveTo(0,mDefaultThreshold);
        mPath.quadTo(300,1000,getWidth(),mDefaultThreshold);
        mPath.setLastPoint(0,mDefaultThreshold);
        mPaint.setColor(mBackgroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPath=new Path();
    }

    public void setDefaultThreshold(int height){
        mDefaultThreshold=height;
    }

    public void pullDown(int targetY, int fingerx, int fingerY){
        if(targetY>mDefaultThreshold){
            mPath.reset();
            mPath.moveTo(0,mDefaultThreshold);
            mPath.quadTo(fingerx,fingerY,getWidth(),mDefaultThreshold);
            mPath.setLastPoint(0,mDefaultThreshold);
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath,mPaint);
    }
}
