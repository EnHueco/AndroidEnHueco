package com.diegoalejogm.enhueco.model.managers.connectionManager;

import com.google.common.base.Optional;
import org.json.JSONObject;

public class ConnectionManagerRequest
{
    public final String URL;
    public final HTTPMethod method;
    public final Optional<JSONObject> params;
    public final boolean responseIsArray;

    /**
     * @param params JSON parameters to send with a POST request
     * @param responseIsArray True if the JSON response is expected to be a JSON array, false if expected to be a single JSON object.
     */
    public ConnectionManagerRequest(String URL, HTTPMethod method, Optional<JSONObject> params, boolean responseIsArray)
    {
        this.URL = URL;
        this.method = method;
        this.params = params;
        this.responseIsArray = responseIsArray;
    }
}
