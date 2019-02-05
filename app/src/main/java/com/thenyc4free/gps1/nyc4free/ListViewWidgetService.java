package com.thenyc4free.gps1.nyc4free;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.thenyc4free.gps1.nyc4free.database.AppDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to populate the app widget ListView with favorite events.
 */

public class ListViewWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListViewRemoteViewsFactory(this.getApplicationContext());
    }

    class ListViewRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        //Member variable for the Database of favorite events.
        private final AppDatabase mDbFavorites;
        final Context mContext;
        List<String> mFavoritesStrings = new ArrayList<>();
        final List<Event> mCurrentFavoriteEvents = new ArrayList<>();

        ListViewRemoteViewsFactory(Context applicationContext){
            mContext = applicationContext;
            //Initialize the database of favorite events.
            mDbFavorites = AppDatabase.getInstance(getApplicationContext());
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {mFavoritesStrings = getCurrentFavoritesFromDb();}

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            if(mFavoritesStrings == null) return 0;
            return mFavoritesStrings.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews views = new RemoteViews(mContext.getPackageName(),
                    R.layout.widget_list_item);
            if(mFavoritesStrings != null){
                views.setTextViewText(R.id.widget_favorite_list_item_tv,
                        mFavoritesStrings.get(position));
            }
            Bundle extras = new Bundle();
            extras.putInt(MainActivity.INTENT_ID_EXTRA_KEY, position);
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            views.setOnClickFillInIntent(R.id.widget_favorite_list_item_tv, fillInIntent);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        /**
         * Retrieve the list of current favorite events from the database and, for each, create
         * the String of select event information to be displayed in the widget.  Events which have
         * occurred in the past are not shown in the widget and deleted from the favorites database.
         *
         * @return      The list of strings that contain the favorite event information.
         */
        private List<String> getCurrentFavoritesFromDb() {
            final List<String> eventStrings = new ArrayList<>();
            //Save the list of current favorite events, to be used in getViewAt() to retrieve the
            //clicked event's auto-generated id and include it as an intent extra to be used
            //to retrieve event data from the database in DetailActivity.
            List<Event> favorites = mDbFavorites.eventDao().loadAllFavorites();
            for(Event event : favorites) {
                if(SharedHelper.eventIsCurrent(event.getDate(), event.getEndDate(), event.getFlag())) {
                    eventStrings.add(event.getName() + ": " +
                            SharedHelper.createTextEventDayDateTime(mContext, event) + "\n");
                    mCurrentFavoriteEvents.add(event);
                }
                else {
                    mDbFavorites.eventDao().deleteEvent(event);
                }
            }
            return eventStrings;
        }
    }
}
