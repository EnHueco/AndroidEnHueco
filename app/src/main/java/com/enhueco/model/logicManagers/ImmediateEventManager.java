package com.enhueco.model.logicManagers;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.enhueco.model.EHApplication;
import com.enhueco.model.logicManagers.CurrentStateManager.CurrentStateManagerNotification;
import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.immediateEvent.ImmediateEvent;
import com.enhueco.model.model.immediateEvent.InstantFreeTimeEvent;
import com.enhueco.model.model.immediateEvent.InvisibilityEvent;
import com.enhueco.model.other.BasicCompletionListener;
import com.enhueco.model.other.EHURLS;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Diego on 5/1/16.
 */
public class ImmediateEventManager
{
    private static ImmediateEventManager instance;

    public static ImmediateEventManager getSharedManager()
    {
        if (instance == null)
        {
            instance = new ImmediateEventManager();
        }

        return instance;
    }

    private void updateImmediateEvent(ImmediateEvent iEvent, final BasicCompletionListener completionListener)
    {
        try
        {
            JSONObject iEventJSON = iEvent.toJSON();
            final ConnectionManagerObjectRequest request = new ConnectionManagerObjectRequest(EHURLS.BASE + EHURLS.IMMEDIATE_EVENTS_SEGMENT, HTTPMethod.PUT, Optional.of(iEventJSON.toString()));
            ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONObject>()
            {
                @Override
                public void onSuccess(JSONObject jsonResponse) throws JSONException
                {
                    Log.v("IMMEDIATE EVENT MANAGER", "Update Successful");
                    Log.v("IMMEDIATE EVENT MANAGER", jsonResponse.toString());

                    ImmediateEvent event = new ImmediateEvent(jsonResponse);
                    EnHueco.getInstance().getAppUser().getSchedule().setInstantFreeTimePeriod(Optional.of(event));

                    if (PersistenceManager.getSharedManager().persistData())
                    {
                        new Handler(Looper.getMainLooper()).post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                completionListener.onSuccess();
                            }
                        });

                        LocalBroadcastManager.getInstance(EHApplication.getAppContext()).sendBroadcast(new Intent(CurrentStateManagerNotification.DID_POST_INSTANT_FREE_TIME_PERIOD));
                    }
                    else
                    {
                        onFailure(new ConnectionManagerCompoundError(new Exception("Persistence failed"), request));
                    }
                }


                @Override
                public void onFailure(final ConnectionManagerCompoundError error)
                {
                    error.error.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            completionListener.onFailure(error.error);
                        }
                    });
                }
            });
        }

        catch (final JSONException e)
        {
            e.printStackTrace();
            new Handler(Looper.getMainLooper()).post(new Runnable()
            {
                @Override
                public void run()
                {
                    completionListener.onFailure(e);
                }
            });
        }

    }


    /**
     * Posts an instant free time period that everyone sees and that overrides any classes present in the app user's schedule during the instant free time period duration.
     * Network operation must succeed immediately or else the newFreeTimePeriod is discarded.
     *
     * @params InstantFreeTimeEvent Event that represents the free time period to be posted
     */
    public void createInstantFreeTimeEvent(InstantFreeTimeEvent event, BasicCompletionListener completionListener)
    {
        updateImmediateEvent(event, completionListener);
    }

    public void deleteInstantFreeTimeEvent(BasicCompletionListener completionListener)
    {
        deleteImmediateEvent(completionListener);
    }

    private void deleteImmediateEvent(final BasicCompletionListener completionListener)
    {
        Optional<ImmediateEvent> event = EnHueco.getInstance().getAppUser().getSchedule().getInstantFreeTimePeriod();
        if (event.isPresent())
        {
            Calendar currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            ImmediateEvent deletion;
            if (event.get().getType().equals(ImmediateEvent.ImmediateEventType.EVENT))
            {
                deletion = new InstantFreeTimeEvent(event.get().getName(), currentTime, event.get().getLocation());
                updateImmediateEvent(deletion, completionListener);
            }
            else if (event.get().getType().equals(ImmediateEvent.ImmediateEventType.INVISIBILITY))
            {
                deletion = new InvisibilityEvent(currentTime);
                updateImmediateEvent(deletion, completionListener);
            }
            else
            {
                completionListener.onFailure(new Exception("Current immediate event type is invalid"));
            }
        }

    }


    /**
     * Makes the user invisible to everyone else for the time provided
     *
     * @param seconds Seconds that the invisibility should last
     */
    public void turnInvisibleForTimeInterval(int seconds, final BasicCompletionListener completionListener)
    {
        Calendar expiryCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiryCalendar.add(Calendar.SECOND, seconds);

        InvisibilityEvent event = new InvisibilityEvent(expiryCalendar);
        updateImmediateEvent(event, completionListener);
    }

    public void turnVisible(final BasicCompletionListener completionListener)
    {
        deleteImmediateEvent(completionListener);
    }
}
