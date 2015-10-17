package com.diegoalejogm.enhueco.View;

import android.content.Intent;
import android.graphics.RectF;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.R;
import android.support.v7.widget.Toolbar;

import java.util.*;

import com.diegoalejogm.enhueco.Model.MainClasses.System;

public class ScheduleActivity extends AppCompatActivity implements WeekView.EventLongPressListener, WeekView.EventClickListener, WeekView.MonthChangeListener, WeekView.EmptyViewClickListener
{



    public static final String SCHEDULE_EXTRA = "Schedule";
    WeekView mWeekView;
    Schedule schedule;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mWeekView = (WeekView) findViewById(R.id.weekView);

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
    }


    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect)
    {
//        Intent intent = new Intent(this, AddEditEventActivity.class);
//        intent.putExtra(EVENT_EXTRA, event.getId());
//        startActivity(intent);
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

        Log.v("MONTH CHANGED", ""+ newYear + "-" + newMonth);
        ArrayList<WeekViewEvent> events = new ArrayList<>();

        // Get first day of month
        Calendar globalCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        globalCalendar.set(Calendar.YEAR, newYear);
        globalCalendar.set(Calendar.MONTH, newMonth);
        globalCalendar.set(Calendar.DAY_OF_MONTH, 1);

//        Calendar temp = Calendar.getInstance(/*TimeZone.getTimeZone("UTC"))*/);
//        Calendar temp2 = (Calendar) temp.clone(); temp2.add(Calendar.HOUR_OF_DAY, 2);
//        System.instance.getAppUser().getSchedule().getWeekDays()[5].addEvent(new Event(Event.EventType.CLASS, temp, temp2));

        int id = 0;
        // Iterate through month
        while (globalCalendar.get(Calendar.MONTH) == newMonth)
        {
            // Add all events current weekday
            int newEventWeekday = globalCalendar.get(Calendar.DAY_OF_WEEK);
            List<Event> currentWeekDayEvents = System.instance.getAppUser().getSchedule().getWeekDays()[newEventWeekday].getEvents();
            for (int j = 0; j < currentWeekDayEvents.size(); j++)
            {
                Event currentEvent = currentWeekDayEvents.get(j);

                // Update global Calendar to match start time
                globalCalendar.set(Calendar.HOUR_OF_DAY, currentEvent.getStartHour().get(Calendar.HOUR_OF_DAY));
                globalCalendar.set(Calendar.MINUTE, currentEvent.getStartHour().get(Calendar.MINUTE));

                // Set global calendar time in start local calendar
                Calendar startCalendarLocal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                startCalendarLocal.setTimeInMillis(globalCalendar.getTimeInMillis());

                // Update global Calendar to match end time
                globalCalendar.set(Calendar.HOUR_OF_DAY, currentEvent.getEndHour().get(Calendar.HOUR_OF_DAY));
                globalCalendar.set(Calendar.MINUTE, currentEvent.getEndHour().get(Calendar.MINUTE));

                // Set global calendar time in end local calendar
                Calendar endCalendarLocal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                endCalendarLocal.setTimeInMillis(globalCalendar.getTimeInMillis());

                // Add weekViewEvent
                events.add(new WeekViewEvent(id++, currentEvent.getName().get(), startCalendarLocal, endCalendarLocal));
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
