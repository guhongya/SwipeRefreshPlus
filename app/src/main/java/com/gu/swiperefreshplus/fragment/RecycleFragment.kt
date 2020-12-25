package com.gu.swiperefreshplus.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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
import kotlinx.android.synthetic.main.fragment_recycle.*
import me.guhy.swiperefresh.SwipeRefreshPlus


class RecycleFragment : androidx.fragment.app.Fragment(), DemoContact.View {
    private lateinit var recycleAdapter: SimpleRecycleAdapter
    private lateinit var datas: List<Int>
    private var noMoreView: View? = null
    private lateinit var presenter: DemoContact.Presenter
    private lateinit var mRefreshViewController: MRefreshViewController

    internal var count = 0
    internal var page = 4

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_recycle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefresh.loadViewController = LoadMoreController(activity!!, swipeRefresh)
        mRefreshViewController = MRefreshViewController(activity!!, swipeRefresh)
        mRefreshViewController.setBackgroundColor(activity!!.resources.getColor(R.color.colorAccent))
        swipeRefresh.setRefreshViewController(mRefreshViewController)
        DataPresenter(this)
        setHasOptionsMenu(true)
        iniView()
    }

    private fun iniView() {
        recycleContent.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        recycleAdapter = SimpleRecycleAdapter()
        recycleAdapter.setData(datas)
        recycleContent.adapter = recycleAdapter
        recycleContent.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        swipeRefresh.setRefreshColorResources(*intArrayOf(R.color.colorPrimary))
        swipeRefresh.setOnRefreshListener(object : SwipeRefreshPlus.OnRefreshListener {
            override fun onPullDownToRefresh() {
                swipeRefresh.postDelayed({
                    presenter.refresh()
                    LogUtils.d(swipeRefresh.loadViewController!!.isLoading())
                }, 1000)
            }

            override fun onPullUpToRefresh() {
                LogUtils.d("onloading")
                count++
                if (count >= page) {
                    swipeRefresh.showNoMore(true)

                } else {
                    swipeRefresh.postDelayed({ presenter.loadMore() }, 1500)
                }
            }
        })
        noMoreView = LayoutInflater.from(activity).inflate(R.layout.item_no_more, null, false)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        noMoreView?.setPadding(10, 10, 10, 10)
        swipeRefresh.setNoMoreView(noMoreView as View, layoutParams)
    }

    override fun onDataChange() {
        recycleAdapter.notifyDataSetChanged()

    }

    override fun onDataAdded(from: Int, to: Int) {
        recycleAdapter.notifyItemRangeInserted(from, to - from)
        if (from == 0) {
            recycleContent.scrollToPosition(0)
        }
        val id = datas[0]
        Glide.with(this).asBitmap().load(id).listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                LogUtils.d("ok")
                val palette = androidx.palette.graphics.Palette.from(resource).generate()
                var color = palette.getVibrantColor(Color.WHITE)
                if (color == Color.WHITE) {
                    color = palette.getMutedColor(Color.WHITE)
                    if (color == Color.WHITE) {
                        color = palette.getDominantColor(Color.WHITE)
                    }
                }
                if (color != Color.WHITE) {
                    mRefreshViewController.setBackgroundColor(color)
                }
                return false
            }
        }).submit()
        swipeRefresh.stopLoading()
    }

    override fun setPresenter(presenter: DemoContact.Presenter) {
        this.presenter = presenter
        presenter.bind()
        datas = presenter.getData()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.recycleview_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.liner_layout -> recycleContent.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            R.id.grid_layout -> recycleContent.layoutManager = androidx.recyclerview.widget.GridLayoutManager(activity, 2)
            R.id.staggered_grid_layout -> recycleContent.layoutManager = androidx.recyclerview.widget.StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL)
        }
        return true
    }
}
