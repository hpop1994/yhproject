package cn.yhsh.yhservecar.Core;

import android.content.Context;
import cn.yhsh.yhservecar.Core.entry.CarInfoList;
import cn.yhsh.yhservecar.Core.entry.OrderDetail;
import cn.yhsh.yhservecar.Core.entry.OrderList;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/5/13 013.
 */
public class NewAPI {
    public interface InfoReceiver<T>{
        void success(T t);
        void failed();
    }

    public static void getOrderList(Context context,Account account, final InfoReceiver<OrderList> receiver){
        APIs.getAllOrders(account, new NetworkCallback(context) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    receiver.success(OrderList.getOrderList(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                    receiver.failed();
                }
            }

            @Override
            protected void onFailed() {
                receiver.failed();
            }
        });
    }

    public static void getOrderDetail(Context context,int id,Account account, final InfoReceiver<OrderDetail> receiver){
        APIs.getOrderDetail(id, account, new NetworkCallback(context) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    receiver.success(OrderDetail.getOrderDetail(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                    receiver.failed();
                }
            }

            @Override
            protected void onFailed() {
                receiver.failed();
            }
        });
    }

    public static void getCarInforList(Context context,int id,Account account, final  InfoReceiver<CarInfoList> receiver){
        APIs.getUserCars(id, account, new NetworkCallback(context) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    receiver.success(CarInfoList.getCarInfoList(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                    receiver.failed();
                }
            }

            @Override
            protected void onFailed() {
                receiver.failed();
            }
        });
    }

}
