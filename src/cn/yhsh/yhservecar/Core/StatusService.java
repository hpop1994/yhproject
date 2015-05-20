package cn.yhsh.yhservecar.Core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.*;
import android.os.Process;
import android.util.Log;
import cn.yhsh.yhservecar.R;
import cn.yhsh.yhservecar.UI.activity.MainActivity;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.igexin.sdk.PushManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Xujc on 2015/1/19.
 */
public class StatusService extends Service implements AMapLocationListener {
    public static final int OFF_SERVICE = 0;
    public static final int WAITING = 1;

    private boolean sending = false;
    private boolean locating = false;

    private boolean disconnected = false;//访问需要上service锁
    private int failedTime = 0;

    private int status = 0;
    private double lat = 0;
    private double lon = 0;

    private final ArrayList<Order> ordersAsked = new ArrayList<Order>();//order time
    private final Handler countdownHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COUNTDOWN) {
                for (int i = ordersAsked.size() - 1; i >= 0; i--) {
                    final int index=i;
                    Order o = ordersAsked.get(i);
                    o.inTime++;
                    if (o.inTime == 120 || o.inTime==130 || o.inTime == 140) {
                        APIs.replyOrderRequest(o.orderID, "超时",false, account, new NetworkCallback(StatusService.this) {
                            @Override
                            protected void onSuccess(JSONObject data) {
                                getAndNotifyOrders();
                            }
                        });
                        notifyOrderListChanged();
                        getAndNotifyOrders();
                    }
                    if (o.inTime>140){
                        ordersAsked.remove(i);
                    }
                }
                notifyOrderListChanged();
                if (!ordersAsked.isEmpty()) {
                    sendMessageDelayed(obtainMessage(COUNTDOWN), 1000);
                }
            }
        }
    };


    //    private static final int START_WAITING = 2;
    private Looper mServiceLooper;
    private WorkerHandler mWorkerHandler;

    private static final int SEND_LOCATION = 3;
    private StatusHandler mStatusHandler;
    private static final int RECONNECTED = 0;
    private static final int LOST_CONNECTION = 1;
    private static final int COUNTDOWN = 2;
    private static final int NEW_ORDER = 3;


    //    private static final int STOP_WAITING =3;
    private LocationManagerProxy mLocationManagerProxy;
    private Account account;
    private StatusListener statusListener = null;
    private PushManager pushManager;
    private HandlerThread mThread;

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        lat = aMapLocation.getLatitude();
        lon = aMapLocation.getLongitude();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private final class WorkerHandler extends Handler {
        public static final int ONETIME = 0;
        public static final int LOOP = 1;
        private NetworkCallback callback;

        public WorkerHandler(Looper looper) {
            super(looper);
            callback = new NetworkCallback(StatusService.this) {
                @Override
                protected void onStatusCode(int code, JSONObject object) {
                    if (code == 9) {
                        mStatusHandler.sendMessage(mStatusHandler.obtainMessage(NEW_ORDER));
                        onSuccess(object);
                    }
                }

                @Override
                protected void onSuccess(JSONObject data) {
                    if (disconnected) {
                        reConnected();
                    }
                }

                @Override
                protected void onFailed() {
                    failed();
                }

                @Override
                public void dealAccountError() {
                    onFailed();
                }

                @Override
                public void dealClientFormatError() {
                    onFailed();
                }

                @Override
                public void dealServerFormatError() {
                    onFailed();
                }

                @Override
                public void dealNetworkError() {
                    onFailed();
                }

                @Override
                public void dealUnexpectedError() {
                    onFailed();
                }
            };
        }

        @Override
        public void handleMessage(Message msg) {

            APIs.sendLocation(lat, lon, account, callback);

            if (msg.arg1 == LOOP) {
                Message msg1 = obtainMessage(SEND_LOCATION);
                msg1.arg1 = LOOP;
                sendMessageDelayed(msg1, 10000);
            }
        }

        private void failed() {
            if (disconnected) {
                return;
            }
            failedTime++;
            if (failedTime == 2) {
                lostConnection();
            }
        }

        private void reConnected() {
            synchronized (StatusService.this) {
                disconnected = false;
                failedTime = 0;
                Message message = obtainMessage();
                message.what = RECONNECTED;
                mStatusHandler.sendMessage(message);
            }
            if (statusListener != null) {
                statusListener.connectionChanged();
            }
        }

        private void lostConnection() {
            synchronized (StatusService.this) {
                disconnected = true;
                Message message = obtainMessage();
                message.what = LOST_CONNECTION;
                mStatusHandler.sendMessage(message);
            }
            if (statusListener != null) {
                statusListener.connectionChanged();
            }
        }
    }

    private class StatusHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOST_CONNECTION:
                    stopLocating();
                    break;
                case RECONNECTED:
                    startLocating();
                    break;
                case NEW_ORDER:
                    getAndNotifyOrders();
                    break;
            }
        }
    }

    private void startSending() {
        sending = true;
        synchronized (this) {
            disconnected = false;
        }
        failedTime = 0;
        Message message = new Message();
        message.what = SEND_LOCATION;
        message.arg1 = WorkerHandler.LOOP;
        mWorkerHandler.sendMessage(message);
    }

    private void stopSending() {
        sending = false;
        mWorkerHandler.removeMessages(SEND_LOCATION);
    }


    public interface StatusListener {
        public void connectionChanged();

        public void orderListChanged();

        public void statusChanged();
    }

    public void setListener(StatusListener listener) {
        statusListener = listener;
    }

    private void startLocating() {
        locating = true;
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, 10000, 15, this);
    }

    private void stopLocating() {
        locating = false;
        mLocationManagerProxy.removeUpdates(this);
    }

    private void becomeWaiting() {
        status = WAITING;
        if (locating == false) {
            startLocating();
        }
        if (sending == false) {
            startSending();
        }
        notifyStatusListener();
        startGetuiPush();
    }

    private void notifyStatusListener() {
        if (statusListener != null) {
            Log.i("POST_BACK", "USING Local : notifyStatusListener()");
            statusListener.statusChanged();
        }
    }

    private void becomeOffService() {
        Log.i("POST_BACK", "USING Local : becomeOffService()");
        if (locating == true) {
            stopLocating();
        }
        if (sending == true) {
            stopSending();
        }
        status = OFF_SERVICE;
        notifyStatusListener();

    }

    public void getAndNotifyOrders() {
        Log.i("POST_BACK", "USING Local : getAndNotifyOrders()");
        APIs.getOrderForMe(account, new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                boolean startEmpty = ordersAsked.isEmpty();
                try {
                    JSONArray array = data.getJSONArray("orders");
                    ArrayList<Order> tmpArray = new ArrayList<Order>(array.length());
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        Order order = new Order();
                        order.orderID = object.getInt("id");
                        order.uid = object.getInt("uid");
                        order.iid = object.getString("iid");
                        order.name = object.getString("realname");
                        order.phone = object.getString("phonenum");
                        order.address = object.getString("address");
//                        order.time = object.getString("ordertime");
                        order.appointmentTime = object.getString("time");
                        order.lat = object.getDouble("lat");
                        order.lon = object.getDouble("lon");
                        order.item = object.getString("item");
                        tmpArray.add(order);
                    }
                    ordersAsked.retainAll(tmpArray);
                    tmpArray.removeAll(ordersAsked);
                    ordersAsked.addAll(tmpArray);
                    if (ordersAsked.isEmpty() && !startEmpty) {
                        countdownHandler.removeMessages(1);
                    } else if (!ordersAsked.isEmpty() && startEmpty) {
                        Message message = new Message();
                        message.what = COUNTDOWN;
                        countdownHandler.sendMessageDelayed(message, 1000);
                    }
                    if (!tmpArray.isEmpty()) {
                        //有新单
                        Intent ongoing = new Intent(StatusService.this, MainActivity.class);
                        ongoing.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(ongoing);

                        Intent pandent= new Intent(StatusService.this, MainActivity.class);
                        pandent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent intent=PendingIntent.getActivity(StatusService.this,
                                0, pandent, 0);

                        Notification.Builder mBuilder =
                                new Notification.Builder(StatusService.this)
                                        .setSmallIcon(R.drawable.app_logo)
                                        .setContentTitle("有新订单正在等待")
                                        .setAutoCancel(true)
                                        .setContentIntent(intent)
                                        .setDefaults(Notification.DEFAULT_ALL);

                        NotificationManager mNotificationManager =
                                (NotificationManager) StatusService.this.getSystemService(Context.NOTIFICATION_SERVICE);

                        // id allows you to update the notification later on.
                        mNotificationManager.notify(0, mBuilder.getNotification());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dealServerFormatError();
                }
                notifyOrderListChanged();
            }

            @Override
            protected void onFailed() {
                ordersAsked.clear();
                countdownHandler.removeMessages(COUNTDOWN);
                notifyOrderListChanged();
            }

            @Override
            public void dealAccountError() {
                onFailed();
            }

            @Override
            public void dealClientFormatError() {
                onFailed();
            }

            @Override
            public void dealServerFormatError() {
                onFailed();
            }

            @Override
            public void dealNetworkError() {
                onFailed();
            }

            @Override
            public void dealUnexpectedError() {
                onFailed();
            }
        });


    }

    private void notifyOrderListChanged() {
        if (statusListener != null) {
            Log.i("POST_BACK", "USING Local : notifyOrderListChanged()");
            statusListener.orderListChanged();
        }
    }

    private void getNewestStatus() {
        Log.i("POST_BACK", "USING Local : getNewestStatus()");
        APIs.getStatus(Account.getInstance(this), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    int newStatus = data.getInt("status");
                    if (newStatus == WAITING) {
                        becomeWaiting();
                    } else if (newStatus == OFF_SERVICE) {
                        becomeOffService();
                    }
                } catch (JSONException e) {
                    dealServerFormatError();
                    e.printStackTrace();
                    notifyStatusListener();
                }
            }

            @Override
            protected void onFailed() {
                notifyStatusListener();
//                makeText("获取状态失败");
                super.onFailed();
            }

            @Override
            public void dealAccountError() {
                onFailed();
            }

            @Override
            public void dealClientFormatError() {
                onFailed();

            }

            @Override
            public void dealServerFormatError() {
                onFailed();
            }

            @Override
            public void dealNetworkError() {
                onFailed();
            }

            @Override
            public void dealUnexpectedError() {
                onFailed();
            }
        });
    }

    public interface BackgroundJobListener {

        public void jobSuccess();

        public void jobFailed();
    }

    public void getNewestStatus(final BackgroundJobListener backgroundJobListener) {
        Log.i("POST_BACK", "USING Local : getNewestStatus(backgroundjob)");
        APIs.getStatus(Account.getInstance(this), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    int newStatus = data.getInt("status");
                    if (newStatus == WAITING) {
                        becomeWaiting();
                    } else if (newStatus == OFF_SERVICE) {
                        becomeOffService();
                    }
                    backgroundJobListener.jobSuccess();
                } catch (JSONException e) {
                    dealServerFormatError();
                    e.printStackTrace();
                    backgroundJobListener.jobFailed();
                }
            }

            @Override
            protected void onFailed() {
                backgroundJobListener.jobFailed();
//                makeText("获取状态失败");
                super.onFailed();
            }
        });
    }

    public void logout() {
        Log.i("POST_BACK", "USING Local : logout()");
        account.logout();
        if (locating == true) {
            stopLocating();
        }
        if (sending == true) {
            stopSending();
        }
        stopGetuiPsh();
    }

    public void login(final String name, final String password, final BackgroundJobListener listener) {
        Log.i("POST_BACK", "USING Local : login()");
        APIs.login(name, password, new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    account.login(data.getString("id"), name, password);
                    APIs.updateGetuiID(pushManager.getClientid(StatusService.this), account, new NetworkCallback(StatusService.this) {
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
                    getNewestStatus();
                    listener.jobSuccess();
                    startGetuiPush();
                } catch (JSONException e) {
                    dealServerFormatError();
                }
            }

            @Override
            protected void onFailed() {
                listener.jobFailed();
            }
        });
    }

    private void startGetuiPush() {
        pushManager.turnOnPush(this.getApplicationContext());
    }

    private void stopGetuiPsh() {
        pushManager.stopService(this);
    }

    public void goIntoWait(final BackgroundJobListener listener) {
        Log.i("POST_BACK", "USING Local : goIntoWait()");
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 5, new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                double lat = aMapLocation.getLatitude();
                double lon = aMapLocation.getLongitude();
                APIs.sendLocation(lat, lon, account, new NetworkCallback(StatusService.this) {
                    @Override
                    protected void onStatusCode(int code, JSONObject object) {
                        if (code==ReplyStatus.NEW_ORDER){
                            listener.jobSuccess();
                            mStatusHandler.sendMessage(mStatusHandler.obtainMessage(NEW_ORDER));
                        }
                    }

                    @Override
                    protected void onSuccess(JSONObject data) {
                        becomeWaiting();
                        listener.jobSuccess();
                        getAndNotifyOrders();
                    }

                    @Override
                    protected void onFailed() {
                        listener.jobFailed();
                    }
                });
            }

            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }

    public void goIntoOffService(final BackgroundJobListener listener) {
        Log.i("POST_BACK", "USING Local : goIntoOffService()");
        becomeOffService();
        APIs.becomeOffline(account, new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {

            }
        });
        listener.jobSuccess();
    }

