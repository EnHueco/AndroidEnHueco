package com.diegoalejogm.enhueco.model.managers.connectionManager;

import com.diegoalejogm.enhueco.model.other.JSONResponse;

public interface ConnectionManagerCompletionHandler
{
    void onSuccess(JSONResponse jsonResponse);
    void onFailure(ConnectionManagerCompoundError error);
}
