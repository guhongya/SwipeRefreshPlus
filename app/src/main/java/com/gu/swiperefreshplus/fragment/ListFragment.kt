package com.gu.swiperefreshplus.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import com.gu.swiperefreshplus.R
import me.guhy.swiperefresh.SwipeRefreshPlus
import java.util.*


class ListFragment : Fragment(), DemoContact.View {
    private var mListView: ListView? = null
    private var mSimplaeAdaptr: SimpleAdapter? = null
    private var mSwipeRefreshPlus: SwipeRefreshPlus? = null
    private var datas: List<Int>? = null
    internal var count = 0
    internal var page = 3
    private var noMoreView: View?=null
    private var presenter: DemoContact.Presenter? = null
    private var mAdapterDatas: ArrayList<Map<String, Int>>? = null
    private val from = arrayOf("src")
    private val to = intArrayOf(R.id.item_content)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_list, container, false)
        // View view=inflater.inflate(R.layout.fragment_list, container, false);
        mListView = view.findViewById<View>(R.id.list_content) as ListView
        mSwipeRefreshPlus = view.findViewById<View>(R.id.list_swipe_refresh) as SwipeRefreshPlus
        mAdapterDatas = ArrayList()
        mSimplaeAdaptr = SimpleAdapter(activity, mAdapterDatas, R.layout.item_recycle_content, from, to)
        mListView!!.adapter = mSimplaeAdaptr
        DataPresenter(this)
        iniView()
        return view
    }


    override fun onDetach() {
        mSwipeRefreshPlus!!.isEnabled = false
        super.onDetach()
    }

    override fun onDataChange() {
        mAdapterDatas!!.clear()
        for (src in datas!!) {
            val tem = HashMap<String, Int>(1)
            tem.put(from[0], src)
            mAdapterDatas!!.add(tem)
        }
        mSimplaeAdaptr!!.notifyDataSetChanged()
    }

    override fun onDataAdded(from: Int, to: Int) {
        onDataChange()
        mSwipeRefreshPlus!!.setLoadMore(false)
    }

    private fun iniView() {
        mSwipeRefreshPlus!!.setRefreshColorResources(*intArrayOf(R.color.colorPrimary))
        //mSwipeRefreshPlus.setLoadViewController(new LoadMoreController(getActivity(), mSwipeRefreshPlus));
        mSwipeRefreshPlus!!.setOnRefreshListener(object : SwipeRefreshPlus.OnRefreshListener {
            override fun onPullDownToRefresh() {
                mSwipeRefreshPlus!!.postDelayed({
                    presenter!!.refresh()
                    mSwipeRefreshPlus!!.setRefresh(false)
                    mSwipeRefreshPlus!!.showNoMore(false)
                }, 1000)
            }

            override fun onPullUpToRefresh() {
                count++
                if (count >= page) {
                    mSwipeRefreshPlus!!.showNoMore(true)

                } else {
                    mSwipeRefreshPlus!!.postDelayed({ presenter!!.loadMore() }, 1500)
                }
            }
        })
        noMoreView = LayoutInflater.from(activity).inflate(R.layout.item_no_more, null, false)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        noMoreView!!.setPadding(10, 10, 10, 10)
        mSwipeRefreshPlus!!.setNoMoreView(noMoreView as View, layoutParams)
        presenter!!.refresh()
    }

    override fun setPresenter(presenter: DemoContact.Presenter) {
        this.presenter = presenter
        presenter.bind()
        datas = presenter.getData()
    }

}// Required empty public constructor