//    public void answerOrder(final int orderID, final boolean taken, final BackgroundJobListener listener) {
//        Log.i("POST_BACK", "USING Local : answerOrder()");
//        final Order order;
//        Order tmp = null;
//        for (Order o : ordersAsked) {
//            if (o.orderID == orderID) {
//                tmp = o;
//                break;
//            }
//        }
//        if (tmp == null) {
//            MyToast.makeText(this, "此单已过期", Toast.LENGTH_SHORT).show();
//            listener.jobFailed();
//            return;
//        }
//        APIs.replyOrderRequest(orderID, taken, account, new NetworkCallback(this) {
//            @Override
//            protected void onSuccess(JSONObject data) {
//                listener.jobSuccess();
//            }
//
//            @Override
//            protected void onFailed() {
//                listener.jobFailed();
//            }
//        });
//    }

//    public void finishOrder(int orderID, int carID, JSONObject goods, double price, final BackgroundJobListener listener) {
//        Log.i("POST_BACK", "USING Local : finishOrder()");
//        APIs.finishOrder(orderID, carID, goods, price, account, new NetworkCallback(this) {
//            @Override
//            protected void onSuccess(JSONObject data) {
//                listener.jobSuccess();
//                getAndNotifyOrders();
//            }
//
//            @Override
//            protected void onFailed() {
//                listener.jobFailed();
//            }
//        });
//    }



    @Override
    public void onCreate() {
        mThread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        mThread.start();

        mServiceLooper = mThread.getLooper();
        mWorkerHandler = new WorkerHandler(mServiceLooper);
        mStatusHandler = new StatusHandler();

        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.setGpsEnable(true);

        account = Account.getInstance(this);

        pushManager = PushManager.getInstance();
        pushManager.initialize(this);
        pushManager.stopService(this);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (account.isLogined()) {
            getNewestStatus(new BackgroundJobListener() {
                @Override
                public void jobSuccess() {

                }

                @Override
                public void jobFailed() {

                }
            });
            startGetuiPush();
            APIs.updateGetuiID(pushManager.getClientid(StatusService.this), account, new NetworkCallback(this) {
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
        }

        return START_STICKY;
    }


//    public void saveServeStatus() {
//        Log.i("POST_BACK", "USING Local : saveServeStatus()");
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = sp.edit();
//        if (status != SERVING) {
//            editor.putBoolean("serving", false);
//            editor.apply();
//            return;
//        } else {
//            editor.putBoolean("serving", true)
//                    .putInt("id", servingOrder.orderID)
//                    .putInt("uid", servingOrder.uid)
//                    .putString("name", servingOrder.name)
//                    .putString("phone", servingOrder.phone)
//                    .putString("address", servingOrder.address)
//                    .putString("time", servingOrder.time)
//                    .putFloat("lat", (float) servingOrder.lat)
//                    .putFloat("lon", (float) servingOrder.lon)
//                    .putString("item", servingOrder.item)
//                    .apply();
//
//        }
//
//    }
//
//    private void retrieveStatus() {
//        Log.i("POST_BACK", "USING Local : retrieveStatus()");
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        if (sp.getBoolean("serving", false)) {
//            Order order = new Order();
//            order.orderID = sp.getInt("id", 0);
//            order.uid = sp.getInt("uid", 0);
//            order.name = sp.getString("name", "");
//            order.phone = sp.getString("phone", "");
//            order.address = sp.getString("address", "");
//            order.time = sp.getString("time", "");
//            order.lat = sp.getFloat("lat", 0);
//            order.lon = sp.getFloat("lon", 0);
//            order.item = sp.getString("item", "");
//            mOrderID = order.orderID;
//            servingOrder = order;
//            status = SERVING;
//        } else {
//            status = OFF_SERVICE;
//        }
//        notifyStatusListener();
//    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public StatusService getService() {
            return StatusService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    @Override
    public void onDestroy() {
        mWorkerHandler.removeMessages(SEND_LOCATION);
        mLocationManagerProxy.removeUpdates(this);
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public int getStatus() {
        return status;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public ArrayList<Order> getOrdersAsked() {
        return ordersAsked;
    }

    public Account getAccount() {
        return account;
    }
}
