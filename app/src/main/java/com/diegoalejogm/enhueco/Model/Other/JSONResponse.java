package com.diegoalejogm.enhueco.model.other;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Diego on 10/18/15.
 * This class is a wrapper for a JSON Response received, usually only one of it's attributes will contain a value and
 * the other one will be null. So it is used at the developers desire.
 */
public class JSONResponse
{
    /**
     * JSON object of the JSON Response
     */
    public JSONObject jsonObject;
    /**
     * JSON array of the JSON Response
     */
    public JSONArray jsonArray;

    /**
     * Creates a ew JSONResponse object
     * @param left JSONObject
     * @param right JSONArray
     */
    public JSONResponse(JSONObject left, JSONArray right)
    {
        this.jsonObject = left;
        this.jsonArray = right;
    }
}
