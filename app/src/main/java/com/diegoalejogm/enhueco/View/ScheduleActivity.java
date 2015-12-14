package com.diegoalejogm.enhueco.View;

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
import com.diegoalejogm.enhueco.Model.MainClasses.Event;
import com.diegoalejogm.enhueco.Model.MainClasses.Schedule;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class ScheduleActivity extends AppCompatActivity implements WeekView.EventLongPressListener, WeekView.EventClickListener, WeekView.MonthChangeListener, WeekView.EmptyViewClickListener
{
    public static final String SCHEDULE_EXTRA = "Schedule";
    private static final String LOG = "ScheduleActivity";
    private FloatingActionButton fab;
    private WeekView mWeekView;
    private Schedule schedule = System.getInstance().getAppUser().getSchedule();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        fab = (FloatingActionButton) findViewById(R.id.addEventButton);
        if (schedule != System.getInstance().getAppUser().getSchedule()) fab.setVisibility(View.GONE);

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
        startHour.setTimeZone(TimeZone.getDefault());

        int localWeekday = startHour.get(Calendar.DAY_OF_WEEK);

        startHour.setTimeZone(TimeZone.getTimeZone("UTC"));
        Event eventToEdit = System.getInstance().getAppUser().getSchedule().getWeekDays()[localWeekday].getEventWithStartHour(startHour).get();

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
        Log.v("MONTH CHANGED", ""+ newYear + "-" + newMonth);
        ArrayList<WeekViewEvent> events = new ArrayList<>();

        // Get first day of month
        Calendar globalCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        globalCalendar.set(Calendar.YEAR, newYear);
        globalCalendar.set(Calendar.MONTH, newMonth);
        globalCalendar.set(Calendar.DAY_OF_MONTH, 1);

//        Calendar temp = Calendar.getInstance(/*TimeZone.getTimeZone("UTC"))*/);
//        Calendar temp2 = (Calendar) temp.clone(); temp2.add(Calendar.HOUR_OF_DAY, 2);
//        System.getInstance().getAppUser().getSchedule().getWeekDays()[5].addEvent(new Event(Event.EventType.CLASS, temp, temp2));

        int id = 0;
        // Iterate through month
        while (globalCalendar.get(Calendar.MONTH) == newMonth)
        {
            // Add all events current weekday
            int newEventWeekday = globalCalendar.get(Calendar.DAY_OF_WEEK);

            List<Event> currentWeekDayEvents = schedule.getWeekDays()[newEventWeekday].getEvents();

            for (int j = 0; j < currentWeekDayEvents.size(); j++)
            {
                Event currentEvent = currentWeekDayEvents.get(j);

                Calendar startTimeEvent = (Calendar) currentEvent.getStartHour().clone();
                startTimeEvent.setTimeZone(TimeZone.getDefault());

                Calendar endTimeEvent = (Calendar) currentEvent.getEndHour().clone();
                endTimeEvent.setTimeZone(TimeZone.getDefault());

                // Update global Calendar to match start time
//                Log.v(LOG, "Event start: " + currentEvent.getStartHour().get(Calendar.HOUR_OF_DAY) + ":" + currentEvent.getStartHour().get(Calendar.MINUTE));
//                Log.v(LOG, "Timezone start: " + currentEvent.getStartHour().getTimeZone().getDisplayName());
//
//                Log.v(LOG, "Event end: " + currentEvent.getEndHour().get(Calendar.HOUR_OF_DAY) + ":" + currentEvent.getEndHour().get(Calendar.MINUTE));
//                Log.v(LOG, "Timezone end: " + currentEvent.getEndHour().getTimeZone().getDisplayName());
//                globalCalendar.set(Calendar.HOUR_OF_DAY, currentEvent.getStartHour().get(Calendar.HOUR_OF_DAY));
//                globalCalendar.set(Calendar.MINUTE, currentEvent.getStartHour().get(Calendar.MINUTE));
//                globalCalendar.set(Calendar.DAY_OF_WEEK, currentEvent.getStartHour().get(Calendar.DAY_OF_WEEK));

                // Set global calendar time in start local calendar
                Calendar startCalendarLocal = (Calendar) globalCalendar.clone();
                startCalendarLocal.setTimeZone(TimeZone.getDefault());
                startCalendarLocal.set(Calendar.HOUR_OF_DAY, startTimeEvent.get(Calendar.HOUR_OF_DAY));
                startCalendarLocal.set(Calendar.MINUTE, startTimeEvent.get(Calendar.MINUTE));

                Calendar endCalendarLocal = (Calendar) globalCalendar.clone();
                endCalendarLocal.setTimeZone(TimeZone.getDefault());
                endCalendarLocal.set(Calendar.HOUR_OF_DAY, endTimeEvent.get(Calendar.HOUR_OF_DAY));
                endCalendarLocal.set(Calendar.MINUTE, endTimeEvent.get(Calendar.MINUTE));


//                Log.v(LOG, "Event start: " + startCalendarLocal.get(Calendar.HOUR_OF_DAY) + ":" + startCalendarLocal.get(Calendar.MINUTE));


//                // Update global Calendar to match end time
//                globalCalendar.set(Calendar.HOUR_OF_DAY, currentEvent.getEndHour().get(Calendar.HOUR_OF_DAY));
//                globalCalendar.set(Calendar.MINUTE, currentEvent.getEndHour().get(Calendar.MINUTE));

                // Set global calendar time in end local calendar
//                Calendar endCalendarLocal = (Calendar) currentEvent.getEndHour().clone();
//                endCalendarLocal.setTimeZone(TimeZone.getDefault());
//                startCalendarLocal.set(Calendar.YEAR, globalCalendar.get(Calendar.YEAR));
//                endCalendarLocal.set(Calendar.DAY_OF_MONTH, globalCalendar.get(Calendar.DAY_OF_MONTH));
//                endCalendarLocal.set(Calendar.MONTH, globalCalendar.get(Calendar.MONTH));
//                startCalendarLocal.set(Calendar.DAY_OF_WEEK, globalCalendar.get(Calendar.DAY_OF_WEEK));

//                Log.v(LOG, "Event end: " + endCalendarLocal.get(Calendar.HOUR_OF_DAY) + ":" + endCalendarLocal.get(Calendar.MINUTE));
                // Add weekViewEvent

                WeekViewEvent weekViewEvent = new WeekViewEvent(id++, currentEvent.getName().get(), startCalendarLocal, endCalendarLocal);
                weekViewEvent.setColor(currentEvent.getType().equals(Event.EventType.FREE_TIME)? Color.argb(35, 0, 150, 245) : Color.argb(35, 255, 213, 0));

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

    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;

        if (schedule != System.getInstance().getAppUser().getSchedule()) fab.setVisibility(View.GONE);
    }
}
