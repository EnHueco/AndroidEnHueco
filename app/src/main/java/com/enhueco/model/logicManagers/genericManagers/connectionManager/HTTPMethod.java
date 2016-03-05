package com.enhueco.model.logicManagers.genericManagers.connectionManager;

import com.android.volley.Request;

/**
 * Created by Diego on 10/11/15.
 */

public enum HTTPMethod
{
    GET(Request.Method.GET), POST(Request.Method.POST), PUT(Request.Method.PUT), PATCH(Request.Method.PATCH), HEAD(Request.Method.HEAD);

    /** Value that corresponds for Volley */
    public final int volleyValue;

    HTTPMethod(int s)
    {
        volleyValue = s;
    }
}
