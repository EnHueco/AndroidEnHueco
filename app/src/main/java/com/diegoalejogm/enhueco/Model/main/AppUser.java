package com.diegoalejogm.enhueco.model.main;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.diegoalejogm.enhueco.model.managers.connection.*;
import com.diegoalejogm.enhueco.model.managers.SynchronizationManager;

import com.diegoalejogm.enhueco.model.EHApplication;
import com.diegoalejogm.enhueco.model.managers.ProximityManager;
import com.diegoalejogm.enhueco.model.other.BasicOperationCompletionListener;
import com.diegoalejogm.enhueco.model.other.EHURLS;
import com.diegoalejogm.enhueco.model.other.JSONResponse;
import com.diegoalejogm.enhueco.model.structures.Tuple;
import com.diegoalejogm.enhueco.view.FriendRequestsActivity;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;

public class AppUser extends User implements Serializable
{
    //////////////////////////////////
    //          Attributes          //
    //////////////////////////////////

    /**
     * Log String for this class
     */
    private static final String LOG = "AppUser";

    /**
     * App User Token to acces API services
     */
    private String token;

    /**
     * Dictionary of app user's friends
     */
    private HashMap<String, User> friends = new HashMap<>();

    /**
     * Values for persistence
     */
    public static final String FILE_NAME = "appUser";


    // Values for QR encoding
    private static final char splitCharacter = '\\';
    private static final char separationCharacter = '-';
    private static final char multipleElementsCharacter = ',';
    private static final char hourMinuteSeparationChacter = ':';

    //////////////////////////////////
    //    Constructors & Helpers    //
    //////////////////////////////////

    public AppUser(String username, String token, String firstNames, String lastNames, String phoneNumber, Optional<String> imageURL, String ID, Date lastUpdatedOn)
    {
        super(username, firstNames, lastNames, phoneNumber, imageURL, ID, lastUpdatedOn);

        this.token = token;
    }

    public static AppUser userFromJSONObject(JSONObject object) throws JSONException, ParseException
    {
        User user = User.fromJSONObject(object.getJSONObject("user"));
        String token = object.getString("value");
        return new AppUser(user.getUsername(), token, user.getFirstNames(), user.getLastNames(), user.getPhoneNumber(), user.getImageURL(), user.getID(), user.getUpdatedOn());
    }


    //////////////////////////////////
    //      Main Functionality      //
    //////////////////////////////////

    @Override
    public void refreshIsNearby()
    {
        if (getCurrentBSSID().isPresent())
        {
            for (User friend : friends.values())
            {
                friend.refreshIsNearby();
            }
        }
    }

