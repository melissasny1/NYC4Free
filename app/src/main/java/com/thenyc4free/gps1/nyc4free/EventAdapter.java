package com.thenyc4free.gps1.nyc4free;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

/**
 * Create and populate view holders for the Event RecyclerView.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder>{

    private final EventClickListener mEventClickListener;
    private List<Event> mEvents;
    private final Context mContext;
    private boolean mHideDeleteButton;
    private int mExpandedPosition;
    private int mPreviousExpandedPosition = -1;
    //Indicator that has a value of true when the smallest screen width is greater than or equal
    //to 600dp.
    private final boolean mSmallestScreenWidthGTE600;

    EventAdapter (Context context, EventClickListener eventClickListener, boolean screenWidth){
        mContext = context;
        mEventClickListener = eventClickListener;
        //Flag to indicate whether the delete button in the list item should be hidden
        mHideDeleteButton = true;
        mSmallestScreenWidthGTE600 = screenWidth;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item_event;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder eventViewHolder, int position) {
        //Retrieve the Event Object for the current position.
        Event currentEvent = mEvents.get(position);
        //Retrieve the event type and set the appropriate drawable as the background for the event
        //type indicator textview.
        eventViewHolder.mEventTypeIndicator
                .setBackgroundResource(SharedHelper
                        .getBackgroundColorForIndicator(currentEvent.getEventType()));
        //Retrieve the Event data to be bound.
        String eventName = currentEvent.getName();
        String eventLocation = currentEvent.getLocation();
        String eventDescription = currentEvent.getDescription();
        String eventNotes = currentEvent.getNotes();

        eventViewHolder.mEventNameTextView.setText(eventName);

        eventViewHolder.mEventDayDateTimeTextView.setText(SharedHelper
                .createTextEventDayDateTime(mContext, currentEvent));

        eventViewHolder.mEventLocationTextView.setText(String.format(mContext
                .getString(R.string.event_location_text), SharedHelper
                .getBoroughName(eventLocation)));

        eventViewHolder.mEventDescriptionTextView.setVisibility(View.VISIBLE);
        eventViewHolder.mEventNotesTextView.setVisibility(View.VISIBLE);
        eventViewHolder.mEventDescriptionTextView.setText(eventDescription);
        eventViewHolder.mEventNotesTextView.setText(eventNotes);

        if(mHideDeleteButton){
            //When the MainActivity Favorites menu option is not selected, hide the favorite delete
            //button and make visible the button to create a favorite.
            eventViewHolder.mFavoriteDeleteButton.setVisibility(View.GONE);
            eventViewHolder.mFavoriteButton.setVisibility(View.VISIBLE);
        } else {
            //When the MainActivity Favorites menu option is selected, make visible the favorite
            //delete button and hide the button to create a favorite.
            eventViewHolder.mFavoriteDeleteButton.setVisibility(View.VISIBLE);
            eventViewHolder.mFavoriteButton.setVisibility(View.GONE);
        }

        //Determine whether the view holder is in the expanded position and set the visibility
        //of the two expansion linear layouts and the activated status of the button to expand
        //the CardView appropriately.
        eventViewHolder.mIsExpanded = position==mExpandedPosition;

        //The layout for devices with the smallest width greater than or equal to 600dp eliminates
        //the button to expand the item view and automatically displays all fields.  For these
        //devices, mIsExpanded is always set to true.
        if(mSmallestScreenWidthGTE600) {
            eventViewHolder.mIsExpanded = true;
        }

        eventViewHolder.mExpTextLinearLayout
                .setVisibility(eventViewHolder.mIsExpanded?View.VISIBLE:GONE);
        eventViewHolder.mExpButtonLinearLayout
                .setVisibility(eventViewHolder.mIsExpanded?View.VISIBLE:GONE);
        eventViewHolder.mButtonExpandCardView.setActivated(eventViewHolder.mIsExpanded);

        //If the eventViewHolder is expanded and the event has no description and/or notes, remove
        //the empty view.
        if(eventViewHolder.mIsExpanded && eventDescription.contentEquals("")) {
            eventViewHolder.mEventDescriptionTextView.setVisibility(View.GONE);
        }
        if(eventViewHolder.mIsExpanded && eventNotes.contentEquals("")) {
            eventViewHolder.mEventNotesTextView.setVisibility(View.GONE);
        }

        if(eventViewHolder.mIsExpanded) {
            mPreviousExpandedPosition = position;
            eventViewHolder.mButtonExpandCardView
                    .setBackgroundResource(R.drawable.baseline_expand_less_black_48);
        } else {
            eventViewHolder.mButtonExpandCardView
                    .setBackgroundResource(R.drawable.baseline_expand_more_black_48);
        }
    }

    @Override
    public int getItemCount() {
        int numberOfItems = 0;
        if(mEvents != null && mEvents.size() > 0){
            numberOfItems = mEvents.size();
        }
        return numberOfItems;
    }

    /**
     * Set the Event data on the EventAdapter if one has already been created.
     *
     * @param events The new event data to be displayed.
     */
    void setEventData(List<Event> events, boolean hideDeleteButton, int savedStateAdapterPosition,
                      boolean hasStateChanged) {
        //Set the value of expanded position to -1 when the Adapter data set is changed.  This
        //ensures that if the user left an event CardView in the expanded position before switching
        //to a different data view, none of the CardViews displaying the new data will already be
        //expanded.  When state has changed, set the expanded position to the saved state adapter
        //position value, so that view will be expanded if it was expanded when the state was
        //changed.
        mExpandedPosition = hasStateChanged ? savedStateAdapterPosition:-1;
        mEvents = events;
        mHideDeleteButton = hideDeleteButton;
        notifyDataSetChanged();
    }

    //Create an interface to define the listener.
    interface EventClickListener {
        void onDeleteClick(Event event);
        void onFavoriteButtonClick(Event event);
        void onWebsiteButtonClick(Event event);
        void onShareButtonClick(Event event);
        void onCalendarButtonClick(Event event);
        void onButtonExpandCardViewClick(int eventPosition, int expandedPosition);
    }

    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //Create a member variable for each view in the item view.
        @BindView(R.id.circle_tv) TextView mEventTypeIndicator;
        @BindView(R.id.event_title_tv) TextView mEventNameTextView;
        @BindView(R.id.event_day_date_time_tv) TextView mEventDayDateTimeTextView;
        @BindView(R.id.event_location_tv) TextView mEventLocationTextView;
        @BindView(R.id.expansion_text_linear_layout) LinearLayout mExpTextLinearLayout;
        @BindView(R.id.event_description_tv) TextView mEventDescriptionTextView;
        @BindView(R.id.event_notes_tv) TextView mEventNotesTextView;
        @BindView(R.id.delete_favorite_button) ImageButton mFavoriteDeleteButton;
        @BindView(R.id.expansion_button_linear_layout) LinearLayout mExpButtonLinearLayout;
        @BindView(R.id.favorite_button) Button mFavoriteButton;
        @BindView(R.id.website_button) Button mWebsiteButton;
        @BindView(R.id.share_button) Button mShareButton;
        @BindView(R.id.calendar_button) Button mCalendarButton;
        @BindView(R.id.expansion_button) Button mButtonExpandCardView;

        //Indicator of whether the item view is expanded or collapsed.
        boolean mIsExpanded;

        EventViewHolder(final View itemView){
            super(itemView);
            ButterKnife.bind(this,itemView);

            //Set listeners on UI buttons.
            mFavoriteDeleteButton.setOnClickListener(this);
            mFavoriteButton.setOnClickListener(this);
            mWebsiteButton.setOnClickListener(this);
            mShareButton.setOnClickListener(this);
            mCalendarButton.setOnClickListener(this);
            mButtonExpandCardView.setOnClickListener(this);

            mIsExpanded = false;
        }

        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            //Get the position of the Event clicked.
            int clickedPosition = getAdapterPosition();
            //Get the Event associated with the clicked position
            Event clickedEvent = mEvents.get(clickedPosition);
            //Invoke the onClickListener of the Adapter class.
            if(viewId == mFavoriteDeleteButton.getId()){
                //If the user clicks on the delete button, call onDeleteClick()
                mEventClickListener.onDeleteClick(clickedEvent);
            } else if(viewId == mFavoriteButton.getId()){
                //If the user clicks the favorite button, call onFavoriteButtonClick()
                mEventClickListener.onFavoriteButtonClick(clickedEvent);
            } else if(viewId == mWebsiteButton.getId()) {
                //If the user clicks the website button, call onWebsiteButtonClick()
                mEventClickListener.onWebsiteButtonClick(clickedEvent);
            } else if(viewId == mShareButton.getId()) {
                //If the user clicks the share button, call onShareButtonClick()
                mEventClickListener.onShareButtonClick(clickedEvent);
            } else if(viewId == mCalendarButton.getId()) {
                //If the user clicks the calendar button, call onCalendarButtonClick()
                mEventClickListener.onCalendarButtonClick(clickedEvent);
            } else if(viewId == mButtonExpandCardView.getId()) {
                //If the user clicks the More button to expand the event's CardView, call
                //onButtonExpandCardView
                mExpandedPosition = mIsExpanded ? -1:clickedPosition;
                mEventClickListener.onButtonExpandCardViewClick(clickedPosition, mExpandedPosition);
                notifyItemChanged(mPreviousExpandedPosition);
                notifyItemChanged(clickedPosition);
            }
        }
    }
}
