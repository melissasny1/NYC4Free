package com.thenyc4free.gps1.nyc4free.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.thenyc4free.gps1.nyc4free.Event;

import java.util.List;

@Dao
public interface EventDao {

    @Query("SELECT * FROM favoriteEvents ORDER BY name")
    LiveData<List<Event>> loadAllEvents();

    @Query("SELECT * FROM favoriteEvents ORDER BY name")
    List<Event> loadAllFavorites();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvent(Event event);

    @Delete
    void deleteEvent(Event event);
}
