package me.guhy.swiperefresh;

import android.view.View;

/**
 * Created by guhongya on 2017/4/9.
 * loadview 的管理接口
 * loadview 相对简单 不需要自己实现滑动动画
 */

public interface ILoadViewController {
    /**
     * 重置LoadView状态
     */
    void reset();

    /**
     * 创建LoadView
     *
     * @return loadview
     */
    View create();

    /**
     * loadview 默认高度
     *
     * @return 高度
     */
    int getDefaultHeight();

    /**
     * loadview 现在高度
     *
     * @return 高度
     */
    int getCurrentHeight();

    /**
     * 向下滑动
     *
     * @param height 滑动距离
     * @return 父view需移动距离
     */
    int move(int height);

    /**
     * 滑动结束
     *
     * @param totalDistance 手指离开屏幕时滑动总距离
     * @return 父view需移动距离
     */
    int finishPullRefresh(float totalDistance);

    /**
     * 设置刷新监听回调
     *
     * @param mListener 监听回调
     */
    void setRefreshListener(SwipeRefreshPlus.OnRefreshListener mListener);

    /**
     * 是否显示nomore
     *
     * @param show
     */
    void showNoMore(boolean show);

    /**
     * 得到默认 loadmore view
     *
     * @return loadmore view
     */
    View getDefaultView();

    /**
     * 设置loadmore 是否显示
     *
     * @param loading false:不显示 true: 显示
     */
    void setLoadMore(boolean loading);

    /**
     * @return 加载view是否正在显示
     */
    boolean isLoading();

    /**
     * 停止所有动画
     */
    void stopAnimation();
}
