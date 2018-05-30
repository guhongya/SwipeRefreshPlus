package com.gu.swiperefreshplus

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Created by Guhy on 2016/11/24.
 */

class SimpleRecycleAdapter : RecyclerView.Adapter<SimpleViewHolder>() {
    private lateinit var data: List<Int>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycle_content, parent, false)
        return SimpleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        holder.setData(data[position])
    }

    override fun getItemCount(): Int {
        return if (data == null) 0 else data.size
    }

    fun setData(data: List<Int>) {
        this.data = data
    }


}
