package me.guhy.swiperefresh;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GUHY on 2017/4/10.
 */

public class TestAdapter extends  RecyclerView.Adapter<TestAdapter.TestViewHolder>{
    private List<RecyclerView.LayoutParams> layoutParamses;
    public TestAdapter(){
        this(new ArrayList<RecyclerView.LayoutParams>());
    }
    public TestAdapter(List<RecyclerView.LayoutParams> layoutParamses ){
        this.layoutParamses=layoutParamses;
    }
    @Override
    public TestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView view=new TextView(parent.getContext());
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TestViewHolder holder, int position) {
        holder.textView.setText(""+position);
        holder.textView.setLayoutParams(layoutParamses.get(position));
    }

    @Override
    public int getItemCount() {
        return layoutParamses.size();
    }
    void addItem(RecyclerView.LayoutParams layoutParams){
        this.layoutParamses.add(layoutParams);
    }
    static class TestViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public TestViewHolder(View itemView) {
            super(itemView);
            textView= (TextView) itemView;
        }
    }
}
