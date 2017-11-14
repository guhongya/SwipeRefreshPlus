/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.guhy.swiperefresh

import android.content.Context
import android.support.annotation.ColorInt
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation

/**
 * Created by Guhy on 2016/12/7.
 * 加载更多和没有更多时的view 控制类
 * 加载更多提示 VIEW 使用SwipeRefreshLayout 中的ProgressDrawable 默认颜色为app的前景色
 * 没有更多提示没有默认的view样式，需要使用者手动设置
 */

class LoadViewController(private val mContext: Context, private val mParent: SwipeRefreshPlus) : ILoadViewController {
    internal val metrics: DisplayMetrics
    private val mDecelerateInterpolator: DecelerateInterpolator
    private lateinit var mCircleImageView: CircleImageView
    private var mProgress: ProgressDrawable? = null
    private var mDefaultProgressColor: Int = 0
    override var currentHeight: Int = 0
        public set(value: Int) {
            currentHeight = value
        }

    override var isLoading: Boolean = false
        public set(value: Boolean) {
            isLoading = value
        }
    //loadview 大小
    private val mCircleDiameter: Int
    private var mMargin = 5
    override var defaultHeight: Int=0
    //加载更多动画
    private val mAnimationShowLoadMore = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            val offset = ((defaultHeight - currentHeight) * interpolatedTime).toInt()
            mParent.scrollBy(0, move(offset))
        }
    }
    private var mListener: SwipeRefreshPlus.OnRefreshListener? = null
    //动画是否在加载
    @Volatile private var isLoadAnimation: Boolean = false
    //是否显示没有更多view
    private var mShowNoMore = false
    //loadmore动画结束，调用回调函数
    private val mLoadMoreListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {
            isLoadAnimation = true
        }

        override fun onAnimationEnd(animation: Animation) {
            if (!mShowNoMore) {
                beginLoading()
            }
            isLoadAnimation = false
        }

        override fun onAnimationRepeat(animation: Animation) {

        }
    }

    override var defaultView: View =mCircleImageView
        get() = mCircleImageView

    init {
        val typedArray = mContext.theme.obtainStyledAttributes(intArrayOf(android.R.attr.colorAccent))
        try {
            mDefaultProgressColor = typedArray.getColor(0, CIRCLE_BG_LIGHT)
        } catch (e: Exception) {
            mDefaultProgressColor = CIRCLE_BG_LIGHT
        }

        metrics = mContext.resources.displayMetrics
        mCircleDiameter = (CIRCLE_DIAMETER * metrics.density).toInt()
        mMargin = (mMargin * metrics.density).toInt()
        defaultHeight = mMargin * 2 + mCircleDiameter
        mDecelerateInterpolator = DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)
        typedArray.recycle()
    }

    override fun create(): View {
        mCircleImageView = CircleImageView(mContext, CIRCLE_BG_LIGHT)
        mProgress = ProgressDrawable(mContext, mParent)
        mProgress!!.setBackgroundColor(CIRCLE_BG_LIGHT)
        mProgress!!.rotation= MAX_PROGRESS_ANGLE;
        //mProgress!!.setRotation(MAX_PROGRESS_ANGLE)
        mProgress!!.setColorSchemeColors(*intArrayOf(mDefaultProgressColor))
        mCircleImageView!!.setImageDrawable(mProgress)
        val marginLayoutParams = ViewGroup.MarginLayoutParams(mCircleDiameter, mCircleDiameter)
        marginLayoutParams.setMargins(0, mMargin, 0, mMargin)
        mCircleImageView!!.layoutParams = marginLayoutParams
        return mCircleImageView as CircleImageView
    }

    override fun setRefreshListener(onRefreshListener: SwipeRefreshPlus.OnRefreshListener) {
        this.mListener = onRefreshListener
    }

    override fun setLoadMore(loading: Boolean) {
        if (isLoading != loading) {
            isLoading = loading
            if (loading) {
                animateShowLoadMore(mLoadMoreListener)
            } else {
                animateHideLoadMore(null)
            }
        }
    }

    override fun stopAnimation() {
        mProgress!!.stop()
    }

    /**
     * 实际移动距离
     *
     * @param scrollDistance
     * @return
     */
    override fun move(scrollDistance: Int): Int {
        currentHeight += scrollDistance
        if (currentHeight > defaultHeight) {
            val result = scrollDistance - (currentHeight - defaultHeight)
            currentHeight = defaultHeight
            return result
        } else if (currentHeight < 0) {
            val result = scrollDistance - currentHeight
            currentHeight = 0
            return result
        }
        return scrollDistance
    }

    protected fun beginLoading() {
        mProgress!!.alpha = MAX_ALPHA
        mProgress!!.start()
        if (!isLoading && mListener != null) {
            isLoading = true
            mListener!!.onPullUpToRefresh()
        }
    }

    override fun reset() {
        isLoading = false
        if (mProgress!!.isRunning) {
            mProgress!!.stop()
        }
        currentHeight = 0
    }

    override fun finishPullRefresh(totalDistance: Float): Int {
        if (isLoadAnimation) {
            return 0
        }
        //beginLoading();
        animateShowLoadMore(mLoadMoreListener)
        return 0
    }


    fun setProgressColors(@ColorInt vararg colors: Int) {
        mProgress!!.setColorSchemeColors(*colors)
    }

    //显示加载更多view
    private fun animateShowLoadMore(listener: Animation.AnimationListener?) {
        mAnimationShowLoadMore.reset()
        mAnimationShowLoadMore.duration = ANIMATE_TO_TRIGGER_DURATION.toLong()
        mAnimationShowLoadMore.interpolator = mDecelerateInterpolator
        if (listener != null) {
            mAnimationShowLoadMore.setAnimationListener(listener)
        }
        mParent.clearAnimation()
        mParent.startAnimation(mAnimationShowLoadMore)
    }

    private fun animateHideLoadMore(listener: Animation.AnimationListener?) {
        mParent.clearAnimation()
        mParent.scrollBy(0, -currentHeight)
        reset()
    }

    override fun showNoMore(show: Boolean) {
        mShowNoMore = show
        //isLoading = false;
        if (mProgress!!.isRunning) {
            mProgress!!.stop()
        }
    }

    companion object {
        //默认circleimage大小
        internal val CIRCLE_DIAMETER = 40
        // Max amount of circle that can be filled by progress during swipe gesture,
        // where 1.0 is a full circle
        private val MAX_PROGRESS_ANGLE = .8f
        private val MAX_ALPHA = 255
        // Default background for the progress spinner
        private val CIRCLE_BG_LIGHT = -0x50506
        private val ANIMATE_TO_TRIGGER_DURATION = 200
        private val DECELERATE_INTERPOLATION_FACTOR = 2f
    }
}
