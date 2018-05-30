package com.gu.swiperefreshplus.extention

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import com.gu.swiperefreshplus.R


/**
 * Created by GUHY on 2017/6/8.
 */

class RefreshViewLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private var mBackgroundColor = 0xff0000
    private var mDefaultThreshold = 150
    private val mAlphaThreshold: Float
    private val mPaint: Paint
    private val mPath: Path
    private val mBkgPaint: Paint
    private var mDampAnimator: ValueAnimator? = null
    private var mOffsetAnimator: ValueAnimator? = null
    private var mTotalOffset: Int = 0
    private var mResertFrom: Int = 0

    private val mProgressView: ProgressView

    private var isfling = false

    init {
        setWillNotDraw(false)
        mBkgPaint = Paint()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.FILL
        mPath = Path()
        initAnimation()
        val metrics = context.resources.displayMetrics
        val dimnet = (metrics.density * progressViewDiment).toInt()
        mAlphaThreshold = dimnet.toFloat()
        mProgressView = ProgressView(context)
        mProgressView.setColor(context.resources.getColor(R.color.white))
        mProgressView.setGap(2 * metrics.density)
        mProgressView.layoutParams = FrameLayout.LayoutParams(dimnet, dimnet)
        addView(mProgressView)
    }

    override fun setBackgroundColor(@ColorInt color: Int) {
        mBackgroundColor = color
        mBkgPaint.color = mBackgroundColor
        mPaint.color = color

    }

    fun setDefaultThreshold(height: Int) {
        mDefaultThreshold = height
    }

    fun pullDown(offset: Int) {
        mTotalOffset = offset
        mPath.reset()
        mPath.moveTo(0f, 0f)
        mPath.lineTo(0f, mDefaultThreshold.toFloat())
        mPath.quadTo((width / 2).toFloat(), mDefaultThreshold + offset * 1.6f, width.toFloat(), mDefaultThreshold.toFloat())
        mPath.lineTo(width.toFloat(), 0f)
        if (!isfling) {
            val bottom = (mDefaultThreshold + offset * 0.8).toInt()
            ViewCompat.offsetTopAndBottom(mProgressView, bottom - mProgressView.height - mProgressView.top)
            val alpha = (bottom - mDefaultThreshold).toFloat() / mAlphaThreshold
            mProgressView.alpha = alpha

        }
        postInvalidate()
    }

    fun start() {
        mProgressView.alpha = 1f
        mProgressView.start()
    }

    fun animatorToCurrentPosition(listener: Animator.AnimatorListener) {
        isfling = true

        //ViewCompat.offsetTopAndBottom(mProgressView,offset-mProgressView.getTop());
        mDampAnimator!!.removeAllListeners()
        if (mOffsetAnimator != null && mOffsetAnimator!!.isRunning) {
            mOffsetAnimator!!.cancel()
        }
        mResertFrom = mTotalOffset
        if (mTotalOffset > 0) {
            if (mDampAnimator!!.isRunning) {
                mDampAnimator!!.cancel()
            }
            mDampAnimator!!.start()
            val offset = (mDefaultThreshold - mProgressView.measuredHeight) / 2
            mOffsetAnimator = ValueAnimator.ofInt(0, offset)
            mOffsetAnimator!!.duration = DEFAULT_OFFSET_DUTION.toLong()
            mOffsetAnimator!!.addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                ViewCompat.offsetTopAndBottom(mProgressView, value - mProgressView.top)
            }
            mOffsetAnimator!!.removeAllListeners()
            mOffsetAnimator!!.addListener(listener)
            mOffsetAnimator!!.start()
        } else {
            reset()
            listener.onAnimationEnd(mOffsetAnimator)
        }
    }

    fun reset() {
        mPath.reset()
        mPath.moveTo(0f, 0f)
        mPath.lineTo(0f, mDefaultThreshold.toFloat())
        mPath.lineTo(width.toFloat(), mDefaultThreshold.toFloat())
        mPath.lineTo(width.toFloat(), 0f)
        postInvalidate()
        isfling = false
        mProgressView.stop()
        mProgressView.disappear(true)
        // postInvalidate(0, mDefaultThreshold, getWidth(), getHeight());

    }

    private fun initAnimation() {
        mDampAnimator = ValueAnimator.ofFloat(0f, 11f)
        mDampAnimator!!.duration = DEFAULT_DAMP_DURATION.toLong()
        mDampAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        mDampAnimator!!.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            val damp = Math.cos(value.toDouble()) / Math.pow(1.2, value.toDouble())
            val offset = mResertFrom * damp
            pullDown(offset.toInt())
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // super.onLayout(changed, left, top, right, bottom);
        val childLeft = (right - left - mProgressView.measuredWidth) / 2
        mProgressView.layout(childLeft, 0, childLeft + mProgressView.measuredWidth, mProgressView.measuredHeight)
        mProgressView.alpha = 0f
        mPath.moveTo(0f, 0f)
        mPath.lineTo(0f, mDefaultThreshold.toFloat())
        mPath.lineTo(right.toFloat(), mDefaultThreshold.toFloat())
        mPath.lineTo(right.toFloat(), 0f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(mPath, mPaint)
    }

    companion object {
        private const val progressViewDiment = 40
        private const val DEFAULT_DAMP_DURATION = 500
        private const val DEFAULT_OFFSET_DUTION = 200
    }

}
