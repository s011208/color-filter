
package com.yenhsun.colorfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ColorFilterReceivers extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        String action = arg1.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            context.startService(new Intent(context, ColorFilterPanelService.class));
            MainActivity.handleSimpleNotification(context);
        }
    }

}
