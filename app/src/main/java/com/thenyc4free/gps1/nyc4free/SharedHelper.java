package com.thenyc4free.gps1.nyc4free;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper methods used by MainActivity and EventAdapter.
 */

class SharedHelper {

    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yy/MM/dd", Locale.US);
    private static final int ONE = 1;
    private static final int YEAR_CONSTANT = 2000;
    private static final String ZERO = "0";

    /**
     * Helper method to determine whether there is an Internet connection.
     *
     * @return True if there is an Internet connection.
     */
    static boolean hasInternetConnection(Context context) {
        boolean connectedToInternet = false;
        //Get a reference to the Connectivity Manager to check the state of network connectivity.
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //Get details on the currently active default data network.
        if(connectivityManager != null){
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            connectedToInternet = activeNetwork != null && activeNetwork.isConnected();
        }
        return connectedToInternet;
    }

    /**
     * Helper method to return the full borough name for the Event location.
     *
     * @param eventLocation     The single character Event location stored in the Event database.
     * @return      The full borough name that corresponds to the Event location.
     */
    static String getBoroughName(String eventLocation){
        switch(eventLocation){
            case "m":
                return "Manhattan";
            case "bk":
                return "Brooklyn";
            case "bx":
                return "The Bronx";
            case "q":
                return "Queens";
            case "s":
                return "Staten Island";
                default:
                    return "";
        }
    }

    /**
     * Helper method to create the appropriate text for the mEventDayDateTimeTextView, depending
     * upon whether the event is an "always-free" event.
     *
     * @param currentEvent  The current Event.
     * @return              The text containing the appropriate day of the week, date and time
     *                          information for the event.
     */
    static String createTextEventDayDateTime (Context context, Event currentEvent) {
        String text;
        if(currentEvent.getFlag() == 0){
            String eventDate;
            if(currentEvent.getDate().substring(4,5).contentEquals("0")){
                eventDate = currentEvent.getDate().substring(5);
            } else {
                eventDate = currentEvent.getDate().substring(4,6);
            }
            text = String.format(
                    context.getString(R.string.event_dow_month_date_time_text),
                    currentEvent.getDayOfWeek(),
                    SharedHelper.getMonthName(currentEvent.getDate().substring(2,4)),
                    eventDate, currentEvent.getTime());
        } else {
            if(!currentEvent.getTime().contentEquals("")) {
                text = String.format(context.getString(R.string.event_day_time_text_view),
                        currentEvent.getDayOfWeek(), currentEvent.getTime());
            } else {
                text = currentEvent.getDayOfWeek();
            }
        }
        return text;
    }

    /**
     * Helper method to create the event data String to be shared when the user clicks the Share
     * button. Contents of the string will vary based upon whether the Event is an "always free"
     * Event or not.
     *
     * @return      The text that will be set to be shared when the user clicks the Share button.
     */
    static String createShareText(Event event, Context context){
        return event.getName() +"\n" + createDetailsText(event, context) + "\n\n"
                + String.format(context.getResources().getString(R.string.event_website_text),
                event.getWebsite());
    }

    /**
     * Helper method to create the String of event details to be displayed in the UI.
     *
     * @param event     The selected event.
     * @return          The String of event details to display.
     */
    static private String createDetailsText(Event event, Context context) {
        StringBuilder sb = new StringBuilder();

        //Add event description, if any
        if(!event.getDescription().contentEquals("")) {
            sb.append(event.getDescription());
            sb.append("\n");
        }

        //Add the event day, date and time, as appropriate depending upon whether this is a
        //free event or an always-free event.
        sb.append(SharedHelper.createTextEventDayDateTime(context, event));
        sb.append("\n");
        //Add notes, if any
        if(!event.getNotes().contentEquals("")) {
            sb.append(event.getNotes());
            sb.append("\n");
        }
        //Add event location
        sb.append(String.format(context.getResources()
                        .getString(R.string.event_location_text),
                SharedHelper.getBoroughName(event.getLocation())));
        return sb.toString();
    }

    /**
     * Helper method to return the month name.
     *
     * @param monthNumber   The month, in string format, expressed as a number from 01 to 12.
     * @return              The name of the Event month.
     */
    private static String getMonthName(String monthNumber){
        switch(monthNumber){
            case "01":
                return "January";
            case"02":
                return "February";
            case "03":
                return "March";
            case "04":
                return "April";
            case "05":
                return "May";
            case "06":
                return "June";
            case "07":
                return "July";
            case "08":
                return "August";
            case "09":
                return "September";
            case "10":
                return "October";
            case "11":
                return "November";
            case "12":
                return "December";
                default:
                    return "";
        }
    }

    /**
     * Helper method to reformat the event date string into a format comparable to today's date.
     *
     * @param eventDate The event date
     * @return  Reformatted event date string.
     */

    static String getComparableDate(String eventDate) {
        return eventDate.substring(0,2) + "/" + eventDate.substring(2,4) + "/"
                + eventDate.substring(4,6);
    }

