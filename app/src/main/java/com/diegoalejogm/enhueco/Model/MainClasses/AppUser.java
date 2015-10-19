package com.diegoalejogm.enhueco.Model.MainClasses;

import android.app.VoiceInteractor;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import com.diegoalejogm.enhueco.Model.EHApplication;
import com.diegoalejogm.enhueco.Model.Other.ConnectionManager.*;
import com.diegoalejogm.enhueco.Model.Other.EHParameters;
import com.diegoalejogm.enhueco.Model.Other.EHURLS;
import com.diegoalejogm.enhueco.Model.Other.Tuple;
import com.diegoalejogm.enhueco.Model.Other.Utilities;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Diego on 10/11/15.
 */
public class AppUser extends User implements Serializable
{

    private String token;

    private List<User> friends = new ArrayList<>();
    private List<User> outgoingFriendRequests = new ArrayList<>();
    private List<User> incomingFriendRequests = new ArrayList<>();

    // Values for persistence
    public static final String FILE_NAME = "appUser";

    // Values for QR encoding
    private static final char splitCharacter = '\\';
    private static final char separationCharacter = '-';
    private static final char multipleElementsCharacter = ',';
    private static final char hourMinuteSeparationChacter = ':';

    public AppUser(String username, String token, String firstNames, String lastNames, String phoneNumber, Optional<String> imageURL, String ID, Date lastUpdatedOn)
    {
        super(username, firstNames, lastNames, phoneNumber, imageURL, ID, lastUpdatedOn);

        this.token = token;
    }

    public static AppUser appUserFromJSONObject(JSONObject object) throws JSONException, ParseException
    {
        User user = User.userFromJSONObject(object.getJSONObject("user"));
        String token = object.getString("value");
        return new AppUser(user.getUsername(), token, user.getFirstNames(), user.getLastNames(), user.getPhoneNumber(), user.getImageURL(), user.getID(), user.getLastUpdatedOn());
    }

    public String getToken()
    {
        return token;
    }

    public List<User> getFriends()
    {
        return friends;
    }

    /**
     * Checks for and downloads any updates from the server including Session Status, Friend list, Friends Schedule, User's Info
     */
    public void fetchUpdates()
    {

    }

