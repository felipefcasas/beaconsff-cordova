package cordova.plugin.beaconsff;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

import io.ionic.starter.MainActivity;
import io.ionic.starter.R;

public class MonitoringService extends Service implements BeaconConsumer {

    protected static final String TAG = "MonitoringService";
    private BeaconManager beaconManager;
    private Region region;
    private PowerManager.WakeLock wakeLock;
    public static boolean STARTED = false;

    public static final String iBeaconsLayout = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    private static String API_URL = null;
    private static String HEADERS = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Si el servicio de monitoreo está activo paramos la ejecución del método
        if (MonitoringService.STARTED) {
            return Service.START_STICKY;
        }

        if(intent == null) {
            return Service.START_STICKY;
        }

        String title = intent.getStringExtra("title");

        MonitoringService.API_URL = intent.getStringExtra("url");
        MonitoringService.HEADERS = intent.getStringExtra("headers");

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

                    for(Beacon beacon: beacons) {
                        this.sendRequest(beacon);
                    }

                    Log.i(TAG, "The first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away.");
                    Log.i(TAG, "BEACON_NAME " + beacons.iterator().next().getBluetoothName());
                    Log.i(TAG, "BEACON_UUID " + beacons.iterator().next().getServiceUuid());
                    Log.i(TAG, "BEACON_RSSI " + beacons.iterator().next().getRssi());
                    Log.i(TAG, "BEACON_MANUFACTURER " + beacons.iterator().next().getManufacturer());
                    Log.i(TAG, "BEACON_ID1 " + beacons.iterator().next().getId1().toString());
                }
            }

            private void sendRequest(Beacon beacon) {

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
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.unbind(this);
        stopForeground(true);
        MonitoringService.STARTED = false;
        MonitoringService.HEADERS = null;
        MonitoringService.API_URL = null;
    }
}
