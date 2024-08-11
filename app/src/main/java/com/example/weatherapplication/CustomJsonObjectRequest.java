package com.example.weatherapplication;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomJsonObjectRequest extends JsonObjectRequest {
    private Map<String, String> customHeaders;

    public CustomJsonObjectRequest(int method, String url, JSONObject jsonRequest,
                                   Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        customHeaders = new HashMap<>();
    }

    public void setCustomHeaders(Map<String, String> headers) {
        customHeaders.putAll(headers);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.putAll(super.getHeaders());
        headers.putAll(customHeaders);
        return headers;
    }
}
