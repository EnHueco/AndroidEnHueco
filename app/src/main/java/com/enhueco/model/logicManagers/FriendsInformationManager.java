package com.enhueco.model.logicManagers;

import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.model.*;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Diego on 2/28/16.
 */
public class FriendsInformationManager extends LogicManager
{
    private static FriendsInformationManager instance;

    public static FriendsInformationManager getSharedManager()
    {
        if (instance == null)
        {
            instance = new FriendsInformationManager();
        }

        return instance;
    }

    private FriendsInformationManager()
    {
    }

    /**
     * Friends sync information from the server and generates request to updated if needed via Notification Center.
     * <p/>
     * Notifications
     * - EHSystemNotification.SystemDidReceiveFriendAndScheduleUpdates in case of success
     */
    public void fetchUpdatesForFriendsAndFriendSchedules(final BasicCompletionListener completionListener)
    {
        String url = EHURLS.BASE + EHURLS.FRIENDS_SYNC_SEGMENT;
        ConnectionManagerArrayRequest request = new ConnectionManagerArrayRequest(url, HTTPMethod.GET, Optional.<String>absent
                ());

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONArray>()
        {
            @Override
            public void onSuccess(JSONArray friendsJSON)
            {
                try
                {
                    AppUser appUser = EnHueco.getInstance().getAppUser();

                    ArrayList<UserSync> friendsToSync = new ArrayList<>();
                    HashMap<String, UserSync> friendsInServer = new HashMap<>();

                    for (int i = 0; i < friendsJSON.length(); i++)
                    {
                        JSONObject friendJSON = friendsJSON.getJSONObject(i);

                        UserSync userSync = new UserSync(friendJSON);
                        friendsInServer.put(userSync.getUsername(), userSync);

                        boolean isNewFriend = !appUser.getFriends().containsKey(userSync.getUsername());

                        if (isNewFriend
                                || appUser.getFriends().get(userSync.getUsername()).getUpdatedOn().isBefore(userSync.getUpdatedOn())
                                || appUser.getFriends().get(userSync.getUsername()).getSchedule().getUpdatedOn()
                                .isBefore(userSync.getScheduleUpdatedOn()))
                        {
                            friendsToSync.add(userSync);
                        }
                    }

                    // Delete not found friends
                    ArrayList<User> friendsToDelete = new ArrayList<>();

                    for (User friend : appUser.getFriends().values())
                    {
                        if (!friendsInServer.containsKey(friend.getUsername())) friendsToDelete.add(friend);
                    }
                    for (User friend : friendsToDelete)
                    {
                        appUser.getFriends().remove(friend.getUsername());
                    }

                    // Sync friends
                    boolean hasToSyncFriends = friendsToDelete.size() > 0 || friendsToSync.size() > 0;

                    if (hasToSyncFriends)
                    {
                        JSONArray jsonArray = new JSONArray();
                        for(UserSync user : friendsToSync)
                        {
                            jsonArray.put(user.toJSON());
                        }
                        _fetchUpdatesForFriendsAndFriendSchedules(jsonArray, completionListener);
                    }
                    else
                    {
                        callCompletionListenerSuccessHandlerOnMainThread(completionListener);
                    }
                }

                catch (JSONException e)
                {
                    callCompletionListenerFailureHandlerOnMainThread(completionListener, e);
                }
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                callCompletionListenerFailureHandlerOnMainThread(completionListener, error.error);
            }
        });
    }

    /**
     * Fetches full friends and schedule information from the server
     * and notifies the result via Notification Center.
     * Notifications
     * - EHSystemNotification.SystemDidReceiveFriendAndScheduleUpdates in case of success
     */
    private void _fetchUpdatesForFriendsAndFriendSchedules(JSONArray friendArray, final BasicCompletionListener completionListener)
    {
        String url = EHURLS.BASE + EHURLS.FRIENDS_SEGMENT;
        ConnectionManagerArrayRequest request = new ConnectionManagerArrayRequest(url, HTTPMethod.POST, Optional.of(friendArray.toString()));

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONArray>()
        {
            @Override
            public void onSuccess(JSONArray friendsJSON)
            {
                try
                {
                    AppUser appUser = EnHueco.getInstance().getAppUser();

                    for (int i = 0; i < friendsJSON.length(); i++)
                    {
                        JSONObject friendJSON = friendsJSON.getJSONObject(i);

                        User newFriend = new User(friendJSON);
                        appUser.getFriends().put(newFriend.getUsername(), newFriend);
                    }

                    PersistenceManager.getSharedManager().persistData();
                    callCompletionListenerSuccessHandlerOnMainThread(completionListener);

                }
                catch (JSONException | IOException e)
                {
                    callCompletionListenerFailureHandlerOnMainThread(completionListener, e);
                }
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                callCompletionListenerFailureHandlerOnMainThread(completionListener, error.error);
            }
        });
    }
}
