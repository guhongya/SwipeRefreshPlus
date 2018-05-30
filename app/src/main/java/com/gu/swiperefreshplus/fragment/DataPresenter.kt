package com.gu.swiperefreshplus.fragment

import com.gu.swiperefreshplus.R
import java.util.Random
import kotlin.collections.ArrayList

/**
 * Created by gu on 2016/12/22.
 */

class DataPresenter(var view: DemoContact.View) : DemoContact.Presenter {
    internal var datas: ArrayList<Int> = ArrayList()
    private val mRandom: Random = Random()
    private val dataSource = intArrayOf(R.mipmap.pic1, R.mipmap.pic2, R.mipmap.pic3, R.mipmap.pic4, R.mipmap.pic5)
    private var mView:DemoContact.View?=null

    init {
        view.setPresenter(this)
        mView=view
    }

    override fun refresh() {
        val tem = generatorData(1)
        datas.addAll(0, tem)
        mView?.onDataAdded(0, tem.size)
    }

    override fun loadMore() {
        val tem = generatorData(5)
        datas.addAll(tem)
        mView?.onDataAdded(datas.size - tem.size, datas.size)
    }

    override fun getData(): List<Int> {
        return datas
    }

    override fun bind() {
        datas.addAll(generatorData(10))
    }

    override fun unbind() {
        mView=null
    }

    private fun generatorData(n: Int): List<Int> {
        var num: Long? = System.currentTimeMillis()
        val resul = ArrayList<Int>()
        for (i in 0 until n) {
            mRandom.setSeed(num!!)
            num = mRandom.nextLong()
            resul.add(dataSource[Math.abs(num.toInt()) % 5])
        }
        return resul
    }
}
