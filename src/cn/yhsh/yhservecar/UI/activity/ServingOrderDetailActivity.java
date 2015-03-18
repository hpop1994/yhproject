package cn.yhsh.yhservecar.UI.activity;

import android.app.Activity;
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
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/3/7 007.
 */
public class ServingOrderDetailActivity extends Activity {
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

    @ViewInject(R.id.map)
    private MapView mapView;

    private double lat;
    private double lon;
    private AMap aMap;
    private int orderID;

    private LoadLocker loadLocker;
    private Order order;

    private boolean infoDone = false;

    private String dateStr;
    private String timeStr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serving_order_detail);
        ViewUtils.inject(this);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
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
        appointmentText.setText(servingOrder.appointmentTime);
        itemsText.setText(servingOrder.item);
        lat = servingOrder.lat;
        lon = servingOrder.lon;

        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng = new LatLng(lat, lon);
        markerOptions.position(latLng);
        markerOptions.snippet("用户");
        aMap.addMarker(markerOptions);
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
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

    @OnClick(R.id.finish_btn)
    private void finishClicked(View v) {
        Intent intent = new Intent(this, FinishActivity.class);
        intent.putExtra("orderID",orderID);
        startActivityForResult(intent,1);
    }


    @OnClick(R.id.client_phone)
    private void phoneClicked(View v){
        if (!clientPhoneText.getText().equals("正在等待接单")){
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clientPhoneText.getText()));
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode==1){
            finish();
        }
    }
}