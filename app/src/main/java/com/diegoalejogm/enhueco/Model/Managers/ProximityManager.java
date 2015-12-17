package com.diegoalejogm.enhueco.model.managers;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import au.com.bytecode.opencsv.CSVReader;
import com.diegoalejogm.enhueco.model.EHApplication;
import com.diegoalejogm.enhueco.model.mainClasses.AppUser;
import com.diegoalejogm.enhueco.model.mainClasses.Event;
import com.diegoalejogm.enhueco.model.mainClasses.System;
import com.diegoalejogm.enhueco.model.other.Tuple;
import com.google.common.base.Optional;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Diego on 11/9/15.
 */
public class ProximityManager
{
    // Values for persistence
    public static final String FILE_NAME = "proximityManager";

    private static ProximityManager sharedManager;

    /** The interval used to report location (and get location from the app user's friends) when app user is available (i.e. in an app user's free time) */
    private static final int REPORTING_INTERVAL = 1000 * 60 * 5; //5 Minutes

    private ProximityManager () {}

    private UndirectedGraph<String, DefaultEdge> wifiAccessPointsBSSIDSGraph = new SimpleGraph<>(DefaultEdge.class);

    private PendingIntent reportingAlarmIntent;

    public static ProximityManager getSharedManager()
    {
        if (sharedManager == null && (sharedManager = loadFromPersistence().orNull()) == null)
        {
            sharedManager = new ProximityManager();
        }
        return sharedManager;
    }

    public class ProximityManagerReportingServiceAlarmReceiver extends BroadcastReceiver
    {
        public static final int REQUEST_CODE = 12345;
        public static final String ACTION = "com.codepath.example.servicesdemo.alarm";

