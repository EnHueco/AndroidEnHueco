package com.diegoalejogm.enhueco.Model.Managers.ConnectionManager;

import com.diegoalejogm.enhueco.Model.Other.JSONResponse;

public interface ConnectionManagerCompletionHandler
{
    void onSuccess(JSONResponse jsonResponse);
    void onFailure(ConnectionManagerCompoundError error);
}
