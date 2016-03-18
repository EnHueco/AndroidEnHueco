package com.enhueco.model.logicManagers;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import com.enhueco.model.EHApplication;
import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.model.AppUser;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.Event;
import com.enhueco.model.model.User;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.CompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.view.FriendRequestsActivity;
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
    private static FriendsManager instance;

    public static FriendsManager getSharedManager()
    {
        if (instance == null)
        {
            instance = new FriendsManager();
        }

        return instance;
    }

    private FriendsManager() {}

    /**
     * Fetches updates for both outgoing and incoming friend requests on the server and notifies the result via Notification Center.
     * Notifications
     * -EHSystemNotification.SystemDidReceiveFriendRequestUpdates in case of success
     */
    public void fetchFriendRequests()
    {
        ConnectionManagerArrayRequest incomingRequestsRequest = new ConnectionManagerArrayRequest(EHURLS.BASE + EHURLS.INCOMING_FRIEND_REQUESTS_SEGMENT, HTTPMethod.GET, Optional.<String>absent());
        ConnectionManager.sendAsyncRequest(incomingRequestsRequest, new ConnectionManagerCompletionHandler<JSONArray>()
        {
            @Override
            public void onSuccess(JSONArray array)
            {
                try
                {
                    ArrayList<User> requests = new ArrayList<>();
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
    public void sendFriendRequestToUserRequestWithUsername(String username, final BasicCompletionListener listener)
    {
        ConnectionManagerObjectRequest request = new ConnectionManagerObjectRequest(EHURLS.BASE + EHURLS.FRIENDS_SEGMENT + "/" + username + "/", HTTPMethod.POST, Optional.<String>absent());

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONObject>()
        {
            @Override
            public void onSuccess(JSONObject response)
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
    public User addFriendFromStringEncodedFriendRepresentation(String encodedUser)
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

    public void acceptFriendRequestFromUserWithUsername(String username, final BasicCompletionListener listener)
    {
        String url = EHURLS.BASE + EHURLS.FRIENDS_SEGMENT + username + "/";

        ConnectionManagerObjectRequest request = new ConnectionManagerObjectRequest(url, HTTPMethod.POST, Optional.<String>absent());

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONObject>()
        {
            @Override
            public void onSuccess(JSONObject friendship)
            {
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
    public void searchUsers(String id, final CompletionListener<List<User>> listener)
    {
        ConnectionManagerArrayRequest request = new ConnectionManagerArrayRequest(EHURLS.BASE + EHURLS.USERS_SEARCH + id, HTTPMethod.GET, Optional.<String >absent());

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONArray>()
        {
            @Override
            public void onSuccess(JSONArray array)
            {
                try
                {
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
