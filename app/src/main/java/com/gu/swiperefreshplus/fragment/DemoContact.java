package com.gu.swiperefreshplus.fragment;

import java.util.List;

/**
 * Created by gu on 2016/12/22.
 */

public class DemoContact {
    interface View{
        void onDataChange();
        void onDataAdded(int from,int to);
        void setPresenter(Presenter presenter);
    }
    interface Presenter{
        void refresh();
        void loadMore();
        List getData();
        void bind();
        void unbind();
    }
}
