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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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

        DateTime startDateTime = new DateTime(event.getStartTime().getTime()).withZone(DateTimeZone.UTC);

        Event eventToEdit = EnHueco.getInstance().getAppUser().getSchedule().getWeekDays()[startDateTime.getDayOfWeek()]
                .getEventWithStartHour(startDateTime.toLocalTime()).get();

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
        ArrayList<WeekViewEvent> events = new ArrayList<>();

        // Get first day of month
        DateTime time = new DateTime(DateTimeZone.UTC).withYear(newYear).withMonthOfYear(newMonth).withDayOfMonth(1);

        int id = 0;
        // Iterate through month
        while (time.getMonthOfYear() == newMonth)
        {
            // Add all events current weekday
            int newEventWeekday = time.getDayOfWeek();
            Collection<Event> currentWeekDayEvents = EnHueco.getInstance().getAppUser().getSchedule().getWeekDays()[newEventWeekday].getEvents();

            for (Event currentEvent : currentWeekDayEvents)
            {
                // Update global Calendar to match start time

                DateTime startTime = time.withHourOfDay(currentEvent.getStartHour().getHourOfDay()).withMinuteOfHour
                        (currentEvent.getStartHour().getMinuteOfHour()).withZone(DateTimeZone.forTimeZone(TimeZone.getDefault()));

                DateTime endTime = time.withHourOfDay(currentEvent.getEndHour().getHourOfDay()).withMinuteOfHour
                        (currentEvent.getEndHour().getMinuteOfHour()).withZone(DateTimeZone.forTimeZone(TimeZone.getDefault()));

                // Add weekViewEvent
                WeekViewEvent weekViewEvent = new WeekViewEvent(id++, currentEvent.getName().get(), startTime.toCalendar(null), endTime.toCalendar(null));
                weekViewEvent.setColor(currentEvent.getType().equals(Event.EventType.FREE_TIME) ? Color.argb(35, 0, 150, 245) : Color.argb(35, 255, 213, 0));
                events.add(weekViewEvent);
            }
            time = time.plusDays(1);
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
