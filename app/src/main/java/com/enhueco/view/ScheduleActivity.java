package com.enhueco.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.bumptech.glide.util.Util;
import com.enhueco.R;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.Event;
import com.enhueco.model.model.User;
import com.enhueco.model.other.Utilities;

import java.util.*;

public class ScheduleActivity extends AppCompatActivity implements WeekView.EventLongPressListener, WeekView.EventClickListener, WeekView.MonthChangeListener, WeekView.EmptyViewClickListener
{
    public static final String SCHEDULE_EXTRA = "Schedule";
    private static final String LOG = "ScheduleActivity";
    private FloatingActionButton fab;
    private WeekView mWeekView;

    private User user = EnHueco.getInstance().getAppUser();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        for (User user : EnHueco.getInstance().getAppUser().getFriends().values())
        {
            if (user.getID().equals(getIntent().getStringExtra("friendID")))
            {
                this.user = user;
                break;
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        fab = (FloatingActionButton) findViewById(R.id.addEventButton);
        if (user != EnHueco.getInstance().getAppUser()) fab.setVisibility(View.GONE);

        mWeekView = (WeekView) findViewById(R.id.weekView);

        mWeekView.setNumberOfVisibleDays(3);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Mi Horario");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set an action when any event is clicked.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        findViewById(R.id.importButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ScheduleActivity.this, SelectCalendarActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect)
    {
        Intent intent = new Intent(this, AddEditEventActivity.class);

        Calendar startHour = event.getStartTime();
        int millis = startHour.getTimeZone().getOffset(startHour.getTimeInMillis());
        startHour.add(Calendar.MILLISECOND, +millis);

        Log.v("SCHEDULE ACTIVITY 1", startHour.get(Calendar.HOUR_OF_DAY) + "");
        Log.v("SCHEDULE ACTIVITY 1.1", startHour.getTimeZone().getDisplayName() + "");
        startHour.setTimeZone(Utilities.getDeviceTimezone());
        Log.v("SCHEDULE ACTIVITY 1.2", startHour.getTimeZone().getDisplayName() + "");
        Log.v("SCHEDULE ACTIVITY 2", startHour.get(Calendar.HOUR_OF_DAY) + "");

        int localWeekday = startHour.get(Calendar.DAY_OF_WEEK);

        startHour.setTimeZone(TimeZone.getTimeZone("UTC"));
        Log.v("SCHEDULE ACTIVITY 2.1", startHour.getTimeZone().getDisplayName() + "");
        Log.v("SCHEDULE ACTIVITY 3", startHour.get(Calendar.HOUR_OF_DAY) + "");
        Event eventToEdit = EnHueco.getInstance().getAppUser().getSchedule().getWeekDays()[localWeekday].getEventWithStartHour(startHour).get();

        intent.putExtra("eventToEdit", eventToEdit);
        startActivity(intent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mWeekView.notifyDatasetChanged();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect)
    {
        Log.v("Schedule Activity", "EVENT LONG PRESS");
    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth)
    {
        Log.v("MONTH CHANGED", "" + newYear + "-" + newMonth);
        ArrayList<WeekViewEvent> events = new ArrayList<>();

        // Get first day of month
        Calendar globalCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        globalCalendar.set(Calendar.YEAR, newYear);
        globalCalendar.set(Calendar.MONTH, newMonth);

        long id = 0;


        // Iterate through month
        while (globalCalendar.get(Calendar.MONTH) == newMonth)
        {
            //TODO: Reuse already generated week elements
            // Add all events current weekday
            int newEventWeekday = globalCalendar.get(Calendar.DAY_OF_WEEK);

            Collection<Event> currentWeekDayEvents = user.getSchedule().getWeekDays()[newEventWeekday].getEvents();

            for (Event currentEvent : currentWeekDayEvents)
            {
                Calendar calendar = Calendar.getInstance();
                int difMillis = calendar.getTimeZone().getDefault().getOffset(calendar.getTimeInMillis());

                Calendar startTimeEvent = (Calendar) currentEvent.getStartHour().clone();
                startTimeEvent.setTimeZone(Utilities.getDeviceTimezone());

                Calendar endTimeEvent = (Calendar) currentEvent.getEndHour().clone();
                endTimeEvent.setTimeZone(Utilities.getDeviceTimezone());


                Calendar startCalendar = (Calendar) globalCalendar.clone();
                startCalendar.set(Calendar.HOUR_OF_DAY, startTimeEvent.get(Calendar.HOUR_OF_DAY));
                startCalendar.set(Calendar.MINUTE, startTimeEvent.get(Calendar.MINUTE));
                startCalendar.add(Calendar.MILLISECOND, difMillis);


                Calendar endCalendar = (Calendar) globalCalendar.clone();
                endCalendar.set(Calendar.HOUR_OF_DAY, endTimeEvent.get(Calendar.HOUR_OF_DAY));
                endCalendar.set(Calendar.MINUTE, endTimeEvent.get(Calendar.MINUTE));
                endCalendar.add(Calendar.MILLISECOND, difMillis);

                // Add weekViewEvent
                WeekViewEvent weekViewEvent = new WeekViewEvent(id++, currentEvent.getName().get(), startCalendar, endCalendar);
                weekViewEvent.setColor(currentEvent.getType().equals(Event.EventType.FREE_TIME) ? Color.argb(35, 0, 150, 245) : Color.argb(35, 255, 213, 0));

                events.add(weekViewEvent);
            }

            globalCalendar.add(Calendar.DATE, 1);
        }


            return events;
    }

    @Override
    public void onEmptyViewClicked(Calendar time)
    {

    }

    public void addEvent(View view)
    {
        Intent intent = new Intent(this, AddEditEventActivity.class);
        startActivity(intent);
    }
}