    /**
     * Checks for and downloads any updates from the server including
     * Session Status, Friend list, Friends Schedule, User's Info
     * -EHSystemNotification.SYSTEM_DID_RECEIVE_APPUSER_UPDATE in case of success
     */
    public void fetchUpdatesForAppUserAndSchedule()
    {
        try
        {
            ConnectionManagerRequest incomingRequestsRequest = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.ME_SEGMENT, HTTPMethod.GET, Optional.<JSONObject>absent(), false);
            ConnectionManager.sendAsyncRequest(incomingRequestsRequest, new ConnectionManagerCompletionHandler()
            {
                @Override
                public void onSuccess(JSONResponse responseJSON)
                {
                    try
                    {
                        JSONObject responseObject = responseJSON.jsonObject;

                        User user = User.fromJSONObject(responseObject);
                        setImageURL(user.getImageURL());
                        setPhoneNumber(user.getPhoneNumber());

                        String scheduleUpdatedOnString = responseObject.getString("schedule_updated_on");
                        Date scheduleUpdatedOn = EHSynchronizable.dateFromServerString(scheduleUpdatedOnString);

                        Schedule schedule = Schedule.fromJSON(scheduleUpdatedOn, responseObject.getJSONArray("gap_set"));
                        setSchedule(schedule);

                        Intent intent = new Intent(System.EHSystemNotification.SYSTEM_DID_RECEIVE_APPUSER_UPDATE);
                        LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(intent);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(ConnectionManagerCompoundError error)
                {
                    Log.v(LOG, error.toString());
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Fetches updates for both outgoing and incoming friend requests on the server and notifies the result via Notification Center.
     * Notifications
     * -EHSystemNotification.SystemDidReceiveFriendRequestUpdates in case of success
     */
    public void fetchFriendRequests()
    {
        ConnectionManagerRequest incomingRequestsRequest = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.INCOMING_FRIEND_REQUESTS_SEGMENT, HTTPMethod.GET, Optional.<JSONObject>absent(), true);
        ConnectionManager.sendAsyncRequest(incomingRequestsRequest, new ConnectionManagerCompletionHandler()
        {
            @Override
            public void onSuccess(JSONResponse responseJSON)
            {
                try
                {
                    ArrayList<User> requests = new ArrayList<>();
                    JSONArray array = responseJSON.jsonArray;
                    for (int i = 0; i < array.length(); i++)
                    {
                        JSONObject user = array.getJSONObject(i);
                        requests.add(User.fromJSONObject(user));
                    }
                    Intent intent = new Intent(System.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_REQUEST_UPDATES);
                    intent.putExtra(FriendRequestsActivity.EXTRA_REQUESTS, requests);
                    LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(intent);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                Log.v(LOG, error.toString());
            }
        });
    }

    /**
     * Friends sync information from the server and generates request to updated if needed via Notification Center.
     * <p/>
     * Notifications
     * - EHSystemNotification.SystemDidReceiveFriendAndScheduleUpdates in case of success
     */
    public void fetchUpdatesForFriendsAndFriendSchedules()
    {

        String url = EHURLS.BASE + EHURLS.FRIENDS_SYNC_SEGMENT;
        ConnectionManagerRequest r = new ConnectionManagerRequest(url, HTTPMethod.GET, Optional.<JSONObject>absent(), true);

        ConnectionManager.sendAsyncRequest(r, new ConnectionManagerCompletionHandler()
        {
            @Override
            public void onSuccess(JSONResponse response)
            {
                JSONArray friendsJSON = response.jsonArray;

                try
                {
                    JSONArray friendsToSync = new JSONArray();
                    HashMap<String, Boolean> friendsInServer = new HashMap<String, Boolean>();

                    for (int i = 0; i < friendsJSON.length(); i++)
                    {
                        JSONObject friendJSON = friendsJSON.getJSONObject(i);

                        String friendJSONID = friendJSON.getString("login"); friendsInServer.put(friendJSONID, true);
                        Date serverFriendupdatedOn = EHSynchronizable.dateFromServerString(friendJSON.getString("updated_on"));
                        Date serverFriendScheduleupdatedOn = EHSynchronizable.dateFromServerString(friendJSON.getString("schedule_updated_on"));

                        // TODO: Use hash to search user
                        User friendFound = null;

                        for (User friend : friends.values())
                        {
                            if (friend.getUsername().equals(friendJSONID)) friendFound = friend;
                        }

                        if (friendFound == null
                                || serverFriendupdatedOn.getTime() > friendFound.getUpdatedOn().getTime()
                                || serverFriendScheduleupdatedOn.getTime() > friendFound.getSchedule().getUpdatedOn().getTime())
                        {
                            friendJSON.remove("updated_on");
                            friendJSON.remove("schedule_updated_on");
                            friendsToSync.put(friendJSON);
                        }
                    }

                    boolean removeFriend = false;

                    for (User friend : friends.values())
                    {
                        if (!friendsInServer.containsKey(friend.getUsername()))
                        {
                            friends.remove(friend);
                            removeFriend = true;
                        }
                    }

                    if(removeFriend)
                    {
                        LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(new Intent(System.EHSystemNotification.SYSTEM_DID_DELETE_FRIEND));
                    }

                    if (friendsToSync.length() > 0)
                    {
                        _fetchUpdatesForFriendsAndFriendSchedules(friendsToSync);
                    }
                }

                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                Log.e(LOG, error.toString());
            }
        });
    }

    /**
     * Fetches full friends and schedule information from the server
     * and notifies the result via Notification Center.
     * Notifications
     * - EHSystemNotification.SystemDidReceiveFriendAndScheduleUpdates in case of success
     */
    private void _fetchUpdatesForFriendsAndFriendSchedules(JSONArray friendArray)
    {
        String url = EHURLS.BASE + EHURLS.FRIENDS_SEGMENT;
        ConnectionManagerArrayRequest request = new ConnectionManagerArrayRequest(url, HTTPMethod.POST, Optional.of(friendArray), true);

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler()
        {
            @Override
            public void onSuccess(JSONResponse jsonResponse)
            {
                try
                {
                    JSONArray friendsJSON = jsonResponse.jsonArray;

                    for (int i = 0; i < friendsJSON.length(); i++)
                    {
                        JSONObject friendJSON = friendsJSON.getJSONObject(i);

                        // TODO: Use hash to search user
                        User oldFriend = null;
                        for (User friend : friends.values())
                        {
                            if (friend.getUsername().equals(friendJSON.getString("login"))) oldFriend = friend;
                        }

                        if (oldFriend != null)
                        {
                            oldFriend.updateWithJSON(friendJSON);
                        }
                        else
                        {
                            User newFriend = User.fromJSONObject(friendJSON);
                            friends.put(newFriend.getUsername(), newFriend);
                        }
                    }

                    LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(new Intent(System.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_AND_SCHEDULE_UPDATES));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                Log.e(LOG, error.toString());
            }
        });

    }

    /**
<<<<<<< HEAD:app/src/main/java/com/diegoalejogm/enhueco/Model/main/AppUser.java
     * Returns a list of tuples of friends & events of those friends
     * that are currently available.
=======
     * Returns friends who are currently nearby and for who the app user has not been notified for
     * a time longer than ProximityManager.MINIMUM_TIME_INTERVAL_BETWEEN_NOTIFICATIONS
     */
    public Collection<User> getFriendsCurrentlyNearbyAndEligibleForNotification ()
    {
        return Collections2.filter(friends.values(), new Predicate<User>()
        {
            @Override
            public boolean apply(User friend)
            {
                return friend.isNearby()
                        && (!friend.getLastNotifiedNearbyStatusDate().isPresent()
                        || new Date().getTime()-friend.getLastNotifiedNearbyStatusDate().get().getTime() > ProximityManager.MINIMUM_TIME_INTERVAL_BETWEEN_NOTIFICATIONS);
            }
        });
    }

    /**
     * Returns all friends that are currently available.
     * @return Friends with their current free time period
     */
    public List<Tuple<User, Event>> getCurrentlyAvailableFriends()
    {
        List<Tuple<User, Event>> friendsAndFreeTimePeriods = new ArrayList<>();

        for (User friend : friends.values())
        {
            Optional<Event> currentFreeTimePeriod = friend.getCurrentFreeTimePeriod();

            if (currentFreeTimePeriod.isPresent())
            {
                friendsAndFreeTimePeriods.add(new Tuple<User, Event>(friend, currentFreeTimePeriod.get()));
            }
        }

        return friendsAndFreeTimePeriods;
    }

    /**
     * Returns a schedule with the common free time periods of
     * the users provided.
     * @return schedule A schedule with the common free time periods of all users.
     */
    public Schedule getCommonFreeTimePeriodsScheduleForUsers(User[] users)
    {
        Date currentDate = new Date();
        Schedule commonFreeTimePeriodsSchedule = new Schedule();

        if (users.length < 2) return commonFreeTimePeriodsSchedule;

        for (int i = 1; i < getSchedule().getWeekDays().length; i++)
        {
            Predicate<Event> eventsFilterPredicate = new Predicate<Event>()
            {
                @Override
                public boolean apply(Event event)
                {
                    return event.getType().equals(Event.EventType.FREE_TIME);
                }
            };

            Collection<Event> currentCommonFreeTimePeriods = Collections2.filter(users[0].getSchedule().getWeekDays()[i].getEvents(), eventsFilterPredicate);

            for (int j = 1; j < users.length; j++)
            {
                Collection<Event> newCommonFreeTimePeriods = new ArrayList<>();

                for (Event freeTimePeriod1 : currentCommonFreeTimePeriods)
                {
                    Date startHourInCurrentDate1 = freeTimePeriod1.getStartHourInDate(currentDate);
                    Date endHourInCurrentDate1 = freeTimePeriod1.getEndHourInDate(currentDate);

                    for (Event freeTimePeriod2 : Collections2.filter(users[j].getSchedule().getWeekDays()[i].getEvents(), eventsFilterPredicate))
                    {
                        Date startHourInCurrentDate2 = freeTimePeriod2.getStartHourInDate(currentDate);
                        Date endHourInCurrentDate2 = freeTimePeriod2.getEndHourInDate(currentDate);

                        if (!(endHourInCurrentDate1.before(startHourInCurrentDate2) || startHourInCurrentDate1.after(endHourInCurrentDate2)))
                        {
                            Calendar startHour = ((startHourInCurrentDate1.after(startHourInCurrentDate2) && startHourInCurrentDate1.before(endHourInCurrentDate2)) ? freeTimePeriod1.getStartHour() : freeTimePeriod2.getStartHour());
                            Calendar endHour = ((endHourInCurrentDate1.after(startHourInCurrentDate2) && endHourInCurrentDate1.before(endHourInCurrentDate2)) ? freeTimePeriod1.getEndHour() : freeTimePeriod2.getEndHour());

                            newCommonFreeTimePeriods.add(new Event(Event.EventType.FREE_TIME, startHour, endHour));
                        }
                    }
                }

                currentCommonFreeTimePeriods = newCommonFreeTimePeriods;
            }

            commonFreeTimePeriodsSchedule.getWeekDays()[i].setEvents(currentCommonFreeTimePeriods);
        }

        return commonFreeTimePeriodsSchedule;
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
        ConnectionManagerRequest request = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.FRIENDS_SEGMENT + "/" + username + "/", HTTPMethod.POST, Optional.<JSONObject>absent(), false);

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler()
        {
            @Override
            public void onSuccess(JSONResponse eitherJSONObjectOrJSONArray)
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

    /**
     * Posts an instant free time period that everyone sees and that overrides any classes present in the app user's schedule during the instant free time period duration.
     * Network operation must succeed immediately or else the newFreeTimePeriod is discarded.
     * @param newFreeTimePeriod Event that represents the free time period to be posted
     */
    public void postInstantFreeTimePeriod(Event newFreeTimePeriod, BasicOperationCompletionListener listener)
    {
        listener.onSuccess();
    }

    /**
     * Imports all events from a calendar to AppUser's calendar.
     * @param calendarID ID of calendar to be imported
     * @param generateFreeTimePeriodsBetweenClasses Determines if free time periods will be generated.
     */
    public void importFromCalendarWithID(String calendarID, boolean generateFreeTimePeriodsBetweenClasses)
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

            SynchronizationManager.getSharedManager().reportNewEvent(newEvent);

            cursor.moveToNext();
        }

        if (generateFreeTimePeriodsBetweenClasses)
        {
            //TODO: Calculate free time periods and add them
        }
    }

    /**
     * Generates QR encoded representation of user.
     * @return representation QR encoded representation of user.
     */
    public String getStringEncodedRepresentation()
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
        int i = 1;

        for (DaySchedule currentDS: getSchedule().getWeekDays())
        {
            for (Event currentEvent: currentDS.getEvents())
            {
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
            i++;
        }
        sb.append(splitCharacter);
        return sb.toString();
    }

    /**
     * Adds a friend from a QR string encoded representation and returns it.
     * @param encodedUser Encoded friend QR string representation.
     * @return user New friend.
     */
    public User addFriendFromStringEncodedFriendRepresentation(String encodedUser)
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

        String[] freeTimePeriods = categories.length < 5 ? new String[0] : categories[4].split(Character.toString(multipleElementsCharacter));
        for (String freeTimePeriodString : freeTimePeriods)
        {
            String[] freeTimePeriodValues = freeTimePeriodString.split(Character.toString(separationCharacter));
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();

            Event.EventType eventType = freeTimePeriodValues[0].equals("G") ? Event.EventType.FREE_TIME : Event.EventType.CLASS;
            int weekday = Integer.parseInt(freeTimePeriodValues[1]);
            // Get Start Date
            String[] startTimeValues = freeTimePeriodValues[2].split(Character.toString(hourMinuteSeparationChacter));
            startTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTimeValues[0]));
            startTime.set(Calendar.MINUTE, Integer.parseInt(startTimeValues[1]));
            // Get End Date
            String[] endTimeValues = freeTimePeriodValues[3].split(Character.toString(hourMinuteSeparationChacter));
            endTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTimeValues[0]));
            endTime.set(Calendar.MINUTE, Integer.parseInt(startTimeValues[1]));

            Event newEvent = new Event(eventType, startTime, endTime);
            newFriend.getSchedule().getWeekDays()[weekday].addEvent(newEvent);
        }

        friends.put(newFriend.getUsername(), newFriend);

        return newFriend;
    }

