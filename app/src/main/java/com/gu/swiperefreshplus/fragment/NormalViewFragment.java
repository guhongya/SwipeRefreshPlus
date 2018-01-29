package com.gu.swiperefreshplus.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apkfuns.logutils.LogUtils;
import com.gu.swiperefreshplus.R;

import me.guhy.swiperefresh.SwipeRefreshPlus;

/**
 * A simple {@link Fragment} subclass.
 */
public class NormalViewFragment extends Fragment {
    private SwipeRefreshPlus mNormalRefresh;

    public NormalViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_normal_view, container, false);
        mNormalRefresh= (SwipeRefreshPlus) view.findViewById(R.id.normal_view_refresh);
        //mNormalRefresh.setRefreshViewController(new MRefreshViewController(container.getContext(),mNormalRefresh));
        //mNormalRefresh.setRefresh(true);
        mNormalRefresh.setOnRefreshListener(new SwipeRefreshPlus.OnRefreshListener() {
            @Override
            public void onPullDownToRefresh() {
                LogUtils.d("refresh");
                mNormalRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNormalRefresh.setRefresh(false);
                    }
                },5000);
            }

            @Override
            public void onPullUpToRefresh() {

            }
        });

        return view;
    }

}
