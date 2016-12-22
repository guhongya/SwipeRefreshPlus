package com.gu.swiperefreshplush;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apkfuns.logutils.LogUtils;
import com.gu.swiperefresh.SwipeRefreshPlush;

import java.util.ArrayList;
import java.util.List;



public class RecycleFragment extends Fragment implements DemoContact.View{
    private RecyclerView recycleContent;
    private SimpleRecycleAdapter recycleAdapter;
    private SwipeRefreshPlush swipeRefreshPlush;
    private List<Integer> datas;
    int count=0;
    int page=2;
    View noMoreView;
    DemoContact.Presenter presenter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_recycle, container, false);
        recycleContent= (RecyclerView) view.findViewById(R.id.recycle_content);
        swipeRefreshPlush= (SwipeRefreshPlush) view.findViewById(R.id.swipe_refresh);
        iniView();
        return view;
    }
    private void iniView(){

        recycleContent.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycleAdapter=new SimpleRecycleAdapter();
        recycleAdapter.setData(datas);
        recycleContent.setAdapter(recycleAdapter);
        swipeRefreshPlush.setRefreshColorResources(new int[]{R.color.colorPrimary});
        swipeRefreshPlush.setOnScrollListener(new SwipeRefreshPlush.OnScrollListener() {
            @Override
            public void onRefresh() {
                swipeRefreshPlush.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        presenter.refresh();
                        swipeRefreshPlush.setRefresh(false);
                        swipeRefreshPlush.showNoMore(false);
                    }
                },1000);
            }

            @Override
            public void onLoadMore() {
                LogUtils.d("onloading");
                count++;
                if (count >= page) {
                    swipeRefreshPlush.showNoMore(true);

                } else {
                    swipeRefreshPlush.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                           presenter.loadMore();
                            swipeRefreshPlush.setLoadMore(false);
                        }
                    }, 1500);
                }
            }
        });
        noMoreView = LayoutInflater.from(getActivity()).inflate(R.layout.item_no_more, null, false);
        ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        noMoreView.setPadding(10,10,10,10);
        swipeRefreshPlush.setNoMoreView(noMoreView,layoutParams);
    }

    @Override
    public void onDataChange() {
        recycleAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(DemoContact.Presenter presenter) {
        this.presenter=presenter;
        presenter.bind();
        datas=presenter.getData();
    }
}
