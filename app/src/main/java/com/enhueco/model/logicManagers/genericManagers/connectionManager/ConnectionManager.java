package com.enhueco.model.logicManagers.genericManagers.connectionManager;

import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.enhueco.model.EHApplication;
import com.enhueco.model.model.EnHueco;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ConnectionManager
{
    static RequestQueue requestQueue = Volley.newRequestQueue(EHApplication.getAppContext());

    public static void sendAsyncRequest(final ConnectionManagerObjectRequest request, final ConnectionManagerCompletionHandler<JSONObject> completionHandler)
    {
        Log.v("CONNECTION MANAGER", "SENDING REQUEST: " + request.toString());
        JsonRequest jsonRequest = new JsonObjectRequest(request.method.volleyValue, request.URL, request.jsonStringParams.orNull(), new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    Log.v("CONNECTION MANAGER", "RECEIVED RESPONSE: " + response.toString());
                    completionHandler.onSuccess(response);
                }
                catch (Exception e)
                {
                    completionHandler.onFailure(new ConnectionManagerCompoundError(e, request));
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                completionHandler.onFailure(new ConnectionManagerCompoundError(error, request));
                error.printStackTrace();

                try
                {
                    if (error.networkResponse != null)
                    {
                        String responseBody = new String(error.networkResponse.data, "utf-8" );
                        Log.e("CONNECTION MANAGER", responseBody);
                    }
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map headers = new HashMap();
                if (EnHueco.getInstance().getAppUser() != null)
                {
                    headers.put("X-USER-ID", EnHueco.getInstance().getAppUser().getUsername());
                    headers.put("X-USER-TOKEN", EnHueco.getInstance().getAppUser().getToken());
                }
                return headers;
            }
        };

        requestQueue.add(jsonRequest);
    }

    public static void sendAsyncRequest(final ConnectionManagerArrayRequest request, final ConnectionManagerCompletionHandler<JSONArray> completionHandler)
    {
        Log.v("CONNECTION MANAGER", "SENDING REQUEST: " + request.toString());
        JsonRequest jsonRequest = new JsonArrayRequest(request.method.volleyValue, request.URL, request.jsonStringParams.orNull(), new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                try
                {
                    Log.v("CONNECTION MANAGER", "RECEIVED RESPONSE: " + response.toString());
                    completionHandler.onSuccess(response);
                }
                catch (Exception e)
                {
                    completionHandler.onFailure(new ConnectionManagerCompoundError(e, request));
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                completionHandler.onFailure(new ConnectionManagerCompoundError(error, request));
                error.printStackTrace();
                try
                {
                    if(error.networkResponse != null)
                    {
                        error.printStackTrace();
                        String responseBody = new String(error.networkResponse.data, "utf-8" );
                        Log.e("CONNECTION MANAGER", responseBody);
                    }
                }
                catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map headers = new HashMap();
                if (EnHueco.getInstance().getAppUser() != null)
                {
                    headers.put("X-USER-ID", EnHueco.getInstance().getAppUser().getUsername());
                    headers.put("X-USER-TOKEN", EnHueco.getInstance().getAppUser().getToken());
                }
                return headers;
            }
        };

        requestQueue.add(jsonRequest);
    }

//    public static JSONResponse sendSyncRequest(ConnectionManagerObjectRequest request) throws ExecutionException, InterruptedException
//    {
//        int method;
//
//        if (request.method.equals(HTTPMethod.GET))
//        {
//            method = Request.Method.GET;
//        }
//        else
//        {
//            method = Request.Method.POST;
//        }
//
//        RequestFuture future = RequestFuture.newFuture();
//        JsonRequest jsonRequest;
//
//        if (request.responseIsArray)
//        {
//            jsonRequest = new JsonArrayRequest(method, request.URL, future, future);
//        }
//        else
//        {
//            jsonRequest = new JsonObjectRequest(method, request.URL, request.params.orNull(), future, future);
//        }
//
//        requestQueue.add(jsonRequest);
//
//        Object response = future.get();
//
//        return request.responseIsArray ? new JSONResponse(null, (JSONArray) response) : new JSONResponse((JSONObject) response, null);
//    }

//    public static JSONResponse sendSyncRequest(ConnectionManagerArrayRequest request) throws ExecutionException, InterruptedException
//    {
//        int method;
//
//        if (request.method.equals(HTTPMethod.GET)) method = Request.Method.GET;
//        else method = Request.Method.POST;
//
//        RequestFuture future = RequestFuture.newFuture();
//        JsonRequest jsonRequest;
//
//        if (request.responseIsArray)
//        {
//            jsonRequest = new JsonArrayRequest(method, request.URL, future, future)
//            {
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError
//                {
//                    Map headers = new HashMap();
//                    if (EnHueco.getInstance().getAppUser() != null)
//                    {
//                        headers.put("X-USER-ID", EnHueco.getInstance().getAppUser().getUsername());
//                        headers.put("X-USER-TOKEN", EnHueco.getInstance().getAppUser().getToken());
//                    }
//                    return headers;
//                }
//            };
//        }
//        else
//        {
//            jsonRequest = new JsonArrayRequest(method, request.URL, request.params.orNull(), future, future)
//            {
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError
//                {
//                    Map headers = new HashMap();
//                    if (EnHueco.getInstance().getAppUser() != null)
//                    {
//                        headers.put("X-USER-ID", EnHueco.getInstance().getAppUser().getUsername());
//                        headers.put("X-USER-TOKEN", EnHueco.getInstance().getAppUser().getToken());
//                    }
//                    return headers;
//                }
//            };
//        }
//
//        requestQueue.add(jsonRequest);
//
//        Object response = future.get();
//
//        return request.responseIsArray ? new JSONResponse(null, (JSONArray) response) : new JSONResponse((JSONObject) response, null);
//    }
}
