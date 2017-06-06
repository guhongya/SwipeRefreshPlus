package com.gu.swiperefresh;

import android.view.View;

import com.gu.swiperefresh.Utils.Size;

/**
 * Created by guhongya on 2017/4/9.
 * refreshview 的管理接口
 */

public interface IRefreshViewController {
    void reset();

    View create();

    @ZIndex
    int getZIndex();

    int getCurrentTargetOffsetTop();

    Size getRefreshViewSize();

    boolean isRefresh();

    float showPullRefresh(float overscrollTop);

    /**
     * 根据下拉距离，判断是否应该刷新
     *
     * @param overscrollTop 下拉总距离
     */
    float finishPullRefresh(float overscrollTop);

    void startProgress();

    void setTargetOffsetTopAndBottom(int i, boolean b);

    void setRefreshListener(SwipeRefreshPlus.OnRefreshListener mListener);

    void setRefreshing(boolean refresh);


}
