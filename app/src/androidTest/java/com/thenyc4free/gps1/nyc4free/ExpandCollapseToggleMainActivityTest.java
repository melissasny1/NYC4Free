package com.thenyc4free.gps1.nyc4free;

import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

/**
 *These tests (1) confirm that the MainActivity displays the event Recyclerview and not the
 *loading indicator or error message and (2) demo a user clicking on an event and verify
 *that it opens the detail activity.
 */

@RunWith(AndroidJUnit4.class)
public class ExpandCollapseToggleMainActivityTest {

    @Rule
    public final ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    private IdlingResource mIdlingResource;

    //Register IdlingResource before running tests.
    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityTestRule.getActivity().getIdlingResource();
        IdlingRegistry.getInstance().register(mIdlingResource);
    }

    @Test
    public void clickItemToggleButton_ExpandsAndCollapsesItemDetail() {
        //Click on the first event recyclerview item, on the expansion button, to expand
        //the item view.
        onView(withId(R.id.rv_events)).perform(RecyclerViewActions
                .actionOnItemAtPosition(0,
                        MyViewAction.clickChildViewWithId(R.id.expansion_button)));

        //Confirm that the 2 expansion views are displayed.
        onView(withRecyclerView(R.id.rv_events).atPositionOnView(0,
                R.id.expansion_text_linear_layout)).check(matches(isDisplayed()));

        onView(withRecyclerView(R.id.rv_events).atPositionOnView(0,
                R.id.expansion_button_linear_layout)).check(matches(isDisplayed()));

        //Click on the first event recyclerview item, again on the expansion button, to collapse
        //the item view.
        onView(withId(R.id.rv_events)).perform(RecyclerViewActions
                .actionOnItemAtPosition(0,
                        MyViewAction.clickChildViewWithId(R.id.expansion_button)));

        //Confirm that the 2 expansion views are not displayed.
        onView(withRecyclerView(R.id.rv_events).atPositionOnView(0,
                R.id.expansion_text_linear_layout)).check(matches(not(isDisplayed())));

        onView(withRecyclerView(R.id.rv_events).atPositionOnView(0,
                R.id.expansion_button_linear_layout)).check(matches(not(isDisplayed())));
    }


    @Test
    public void clickItemToggleButton_ExpandTwoItems() {
        //Click on the first event RecyclerView item, on the expansion button, to expand
        //the item view.
        onView(withId(R.id.rv_events)).perform(RecyclerViewActions
                .actionOnItemAtPosition(0,
                        MyViewAction.clickChildViewWithId(R.id.expansion_button)));

        //Confirm that the 2 expansion views are displayed.
        onView(withRecyclerView(R.id.rv_events).atPositionOnView(0,
                R.id.expansion_text_linear_layout)).check(matches(isDisplayed()));

        onView(withRecyclerView(R.id.rv_events).atPositionOnView(0,
                R.id.expansion_button_linear_layout)).check(matches(isDisplayed()));

        //Click on the second event RecyclerView item, again on the expansion button, to expand
        //the second event item view and to collapse the first event item view.

        onView(withId(R.id.rv_events)).perform(RecyclerViewActions
                .actionOnItemAtPosition(1,
                        MyViewAction.clickChildViewWithId(R.id.expansion_button)));

        //Confirm that the second event's 2 expansion views are displayed.
        onView(withRecyclerView(R.id.rv_events).atPositionOnView(1,
                R.id.expansion_text_linear_layout)).check(matches(isDisplayed()));

        onView(withRecyclerView(R.id.rv_events).atPositionOnView(1,
                R.id.expansion_button_linear_layout)).check(matches(isDisplayed()));

        //Scroll to the first event
        onView(withId(R.id.rv_events)).perform(RecyclerViewActions.scrollToPosition(0));

        //Confirm that the first event's 2 expansion views are not displayed.
        onView(withRecyclerView(R.id.rv_events).atPositionOnView(0,
                R.id.expansion_text_linear_layout)).check(matches(not(isDisplayed())));

        onView(withRecyclerView(R.id.rv_events).atPositionOnView(0,
                R.id.expansion_button_linear_layout)).check(matches(not(isDisplayed())));
    }

    // Convenience helper
    public RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }

    //Unregister resources when not needed
    @After
    public void unregisterIdlingResource() {
        if(mIdlingResource != null){
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }
}
