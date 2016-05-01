package com.enhueco.view;

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
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.enhueco.R;
import com.enhueco.model.logicManagers.CurrentStateManager.CurrentStateManager;
import com.enhueco.model.logicManagers.ImmediateEventManager;
import com.enhueco.model.model.Event;
import com.enhueco.model.model.immediateEvent.InstantFreeTimeEvent;
import com.enhueco.model.other.BasicCompletionListener;
import com.google.common.base.Optional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class InstantFreeTimeFragment extends DialogFragment
{
    @Bind(R.id.freeTimeName) EditText nameEditText;
    @Bind(R.id.freeTimeLocation) EditText locationEditText;
    @Bind(R.id.endTimeTextClock) TextView endTimeTextView;

    private Calendar endTime;

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
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_instant_free_time, container, false);
        ButterKnife.bind(this, view);

        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });

        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        dateFormat.setTimeZone(TimeZone.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);

        endTime = calendar;

        endTimeTextView.setText(dateFormat.format(calendar.getTime()));

        return view;
    }

    @OnClick(R.id.endTimeTextClock)
    public void onEndTimeTextViewClick (View textView)
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

    @OnClick(R.id.postButton)
    public void post (View button)
    {
        Calendar endTimeInUTC = (Calendar) endTime.clone();
        endTimeInUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        InstantFreeTimeEvent freeTimeEvent = new InstantFreeTimeEvent(nameEditText.getText().toString(), endTimeInUTC, locationEditText.getText().toString());

        ImmediateEventManager.getSharedManager().createInstantFreeTimeEvent(freeTimeEvent, new BasicCompletionListener()
        {
            @Override
            public void onSuccess()
            {
                dismiss();
            }

            @Override
            public void onFailure(Exception error)
            {
                error.printStackTrace();
            }
        });
    }
}
