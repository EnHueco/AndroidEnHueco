package com.diegoalejogm.enhueco.Model.MainClasses;

import android.net.Uri;
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
}
