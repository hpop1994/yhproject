package cn.yhsh.yhservecar.UI.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.yhsh.yhservecar.Core.APIs;
import cn.yhsh.yhservecar.Core.Account;
import cn.yhsh.yhservecar.Core.NetworkCallback;
import cn.yhsh.yhservecar.Core.Order;
import cn.yhsh.yhservecar.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.todddavies.components.progressbar.ProgressWheel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Xujc on 2015/3/7 007.
 */
public class ServingOrderListActivity extends Activity {
    @ViewInject(R.id.list)
    private ListView listView;

    @ViewInject(R.id.pw_spinner)
    private ProgressWheel progressWheel;

    private ArrayList<Order> list;
    private LayoutInflater inflater;
    private MyOrderAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_list);
        ViewUtils.inject(this);

        inflater=getLayoutInflater();

        list=new ArrayList<Order>();

        adapter = new MyOrderAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(ServingOrderListActivity.this, ServingOrderDetailActivity.class);
                intent.putExtra("orderID", list.get(position).orderID);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        list.clear();
        progressWheel.setVisibility(View.VISIBLE);
        progressWheel.spin();
        APIs.getServingOrders(Account.getInstance(this), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    JSONArray array = data.getJSONArray("orders");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        Order order = new Order();
                        order.orderID = object.getInt("id");
                        order.uid = object.getInt("uid");
                        order.name = object.getString("realname");
                        order.phone = object.getString("phonenum");
                        order.address = object.getString("address");
//                        order.time = object.getString("ordertime");
                        order.appointmentTime = object.getString("time");
                        order.lat = object.getDouble("lat");
                        order.lon = object.getDouble("lon");
                        order.item = object.getString("item");
                        list.add(order);
                    }
                    adapter.notifyDataSetChanged();
                    progressWheel.stopSpinning();
                    progressWheel.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressWheel.stopSpinning();
                    progressWheel.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onFailed() {
                progressWheel.stopSpinning();
                progressWheel.setVisibility(View.GONE);
            }
        });
    }

    private class MyOrderAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Order getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).orderID;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView=inflater.inflate(R.layout.order_item,null);
            }
            TextView idText=(TextView) convertView.findViewById(R.id.order_id);
            TextView timeText=(TextView) convertView.findViewById(R.id.time);
            Order order = getItem(position);
            timeText.setText(order.appointmentTime);
            int i = -1;
            try {
                i = Integer.parseInt(idText.getText().toString());
            } catch (NumberFormatException e) {
            }
            if (i == order.orderID){
                return convertView;
            }
            TextView itemText=(TextView) convertView.findViewById(R.id.item);
            TextView addressText=(TextView) convertView.findViewById(R.id.address);
            idText.setText(String.valueOf(order.orderID));
            itemText.setText(order.item);
            addressText.setText(order.address);
            return convertView;
        }
    }
}