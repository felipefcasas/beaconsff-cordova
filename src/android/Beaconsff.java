package cordova.plugin.beaconsff;

import org.altbeacon.beacon.Beacon;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.util.UUID;

public class Beaconsff extends CordovaPlugin {

    private static Context CONTEXT = null;
    public static Intent MONITORING_INTENT = null;
    public static Intent ADVERTISEMENT_INTENT = null;

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Beaconsff.CONTEXT = this.cordova.getActivity().getBaseContext();
    }

    /**
     * Ejecuta un método a partir de la acción que entra por parámetro
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("startMonitoring")) {
            this.startMonitoring(callbackContext);
            return true;
        }

        if (action.equals("stopMonitoring")) {
            this.stopMonitoring(callbackContext);
            return true;
        }

        if (action.equals("startAdvertising")) {
            String uuid = args.getJSONObject(0).getString("uuid");
            this.startAdvertising(uuid, callbackContext);
            return true;
        }

        if (action.equals("stopAdvertising")) {
            this.stopAdvertising(callbackContext);
            return true;
        }

        if (action.equals("requestPermissions")) {
            this.requestPermissions(callbackContext);
            return true;
        }
        return false;
    }

    /**
     * Inicia el servicio de monitoreo de Beacons
     *
     * @param callbackContext
     * @throws JSONException
     */
    private void startMonitoring(CallbackContext callbackContext) throws JSONException {
        JSONObject result = new JSONObject();
        try {
            Beaconsff.MONITORING_INTENT = new Intent(Beaconsff.CONTEXT, MonitoringService.class);
            Beaconsff.CONTEXT.startService(Beaconsff.MONITORING_INTENT);

            result.put("success", true);
            callbackContext.success(result);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("error", ex.toString());
            callbackContext.error(result);
        }
    }

    /**
     * Detiene el servicio de monitoreo de Beacons
     *
     * @param callbackContext
     * @throws JSONException
     */
    private void stopMonitoring(CallbackContext callbackContext) throws JSONException {
        JSONObject result = new JSONObject();
        try {
            result.put("message", "No existe una instancia de Intent.");

            if (Beaconsff.MONITORING_INTENT != null) {
                Beaconsff.CONTEXT.stopService(Beaconsff.MONITORING_INTENT);
                Beaconsff.MONITORING_INTENT = null;
                result.put("message", "Servicio detenido.");
            }

            result.put("success", true);
            callbackContext.success(result);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("error", ex.toString());
            callbackContext.error(result);
        }
    }

    /**
     * Inicia el servicio de beacon virtual
     *
     * @param uuid
     * @param callbackContext
     * @throws JSONException
     */
    private void startAdvertising(String uuid, CallbackContext callbackContext) throws JSONException {
        JSONObject result = new JSONObject();
        try {
            if (isUUID(uuid)) {
                Beaconsff.ADVERTISEMENT_INTENT = new Intent(Beaconsff.CONTEXT, AdvertisementService.class);
                Beaconsff.ADVERTISEMENT_INTENT.putExtra("uuid", uuid);

                Beaconsff.CONTEXT.startService(Beaconsff.ADVERTISEMENT_INTENT);

                result.put("success", true);
                callbackContext.success(result);
            } else {
                result.put("success", false);
                result.put("error", "No se ingresó un uuid válido.");
                callbackContext.error(result);
            }
        } catch (Exception ex) {
            result.put("success", false);
            result.put("error", ex.toString());
            callbackContext.error(result);
        }
    }

    /**
     * Detiene el servicio de beacon virtual
     *
     * @param callbackContext
     * @throws JSONException
     */
    private void stopAdvertising(CallbackContext callbackContext) throws JSONException {
        JSONObject result = new JSONObject();
        try {

            result.put("message", "No existe una instancia de Intent.");

            if (Beaconsff.ADVERTISEMENT_INTENT != null) {
                Beaconsff.CONTEXT.stopService(Beaconsff.ADVERTISEMENT_INTENT);
                Beaconsff.ADVERTISEMENT_INTENT = null;
                result.put("message", "Servicio detenido.");
            }

            result.put("success", true);
            callbackContext.success(result);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("error", ex.toString());
            callbackContext.error(result);
        }
    }

    /**
     * @param callbackContext
     * @throws JSONException
     */
    private void requestPermissions(CallbackContext callbackContext) throws JSONException {
        JSONObject result = new JSONObject();
        try {

            Activity cordovaActivity = this.cordova.getActivity();


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (cordovaActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (cordovaActivity.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (cordovaActivity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Beaconsff.CONTEXT);
                            builder.setTitle("This app needs background location access");
                            builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @TargetApi(23)
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    cordovaActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_BACKGROUND_LOCATION);
                                }

                            });
                            builder.show();
                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(Beaconsff.CONTEXT);
                            builder.setTitle("Functionality limited");
                            builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                }

                            });
                            builder.show();
                        }

                    }
                } else {
                    if (cordovaActivity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        cordovaActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Beaconsff.CONTEXT);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }

                }
            }


            result.put("success", true);
            callbackContext.success(result);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("error", ex.toString());
            callbackContext.error(result);
        }
    }

    /**
     * Retorna un booleano que indica si el uuid que entra por parámetro es válido o no
     *
     * @param uuid
     * @return
     */
    private boolean isUUID(String uuid) {
        try {
            UUID aux = UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
