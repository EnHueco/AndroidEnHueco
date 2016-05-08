package com.enhueco.view;

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
import com.enhueco.model.model.Event;
import com.enhueco.model.model.Schedule;
import com.enhueco.model.model.EnHueco;
import com.enhueco.R;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.*;

public class ScheduleFragment extends Fragment implements WeekView.EventLongPressListener, WeekView.EventClickListener, WeekView.MonthChangeListener, WeekView.EmptyViewClickListener
{
    public static final String SCHEDULE_EXTRA = "Schedule";
    private static final String LOG = "ScheduleActivity";
    private WeekView mWeekView;
    private Schedule schedule = EnHueco.getInstance().getAppUser().getSchedule();
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
        if (schedule != EnHueco.getInstance().getAppUser().getSchedule()) fab.setVisibility(View.GONE);

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

    public void reloadData()
    {
        if (mWeekView != null) mWeekView.notifyDatasetChanged();
    }

    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;

        if (schedule != EnHueco.getInstance().getAppUser().getSchedule() && fab != null) fab.setVisibility(View.GONE);
    }
}
