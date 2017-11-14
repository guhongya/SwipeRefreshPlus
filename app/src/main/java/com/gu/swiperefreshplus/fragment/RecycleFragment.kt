package com.gu.swiperefreshplus.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.graphics.Palette
import android.support.v7.widget.*
import android.view.*
import com.apkfuns.logutils.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.gu.swiperefreshplus.R
import com.gu.swiperefreshplus.SimpleRecycleAdapter
import com.gu.swiperefreshplus.extention.LoadMoreController
import com.gu.swiperefreshplus.extention.MRefreshViewController
import me.guhy.swiperefresh.SwipeRefreshPlus


class RecycleFragment : Fragment(), DemoContact.View {
    private var recycleContent: RecyclerView? = null
    private var recycleAdapter: SimpleRecycleAdapter? = null
    private var swipeRefreshPlush: SwipeRefreshPlus? = null
    private var datas: List<Int>? = null
    internal var count = 0
    internal var page = 4
    private var noMoreView: View?=null
    private var presenter: DemoContact.Presenter?=null
    private var mRefreshViewController: MRefreshViewController? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        LogUtils.d("recycle created")
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_recycle, container, false)
        recycleContent = view.findViewById<View>(R.id.recycle_content) as RecyclerView
        swipeRefreshPlush = view.findViewById<View>(R.id.swipe_refresh) as SwipeRefreshPlus
        swipeRefreshPlush!!.loadViewController = LoadMoreController(container!!.context, swipeRefreshPlush!!)
        mRefreshViewController = MRefreshViewController(container.context, swipeRefreshPlush!!)
        mRefreshViewController!!.setBackgroundColor(activity.resources.getColor(R.color.colorAccent))
        swipeRefreshPlush!!.setRefreshViewController(mRefreshViewController as MRefreshViewController)
        DataPresenter(this)
        setHasOptionsMenu(true)
        iniView()
        return view
    }

    private fun iniView() {
        recycleContent!!.layoutManager = LinearLayoutManager(activity)
        recycleAdapter = SimpleRecycleAdapter()
        recycleAdapter!!.setData(datas!!)
        recycleContent!!.adapter = recycleAdapter
        recycleContent!!.itemAnimator = DefaultItemAnimator()
        swipeRefreshPlush!!.setRefreshColorResources(*intArrayOf(R.color.colorPrimary))
        swipeRefreshPlush!!.setOnRefreshListener(object : SwipeRefreshPlus.OnRefreshListener {
            override fun onPullDownToRefresh() {
                swipeRefreshPlush!!.postDelayed({
                    presenter?.refresh()
                    LogUtils.d(swipeRefreshPlush!!.loadViewController!!.isLoading)
                }, 1000)
            }

            override fun onPullUpToRefresh() {
                LogUtils.d("onloading")
                count++
                if (count >= page) {
                    swipeRefreshPlush!!.showNoMore(true)

                } else {
                    swipeRefreshPlush!!.postDelayed({ presenter?.loadMore() }, 1500)
                }
            }
        })
        noMoreView = LayoutInflater.from(activity).inflate(R.layout.item_no_more, null, false)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        noMoreView?.setPadding(10, 10, 10, 10)
        swipeRefreshPlush!!.setNoMoreView(noMoreView as View, layoutParams)
    }

    override fun onDataChange() {
        recycleAdapter!!.notifyDataSetChanged()

    }

    override fun onDataAdded(from: Int, to: Int) {
        recycleAdapter!!.notifyItemRangeInserted(from, to - from)
        if (from == 0) {
            recycleContent!!.scrollToPosition(0)
        }
        val id = datas!![0]
        Glide.with(this).asBitmap().load(id).listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                LogUtils.d("ok")
                val palette = Palette.from(resource).generate()
                var color = palette.getVibrantColor(Color.WHITE)
                if (color == Color.WHITE) {
                    color = palette.getMutedColor(Color.WHITE)
                    if (color == Color.WHITE) {
                        color = palette.getDominantColor(Color.WHITE)
                    }
                }
                if (color != Color.WHITE) {
                    mRefreshViewController!!.setBackgroundColor(color)
                }
                return false
            }
        }).submit()
        swipeRefreshPlush!!.setRefresh(false)
        swipeRefreshPlush!!.setLoadMore(false)
    }

    override fun setPresenter(presenter: DemoContact.Presenter) {
        this.presenter = presenter
        presenter.bind()
        datas = presenter.getData()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.recycleview_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.liner_layout -> recycleContent!!.layoutManager = LinearLayoutManager(activity)
            R.id.grid_layout -> recycleContent!!.layoutManager = GridLayoutManager(activity, 2)
            R.id.staggered_grid_layout -> recycleContent!!.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
        return true
    }
}
