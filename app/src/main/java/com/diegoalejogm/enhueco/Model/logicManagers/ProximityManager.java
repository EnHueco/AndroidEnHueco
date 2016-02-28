package com.diegoalejogm.enhueco.model.logicManagers;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;
import com.diegoalejogm.enhueco.model.EHApplication;
import com.diegoalejogm.enhueco.model.model.Event;
import com.diegoalejogm.enhueco.model.model.System;
import com.diegoalejogm.enhueco.model.model.User;
import com.diegoalejogm.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.diegoalejogm.enhueco.model.other.EHURLS;
import com.diegoalejogm.enhueco.model.other.JSONResponse;
import com.diegoalejogm.enhueco.model.structures.Tuple;
import com.diegoalejogm.enhueco.view.MainTabbedActivity;
import com.google.common.base.Optional;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Diego on 11/9/15.
 */
public class ProximityManager implements Serializable
{
    // Values for persistence
    public static final String FILE_NAME = "proximityManager";

    private static ProximityManager sharedManager;

    /** The interval used to report location (and get location from the app user's friends) when app user is available (i.e. in an app user's free time) */
    private static final int REPORTING_INTERVAL = 1000 * 60 * 5; //5 Minutes

    public static final int MINIMUM_TIME_INTERVAL_BETWEEN_NOTIFICATIONS = 1000*60*80; //80 minutes

    private ProximityManager () {}

    private UndirectedGraph<String, DefaultEdge> wifiAccessPointsBSSIDSGraph = new SimpleGraph<>(DefaultEdge.class);

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

    public class ProximityManagerReportingBootReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
            {
                ProximityManager.getSharedManager().scheduleReportingDuringCurrentOrNextFreeTimePeriods();
            }
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
            Log.d("ProximityManager", "----------------------- Reporting !");

            //Report new visible access points and access point with best signal. On server's response update user's friend's BSSIDs.
            ProximityManager.getSharedManager().reportVisibleBestSignalAccessPointAndNewAccessPointsIfNecessary();

            boolean shouldRescheduleAlarm = true;

            if (shouldRescheduleAlarm)
            {
                //alarmMgr.cancel(reportingAlarmIntent);
            }
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

    public void disableBackgroundReporting ()
    {
        PendingIntent reportingAlarmIntent = PendingIntent.getBroadcast(EHApplication.getAppContext(),
                ProximityManagerReportingServiceAlarmReceiver.REQUEST_CODE,
                new Intent(EHApplication.getAppContext(), ProximityManagerReportingServiceAlarmReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Cancel currently ongoing reporting
        AlarmManager alarmManager = (AlarmManager) EHApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(reportingAlarmIntent);

        //Disable reporting rescheduling upon reboot
        ComponentName receiver = new ComponentName(EHApplication.getAppContext(), ProximityManagerReportingBootReceiver.class);
        PackageManager pm = EHApplication.getAppContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private void scheduleReportingDuringCurrentOrNextFreeTimePeriods()
    {
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
            //TODO: Look for next free time period another day and schedule for another day
        }

        if (currentFreeTimePeriod.isPresent() || nextFreeTimePeriod.isPresent())
        {
            Intent intent = new Intent(EHApplication.getAppContext(), ProximityManagerReportingServiceAlarmReceiver.class);
            PendingIntent reportingAlarmIntent = PendingIntent.getBroadcast(EHApplication.getAppContext(), ProximityManagerReportingServiceAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) EHApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(reportingAlarmIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTriggerCalendar.getTimeInMillis(), REPORTING_INTERVAL, reportingAlarmIntent);

            // Enable alarm to be rescheduled upon reboot
            ComponentName receiver = new ComponentName(EHApplication.getAppContext(), ProximityManagerReportingBootReceiver.class);
            PackageManager pm = EHApplication.getAppContext().getPackageManager();

            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
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

        ConnectionManagerRequest request = new ConnectionManagerRequest(EHURLS.BASE, HTTPMethod.POST, Optional.<JSONObject>absent(), false);

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler()
        {
            @Override
            public void onSuccess(JSONResponse response)
            {
                // Update BSSID values

                JSONArray friendsJSON = response.jsonArray;

                try
                {
                    for (int i = 0; i < friendsJSON.length(); i++)
                    {
                        JSONObject friendJSON = friendsJSON.getJSONObject(i);

                        System.getInstance().getAppUser().getFriends().get(friendJSON.getString("login")).setCurrentBSSID(Optional.of(friendJSON.getJSONObject("location").getString("BSSID").toUpperCase()));
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                //Notify app user

                Collection<User> friendsToNotifyToUser = System.getInstance().getAppUser().getFriendsCurrentlyNearbyAndEligibleForNotification();

                Date currentDate = new Date();

                for (User friend: friendsToNotifyToUser)
                {
                    friend.setLastNotifiedNearbyStatusDate(Optional.of(currentDate));
                }

                if (!friendsToNotifyToUser.isEmpty())
                {
                    String notificationText = "";

                    int i = 0;

                    for (User friend: friendsToNotifyToUser)
                    {
                        if (i <= 3)
                        {
                            notificationText += friend.getFirstNames();

                            if (i == friendsToNotifyToUser.size()-1 && friendsToNotifyToUser.size() > 1)
                            {
                                notificationText += " y ";
                            }
                            else
                            {
                                notificationText += (i != 0 ? ", ":"");
                            }
                        }
                        else { break; }

                        i++;
                    }

                    if (friendsToNotifyToUser.size() > 3)
                    {
                        notificationText += " y otros amigos";
                    }

                    if (friendsToNotifyToUser.size() > 1)
                    {
                        notificationText += " parecen estar cerca y en hueco, ¿por qué no les escribes?";
                    }
                    else
                    {
                        notificationText += " parece estar cerca y en hueco, ¿por qué no le escribes?";
                    }

                    Intent intent = new Intent(EHApplication.getAppContext(), MainTabbedActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(EHApplication.getAppContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder b = new NotificationCompat.Builder(EHApplication.getAppContext());

                    b.setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setWhen(java.lang.System.currentTimeMillis())
                            .setContentTitle("Amigos cerca")
                            .setContentText(notificationText)
                            .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                            .setContentIntent(contentIntent)
                            .setContentInfo("Info");

                    NotificationManager notificationManager = (NotificationManager) EHApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, b.build());
                }
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
            }

        });
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
            FileInputStream fis = EHApplication.getAppContext().openFileInput(FILE_NAME);
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
