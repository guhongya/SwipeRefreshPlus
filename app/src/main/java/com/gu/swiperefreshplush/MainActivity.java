package com.gu.swiperefreshplush;

import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.apkfuns.logutils.LogUtils;
import com.gu.swiperefresh.SwipeRefreshPlush;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recycleContent;
    private SimpleRecycleAdapter recycleAdapter;
    private SwipeRefreshPlush swipeRefreshPlush;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recycleContent= (RecyclerView) findViewById(R.id.content);
        swipeRefreshPlush= (SwipeRefreshPlush) findViewById(R.id.swipe_refresh);
        iniView();
    }
    private void iniView(){
        recycleContent.setLayoutManager(new LinearLayoutManager(this));

        recycleAdapter=new SimpleRecycleAdapter();
        recycleAdapter.setData(generatorData());
        recycleContent.setAdapter(recycleAdapter);
        swipeRefreshPlush.setColorSchemeResources(new int[]{R.color.colorPrimary});
        swipeRefreshPlush.setOnScrollListener(new SwipeRefreshPlush.OnScrollListener() {
            @Override
            public void onRefresh() {
                generatorData();
                swipeRefreshPlush.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshPlush.setRefresh(false);
                        LogUtils.d("heigth:"+recycleContent.getHeight()+"scrolly"+recycleContent.getScrollY());
                    }
                },1000);
            }

            @Override
            public void onLoadMore() {
                LogUtils.d("load more");
            }
        });

    }
    private List generatorData(){
        List<String> resul=new ArrayList<>();
        for(int i=0;i<30;i++){
            resul.add("ewetwes");
        }
        return resul;
    }
}
