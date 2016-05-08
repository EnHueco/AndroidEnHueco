package com.enhueco.model.model;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Diego on 10/11/15.
 */
public class EHSynchronizable implements Serializable
{
    //////////////////////////////////
    //          Attributes          //
    //////////////////////////////////

    private String ID;
    private DateTime updatedOn;

    //////////////////////////////////
    //         Constructors         //
    //////////////////////////////////

    public EHSynchronizable(String ID, DateTime lastUpdatedOn)
    {
        this.ID = ID;
        this.updatedOn = lastUpdatedOn;
    }


    //////////////////////////////////
    //      Getters & Setters       //
    //////////////////////////////////

    public String getID()
    {
        return ID;
    }
    public void setID(String ID)
    {
        this.ID = ID;
    }

    public DateTime getUpdatedOn()
    {
        return updatedOn;
    }

    public void setUpdatedOn(DateTime updatedOn)
    {
        this.updatedOn = updatedOn;
    }
}
