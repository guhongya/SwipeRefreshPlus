package me.guhy.swiperefresh.test;

import android.app.Activity;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.filters.FlakyTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import me.guhy.swiperefresh.*;

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
