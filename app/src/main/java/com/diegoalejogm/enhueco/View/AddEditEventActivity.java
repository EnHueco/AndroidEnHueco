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
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import com.diegoalejogm.enhueco.R;

import java.text.DecimalFormat;
import java.util.Calendar;

public class AddEditEventActivity extends AppCompatActivity implements View.OnClickListener
{

    EditText startTimeText, endTimeText, weekDaysText;
    Calendar startTime, endTime;
    String[] weekDaysArray;
    boolean[] selectedWeekDays;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);

        selectedWeekDays = new boolean[7];
        weekDaysArray = getResources().getStringArray(R.array.weekDay_array);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Evento");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();

        startTimeText = (EditText) findViewById(R.id.startTimeEditText);
        startTimeText.setOnClickListener(this);
        updateTextEdit(startTime, startTimeText);


        endTimeText = (EditText) findViewById(R.id.endTimeEditText);
        endTimeText.setOnClickListener(this);
        updateTextEdit(endTime, endTimeText);

        weekDaysText = (EditText) findViewById(R.id.weekDaysEditText);
        weekDaysText.setOnClickListener(this);

    }

    private void updateCalendarAndTextEdit(int hourOfDay, int minute, Calendar calendar, EditText et)
    {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        updateTextEdit(calendar, et);
    }

    private void updateTextEdit(Calendar calendar, EditText et)
    {
        String ampm = (calendar.get(Calendar.AM_PM) == calendar.AM )? "AM" : "PM";

        DecimalFormat mFormat= new DecimalFormat("00");
        et.setText(mFormat.format(Double.valueOf(calendar.get(Calendar.HOUR)))
                + " : " + mFormat.format(Double.valueOf(calendar.get(Calendar.MINUTE)))
                + " " + ampm);
        ;
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
                    AddEditEventActivity.this.updateCalendarAndTextEdit(hourOfDay, minute,
                            AddEditEventActivity.this.startTime, AddEditEventActivity.this.startTimeText);
                }
            }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), false);
            startTimePicker.show();
        }
        else if(v == endTimeText)
        {
            TimePickerDialog endTimePicker = new TimePickerDialog(new ContextThemeWrapper(this, R.style.Dialog), new TimePickerDialog.OnTimeSetListener()
            {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute)
                {
                    AddEditEventActivity.this.updateCalendarAndTextEdit(hourOfDay, minute,
                            AddEditEventActivity.this.endTime, AddEditEventActivity.this.endTimeText);
                }
            }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), false);
            endTimePicker.show();
        }

        else if(v == weekDaysText)
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
        for(int i = 0; i < selectedWeekDays.length ; i++)
        {
            all &= selectedWeekDays[i];
            if(selectedWeekDays[i] && first)
            {
                s.append(weekDaysArray[i].substring(0,3));
                first = false;
            }
            else if(selectedWeekDays[i]) s.append(", ").append(weekDaysArray[i].substring(0,3));
        }
        if(all) AddEditEventActivity.this.weekDaysText.setText("Todos");
        else AddEditEventActivity.this.weekDaysText.setText(s.toString());
    }
}
