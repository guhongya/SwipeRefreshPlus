package me.guhy.swiperefresh

import android.view.View

/**
 * Created by guhongya on 2017/4/9.
 * refreshview 的管理接口
 * RefreshViewController 相对比较复杂，需要实现此接口的类
 * 创建refreshview,并负责refreshview的显示，消失动画
 */

interface IRefreshViewController {

    /**
     * @return refreshview 的z轴顺序
     */
    fun getZIndex(): Int

    /**
     * @return refreshView 现在距顶部的距离
     */
    fun getCurrentTargetOffsetTop(): Int

    /**
     * 是否正在刷新
     *
     * @return true:正在刷新 false:
     */
    fun isRefresh(): Boolean

    /**
     * 重置refreshView 状态
     */
    fun reset()

    /**
     * 创建refreshView
     *
     * @return refreshView
     */
    fun create(): View

    /**
     * 开始下拉
     */
    fun startPulling()


    /**
     * 下拉中，refreshview显示动画在此实现
     *
     * @param overscrollTop 下拉总距离
     */
    fun showPullRefresh(overscrollTop: Float)

    /**
     * 下拉结束，根据下拉距离，判断是否应该刷新
     *
     * @param overscrollTop 下拉总距离
     */
    fun finishPullRefresh(overscrollTop: Float)

    /**
     * 设置refreview 的top值
     *
     * @param i refreshview 距离顶部的距离
     * @param b 是否提示父view 重绘
     */
    fun setTargetOffsetTopAndBottom(i: Int, b: Boolean)

    /**
     * 设置refresh 监听回调
     *
     * @param mListener
     */
    fun setRefreshListener(mListener: SwipeRefreshPlus.OnRefreshListener)

    /**
     * 设置 refresh 状态
     *
     * @param refresh
     */
    fun setRefreshing(refresh: Boolean)

}
