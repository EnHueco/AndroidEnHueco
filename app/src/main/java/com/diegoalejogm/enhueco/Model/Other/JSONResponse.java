package com.diegoalejogm.enhueco.model.other;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Diego on 10/18/15.
 */
public class JSONResponse
{
    public JSONObject jsonObject;
    public JSONArray jsonArray;

    public JSONResponse(JSONObject left, JSONArray right)
    {
        this.jsonObject = left;
        this.jsonArray = right;
    }
}
