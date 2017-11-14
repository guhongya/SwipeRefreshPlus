package com.gu.swiperefreshplus.extention

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gu.swiperefreshplus.R
import me.guhy.swiperefresh.ILoadViewController
import me.guhy.swiperefresh.SwipeRefreshPlus

/**
 * Created by GUHY on 2017/4/18.
 */

class LoadMoreController(private val mContext: Context, private val mParent: View) : ILoadViewController {

    private var mDefaultHeight = 80
    private val mMaxHeight: Int
    private var mDefaultThreshold = 60

    private var mDefaultView: View? = null

    private var mCurrentOffsetToTop: Float = 0.toFloat()
    private var mOnRefreshListener: SwipeRefreshPlus.OnRefreshListener? = null
    private var isNoMore: Boolean = false
    override var isLoading: Boolean = false


    init {
        val metrics = mContext.resources.displayMetrics
        mMaxHeight = metrics.heightPixels
        mDefaultHeight *= metrics.density.toInt()
        mDefaultThreshold *= metrics.density.toInt()
    }

    override fun reset() {
        mCurrentOffsetToTop = 0f
    }

    override fun create(): View {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_load_more, null)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mDefaultHeight)
        mDefaultView = view
        return view
    }

    override fun finishPullRefresh(totalDistance: Float): Int {
        if (mCurrentOffsetToTop > mDefaultThreshold || totalDistance + mCurrentOffsetToTop > mDefaultThreshold) {
            if (!isNoMore && !isLoading) {
                isLoading = true
                mOnRefreshListener!!.onPullUpToRefresh()
            }
            val dis = (mDefaultHeight - mCurrentOffsetToTop).toInt()
            mCurrentOffsetToTop = mDefaultHeight.toFloat()
            return dis
        } else {
            return 0
        }
    }

    override var defaultHeight: Int
        get() = mDefaultHeight
        set(value) {}
    override var currentHeight: Int
        get() = mCurrentOffsetToTop.toInt()
        set(value) {}
    override var defaultView: View
        get() = mDefaultView as View
        set(value) {}

//    override fun getDefaultHeight(): Int {
//        return mDefaultHeight
//    }
//
//    override fun getCurrentHeight(): Int {
//        return mCurrentOffsetToTop.toInt()
//    }

    override fun move(height: Int): Int {
        if (height > 0) {
            if (mCurrentOffsetToTop < mMaxHeight && mCurrentOffsetToTop >= 0) {
                val dis = (height * (mMaxHeight - mCurrentOffsetToTop) / (2 * mMaxHeight)).toInt()
                if (mCurrentOffsetToTop + dis < mMaxHeight) {
                    mCurrentOffsetToTop += dis.toFloat()
                    return dis
                } else {
                    val result = (mMaxHeight - mCurrentOffsetToTop).toInt()
                    mCurrentOffsetToTop = mMaxHeight.toFloat()
                    return result
                }
            }
        } else {
            mCurrentOffsetToTop += height.toFloat()
            return if (mCurrentOffsetToTop < 0) {
                (height - mCurrentOffsetToTop).toInt()
            } else {
                height
            }
        }
        return 0
    }

    override fun setRefreshListener(mListener: SwipeRefreshPlus.OnRefreshListener) {
        mOnRefreshListener = mListener
    }

    override fun showNoMore(show: Boolean) {
        isNoMore = show
        isLoading = false
    }

//    override fun getDefaultView(): View? {
//        return mDefaultView
//    }

    override fun setLoadMore(loading: Boolean) {
        if (isLoading != loading) {
            isLoading = loading
            if (isLoading) {
                mParent.scrollBy(0, mDefaultHeight)
            } else {
                mParent.scrollBy(0, -mCurrentOffsetToTop.toInt())
                reset()
            }
            isLoading = loading
        }
    }
//
//    override fun isLoading(): Boolean {
//        return isLoading
//    }

    override fun stopAnimation() {
        mDefaultView!!.visibility = View.INVISIBLE
    }

}
