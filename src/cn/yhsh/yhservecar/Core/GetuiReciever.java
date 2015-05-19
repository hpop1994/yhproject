package cn.yhsh.yhservecar.Core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.igexin.sdk.PushConsts;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/1/22.
 */
public class GetuiReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent findService = new Intent(context, StatusService.class);
        StatusService.LocalBinder localBinder = (StatusService.LocalBinder) peekService(context, findService);
        if (localBinder==null){
            return;
        }
        StatusService myService = localBinder.getService();
        Account account =myService.getAccount();
        Bundle bundle = intent.getExtras();
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA:
                // 获取透传（payload）数据
                Log.i("POST_BACK","GETUI : got getui push");
                byte[] payload = bundle.getByteArray("payload");
                if (payload != null) {
                    if (!account.isLogined()) {
                        return;
                    }
                    myService.getAndNotifyOrders();
                }
                break;
             case PushConsts.GET_CLIENTID:
                 Log.i("POST_BACK","GETUI : Update clientid");
                 if (!account.isLogined()){
                     return;
                 }
                 String cid = bundle.getString("clientid");
                 APIs.updateGetuiID(cid, account, new NetworkCallback(context) {
                     @Override
                     protected void onSuccess(JSONObject data) {

                     }

                     @Override
                     public void dealAccountError() {

                     }

                     @Override
                     public void dealClientFormatError() {
                     }

                     @Override
                     public void dealServerFormatError() {
                     }

                     @Override
                     public void dealNetworkError() {
                     }

                     @Override
                     public void dealUnexpectedError() {
                     }
                 });
            default:
                break;
        }
    }
}
