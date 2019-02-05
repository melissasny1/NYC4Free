package com.thenyc4free.gps1.nyc4free;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.thenyc4free.gps1.nyc4free.database.AppDatabase;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final LiveData<List<Event>> favoriteEvents;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        favoriteEvents = database.eventDao().loadAllEvents();
    }

    public LiveData<List<Event>> getFavoriteEvents() {
        return favoriteEvents;
    }
}
