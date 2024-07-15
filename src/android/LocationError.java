package org.apache.cordova.geolocation;

import org.json.JSONException;
import org.json.JSONObject;

public enum LocationError {
    LOCATION_PERMISSION_DENIED (100, "Location permission request denied"),
    GOOGLE_SERVICES_ERROR_RESOLVABLE (101, "Google Play Services error user resolvable"),
    GOOGLE_SERVICES_ERROR (102, "Google Play Services error"),
    SERIALIZATION_ERROR (103, "Location result serialization error"),
    WATCH_ID_NOT_FOUND (104, "Watch id not found"),
    LOCATION_SETTINGS_ERROR_RESOLVABLE (105, "Current location settings can not satisfy this request"),
    LOCATION_SETTINGS_ERROR (106, "Location settings error"),
    LOCATION_NULL (107, "Could not retrieve location"),
    LOCATION_ENABLE_REQUEST_DENIED (108, "Request to enable location denied");

    private final int code;
    private final String message;

    LocationError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("code", this.code);
            obj.put("message", this.message);
        }
        catch(JSONException e) {
            return obj;
        }

        return obj;
    }
}