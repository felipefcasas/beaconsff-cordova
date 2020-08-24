package cordova.plugin.beaconsff;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import java.util.ArrayList;
import java.util.Date;

import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class Beaconsff extends CordovaPlugin {

    private static Context context = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Beaconsff.context = this.cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        /**
            Inicia la busqueda de Beacons
        */
        if (action.equals("startMonitoring")) {
            this.startMonitoring(callbackContext);
            return true;
        }

        /**
            Detiene la busqueda de Beacons
        */
        if (action.equals("stopMonitoring")) {
            this.stopMonitoring(callbackContext);
            return true;
        }

        /**
            Genera e inicia un Beacon virtual a partir del dispositivo
        */
        if (action.equals("startAdvertisiment")) {
            String uuid = args.getJSONObject(0).getString("uuid");
            this.startAdvertisiment(uuid, callbackContext);
            return true;
        }

        /**
            Detiene el Beacon virtual
        */
        if (action.equals("stopAdvertisiment")) {
            this.stopAdvertisiment(callbackContext);
            return true;
        }

        /**
            Solicita los permisos necesarios para utilizar este plugin
        */
        if (action.equals("requestPermissions")) {
            this.requestPermissions(callbackContext);
            return true;
        }

        return false;
    }

    /**
        Inicia el servicio de monitoreo de Beacons
     */
    private void startMonitoring(CallbackContext callbackContext) throws JSONException {
        JSONObject result = new JSONObject();
        try {

            Intent intent = new Intent(Beaconsff.context, MonitoringService.class);
            Beaconsff.context.startService(intent);

            result.put("success", true);
            callbackContext.success(result);

        } catch (Exception ex) {
            result.put("success", false);
            result.put("error", ex.toString());
            callbackContext.error(result);
        }
    }

    /**
        Detiene el servicio de monitoreo de Beacons
     */
    private void stopMonitoring(CallbackContext callbackContext) throws JSONException {
        JSONObject result = new JSONObject();
        try {
            result.put("success", true);
            callbackContext.success(result);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("error", ex.toString());
            callbackContext.error(result);
        }
    }

    /**
        
     */
    private void startAdvertisiment(String uuid, CallbackContext callbackContext) throws JSONException {
        JSONObject result = new JSONObject();
        try {
            result.put("success", true);
            callbackContext.success(result);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("error", ex.toString());
            callbackContext.error(result);
        }
    }

    /**
        
     */
    private void stopAdvertisiment(CallbackContext callbackContext) throws JSONException {
        JSONObject result = new JSONObject();
        try {
            result.put("success", true);
            callbackContext.success(result);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("error", ex.toString());
            callbackContext.error(result);
        }
    }

    /**
        
     */
    private void requestPermissions(CallbackContext callbackContext) throws JSONException {
        JSONObject result = new JSONObject();
        try {
            result.put("success", true);
            callbackContext.success(result);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("error", ex.toString());
            callbackContext.error(result);
        }
    }
}
