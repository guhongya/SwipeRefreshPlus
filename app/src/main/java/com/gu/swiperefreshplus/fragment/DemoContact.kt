package com.gu.swiperefreshplus.fragment;


/**
 * Created by gu on 2016/12/22.
 */

 class DemoContact {
    interface View{
        fun onDataChange();
        fun onDataAdded(from:Int,to:Int);
        fun setPresenter(presenter:Presenter );
    }
    interface Presenter{
        fun refresh();
        fun loadMore();
        fun getData():List<Int>;
        fun bind();
        fun unbind();
    }
}
