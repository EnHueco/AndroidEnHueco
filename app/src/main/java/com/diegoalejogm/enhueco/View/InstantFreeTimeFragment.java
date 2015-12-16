package com.diegoalejogm.enhueco.view;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import com.diegoalejogm.enhueco.model.mainClasses.BasicOperationCompletionListener;
import com.diegoalejogm.enhueco.model.mainClasses.Event;
import com.diegoalejogm.enhueco.model.mainClasses.System;
import com.diegoalejogm.enhueco.R;
import com.google.common.base.Optional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class InstantFreeTimeFragment extends DialogFragment
{
    private EditText nameEditText;
    private EditText locationEditText;
    private Calendar endTime;
    private TextView endTimeTextView;

    public InstantFreeTimeFragment()
    {
        // Required empty public constructor
    }

    public static InstantFreeTimeFragment newInstance()
    {
        Bundle args = new Bundle();

        InstantFreeTimeFragment fragment = new InstantFreeTimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_instant_free_time, container, false);

        nameEditText = (EditText) view.findViewById(R.id.freeTimeName);
        locationEditText = (EditText) view.findViewById(R.id.freeTimeLocation);

        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });

        endTimeTextView = (TextView) view.findViewById(R.id.endTimeTextClock);

        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        dateFormat.setTimeZone(TimeZone.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);

        endTime = calendar;

        endTimeTextView.setText(dateFormat.format(calendar.getTime()));

        endTimeTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker = new TimePickerDialog(InstantFreeTimeFragment.this.getContext(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
                    {
                        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                        dateFormat.setTimeZone(TimeZone.getDefault());

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);

                        if (calendar.after(Calendar.getInstance()))
                        {
                            endTime = calendar;
                            endTimeTextView.setText(dateFormat.format(calendar.getTime()));
                        }
                        else
                        {
                            new AlertDialog.Builder(InstantFreeTimeFragment.this.getContext())
                                    .setTitle("Advertencia")
                                    .setMessage("¡La hora a la que se acaba tu hueco no puede ser anterior a la hora actual!")
                                    .setPositiveButton("Tienes razón", null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                }, hour, minute, false);

                mTimePicker.setTitle("Selecciona hora de finalización");
                mTimePicker.show();
            }
        });

        view.findViewById(R.id.postButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Event newFreeTimePeriod = new Event(Event.EventType.FREE_TIME, Optional.of(nameEditText.getText().toString()), Optional.of(locationEditText.getText().toString()), Calendar.getInstance(), endTime);
                System.getInstance().getAppUser().postInstantFreeTime(newFreeTimePeriod, new BasicOperationCompletionListener()
                {
                    @Override
                    public void onSuccess()
                    {
                        //TODO
                    }

                    @Override
                    public void onFailure()
                    {

                    }
                });
            }
        });

        return view;
    }
}
