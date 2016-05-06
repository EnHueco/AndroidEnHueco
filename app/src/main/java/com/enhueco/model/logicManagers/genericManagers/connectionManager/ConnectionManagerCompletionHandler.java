package com.enhueco.model.logicManagers.genericManagers.connectionManager;

import org.json.JSONException;

/**
 * Completion handler of a given type
 * @param <T> Type expected by the request, for example JSONArray or JSONObject
 */
public interface ConnectionManagerCompletionHandler<T>
{
    void onSuccess(T jsonResponse);
    void onFailure(ConnectionManagerCompoundError error);
}
