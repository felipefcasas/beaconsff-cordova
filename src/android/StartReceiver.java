package cordova.plugin.beaconsff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class StartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {

            Intent it = new Intent(context, MonitoringService.class);
            context.startService(it);

                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(it);
                } else {
                    context.startService(it);
                }*/
        }
    }
}
