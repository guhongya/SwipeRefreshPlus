package com.gu.swiperefreshplus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(tool_bar)
        vp_content.adapter = PagerAdapter(supportFragmentManager)
        tab_layout.setupWithViewPager(vp_content)
    }

}
