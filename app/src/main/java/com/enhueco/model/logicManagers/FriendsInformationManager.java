package com.enhueco.model.logicManagers;

import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.model.AppUser;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.User;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.enhueco.model.other.Utilities;
import com.google.common.base.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    public void fetchUpdatesForFriendsAndFriendSchedules(BasicCompletionListener completionListener)
    {
        final BasicCompletionListener listener = (completionListener != null)? completionListener : new BasicCompletionListener()
        {
            @Override
            public void onSuccess()
            {

            }

            @Override
            public void onFailure(Exception error)
            {

            }
        };

        String url = EHURLS.BASE + EHURLS.FRIENDS_SYNC_SEGMENT;
        ConnectionManagerArrayRequest r = new ConnectionManagerArrayRequest(url, HTTPMethod.GET, Optional.<String>absent());

        ConnectionManager.sendAsyncRequest(r, new ConnectionManagerCompletionHandler<JSONArray>()
        {
            @Override
            public void onSuccess(JSONArray friendsJSON)
            {
                try
                {
                    AppUser appUser = EnHueco.getInstance().getAppUser();

                    JSONArray friendsToSync = new JSONArray();
                    HashMap<String, Boolean> friendsInServer = new HashMap<String, Boolean>();

                    for (int i = 0; i < friendsJSON.length(); i++)
                    {
                        JSONObject friendJSON = friendsJSON.getJSONObject(i);

                        String friendJSONID = friendJSON.getString("login");
                        friendsInServer.put(friendJSONID, true);
                        Date serverFriendupdatedOn = Utilities.getDateFromServerString(friendJSON.getString("updated_on"));
                        Date serverFriendScheduleupdatedOn = Utilities.getDateFromServerString(friendJSON.getString("schedule_updated_on"));

                        // TODO: Use hash to search user
                        User friendFound = null;

                        for (User friend : appUser.getFriends().values())
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

                    for (User friend : appUser.getFriends().values())
                    {
                        if (!friendsInServer.containsKey(friend.getUsername()))
                        {
                            appUser.getFriends().remove(friend);
                        }
                    }

                    boolean hasToSyncFriends = friendsToSync.length() > 0;
                    if (hasToSyncFriends) _fetchUpdatesForFriendsAndFriendSchedules(friendsToSync, listener);
                    else
                    {
                        callCompletionListenerSuccessHandlerOnMainThread(listener);
                    }
                }

                catch (JSONException e)
                {
                    callCompletionListenerFailureHandlerOnMainThread(listener, e);
                }
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                callCompletionListenerFailureHandlerOnMainThread(listener, error.error);
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
                        String friendJSONusername = friendJSON.getString("login");

                        if (appUser.getFriends().containsKey(friendJSONusername))
                        {
                            User oldFriend = appUser.getFriends().get(friendJSONusername);
                            oldFriend.updateWithJSON(friendJSON);
                        }

                        else
                        {
                            User newFriend = new User(friendJSON);
                            appUser.getFriends().put(newFriend.getUsername(), newFriend);
                        }
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
