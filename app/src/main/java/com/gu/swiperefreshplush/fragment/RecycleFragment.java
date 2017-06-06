package com.gu.swiperefreshplush.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.apkfuns.logutils.LogUtils;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.gu.swiperefresh.SwipeRefreshPlus;
import com.gu.swiperefreshplush.R;
import com.gu.swiperefreshplush.SimpleRecycleAdapter;
import com.gu.swiperefreshplush.extention.LoadMoreController;

import java.util.List;



public class RecycleFragment extends Fragment implements DemoContact.View {
    private RecyclerView recycleContent;

    private SimpleRecycleAdapter recycleAdapter;
    private SwipeRefreshPlus swipeRefreshPlush;
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
        swipeRefreshPlush= (SwipeRefreshPlus) view.findViewById(R.id.swipe_refresh);
        swipeRefreshPlush.setLoadViewController(new LoadMoreController(container.getContext(),swipeRefreshPlush));
        new DataPresenter(this);
        setHasOptionsMenu(true);
        iniView();
        return view;
    }
    private void iniView(){
//        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
//        layoutManager.setFlexDirection(FlexDirection.ROW);
//        layoutManager.setJustifyContent(JustifyContent.FLEX_END);
//        recycleContent.setLayoutManager(layoutManager);
       recycleContent.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycleAdapter=new SimpleRecycleAdapter();
        recycleAdapter.setData(datas);
        recycleContent.setAdapter(recycleAdapter);
        swipeRefreshPlush.setRefreshColorResources(new int[]{R.color.colorPrimary});
        swipeRefreshPlush.setOnRefreshListener(new SwipeRefreshPlus.OnRefreshListener() {
            @Override
            public void onPullDownToRefresh() {
                swipeRefreshPlush.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        presenter.refresh();
                        swipeRefreshPlush.setRefresh(false);
                        swipeRefreshPlush.showNoMore(false);
                        LogUtils.d(swipeRefreshPlush.getLoadViewController().isLoading());
                    }
                },1000);
            }

            @Override
            public void onPullUpToRefresh() {
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.recycleview_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.liner_layout:
                recycleContent.setLayoutManager(new LinearLayoutManager(getActivity()));
                break;
            case R.id.grid_layout:
                recycleContent.setLayoutManager(new GridLayoutManager(getActivity(),2));
                break;
            case R.id.staggered_grid_layout:
                recycleContent.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
                break;
        }
        return true;
    }
}
