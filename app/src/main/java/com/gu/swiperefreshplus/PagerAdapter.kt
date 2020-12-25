package com.gu.swiperefreshplus

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.gu.swiperefreshplus.fragment.ListFragment
import com.gu.swiperefreshplus.fragment.NormalViewFragment
import com.gu.swiperefreshplus.fragment.RecycleFragment

/**
 * Created by GUHY on 2017/4/5.
 */

class PagerAdapter(fm: androidx.fragment.app.FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): androidx.fragment.app.Fragment? {
        var fragment: androidx.fragment.app.Fragment? = null
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
