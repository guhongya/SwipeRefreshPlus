package com.gu.swiperefreshplush.extention;

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

    public void pullDown(int targetY) {
        mPath.reset();
        mPath.moveTo(0, mDefaultThreshold);
        mPath.quadTo(getWidth() / 2, targetY * 1.4f, getWidth(), mDefaultThreshold);
        mPath.lineTo(0,mDefaultThreshold);
        //mPath.setLastPoint(0,mDefaultThreshold);
        postInvalidate(0, mDefaultThreshold, getWidth(), getHeight());
    }

    public void reset() {
        mPath.reset();
        //postInvalidate();
        postInvalidate(0, mDefaultThreshold, getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), mDefaultThreshold, mBkgPaint);

        canvas.drawPath(mPath, mPaint);
        //mPaint.setColorFilter(new PorterDuffColorFilter(mBackgroundColor, PorterDuff.Mode.XOR));
        //canvas.drawPath(mPath,mPaint);
    }

}
