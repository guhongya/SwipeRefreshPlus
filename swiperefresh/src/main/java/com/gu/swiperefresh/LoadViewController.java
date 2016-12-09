package com.gu.swiperefresh;

/**
 * Created by Guhy on 2016/12/7.
 */

public class LoadViewController {

    private int scroll;
    private int maxHeight;
    private int parentHeight;

    public LoadViewController(int maxHeight){
        this.maxHeight=maxHeight;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    private boolean show;
    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }
    public boolean canScroll(){
        return maxHeight-scroll>0;
    }
    public void move(int y){
        scroll+=y;
    }
    public int getCurrentPosition(){
        if(show)
        return maxHeight-scroll;
        else
            return 0;
    }
    public int getCurrentHeight(){
        return scroll;
    }

    public int getParentHeight() {
        return parentHeight;
    }
    public void reset(){
        scroll=0;
    }
    public void setParentHeight(int parentHeight) {
        this.parentHeight = parentHeight;
    }
}
