package com.enhueco.model.logicManagers.genericManagers.connectionManager;

import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.enhueco.model.EHApplication;
import com.enhueco.model.model.EnHueco;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ConnectionManager
{
    static RequestQueue requestQueue = Volley.newRequestQueue(EHApplication.getAppContext());

    public static void sendAsyncRequest(final ConnectionManagerObjectRequest request, final ConnectionManagerCompletionHandler<JSONObject> completionHandler)
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

        JsonRequest jsonRequest = new JsonObjectRequest(method, request.URL, request.jsonStringParams.orNull(), new Response.Listener<JSONObject>()
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
                error.printStackTrace();
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
        int method;

        if (request.method.equals(HTTPMethod.GET))
        {
            method = Request.Method.GET;
        }
        else
        {
            method = Request.Method.POST;
        }

        JsonRequest jsonRequest = new JsonArrayRequest(method, request.URL, request.jsonStringParams.orNull(), new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
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
