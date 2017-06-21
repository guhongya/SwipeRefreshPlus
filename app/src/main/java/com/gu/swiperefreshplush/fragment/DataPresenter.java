package com.gu.swiperefreshplush.fragment;

import com.gu.swiperefreshplush.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by gu on 2016/12/22.
 */

public class DataPresenter implements DemoContact.Presenter {
    List<Integer> datas;
    private Random mRandom;
    private DemoContact.View view;
    private int[] dataSource=new int[]{R.mipmap.pic1,R.mipmap.pic2,R.mipmap.pic3,R.mipmap.pic4,R.mipmap.pic5};
    public DataPresenter(DemoContact.View view){
        this.view=view;
        datas=new ArrayList<>();
        mRandom=new Random();
        view.setPresenter(this);
    }
    @Override
    public void refresh() {
        //datas.clear();
        List<Integer> tem=generatorData(1);
        datas.addAll(0,tem);
       view.onDataAdded(0,tem.size());
    }

    @Override
    public void loadMore() {
        List<Integer> tem=generatorData(5);
        datas.addAll(tem);
       // view.onDataChange();
        view.onDataAdded(datas.size()-tem.size(),datas.size());
    }

    @Override
    public List getData() {
        return datas;
    }

    @Override
    public void bind() {
        datas.addAll(generatorData(10));
    }

    @Override
    public void unbind() {
        view=null;
    }

    private List generatorData(int n){
        Long num=System.currentTimeMillis();
        List<Integer> resul=new ArrayList<>();
        for(int i=0;i<n;i++){
            mRandom.setSeed(num);
            num=mRandom.nextLong();
            resul.add(dataSource[Math.abs(num.intValue())%5]);
        }
        return resul;
    }
}
