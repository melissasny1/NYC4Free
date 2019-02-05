package com.thenyc4free.gps1.nyc4free;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality to create a widget that displays basic event
 * information for the favorite events selected within the app by the user.
 */
public class NYC4FreeAppWidget extends AppWidgetProvider {

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.nyc4_free_app_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setEmptyView(R.id.widget_list_view, R.id.empty_view);

        Intent intent = new Intent(context, ListViewWidgetService.class);
        views.setRemoteAdapter(R.id.widget_list_view, intent);

        //Create an Intent to launch MainActivity when the widget is clicked.
        Intent launchMainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(context, 0 , launchMainActivityIntent, 0);
        //Set click handler to launch pending intent.
        views.setOnClickPendingIntent(R.id.widget_linear_layout, pendingIntent);

        //Create an Intent to launch MainActivity favorites view when a specific favorite event
        //is clicked.
        Intent launchMainActivityFavoriteViewIntent = new Intent(context, MainActivity.class);
        PendingIntent eventPendingIntent = PendingIntent
                .getActivity(context, 0 , launchMainActivityFavoriteViewIntent, 0);
        //Set click handler to launch pending intent.
        views.setPendingIntentTemplate(R.id.widget_list_view, eventPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Start the intent service update widget action, the service takes care
        //of updating the widgets UI
        FavoriteUpdateService.startActionUpdateFavorites(context);
    }

    public static void updateFavoriteWidgets(Context context, AppWidgetManager appWidgetManager,
                                             int[] appWidgetIds){
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


