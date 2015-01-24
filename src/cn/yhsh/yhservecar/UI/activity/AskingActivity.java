package cn.yhsh.yhservecar.UI.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.yhsh.yhservecar.Core.APIs;
import cn.yhsh.yhservecar.Core.Account;
import cn.yhsh.yhservecar.Core.NetworkCallback;
import cn.yhsh.yhservecar.Core.StatusService;
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
 * Created by Xujc on 2015/1/22.
 */
public class AskingActivity extends BindActivity {
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

    @ViewInject(R.id.map)
    private MapView mapView;

    private double lat;
    private double lon;
    private AMap aMap;
    private int orderID;
    private LoadLocker loadLocker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asking_activity);
        ViewUtils.inject(this);
        getActionBar().setTitle("新订单");

        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        Intent intent=getIntent();
        orderID = intent.getIntExtra("order_id",0);
        loadLocker = new LoadLocker(this);
        loadLocker.setCancalable(false);

    }

    @Override
    protected void onServiceConnected(final StatusService myService) {
        APIs.getOrderDetail(orderID, Account.getInstance(this), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    clientNameText.setText(data.getString("realname"));
                    clientPhoneText.setText(data.getString("phonenum"));
                    positionText.setText(data.getString("address"));
                    timeText.setText(data.getString("time"));
                    itemsText.setText(data.getString("item"));
                    lat = data.getDouble("lat");
                    lon = data.getDouble("lon");

                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(lat, lon);
                    markerOptions.position(latLng);
                    markerOptions.snippet("用户");
                    aMap.addMarker(markerOptions);
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                } catch (JSONException e) {
                    e.printStackTrace();
                    dealServerFormatError();
                }
            }

            @Override
            protected void onFailed() {
                Toast.makeText(myService, "获取信息失败",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onServiceDisconnected(StatusService myService) {

    }

    @OnClick(R.id.refuse)
    private void refuseClicked(View v){
        loadLocker.start("正在拒绝");
        getService().answerOrder(orderID, false, new StatusService.BackgroundJobListener() {
            @Override
            public void jobSuccess() {
                loadLocker.jobFinished();
                Toast.makeText(AskingActivity.this, "拒绝成功",Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void jobFailed() {
                loadLocker.jobFinished();
                Toast.makeText(AskingActivity.this, "拒绝失败",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @OnClick(R.id.take)
    private void takeClicked(View v){
        loadLocker.start("正在接受");
        getService().answerOrder(orderID, true, new StatusService.BackgroundJobListener() {
            @Override
            public void jobSuccess() {
                loadLocker.jobFinished();
                Toast.makeText(AskingActivity.this, "接受成功",Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void jobFailed() {
                loadLocker.jobFinished();
                Toast.makeText(AskingActivity.this, "接受失败",Toast.LENGTH_SHORT).show();
                finish();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}