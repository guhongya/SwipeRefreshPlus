package com.gu.swiperefreshplus.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import com.gu.swiperefreshplus.R
import kotlinx.android.synthetic.main.fragment_list.*
import me.guhy.swiperefresh.SwipeRefreshPlus
import java.util.*
import kotlin.collections.ArrayList


class ListFragment : androidx.fragment.app.Fragment(), DemoContact.View {

    private lateinit var mSimplaeAdaptr: SimpleAdapter
    private lateinit var datas: List<Int>
    private var noMoreView: View? = null
    private lateinit var presenter: DemoContact.Presenter
    private var mAdapterDatas: ArrayList<Map<String, Int>> = ArrayList()
    private val from = arrayOf("src")
    private val to = intArrayOf(R.id.item_content)

    internal var count = 0
    internal var page = 3

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapterDatas = ArrayList()
        mSimplaeAdaptr = SimpleAdapter(activity, mAdapterDatas, R.layout.item_recycle_content, from, to)
        listContent.adapter = mSimplaeAdaptr
        DataPresenter(this)
        iniView()
    }


    override fun onDataChange() {
        mAdapterDatas.clear()
        for (src in datas) {
            val tem = HashMap<String, Int>(1)
            tem.put(from[0], src)
            mAdapterDatas.add(tem)
        }
        mSimplaeAdaptr.notifyDataSetChanged()
    }

    override fun onDataAdded(from: Int, to: Int) {
        onDataChange()
        listSwipeRefresh.setLoadMore(false)
    }

    private fun iniView() {
        listSwipeRefresh.setRefreshColorResources(*intArrayOf(R.color.colorPrimary))
        //listSwipeRefresh.setLoadViewController(new LoadMoreController(getActivity(), listSwipeRefresh));
        listSwipeRefresh.setOnRefreshListener(object : SwipeRefreshPlus.OnRefreshListener {
            override fun onPullDownToRefresh() {
                listSwipeRefresh.postDelayed({
                    presenter.refresh()
                    listSwipeRefresh.stopLoading()
                }, 1000)
            }

            override fun onPullUpToRefresh() {
                count++
                if (count >= page) {
                    listSwipeRefresh.showNoMore(true)

                } else {
                    listSwipeRefresh.postDelayed({ presenter.loadMore() }, 1500)
                }
            }
        })
        noMoreView = LayoutInflater.from(activity).inflate(R.layout.item_no_more, null, false)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        noMoreView!!.setPadding(10, 10, 10, 10)
        listSwipeRefresh.setNoMoreView(noMoreView as View, layoutParams)
        presenter.refresh()
    }

    override fun setPresenter(presenter: DemoContact.Presenter) {
        this.presenter = presenter
        presenter.bind()
        datas = presenter.getData()
    }

}// Required empty public constructor
