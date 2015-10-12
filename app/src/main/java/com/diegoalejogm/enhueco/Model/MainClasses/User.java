package com.diegoalejogm.enhueco.Model.MainClasses;

import android.net.Uri;
import com.google.common.base.Optional;

import java.util.Date;

/**
 * Created by Diego on 10/9/15.
 */
public class User extends EHSynchronizable
{
    private String username;
    private String firstNames;
    private String lastNames;

    private Optional<Uri> imageURL;
    private String phoneNumber;

    private Schedule schedule = new Schedule();

    public User(String username, String firstNames, String lastNames, String phoneNumber, Optional<Uri> imageURL, String ID, Date lastUpdatedOn)
    {
        super(ID, lastUpdatedOn);

        // Initialize User Data
        this.username = username;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.phoneNumber = phoneNumber;
        this.imageURL = imageURL;
    }

    @Override
    public String toString()
    {
        return firstNames + " " + lastNames;
    }

    public String getUsername()
    {
        return username;
    }

    public String name()
    {
        return firstNames + " " + lastNames;
    }

    public Optional<Event> getCurrentGap ()
    {
        // TODO
        return null;
    }

    public Optional<Event> getNextEvent ()
    {
        // TODO
        return null;
    }
}
