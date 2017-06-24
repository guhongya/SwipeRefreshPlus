package com.gu.swiperefreshplush;

import android.content.Intent;
import android.net.LinkAddress;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.List;

/**
 * Created by Guhy on 2016/11/24.
 */

public class SimpleRecycleAdapter extends RecyclerView.Adapter<SimpleRecycleAdapter.SimpleViewHolder> {
    private List<Integer> data;
    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle_content,parent,false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        holder.setData(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data==null?0:data.size();
    }
    public void setData(List<Integer> data){
        this.data=data;
    }
    static class SimpleViewHolder  extends RecyclerView.ViewHolder{
        private ImageView mImageView;
        private View rootView;
        public SimpleViewHolder(View itemView) {
            super(itemView);
            this.rootView=itemView;
            mImageView= (ImageView) itemView.findViewById(R.id.item_content);
        }
        public void setData(int id){
           // mImageView.setImageDrawable(drawable);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) rootView.getLayoutParams();
            if (lp instanceof FlexboxLayoutManager.LayoutParams) {
                FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
                flexboxLp.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                flexboxLp.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                flexboxLp.setFlexGrow(1.0f);
            }else{
                lp.width=ViewGroup.LayoutParams.MATCH_PARENT;
            }
            Glide.with(mImageView)
                    .asDrawable()
                    .load(id)
                    .into(mImageView);
        }
    }
}
