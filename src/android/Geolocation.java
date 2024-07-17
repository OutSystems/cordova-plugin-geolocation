package org.apache.cordova.geolocation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.Manifest;
import android.location.Location;
import androidx.annotation.NonNull;

import android.util.SparseArray;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class Geolocation extends CordovaPlugin implements OnLocationResultEventListener {

    private SparseArray<LocationContext> locationContexts;
    private FusedLocationProviderClient fusedLocationClient;

    private static HashMap<Integer, CompletableFuture<Integer>> completableFutureMap = new HashMap<>();
    private static int requestCodeCounter = 100;

    public static final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        locationContexts = new SparseArray<LocationContext>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(cordova.getActivity());
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(!checkGooglePlayServicesAvailable(callbackContext)) {
            return false;
        }

        if ("getLocation".equals(action)) {
            int id = args.getString(3).hashCode();
            LocationContext lc = new LocationContext(id, LocationContext.Type.RETRIEVAL, args, callbackContext, this);
            locationContexts.put(id, lc);

            if (hasPermission()) {
                getLocation(lc);
            } else {
                PermissionHelper.requestPermissions(this, id, permissions);
            }

        } else if ("addWatch".equals(action)) {
            int id = args.getString(0).hashCode();
            LocationContext lc = new LocationContext(id, LocationContext.Type.UPDATE, args, callbackContext, this);
            locationContexts.put(id, lc);

            if (hasPermission()) {
                addWatch(lc);
            } else {
                PermissionHelper.requestPermissions(this, id, permissions);
            }

        } else if ("clearWatch".equals(action)) {
            clearWatch(args, callbackContext);

        } else {
            return false;
        }

        return true;
    }

    private boolean hasPermission() {
        for (String permission : permissions) {
            if (!PermissionHelper.hasPermission(this, permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        // In case a permission request is cancelled, the permissions and grantResults arrays are empty.
        // We must exit immediately to avoid calling getLocation erroneously.
        if(permissions == null || permissions.length == 0) {
            return;
        }

        LocationContext lc = locationContexts.get(requestCode);

        //if we are granted either ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION
        if (ArrayUtils.contains(grantResults, PackageManager.PERMISSION_GRANTED)) {
            if (lc != null) {
                switch (lc.getType()) {
                    case UPDATE:
                        addWatch(lc);
                        break;
                    default:
                        getLocation(lc);
                        break;
                }
            }
        } else {
            if(lc != null){
                PluginResult result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION, LocationError.LOCATION_PERMISSION_DENIED.toJSON());
                lc.getCallbackContext().sendPluginResult(result);
                locationContexts.delete(lc.getId());
            }
        }
    }

    private boolean checkGooglePlayServicesAvailable(CallbackContext callbackContext) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(cordova.getActivity());

        if(status != ConnectionResult.SUCCESS) {
            PluginResult result;

            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(cordova.getActivity(), status, 1).show();
                result = new PluginResult(PluginResult.Status.ERROR, LocationError.GOOGLE_SERVICES_ERROR_RESOLVABLE.toJSON());
            }
            else {
                result = new PluginResult(PluginResult.Status.ERROR, LocationError.GOOGLE_SERVICES_ERROR.toJSON());
            }

            callbackContext.sendPluginResult(result);
            return false;
        }

        return true;
    }

    private void getLocation(LocationContext locationContext) {
        JSONArray args = locationContext.getExecuteArgs();
        long timeout = args.optLong(2);
        boolean enableHighAccuracy = args.optBoolean(0, false);
        LocationRequest request = LocationRequest.create();

        request.setNumUpdates(1);

        // This is necessary to be able to get a response when location services are initially off and then turned on before this request.
        request.setInterval(0);

        if(enableHighAccuracy) {
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        if(timeout != 0) {
            request.setExpirationDuration(timeout);
        }

        requestLocationUpdatesIfSettingsSatisfied(locationContext, request);
    }

    private void addWatch(LocationContext locationContext) {
        JSONArray args = locationContext.getExecuteArgs();
        boolean enableHighAccuracy = args.optBoolean(1, false);
        long maximumAge = args.optLong(2, 5000);

        LocationRequest request = LocationRequest.create();

        request.setInterval(maximumAge);

        if(enableHighAccuracy) {
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        requestLocationUpdatesIfSettingsSatisfied(locationContext, request);
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates(LocationContext locationContext, LocationRequest request) {
        fusedLocationClient.requestLocationUpdates(request, locationContext.getLocationCallback(), null);
    }

    private void clearWatch(JSONArray args, CallbackContext callbackContext) {
        String id = args.optString(0);

        if(id != null) {
            int requestId = id.hashCode();
            LocationContext lc = locationContexts.get(requestId);

            if(lc == null) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, LocationError.WATCH_ID_NOT_FOUND.toJSON());
                callbackContext.sendPluginResult(result);
            }
            else {
                this.locationContexts.delete(requestId);
                fusedLocationClient.removeLocationUpdates(lc.getLocationCallback());

                PluginResult result = new PluginResult(PluginResult.Status.OK);
                callbackContext.sendPluginResult(result);
            }
        }
    }

    @Override
    public void onLocationResultSuccess(LocationContext locationContext, LocationResult locationResult) {
        for (Location location : locationResult.getLocations()) {
            try {
                JSONObject locationObject = LocationUtils.locationToJSON(location);
                PluginResult result = new PluginResult(PluginResult.Status.OK, locationObject);

                if (locationContext.getType() == LocationContext.Type.UPDATE) {
                    result.setKeepCallback(true);
                }
                else {
                    locationContexts.delete(locationContext.getId());
                }

                locationContext.getCallbackContext().sendPluginResult(result);

            } catch (JSONException e) {
                PluginResult result = new PluginResult(PluginResult.Status.JSON_EXCEPTION, LocationError.SERIALIZATION_ERROR.toJSON());

                if (locationContext.getType() == LocationContext.Type.UPDATE) {
                    result.setKeepCallback(true);
                }
                else {
                    locationContexts.delete(locationContext.getId());
                }

                locationContext.getCallbackContext().sendPluginResult(result);
            }
        }
    }

    @Override
    public void onLocationResultError(LocationContext locationContext, LocationError error) {
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, error.toJSON());

        if (locationContext.getType() == LocationContext.Type.UPDATE) {
            result.setKeepCallback(true);
        }
        else {
            locationContexts.delete(locationContext.getId());
        }

        locationContext.getCallbackContext().sendPluginResult(result);
    }

    private void requestLocationUpdatesIfSettingsSatisfied(final LocationContext locationContext, final LocationRequest request) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(request);
        SettingsClient client = LocationServices.getSettingsClient(cordova.getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        OnSuccessListener<LocationSettingsResponse> checkLocationSettingsOnSuccess = new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize location requests here.
                requestLocationUpdates(locationContext, request);
            }
        };

        OnFailureListener checkLocationSettingsOnFailure = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                PluginResult result;
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // to get the response for the resolution in onActivityResult
                        cordova.setActivityResultCallback(Geolocation.this);

                        // use CompletableFuture to get callback from calling startResolutionForResult
                        int requestCode = requestCodeCounter++;
                        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
                        completableFutureMap.put(requestCode, completableFuture);

                        completableFuture.thenAccept(resolvableResult -> {
                            if (resolvableResult == Activity.RESULT_OK) {
                                requestLocationUpdates(locationContext, request);
                            } else {
                                PluginResult errorResult = new PluginResult(PluginResult.Status.ERROR, LocationError.LOCATION_ENABLE_REQUEST_DENIED.toJSON());
                                locationContext.getCallbackContext().sendPluginResult(errorResult);
                                locationContexts.delete(locationContext.getId());
                            }
                        });

                        // Show the dialog to enable location by calling startResolutionForResult(),
                        // and then handle the result in onActivityResult
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(cordova.getActivity(), requestCode);

                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
                else {
                    result = new PluginResult(PluginResult.Status.ERROR, LocationError.LOCATION_SETTINGS_ERROR.toJSON());
                    locationContext.getCallbackContext().sendPluginResult(result);
                    locationContexts.remove(locationContext.getId());
                }
            }
        };

        task.addOnSuccessListener(checkLocationSettingsOnSuccess);
        task.addOnFailureListener(checkLocationSettingsOnFailure);
    }

    /**
     * Used to handle the result of 'ResolvableApiException.startResolutionForResult'
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        CompletableFuture<Integer> completableFuture = completableFutureMap.remove(requestCode);
        if (completableFuture != null) {
            completableFuture.complete(resultCode);
        }
    }

}
