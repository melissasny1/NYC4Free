package com.thenyc4free.gps1.nyc4free;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Create an Event Object.
 */

@Entity(tableName = "favoriteEvents")
public class Event implements Parcelable {
    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel parcel) {
            return new Event(parcel);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "event_type")
    private String eventType;
    private String name;
    private String description;
    private final int flag;
    @ColumnInfo(name = "end_date")
    private final String endDate;
    private final String location;
    @ColumnInfo(name = "day_of_week")
    private final String dayOfWeek;
    private String time;
    private final String notes;
    private final String website;
    private String date;

    @Ignore
    public Event (String eventType, String name, String description, int flag, String endDate, String location,
                  String dayOfWeek, String time, String notes, String website, String date) {

        this.eventType = eventType;
        this.name = name;
        this.description = description;
        this.flag = flag;
        this.endDate = endDate;
        this.location = location;
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.notes = notes;
        this.website = website;
        this.date = date;
    }

    public Event (int id, String eventType, String name, String description, int flag, String endDate,
                  String location, String dayOfWeek, String time, String notes,
                  String website, String date) {

        this.id = id;
        this.eventType = eventType;
        this.name = name;
        this.description = description;
        this.flag = flag;
        this.endDate = endDate;
        this.location = location;
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.notes = notes;
        this.website = website;
        this.date = date;
    }

    private Event(Parcel in) {
        eventType = in.readString();
        name = in.readString();
        description = in.readString();
        flag = in.readInt();
        endDate = in.readString();
        location = in.readString();
        dayOfWeek = in.readString();
        time = in.readString();
        notes = in.readString();
        website = in.readString();
        date = in.readString();
    }

    //Getter methods
    public int getId() {return id;}

    public String getEventType() {return eventType;}

    public String getName() {return name;}

    public String getDescription() {return description;}

    public int getFlag() {return flag;}

    public String getEndDate() {return endDate;}

    public String getLocation() {return location;}

    public String getDayOfWeek() {return dayOfWeek;}

    public String getTime() {return time;}

    public String getNotes() {return notes;}

    public String getWebsite() {return website;}

    public String getDate() {return date;}

    //Setter methods.

    public void setId(int id) {
        this.id = id;
    }

    public void setEventType(String eventType) { this.eventType = eventType; }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(eventType);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeInt(flag);
        parcel.writeString(endDate);
        parcel.writeString(location);
        parcel.writeString(dayOfWeek);
        parcel.writeString(time);
        parcel.writeString(notes);
        parcel.writeString(website);
        parcel.writeString(date);
    }
}
