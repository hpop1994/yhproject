package cn.yhsh.yhservecar.UI.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.yhsh.yhservecar.Core.APIs;
import cn.yhsh.yhservecar.Core.NetworkCallback;
import cn.yhsh.yhservecar.Core.Order;
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
public class ServingFragment extends Fragment implements ServiceFragment {
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

    private boolean firstTime=true;

    private LoadLocker loadLocker;
    private StatusService statusService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.serving_fragment,container,false);
        ViewUtils.inject(this,v);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        loadLocker=new LoadLocker(getActivity());
        loadLocker.setCancalable(false);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

    }

    @Override
    public void onConnected(final StatusService service) {
        statusService = service;
        orderID = service.getOrderID();
        if (!firstTime){
            return;
        }
        firstTime=false;
        final Order servingOrder = service.getServingOrder();
        if (servingOrder !=null&& servingOrder.orderID==orderID){
            setOrderInfoWith(servingOrder);
            return;
        }
        reFresh();
    }

    public void reFresh() {
        if (statusService==null){
            return;
        }
        APIs.getOrderDetail(orderID, statusService.getAccount(), new NetworkCallback(statusService) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    Order order = new Order();
                    order.orderID = orderID;
                    order.uid=data.getInt("uid");
                    order.name = data.getString("realname");
                    order.phone = data.getString("phonenum");
                    order.address = data.getString("address");
                    order.time = data.getString("time");
                    order.lat = data.getDouble("lat");
                    order.lon = data.getDouble("lon");
                    order.item=data.getString("item");
                    statusService.setServingOrder(order);
                    statusService.saveServeStatus();
                    setOrderInfoWith(order);
                } catch (JSONException e) {
                    e.printStackTrace();
                    dealServerFormatError();
                }
            }

        });
    }

    @Override
    public void onDisconnected(StatusService service) {
        statusService=null;
    }


    private void setOrderInfoWith(Order servingOrder) {
        clientNameText.setText(servingOrder.name);
        clientPhoneText.setText(servingOrder.phone);
        positionText.setText(servingOrder.address);
        timeText.setText(servingOrder.time);
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
    private void cnacelClicked(View v){
        BindActivity activity=(BindActivity) getActivity();
        StatusService service=activity.getService();
        loadLocker.start("正在取消订单");
        service.cancelOrder(orderID, new StatusService.BackgroundJobListener() {
            @Override
            public void jobSuccess() {
                loadLocker.jobFinished();
            }

            @Override
            public void jobFailed() {
                loadLocker.jobFinished();
            }
        });
    }

    @OnClick(R.id.finish)
    private void finishClicked(View v){
        startActivity(new Intent(getActivity(), FinishActivity.class));
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }
}