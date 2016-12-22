package com.gu.swiperefreshplush;

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
        datas.clear();
        datas.addAll(generatorData());
        view.onDataChange();
    }

    @Override
    public void loadMore() {
        datas.addAll(generatorData());
        view.onDataChange();
    }

    @Override
    public List getData() {
        return datas;
    }

    @Override
    public void bind() {
        datas.addAll(generatorData());
    }

    @Override
    public void unbind() {
        view=null;
    }

    private List generatorData(){
        Long num=System.currentTimeMillis();
        List<Integer> resul=new ArrayList<>();
        for(int i=0;i<6;i++){
            mRandom.setSeed(num);
            num=mRandom.nextLong();
            resul.add(dataSource[Math.abs(num.intValue())%5]);
        }
        return resul;
    }
}
