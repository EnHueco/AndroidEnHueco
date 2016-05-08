package com.enhueco.model.model.immediateEvent;

import com.enhueco.model.other.Utilities;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;

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
    private LocalTime endHour;

    /**
     * Event's location
     */
    private final String location;

    protected ImmediateEvent(String name, ImmediateEventType type, LocalTime endHour, String location)
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

        object.put("valid_until", Utilities.getServerFormattedStringFromDate(DateTime.now(DateTimeZone.UTC).withTime
                (endHour)));
        return object;
    }

    public ImmediateEvent(JSONObject object) throws JSONException
    {
        this.name = object.getString("name");
        this.location = object.getString("location");

        ImmediateEventType newType = null;

        String objectType = object.getString("type");
        for (ImmediateEventType type : ImmediateEventType.values())
        {
            if (type.toString().equals(objectType))
            {
                newType = type;
                break;
            }
        }
        if (newType == null) throw new JSONException("JSON 'type' value has no correspondence with model types");
        else this.type = newType;

        this.endHour = Utilities.getLocalTimeFromServerString(object.getString("valid_until"));
    }

    public String getName()
    {
        return name;
    }

    public ImmediateEventType getType()
    {
        return type;
    }

    public LocalTime getEndHour()
    {
        return endHour;
    }

    public String getLocation()
    {
        return location;
    }


    public boolean isCurrentlyHappening()
    {
        return endHour.isBefore(new LocalTime(DateTimeZone.UTC));
    }
}
