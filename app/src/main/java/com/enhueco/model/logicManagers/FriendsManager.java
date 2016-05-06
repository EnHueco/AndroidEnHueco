package com.enhueco.model.logicManagers;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.enhueco.model.EHApplication;
import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.model.EnHueco;
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
import java.util.List;

/**
 * Created by Diego on 2/28/16.
 */
public class FriendsManager extends LogicManager
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
                        requests.add(new User(user));
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
                callCompletionListenerSuccessHandlerOnMainThread(listener);
            }

            @Override
            public void onFailure(final ConnectionManagerCompoundError error)
            {
                callCompletionListenerFailureHandlerOnMainThread(listener, error.error);
            }

        });
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
                    User friend = new User(friendship.getJSONObject("secondUser"));
                    EnHueco.getInstance().getAppUser().getFriends().put(friend.getUsername(), friend);

                    callCompletionListenerSuccessHandlerOnMainThread(listener);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(final ConnectionManagerCompoundError error)
            {
                callCompletionListenerFailureHandlerOnMainThread(listener, error.error);
            }
        });
    }
    
    public void deleteFriend (final User friend, final BasicCompletionListener completionListener)
    {
        String url = EHURLS.BASE + EHURLS.FRIENDS_SEGMENT + friend.getUsername() + "/";

        ConnectionManagerObjectRequest request = new ConnectionManagerObjectRequest(url, HTTPMethod.DELETE, Optional.<String>absent());

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONObject>()
        {
            @Override
            public void onSuccess(JSONObject friendship)
            {
                EnHueco.getInstance().getAppUser().getFriends().remove(friend.getUsername());
                PersistenceManager.getSharedManager().persistData();

                callCompletionListenerSuccessHandlerOnMainThread(completionListener);
            }

            @Override
            public void onFailure(final ConnectionManagerCompoundError error)
            {
                callCompletionListenerFailureHandlerOnMainThread(completionListener, error.error);
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
                        users.add(new User(jsonUser));
                    }

                    callCompletionListenerSuccessHandlerOnMainThread(listener, users);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(final ConnectionManagerCompoundError error)
            {
                callCompletionListenerFailureHandlerOnMainThread(listener, error.error);
            }
        });
    }
}
