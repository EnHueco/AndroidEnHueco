package com.enhueco.model.logicManagers.genericManagers.connectionManager;

import com.google.common.base.Optional;
import org.json.JSONArray;

/** Request to retrieve a JSONArray as a response */
public class ConnectionManagerArrayRequest extends ConnectionManagerRequest<JSONArray>
{
    public final String URL;
    public final HTTPMethod method;
    public final Optional<String> jsonStringParams;

    /**
     * @param params JSON parameters to send with a POST request
     * @param params JSON parameters to send with request, can be either JSONObject or JSONArray
     */
    public ConnectionManagerArrayRequest(String URL, HTTPMethod method, Optional<String> params)
    {
        this.URL = URL;
        this.method = method;
        this.jsonStringParams = params;
    }
}
