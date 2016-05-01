package com.enhueco.model.model.immediateEvent;

import java.util.Calendar;

/**
 * Created by Diego on 5/1/16.
 */
public class InstantFreeTimeEvent extends ImmediateEvent
{

    public InstantFreeTimeEvent(String name, Calendar endHour, String location)
    {
        super(name, ImmediateEventType.EVENT, endHour, location);
    }
}
