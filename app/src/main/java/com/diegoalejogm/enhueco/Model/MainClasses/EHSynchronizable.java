package com.diegoalejogm.enhueco.Model.MainClasses;

import java.util.Date;

/**
 * Created by Diego on 10/11/15.
 */
public class EHSynchronizable
{
    private String ID;
    private Date lastUpdatedOn;

    public EHSynchronizable(String ID, Date lastUpdatedOn)
    {
        this.ID = ID;
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public String getID()
    {
        return ID;
    }

    public Date getLastUpdatedOn()
    {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(Date lastUpdatedOn)
    {
        this.lastUpdatedOn = lastUpdatedOn;
    }
}
