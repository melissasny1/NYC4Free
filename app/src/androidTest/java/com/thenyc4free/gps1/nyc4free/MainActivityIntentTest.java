package com.thenyc4free.gps1.nyc4free;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.content.Intent.ACTION_INSERT;
import static android.content.Intent.ACTION_VIEW;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Verify that clicking on the website, share and calendar buttons for an event creates an intent
 * that starts the appropriate action.
 */

public class MainActivityIntentTest {

    @Rule
    public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(
            MainActivity.class);


    @Before
    public void stubIntents() {
        Instrumentation.ActivityResult intentResult = new Instrumentation
                .ActivityResult(Activity.RESULT_OK, null);

        intending(anyIntent()).respondWith(intentResult);
    }

    @Test
    public void clickWebsiteButton_CreatesIntentWithActionView() {

        onView(withId(R.id.rv_events)).perform(RecyclerViewActions
                .actionOnItemAtPosition(0,
                        MyViewAction.clickChildViewWithId(R.id.website_button)));

        intended(allOf(
                hasAction(ACTION_VIEW)));
    }

    @Test
    public void clickShareButton_CreatesChooser() {

        onView(withId(R.id.rv_events)).perform(RecyclerViewActions
                .actionOnItemAtPosition(0,
                        MyViewAction.clickChildViewWithId(R.id.share_button)));

        intended(allOf(hasAction(Intent.ACTION_CHOOSER),
                hasExtra(is(Intent.EXTRA_INTENT),
                   allOf( hasAction(Intent.ACTION_SEND)
                   ))));
    }

    @Test
    public void clickCalendarButton_CreatesInsertIntent() {

        onView(withId(R.id.rv_events)).perform(RecyclerViewActions
                .actionOnItemAtPosition(0,
                        MyViewAction.clickChildViewWithId(R.id.calendar_button)));

        intended(allOf(
                hasAction(ACTION_INSERT)));
    }
}
