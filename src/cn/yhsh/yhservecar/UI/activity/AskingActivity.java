package cn.yhsh.yhservecar.UI.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.yhsh.yhservecar.Core.*;
import cn.yhsh.yhservecar.Core.entry.OrderDetail;
import cn.yhsh.yhservecar.R;
import cn.yhsh.yhservecar.UI.component.LoadLocker;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/1/22.
 */
public class AskingActivity extends BackActivity {
    @ViewInject(R.id.client_name)
    private TextView clientNameText;

    @ViewInject(R.id.order_serial_id)
    private TextView serialID;

    @ViewInject(R.id.client_type)
    private TextView clientType;

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

        final Intent intent = getIntent();
        orderID = intent.getIntExtra("order_id", 0);
        loadLocker = new LoadLocker(this);
        loadLocker.setCancalable(false);

        NewAPI.getOrderDetail(this, orderID, Account.getInstance(this), new NewAPI.InfoReceiver<OrderDetail>() {
            @Override
            public void success(OrderDetail orderDetail) {
                serialID.setText(orderDetail.serialId);
                clientType.setText(orderDetail.clientType);
                clientNameText.setText(orderDetail.clientName);
                clientPhoneText.setText(orderDetail.clientPhone);
                positionText.setText(orderDetail.address);
                timeText.setText(orderDetail.makeOrderTime.split("\\s")[0]);
                itemsText.setText(orderDetail.appointmentItem);
                lat = orderDetail.lat;
                lon = orderDetail.lon;

                if (!(lat < 1 && lon < 1)) {
                    positionText.setTextColor(getResources().getColor(R.color.click_blue));
                    positionText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent mapIntent = new Intent(AskingActivity.this, MapActivity.class);
                            mapIntent.putExtra("lat", lat);
                            mapIntent.putExtra("lon", lon);
                            startActivity(mapIntent);
                        }
                    });
                }
            }

            @Override
            public void failed() {
                MyToast.makeText(AskingActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }


    @OnClick(R.id.refuse)
    private void refuseClicked(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View inflate = View.inflate(this, R.layout.refuse_dialog, null);
        builder.setView(inflate);
        builder.setTitle("拒绝原因");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editText = (EditText) inflate.findViewById(R.id.reason);
                String reasonText = editText.getText().toString().trim();
                if (reasonText.length() == 0) {
                    MyToast.makeText(AskingActivity.this, "拒绝原因不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                loadLocker.start("正在拒绝");
                APIs.replyOrderRequest(orderID, reasonText, false,
                        Account.getInstance(AskingActivity.this), new NetworkCallback(AskingActivity.this) {
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
//                        finish();
                    }
                });

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    @OnClick(R.id.take)
    private void takeClicked(View v) {
        APIs.replyOrderRequest(orderID, "", true, Account.getInstance(AskingActivity.this), new NetworkCallback(AskingActivity.this) {
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
    private void phoneClicked(View v) {
        if (clientPhoneText.getText().length() == 11) {
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