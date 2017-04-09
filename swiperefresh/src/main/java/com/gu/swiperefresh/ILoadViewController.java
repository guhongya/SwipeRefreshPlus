package com.gu.swiperefresh;

import android.view.View;

/**
 * Created by guhongya on 2017/4/9.
 * loadview 的管理接口
 */

public interface ILoadViewController {
    void reset();

    View create();

    void showLoadMore();

    int getMaxHeight();

    int getCurrentHeight();

    int move(int height);

    void setRefreshListener(SwipeRefreshPlush.OnRefreshListener mListener);

    void setProgressColors(int[] colors);

    void showNoMore(boolean show);

    View getDefaultView();
}
