package com.diegoalejogm.enhueco.Model.Managers.ConnectionManager;

import com.google.common.base.Optional;
import org.json.JSONArray;

public class ConnectionManagerArrayRequest
{
    public final String URL;
    public final HTTPMethod method;
    public final Optional<JSONArray> params;
    public final boolean responseIsArray;

    /**
     * @param params JSON parameters to send with a POST request
     * @param responseIsArray True if the JSON response is expected to be a JSON array, false if expected to be a single JSON object.
     */
    public ConnectionManagerArrayRequest(String URL, HTTPMethod method, Optional<JSONArray> params, boolean responseIsArray)
    {
        this.URL = URL;
        this.method = method;
        this.params = params;
        this.responseIsArray = responseIsArray;
    }
}