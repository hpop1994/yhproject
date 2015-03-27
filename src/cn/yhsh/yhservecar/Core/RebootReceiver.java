package cn.yhsh.yhservecar.Core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Xujc on 2015/3/27 027.
 */
public class RebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("POST_BACK","REBOOT");
        context.startService(new Intent(context,StatusService.class));
    }
}
