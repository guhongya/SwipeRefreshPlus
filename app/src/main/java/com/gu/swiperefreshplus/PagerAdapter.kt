package com.gu.swiperefreshplus

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

import com.gu.swiperefreshplus.fragment.ListFragment
import com.gu.swiperefreshplus.fragment.NormalViewFragment
import com.gu.swiperefreshplus.fragment.RecycleFragment

/**
 * Created by GUHY on 2017/4/5.
 */

class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = RecycleFragment()
            1 -> fragment = ListFragment()
            2 -> fragment = NormalViewFragment()
        }
        return fragment
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        when (position) {
            0 -> return "RecycleView"
            1 -> return "ListView"
            2 -> return "NormalView"
            else-> return ""
        }
    }
}
