package com.enhueco.view;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.enhueco.view.dialog.EHProgressDialog;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class InstantFreeTimeFragment extends DialogFragment
{
    @Bind(R.id.freeTimeName)
    EditText nameEditText;
    @Bind(R.id.freeTimeLocation)
    EditText locationEditText;
    @Bind(R.id.endTimeTextClock)
    TextView endTimeTextView;

    private DateTime endTime;
    private DateTimeFormatter dtf;
    public static final int resultCode = 1;

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
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

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


        endTime = DateTime.now().plusMinutes(15);
        dtf = DateTimeFormat.forPattern("hh:mm a");
        endTimeTextView.setText(dtf.print(endTime));

        return view;
    }

    @OnClick(R.id.endTimeTextClock)
    public void onEndTimeTextViewClick(View textView)
    {
        int hour = endTime.getHourOfDay();
        int minute = endTime.getMinuteOfHour();

        TimePickerDialog mTimePicker = new TimePickerDialog(InstantFreeTimeFragment.this.getContext(), new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {
                DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                dateFormat.setTimeZone(TimeZone.getDefault());


                DateTime pickedDateTime = DateTime.now().withHourOfDay(selectedHour).withMinuteOfHour(selectedMinute);

                if (pickedDateTime.isAfter(DateTime.now()))
                {
                    endTime = pickedDateTime;
                    endTimeTextView.setText(dtf.print(endTime));
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
    public void post(View button)
    {
        final EHProgressDialog dialog = new EHProgressDialog(getContext());
        dialog.show();

        InstantFreeTimeEvent freeTimeEvent = new InstantFreeTimeEvent(nameEditText.getText().toString(), endTime
                .withZone(DateTimeZone.UTC).toLocalTime(), locationEditText.getText().toString());

        ImmediateEventManager.getSharedManager().createInstantFreeTimeEvent(freeTimeEvent, new BasicCompletionListener()
        {
            @Override
            public void onSuccess()
            {
                dialog.dismiss();
                getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, getActivity().getIntent());
                dismiss();
            }

            @Override
            public void onFailure(Exception error)
            {
                dialog.dismiss();
            }
        });
    }
}
