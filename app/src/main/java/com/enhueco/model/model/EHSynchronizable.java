package com.enhueco.model.model;

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
