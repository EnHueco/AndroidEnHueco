package com.diegoalejogm.enhueco.model.main;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Diego on 10/11/15.
 */
public class EHSynchronizable implements Serializable
{
    //////////////////////////////////
    //          Attributes          //
    //////////////////////////////////

    private String ID;
    private Date updatedOn;

    //////////////////////////////////
    //         Constructors         //
    //////////////////////////////////

    public EHSynchronizable(String ID, Date lastUpdatedOn)
    {
        this.ID = ID;
        this.updatedOn = lastUpdatedOn;
    }

    //////////////////////////////////
    //      Main Functionality      //
    //////////////////////////////////

    public static Date dateFromServerString(String lastUpdated_on)
    {
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df1.setTimeZone(TimeZone.getTimeZone("UTC"));
        try
        {
            return df1.parse(lastUpdated_on);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return null;
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

    public Date getUpdatedOn()
    {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn)
    {
        this.updatedOn = updatedOn;
    }
}
