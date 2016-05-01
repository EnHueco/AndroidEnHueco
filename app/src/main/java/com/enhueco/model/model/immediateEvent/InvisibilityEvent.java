package com.enhueco.model.model.immediateEvent;

import java.util.Calendar;

/**
 * Created by Diego on 5/1/16.
 */
public class InvisibilityEvent extends ImmediateEvent
{

    public InvisibilityEvent(Calendar endHour)
    {
        super("", ImmediateEventType.INVISIBILITY, endHour, "");
    }
}
