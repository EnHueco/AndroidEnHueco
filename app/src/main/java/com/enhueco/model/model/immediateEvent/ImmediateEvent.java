package com.enhueco.model.model.immediateEvent;

import com.bumptech.glide.util.Util;
import com.enhueco.model.other.Utilities;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Diego on 5/1/16.
 */
public class ImmediateEvent implements Serializable
{

    public enum ImmediateEventType
    {
        EVENT("EVENT"),
        INVISIBILITY("INVISIBILITY");

        private final String text;

        /**
         * @param text
         */
        private ImmediateEventType(final String text)
        {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString()
        {
            return text;
        }
    }

    /**
     * Name of the event
     */
    private final String name;

    /**
     * Type of the event
     */
    private final ImmediateEventType type;

    /**
     * Event's end hour
     */
    private Calendar endHour;

    /**
     * Event's location
     */
    private final String location;

    protected ImmediateEvent(String name, ImmediateEventType type, Calendar endHour, String location)
    {
        this.name = name;
        this.type = type;
        this.endHour = endHour;
        this.location = location;
    }


    public JSONObject toJSON() throws JSONException
    {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("type", type.toString());
        object.put("location", location);
        object.put("valid_until", Utilities.getServerFormattedStringFromDate(endHour.getTime()));
        return object;
    }

    public ImmediateEvent(JSONObject object) throws JSONException
    {
        this.name = object.getString("name");
        this.location = object.getString("location");

        ImmediateEventType newType = null;
        for(ImmediateEventType type : ImmediateEventType.values())
        {
            if(type.toString().equals(object.getString("type")))
            {
                newType = type;
                break;
            }
        }
        if(newType == null) throw new JSONException("JSON 'type' value has no correspondence with model types");
        else this.type = newType;

        this.endHour = Calendar.getInstance();
        this.endHour.setTime(Utilities.getDateFromServerString(object.getString("valid_until")));
    }

    public String getName()
    {
        return name;
    }

    public ImmediateEventType getType()
    {
        return type;
    }

    public Calendar getEndHour()
    {
        return endHour;
    }

    public String getLocation()
    {
        return location;
    }

    public void setEndHour(Calendar endHour)
    {
        this.endHour = endHour;
    }

    /**
     * Returns the end hour (Weekday, Hour, Minute) by setting the components to the date provided.
     * @param date Date to which start hour will be set
     * @return newDate Date with new components
     */
    public Date getEndHourInDate (Date date)
    {
        Calendar globalCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        globalCalendar.setTime(date);

        globalCalendar.set(Calendar.DAY_OF_WEEK, endHour.get(Calendar.DAY_OF_WEEK));
        globalCalendar.set(Calendar.HOUR_OF_DAY, endHour.get(Calendar.HOUR_OF_DAY));
        globalCalendar.set(Calendar.MINUTE, endHour.get(Calendar.MINUTE));
        globalCalendar.set(Calendar.SECOND, 0);

        return globalCalendar.getTime();
    }
}
