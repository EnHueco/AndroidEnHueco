package com.diegoalejogm.enhueco.Model.Other.ConnectionManager;

import org.json.JSONObject;

public interface ConnectionManagerCompletionHandler
{
    void onSuccess(JSONObject responseJSON);
    void onFailure(ConnectionManagerCompoundError error);
}
