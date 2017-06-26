package me.guhy.swiperefresh;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by guhongya on 2017/5/17.
 */

@IntDef({SwipeRefreshMode.MODE_BOTH, SwipeRefreshMode.MODE_LOADMODE, SwipeRefreshMode.MODE_REFRESH_ONLY, SwipeRefreshMode.MODE_NONE})
@Retention(RetentionPolicy.SOURCE)
public @interface SwipeRefreshMode {
    int MODE_BOTH = 1;//刷新和下拉加载更多模式
    int MODE_REFRESH_ONLY = 2;//刷新
    int MODE_LOADMODE = 3;//加载更多
    int MODE_NONE = 4;//即不能加载更多也不能下拉刷新
}
