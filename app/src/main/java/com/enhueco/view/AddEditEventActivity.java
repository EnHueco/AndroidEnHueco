package com.enhueco.view;

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
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.enhueco.model.logicManagers.ScheduleManager;
import com.enhueco.model.model.*;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.Utilities;
import com.enhueco.R;
import com.enhueco.view.dialog.EHProgressDialog;
import com.google.common.base.Optional;
import com.enhueco.model.model.EnHueco;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;

public class AddEditEventActivity extends AppCompatActivity
{

    private static final String LOG = "AddEditEventActivity";
    RadioGroup eventType;
    @Bind(R.id.freeTimeEventTypeRadioButton)
    RadioButton freeTimeEventTypeRadioButton;
    @Bind(R.id.eventNameTextEdit)
    EditText eventNameText;
    @Bind(R.id.eventLocationTextEdit)
    EditText eventLocationText;
    @Bind(R.id.startTimeEditText)
    EditText startTimeText;
    @Bind(R.id.endTimeEditText)
    EditText endTimeText;
    @Bind(R.id.weekDaysEditText)
    EditText weekDaysText;
    DateTime startTime, endTime;
    String[] weekDaysArray;
    boolean[] selectedWeekDays;

    Optional<Event> eventToEdit;

    public static final String EVENT_EXTRA = "Event";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);
        ButterKnife.bind(this);

        eventToEdit = Optional.fromNullable((Event) getIntent().getSerializableExtra("eventToEdit"));

        // Get views
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Evento");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        startTime = DateTime.now();
        endTime = DateTime.now().plusMinutes(30);
        selectedWeekDays = new boolean[7];
        weekDaysArray = getResources().getStringArray(R.array.weekDay_array);

        updateTextEdit(startTimeText, startTime);
        updateTextEdit(endTimeText, endTime);

        if (eventToEdit.isPresent())
        {
            Event eventToEdit = this.eventToEdit.get();

            eventNameText.setText(eventToEdit.getName().orNull());
            eventLocationText.setText(eventToEdit.getLocation().orNull());

            startTime = DateTime.now(DateTimeZone.UTC).withTime(eventToEdit.getStartHourInLocalTimezone()).withZone
                    (DateTimeZone.forTimeZone(TimeZone.getDefault()));
            endTime = DateTime.now(DateTimeZone.UTC).withTime(eventToEdit.getEndHourInLocalTimezone()).withZone
                    (DateTimeZone.forTimeZone(TimeZone.getDefault()));

            updateTextEdit(startTimeText, startTime);
            updateTextEdit(endTimeText, endTime);

            weekDaysText.setVisibility(View.GONE);
        }
    }

    private void updateTextEdit(EditText et, DateTime time)
    {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("hh : mm a");
        String string = dtf.print(time);
        et.setText(string);
    }

    @OnClick(R.id.startTimeEditText)
    void onStartTimeTextTap(View sender)
    {
        TimePickerDialog startTimePicker = new TimePickerDialog(new ContextThemeWrapper(this, android.R.style
                .Theme_Holo_Light_NoActionBar), new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute)
            {
                startTime = startTime.withHourOfDay(hourOfDay).withMinuteOfHour(minute);
                updateTextEdit(startTimeText, startTime);

            }
        }, startTime.getHourOfDay(), startTime.getMinuteOfHour(), false);

        startTimePicker.show();
    }

    @OnClick(R.id.endTimeEditText)
    void onEndTimeTextTap(View sender)
    {
        TimePickerDialog endTimePicker = new TimePickerDialog(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_NoActionBar), new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute)
            {
                endTime = endTime.withHourOfDay(hourOfDay).withMinuteOfHour(minute);
                updateTextEdit(endTimeText, endTime);

            }
        }, endTime.getHourOfDay(), endTime.getMinuteOfHour(), false);

        endTimePicker.show();
    }

    @OnClick(R.id.weekDaysEditText)
    void onWeekDaysEditTextTap(View sender)
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_item_save_event:
                if(eventToEdit.isPresent()) updateEvent();
                else addEvents();
                break;
            case R.id.menu_item_cancel_event:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateEvent()
    {
        //TODO: Implement event updating
    }

    public void addEvents()
    {
        ArrayList eventsToAdd = new ArrayList<>();
        boolean canAddEvents = true;


        DaySchedule[] weekDaysSchedule = EnHueco.getInstance().getAppUser().getSchedule().getWeekDays();
        for (int i = 0; i < selectedWeekDays.length; i++)
        {
            if (!selectedWeekDays[i]) continue;

            Event.EventType eventType = freeTimeEventTypeRadioButton.isChecked() ? Event.EventType.FREE_TIME : Event.EventType.CLASS;

            DateTime startTimeUTC = startTime.withDayOfWeek(Utilities.serverWeekDayToJodaWeekDay(i+1)).withZone
                    (DateTimeZone.UTC);
            DateTime endTimeUTC = endTime.withDayOfWeek(Utilities.serverWeekDayToJodaWeekDay(i + 1)).withZone
                    (DateTimeZone.UTC);

            Event newEvent = new Event(eventType, Optional.of(eventNameText.getText().toString()), Optional.of
                    (eventLocationText.getText().toString()), startTimeUTC.getDayOfWeek(), startTimeUTC
                    .getHourOfDay(), startTimeUTC.getMinuteOfHour(), endTimeUTC.getHourOfDay(),
                    endTimeUTC.getHourOfDay(), endTimeUTC.getMinuteOfHour());

            // TODO: Delete daySchedules
            DaySchedule daySchedule = weekDaysSchedule[newEvent.getLocalTimezoneWeekDay()];

            if (!daySchedule.canAddEvent(newEvent, eventToEdit))
            {
                canAddEvents = false;
            }
            else
            {
                eventsToAdd.add(newEvent);
            }
        }

        if (canAddEvents)
        {
            final EHProgressDialog dialog = new EHProgressDialog(this);
            dialog.show();
            if (eventToEdit.isPresent()) eventToEdit.get().getDaySchedule().removeEvent(eventToEdit.get());

            ScheduleManager.getSharedManager().reportNewEvents(eventsToAdd, new BasicCompletionListener()
            {
                @Override
                public void onSuccess()
                {
                    dialog.dismiss();
                    finish();
                }

                @Override
                public void onFailure(Exception error)
                {
                    dialog.dismiss();
                    Utilities.showErrorToast(AddEditEventActivity.this);
                }
            });
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
}
