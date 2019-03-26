package com.thenyc4free.gps1.nyc4free;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Helper methods related to retrieving Event data from the online database.
 */

final class QueryUtils {

    private static final String EVENTS_URL_STRING =
         "https://s3.us-east-2.amazonaws.com/capstone.nyc4free.events/capstonev001.json";

    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yy/MM/dd");

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    static List<List<Event>> fetchEvents(){
        //Create the query URL.
        URL eventsUrl = null;
        try {
            eventsUrl = new URL(EVENTS_URL_STRING);
        } catch (MalformedURLException e) {
            Log.e(com.thenyc4free.gps1.nyc4free.MainActivity.LOG_TAG, "Error creating URL.", e);
        }

        if (eventsUrl == null) return null;

        try{
            //Perform the network request for events.
            String jsonResponse = getResultsFromHttpRequest(eventsUrl);
            //Extract the event data required to create the Event objects and return the list
            //of events to be displayed in the RecyclerView of query results.
            return getEventInfo(jsonResponse);
        } catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP query.
     * @throws IOException related to network and stream reading.
     */
    private static String getResultsFromHttpRequest(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(10000);

        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();

            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    private static List<List<Event>> getEventInfo(String eventJsonString) {
        //Create two list of Event objects to be populated with the data to be extracted from the
        //eventJsonString: events is the list of events/attractions that are not always free;
        //alwaysFreeEvents is the list of events that are always free.
        List<Event> events = new ArrayList<>();
        List<Event> alwaysFreeEvents = new ArrayList<>();

        //listOfEventLists is the list of lists that will contain the two lists of Events produced.

        List<List<Event>> listOfEventLists = new ArrayList<>();

        try {
            JSONArray eventDataArray = new JSONArray(eventJsonString);

            for (int i = 0; i < eventDataArray.length(); i++) {
                JSONObject individualEvent = eventDataArray.getJSONObject(i);

                int flag = individualEvent.optInt("flag", 0);
                String endDate = individualEvent.optString("endDate", "");
                JSONArray datesArray = individualEvent.getJSONArray("dates");

                for (int j = 0; j < datesArray.length(); j++) {
                    JSONObject individualDate = datesArray.getJSONObject(j);
                    String date = individualDate
                            .optString("date", "");

                    //If the Event date is the current date or later and if the Event end date, if
                    //one exists, is the Event date or later, add the event to the list of Events.
                    if (SharedHelper.eventIsCurrent(date, endDate, flag)) {
                        //If the event is not an always-free event, create a new Event Object and
                        //add it to the list of events.
                        if(flag == 0){
                            //Create new Event and add it to the list of Events
                            events.add(createEvent(individualEvent, date, endDate, flag));
                        } else {
                            //If the event is an always-free event, create a new Event Object and
                            //add it to the list of always-free events.
                            alwaysFreeEvents.add(createEvent(individualEvent, date, endDate, flag));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(com.thenyc4free.gps1.nyc4free.MainActivity.LOG_TAG, "Error parsing JSON data. ", e);
            return null;
        }

        //Sort events chronologically by event date, then alphabetically by event name within the
        //event date.
        if(events.size() > 0) {
            Collections.sort(events, new Comparator<Event>(){
                @Override
                public int compare(Event event1, Event event2) {
                    Date event1Date = new Date();
                    Date event2Date = new Date();
                    try {
                        event1Date = mDateFormat.parse(SharedHelper.getComparableDate(event1.getDate()));
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    try {
                        event2Date = mDateFormat.parse(SharedHelper.getComparableDate(event2.getDate()));
                    } catch (ParseException e2) {
                        e2.printStackTrace();
                    }
                    int dateComp = event1Date.compareTo(event2Date);

                    if(dateComp != 0) {
                        return dateComp;
                    }
                    return event1.getName().compareToIgnoreCase(event2.getName());
                }
            });
        }

        if(alwaysFreeEvents.size() > 0) {
            //Sort events alphabetically by name
            Collections.sort(alwaysFreeEvents, new Comparator<Event>() {
                @Override
                public int compare(Event event1, Event event2) {
                    return event1.getName().compareToIgnoreCase(event2.getName());
                }
            });
        }

        listOfEventLists.add(events);
        listOfEventLists.add(alwaysFreeEvents);
        //return allEvents;
        return listOfEventLists;
    }

    /**
     * Helper method to create an Event Object from a JSONObject.
     *
     * @param individualEvent   The JSONObject containing the event information
     * @param date              The event date
     * @param endDate           The event end date
     * @param flag              Indicator that shows whether the event is always-free or not.
     * @return                  The Event Object to be stored in the appropriate list of events.
     */
    private static Event createEvent(JSONObject individualEvent, String date, String endDate, int flag) {

        return new Event(
                individualEvent.optString("id", "O"),
                individualEvent.optString("event", "NA"),
                individualEvent.optString("description", ""),
                flag,
                endDate,
                individualEvent.optString("location", "NA"),
                individualEvent.optString("dow", ""),
                individualEvent.optString("time", ""),
                individualEvent.optString("notes", ""),
                individualEvent.optString("website", ""),
                date
        );
    }
}
