package com.gu.swiperefresh;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by GUHY on 2017/6/6.
 */
@IntDef({ZIndex.BOTTOM, ZIndex.TOP, ZIndex.NORMAL})
@Retention(RetentionPolicy.SOURCE)
public @interface ZIndex {
    int NORMAL = 0;
    int TOP = 1;
    int BOTTOM = 2;
}
