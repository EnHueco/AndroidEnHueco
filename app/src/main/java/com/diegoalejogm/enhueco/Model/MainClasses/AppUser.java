package com.diegoalejogm.enhueco.Model.MainClasses;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Diego on 10/11/15.
 */
public class AppUser extends User
{
    private String token;

    private Collection<User> friends = new ArrayList<>();
    private Collection<User> outgoingFriendRequests = new ArrayList<>();
    private Collection<User> incomingFriendRequests = new ArrayList<>();

    public AppUser(String username, String token, String firstNames, String lastNames, String phoneNumber, Optional<String> imageURL, String ID, Date lastUpdatedOn)
    {
        super(username, firstNames, lastNames, phoneNumber, imageURL, ID, lastUpdatedOn);

        this.token = token;
    }

    public static AppUser appUserFromJSONObject (JSONObject object) throws JSONException, ParseException
    {
        User user = User.userFromJSONObject(object);
        String token = object.getString("token");
        return new AppUser(user.getUsername(), token, user.getFirstNames(), user.getLastNames(), user.getPhoneNumber(), user.getImageURL(), user.getID(), user.getLastUpdatedOn());
    }

    public String getToken()
    {
        return token;
    }

    public Collection<User> getFriends()
    {
        return friends;
    }

    /**
        Checks for and downloads any updates from the server including Session Status, Friend list, Friends Schedule, User's Info
     */
    public void fetchUpdates ()
    {

    }

    /**
     * Fetches updates for both outgoing and incoming friend requests on the server and notifies the result via Notification Center.
     *
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
                    catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
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
     *
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
                        Collection<User> newFriends = new ArrayList<User>();

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
                                globalCalendar.set(Calendar.HOUR, newEvent.getStartHour().get(Calendar.HOUR));
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
     *  Returns all friends that are currently in gap.
     *  @return Friend in gap with their current gap
     */
    public List<Tuple<User, Event>> getFriendsCurrentlyInGap ()
    {
        List<Tuple<User, Event>> friendsAndGaps =  new ArrayList<>();

        for (User friend: friends)
        {
            Optional<Event> currentGap = friend.getCurrentGap();

            if (currentGap.isPresent())
            {
                friendsAndGaps.add(new Tuple<User, Event>(friend, currentGap.get()));
            }
        }

        return  friendsAndGaps;
    }

    /**
        Returns a schedule with the common gaps of the users provided.
     */
    public Schedule getCommonGapsScheduleForUsers (User[] users)
    {
        Date currentDate = new Date();
        Schedule commonGapsSchedule = new Schedule();

        if (users.length < 2) return commonGapsSchedule;

        for (int i = 1; i < getSchedule().getWeekDays().length; i++)
        {
            Predicate<Event> eventsFilterPredicate = new Predicate<Event>()
            {
                @Override public boolean apply(Event event) { return event.getType().equals(Event.EventType.GAP); }
            };

            Collection<Event> currentCommonGaps = Collections2.filter(users[0].getSchedule().getWeekDays()[i].getEvents(), eventsFilterPredicate);

            for (int j = 1; j < users.length; j++)
            {
                Collection<Event> newCommonGaps = new ArrayList<>();

                for (Event gap1: currentCommonGaps)
                {
                    Date startHourInCurrentDate1 = gap1.getStartHourInDate(currentDate);
                    Date endHourInCurrentDate1 = gap1.getEndHourInDate(currentDate);

                    for (Event gap2: Collections2.filter(users[j].getSchedule().getWeekDays()[i].getEvents(), eventsFilterPredicate))
                    {
                        Date startHourInCurrentDate2 = gap2.getStartHourInDate(currentDate);
                        Date endHourInCurrentDate2 = gap2.getEndHourInDate(currentDate);

                        if (!(endHourInCurrentDate1.before(startHourInCurrentDate2) || startHourInCurrentDate1.after(endHourInCurrentDate2)))
                        {
                            Calendar startHour = ((startHourInCurrentDate1.after(startHourInCurrentDate2) && startHourInCurrentDate1.before(endHourInCurrentDate2))? gap1.getStartHour() : gap2.getStartHour());
                            Calendar endHour = ((endHourInCurrentDate1.after(startHourInCurrentDate2) && endHourInCurrentDate1.before(endHourInCurrentDate2))? gap1.getEndHour() : gap2.getEndHour());

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
     *
     * Notifications
     * - EHSystemNotification.SystemDidSendFriendRequest in case of success
     * - EHSystemNotification.SystemDidFailToSendFriendRequest in case of failure
     */
    public void sendFriendRequestToUserRequestWithUsername (String username)
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
}
