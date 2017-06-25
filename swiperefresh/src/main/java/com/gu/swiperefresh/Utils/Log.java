package com.gu.swiperefresh.Utils;

/**
 * Created by gu on 2016/12/23.
 */

public class Log {
    private static final boolean DEBUG = false;

    public static void i(String tag, String info) {
        if (DEBUG)
            android.util.Log.i(tag, info);
    }

    public static void d(String tag, String info) {
        if (DEBUG)
            android.util.Log.d(tag, info);
    }

    public static void e(String tag, String info) {
        if (DEBUG)
            android.util.Log.e(tag, info);
    }

    public static void v(String tag, String info) {
        if (DEBUG)
            android.util.Log.v(tag, info);
    }
}
