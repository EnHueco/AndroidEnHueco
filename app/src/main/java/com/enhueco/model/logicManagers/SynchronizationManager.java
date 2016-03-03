package com.enhueco.model.logicManagers;

import android.os.AsyncTask;
import com.enhueco.model.logicManagers.genericManagers.connectionManager.*;
import com.enhueco.model.model.EHSynchronizable;
import com.enhueco.model.model.EnHueco;
import com.enhueco.model.model.Event;
import com.enhueco.model.other.EHURLS;
import com.google.common.base.Optional;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Diego on 10/11/15.
 */
public class SynchronizationManager
{
    private static final SynchronizationManager sharedManager = new SynchronizationManager();

    private class SynchronizationManagerQueueItem
    {
        /** ConnectionManagerObjectRequest that was attempted */
        public ConnectionManagerObjectRequest request;

        /** CompletionHandler to be called when reattempting the request */
        public ConnectionManagerCompletionHandler completionHandler;

        /** Object associated with the request (For example, the free time period that was going to be updated). */
         public EHSynchronizable associatedObject;

        public SynchronizationManagerQueueItem(ConnectionManagerObjectRequest request, ConnectionManagerCompletionHandler completionHandler, EHSynchronizable associatedObject)
        {
            this.request = request;
            this.completionHandler = completionHandler;
            this.associatedObject = associatedObject;
        }
    }

    /**
        FIFO queue containing pending requests that failed because of a network error.
     */
    private Queue<SynchronizationManagerQueueItem> pendingRequestsQueue = new LinkedBlockingDeque<>();

    private SynchronizationManager () {}

    public static SynchronizationManager getSharedManager()
    {
        return sharedManager;
    }

    private void addFailedRequestToQueue(ConnectionManagerObjectRequest request,
                                         ConnectionManagerCompletionHandler completionHandler,
                                         EHSynchronizable associatedObject)
    {
        pendingRequestsQueue.add(new SynchronizationManagerQueueItem(request, completionHandler, associatedObject));
    }

    /**
     * Attempts to retry every request in the pending requests queue in order.

     * If every request could be executed successfully the queue is emptied, if
     * any request fails the queue is emptied only partially.
     */
    public void retryPendingRequests()
    {
        class RetryPendingRequestsTask extends AsyncTask<Void, Void, Void>
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                while (trySendingSyncRequestInQueue());

                return null;
            }
        }

        new RetryPendingRequestsTask().execute();
    }

    /**
       Tries the given request asynchronously and adds the request to the queue in case it fails.

       @param request: NSURLRequest that was attempted

       @param completionHandler: CompletionHandler to be called when reattempting the request.
       @param associatedObject: Object associated with the request (For example, the free time period that was going to be updated).
     */
    public void trySendingAsyncRequestToURL(final ConnectionManagerObjectRequest request,
                                               final ConnectionManagerCompletionHandler<JSONObject> completionHandler,
                                               final EHSynchronizable associatedObject)
    {
        ConnectionManagerCompletionHandler<JSONObject> modifiedCompletionHandler = new ConnectionManagerCompletionHandler<JSONObject>()
        {
            @Override
            public void onSuccess(JSONObject jsonResponse)
            {
                completionHandler.onSuccess(jsonResponse);
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
                completionHandler.onFailure(error);

                addFailedRequestToQueue(request, completionHandler, associatedObject);
            }
        };

        ConnectionManager.sendAsyncRequest(request, modifiedCompletionHandler);
    }

    /**
        Tries the given request and adds the request to the queue in case it fails.
     */
    private boolean trySendingSyncRequestInQueue ()
    {
        return false;
//        SynchronizationManagerQueueItem item = pendingRequestsQueue.peek();
//
//        if (item == null) return false;
//
//        try
//        {
//            JSONResponse response = ConnectionManager.sendSyncRequest(item.request);
//            pendingRequestsQueue.remove();
//
//            item.completionHandler.onSuccess(response);
//
//            // TODO: Handle lastUpdatedOn
//
//            return true;
//        }
//        catch (ExecutionException | InterruptedException e)
//        {
//            item.completionHandler.onFailure(new ConnectionManagerCompoundError(e, item.request));
//
//            return false;
//        }
    }

    //Reporting

    /**
     * Reports the new event to the server.
     */
    public void reportNewEvent (Event event)
    {
        String url = EHURLS.BASE + EHURLS.EVENTS_SEGMENT;

        JSONObject eventJSON = event.toJSONObject();
        try
        {
            eventJSON.put("user", EnHueco.getInstance().getAppUser().getUsername());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        ConnectionManagerObjectRequest request = new ConnectionManagerObjectRequest(url, HTTPMethod.POST, Optional.of(eventJSON.toString()));

        ConnectionManager.sendAsyncRequest(request, new ConnectionManagerCompletionHandler<JSONObject>()
        {
            @Override
            public void onSuccess(JSONObject responseJSON)
            {
            }

            @Override
            public void onFailure(ConnectionManagerCompoundError error)
            {
            }
        });
    }
}
