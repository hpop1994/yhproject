package cn.yhsh.yhservecar.UI.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.yhsh.yhservecar.Core.*;
import cn.yhsh.yhservecar.R;
import cn.yhsh.yhservecar.UI.component.LoadLocker;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/1/22.
 */
public class AskingActivity extends BackActivity {
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
//
//    @ViewInject(R.id.map)
//    private MapView mapView;

    private double lat;
    private double lon;
//    private AMap aMap;
    private int orderID;
    private Order order;
    private LoadLocker loadLocker;

    private String dateStr;
    private String timeStr;
    private String newAppointmentTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asking_activity);
        ViewUtils.inject(this);
        getActionBar().setTitle("新订单");
//
//        mapView.onCreate(savedInstanceState);
//        if (aMap == null) {
//            aMap = mapView.getMap();
//        }

        final Intent intent=getIntent();
        orderID = intent.getIntExtra("order_id",0);
        loadLocker = new LoadLocker(this);
        loadLocker.setCancalable(false);

        APIs.getOrderDetail(orderID, Account.getInstance(this), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    order = new Order();
                    order.orderID = data.getInt("id");
                    order.uid = data.getInt("uid");
                    order.name = data.getString("realname");
                    order.phone = data.getString("phonenum");
                    order.address = data.getString("address");
                    order.time = data.getString("ordertime");
                    order.appointmentTime = data.getString("time");
                    order.lat = data.getDouble("lat");
                    order.lon = data.getDouble("lon");
                    order.item = data.getString("item");

                    clientNameText.setText(data.getString("realname"));
                    clientPhoneText.setText(data.getString("phonenum"));
                    positionText.setText(data.getString("address"));
                    timeText.setText(data.getString("ordertime").split("\\s")[0]);
                    itemsText.setText(data.getString("item"));
                    lat = data.getDouble("lat");
                    lon = data.getDouble("lon");

                    if (!(lat<1 && lon < 1)){
                        positionText.setTextColor(getResources().getColor(R.color.click_blue));
                        positionText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent mapIntent=new Intent(AskingActivity.this,MapActivity.class);
                                mapIntent.putExtra("lat",lat);
                                mapIntent.putExtra("lon",lon);
                                startActivity(mapIntent);
                            }
                        });
                    }

//                    MarkerOptions markerOptions = new MarkerOptions();
//                    LatLng latLng = new LatLng(lat, lon);
//                    markerOptions.position(latLng);
//                    markerOptions.snippet("用户");
//                    aMap.addMarker(markerOptions);
//                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
//                    aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                } catch (JSONException e) {
                    e.printStackTrace();
                    dealServerFormatError();
                }
            }

            @Override
            protected void onFailed() {
                MyToast.makeText(AskingActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }


    @OnClick(R.id.refuse)
    private void refuseClicked(View v){
        loadLocker.start("正在拒绝");
        APIs.replyOrderRequest(orderID, false, Account.getInstance(this), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                loadLocker.jobFinished();
                MyToast.makeText(AskingActivity.this, "拒绝成功", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            protected void onFailed() {
                loadLocker.jobFinished();
                MyToast.makeText(AskingActivity.this, "拒绝失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    @OnClick(R.id.take)
    private void takeClicked(View v){
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        Date date = null;
//        int tYear;
//        int tMonth;
//        int tDay;
//
//        try {
//            date = format.parse(order.appointmentTime);
//            Calendar c = Calendar.getInstance();
//            c.setTime(date);
//            tYear = c.get(Calendar.YEAR);
//            tMonth = c.get(Calendar.MONTH);
//            tDay = c.get(Calendar.DAY_OF_MONTH);
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//            Calendar c = Calendar.getInstance();
//            tYear = c.get(Calendar.YEAR);
//            tMonth = c.get(Calendar.MONTH);
//            tDay = c.get(Calendar.DAY_OF_MONTH);
//        }
//        Calendar today=Calendar.getInstance();
//        final int year=tYear;
//        final int month=tMonth;
//        final int day=tDay;
//        final int hour=today.get(Calendar.HOUR_OF_DAY);
//        final int minutes=today.get(Calendar.MINUTE);
//
//        dateStr=null;
//        newAppointmentTime=null;
//        MyToast.makeText(this, "请设定预约的时间",Toast.LENGTH_SHORT).show();
//        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                dateStr = "" + year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
//            }
//        }, year, month, day);
//        datePicker.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                TimePickerDialog timePickerDialog = new TimePickerDialog(AskingActivity.this,  new TimePickerDialog.OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                        if (dateStr==null){
//                            return;
//                        }
//                        timeStr=""+hourOfDay+":"+minute;
//                        newAppointmentTime = dateStr + " " + timeStr;
//
//                    }
//                }, hour,minutes,true);
//                timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        if (timeStr==null){
//                            return;
//                        }
//                        loadLocker.start("正在接受");
//                        APIs.changeAppointmentTime(orderID, newAppointmentTime
//                                , Account.getInstance(AskingActivity.this)
//                                , new NetworkCallback(AskingActivity.this) {
//                            @Override
//                            protected void onSuccess(JSONObject data) {
//                                makeText("预约时间设定成功");
//                            }
//                        });
//
//                    }
//                });
//                timePickerDialog.setTitle("设定预约时间");
//                timePickerDialog.show();
//            }
//        });
//        datePicker.setTitle("设定预约时间");
//        datePicker.show();

        APIs.replyOrderRequest(orderID, true, Account.getInstance(AskingActivity.this), new NetworkCallback(AskingActivity.this) {
            @Override
            protected void onSuccess(JSONObject data) {
                loadLocker.jobFinished();
                MyToast.makeText(AskingActivity.this, "接受成功", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            protected void onFailed() {
                loadLocker.jobFinished();
                MyToast.makeText(AskingActivity.this, "接受失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @OnClick(R.id.client_phone)
    private void phoneClicked(View v){
        if (!clientPhoneText.getText().equals("正在等待接单")){
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clientPhoneText.getText()));
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mapView.onDestroy();
    }
}