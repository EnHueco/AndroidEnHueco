package com.diegoalejogm.enhueco.Model.Other;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Diego on 10/13/15.
 */
public class Utilities
{
    public static Date dateFromServerFormattedString (String date) throws ParseException
    {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(date);
    }

    public static Calendar calendarWithWeekdayHourMinute (int weekday, int hour, int minute)
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(0, 0, 0, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.DAY_OF_WEEK, weekday);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        return calendar;
    }
}
