package com.gu.swiperefresh.test;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.filters.FlakyTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.gu.swiperefresh.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by GUHY on 2017/4/10.
 */
@RunWith(AndroidJUnit4.class)
public class SwipeRefreshPlusTest {
    @Rule
    public ActivityTestRule<SwipeRefreshTestActivity> mActivityRule =
            new ActivityTestRule<>(SwipeRefreshTestActivity.class);

    @Test
    @FlakyTest
    public void testNoData() throws Throwable {
        final Activity activity=mActivityRule.getActivity();
        final TestAdapter testAdapter=new TestAdapter();
        SwipeRefreshPlus swipeRefreshPlus;
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_test);
                RecyclerView recyclerView= (RecyclerView) activity.findViewById(R.id.recycle_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                recyclerView.setAdapter(testAdapter);
                SwipeRefreshPlus swipeRefreshPlus = (SwipeRefreshPlus) activity.findViewById(R.id.swipe_refresh);
            }
        });
        swipeRefreshPlus = (SwipeRefreshPlus) activity.findViewById(R.id.swipe_refresh);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        swipe(GeneralLocation.TOP_CENTER,GeneralLocation.BOTTOM_CENTER);
        assertThat(swipeRefreshPlus.getRefreshController().getCurrentTargetOffsetTop(),is(not(0)));
        swipeRefreshPlus.setRefresh(false);
        assertThat(swipeRefreshPlus.getRefreshController().getCurrentTargetOffsetTop(),is(-TestUtil.dpToPixel(activity,40)));
        swipe(GeneralLocation.BOTTOM_CENTER,GeneralLocation.TOP_CENTER);
        assertThat(swipeRefreshPlus.getLoadViewController().getCurrentHeight(),is(0));
    }

    @Test
    @FlakyTest
    public void diffModeTest() throws Throwable {
        final Activity activity=mActivityRule.getActivity();
        final TestAdapter testAdapter=new TestAdapter();
        SwipeRefreshPlus swipeRefreshPlus;
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setContentView(R.layout.activity_test);
                RecyclerView recyclerView= (RecyclerView) activity.findViewById(R.id.recycle_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                recyclerView.setAdapter(testAdapter);
                SwipeRefreshPlus swipeRefreshPlus = (SwipeRefreshPlus) activity.findViewById(R.id.swipe_refresh);
                swipeRefreshPlus.setScrollMode(SwipeRefreshMode.MODE_NONE);
            }
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        swipeRefreshPlus = (SwipeRefreshPlus) activity.findViewById(R.id.swipe_refresh);
        swipe(GeneralLocation.TOP_CENTER,GeneralLocation.BOTTOM_CENTER);
        assertThat(swipeRefreshPlus.getRefreshController().isRefresh(),is(false));
        swipe(GeneralLocation.BOTTOM_CENTER,GeneralLocation.TOP_CENTER);
        swipe(GeneralLocation.BOTTOM_CENTER,GeneralLocation.TOP_CENTER);
        assertThat(swipeRefreshPlus.getLoadViewController().getCurrentHeight(),is(0));
    }

    /**
     * Creates a new item.
     *
     * @param context the context
     * @param height  in DP
     * @return
     */
    private RecyclerView.LayoutParams createLayoutParams(Context context, int height) {
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                TestUtil.dpToPixel(context, height));
    }

    private static ViewAction swipe(CoordinatesProvider from, CoordinatesProvider to) {
        return new GeneralSwipeAction(Swipe.FAST, from, to, Press.FINGER);
    }
}
