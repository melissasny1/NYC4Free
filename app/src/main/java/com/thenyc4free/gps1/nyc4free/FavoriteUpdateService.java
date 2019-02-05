package com.thenyc4free.gps1.nyc4free;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * IntentService to update the user favorites displayed in the app widget.
 */

public class FavoriteUpdateService extends IntentService {

    private static final String ACTION_UPDATE_FAVORITES = "com.example.android.nyc4free" +
            ".action.update_favorites";

    public FavoriteUpdateService(){ super("FavoriteUpdateService"); }

    public static void startActionUpdateFavorites(Context context){
        Intent intent = new Intent(context, FavoriteUpdateService.class);
        intent.setAction(ACTION_UPDATE_FAVORITES);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(intent != null){
            final String action = intent.getAction();
            if(ACTION_UPDATE_FAVORITES.equals(action)){
                handleActionUpdateFavorites();
            }
        }
    }

    private void handleActionUpdateFavorites(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager
                .getAppWidgetIds(new ComponentName(this, NYC4FreeAppWidget.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,
                R.id.widget_list_view);
        NYC4FreeAppWidget.updateFavoriteWidgets(this,
                appWidgetManager, appWidgetIds);
    }
}
