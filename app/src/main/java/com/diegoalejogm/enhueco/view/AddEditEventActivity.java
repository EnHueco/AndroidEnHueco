package com.diegoalejogm.enhueco.view;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import com.diegoalejogm.enhueco.model.main.*;
import com.diegoalejogm.enhueco.model.managers.SynchronizationManager;
import com.diegoalejogm.enhueco.model.structures.Tuple;
import com.diegoalejogm.enhueco.R;
import com.google.common.base.Optional;
import com.diegoalejogm.enhueco.model.main.System;

import java.text.DecimalFormat;
import java.util.*;

public class AddEditEventActivity extends AppCompatActivity implements View.OnClickListener
{

    private static final String LOG = "AddEditEventActivity";
    RadioGroup eventType;
    RadioButton freeTimeEventTypeRadioButton;
    EditText eventNameText, eventLocationText;
    EditText startTimeText, endTimeText, weekDaysText;
    Calendar startTime, endTime;
    String[] weekDaysArray;
    boolean[] selectedWeekDays;

    Optional<Event> eventToEdit;

    public static final String EVENT_EXTRA = "Event";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);

        eventToEdit = Optional.fromNullable((Event) getIntent().getSerializableExtra("eventToEdit"));

        // Get views
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        startTimeText = (EditText) findViewById(R.id.startTimeEditText);
        endTimeText = (EditText) findViewById(R.id.endTimeEditText);
        freeTimeEventTypeRadioButton = (RadioButton) findViewById(R.id.freeTimeEventTypeRadioButton);
        weekDaysText = (EditText) findViewById(R.id.weekDaysEditText);
        eventNameText = (EditText) findViewById(R.id.eventNameTextEdit);
        eventLocationText = (EditText) findViewById(R.id.eventLocationTextEdit);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Evento");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        if (eventToEdit.isPresent())
        {
            Event eventToEdit = this.eventToEdit.get();

            eventNameText.setText(eventToEdit.getName().orNull());
            eventLocationText.setText(eventToEdit.getLocation().orNull());

            Calendar calendar =  Calendar.getInstance();
            Date currentDate = new Date();

            calendar.setTime(eventToEdit.getStartHourInDate(currentDate));
            startTime = (Calendar) calendar.clone();

            calendar.setTime(eventToEdit.getEndHourInDate(currentDate));
            endTime = (Calendar) calendar.clone();

            updateTextEdit(startTimeText, startTime);
            updateTextEdit(endTimeText, endTime);
        }
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
                    updateCalendar(startTime, hourOfDay, minute);
                    updateTextEdit(startTimeText, startTime);

                }
            }, startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), false);

            startTimePicker.show();
        }

        else if (v == endTimeText)
        {
            TimePickerDialog endTimePicker = new TimePickerDialog(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_NoActionBar), new TimePickerDialog.OnTimeSetListener()
            {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                {
                    updateCalendar(endTime, hourOfDay, minute);
                    updateTextEdit(endTimeText, endTime);

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
                            updateWeekDays();
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
            else if (selectedWeekDays[i])
            {
                s.append(", ").append(weekDaysArray[i].substring(0, 3));
            }
        }

        if (all)
        {
            weekDaysText.setText("Todos");
        }
        else
        {
            weekDaysText.setText(s.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
        return true;
    }

    public void addEvent(MenuItem item)
    {
        List<Tuple<DaySchedule, Event>> daySchedulesAndEventsToAdd = new ArrayList<>();
        boolean canAddEvents = true;


        DaySchedule[] weekDaysSchedule = System.getInstance().getAppUser().getSchedule().getWeekDays();
        for (int i = 0; i < selectedWeekDays.length; i++)
        {
            if (!selectedWeekDays[i]) continue;

            Calendar newEventStartTime = (Calendar) startTime.clone();
            Calendar newEventEndTime = (Calendar) endTime.clone();

            newEventStartTime.set(Calendar.DAY_OF_WEEK, i+1);
            newEventEndTime.set(Calendar.DAY_OF_WEEK, i+1);

            newEventStartTime.set(Calendar.SECOND, 0);
            newEventStartTime.set(Calendar.MILLISECOND, 0);
            newEventEndTime.set(Calendar.SECOND, 0);
            newEventEndTime.set(Calendar.MILLISECOND, 0);

            //while (startTimeCopy.get(Calendar.DAY_OF_WEEK) != i + 1) startTimeCopy.add(Calendar.DAY_OF_YEAR, 1);

            Event.EventType eventType = freeTimeEventTypeRadioButton.isChecked() ? Event.EventType.FREE_TIME : Event.EventType.CLASS;

            startTime.setTimeZone((TimeZone.getTimeZone("UTC")));
            endTime.setTimeZone((TimeZone.getTimeZone("UTC")));

            Event newEvent = new Event(eventType, Optional.of(eventNameText.getText().toString()), Optional.of(eventLocationText.getText().toString()), startTime, endTime);

            DaySchedule daySchedule = weekDaysSchedule[i + 1];

            if (!daySchedule.canAddEvent(newEvent, eventToEdit))
            {
                canAddEvents = false;
            }
            else
            {
                daySchedulesAndEventsToAdd.add(new Tuple<>(daySchedule, newEvent));
            }
        }

        if (canAddEvents)
        {
            if (eventToEdit.isPresent()) eventToEdit.get().getDaySchedule().removeEvent(eventToEdit.get());

            for (Tuple<DaySchedule, Event> dayScheduleAndEvent: daySchedulesAndEventsToAdd)
            {
                dayScheduleAndEvent.first.addEvent(dayScheduleAndEvent.second);
                SynchronizationManager.getSharedManager().reportNewEvent(dayScheduleAndEvent.second);
            }

            System.getInstance().persistData(getApplicationContext());

            finish();
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setTitle("Imposible agregar evento")
                    .setMessage("La clase que estas tratando de agregar se cruza con algún otro evento en tu calendario en alguno de los días que elegiste...")
                    .setNeutralButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    public void cancelEvent(MenuItem item)
    {
        finish();
    }
}
