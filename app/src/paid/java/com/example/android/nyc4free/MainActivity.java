package com.thenyc4free.gps1.nyc4free;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thenyc4free.gps1.nyc4free.database.AppDatabase;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity implements EventAdapter.EventClickListener,
        DeleteFavoriteDialogFragment.DeleteFavoriteDialogListener {

    static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String RECYCLERVIEW_STATE_KEY = "rv state";
    private static final String EVENT_DATA_SET_KEY = "event data set key";
    private static final String TITLE_KEY = "title";
    private static final String ERROR_MSG_KEY = "error msg displayed";
    private static final String FREE_EVENT_LIST_KEY = "free events";
    private static final String ALWAYS_FREE_EVENT_LIST_KEY = "always-free events";
    private static final String EXPANDED_POSITION_KEY = "expanded adapter position";
    private static final String STATE_CHANGE_KEY = "state change key";
    private static final String DIALOG = "dialog";
    private static final String FAVORITE_EVENT_TO_DELETE = "favorite event to delete";
    private static final int GRID_COLUMN_WIDTH_DP = 450;

    static final String INTENT_ID_EXTRA_KEY = "Id";

    //Member variable for the Database of favorite events.
    private AppDatabase mDbFavorites;

    @BindView(R.id.rv_events) RecyclerView mRecyclerViewEvents;
    @BindView(R.id.pb_loading_indicator) ProgressBar mLoadingIndicator;
    @BindView(R.id.tv_error_message_display) TextView mErrorMsgDisplay;

    //Variable for the list of Event objects for free events by date
    private List<Event> mEvents;
    //Variable for the list of Event objects for events where admission is always free.
    private List<Event> mAlwaysFreeEvents;
    //Variable for the list of Event objects that the user has saved as favorites.
    private List<Event> mFavoriteEvents;
    private EventAdapter mEventAdapter;
    //Flag where a value of 1 indicates that free events by date are displayed, 2 indicates that
    //always-free events are displayed, 3 indicates that favorites are displayed.
    private int mEventDataSet = 1;
    //Integer representing which error message is currently displayed, if any.
    private int mErrorMsgDisplayed;
    //The user's most recent menu item selection.
    private MenuItem mPreviousMenuSelection;
    private GridLayoutManager mLayoutManager;
    private Parcelable mSavedRecyclerViewLayoutState;
    //Store the position of the user-expanded event view, if any.
    private int mExpandedAdapterPosition = -1;
    private boolean mHasStateChanged = false;
    //Indicator of whether the MainActivity was started by a user click on a favorite shown in
    //the app widget.
    private boolean mActivityStartedByWidgetFavoriteClick = false;
    //Store the value of the position of the user-clicked favortie event that opened the
    //MainActivity from the app widget.
    private int mFavoriteEventId;

    //Variable for the ButterKnife unbinder.
    private Unbinder unbinder;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Nullable
    private
    SimpleIdlingResource mIdlingResource;

    //Test-only method that instantiates a new instance of SimpleIdlingResource if
    //IdlingResource is null.
    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource(){
        if(mIdlingResource == null){
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //Determine whether the smallest screen width of the user's device is at least 600dp.
        boolean isSmallestScreenWidthGT600 = getResources().getConfiguration()
                .smallestScreenWidthDp >= 600;

        //Create a new Event Adapter
        mEventAdapter = new EventAdapter(this,this,
                isSmallestScreenWidthGT600);

        //Create a new GridLayoutManager and use the RecyclerView reference
        //to assign the layout manager.
        mLayoutManager = new GridLayoutManager(this, calcNumberOfGridCols());
        mRecyclerViewEvents.setLayoutManager(mLayoutManager);
        mRecyclerViewEvents.setHasFixedSize(true);
        //Connect the Adapter to the RecyclerView
        mRecyclerViewEvents.setAdapter(mEventAdapter);

        //Initialize the database of favorite events.
        mDbFavorites = AppDatabase.getInstance(getApplicationContext());

        mIdlingResource = (SimpleIdlingResource) getIdlingResource();

        //Get the Intent used to start MainActivity.
        Intent startMainActivityIntentWithPosition = getIntent();

        //Set up the View Model for the user's stored favorite events.
        setUpViewModel();

        if(savedInstanceState != null && savedInstanceState.containsKey(EVENT_DATA_SET_KEY)) {
            if(savedInstanceState.containsKey(FREE_EVENT_LIST_KEY)){
                mEvents = savedInstanceState.getParcelableArrayList(FREE_EVENT_LIST_KEY);
            }
            if(savedInstanceState.containsKey(ALWAYS_FREE_EVENT_LIST_KEY)) {
                mAlwaysFreeEvents = savedInstanceState
                        .getParcelableArrayList(ALWAYS_FREE_EVENT_LIST_KEY);
            }
            if(savedInstanceState.containsKey(EXPANDED_POSITION_KEY)) {
                mExpandedAdapterPosition = savedInstanceState.getInt(EXPANDED_POSITION_KEY);
                mHasStateChanged = savedInstanceState.getBoolean(STATE_CHANGE_KEY);
            }
            setTitle(String.valueOf(savedInstanceState.getString(TITLE_KEY)));
            mEventDataSet = savedInstanceState.getInt(EVENT_DATA_SET_KEY);
            mSavedRecyclerViewLayoutState = savedInstanceState
                    .getParcelable(RECYCLERVIEW_STATE_KEY);
            mLayoutManager.onRestoreInstanceState(mSavedRecyclerViewLayoutState);

            if (savedInstanceState.containsKey(ERROR_MSG_KEY)) {
                int errorMessage = savedInstanceState.getInt(ERROR_MSG_KEY);
                switch (errorMessage) {
                    case (1):
                        SharedHelper.showErrorMessage(getString(R.string.text_no_internet),
                                mRecyclerViewEvents, mErrorMsgDisplay);
                        mErrorMsgDisplayed = 1;
                        break;
                    case (2):
                        SharedHelper.showErrorMessage(getString(R.string.text_no_events),
                                mRecyclerViewEvents, mErrorMsgDisplay);
                        mErrorMsgDisplayed = 2;
                        break;
                    default:
                        mErrorMsgDisplayed = 0;
                }
            } else if(mEventDataSet == 1 || mEventDataSet == 2){
                switch(mEventDataSet) {
                    case 1:
                        displayCorrectListOfEvents(mEvents, true);
                        break;
                    case 2:
                        displayCorrectListOfEvents(mAlwaysFreeEvents, true);
                        break;
                }
            }

        } else if(startMainActivityIntentWithPosition != null
                && startMainActivityIntentWithPosition.hasExtra(INTENT_ID_EXTRA_KEY)) {
            //If MainActivity is started from the app widget when the user clicks on a specific
            //favorite event, display the list of all favorite events, with the Event the user
            //clicked at the top of the screen.
            mActivityStartedByWidgetFavoriteClick = true;
            mEventDataSet = 3;

            mFavoriteEventId = startMainActivityIntentWithPosition
                    .getIntExtra(MainActivity.INTENT_ID_EXTRA_KEY, 0);
            setTitle(R.string.title_favorites);
        } else {
            //If MainActivity is started by opening the app, fetch the event data and display the
            //"Free Events By Date" data set.
            mEventDataSet = 1;
            setTitle(R.string.title_free_events);
            displayEvents();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idSelected = item.getItemId();

        if (mPreviousMenuSelection != null) {
            mPreviousMenuSelection.setChecked(false);
        }

        //Mark the user's current menu item selection as checked.
        item.setChecked(true);

        //Store the user's current menu item selection.
        mPreviousMenuSelection = item;

        //Reset to their initial values to ensure that if the user had expanded an event CardView,
        //rotated the device and then changed the data set, the new data set will not display an
        //expanded CardView in the position the user had most recently expanded with the previous
        //data set.
        mExpandedAdapterPosition = -1;
        mHasStateChanged = false;

        switch (idSelected) {
            case R.id.action_display_events:
                setTitle(R.string.title_free_events);
                mEventDataSet = 1;
                //Display free events from the events database.
                if(mEvents != null && mEvents.size() > 0){
                    displayCorrectListOfEvents(mEvents,true);
                    mLayoutManager.scrollToPosition(0);
                } else {
                    //If the user opened the app by clicking on a specific favorite event in
                    //the widget and then made this selection from the options menu, displayEvents()
                    //must be called to fetch event data.
                    displayEvents();
                    mLayoutManager.scrollToPosition(0);
                }
                return true;
            case R.id.action_display_always_free_events:
                setTitle(R.string.title_always_free_events);
                mEventDataSet = 2;

                //Display the always-free events from the events database.
                if(mAlwaysFreeEvents != null && mAlwaysFreeEvents.size() > 0){
                    displayCorrectListOfEvents(mAlwaysFreeEvents, true);
                    mLayoutManager.scrollToPosition(0);
                } else {
                    //If the user opened the app by clicking on a specific favorite event in
                    //the widget and then made this selection from the options menu, displayEvents()
                    //must be called to fetch event data.
                    displayEvents();
                    mLayoutManager.scrollToPosition(0);
                }
                return true;
            case R.id.action_display_favorites:
                setTitle(R.string.title_favorites);
                mEventDataSet = 3;

                if(mFavoriteEvents != null && mFavoriteEvents.size() > 0) {
                    displayCorrectListOfEvents(mFavoriteEvents, false);
                    mLayoutManager.scrollToPosition(0);
                } else {
                    //Display message that there are no favorites to display.
                    SharedHelper.showErrorMessage(getString(R.string.text_no_favorites),
                            mRecyclerViewEvents, mErrorMsgDisplay);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //If there is a list of free events, save it.
        if(mEvents != null && mEvents.size() > 0){
            outState.putParcelableArrayList(FREE_EVENT_LIST_KEY, new ArrayList<>(mEvents));
        }
        //If there is a list of always-free events, save it.
        if(mAlwaysFreeEvents != null && mAlwaysFreeEvents.size() > 0){
            outState.putParcelableArrayList(ALWAYS_FREE_EVENT_LIST_KEY,
                    new ArrayList<>(mAlwaysFreeEvents));
        }
        //If there is an applicable error message, save it.
        if(mErrorMsgDisplayed > 0){
            outState.putInt(ERROR_MSG_KEY, mErrorMsgDisplayed);
        }
        outState.putParcelable(RECYCLERVIEW_STATE_KEY, mLayoutManager.onSaveInstanceState());
        outState.putInt(EVENT_DATA_SET_KEY, mEventDataSet);
        outState.putString(TITLE_KEY, String.valueOf(getTitle()));
        //If the user has expanded an Event item view, save the expanded position, as well as\
        //the indicator that the state has changed
        if(mExpandedAdapterPosition >= 0) {
            outState.putInt(EXPANDED_POSITION_KEY, mExpandedAdapterPosition);
            outState.putBoolean(STATE_CHANGE_KEY, true);
        }
    }

    @Override
    public void onDeleteClick(final Event event) {
        //Get Fragment Manager and get rid of any previous versions of the fragment that contains
        //the Alert dialog to delete a favorite event.
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        android.support.v4.app.Fragment prev = getSupportFragmentManager()
                .findFragmentByTag(DIALOG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        //Create and show a new alert dialog to confirm that the user wants to delete the selected
        //event that has been saved as a favorite.
        DeleteFavoriteDialogFragment confirmDelete = new DeleteFavoriteDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(FAVORITE_EVENT_TO_DELETE, event);
        confirmDelete.setArguments(bundle);
        confirmDelete.show(ft, DIALOG);
    }

    /**
     * Method to save an Event as a Favorite in the SQLite database.
     *
     * @param event     The Event object to be saved as a favorite.
     */
    @Override
    public void onFavoriteButtonClick(final Event event) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDbFavorites.eventDao().insertEvent(event);
            }

        });
        //Update the widget to display the updated list of favorite events.
        FavoriteUpdateService.startActionUpdateFavorites(MainActivity.this);
        SharedHelper.makeAndDisplayToast(R.string.saved_favorite_msg,
                MainActivity.this);
        //Capture the user click on the website button in FirebaseAnalytics
        logFirebaseAnalyticsEvent("favorite_button", "favorite_click");
    }

    /**
     * Method to open the website for the clicked Event in a new window.
     *
     * @param event     The Event object for which the website will be opened.
     */
    @Override
    public void onWebsiteButtonClick(Event event) {
        //Open the Event website when the button is clicked, if there is an internet
        //connection.
        if(SharedHelper.hasInternetConnection(MainActivity.this)){
            Intent openEventWebsiteIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(event.getWebsite()));
            if (openEventWebsiteIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(openEventWebsiteIntent);
            }else{
                SharedHelper.makeAndDisplayToast(R.string.no_browser_msg,
                        MainActivity.this);
            }
        }
        else {
            //Display a Toast indicating there is no internet connection.
            SharedHelper.makeAndDisplayToast(R.string.text_no_internet, MainActivity.this);
        }

        //Capture the user click on the website button in Firebase Analytics
        logFirebaseAnalyticsEvent("website_button", "website_click");
    }

    /**
     * Method to create and start the activity to create a chooser to share the clicked Event
     * object.
     *
     * @param event     The Event object to share.
     */
    @Override
    public void onShareButtonClick(Event event) {
        String shareText = SharedHelper.createShareText(event, this);
        Intent shareIntent = ShareCompat.IntentBuilder
                .from(MainActivity.this)
                .setType("text/plain")
                .setSubject(event.getName())
                .setText(shareText)
                .getIntent();
        if (shareIntent.resolveActivity(getPackageManager()) != null){
            startActivity(Intent.createChooser(shareIntent,
                    getString(R.string.action_share)));
        } else {
            SharedHelper.makeAndDisplayToast(R.string.no_share_msg, MainActivity.this);
        }

        //Capture the user click on the share button in FirebaseAnalytics
        logFirebaseAnalyticsEvent("share_button", "share_click");
    }

    /**
     * Method to create a calendar entry for the clicked Event object.
     *
     * @param event     Te Event object to be added to the calendar.
     */
    @Override
    public void onCalendarButtonClick(Event event) {
        final int[] yearMonthDateInNY = SharedHelper.getCurrentYearMonthDayInNY();
        Intent insertCalendarItemIntent;

        if(event.getFlag() == 0){
            int[] eventTime = SharedHelper.convertTime(event.getTime(), this);
            int[] eventDate = SharedHelper.convertDate(event.getDate(), this);

            Calendar beginTime = Calendar.getInstance();
            beginTime.set(eventDate[0], eventDate[1], eventDate[2], eventTime[0],
                    eventTime[1]);
            Calendar endTime = Calendar.getInstance();
            endTime.set(eventDate[0], eventDate[1], eventDate[2], eventTime[2],
                    eventTime[3]);

            if(!event.getTime().contains("-")){
                insertCalendarItemIntent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                beginTime.getTimeInMillis())
                        .putExtra(CalendarContract.Events.TITLE, event.getName())
                        .putExtra(CalendarContract.Events.DESCRIPTION,
                                event.getDescription())
                        .putExtra(CalendarContract.Events.AVAILABILITY,
                                CalendarContract.Events.AVAILABILITY_BUSY);
            } else {
                insertCalendarItemIntent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                beginTime.getTimeInMillis())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                endTime.getTimeInMillis())
                        .putExtra(CalendarContract.Events.TITLE, event.getName())
                        .putExtra(CalendarContract.Events.DESCRIPTION,
                                event.getDescription())
                        .putExtra(CalendarContract.Events.AVAILABILITY,
                                CalendarContract.Events.AVAILABILITY_BUSY);
            }

        } else {
            //For an "always free" Event.
            //Have the Calendar Intent display the Events days of the week and time in the
            //description field.
            String eventDayAndTime = event.getDayOfWeek() + " " + event.getTime();

            //Have the Calendar Intent open the calendar to today's
            //date at 10am.
            Calendar beginTime = Calendar.getInstance();
            beginTime.set(yearMonthDateInNY[0], yearMonthDateInNY[1],
                    yearMonthDateInNY[2], 10, 0);

            insertCalendarItemIntent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                            beginTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.TITLE, event.getName())
                    .putExtra(CalendarContract.Events.DESCRIPTION, eventDayAndTime)
                    .putExtra(CalendarContract.Events.AVAILABILITY,
                            CalendarContract.Events.AVAILABILITY_BUSY);
        }

        if (insertCalendarItemIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(insertCalendarItemIntent);
        } else {
            SharedHelper.makeAndDisplayToast(R.string.no_calendar_msg, MainActivity.this);
        }

        //Capture the user click on the calendar button in FirebaseAnalytics
        logFirebaseAnalyticsEvent("calendar_button", "calendar_click");
    }

    /**
     * Method to scroll the clicked Event object to the top of the screen.
     *
     * @param eventPosition     The Adapter position of the Event object to be moved to the top of
     *                          the screen.
     */
    @Override
    public void onButtonExpandCardViewClick(int eventPosition, int expandedAdapterPosition) {
        mExpandedAdapterPosition = expandedAdapterPosition;
        mLayoutManager.scrollToPositionWithOffset(eventPosition, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    /**
     * Helper method that checks the internet connection and fetches and displays events
     * if there is a connection and displays an error message if there is no connection.
     */
    private void displayEvents() {
        if (SharedHelper.hasInternetConnection(this)) {
            //Display the loading indicator while the data request to the events database is
            //executed.
            mLoadingIndicator.setVisibility(View.VISIBLE);
            new FetchEventTask(new FetchEventTaskListener(), mIdlingResource).execute();
        } else {
            //Hide the RecyclerView that contains the Event info; set the error message text and
            //make the error message textview visible.
            SharedHelper.showErrorMessage(getString(R.string.text_no_internet), mRecyclerViewEvents,
                    mErrorMsgDisplay);
            mErrorMsgDisplayed = 1;
        }
    }

    /**
     * Helper method to set-up the ViewModel and to retrieve the list of favorite Event objects the
     * user has saved, assign the list to the global variable mFavoriteEvents, display the favorites
     * if the user has selected the favorites menu option and to display the no favorites textview
     * after the user deletes the final favorite in the database.
     */
    private void setUpViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        viewModel.getFavoriteEvents().observe(this, new Observer<List<Event>>() {
            @Override
            public void onChanged(@Nullable List<Event> events) {
                mFavoriteEvents = events;

                //If the user has selected the Favorites menu option.
                if(mEventDataSet == 3) {
                    //If the user has deleted all favorites, display the no favorites textview.
                    if(mFavoriteEvents != null && mFavoriteEvents.size() == 0) {
                        SharedHelper.showErrorMessage(getString(R.string.text_no_favorites),
                                mRecyclerViewEvents, mErrorMsgDisplay);
                    } else {
                        //Display the list of favorite events.
                        displayCorrectListOfEvents(mFavoriteEvents, false);
                        if(mActivityStartedByWidgetFavoriteClick) {
                            mLayoutManager.scrollToPositionWithOffset(mFavoriteEventId, 0);
                        } else {
                            mLayoutManager.scrollToPosition(0);
                        }
                    }
                }
            }
        });
    }

    /**
     * Helper method to display the recyclerview, hide the error message textview and set the
     * correct list of existing Event objects on the EventAdapter.
     *
     * @param events    The list of Event objects to be set on the adapter for display.
     */
    private void displayCorrectListOfEvents(List<Event> events, boolean hideDeleteButton){
        mErrorMsgDisplayed = 0;
        SharedHelper.showEventInfo(mRecyclerViewEvents, mErrorMsgDisplay);
        mEventAdapter.setEventData(events, hideDeleteButton, mExpandedAdapterPosition,
                mHasStateChanged);
        mLayoutManager.onRestoreInstanceState(mSavedRecyclerViewLayoutState);
    }

    /**
     * Method that deletes the data for a selected favorite event previously saved by the user.
     *
     * @param dialog    The DeleteFavorite dialog fragment where the user has confirmed that they
     *                  want to delete the selected favorite event.
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Bundle bundle = dialog.getArguments();
        if(bundle != null) {
            final Event event = bundle.getParcelable(FAVORITE_EVENT_TO_DELETE);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDbFavorites.eventDao().deleteEvent(event);
                }
            });
            //Update the widget to display the updated list of favorite events.
            FavoriteUpdateService.startActionUpdateFavorites(this);
        }
        dialog.dismiss();
    }

    /**
     * Method that takes no action for a selected favorite event, when the user confirms through the
     * Alert dialog that they do not want to delete the event.
     *
     * @param dialog    The DeleteFavorite dialog fragment where the user has confirmed that they
     *      *               do not want to delete the selected favorite event.
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

    /**
     * Helper method to calculate the number of grid columns to display based upon device screen
     * width.
     *
     * @return      The number of grid columns to display.
     */
    private int calcNumberOfGridCols(){
        Configuration config = getResources().getConfiguration();
        int width = config.screenWidthDp;
        //return the larger of the screen width divided by the column width or 1.
        return (width/GRID_COLUMN_WIDTH_DP >= 1?width/GRID_COLUMN_WIDTH_DP:1);
    }

    /**
     * Helper method to log a FirebaseAnalytics event when the user clicks on the Favorite, Website,
     * Share or Calendar button.
     *
     * @param itemId        The item id to be logged to FirebaseAnalytics.
     * @param itemName      The item name to be logged to FirebaseAnalytics.
     */
    private void logFirebaseAnalyticsEvent(String itemId, String itemName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    class FetchEventTaskListener implements AsyncTaskCompleteListener<List<List<Event>>>{

        @Override
        public void onTaskComplete(List<List<Event>> allEvents) {
            //Hide the loading indicator when the request to the events database is complete.
            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if(allEvents != null && allEvents.size() > 0) {
                //If the data request has returned lists of event objects, assign the lists to the
                //appropriate global variables.
                if(allEvents.get(0) != null && allEvents.get(0).size() > 0){
                    mEvents = allEvents.get(0);
                }
                if(allEvents.get(1) != null && allEvents.get(1).size() > 0){
                    mAlwaysFreeEvents = allEvents.get(1);
                }

                if(mEventDataSet == 1 && (mEvents != null && mEvents.size() > 0)){
                    displayCorrectListOfEvents(mEvents, true);
                } else if(mEventDataSet == 2 && (mAlwaysFreeEvents != null
                        && mAlwaysFreeEvents.size() > 0)){
                    displayCorrectListOfEvents(mAlwaysFreeEvents, true);
                } else {
                    SharedHelper.showErrorMessage(getString(R.string.text_no_events),
                            mRecyclerViewEvents, mErrorMsgDisplay);
                    mErrorMsgDisplayed = 2;
                }
            } else {
                SharedHelper.showErrorMessage(getString(R.string.text_no_events),
                        mRecyclerViewEvents, mErrorMsgDisplay);
                mErrorMsgDisplayed = 2;
            }
        }
    }
}

