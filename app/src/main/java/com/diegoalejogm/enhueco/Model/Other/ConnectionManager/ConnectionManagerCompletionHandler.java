package com.diegoalejogm.enhueco.Model.Other.ConnectionManager;

import com.diegoalejogm.enhueco.Model.Other.Either;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

public interface ConnectionManagerCompletionHandler
{
    void onSuccess(Either<JSONObject, JSONArray> eitherJSONObjectOrJSONArray);
    void onFailure(ConnectionManagerCompoundError error);
}
