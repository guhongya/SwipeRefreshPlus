package com.gu.swiperefreshplus

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
//    private var mContent: ViewPager? = null
//    private var mTabLayout: TabLayout? = null
//    private var mToolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        mContent = findViewById<View>(R.id.vp_content)
//        mTabLayout = findViewById<View>(R.id.tab_layout)
//        mToolbar = findViewById<View>(R.id.tool_bar)
        setSupportActionBar(tool_bar)
        vp_content.adapter = PagerAdapter(supportFragmentManager)
        tab_layout.setupWithViewPager(vp_content)
    }

}
