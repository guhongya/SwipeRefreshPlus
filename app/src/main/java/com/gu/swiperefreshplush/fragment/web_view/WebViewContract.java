package com.gu.swiperefreshplush.fragment.web_view;


/**
 * Created by GUHY on 2017/3/9.
 */

class WebViewContract {
    interface View {
    }

    interface Presenter {
        void subscribe();
        void unSubscribe();
    }
}
