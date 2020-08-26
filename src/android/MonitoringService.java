package cordova.plugin.beaconsff;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.ionic.starter.MainActivity;
import io.ionic.starter.R;

public class MonitoringService extends Service implements BeaconConsumer {

    protected static final String TAG = "MonitoringService";
    private BeaconManager beaconManager;
    private Region region;
    private PowerManager.WakeLock wakeLock;
    public static boolean STARTED = false;
    public static final String iBeaconsLayout = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static RequestQueue QUEUE = null;

    private String title = null;
    private String url = null;
    private String token = null;
    private String uuid = null;

    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Si el servicio de monitoreo está activo paramos la ejecución del método
        if (MonitoringService.STARTED) {
            return Service.START_STICKY;
        }

        /*for (Intent intentAux : POWERMANAGER_INTENTS) {
            if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                // show dialog to ask user action
                break;
            }
        }*/

        QUEUE = Volley.newRequestQueue(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        title = sharedPreferences.getString("title", null);
        url = sharedPreferences.getString("url", null);
        token = sharedPreferences.getString("token", null);
        uuid = sharedPreferences.getString("uuid", null);

        // Si el beaconManager fue inicializado, desenlazamos la instancia actual
        if (beaconManager != null) {
            beaconManager.unbind(this);
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);

        // Definimos una región con una lista vacía
        // con el fin de detectar todos los beacons que estén transmitiendo su señal
        ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
        region = new Region("ALL_BEACONS", identifiers);

        // El escaneo en foreground será cada 5 segundos
        beaconManager.setForegroundBetweenScanPeriod(5000l);

        // Agregamos los layouts para filtrar el escaneo
        // de momento usamos el layout de iBeacons
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(MonitoringService.iBeaconsLayout));

        // El escaneo en background será cada 1.1 segundos
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1100);

        // Creamos una notificación
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        Intent appIntent = new Intent(getApplicationContext(), MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle(title != null ? title : "Beaconsff");
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("BFF01",
                    "BFFN", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("BFFD");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }

        beaconManager.enableForegroundServiceScanning(builder.build(), 456);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Beaconsff::WL");
        wakeLock.acquire(60 * 1000);

        beaconManager.bind(this);

        MonitoringService.STARTED = true;

        // Al retornar START_STICKY nos aseguramos que el servicio se reiniciará si el sistema lo mata
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.removeAllMonitorNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    for (Beacon beacon : beacons) {
                        this.sendRequest(beacon);
                    }
                }
            }

            private boolean sendRequest(Beacon beacon) {

                if (url == null) {
                    return false;
                }

                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // response
                                Log.d("Response", response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO -> SAVE REQUEST TO RESEND
                                Log.d("Error.Response", error.toString());
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("finder_uuid", uuid);
                        params.put("discovered_uuid", beacon.getId1().toString());
                        params.put("distance", beacon.getDistance() + "");
                        params.put("rssi", beacon.getRssi() + "");
                        params.put("discovered_name", beacon.getBluetoothName() == null ? "" : beacon.getBluetoothName());

                        return params;
                    }
                };
                QUEUE.add(postRequest);
                return true;
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.d("SRBIR", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MonitoringService.STARTED = false;
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.unbind(this);
        url = null;
        token = null;
        title = null;
        stopForeground(true);
    }
}