        // Triggered by the Alarm periodically (starts the service to run task)
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Intent i = new Intent(context, ProximityManagerReportingService.class);
            context.startService(i);
        }
    }

    public class ProximityManagerReportingService extends IntentService
    {
        // Must create a default constructor
        public ProximityManagerReportingService()
        {
            // Used to name the worker thread, important only for debugging.
            super("ProximityManagerReportingService");
        }

        @Override
        public void onCreate()
        {
            super.onCreate();
        }

        @Override
        protected void onHandleIntent(Intent intent)
        {
            //Report new visible access points and access point with best signal. On server's response update user's friend's BSSIDs.
            getSharedManager().reportVisibleBestSignalAccessPointAndNewAccessPointsIfNecessary();

            //Check if we need to stop the reporting and reschedule it.
            AlarmManager alarmMgr = (AlarmManager) EHApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);

            alarmMgr.cancel(reportingAlarmIntent);
        }
    }

    public static List<ScanResult> scanVisibleWifiAccessPoints ()
    {
        WifiManager wifiManager = (WifiManager) EHApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled())
        {
            //TODO: Ask user kindly
            wifiManager.setWifiEnabled(true);
        }

        return wifiManager.getScanResults();
    }

    public static Optional<ScanResult> getVisibleWifiAccessPointWithBestSignal ()
    {
        List<ScanResult> visibleAccessPoints = scanVisibleWifiAccessPoints();

        if (visibleAccessPoints.isEmpty()) return Optional.absent();

        ScanResult bestSignalAccessPoint = visibleAccessPoints.get(0);

        for (ScanResult accessPoint: visibleAccessPoints)
        {
            if (accessPoint.level > bestSignalAccessPoint.level)
            {
                bestSignalAccessPoint = accessPoint;
            }
        }

        return Optional.of(bestSignalAccessPoint);
    }

    public void syncGraphFromServer()
    {
        //TODO:

        persistData();
    }

    public void generateGraphFromFile ()
    {
        try
        {
            CSVReader reader = new CSVReader(new FileReader("yourfile.csv"));

            List<String[]> rows = reader.readAll();

            final DateFormat format = new SimpleDateFormat("hh:mm:ss a", Locale.ENGLISH);

            Collections.sort(rows, new Comparator<String[]>()
            {
                @Override
                public int compare(String[] lhs, String[] rhs)
                {
                    try
                    {
                        int dateComparison = format.parse(lhs[4]).compareTo(format.parse(rhs[4]));

                        if (dateComparison != 0)
                        {
                            return dateComparison;
                        }
                        else
                        {
                            return Integer.getInteger(lhs[2]).compareTo(Integer.getInteger(rhs[2]));
                        }
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }

                    return 0;
                }
            });

            String currentDate = "";
            String referenceAccessPointBSSID = "";

            for (String[] row: rows)
            {
                wifiAccessPointsBSSIDSGraph.addVertex(row[4]);

                if (!row[4].equals(currentDate))
                {
                    currentDate = row[4];
                    referenceAccessPointBSSID = row[4];
                }
                else
                {
                    wifiAccessPointsBSSIDSGraph.addEdge(referenceAccessPointBSSID, row[4]);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void enableBackgroundReporting ()
    {
        scheduleReportingDuringCurrentOrNextFreeTimePeriods();
    }

    private void scheduleReportingDuringCurrentOrNextFreeTimePeriods()
    {
        Intent intent = new Intent(EHApplication.getAppContext(), ProximityManagerReportingServiceAlarmReceiver.class);
        reportingAlarmIntent = PendingIntent.getBroadcast(EHApplication.getAppContext(), ProximityManagerReportingServiceAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) EHApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);

        Tuple<Optional<Event>, Optional<Event>> currentAndNextFreeTimePeriods = System.getInstance().getAppUser().getCurrentAndNextFreeTimePeriods();

        Optional<Event> currentFreeTimePeriod = currentAndNextFreeTimePeriods.first,
                        nextFreeTimePeriod = currentAndNextFreeTimePeriods.second;

        Calendar alarmTriggerCalendar = Calendar.getInstance();

        // Set the alarm to start when app user's next free time period starts, or right now if app user is currently available.

        if (currentFreeTimePeriod.isPresent())
        {
            //Start immediately
            alarmTriggerCalendar.setTimeInMillis(java.lang.System.currentTimeMillis());
        }
        if (nextFreeTimePeriod.isPresent())
        {
            //Start when next free time period starts
            alarmTriggerCalendar.set(Calendar.HOUR_OF_DAY, nextFreeTimePeriod.get().getStartHour().get(Calendar.HOUR_OF_DAY));
            alarmTriggerCalendar.set(Calendar.MINUTE, nextFreeTimePeriod.get().getStartHour().get(Calendar.MINUTE));
        }
        else //The day is over, user doesn't have more free time periods ahead
        {

        }

        if (currentFreeTimePeriod.isPresent() || nextFreeTimePeriod.isPresent())
        {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTriggerCalendar.getTimeInMillis(), REPORTING_INTERVAL, reportingAlarmIntent);
        }

        persistData();
    }

    public boolean accessPointsAreNear (String bssidA, String bssidB)
    {
        for (DefaultEdge edge: wifiAccessPointsBSSIDSGraph.edgesOf(bssidA))
        {
            String neighbor = wifiAccessPointsBSSIDSGraph.getEdgeTarget(edge);

            if (neighbor.equals(bssidB)) return true;

            for (DefaultEdge edge2: wifiAccessPointsBSSIDSGraph.edgesOf(neighbor))
            {
                String neighbor2 = wifiAccessPointsBSSIDSGraph.getEdgeTarget(edge2);

                if (neighbor2.equals(bssidB)) return true;
            }
        }

        return false;
    }

    /**
     * Report new visible access points and access point with best signal. On server's response update user's friend's BSSIDs
     */
    public void reportVisibleBestSignalAccessPointAndNewAccessPointsIfNecessary()
    {
        boolean shouldReportNewAccessPoints = false;

        Optional<ScanResult> visibleWifiAccessPointWithBestSignal = getVisibleWifiAccessPointWithBestSignal();

        if (shouldReportNewAccessPoints)
        {
            List<ScanResult> visibleAccessPoints = scanVisibleWifiAccessPoints();

            ScanResult bestSignalAccessPoint = visibleAccessPoints.get(0);

            for (ScanResult accessPoint: visibleAccessPoints)
            {
                if (accessPoint.level > bestSignalAccessPoint.level)
                {
                    bestSignalAccessPoint = accessPoint;
                }
            }
        }

        //TODO:
    }

    public boolean persistData()
    {
        try
        {
            FileOutputStream fos = EHApplication.getAppContext().openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private static Optional<ProximityManager> loadFromPersistence()
    {
        try
        {
            FileInputStream fis = EHApplication.getAppContext().openFileInput(AppUser.FILE_NAME);
            ObjectInputStream is = new ObjectInputStream(fis);
            ProximityManager proximityManager = (ProximityManager) is.readObject();
            is.close();
            fis.close();
            return Optional.of(proximityManager);
        }
        catch (ClassNotFoundException | IOException e)
        {
            e.printStackTrace();
        }
        return Optional.absent();
    }
}
