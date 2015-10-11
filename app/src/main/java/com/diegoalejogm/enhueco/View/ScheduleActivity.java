package com.diegoalejogm.enhueco.View;

import android.content.Intent;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.diegoalejogm.enhueco.R;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity implements WeekView.EventLongPressListener, WeekView.EventClickListener, WeekView.MonthChangeListener, WeekView.EmptyViewClickListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Mi Horario");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        WeekView mWeekView = (WeekView) findViewById(R.id.weekView);

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
        Log.v("Schedule Activity", "EVENT CLICKED");
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect)
    {
        Log.v("Schedule Activity", "EVENT LONG PRESS");

    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth)
    {
        Log.v("Schedule Activity", "MONTH CHANGED");
        return new ArrayList<WeekViewEvent>();
    }

    @Override
    public void onEmptyViewClicked(Calendar time)
    {
        Log.v("Schedule Activity", "EMPTY VIEW CLICKED");
    }

    public void addEvent(MenuItem item)
    {
        Intent intent = new Intent(this, AddEditEventActivity.class);
        startActivity(intent);

    }

    public void addEvent(View view)
    {
        Intent intent = new Intent(this, AddEditEventActivity.class);
        startActivity(intent);
    }
}
