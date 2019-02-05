package com.thenyc4free.gps1.nyc4free;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Create a custom AsyncTask to get the event data on a background thread and load the
 * resulting list of Event objects into the EventAdapter to be displayed in the
 * RecyclerView.
 */

class FetchEventTask extends AsyncTask<Void, Void, List<List<Event>>> {
    private final AsyncTaskCompleteListener<List<List<Event>>> mListener;
    private final SimpleIdlingResource mIdlingResource;

    FetchEventTask(AsyncTaskCompleteListener<List<List<Event>>> listener,
                   @Nullable final SimpleIdlingResource idlingResource){

        mListener = listener;
        mIdlingResource = idlingResource;

        if (mIdlingResource != null) {
            mIdlingResource.setIdleState(false);
        }
    }

    @Override
    protected List<List<Event>> doInBackground(Void... voids) {
        return QueryUtils.fetchEvents();
    }

    @Override
    protected void onPostExecute(List<List<Event>> events) {
        super.onPostExecute(events);
        mListener.onTaskComplete(events);
        if(mIdlingResource != null){
            mIdlingResource.setIdleState(true);
        }
    }
}
