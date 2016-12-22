package com.gu.swiperefreshplush;

import android.app.Fragment;
import android.content.Intent;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.apkfuns.logutils.LogUtils;
import com.gu.swiperefresh.SwipeRefreshPlush;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction().add(R.id.main_content,new ReplaceFragment()).commit();

    }
   public void showList(View view){
       if(getFragmentManager().getBackStackEntryCount()>0){
           getFragmentManager().popBackStack();
       }
       ListFragment listFragment=new ListFragment();
       new DataPresenter(listFragment);
       showFragment(listFragment);
   }
    public void showRecycle(View view){
       if(getFragmentManager().getBackStackEntryCount()>0){
           getFragmentManager().popBackStack();
       }
        RecycleFragment fragment=new RecycleFragment();
        showFragment(fragment);
        new DataPresenter(fragment);
    }
    private void showFragment(Fragment fragment){
        getFragmentManager().beginTransaction().replace(R.id.main_content,fragment).addToBackStack(fragment.getClass().toString()).commit();
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount()>0){
            getFragmentManager().popBackStack();
        }else {
            super.onBackPressed();
        }
    }
}
