package com.diegoalejogm.enhueco.Model.Other.ConnectionManager;

import com.diegoalejogm.enhueco.Model.Other.Either;
import org.json.JSONArray;
import org.json.JSONObject;

public interface ConnectionManagerCompletionHandler
{
    void onSuccess(Either<JSONObject, JSONArray> responseJSON);
    void onFailure(ConnectionManagerCompoundError error);
}
