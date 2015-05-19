package cn.yhsh.yhservecar.UI.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cn.yhsh.yhservecar.Core.Account;
import cn.yhsh.yhservecar.Core.MyToast;
import cn.yhsh.yhservecar.Core.NewAPI;
import cn.yhsh.yhservecar.Core.Status;
import cn.yhsh.yhservecar.Core.entry.OrderList;
import cn.yhsh.yhservecar.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.todddavies.components.progressbar.ProgressWheel;

/**
 * Created by Xujc on 2015/3/6 006.
 */
public class OrderListActivity extends BackActivity {
    @ViewInject(R.id.list)
    private ListView listView;

    @ViewInject(R.id.pw_spinner)
    private ProgressWheel progressWheel;

    private OrderList list;
    private LayoutInflater inflater;
    private MyOrderAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_list);
        ViewUtils.inject(this);

        inflater=getLayoutInflater();

        adapter = new MyOrderAdapter();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OrderList.Entry order=adapter.getItem(position);
                Intent intent=null;
                switch (order.status){
                    case Status.CANCELED:
                        intent=new Intent(OrderListActivity.this, CanceledActivity.class);
                        break;
                    case Status.TAKEN:
                        intent=new Intent(OrderListActivity.this, TakenOrderDetailActivity.class);
                        break;
                    case Status.FINISHED:
                        intent=new Intent(OrderListActivity.this,FinishedOrderActivity.class);
                        break;
                    case Status.PRE_ORDER:
                    case Status.NEED_TAKEN:
                        intent=new Intent(OrderListActivity.this,AskingActivity.class);
                }
                if (intent!=null) {
                    intent.putExtra("orderID", Integer.parseInt(list.list.get(list.list.size()-1-position).id));
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        progressWheel.setVisibility(View.VISIBLE);
        progressWheel.spin();
        NewAPI.getOrderList(this, Account.getInstance(this), new NewAPI.InfoReceiver<OrderList>() {
            @Override
            public void success(OrderList orderList) {
                list = orderList;
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                progressWheel.stopSpinning();
                progressWheel.setVisibility(View.GONE);
            }

            @Override
            public void failed() {
                MyToast.makeText(OrderListActivity.this, "获取订单信息失败", Toast.LENGTH_SHORT).show();
                progressWheel.stopSpinning();
                progressWheel.setVisibility(View.GONE);
            }
        });
    }

    private class MyOrderAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.list.size();
        }

        @Override
        public OrderList.Entry getItem(int position) {
            return list.list.get(list.list.size()-1-position);
        }

        @Override
        public long getItemId(int position) {
            return Long.parseLong(getItem(position).id);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView=inflater.inflate(R.layout.order_item,null);
            }
            TextView idText=(TextView) convertView.findViewById(R.id.order_id);
            TextView timeText=(TextView) convertView.findViewById(R.id.time);
            timeText.setVisibility(View.GONE);
            TextView statusText=(TextView) convertView.findViewById(R.id.status);
            OrderList.Entry order = getItem(position);
            statusText.setText(Status.toName(order.status));
            TextView itemText=(TextView) convertView.findViewById(R.id.item);
            TextView addressText=(TextView) convertView.findViewById(R.id.address);
            idText.setText(String.valueOf(order.serialId));
            if (order.status==Status.FINISHED){
                itemText.setText("客户姓名:"+order.clientName+"    保养车辆:"+order.carSerialID);
                addressText.setText("保养位置:"+order.address);
            }else{
                itemText.setText("保养项目:"+order.appointmentItem);
                addressText.setText("预约时间:"+order.appointmentTime.split("\\s")[0]);
            }
            return convertView;
        }
    }
}