package me.guhy.swiperefresh.Utils

/**
 * Created by gu on 2016/12/23.
 */

object Log {
    private val DEBUG = false

    @JvmStatic fun i(tag: String, info: String) {
        if (DEBUG) {
            android.util.Log.i(tag, info)
        }
    }

    @JvmStatic fun d(tag: String, info: String) {
        if (DEBUG) {
            android.util.Log.d(tag, info)
        }
    }

    @JvmStatic fun e(tag: String, info: String) {
        if (DEBUG) {
            android.util.Log.e(tag, info)
        }
    }

    @JvmStatic fun v(tag: String, info: String) {
        if (DEBUG) {
            android.util.Log.v(tag, info)
        }
    }
}