    /**
     * Helper method to determine whether the Event is current, defined as the Event date is equal
     * to or after the current date and the Event end date, if one exists, is equal to or after
     * the Event date.
     *
     * @param date      The date of the Event.
     * @param endDate   The final date of the Event, for recurring Events.
     * @return          True if the Event is current, false if it is not.
     */
    static boolean eventIsCurrent(String date, String endDate, int alwaysFreeFlag){
        boolean eventIsCurrent = false;
        Date eventDate;
        Date eventEndDate;
        boolean noEndDate = true;
        Date todayDateInNY = getCurrentDateInNY();

        //An "always free" event is current if: (1) there's an end date and the end date is greater
        //than or equal to the current date; (2) there is no end date.
        if(alwaysFreeFlag == 1){
            if(!endDate.contentEquals("")) {
                try{
                    String compEndDate = SharedHelper.getComparableDate(endDate);
                    eventEndDate = mDateFormat.parse(compEndDate);
                    if(todayDateInNY.compareTo(eventEndDate) <= 0) {
                        eventIsCurrent = true;
                    }
                } catch(ParseException e1) {
                    e1.printStackTrace();
                }
            } else {
                eventIsCurrent = true;
            }
        } else {
            //If the event is not an "always free" event
            String compEventDate = SharedHelper.getComparableDate(date);

            try{
                eventDate = mDateFormat.parse(compEventDate);
                if(!endDate.contentEquals("")) {
                    String compEndDate = SharedHelper.getComparableDate(endDate);
                    eventEndDate = mDateFormat.parse(compEndDate);
                    noEndDate = false;
                } else {
                    eventEndDate = new java.util.Date();
                }
                //If the event date is today or after today and the event end date, if any,
                //is on or after the event date, the Event is current.
                if ((todayDateInNY.compareTo(eventDate) <= 0) && (noEndDate
                        || eventDate.compareTo(eventEndDate) <= 0))
                { eventIsCurrent = true; }
            } catch(ParseException e1){
                e1.printStackTrace();
            }
        }
        return eventIsCurrent;
    }

    /**
     * Helper method to get the current date in New York
     *
     * @return      The current date in New York.
     */
    private static Date getCurrentDateInNY() {
        //Initialize
        Date todayInNY = new java.util.Date();

        Calendar currentDateInNY = Calendar.getInstance();
        currentDateInNY.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        int todayYearInNY = currentDateInNY.get(Calendar.YEAR);
        int todayMonthInNY = currentDateInNY.get(Calendar.MONTH);
        int adjustedTodayMonthInNY = todayMonthInNY + ONE;
        int todayDayInNY = currentDateInNY.get(Calendar.DAY_OF_MONTH);

        //Convert integer year, month and date into strings in format comparable to the Event dates
        //and end dates.
        String yearString = Integer.toString(todayYearInNY
                - YEAR_CONSTANT);

        String monthPrefix = "";
        if(adjustedTodayMonthInNY < 10){
            monthPrefix = ZERO;
        }
        String monthString = monthPrefix + Integer.toString(adjustedTodayMonthInNY);

        String dayPrefix = "";
        if(todayDayInNY <10){
            dayPrefix = ZERO;
        }

        String dayString = dayPrefix + Integer.toString(todayDayInNY);

        String todayDateInNYString = yearString + monthString + dayString;

        String compDate = SharedHelper.getComparableDate(todayDateInNYString);
        try{
            todayInNY = mDateFormat.parse(compDate);
        } catch(ParseException e1) {
            e1.printStackTrace();
        }
        return todayInNY;
    }

    /**
     * Helper method to get the current year, month and day of the month in New York.
     *
     * @return      The current year, month and day of the month in integer format.
     */
    static int[] getCurrentYearMonthDayInNY() {
        Calendar currentDateInNY = Calendar.getInstance();
        currentDateInNY.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        return new int[] {currentDateInNY.get(Calendar.YEAR), currentDateInNY.get(Calendar.MONTH),
        currentDateInNY.get(Calendar.DAY_OF_MONTH)};
    }

    /**
     * Helper method to convert the date string to the appropriate integer values for use in the
     * Calendar Intent.
     *
     * @param eventDate     The Event date String.
     * @return              The date integers to be used in the Calendar Intent.
     */
    static int[] convertDate(String eventDate, Context context){
        int year = 0;
        int month = 0;
        int day = 0;

        try{
            year = Integer.parseInt(eventDate.substring(0,2))
                    + context.getResources().getInteger(R.integer.year_constant);
            month = Integer.parseInt(eventDate.substring(2,4))
                    - context.getResources().getInteger(R.integer.one);
            day = Integer.parseInt(eventDate.substring(4,6));

        } catch(NumberFormatException nfe) {
            nfe.printStackTrace();
        }

        return new int[] {year, month, day};
    }

