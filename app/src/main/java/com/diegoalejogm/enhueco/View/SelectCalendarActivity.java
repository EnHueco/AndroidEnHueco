package com.diegoalejogm.enhueco.View;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.R;
import mehdi.sakout.fancybuttons.FancyButton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class SelectCalendarActivity extends AppCompatActivity
{
    ListView listView;
    ArrayAdapter<DeviceCalendar> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_calendar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.listView);

        List<DeviceCalendar> calendars = new ArrayList<>();
        Cursor cursor = getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"), new String[] { "_id", "calendar_displayName" }, null, null, null);

        while (cursor.moveToNext())
        {
            final String ID = cursor.getString(0);
            final String displayName = cursor.getString(1);
            final Boolean selected = !cursor.getString(2).equals("0");
            final String accountName = cursor.getString(3);

            calendars.add(new DeviceCalendar(displayName, ID));
        }

        adapter = new CalendarsAdapter(this, 0, calendars);
        listView.setAdapter(adapter);
    }

    class DeviceCalendar
    {
        public String name;
        public String ID;

        public DeviceCalendar(String name, String ID)
        {
            this.name = name;
            this.ID = ID;
        }
    }

    public class CalendarsAdapter extends ArrayAdapter<DeviceCalendar>
    {
        Context context;
        List<DeviceCalendar> objects;

        public CalendarsAdapter(Context context, int resource, List<DeviceCalendar> objects)
        {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_item, null);

            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setText(objects.get(position).name);

            return view;
        }
    }

}
