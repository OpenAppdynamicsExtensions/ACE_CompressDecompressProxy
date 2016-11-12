package com.appdynamics.ace.compDecompProxy.handler.health;

import org.json.simple.JSONObject;

/**
 * Created by stefan.marx on 12.11.16.
 */
public interface HealthDetailCallback {
    JSONObject getDetailJson();
}
