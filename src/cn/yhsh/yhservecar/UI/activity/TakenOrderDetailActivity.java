package cn.yhsh.yhservecar.UI.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import cn.yhsh.yhservecar.Core.APIs;
import cn.yhsh.yhservecar.Core.Account;
import cn.yhsh.yhservecar.Core.NetworkCallback;
import cn.yhsh.yhservecar.Core.Order;
import cn.yhsh.yhservecar.R;
import cn.yhsh.yhservecar.UI.component.LoadLocker;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/3/6 006.
 */
public class TakenOrderDetailActivity extends BackActivity {
    @ViewInject(R.id.client_name)
    private TextView clientNameText;

    @ViewInject(R.id.client_phone)
    private TextView clientPhoneText;

    @ViewInject(R.id.position_text)
    private TextView positionText;

    @ViewInject(R.id.time)
    private TextView timeText;

    @ViewInject(R.id.items)
    private TextView itemsText;

    @ViewInject(R.id.appointment_time)
    private TextView appointmentText;

//    @ViewInject(R.id.map)
//    private MapView mapView;

    private double lat;
    private double lon;
//    private AMap aMap;
    private int orderID;

    private LoadLocker loadLocker;
    private Order order;

    private boolean infoDone = false;

    private String dateStr;
    private String timeStr;
    private String newAppointmentStr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taken_order_detail);
        ViewUtils.inject(this);
//        mapView.onCreate(savedInstanceState);
//        if (aMap == null) {
//            aMap = mapView.getMap();
//        }
        loadLocker = new LoadLocker(this);
        loadLocker.setCancalable(false);

        orderID = getIntent().getIntExtra("orderID", 0);
        APIs.getOrderDetail(orderID, Account.getInstance(this), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    order = new Order();
                    order.orderID = orderID;
                    order.uid = data.getInt("uid");
                    order.name = data.getString("realname");
                    order.phone = data.getString("phonenum");
                    order.address = data.getString("address");
                    order.time = data.getString("ordertime");
                    order.appointmentTime = data.getString("time");
                    order.lat = data.getDouble("lat");
                    order.lon = data.getDouble("lon");
                    order.item = data.getString("item");
                    setOrderInfoWith(order);
                    infoDone = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setOrderInfoWith(Order servingOrder) {
        clientNameText.setText(servingOrder.name);
        clientPhoneText.setText(servingOrder.phone);
        positionText.setText(servingOrder.address);
        timeText.setText(servingOrder.time);
        itemsText.setText(servingOrder.item);
        appointmentText.setText(servingOrder.appointmentTime.split("\\s")[0]);
        lat = servingOrder.lat;
        lon = servingOrder.lon;

//        MarkerOptions markerOptions = new MarkerOptions();
//        LatLng latLng = new LatLng(lat, lon);
//        markerOptions.position(latLng);
//        markerOptions.snippet("用户");
//        aMap.addMarker(markerOptions);
//        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
//        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }

    @OnClick(R.id.cancel)
    private void cancelClicked(View v) {
        loadLocker.start("正在取消订单");
        APIs.cancelOrder(orderID, Account.getInstance(this), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                loadLocker.jobFinished();
                finish();
                makeText("取消成功");
            }

            @Override
            protected void onFailed() {
                loadLocker.jobFinished();
            }
        });
    }

    @OnClick(R.id.serve_now_btn)
    private void serveClicked(View v) {
        Intent intent = new Intent(this, FinishActivity.class);
        intent.putExtra("orderID", orderID);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode==1){
            finish();
        }
    }

//    @OnClick(R.id.appointment_time)
//    private void appointmentTimeClicked(View v) {
//        if (!infoDone) {
//            return;
//        }
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//        Date date = null;
//        int tYear;
//        int tMonth;
//        int tDay;
//        int tHour;
//        int tMinutes;
//        try {
//            date = format.parse(order.appointmentTime);
//            Calendar c = Calendar.getInstance();
//            c.setTime(date);
//            tYear = c.get(Calendar.YEAR);
//            tMonth = c.get(Calendar.MONTH);
//            tDay = c.get(Calendar.DAY_OF_MONTH);
//            tHour = c.get(Calendar.HOUR_OF_DAY);
//            tMinutes = c.get(Calendar.MINUTE);
//        } catch (ParseException e) {
//            e.printStackTrace();
//            Calendar c = Calendar.getInstance();
//            tYear = c.get(Calendar.YEAR);
//            tMonth = c.get(Calendar.MONTH);
//            tDay = c.get(Calendar.DAY_OF_MONTH);
//            tHour = c.get(Calendar.HOUR_OF_DAY);
//            tMinutes = c.get(Calendar.MINUTE);
//        }
//        final int year=tYear;
//        final int month=tMonth;
//        final int day=tDay;
//        final int hour=tHour;
//        final int minutes=tMinutes;
//
//        dateStr=null;
//        newAppointmentStr=null;
//        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                dateStr = "" + year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
//            }
//        }, year, month, day);
//        datePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                if (dateStr==null){
//                    return;
//                }
//                TimePickerDialog timePickerDialog = new TimePickerDialog(TakenOrderDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                        timeStr=""+hourOfDay+":"+minute;
//                        newAppointmentStr = dateStr + " " + timeStr;
//                        appointmentText.setText(newAppointmentStr);
//
//                    }
//                }, hour,minutes,true);
//                timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        APIs.changeAppointmentTime(orderID, newAppointmentStr
//                                , Account.getInstance(TakenOrderDetailActivity.this)
//                                , new NetworkCallback(TakenOrderDetailActivity.this) {
//                            @Override
//                            protected void onSuccess(JSONObject data) {
//                                makeText("预约时间修改成功");
//                            }
//                        });
//                    }
//                });
//                timePickerDialog.setTitle("设定预约时间");
//                timePickerDialog.show();
//            }
//        });
//        datePicker.setTitle("设定预约时间");
//        datePicker.show();
//    }

    @OnClick(R.id.client_phone)
    private void phoneClicked(View v){
        if (!clientPhoneText.getText().equals("正在等待接单")){
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clientPhoneText.getText()));
            startActivity(intent);
        }
    }

}