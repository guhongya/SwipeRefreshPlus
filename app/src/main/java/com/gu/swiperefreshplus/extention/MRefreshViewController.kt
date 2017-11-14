package com.gu.swiperefreshplus.extention

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v4.view.ViewCompat
import android.view.View
import android.view.ViewGroup
import com.apkfuns.logutils.LogUtils
import me.guhy.swiperefresh.IRefreshViewController
import me.guhy.swiperefresh.SwipeRefreshPlus
import me.guhy.swiperefresh.ZIndex

/**
 * Created by GUHY on 2017/4/18.
 */

class MRefreshViewController(private val mContext: Context, private val mParent: View) : IRefreshViewController {
    override fun getZIndex(): Int {
        return ZIndex.NORMAL
    }

    override fun getCurrentTargetOffsetTop(): Int {
        return mCurrentOffsetTop
    }

    override fun isRefresh(): Boolean {
        return refreshing
    }
//    override val currentTargetOffsetTop: Int = 0
//
//    override val isRefresh: Boolean = false

    private val mOriginOffset: Int
    private val mTargetPosition: Int
    private var mCurrentOffsetTop: Int = 0
    private var refreshing: Boolean = false
    private var mNotify: Boolean = false
    private var mTotalDragDiatance: Float = 0.toFloat()

    private var mRefreshViewLayout: RefreshViewLayout? = null
    private var mOnRefreshListener: SwipeRefreshPlus.OnRefreshListener? = null

    private var mPullDownAnimation: ValueAnimator? = null
    private var mPullUpAnimation: ValueAnimator? = null
    private var mBackgroundColor = Color.WHITE

    private val mPullUpListener = object : Animator.AnimatorListener {

        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationEnd(animation: Animator) {
            reset()
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {

        }
    }
    private val mPullDownListener = object : Animator.AnimatorListener {

        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationEnd(animation: Animator) {
            mRefreshViewLayout!!.reset()
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {

        }
    }


    init {
        val metrics = mContext.resources.displayMetrics
        mTargetPosition = (DEFAULT_POSITION * metrics.density).toInt()
        mCurrentOffsetTop = -mTargetPosition
        mOriginOffset = mCurrentOffsetTop
        innitAnimation()

    }

    override fun reset() {
        mRefreshViewLayout!!.reset()
        mCurrentOffsetTop = -mTargetPosition
    }

    override fun create(): View {
        mRefreshViewLayout = RefreshViewLayout(mContext)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mRefreshViewLayout!!.layoutParams = params
        mRefreshViewLayout!!.setBackgroundColor(mBackgroundColor)
        mRefreshViewLayout!!.setDefaultThreshold(mTargetPosition)
        return mRefreshViewLayout as RefreshViewLayout
    }

//    override fun getCurrentTargetOffsetTop(): Int {
//        return mCurrentOffsetTop
//    }
//
//
//    override fun isRefresh(): Boolean {
//        return refreshing
//    }

    override fun showPullRefresh(overscrollTop: Float) {
        mTotalDragDiatance = overscrollTop
        if (overscrollTop <= mTargetPosition) {
            mParent.scrollTo(0, (-overscrollTop).toInt())
            mCurrentOffsetTop = (mOriginOffset + overscrollTop).toInt()
        } else {
            mRefreshViewLayout!!.pullDown(overscrollTop.toInt() - mTargetPosition)
        }
    }

    override fun finishPullRefresh(overscrollTop: Float) {
        mTotalDragDiatance = overscrollTop
        if (overscrollTop > mTargetPosition) {
            setRefreshing(true, true)
        } else {
            pullUpAnimation()
        }
    }

    override fun startPulling() {
        //setRefreshing(true,true);
    }

    override fun setTargetOffsetTopAndBottom(i: Int, b: Boolean) {
        LogUtils.d(i)
        //if(mPullDownAnimation.isRunning())mPullDownAnimation.cancel();
        if (mPullUpAnimation!!.isRunning) {
            mPullUpAnimation!!.cancel()
        }
        ViewCompat.offsetTopAndBottom(mParent, i)
        mCurrentOffsetTop = mRefreshViewLayout!!.top
    }

    override fun setRefreshListener(mListener: SwipeRefreshPlus.OnRefreshListener) {
        mOnRefreshListener = mListener
    }

    override fun setRefreshing(refresh: Boolean) {
        if (refresh && refreshing != refresh) {
            // scale and show
            refreshing = refresh
            val endTarget = 0

            setTargetOffsetTopAndBottom(endTarget - mCurrentOffsetTop,
                    true /* requires update */)
            mNotify = false
            pullDownAnimation()
        } else {
            setRefreshing(refresh, false /* notify */)
        }
    }

    fun setBackgroundColor(@ColorInt color: Int) {
        mBackgroundColor = color
        if (mRefreshViewLayout != null) {
            mRefreshViewLayout!!.setBackgroundColor(color)
        }
    }

    private fun innitAnimation() {
        mPullDownAnimation = ValueAnimator()
        mPullDownAnimation!!
                .setDuration(DEFAULT_PULL_DOWWN_DURATION.toLong())
                .addListener(mPullDownListener)
        mPullDownAnimation!!.addUpdateListener { animation ->
            mParent.scrollTo(0, animation.animatedValue as Int)
            mCurrentOffsetTop = mRefreshViewLayout!!.top
        }

        mPullUpAnimation = ValueAnimator()
        mPullUpAnimation!!.duration = DEFAULT_PULL_UP_DURATION.toLong()
        mPullUpAnimation!!.addUpdateListener { animation ->
            mParent.scrollTo(0, animation.animatedValue as Int)
            mCurrentOffsetTop = mRefreshViewLayout!!.top
        }
        mPullUpAnimation!!.addListener(mPullUpListener)
    }

    private fun pullDownAnimation() {
        if (mPullDownAnimation!!.isRunning) {
            // mPullDownAnimation.cancel();
            return
        }
        mPullDownAnimation!!.setIntValues(mParent.top, mTargetPosition)
        mPullDownAnimation!!.start()
    }

    private fun pullUpAnimation() {
        LogUtils.d("pull up")
        if (mPullUpAnimation!!.isRunning) {
            //mPullUpAnimation.cancel();
            return
        }
        mPullUpAnimation!!.setIntValues(-mParent.top, 0)
        mPullUpAnimation!!.start()

    }

    private fun setRefreshing(refresh: Boolean, notify: Boolean) {
        LogUtils.d(refresh)
        if (refreshing != refresh) {
            refreshing = refresh
            mNotify = notify
            if (refreshing) {
                animateOffsetToCorrectPosition()
            } else {
                mRefreshViewLayout!!.animatorToCurrentPosition(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        pullUpAnimation()

                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationRepeat(animation: Animator) {

                    }
                })
            }
        }
    }

    private fun animateOffsetToCorrectPosition() {
        LogUtils.d("animate")
        if (mTotalDragDiatance >= mTargetPosition) {
            mRefreshViewLayout!!.animatorToCurrentPosition(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    if (mNotify && mOnRefreshListener != null) {
                        mOnRefreshListener!!.onPullDownToRefresh()
                        mRefreshViewLayout!!.start()
                    }

                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
        } else {
            pullDownAnimation()
        }
    }

    companion object {
        private val DEFAULT_POSITION = 100
        private val DEFAULT_PULL_UP_DURATION = 300
        private val DEFAULT_PULL_DOWWN_DURATION = 500
    }

}