    /**
     * Fetches updates for both outgoing and incoming friend requests on the server and notifies the result via Notification Center.
     * <p/>
     * Notifications
     * -EHSystemNotification.SystemDidReceiveFriendRequestUpdates in case of success
     */
    public void fetchUpdatesForFriendRequests()
    {
        final JSONObject params = new JSONObject();

        try
        {
            params.put(EHParameters.USER_ID, getUsername());
            params.put(EHParameters.TOKEN, getToken());

            ConnectionManagerRequest outgoingRequestsRequest = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.OUTGOING_FRIEND_REQUESTS_SEGMENT, HTTPMethod.GET, Optional.of(params));
            ConnectionManager.sendAsyncRequest(outgoingRequestsRequest, new ConnectionManagerCompletionHandler()
            {
                @Override
                public void onSuccess(JSONObject responseJSON)
                {
                    try
                    {
                        ConnectionManagerRequest incomingRequestsRequest = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.OUTGOING_FRIEND_REQUESTS_SEGMENT, HTTPMethod.GET, Optional.of(params));
                        JSONObject response = ConnectionManager.sendSyncRequest(incomingRequestsRequest);

                        // TODO
                    }
                    catch (ExecutionException | InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(ConnectionManagerCompoundError error)
                {

                }
            });
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Fetches full friends and schedule information from the server and notifies the result via Notification Center.
     * <p/>
     * Notifications
     * - EHSystemNotification.SystemDidReceiveFriendAndScheduleUpdates in case of success
     */
    public void fetchUpdatesForFriendsAndFriendSchedules()
    {
        JSONObject params = new JSONObject();

        try
        {
            params.put(EHParameters.USER_ID, getUsername());
            params.put(EHParameters.TOKEN, getToken());

            ConnectionManager.sendAsyncRequest(new ConnectionManagerRequest(EHURLS.BASE + EHURLS.FRIENDS_SEGMENT, HTTPMethod.GET, Optional.of(params)), new ConnectionManagerCompletionHandler()
            {
                @Override
                public void onSuccess(JSONObject responseJSON)
                {
                    try
                    {
                        List<User> newFriends = new ArrayList<User>();

                        Date currentDate = new Date();

                        Calendar globalCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        globalCalendar.setTime(currentDate);

                        Calendar localCalendar = Calendar.getInstance();

                        JSONArray friendsJSON = responseJSON.getJSONArray("data");

                        for (int i = 0; i < friendsJSON.length(); i++)
                        {
                            JSONObject friendJSON = friendsJSON.getJSONObject(i);

                            User newFriend = User.userFromJSONObject(friendJSON);

                            JSONObject scheduleJSON = friendJSON.getJSONObject("schedule");
                            JSONArray eventsJSON = scheduleJSON.getJSONArray("events");

                            for (int j = 0; j < eventsJSON.length(); j++)
                            {
                                JSONObject eventJSON = eventsJSON.getJSONObject(j);
                                Event newEvent = Event.eventFromJSONObject(eventJSON);

                                //Locate event in local array of weekdays based on its UTC startHour

                                globalCalendar.set(Calendar.DAY_OF_WEEK, newEvent.getStartHour().get(Calendar.DAY_OF_WEEK));
                                globalCalendar.set(Calendar.HOUR_OF_DAY, newEvent.getStartHour().get(Calendar.HOUR_OF_DAY));
                                globalCalendar.set(Calendar.MINUTE, newEvent.getStartHour().get(Calendar.MINUTE));

                                localCalendar.setTime(globalCalendar.getTime());

                                int localStartHourWeekDay = localCalendar.get(Calendar.DAY_OF_WEEK);

                                DaySchedule daySchedule = newFriend.getSchedule().getWeekDays()[localStartHourWeekDay];
                                daySchedule.addEvent(newEvent);
                            }

                            newFriends.add(newFriend);
                        }

                        friends = newFriends;

                        LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(new Intent(System.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_AND_SCHEDULE_UPDATES));
                    }
                    catch (JSONException | ParseException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(ConnectionManagerCompoundError error)
                {
                }
            });
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Returns all friends that are currently in gap.
     *
     * @return Friend in gap with their current gap
     */
    public List<Tuple<User, Event>> getFriendsCurrentlyInGap()
    {
        List<Tuple<User, Event>> friendsAndGaps = new ArrayList<>();

        for (User friend : friends)
        {
            Optional<Event> currentGap = friend.getCurrentGap();

            if (currentGap.isPresent())
            {
                friendsAndGaps.add(new Tuple<User, Event>(friend, currentGap.get()));
            }
        }

        return friendsAndGaps;
    }

    /**
     * Returns a schedule with the common gaps of the users provided.
     */
    public Schedule getCommonGapsScheduleForUsers(User[] users)
    {
        Date currentDate = new Date();
        Schedule commonGapsSchedule = new Schedule();

        if (users.length < 2) return commonGapsSchedule;

        for (int i = 1; i < getSchedule().getWeekDays().length; i++)
        {
            Predicate<Event> eventsFilterPredicate = new Predicate<Event>()
            {
                @Override
                public boolean apply(Event event)
                {
                    return event.getType().equals(Event.EventType.GAP);
                }
            };

            Collection<Event> currentCommonGaps = Collections2.filter(users[0].getSchedule().getWeekDays()[i].getEvents(), eventsFilterPredicate);

            for (int j = 1; j < users.length; j++)
            {
                Collection<Event> newCommonGaps = new ArrayList<>();

                for (Event gap1 : currentCommonGaps)
                {
                    Date startHourInCurrentDate1 = gap1.getStartHourInDate(currentDate);
                    Date endHourInCurrentDate1 = gap1.getEndHourInDate(currentDate);

                    for (Event gap2 : Collections2.filter(users[j].getSchedule().getWeekDays()[i].getEvents(), eventsFilterPredicate))
                    {
                        Date startHourInCurrentDate2 = gap2.getStartHourInDate(currentDate);
                        Date endHourInCurrentDate2 = gap2.getEndHourInDate(currentDate);

                        if (!(endHourInCurrentDate1.before(startHourInCurrentDate2) || startHourInCurrentDate1.after(endHourInCurrentDate2)))
                        {
                            Calendar startHour = ((startHourInCurrentDate1.after(startHourInCurrentDate2) && startHourInCurrentDate1.before(endHourInCurrentDate2)) ? gap1.getStartHour() : gap2.getStartHour());
                            Calendar endHour = ((endHourInCurrentDate1.after(startHourInCurrentDate2) && endHourInCurrentDate1.before(endHourInCurrentDate2)) ? gap1.getEndHour() : gap2.getEndHour());

                            newCommonGaps.add(new Event(Event.EventType.GAP, startHour, endHour));
                        }
                    }
                }

                currentCommonGaps = newCommonGaps;
            }

            commonGapsSchedule.getWeekDays()[i].setEvents(currentCommonGaps);
        }

        return commonGapsSchedule;
    }

    /**
     * Sends a friend request to the username provided and notifies the result via Notification Center.
     * <p/>
     * Notifications
     * - EHSystemNotification.SystemDidSendFriendRequest in case of success
     * - EHSystemNotification.SystemDidFailToSendFriendRequest in case of failure
     */
    public void sendFriendRequestToUserRequestWithUsername(String username)
    {
        ConnectionManagerRequest request = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.FRIENDS_SEGMENT + "/" + username + "/", HTTPMethod.POST, Optional.<JSONObject>absent());

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler()
        {
            @Override
            public void onSuccess(JSONObject responseJSON)
            {
                // TODO

                LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(new Intent(System.EHSystemNotification.SYSTEM_DID_SEND_FRIEND_REQUEST));
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(new Intent(System.EHSystemNotification.SYSTEM_DID_FAIL_TO_SEND_FRIEND_REQUEST));
            }
        });
    }

    public void importFromCalendarWithID (String calendarID, boolean generateGapsBetweenClasses)
    {
        Collection<Event> importedEvents = new ArrayList<>();

        Calendar lastMondayAtStartOfDay = Calendar.getInstance();
        lastMondayAtStartOfDay.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        lastMondayAtStartOfDay.set(Calendar.HOUR_OF_DAY, 0);
        lastMondayAtStartOfDay.set(Calendar.MINUTE, 0);
        lastMondayAtStartOfDay.set(Calendar.SECOND, 0);

        Calendar nextFridayAtEndOfDay = Calendar.getInstance();
        nextFridayAtEndOfDay.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        nextFridayAtEndOfDay.set(Calendar.HOUR_OF_DAY, 23);
        nextFridayAtEndOfDay.set(Calendar.MINUTE, 59);
        nextFridayAtEndOfDay.set(Calendar.SECOND, 59);

        String selection = "((" + CalendarContract.Calendars._ID + calendarID + ") AND ( " + CalendarContract.Events.DTSTART + " >= " + lastMondayAtStartOfDay.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + nextFridayAtEndOfDay.getTimeInMillis() + " ))";

        // Get events between last Monday and next friday
        Cursor cursor = EHApplication.getAppContext().getContentResolver()
                .query(Uri.parse("content://com.android.calendar/events"),
                        new String[]{"calendar_id", "title", "description", "dtstart", "dtend", "eventLocation"},
                        selection,
                        null,
                        null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            String name = cursor.getString(1);

            Calendar globalCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            Calendar startHour = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            startHour.set(0, 0, 0, 0, 0, 0);

            globalCalendar.setTimeInMillis(Long.parseLong(cursor.getString(3)));
            startHour.set(Calendar.DAY_OF_WEEK, globalCalendar.get(Calendar.DAY_OF_WEEK));
            startHour.set(Calendar.HOUR_OF_DAY, globalCalendar.get(Calendar.HOUR_OF_DAY));
            startHour.set(Calendar.MINUTE, globalCalendar.get(Calendar.MINUTE));
            startHour.set(Calendar.SECOND, 0);

            Calendar endHour = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            endHour.set(0, 0, 0, 0, 0, 0);

            globalCalendar.setTimeInMillis(Long.parseLong(cursor.getString(4)));
            endHour.set(Calendar.DAY_OF_WEEK, globalCalendar.get(Calendar.DAY_OF_WEEK));
            endHour.set(Calendar.HOUR_OF_DAY, globalCalendar.get(Calendar.HOUR_OF_DAY));
            endHour.set(Calendar.MINUTE, globalCalendar.get(Calendar.MINUTE));
            endHour.set(Calendar.SECOND, 0);

            String location = cursor.getString(5);

            Calendar localCalendarWithStartDate = Calendar.getInstance();
            localCalendarWithStartDate.setTimeInMillis(Long.parseLong(cursor.getString(3)));
            int localWeekDayNumber = localCalendarWithStartDate.get(Calendar.DAY_OF_WEEK);

            Event newEvent = new Event(Event.EventType.CLASS, Optional.of(name), Optional.of(location), startHour, endHour);

            DaySchedule weekDayDaySchedule = getSchedule().getWeekDays()[localWeekDayNumber];
            weekDayDaySchedule.addEvent(newEvent);

            cursor.moveToNext();
        }

        if (generateGapsBetweenClasses)
        {
            //TODO: Calculate Gaps and add them
        }
    }

    public String getEncodedRepresentation()
    {
        StringBuilder sb = new StringBuilder();

        // Add username
        sb.append(getUsername());
        sb.append(splitCharacter);
        // Add names
        sb.append(getFirstNames());
        sb.append(separationCharacter);
        sb.append(getLastNames());
        sb.append(splitCharacter);
        // Add phone
        sb.append(getPhoneNumber());
        sb.append(splitCharacter);
        // Add image
        sb.append(getImageURL().get());
        sb.append(splitCharacter);

        boolean firstEvent = true;
        // Add events
        for (int i = 1; i < getSchedule().getWeekDays().length; i++)
        {
            DaySchedule currentDS = getSchedule().getWeekDays()[i];
            for (int j = 0; j < currentDS.getEvents().size(); j++)
            {
                Event currentEvent = currentDS.getEvents().get(j);
                Event.EventType eventType = currentEvent.getType();
                DecimalFormat mFormat = new DecimalFormat("00");

                if (firstEvent) firstEvent = false;
                else if (!firstEvent) sb.append(multipleElementsCharacter);
                // Add Class and weekday
                sb.append(eventType.equals(Event.EventType.CLASS) ? 'C' : 'G');
                sb.append(separationCharacter);
                sb.append(i);
                sb.append(separationCharacter);
                // Add hours
                sb.append(mFormat.format(currentEvent.getStartHour().get(Calendar.HOUR_OF_DAY)));
                sb.append(hourMinuteSeparationChacter);
                sb.append(mFormat.format(currentEvent.getStartHour().get(Calendar.MINUTE)));
                sb.append(separationCharacter);
                sb.append(mFormat.format(currentEvent.getEndHour().get(Calendar.HOUR_OF_DAY)));
                sb.append(hourMinuteSeparationChacter);
                sb.append(mFormat.format(currentEvent.getEndHour().get(Calendar.MINUTE)));
            }
        }
        sb.append(splitCharacter);
        return sb.toString();
    }

    public User addFriendFromStringEncodedFriendRepresentation(String encodedUser) throws Exception
    {
        User newFriend = null;

        String[] categories = encodedUser.split("\\\\");
        // Get Username
        String username = categories[0];
        // Get Names
        String[] completeName = categories[1].split(Character.toString(separationCharacter));
        String firstNames = completeName[0].trim();
        String lastNames = completeName[1].trim();
        // Get Phone and Image
        String phoneNumber = categories[2];

        Optional<String> imageURL = categories.length >= 4 ? Optional.of(categories[3]) : Optional.<String>absent();

        // TODO: Set correct friend last actualization date
        Date lastUpdated = new Date();
        newFriend = new User(username, firstNames, lastNames, phoneNumber, imageURL, username, lastUpdated);

        String[] gaps = categories.length < 5 ? new String[0] : categories[4].split(Character.toString(multipleElementsCharacter));
        for (String gap : gaps)
        {
            String[] gapValues = gap.split(Character.toString(separationCharacter));
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();

            Event.EventType eventType = gapValues[0].equals("G") ? Event.EventType.GAP : Event.EventType.CLASS;
            int weekday = Integer.parseInt(gapValues[1]);
            // Get Start Date
            String[] startTimeValues = gapValues[2].split(Character.toString(hourMinuteSeparationChacter));
            startTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTimeValues[0]));
            startTime.set(Calendar.MINUTE, Integer.parseInt(startTimeValues[1]));
            // Get End Date
            String[] endTimeValues = gapValues[3].split(Character.toString(hourMinuteSeparationChacter));
            endTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTimeValues[0]));
            endTime.set(Calendar.MINUTE, Integer.parseInt(startTimeValues[1]));

            Event newEvent = new Event(eventType, startTime, endTime);
            newFriend.getSchedule().getWeekDays()[weekday].addEvent(newEvent);
        }

        // TODO: Check if existing with a HashMap.
        boolean existing = false;
        for (int i = 0; i < System.instance.getAppUser().friends.size() && !existing; i++)
        {
            // If friend already exist
            if (System.instance.getAppUser().friends.get(i).getUsername().equals(newFriend.getUsername()))
            {
                existing = true;
                System.instance.getAppUser().friends.set(i, newFriend);
            }
        }
        if (!existing) friends.add(newFriend);

        return newFriend;
    }

    public List<User> getIncomingFriendRequests()
    {
        return incomingFriendRequests;
    }
}
