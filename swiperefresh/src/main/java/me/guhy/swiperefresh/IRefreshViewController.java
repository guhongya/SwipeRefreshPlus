package me.guhy.swiperefresh;

import android.view.View;

/**
 * Created by guhongya on 2017/4/9.
 * refreshview 的管理接口
 * RefreshViewController 相对比较复杂，需要实现此接口的类
 * 创建refreshview,并负责refreshview的显示，消失动画
 */

public interface IRefreshViewController {
    void reset();

    View create();

    /**
     *
     * @return refreshview 的z轴顺序
     */
    @ZIndex
    int getZIndex();

    int getCurrentTargetOffsetTop();

    boolean isRefresh();

    /**
     * 开始下拉
     */
    void startPulling();


    /**
     * 下拉中，refreshview显示动画在此实现
     * @param overscrollTop 下拉总距离
     */
    void showPullRefresh(float overscrollTop);

    /**
     * 下拉结束，根据下拉距离，判断是否应该刷新
     *
     * @param overscrollTop 下拉总距离
     */
    void finishPullRefresh(float overscrollTop);

    /**
     * 设置refreview 的top值
     * @param i
     * @param b
     */
    void setTargetOffsetTopAndBottom(int i, boolean b);

    void setRefreshListener(SwipeRefreshPlus.OnRefreshListener mListener);

    void setRefreshing(boolean refresh);

}
