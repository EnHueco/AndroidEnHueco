package com.diegoalejogm.enhueco.Model.MainClasses;

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
    private String ID;
    private Date updatedOn;

    public EHSynchronizable(String ID, Date lastUpdatedOn)
    {
        this.ID = ID;
        this.updatedOn = lastUpdatedOn;
    }

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

    public void setLastUpdatedOn(Date updatedOn)
    {
        this.updatedOn = updatedOn;
    }

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
}
