package com.enhueco.model.logicManagers.genericManagers.connectionManager;

import com.google.common.base.Optional;
import org.json.JSONObject;

/** Request to retrieve a JSONObject as a response */
public class ConnectionManagerObjectRequest extends ConnectionManagerRequest<JSONObject>
{
    public final String URL;
    public final HTTPMethod method;
    public final Optional<String> jsonStringParams;

    /**
     * A request to a service for which a JSONObject is expected
     * @param params JSON parameters to send with request, can be either JSONObject or JSONArray
     */
    public ConnectionManagerObjectRequest(String URL, HTTPMethod method, Optional<String> params)
    {
        this.URL = URL;
        this.method = method;
        this.jsonStringParams = params;
    }
}
