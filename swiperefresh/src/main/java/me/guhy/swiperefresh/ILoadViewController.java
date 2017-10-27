package me.guhy.swiperefresh;

import android.view.View;

/**
 * Created by guhongya on 2017/4/9.
 * loadview 的管理接口
 * loadview 相对简单 不需要自己实现滑动动画
 */

public interface ILoadViewController {

    void reset();

    View create();

    int getDefaultHeight();

    int getCurrentHeight();

    /**
     * 向下滑动
     * @param height 滑动距离
     * @return 父view需移动距离
     */
    int move(int height);

    /**
     * 滑动结束
     * @param totalDistance 手指离开屏幕时滑动总距离
     * @return 父view需移动距离
     */
    int finishPullRefresh(float totalDistance);

    void setRefreshListener(SwipeRefreshPlus.OnRefreshListener mListener);

    void showNoMore(boolean show);

    View getDefaultView();

    void setLoadMore(boolean loading);

    boolean isLoading();

    /**
     * 停止所有动画
     */
    void stopAnimation();
}
