package com.diegoalejogm.enhueco.model.logicManagers.genericManagers.connectionManager;

import com.diegoalejogm.enhueco.model.other.JSONResponse;

public interface ConnectionManagerCompletionHandler
{
    void onSuccess(JSONResponse jsonResponse);
    void onFailure(ConnectionManagerCompoundError error);
}
