package com.enhueco.model.logicManagers;

import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import com.enhueco.model.EHApplication;
import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.model.*;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * Created by Diego on 2/28/16.
 */
public class ScheduleManager extends LogicManager
{
    private static ScheduleManager instance;

    public static ScheduleManager getSharedManager()
    {
        if (instance == null)
        {
            instance = new ScheduleManager();
        }

        return instance;
    }

    private ScheduleManager() {}

    /**
     * Returns a schedule with the common free time periods of
     * the users provided.
     *
     * @return schedule A schedule with the common free time periods of all users.
     */
    public Schedule getCommonFreeTimePeriodsScheduleForUsers(User[] users)
    {

        AppUser appUser = EnHueco.getInstance().getAppUser();

        Date currentDate = new Date();
        Schedule commonFreeTimePeriodsSchedule = new Schedule();
/*
        if (users.length < 2) return commonFreeTimePeriodsSchedule;

        for (int i = 1; i < appUser.getSchedule().getWeekDays().length; i++)
        {
            Predicate<Event> eventsFilterPredicate = new Predicate<Event>()
            {
                @Override
                public boolean apply(Event event)
                {
                    return event.getType().equals(Event.EventType.FREE_TIME);
                }
            };

            Collection<Event> currentCommonFreeTimePeriods = Collections2.filter(users[0].getSchedule().getWeekDays()[i].getEvents(), eventsFilterPredicate);

            for (int j = 1; j < users.length; j++)
            {
                Collection<Event> newCommonFreeTimePeriods = new ArrayList<>();

                for (Event freeTimePeriod1 : currentCommonFreeTimePeriods)
                {
                    Date startHourInCurrentDate1 = freeTimePeriod1.getStartHourInDate(currentDate);
                    Date endHourInCurrentDate1 = freeTimePeriod1.getEndHourInDate(currentDate);

                    for (Event freeTimePeriod2 : Collections2.filter(users[j].getSchedule().getWeekDays()[i].getEvents(), eventsFilterPredicate))
                    {
                        Date startHourInCurrentDate2 = freeTimePeriod2.getStartHourInDate(currentDate);
                        Date endHourInCurrentDate2 = freeTimePeriod2.getEndHourInDate(currentDate);

                        if (!(endHourInCurrentDate1.before(startHourInCurrentDate2) || startHourInCurrentDate1.after(endHourInCurrentDate2)))
                        {
                            Calendar startHour = ((startHourInCurrentDate1.after(startHourInCurrentDate2) && startHourInCurrentDate1.before(endHourInCurrentDate2)) ? freeTimePeriod1.getStartHour() : freeTimePeriod2.getStartHour());
                            Calendar endHour = ((endHourInCurrentDate1.after(startHourInCurrentDate2) && endHourInCurrentDate1.before(endHourInCurrentDate2)) ? freeTimePeriod1.getEndHour() : freeTimePeriod2.getEndHour());

                            newCommonFreeTimePeriods.add(new Event(Event.EventType.FREE_TIME, startHour, endHour));
                        }
                    }
                }

                currentCommonFreeTimePeriods = newCommonFreeTimePeriods;
            }

            commonFreeTimePeriodsSchedule.getWeekDays()[i].setEvents(currentCommonFreeTimePeriods);
        }
*/
        return commonFreeTimePeriodsSchedule;
    }

    /**
     * Reports the new event to the server.
     */
    public void reportNewEvent (Event event, final BasicCompletionListener completionListener)
    {

    }



