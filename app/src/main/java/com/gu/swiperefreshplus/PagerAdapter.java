package com.gu.swiperefreshplus;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.gu.swiperefreshplus.fragment.ListFragment;
import com.gu.swiperefreshplus.fragment.NormalViewFragment;
import com.gu.swiperefreshplus.fragment.RecycleFragment;

/**
 * Created by GUHY on 2017/4/5.
 */

public class PagerAdapter extends FragmentPagerAdapter {
    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment=null;
        switch (position){
            case 0:
                fragment= new RecycleFragment();
                break;
            case 1:
                fragment=new ListFragment();
                break;
            case 2:
                fragment=new NormalViewFragment();
                break;

        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "RecycleView";
            case 1:
                return "ListView";
            case 2:
                return "NormalView";
        }
        return "";
    }
}
