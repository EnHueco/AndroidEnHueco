package com.diegoalejogm.enhueco.model.logicManagers;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import com.diegoalejogm.enhueco.model.EHApplication;
import com.diegoalejogm.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.diegoalejogm.enhueco.model.model.AppUser;
import com.diegoalejogm.enhueco.model.model.EnHueco;
import com.diegoalejogm.enhueco.model.model.Event;
import com.diegoalejogm.enhueco.model.model.User;
import com.diegoalejogm.enhueco.model.other.BasicCompletionListener;
import com.diegoalejogm.enhueco.model.other.CompletionListener;
import com.diegoalejogm.enhueco.model.other.EHURLS;
import com.diegoalejogm.enhueco.model.other.JSONResponse;
import com.diegoalejogm.enhueco.view.FriendRequestsActivity;
import com.google.common.base.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Diego on 2/28/16.
 */
public class FriendsManager
{
    /**
     * Fetches updates for both outgoing and incoming friend requests on the server and notifies the result via Notification Center.
     * Notifications
     * -EHSystemNotification.SystemDidReceiveFriendRequestUpdates in case of success
     */
    public static void fetchFriendRequests()
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
                    Intent intent = new Intent(EnHueco.EHSystemNotification.SYSTEM_DID_RECEIVE_FRIEND_REQUEST_UPDATES);
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
            }
        });
    }

    /**
     * Sends a friend request to the username provided and notifies the result via Notification Center.
     * <p>
     * Notifications
     * - EHSystemNotification.SystemDidSendFriendRequest in case of success
     * - EHSystemNotification.SystemDidFailToSendFriendRequest in case of failure
     */
    public static void sendFriendRequestToUserRequestWithUsername(String username, final BasicCompletionListener listener)
    {
        ConnectionManagerRequest request = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.FRIENDS_SEGMENT + "/" + username + "/", HTTPMethod.POST, Optional.<JSONObject>absent(), false);

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler()
        {
            @Override
            public void onSuccess(JSONResponse eitherJSONObjectOrJSONArray)
            {
                // TODO
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listener.onSuccess();
                    }
                });
            }

            @Override
            public void onFailure(final ConnectionManagerCompoundError error)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listener.onFailure(error.error);
                    }
                });
            }

        });
    }

    /**
     * Adds a friend from a QR string encoded representation and returns it.
     *
     * @param encodedUser Encoded friend QR string representation.
     * @return user New friend.
     */
    public static User addFriendFromStringEncodedFriendRepresentation(String encodedUser)
    {
        User newFriend = null;

        String[] categories = encodedUser.split("\\\\");
        // Get Username
        String username = categories[0];
        // Get Names
        String[] completeName = categories[1].split(Character.toString(AppUser.UserStringEncodingSeparationCharacters.separationCharacter));
        String firstNames = completeName[0].trim();
        String lastNames = completeName[1].trim();
        // Get Phone and Image
        String phoneNumber = categories[2];

        Optional<String> imageURL = categories.length >= 4 ? Optional.of(categories[3]) : Optional.<String>absent();

        // TODO: Set correct friend last actualization date
        Date lastUpdated = new Date();
        newFriend = new User(username, firstNames, lastNames, phoneNumber, imageURL, username, lastUpdated);

        String[] freeTimePeriods = categories.length < 5 ? new String[0] : categories[4].split(Character.toString(AppUser.UserStringEncodingSeparationCharacters.multipleElementsCharacter));
        for (String freeTimePeriodString : freeTimePeriods)
        {
            String[] freeTimePeriodValues = freeTimePeriodString.split(Character.toString(AppUser.UserStringEncodingSeparationCharacters.separationCharacter));
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();

            Event.EventType eventType = freeTimePeriodValues[0].equals("G") ? Event.EventType.FREE_TIME : Event.EventType.CLASS;
            int weekday = Integer.parseInt(freeTimePeriodValues[1]);
            // Get Start Date
            String[] startTimeValues = freeTimePeriodValues[2].split(Character.toString(AppUser.UserStringEncodingSeparationCharacters.hourMinuteSeparationCharacter));
            startTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTimeValues[0]));
            startTime.set(Calendar.MINUTE, Integer.parseInt(startTimeValues[1]));
            // Get End Date
            String[] endTimeValues = freeTimePeriodValues[3].split(Character.toString(AppUser.UserStringEncodingSeparationCharacters.hourMinuteSeparationCharacter));
            endTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTimeValues[0]));
            endTime.set(Calendar.MINUTE, Integer.parseInt(startTimeValues[1]));

            Event newEvent = new Event(eventType, startTime, endTime);
            newFriend.getSchedule().getWeekDays()[weekday].addEvent(newEvent);
        }

        EnHueco.getInstance().getAppUser().getFriends().put(newFriend.getUsername(), newFriend);

        return newFriend;
    }

    /**
     * Accept a friend request to another user with given username.
     *
     * @param username Username of the user who's friend request will be accepted.
     */

    public static void acceptFriendRequestFromUserWithUsername(String username, final BasicCompletionListener listener)
    {
        String url = EHURLS.BASE + EHURLS.FRIENDS_SEGMENT + username + "/";

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
                    EnHueco.getInstance().getAppUser().getFriends().put(friend.getUsername(), friend);

                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            listener.onSuccess();
                        }
                    });
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(final ConnectionManagerCompoundError error)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listener.onFailure(error.error);
                    }
                });
            }
        });
    }

    /**
     * Searches users with keyword id
     *
     * @param id       Keyword that searches for users
     * @param listener Listener of the event
     */
    public static void searchUsers(String id, final CompletionListener<List<User>> listener)
    {
        ConnectionManagerRequest request = new ConnectionManagerRequest(EHURLS.BASE + EHURLS.USERS_SEARCH + id, HTTPMethod.GET, Optional.<JSONObject>absent(), true);

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler()
        {
            @Override
            public void onSuccess(JSONResponse eitherJSONObjectOrJSONArray)
            {
                try
                {
                    JSONArray array = eitherJSONObjectOrJSONArray.jsonArray;
                    final ArrayList<User> users = new ArrayList<User>();
                    for (int i = 0; i < array.length(); i++)
                    {
                        JSONObject jsonUser = array.getJSONObject(i);
                        users.add(User.fromJSONObject(jsonUser));
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            listener.onSuccess(users);
                        }
                    });

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(final ConnectionManagerCompoundError error)
            {

                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listener.onFailure(error.error);
                    }
                });
            }
        });
    }
}
