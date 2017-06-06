package com.gu.swiperefresh;

import android.view.View;

/**
 * Created by guhongya on 2017/4/9.
 * loadview 的管理接口
 */

public interface ILoadViewController {
    void reset();

    View create();

    int finishPullRefresh(float totalDistance);

    int getDefaultHeight();

    int getCurrentHeight();

    int move(int height);

    void setRefreshListener(SwipeRefreshPlus.OnRefreshListener mListener);

    void showNoMore(boolean show);

    View getDefaultView();

    void setLoadMore(boolean loading);

    boolean isLoading();
}
