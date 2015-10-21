package com.diegoalejogm.enhueco.View;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.R;
import com.google.common.base.Optional;
import com.diegoalejogm.enhueco.Model.MainClasses.System;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class AddEditEventActivity extends AppCompatActivity implements View.OnClickListener
{

    private static final String LOG = "AddEditEventActivity";
    RadioGroup eventType;
    RadioButton gapEventType;
    EditText eventName, eventLocation;
    EditText startTimeText, endTimeText, weekDaysText;
    Calendar startTime, endTime;
    String[] weekDaysArray;
    boolean[] selectedWeekDays;
    Event editEvent;

    public static final String EVENT_EXTRA = "Event";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);

        // Get views
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        startTimeText = (EditText) findViewById(R.id.startTimeEditText);
        endTimeText = (EditText) findViewById(R.id.endTimeEditText);
        gapEventType = (RadioButton) findViewById(R.id.radioButton);
        weekDaysText = (EditText) findViewById(R.id.weekDaysEditText);
        eventName = (EditText) findViewById(R.id.eventNameTextEdit);
        eventLocation = (EditText) findViewById(R.id.eventLocationTextEdit);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Evento");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editEvent = (Event) getIntent().getSerializableExtra(EVENT_EXTRA);

        startTime = Calendar.getInstance(TimeZone.getDefault());
        endTime = Calendar.getInstance(TimeZone.getDefault());
        endTime.add(Calendar.MINUTE, 30);
        selectedWeekDays = new boolean[7];
        weekDaysArray = getResources().getStringArray(R.array.weekDay_array);

        startTimeText.setOnClickListener(this);
        updateTextEdit(startTimeText, startTime);

        endTimeText.setOnClickListener(this);
        updateTextEdit(endTimeText, endTime);

        weekDaysText.setOnClickListener(this);

    }

    private void updateCalendar(Calendar calendar, int hourOfDay, int minute)
    {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
    }

    private void updateTextEdit(EditText et, Calendar calendar)
    {
        String ampm = (calendar.get(Calendar.AM_PM) == calendar.AM) ? "AM" : "PM";

        DecimalFormat mFormat = new DecimalFormat("00");
        et.setText(mFormat.format(Double.valueOf(calendar.get(Calendar.HOUR)))
                + " : " + mFormat.format(Double.valueOf(calendar.get(Calendar.MINUTE)))
                + " " + ampm);
    }

    @Override
    public void onClick(View v)
    {
        if (v == startTimeText)
        {
            TimePickerDialog startTimePicker = new TimePickerDialog(new ContextThemeWrapper(this, R.style.Dialog), new TimePickerDialog.OnTimeSetListener()
            {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                {
                    AddEditEventActivity.this.updateCalendar(AddEditEventActivity.this.startTime, hourOfDay, minute);
                    AddEditEventActivity.this.updateTextEdit(AddEditEventActivity.this.startTimeText,
                            AddEditEventActivity.this.startTime);
                }
            }, startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), false);
            startTimePicker.show();
        }

        else if (v == endTimeText)
        {
            TimePickerDialog endTimePicker = new TimePickerDialog(new ContextThemeWrapper(this, R.style.Dialog), new TimePickerDialog.OnTimeSetListener()
            {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                {
                    AddEditEventActivity.this.updateCalendar(AddEditEventActivity.this.endTime, hourOfDay, minute);
                    AddEditEventActivity.this.updateTextEdit(AddEditEventActivity.this.endTimeText,
                            AddEditEventActivity.this.endTime);
                }
            }, endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), false);
            endTimePicker.show();
        }

        else if (v == weekDaysText)
        {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);


            alertDialogBuilder.setTitle("Días del evento")
                    .setMultiChoiceItems(R.array.weekDay_array, selectedWeekDays, new DialogInterface.OnMultiChoiceClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked)
                        {
                            selectedWeekDays[which] = isChecked;
                        }
                    })
                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            AddEditEventActivity.this.updateWeekDays();
                        }
                    });


            alertDialogBuilder.show();
        }
    }

    private void updateWeekDays()
    {
        StringBuffer s = new StringBuffer();
        boolean first = true, all = true;
        for (int i = 0; i < selectedWeekDays.length; i++)
        {
            all &= selectedWeekDays[i];
            if (selectedWeekDays[i] && first)
            {
                s.append(weekDaysArray[i].substring(0, 3));
                first = false;
            }
            else if (selectedWeekDays[i]) s.append(", ").append(weekDaysArray[i].substring(0, 3));
        }
        if (all) AddEditEventActivity.this.weekDaysText.setText("Todos");
        else AddEditEventActivity.this.weekDaysText.setText(s.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
        return true;
    }

    public void addEvent(MenuItem item)
    {
        if (this.editEvent == null)
        {
            DaySchedule[] weekDaysSchedule = System.instance.getAppUser().getSchedule().getWeekDays();
            for (int i = 0; i < selectedWeekDays.length; i++)
            {
                if (!selectedWeekDays[i]) continue;

                Calendar startTimeCopy = (Calendar) startTime.clone(); Calendar endTimeCopy;

                while(startTimeCopy.get(Calendar.DAY_OF_WEEK) != i + 1) startTimeCopy.add(Calendar.DAY_OF_YEAR, 1);
                endTimeCopy = (Calendar) startTimeCopy.clone();

                Event.EventType eventType = gapEventType.isChecked() ? Event.EventType.GAP : Event.EventType.CLASS;
//                Log.v(LOG, "Event start: " + startTime.get(Calendar.HOUR_OF_DAY) + ":" + startTime.get(Calendar.MINUTE));
//                Log.v(LOG, "Timezone: " + startTime.getTimeZone());
//                startTime.set(Calendar.DAY_OF_WEEK, i + 1);
                startTime.setTimeZone((TimeZone.getTimeZone("UTC")));
//                Log.v(LOG, "Event end: " + endTime.get(Calendar.HOUR_OF_DAY) + ":" + endTime.get(Calendar.MINUTE));
//                Log.v(LOG, "Timezone: " + endTime.getTimeZone());
//                Log.v(LOG, "*Event start: " + startTime.get(Calendar.HOUR_OF_DAY) + ":" + startTime.get(Calendar.MINUTE));
//                Log.v(LOG, "*Timezone: " + startTime.getTimeZone());
//                endTime.set(Calendar.DAY_OF_WEEK, i + 1);
                endTime.setTimeZone((TimeZone.getTimeZone("UTC")));
//                Log.v(LOG, "*Event end: " + endTime.get(Calendar.HOUR_OF_DAY) + ":" + endTime.get(Calendar.MINUTE));
//                Log.v(LOG, "*Timezone: " + endTime.getTimeZone());

                Event event = new Event(eventType, Optional.of(eventName.getText().toString()), Optional.of(eventLocation.getText().toString()), startTime, endTime);
                boolean added = weekDaysSchedule[i + 1].addEvent(event);
                if(added)
                {
                    System.instance.getAppUser().uploadEvent(event);
                    System.instance.persistData(getApplicationContext());
                }

            }
        }
        finish();
    }

    public void cancelEvent(MenuItem item)
    {
        finish();
    }
}
