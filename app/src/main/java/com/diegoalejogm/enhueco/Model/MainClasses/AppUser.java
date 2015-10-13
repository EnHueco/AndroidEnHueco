package com.diegoalejogm.enhueco.Model.MainClasses;

import android.net.Uri;
import com.diegoalejogm.enhueco.Model.Other.Tuple;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Diego on 10/11/15.
 */
public class AppUser extends User
{
    private String token;

    private List<User> friends = new ArrayList<>();
    private List<User> outgoingFriendRequests = new ArrayList<>();
    private List<User> incomingFriendRequests = new ArrayList<>();

    public AppUser(String username, String firstNames, String lastNames, String phoneNumber, Optional<Uri> imageURL, String ID, Date lastUpdatedOn)
    {
        super(username, firstNames, lastNames, phoneNumber, imageURL, ID, lastUpdatedOn);
    }

    public List<User> getFriends()
    {
        return friends;
    }

    /**
        Returns all friends that are currently in gap.
        @return Friend in gap with their current gap
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
}
