package com.gu.swiperefreshplus.extention

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt
import android.view.View

/**
 * Created by GUHY on 2017/6/21.
 */

class ProgressView(context: Context) : View(context) {

    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mGap: Float = 0.0f
    private val mStrokePaint: Paint
    private val mRotateAnimator: ValueAnimator
    private var mRectf: RectF? = null
    private var mSwipeAngle = 0f
    private var mStartAngle = 90f
    private val mAlphaAnimatoe: ValueAnimator
    private val mRotateListener = ValueAnimator.AnimatorUpdateListener { animation ->
        val value = animation.animatedValue as Float
        if (value < 1) {
            mSwipeAngle = 360 * value
        } else {
            val angle = 360 * value % 360
            mStartAngle = 90 + angle
            mSwipeAngle = 360 - angle
        }
        invalidate()
    }

    init {
        mPaint.style = Paint.Style.FILL
        mStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mStrokePaint.style = Paint.Style.STROKE
        mStrokePaint.strokeCap = Paint.Cap.SQUARE
        mRotateAnimator = ValueAnimator.ofFloat(0f, 2.0f)
        mRotateAnimator.duration = DEFAULT_DURATION.toLong()
        mRotateAnimator.repeatMode = ValueAnimator.RESTART
        mRotateAnimator.repeatCount = ValueAnimator.INFINITE
        mRotateAnimator.addUpdateListener(mRotateListener)
        mAlphaAnimatoe = ValueAnimator.ofFloat(1.0f, 0f)
        mAlphaAnimatoe.duration = DISAPPEAR_DURATION.toLong()
    }

    fun setGap(gap: Float) {
        mStrokePaint.strokeWidth = gap

    }

    fun setColor(@ColorInt color: Int) {
        mPaint.color = color
        mStrokePaint.color = color
    }

    fun disappear(disappear: Boolean) {
        if (disappear) {
            if (mAlphaAnimatoe.isRunning) {
                mAlphaAnimatoe.cancel()
            }
            mAlphaAnimatoe.removeAllUpdateListeners()
            mAlphaAnimatoe.addUpdateListener { animation ->
                val alpha = animation.animatedValue as Float
                this@ProgressView.alpha = alpha
            }
            mAlphaAnimatoe.start()
        } else {

        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mRectf = RectF((0 + 10).toFloat(), (0 + 10).toFloat(), (measuredWidth - 10).toFloat(), (measuredHeight - 10).toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), width / 2 * 0.7f, mPaint)
        canvas.drawArc(mRectf!!, mStartAngle, mSwipeAngle + 5, false, mStrokePaint)
    }

    fun start() {
        if (mRotateAnimator.isRunning) {
            mRotateAnimator.cancel()
        }
        mRotateAnimator.start()
    }

    fun stop() {
        mRotateAnimator.cancel()
        mSwipeAngle = 0f
        mStartAngle = 90f
        invalidate()
    }

    companion object {
        private const val DEFAULT_DURATION = 2500
        private const val DISAPPEAR_DURATION = 200
    }
}
