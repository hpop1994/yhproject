package cn.yhsh.yhservecar.UI.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.yhsh.yhservecar.Core.Account;
import cn.yhsh.yhservecar.Core.MyToast;
import cn.yhsh.yhservecar.Core.NewAPI;
import cn.yhsh.yhservecar.Core.entry.OrderDetail;
import cn.yhsh.yhservecar.R;
import cn.yhsh.yhservecar.UI.component.LoadLocker;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

/**
 * Created by Xujc on 2015/5/19 019.
 */
public class CanceledActivity extends Activity {
    @ViewInject(R.id.client_name)
    private TextView clientNameText;

    @ViewInject(R.id.client_phone)
    private TextView clientPhoneText;

    @ViewInject(R.id.client_type)
    private TextView clientTypeText;

    @ViewInject(R.id.order_serial_id)
    private TextView serialId;

    @ViewInject(R.id.position_text)
    private TextView positionText;

    @ViewInject(R.id.time)
    private TextView timeText;

    @ViewInject(R.id.items)
    private TextView itemsText;

    @ViewInject(R.id.appointment_time)
    private TextView appointmentText;

    @ViewInject(R.id.cancel_reason)
    private TextView cancelReason;

//    @ViewInject(R.id.map)
//    private MapView mapView;

    private double lat;
    private double lon;
    //    private AMap aMap;
    private int orderID;

    private LoadLocker loadLocker;

    private boolean infoDone = false;

    private String dateStr;
    private String timeStr;
    private String newAppointmentStr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canceled_order_detail);
        ViewUtils.inject(this);

        loadLocker = new LoadLocker(this);
        loadLocker.setCancalable(false);

        orderID = getIntent().getIntExtra("orderID", 0);
        NewAPI.getOrderDetail(this, orderID, Account.getInstance(this), new NewAPI.InfoReceiver<OrderDetail>() {
            @Override
            public void success(OrderDetail orderDetail) {
                setOrderInfoWith(orderDetail);
            }

            @Override
            public void failed() {
                MyToast.makeText(CanceledActivity.this, "获取订单信息失败", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setOrderInfoWith(OrderDetail servingOrder) {
        cancelReason.setText(servingOrder.cancelReason);
        serialId.setText(servingOrder.serialId);
        clientNameText.setText(servingOrder.clientName);
        clientPhoneText.setText(servingOrder.clientPhone);
        clientTypeText.setText(servingOrder.clientType);
        positionText.setText(servingOrder.address);
        timeText.setText(servingOrder.makeOrderTime.split("\\s")[0]);
        itemsText.setText(servingOrder.appointmentItem);
        appointmentText.setText(servingOrder.appointmentTime.split("\\s")[0]);
        lat = servingOrder.lat;
        lon = servingOrder.lon;


        if (!(lat<1 && lon < 1)){
            positionText.setTextColor(getResources().getColor(R.color.click_blue));
            positionText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent=new Intent(CanceledActivity.this,MapActivity.class);
                    mapIntent.putExtra("lat",lat);
                    mapIntent.putExtra("lon",lon);
                    startActivity(mapIntent);
                }
            });
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
//                TimePickerDialog timePickerDialog = new TimePickerDialog(CanceledActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
//                                , Account.getInstance(CanceledActivity.this)
//                                , new NetworkCallback(CanceledActivity.this) {
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