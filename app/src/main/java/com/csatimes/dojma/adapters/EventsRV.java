package com.csatimes.dojma.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.csatimes.dojma.R;
import com.csatimes.dojma.models.EventItem;
import com.csatimes.dojma.viewholders.EventItemViewHolder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmResults;

/**
 * Created by Vikramaditya Kukreja on 19-07-2016.
 */

public class EventsRV extends RecyclerView.Adapter<EventItemViewHolder> {

    private Context context;
    private int pos;
    private RealmResults<EventItem> eventItems;
    private Date currentDate;

    public EventsRV(Context context, RealmResults<EventItem> eventItems, Date currentDate) {
        this.context = context;
        this.eventItems = eventItems;
        this.currentDate = currentDate;
    }

    @Override
    public EventItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View event_item_format = inflater.inflate(R.layout.event_item_format, parent, false);
        // Return a new holder instance
        return new EventItemViewHolder(event_item_format);
    }

    @Override
    public void onBindViewHolder(EventItemViewHolder holder, int position) {
        pos = holder.getAdapterPosition();
        try {
            holder.title.setText(eventItems.get(pos).getTitle());
            holder.desc.setText(eventItems.get(pos).getDesc());
            holder.location.setText(eventItems.get(pos).getLocation());

            DateFormat originalFormat = new SimpleDateFormat("ddMMyyyyHHmm", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("EEE, dd MMM h:mm a", Locale.UK);
            DateFormat editedOriginalFormat = new SimpleDateFormat("ddMMyyyy", Locale.ENGLISH);
            DateFormat editedTargetFormat = new SimpleDateFormat("EEE, dd MMM", Locale.ENGLISH);


            Date date = null;
            Date end = null;

            String startDateText = "";
            try {
                //Check if start time exists
                if (!eventItems.get(pos).getStartTime().equalsIgnoreCase("-")) {
                    date = originalFormat.parse(eventItems.get(pos).getStartDate() +
                            eventItems.get(pos).getStartTime());
                    startDateText = targetFormat.format(date);
                    Log.e("TAG", eventItems.get(pos).getEndTime() + " end time");
                    //check if there is an end time
                    if (!eventItems.get(pos).getEndTime().equalsIgnoreCase("-")) {
                        //check if there is an end date otherwise set it to startdate
                        if (!eventItems.get(pos).getEndDate().equalsIgnoreCase("-")) {
                            Log.e("TAG", "end date is not -");
                            end = originalFormat.parse(eventItems.get(pos).getEndDate() +
                                    eventItems.get(pos).getEndTime());
                        } else {
                            Log.e("TAG", "end date is - using start");

                            end = originalFormat.parse(eventItems.get(pos).getStartDate() +
                                    eventItems.get(pos).getEndTime());
                        }

                    } else {
                        //end time is unknown, set end to null
                        end = null;
                        Log.e("TAG", "end set to null");
                    }
                } else {
                    Log.e("TAG", "start time lo1");
                    //start time does not exist . only use day
                    //don't bother for end time or date then
                    date = editedOriginalFormat.parse(eventItems.get(pos).getStartDate());
                    startDateText = editedTargetFormat.format(date);
                    end = null;
                }

                //If start time is known, only then we can consider time calculation
                if (!eventItems.get(pos).getStartTime().equalsIgnoreCase("-")) {
                    long datesDiff = date.getTime() - currentDate.getTime();
                    if (end != null) {
                        long endDiff = end.getTime() - currentDate.getTime();

                        long days = datesDiff / (24 * 60 * 60 * 1000);
                        long hours = datesDiff / (60 * 60 * 1000) % 24;
                        long minutes = datesDiff / (60 * 1000) % 60;


                        if (datesDiff >= 0) {
                            if (days == 0) {
                                if (hours == 1) {
                                    holder.status.setText("STARTING IN THE NEXT HOUR");
                                    holder.status.setTextColor(ContextCompat.getColor(context, R.color.teal500));
                                } else if (hours > 1) {
                                    holder.status.setText("IN " + hours + " HOURS");
                                    holder.status.setTextColor(ContextCompat.getColor(context, R.color.green700));
                                } else if (hours == 0) {

                                    if (minutes != 1 || minutes != 0) {
                                        holder.status.setText("IN " + minutes + " MINUTES ");
                                        holder.status.setTextColor(ContextCompat.getColor(context, R.color.green500));

                                    } else {
                                        holder.status.setText("STARTING");
                                        holder.status.setTextColor(ContextCompat.getColor(context, R.color.lightblue500));
                                    }
                                } else {
                                    Log.e("TAG", "used this");
                                    holder.status.setText("EVENT OVER");
                                    holder.status.setTextColor(ContextCompat.getColor(context, R.color.red500));
                                }


                            } else if (days > 0) {
                                if (days == 1) {
                                    holder.status.setText("IN 1 DAY " + hours + " HOUR(s)");
                                    holder.status.setTextColor(ContextCompat.getColor(context, R.color.grey900));
                                } else {
                                    holder.status.setText("AFTER " + days + " DAYS");
                                    holder.status.setTextColor(ContextCompat.getColor(context, R.color.grey500));
                                }
                            }

                        } else if (datesDiff < 0 && endDiff >= 0 && endDiff != 0) {
                            holder.status.setText("ONGOING");
                            holder.status.setTextColor(ContextCompat.getColor(context, R.color.lightblue500));

                        } else {

                            holder.status.setText("EVENT OVER");
                            holder.status.setTextColor(ContextCompat.getColor(context, R.color.red500));
                        }


                    } else {
                        //end is null , but we have start date and time
                        //we can't show end date time or if event is ongoing
                        long days = datesDiff / (24 * 60 * 60 * 1000);
                        long hours = datesDiff / (60 * 60 * 1000) % 24;
                        long minutes = datesDiff / (60 * 1000) % 60;

                        if (datesDiff >= 0) {
                            if (days == 0) {
                                if (hours == 1) {
                                    holder.status.setText("STARTING IN THE NEXT HOUR");
                                    holder.status.setTextColor(ContextCompat.getColor(context, R.color.teal500));
                                } else if (hours > 1) {
                                    holder.status.setText("IN " + hours + " HOURS");
                                    holder.status.setTextColor(ContextCompat.getColor(context, R.color.green700));
                                } else if (hours == 0) {

                                    if (minutes != 1 || minutes != 0) {
                                        holder.status.setText("IN " + minutes + " MINUTES ");
                                        holder.status.setTextColor(ContextCompat.getColor(context, R.color.green500));

                                    } else {
                                        holder.status.setText("STARTING");
                                        holder.status.setTextColor(ContextCompat.getColor(context, R.color.lightblue500));
                                    }
                                } else {
                                    holder.status.setText("");
                                }

                            } else if (days > 0) {
                                if (days == 1) {
                                    holder.status.setText("IN 1 DAY " + hours + " HOUR(s)");
                                    holder.status.setTextColor(ContextCompat.getColor(context, R.color.grey900));
                                } else {
                                    holder.status.setText("AFTER " + days + " DAYS");
                                    holder.status.setTextColor(ContextCompat.getColor(context, R.color.grey500));
                                }
                            }

                        } else {
                            if (days > 1) {
                                holder.status.setText("EVENT MAY BE OVER");
                                holder.status.setTextColor(Color.YELLOW);
                            } else {
                                holder.status.setText("EVENT STATUS");
                            }
                        }
                    }
                } else {
                    //calculate only for date changes due to unavailability of start time
                    //needs work
                    holder.status.setText("TIME NOT SET");

                }

            } catch (ParseException e) {
                startDateText = eventItems.get(pos).getStartDate() + " " + eventItems.get(pos)
                        .getStartTime();
                holder.status.setText("EVENT STATUS");
            }
            holder.datetime.setText(startDateText);
            final String temptitle = eventItems.get(pos).getTitle();
            final long tempStartTime = date.getTime();
            final long tempEndTime;
            if (eventItems.get(pos).getEndTime().equalsIgnoreCase("-"))
                tempEndTime = date.getTime() + 1 * 60 * 60 * 1000;
            else
                tempEndTime = end.getTime();
            final String tempDesc = eventItems.get(pos).getDesc();
            final String tempLocation = eventItems.get(pos).getLocation();

//This is wrong...cant use setDate method...instead try to re convert date inside onclicklistener
            if (!eventItems.get(pos).getStartTime().equalsIgnoreCase("-")) {


                holder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {


                            Intent intent = new Intent(Intent.ACTION_INSERT);
                            intent.setData(CalendarContract.Events.CONTENT_URI);
                            if (tempStartTime >= currentDate.getTime())
                                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, tempStartTime);
                            if (tempEndTime >= currentDate.getTime())
                                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, tempEndTime);
                            intent.putExtra(CalendarContract.Events.TITLE, temptitle);
                            intent.putExtra(CalendarContract.Events.DESCRIPTION, tempDesc);
                            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, tempLocation);
                            context.startActivity(intent);

                        } catch (ActivityNotFoundException e) {
                            Snackbar.make(view, "No calendar found", Snackbar.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(context, "Sorry could not open calendar. ", Toast
                                    .LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                holder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "Start time is not known yet!", Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.status.setText("REPORT TO ADMIN/DOJMA");
        }
    }

    @Override
    public int getItemCount() {
        if (eventItems != null) {
            return eventItems.size();
        } else {
            return 0;
        }
    }

}