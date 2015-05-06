package cn.yhsh.yhservecar.UI.activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cn.yhsh.yhservecar.Core.MyToast;
import cn.yhsh.yhservecar.Core.Order;
import cn.yhsh.yhservecar.Core.StatusService;
import cn.yhsh.yhservecar.R;
import cn.yhsh.yhservecar.UI.component.LoadLocker;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.ArrayList;

/**
 * Created by Xujc on 2015/1/22.
 */
public class WaitingFragment extends Fragment implements ServiceFragment {
    @ViewInject(R.id.order_list)
    private ListView listView;

    @ViewInject(R.id.change_status_btn)
    private Button changeStatusBtn;

    @ViewInject(R.id.connection_view)
    private ImageView connectionView;

    private ArrayList<Order> list;
    private BaseAdapter adapter;
    private StatusService statusService;
    private LoadLocker loadLocker;
    private LayoutInflater inflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        View v= this.inflater.inflate(R.layout.waiting_fragment, container, false);
        ViewUtils.inject(this,v);
        adapter = new MyOrderAdapter();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (list.get(position).inTime>120){
                    MyToast.makeText(statusService, "已超时，无法接单", Toast.LENGTH_SHORT).show();
                    return;
                }
                int orderID= list.get(position).orderID;
                Intent intent=new Intent(getActivity(),AskingActivity.class);
                intent.putExtra("order_id",orderID);
                startActivity(intent);
            }
        });
        loadLocker = new LoadLocker(getActivity());
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onConnected(StatusService service) {
        statusService = service;
        list=statusService.getOrdersAsked();
        listView.setAdapter(adapter);
        statusService.getAndNotifyOrders();
        if (statusService.getStatus()==StatusService.OFF_SERVICE){
            changeStatusBtn.setText("进入等待接单状态");
            changeStatusBtn.setBackgroundResource(R.drawable.btn_yellow);
            connectionView.setImageResource(android.R.drawable.presence_offline);
        } else {
            changeStatusBtn.setText("进入停止服务状态");
            changeStatusBtn.setBackgroundResource(R.drawable.btn_green);
            if (statusService.isDisconnected()){
                connectionView.setImageResource(android.R.drawable.presence_offline);
            } else {
                connectionView.setImageResource(android.R.drawable.presence_online);
            }
        }
        orderChanged();
    }

    @Override
    public void onDisconnected(StatusService service) {

    }

    public void connectionChanged() {
        if (statusService.isDisconnected()){
            connectionView.setImageResource(android.R.drawable.presence_offline);
        } else {
            connectionView.setImageResource(android.R.drawable.presence_online);
        }
    }

    @OnClick(R.id.change_status_btn)
    private void changeStatusBtnClicked(View v){
        if (statusService==null){
            return;
        }
        if (statusService.getStatus()==StatusService.OFF_SERVICE){
            loadLocker.start("进入接单状态");
            statusService.goIntoWait(new StatusService.BackgroundJobListener() {
                @Override
                public void jobSuccess() {
                    loadLocker.jobFinished();
                    changeStatusBtn.setText("进入停止服务状态");
                    changeStatusBtn.setBackgroundResource(R.drawable.btn_green);
                    connectionChanged();
                }

                @Override
                public void jobFailed() {
                    loadLocker.jobFinished();
                }
            });
        } else {
            if (list.size()!=0){
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder.setTitle("您有未处理的订单，请先处理再下线。");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return;
            }
            loadLocker.start("变更状态");
            statusService.goIntoOffService(new StatusService.BackgroundJobListener() {
                @Override
                public void jobSuccess() {
                    changeStatusBtn.setText("进入等待接单状态");
                    changeStatusBtn.setBackgroundResource(R.drawable.btn_yellow);
                    loadLocker.jobFinished();
                }

                @Override
                public void jobFailed() {
                    loadLocker.jobFinished();
                }
            });
        }
    }



    @OnClick(R.id.accout)
    private void onAccountBtnClicked(View v){
        startActivity(new Intent(getActivity(), UserInfoActivity.class));
    }

    public void orderChanged() {
        adapter.notifyDataSetChanged();
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
            TextView statusText=(TextView) convertView.findViewById(R.id.status);
            statusText.setVisibility(View.GONE);
            Order order = getItem(position);
            timeText.setText(String.valueOf(120 - order.inTime));
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