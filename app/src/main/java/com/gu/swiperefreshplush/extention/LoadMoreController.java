package com.gu.swiperefreshplush.extention;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gu.swiperefresh.ILoadViewController;
import com.gu.swiperefresh.SwipeRefreshPlus;
import com.gu.swiperefreshplush.R;

/**
 * Created by GUHY on 2017/4/18.
 */

public class LoadMoreController implements ILoadViewController {
    private int mDefaultHeight=80;
    private int mMaxHeight;
    private int mDefaultThreshold=60;

    private View mDefaultView;
    private Context mContext;

    private float mCurrentOffsetToTop;
    private SwipeRefreshPlus.OnRefreshListener mOnRefreshListener;
    private boolean isNoMore;
    private boolean isLoading;
    private View mParent;

    public LoadMoreController(Context context,View parent){
        mContext=context;
        this.mParent=parent;
        DisplayMetrics metrics=context.getResources().getDisplayMetrics();
        mMaxHeight=metrics.heightPixels;
        mDefaultHeight*=metrics.density;
        mDefaultThreshold*=metrics.density;
    }

    @Override
    public void reset() {
        mCurrentOffsetToTop=0;
    }

    @Override
    public View create() {
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_load_more,null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,mDefaultHeight));
        mDefaultView=view;
        return view;
    }

    @Override
    public int finishPullRefresh(float totalDistance) {
        if(mCurrentOffsetToTop>mDefaultThreshold||(totalDistance+mCurrentOffsetToTop)>mDefaultThreshold){
            if(!isNoMore&&!isLoading) {
                isLoading=true;
                mOnRefreshListener.onPullUpToRefresh();
            }
            int dis= (int) (mDefaultHeight-mCurrentOffsetToTop);
            mCurrentOffsetToTop=mDefaultHeight;
            return dis;
        }else{
            return 0;
        }
    }

    @Override
    public int getDefaultHeight() {
        return mDefaultHeight;
    }

    @Override
    public int getCurrentHeight() {
        return (int) mCurrentOffsetToTop;
    }

    @Override
    public int move(int height) {
        if(height>0) {
            if (mCurrentOffsetToTop < mMaxHeight && mCurrentOffsetToTop >= 0) {
                int dis = (int) (height * (mMaxHeight - mCurrentOffsetToTop) / (2 * mMaxHeight));
                if (mCurrentOffsetToTop + dis < mMaxHeight) {
                    mCurrentOffsetToTop += dis;
                    return dis;
                } else {
                    int result = (int) (mMaxHeight - mCurrentOffsetToTop);
                    mCurrentOffsetToTop = mMaxHeight;
                    return result;
                }
            }
        }else{
            mCurrentOffsetToTop+=height;
            if(mCurrentOffsetToTop<0){
                return (int) (height-mCurrentOffsetToTop);
            }else{
                return height;
            }
        }
        return 0;
    }

    @Override
    public void setRefreshListener(SwipeRefreshPlus.OnRefreshListener mListener) {
        mOnRefreshListener=mListener;
    }

    @Override
    public void showNoMore(boolean show) {
        isNoMore=show;
        isLoading=false;
    }

    @Override
    public View getDefaultView() {
        return mDefaultView;
    }

    @Override
    public void setLoadMore(boolean loading) {
        if (loading){
            mParent.scrollBy(0,mDefaultHeight);
        }else{
            mParent.scrollBy(0,-(int) mCurrentOffsetToTop);
            reset();
        }
        isLoading=loading;
    }

    @Override
    public boolean isLoading() {
        return isLoading;
    }

}
