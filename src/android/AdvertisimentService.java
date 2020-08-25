package cordova.plugin.beaconsff;

import android.app.Service;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.Arrays;

public class AdvertisementService extends Service {

    public static boolean STARTED = false;
    protected static final String TAG = "AdvertisementService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(AdvertisementService.STARTED) {
            return Service.START_STICKY;
        }

        String uuid = intent.getStringExtra("uuid");

        Beacon beacon = new Beacon.Builder()
                .setId1(uuid)
                .setId2("14")
                .setId3("13")
                .setManufacturer(0x0118) // Radius Networks.  Change this for other beacon layouts
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[] {0l})) // Remove this for beacon layouts without d: fields
                .build();

                BeaconParser beaconParser = new BeaconParser().setBeaconLayout(MonitoringService.iBeaconsLayout);

                BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
                beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

                    @Override
                    public void onStartFailure(int errorCode) {
                        Log.e(TAG, "Advertisement start failed with code: "+errorCode);
                    }

                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        Log.i(TAG, "Advertisement start succeeded.");
                    }
                });

        AdvertisementService.STARTED = true;

        // Al retornar START_STICKY nos aseguramos que el servicio se reiniciará si el sistema lo mata
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "DESTROYING 1");
        super.onDestroy();
        AdvertisementService.STARTED = false;
        Log.d(TAG, "DESTROYING 2");
    }
}
