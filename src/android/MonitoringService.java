package cordova.plugin.beaconsff;

import android.app.Service;
import android.content.Intent;
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

public class MonitoringService extends Service  implements BeaconConsumer  {

    protected static final String TAG = "MonitoringService";
    private BeaconManager beaconManager;
    private Region region;
    private PowerManager.WakeLock wakeLock;
    private static boolean STARTED = false;

    public static final String iBeaconsLayout = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(MonitoringService.STARTED) {
            return Service.START_STICKY;
        }

        if(beaconManager != null) {
            beaconManager.unbind(this);
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);

        ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
        region = new Region("ALL_BEACONS", identifiers);

        beaconManager.setForegroundBetweenScanPeriod(5000l);

        beaconManager.setBackgroundBetweenScanPeriod(5000l);
        beaconManager.setBackgroundScanPeriod(5000l);

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(MonitoringService.iBeaconsLayout));

        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1100);

        /* Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Scanning for Beacons");
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }

        beaconManager.enableForegroundServiceScanning(builder.build(), 456);*/

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
                    Log.i(TAG, "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
                    Log.i(TAG, "BEACON_NAME "+beacons.iterator().next().getBluetoothName());
                    Log.i(TAG, "BEACON_UUID "+beacons.iterator().next().getServiceUuid());
                    Log.i(TAG, "BEACON_RSSI "+beacons.iterator().next().getRssi());
                    Log.i(TAG, "BEACON_MANUFACTURER "+beacons.iterator().next().getManufacturer());
                    Log.i(TAG, "BEACON_ID1 "+beacons.iterator().next().getId1().toString());
                }
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
    }
}
