package com.diegoalejogm.enhueco.Model.Other;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.diegoalejogm.enhueco.Model.EHApplication;
import com.google.common.base.Optional;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by Diego on 10/11/15.
 */
public class ConnectionManager
{
    public enum HTTPMethod
    {
        GET, POST;
    }

    public static class ConnectionManagerCompoundError
    {
        public final Exception error;
        public final ConnectionManagerRequest request;

        public ConnectionManagerCompoundError(Exception error, ConnectionManagerRequest request)
        {
            this.error = error;
            this.request = request;
        }
    }

    public class ConnectionManagerRequest
    {
        public final String URL;
        public final HTTPMethod method;
        public final Optional<JSONObject> params;

        public ConnectionManagerRequest(String URL, HTTPMethod method, Optional<JSONObject> params)
        {
            this.URL = URL;
            this.method = method;
            this.params = params;
        }
    }

    public interface ConnectionManagerCompletionHandler
    {
        void onSuccess(JSONObject responseJSON);
        void onFailure(ConnectionManagerCompoundError error);
    }

    static RequestQueue requestQueue = Volley.newRequestQueue(EHApplication.getAppContext());

    public static void sendAsyncRequest (final ConnectionManagerRequest request, final ConnectionManagerCompletionHandler completionHandler)
    {
        int method;

        if (request.method.equals(HTTPMethod.GET))
        {
            method = Request.Method.GET;
        }
        else
        {
            method = Request.Method.POST;
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(method, request.URL, request.params.orNull(), new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                completionHandler.onSuccess(response);
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                completionHandler.onFailure(new ConnectionManagerCompoundError(error, request));
            }
        });

        requestQueue.add(jsonRequest);
    }

    public static JSONObject sendSyncRequest (ConnectionManagerRequest request) throws ExecutionException, InterruptedException
    {
        int method;

        if (request.method.equals(HTTPMethod.GET))
        {
            method = Request.Method.GET;
        }
        else
        {
            method = Request.Method.POST;
        }

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(method, request.URL, request.params.orNull(), future, future);

        requestQueue.add(jsonRequest);

        return future.get();
    }
}
