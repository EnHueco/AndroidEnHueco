package com.diegoalejogm.enhueco.Model.Other;

import android.os.AsyncTask;
import com.diegoalejogm.enhueco.Model.MainClasses.EHSynchronizable;
import com.diegoalejogm.enhueco.Model.Other.ConnectionManager.ConnectionManager;
import com.diegoalejogm.enhueco.Model.Other.ConnectionManager.ConnectionManagerCompletionHandler;
import com.diegoalejogm.enhueco.Model.Other.ConnectionManager.ConnectionManagerCompoundError;
import com.diegoalejogm.enhueco.Model.Other.ConnectionManager.ConnectionManagerRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Diego on 10/11/15.
 */
public class SynchronizationManager
{
    private SynchronizationManager sharedManager = new SynchronizationManager();

    private class SynchronizationManagerQueueItem
    {
        /** ConnectionManagerRequest that was attempted*/
        public ConnectionManagerRequest request;

        /** CompletionHandler to be called when reattempting the request */
        public ConnectionManagerCompletionHandler completionHandler;

        /** Object associated with the request (For example, the Gap that was going to be updated). */
         public EHSynchronizable associatedObject;

        public SynchronizationManagerQueueItem(ConnectionManagerRequest request, ConnectionManagerCompletionHandler completionHandler, EHSynchronizable associatedObject)
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

    public SynchronizationManager getSharedManager()
    {
        return sharedManager;
    }

    private void addFailedRequestToQueue(ConnectionManagerRequest request,
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
       @param associatedObject: Object associated with the request (For example, the Gap that was going to be updated).
     */
    public void trySendingAsyncRequestToURL(final ConnectionManagerRequest request,
                                               final ConnectionManagerCompletionHandler completionHandler,
                                               final EHSynchronizable associatedObject)
    {
        ConnectionManagerCompletionHandler modifiedCompletionHandler = new ConnectionManagerCompletionHandler()
        {
            @Override
            public void onSuccess(Either<JSONObject, JSONArray> responseJSON)
            {
                completionHandler.onSuccess(responseJSON);
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
        SynchronizationManagerQueueItem item = pendingRequestsQueue.peek();

        if (item == null) return false;

        try
        {
            Either<JSONObject, JSONArray> response = ConnectionManager.sendSyncRequest(item.request);
            pendingRequestsQueue.remove();

            item.completionHandler.onSuccess(response);

            // TODO: Handle lastUpdatedOn

            return true;
        }
        catch (ExecutionException | InterruptedException e)
        {
            item.completionHandler.onFailure(new ConnectionManagerCompoundError(e, item.request));

            return false;
        }

    }
}