    /**
     * Helper method to convert the time string to the appropriate integer values for
     * use in the Calendar Intent.
     *
     * @param eventTime     The Event time String.
     * @return              The time integers to be used in the Calendar Intent.
     */
    static int[] convertTime(String eventTime, Context context){
        String startTime;
        String endTime = "";
        int startHour;
        int startMinute;
        int endHour = 0;
        int endMinute = 0;
        int[] finishTime;

        if(!eventTime.contains("-")){
            startTime = eventTime;
        } else{
            //If eventTime contains a dash, separate it into strings representing the start and end
            //times.
            startTime = eventTime.substring(0,eventTime.indexOf("-")).trim();
            endTime = eventTime.substring(eventTime.indexOf("-")
                    + context.getResources().getInteger(R.integer.one)).trim();
        }

        //Convert the beginTime string into an array of integers containing the start hour and
        //minute.
        int[] beginTime = getHoursandMinutes(startTime, context);
        startHour = beginTime[0];
        startMinute = beginTime[1];

        //If an end time is specified, convert the endTime string into an array of integers
        //containing the end hour and minute.
        if (!eventTime.contains("-")){
            finishTime = getHoursandMinutes(endTime, context);
            endHour = finishTime[0];
            endMinute = finishTime[1];
        }

        return new int[] {startHour, startMinute, endHour, endMinute};
    }

    /**
     * Helper method to convert a time String ("hh:mmam" or "hh:mmpm") into integers for hours and
     * minutes, based on a 24 hour clock.
     *
     * @param timeString    The time string to be converted into hours and minutes.
     * @return              The hour and minute of the timeString, based on a 24 hour clock.
     */
    static private int[] getHoursandMinutes(String timeString, Context context){
        int hours = 0;
        int minutes = 0;
        int hourAdjustment = 0;

        try{
            //If the timeString does not contain a semi-colon, delete the non-digits, convert to
            //integer and adjust as necessary for the 24 hour clock used by Calendar Intents.
            if(!timeString.contains(":")){
                hours = Integer.parseInt(timeString.replaceAll("\\D+",""))
                        + hourAdjustment;
            } else {
                //If the timeString contains a semi-colon, separate it into hours and minutes for
                //use in the Calendar Intent.
                hours = Integer.parseInt(timeString.substring(0, timeString.indexOf(":")
                        + context.getResources().getInteger(R.integer.one))
                        .replaceAll("\\D+","")) + hourAdjustment ;
                minutes = Integer.parseInt(timeString.substring(timeString.indexOf(":")
                        + context.getResources().getInteger(R.integer.one))
                        .replaceAll("\\D+","")) ;
            }
            if(timeString.contains("p") && hours < 12) {
                hourAdjustment = context.getResources()
                        .getInteger(R.integer.twenty_four_hour_clock_constant);
            }
            hours = hours + hourAdjustment;
        } catch(NumberFormatException nfe) {
            nfe.printStackTrace();
        }

        return new int[]{hours, minutes};
    }

    /**
     * Helper method to make the events RecyclerView visible and the error message invisible.
     *
     * @param eventRecyclerView             The RecyclerView to be made visible.
     * @param errorMessage                  The error message TextView to be made invisible.
     */
    static void showEventInfo(RecyclerView eventRecyclerView, TextView errorMessage) {
        //Hide the error message.
        errorMessage.setVisibility(View.INVISIBLE);
        //Make the RecyclerView that contains the events visible.
        eventRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to make the error message View visible and hide the events RecyclerView.
     *
     * @param messageText               The error message text to be displayed.
     * @param eventRecyclerView         The RecyclerView to be hidden.
     * @param errorMessage              The error message TextView to be made visible.
     */
    static void showErrorMessage(String messageText, RecyclerView eventRecyclerView,
                                 TextView errorMessage) {
        //Hide the RecyclerView that contains the event info.
        eventRecyclerView.setVisibility(View.INVISIBLE);
        //Make the error message visible.
        errorMessage.setText(messageText);
        errorMessage.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to get the appropriate resource for the background for the
     * event type indicator textview
     *
     * @param eventType     The type of the given event, which determines the color of the
     *                      indicator background.
     * @return              The resource for the appropriate background.
     */
    static int getBackgroundColorForIndicator (String eventType) {
        if(eventType != null) {
            switch (eventType){
                case "C":
                    return R.drawable.circle_red;
                case"E":
                    return R.drawable.circle_orange;
                case "T":
                    return R.drawable.circle_yellow;
                case "O":
                    return R.drawable.circle_green;
                case "M":
                    return R.drawable.circle_blue;
                case "P":
                    return R.drawable.circle_indigo;
                case "F":
                    return R.drawable.circle_violet;
                default:
                    return R.drawable.circle_green;
            }
        } else {
            //If the event has no eventType associated with it, assume the default "Other"
            //event type and return the green circle background.
            return R.drawable.circle_green;
        }
    }

    /**
     * Helper method to create and display a Toast in the middle of the screen.
     *
     * @param toastMessageResourceId        The resource id for the Toast message
     * @param context                       The context for the Toast
     */
    static void makeAndDisplayToast(int toastMessageResourceId, Context context) {
        Toast toast = Toast.makeText(context,
                context.getString(toastMessageResourceId), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}
