package com.diegoalejogm.enhueco.Model.Other.ConnectionManager;

import com.google.common.base.Optional;
import org.json.JSONObject;

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
