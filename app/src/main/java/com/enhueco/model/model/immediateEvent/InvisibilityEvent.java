package com.enhueco.model.model.immediateEvent;

import org.joda.time.LocalTime;

import java.util.Calendar;

/**
 * Created by Diego on 5/1/16.
 */
public class InvisibilityEvent extends ImmediateEvent
{

    public InvisibilityEvent(LocalTime endHour)
    {
        super("", ImmediateEventType.INVISIBILITY, endHour, "");
    }
}
