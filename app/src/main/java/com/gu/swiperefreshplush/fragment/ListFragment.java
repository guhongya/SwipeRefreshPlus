package com.gu.swiperefreshplush.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.gu.swiperefresh.SwipeRefreshPlus;
import com.gu.swiperefreshplush.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ListFragment extends Fragment implements DemoContact.View {
    private ListView mListView;
    private SimpleAdapter mSimplaeAdaptr;
    private SwipeRefreshPlus mSwipeRefreshPlus;
    private List<Integer> datas;
    int count=0;
    int page=3;
    View noMoreView;
    private DemoContact.Presenter presenter;
    private ArrayList<Map<String,Integer>> mAdapterDatas;
    private String[] from=new String[]{"src"};
    private int[] to=new int[]{R.id.item_content};
    public ListFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_list, container, false);
       // View view=inflater.inflate(R.layout.fragment_list, container, false);
        mListView=(ListView) view.findViewById(R.id.list_content);
        mSwipeRefreshPlus =(SwipeRefreshPlus) view.findViewById(R.id.list_swipe_refresh);
        mAdapterDatas=new ArrayList<>();
        mSimplaeAdaptr=new SimpleAdapter(getActivity(),mAdapterDatas,R.layout.item_recycle_content,from,to);
        mListView.setAdapter(mSimplaeAdaptr);
        new DataPresenter(this);
        iniView();
        return view;
    }


    @Override
    public void onDetach() {
        mSwipeRefreshPlus.setEnabled(false);
        super.onDetach();
    }

    @Override
    public void onDataChange() {
        mAdapterDatas.clear();
        for(Integer src:datas){
            Map<String ,Integer> tem=new HashMap<>(1);
            tem.put(from[0],src);
            mAdapterDatas.add(tem);
        }
        mSimplaeAdaptr.notifyDataSetChanged();
    }
    private void iniView(){
        mSwipeRefreshPlus.setRefreshColorResources(new int[]{R.color.colorPrimary});
        mSwipeRefreshPlus.setOnRefreshListener(new SwipeRefreshPlus.OnRefreshListener() {
            @Override
            public void onPullDownToRefresh() {
                mSwipeRefreshPlus.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        presenter.refresh();
                        mSwipeRefreshPlus.setRefresh(false);
                        mSwipeRefreshPlus.showNoMore(false);
                    }
                },1000);
            }

            @Override
            public void onPullUpToRefresh() {
                count++;
                if (count >= page) {
                    mSwipeRefreshPlus.showNoMore(true);

                } else {
                    mSwipeRefreshPlus.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            presenter.loadMore();
                            mSwipeRefreshPlus.setLoadMore(false);
                        }
                    }, 1500);
                }
            }
        });
        noMoreView = LayoutInflater.from(getActivity()).inflate(R.layout.item_no_more, null, false);
        ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        noMoreView.setPadding(10,10,10,10);
        mSwipeRefreshPlus.setNoMoreView(noMoreView,layoutParams);
        presenter.refresh();
    }
    @Override
    public void setPresenter(DemoContact.Presenter presenter) {
        this.presenter=presenter;
        presenter.bind();
        datas=presenter.getData();
    }

}
