package com.gu.swiperefreshplus

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager

/**
 * Created by guhongya on 2017/11/13.
 */
class SimpleViewHolder(private val rootView: View) : RecyclerView.ViewHolder(rootView) {
    private val mImageView: ImageView

    init {
        mImageView = rootView.findViewById<View>(R.id.item_content) as ImageView
    }

    fun setData(id: Int) {
        val lp = rootView.layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            lp.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.flexGrow = 1.0f
        } else {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        mImageView.setImageResource(id)
    }
}