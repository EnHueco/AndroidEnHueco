package com.diegoalejogm.enhueco.view;

import android.app.Fragment;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.diegoalejogm.enhueco.model.main.Event;
import com.diegoalejogm.enhueco.model.main.Schedule;
import com.diegoalejogm.enhueco.model.main.System;
import com.diegoalejogm.enhueco.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class ScheduleFragment extends Fragment implements WeekView.EventLongPressListener, WeekView.EventClickListener, WeekView.MonthChangeListener, WeekView.EmptyViewClickListener
{
    public static final String SCHEDULE_EXTRA = "Schedule";
    private static final String LOG = "ScheduleActivity";
    private WeekView mWeekView;
    private Schedule schedule = System.getInstance().getAppUser().getSchedule();
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.activity_schedule, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        fab = (FloatingActionButton) view.findViewById(R.id.addEventButton);
        if (schedule != System.getInstance().getAppUser().getSchedule()) fab.setVisibility(View.GONE);

        mWeekView = (WeekView) view.findViewById(R.id.weekView);

        //getActivity().setSupportActionBar(toolbar);
        //getActivity().getSupportActionBar().setTitle("Mi Horario");
        //getActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
    public void onResume()
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
            List<Event> currentWeekDayEvents = System.getInstance().getAppUser().getSchedule().getWeekDays()[newEventWeekday].getEvents();
            for (int j = 0; j < currentWeekDayEvents.size(); j++)
            {
                Event currentEvent = currentWeekDayEvents.get(j);

                // Update global Calendar to match start time
                Log.v(LOG, "Event start: " + currentEvent.getStartHour().get(Calendar.HOUR_OF_DAY) + ":" + currentEvent.getStartHour().get(Calendar.MINUTE));
                Log.v(LOG, "Event end: " + currentEvent.getEndHour().get(Calendar.HOUR_OF_DAY) + ":" + currentEvent.getEndHour().get(Calendar.MINUTE));
                globalCalendar.set(Calendar.HOUR_OF_DAY, currentEvent.getStartHour().get(Calendar.HOUR_OF_DAY));
                globalCalendar.set(Calendar.MINUTE, currentEvent.getStartHour().get(Calendar.MINUTE));

                // Set global calendar time in start local calendar
                Calendar startCalendarLocal = Calendar.getInstance();
                startCalendarLocal.setTimeInMillis(globalCalendar.getTimeInMillis());
                startCalendarLocal.set(Calendar.DAY_OF_MONTH, globalCalendar.get(Calendar.DAY_OF_MONTH));
                Log.v(LOG, "Event start: " + startCalendarLocal.get(Calendar.HOUR_OF_DAY) + ":" + startCalendarLocal.get(Calendar.MINUTE));


                // Update global Calendar to match end time
                globalCalendar.set(Calendar.HOUR_OF_DAY, currentEvent.getEndHour().get(Calendar.HOUR_OF_DAY));
                globalCalendar.set(Calendar.MINUTE, currentEvent.getEndHour().get(Calendar.MINUTE));

                // Set global calendar time in end local calendar
                Calendar endCalendarLocal = Calendar.getInstance();
                endCalendarLocal.setTimeInMillis(globalCalendar.getTimeInMillis());
                endCalendarLocal.set(Calendar.DAY_OF_MONTH, globalCalendar.get(Calendar.DAY_OF_MONTH));

                Log.v(LOG, "Event end: " + endCalendarLocal.get(Calendar.HOUR_OF_DAY) + ":" + endCalendarLocal.get(Calendar.MINUTE));
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

    public void reloadData()
    {
        if (mWeekView != null) mWeekView.notifyDatasetChanged();
    }

    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;

        if (schedule != System.getInstance().getAppUser().getSchedule() && fab != null) fab.setVisibility(View.GONE);
    }
}