    /**
     * Accept a friend request to another user with given username.
     * @param username Username of the user who's friend request will be accepted.
     */

    public void acceptFriendRequestFromUserWithUsername(String username)
    {
        String url = EHURLS.BASE + EHURLS.FRIENDS_SEGMENT + username + "/";
        Log.v(LOG, url);
        Log.v(LOG, getToken());

        ConnectionManagerRequest request = new ConnectionManagerRequest(url, HTTPMethod.POST, Optional.<JSONObject>absent(), false);

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler()
        {
            @Override
            public void onSuccess(JSONResponse responseJSON)
            {
                JSONObject friendship = responseJSON.jsonObject;
                try
                {
                    User friend = User.fromJSONObject(friendship.getJSONObject("secondUser"));
                    friends.put(friend.getUsername(), friend);
                    LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(new Intent(System.EHSystemNotification.SYSTEM_DID_ACCEPT_FRIEND_REQUEST));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(new Intent(System.EHSystemNotification.SYSTEM_DID_FAIL_TO_SEND_FRIEND_REQUEST));
            }
        });
    }

    //////////////////////////////////
    //      Getters & Setters       //
    //////////////////////////////////

    public String getToken()
    {
        return token;
    }

    public HashMap<String, User> getFriends()
    {
        return friends; //new ArrayList<User>(Arrays.asList(this));
    }
}
