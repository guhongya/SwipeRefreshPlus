package me.guhy.swiperefresh

import android.view.View

/**
 * Created by guhongya on 2017/4/9.
 * loadview 的管理接口
 * loadview 相对简单 不需要自己实现滑动动画
 */

interface ILoadViewController {

    /**
     * loadview 默认高度
     *
     * @return 高度
     */
    fun getDefaultHeight(): Int

    /**
     * loadview 现在高度
     *
     * @return 高度
     */
    fun getCurrentHeight(): Int

    /**
     * 得到默认 loadmore view
     *
     * @return loadmore view
     */
    fun getDefaultView(): View

    /**
     * @return 加载view是否正在显示
     */
    fun isLoading(): Boolean

    /**
     * 重置LoadView状态
     */
    fun reset()

    /**
     * 创建LoadView
     *
     * @return loadview
     */
    fun create(): View

    /**
     * 向下滑动
     *
     * @param height 滑动距离
     * @return 父view需移动距离
     */
    fun move(height: Int): Int

    /**
     * 滑动结束
     *
     * @param totalDistance 手指离开屏幕时滑动总距离
     * @return 父view需移动距离
     */
    fun finishPullRefresh(totalDistance: Float): Int

    /**
     * 设置刷新监听回调
     *
     * @param mListener 监听回调
     */
    fun setRefreshListener(mListener: SwipeRefreshPlus.OnRefreshListener)

    /**
     * 是否显示nomore
     *
     * @param show
     */
    fun showNoMore(show: Boolean)

    /**
     * 设置loadmore 是否显示
     *
     * @param loading false:不显示 true: 显示
     */
    fun setLoadMore(loading: Boolean)

    /**
     * 停止所有动画
     */
    fun stopAnimation()
}
