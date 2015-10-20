package com.diegoalejogm.enhueco.Model.Other.ConnectionManager;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.diegoalejogm.enhueco.Model.EHApplication;
import com.diegoalejogm.enhueco.Model.MainClasses.*;
import com.diegoalejogm.enhueco.Model.MainClasses.System;
import com.diegoalejogm.enhueco.Model.Other.Either;
import com.google.common.base.Optional;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ConnectionManager
{
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

        JsonRequest jsonRequest;

        if (request.responseIsArray)
        {
            jsonRequest = new JsonArrayRequest(method, request.URL, request.params.orNull(), new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response)
                {
                    completionHandler.onSuccess(new Either<JSONObject, JSONArray>(null, response));
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    completionHandler.onFailure(new ConnectionManagerCompoundError(error, request));
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map headers = new HashMap();
                    if(com.diegoalejogm.enhueco.Model.MainClasses.System.instance.getAppUser() != null)
                    {
                        headers.put("X-USER-ID", System.instance.getAppUser().getUsername());
                        headers.put("X-USER-TOKEN", System.instance.getAppUser().getToken());
                    }
                    return headers;
                }
            };
        }
        else
        {
            jsonRequest = new JsonObjectRequest(method, request.URL, request.params.orNull(), new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response)
                {
                    completionHandler.onSuccess(new Either<JSONObject, JSONArray>(response, null));
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    completionHandler.onFailure(new ConnectionManagerCompoundError(error, request));
                    error.printStackTrace();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map headers = new HashMap();
                    if(com.diegoalejogm.enhueco.Model.MainClasses.System.instance.getAppUser() != null)
                    {
                        headers.put("X-USER-ID", System.instance.getAppUser().getUsername());
                        headers.put("X-USER-TOKEN", System.instance.getAppUser().getToken());
                    }
                    return headers;
                }
            };
        }

        requestQueue.add(jsonRequest);
    }

    public static Either<JSONObject, JSONArray> sendSyncRequest (ConnectionManagerRequest request) throws ExecutionException, InterruptedException
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

        RequestFuture future = RequestFuture.newFuture();
        JsonRequest jsonRequest;

        if (request.responseIsArray)
        {
            jsonRequest = new JsonArrayRequest(method, request.URL, future, future);
        }
        else
        {
            jsonRequest = new JsonObjectRequest(method, request.URL, request.params.orNull(), future, future);
        }

        requestQueue.add(jsonRequest);

        Object response = future.get();

        return request.responseIsArray? new Either<JSONObject, JSONArray>(null, (JSONArray) response) : new Either<JSONObject, JSONArray>((JSONObject) response, null);
    }
}
