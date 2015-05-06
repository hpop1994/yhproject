package cn.yhsh.yhservecar.UI.activity;

import android.content.Intent;
import android.os.Bundle;
import cn.yhsh.yhservecar.R;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by Xujc on 2015/5/6 006.
 */
public class MapActivity extends BackActivity {
    @ViewInject(R.id.map)
    private MapView mapView;
    private AMap aMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        ViewUtils.inject(this);

        mapView.onCreate(savedInstanceState);

        Intent intent=getIntent();
        double lat = intent.getDoubleExtra("lat", 0);
        double lon = intent.getDoubleExtra("lon", 0);
        aMap=mapView.getMap();
        LatLng latLng = new LatLng(lat, lon);
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("¿Í»§Î»ÖÃ");
        aMap.addMarker(markerOptions);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}