    /**
     * Imports all events from a calendar to AppUser's calendar.
     *
     * @param calendarID                            ID of calendar to be imported
     * @param generateFreeTimePeriodsBetweenClasses Determines if free time periods will be generated.
     */
    public void importFromCalendarWithID(String calendarID, boolean generateFreeTimePeriodsBetweenClasses, BasicCompletionListener completionListener)
    {
        /*
        Collection<Event> importedEvents = new ArrayList<>();

        Calendar lastMondayAtStartOfDay = Calendar.getInstance();
        lastMondayAtStartOfDay.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        lastMondayAtStartOfDay.set(Calendar.HOUR_OF_DAY, 0);
        lastMondayAtStartOfDay.set(Calendar.MINUTE, 0);
        lastMondayAtStartOfDay.set(Calendar.SECOND, 0);

        Calendar nextFridayAtEndOfDay = Calendar.getInstance();
        nextFridayAtEndOfDay.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        nextFridayAtEndOfDay.set(Calendar.HOUR_OF_DAY, 23);
        nextFridayAtEndOfDay.set(Calendar.MINUTE, 59);
        nextFridayAtEndOfDay.set(Calendar.SECOND, 59);

        String selection = "((" + CalendarContract.Calendars._ID + calendarID + ") AND ( " + CalendarContract.Events.DTSTART + " >= " + lastMondayAtStartOfDay.getTimeInMillis() + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + nextFridayAtEndOfDay.getTimeInMillis() + " ))";

        // Get events between last Monday and next friday
        Cursor cursor = EHApplication.getAppContext().getContentResolver()
                .query(Uri.parse("content://com.android.calendar/events"),
                        new String[]{"calendar_id", "title", "description", "dtstart", "dtend", "eventLocation"},
                        selection,
                        null,
                        null);

        cursor.moveToFirst();

        AppUser appUser = EnHueco.getInstance().getAppUser();

        while (!cursor.isAfterLast())
        {
            String name = cursor.getString(1);

            Calendar globalCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            Calendar startHour = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            startHour.set(0, 0, 0, 0, 0, 0);

            globalCalendar.setTimeInMillis(Long.parseLong(cursor.getString(3)));
            startHour.set(Calendar.DAY_OF_WEEK, globalCalendar.get(Calendar.DAY_OF_WEEK));
            startHour.set(Calendar.HOUR_OF_DAY, globalCalendar.get(Calendar.HOUR_OF_DAY));
            startHour.set(Calendar.MINUTE, globalCalendar.get(Calendar.MINUTE));
            startHour.set(Calendar.SECOND, 0);

            Calendar endHour = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            endHour.set(0, 0, 0, 0, 0, 0);

            globalCalendar.setTimeInMillis(Long.parseLong(cursor.getString(4)));
            endHour.set(Calendar.DAY_OF_WEEK, globalCalendar.get(Calendar.DAY_OF_WEEK));
            endHour.set(Calendar.HOUR_OF_DAY, globalCalendar.get(Calendar.HOUR_OF_DAY));
            endHour.set(Calendar.MINUTE, globalCalendar.get(Calendar.MINUTE));
            endHour.set(Calendar.SECOND, 0);

            String location = cursor.getString(5);

            Calendar localCalendarWithStartDate = Calendar.getInstance();
            localCalendarWithStartDate.setTimeInMillis(Long.parseLong(cursor.getString(3)));
            int localWeekDayNumber = localCalendarWithStartDate.get(Calendar.DAY_OF_WEEK);

            Event newEvent = new Event(Event.EventType.CLASS, Optional.of(name), Optional.of(location), startHour, endHour);

            DaySchedule weekDayDaySchedule = appUser.getSchedule().getWeekDays()[localWeekDayNumber];
            weekDayDaySchedule.addEvent(newEvent);

            reportNewEvent(newEvent, completionListener);

            cursor.moveToNext();
        }

        if (generateFreeTimePeriodsBetweenClasses)
        {
            //TODO: Calculate free time periods and add them
        }
        */
    }

    public void reportNewEvents(ArrayList<Event> second, final BasicCompletionListener completionListener)
    {
        try
        {
            String url = EHURLS.BASE + EHURLS.EVENTS_SEGMENT;
            JSONArray eventsArray = new JSONArray();
            for(Event e : second)
            {
                eventsArray.put(e.toJSONObject());
            }
            ConnectionManagerArrayRequest request = new ConnectionManagerArrayRequest(url, HTTPMethod.POST,Optional.of(eventsArray.toString()));
            ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONArray>()
            {
                @Override
                public void onSuccess(JSONArray jsonResponse)
                {
                    try
                    {
                        Schedule schedule = EnHueco.getInstance().getAppUser().getSchedule();
                        for(int i = 0 ; i < jsonResponse.length() ; i++)
                        {
                            JSONObject eventJSON = (JSONObject) jsonResponse.get(i);
                            Event event = new Event(eventJSON);

                            schedule.getWeekDays()[event.getWeekday()].addEvent(event);
                        }
                        PersistenceManager.getSharedManager().persistData();
                        callCompletionListenerSuccessHandlerOnMainThread(completionListener);
                    }
                    catch (JSONException | IOException e)
                    {
                        callCompletionListenerFailureHandlerOnMainThread(completionListener, e);
                    }
                }

                @Override
                public void onFailure(ConnectionManagerCompoundError error)
                {
                    callCompletionListenerFailureHandlerOnMainThread(completionListener, error.error);
                }
            });
        }
        catch (JSONException e)
        {
            callCompletionListenerFailureHandlerOnMainThread(completionListener, e);
        }
    }
}
