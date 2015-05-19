package cn.yhsh.yhservecar.UI.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.yhsh.yhservecar.Core.Account;
import cn.yhsh.yhservecar.Core.MyToast;
import cn.yhsh.yhservecar.Core.NewAPI;
import cn.yhsh.yhservecar.Core.entry.OrderDetail;
import cn.yhsh.yhservecar.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

import java.util.ArrayList;

/**
 * Created by Xujc on 2015/1/12.
 */
public class FinishedOrderActivity extends BackActivity  {

    @ViewInject(R.id.order_serial_id)
    private TextView serialId;

    @ViewInject(R.id.info_layout)
    private LinearLayout infoLayout;

    @ViewInject(R.id.status)
    private TextView orderStatus;

    @ViewInject(R.id.scar_name_layout)
    private LinearLayout scarNameLayout;

    @ViewInject(R.id.client_name)
    private TextView clientNameText;

    @ViewInject(R.id.client_phone)
    private TextView clientPhoneText;

    @ViewInject(R.id.client_type)
    private TextView clientTypeText;

    @ViewInject(R.id.serve_time_layout)
    private LinearLayout serveTimeLayout;

    @ViewInject(R.id.serve_time)
    private TextView serveTimeText;

    @ViewInject(R.id.finish_time)
    private TextView finishTimeText;

    @ViewInject(R.id.position_text)
    private TextView positionText;

    @ViewInject(R.id.item_list)
    private LinearLayout itemList;

    @ViewInject(R.id.price_text)
    private TextView priceText;

    @ViewInject(R.id.car_area)
    private View carArea;

    @ViewInject(R.id.car_id)
    private TextView carIDText;

    @ViewInject(R.id.car_km)
    private TextView carKmText;

    @ViewInject(R.id.next_car_km)
    private TextView carNextKmText;

    @ViewInject(R.id.car_class)
    private TextView carNameText;

    @ViewInject(R.id.remark)
    private TextView remarkText;

    private int orderID;

    public static final int FINISHED=3;
    public static final int CANCELED=2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finished_order_detail_activity);
        ViewUtils.inject(this);

        orderID = getIntent().getIntExtra("orderID", 0);

        NewAPI.getOrderDetail(this, orderID, Account.getInstance(this), new NewAPI.InfoReceiver<OrderDetail>() {
            @Override
            public void success(OrderDetail orderDetail) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                serialId.setText(orderDetail.serialId);
                orderStatus.setText(getString(R.string.order_finished));
                clientNameText.setText(orderDetail.clientName);
                clientPhoneText.setText(orderDetail.clientPhone);
                clientTypeText.setText(orderDetail.clientType);
                positionText.setText(orderDetail.address);
                remarkText.setText(orderDetail.remark);
                serveTimeText.setText(orderDetail.makeOrderTime.split("\\s")[0]);
                finishTimeText.setText(orderDetail.appointmentTime.split("\\s")[0]);

                ArrayList<OrderDetail.ActualCareItemEntry> array = orderDetail.actualCareItemList;
                int length = array.size();
                for (int i = 0; i < length; i++) {
                    inflater.inflate(R.layout.care_item_without_checkbox, itemList);
                    View view = itemList.getChildAt(i);
                    OrderDetail.ActualCareItemEntry aItme = array.get(i);
                    ((TextView) view.findViewById(R.id.text)).setText(aItme.name);
                    String aprice = aItme.price;
                    String number = aItme.number;
                    String discount = aItme.discount;
                    float value = Float.parseFloat(aprice) * Float.parseFloat(discount) * Float.parseFloat(number);
                    String acPrice=String.valueOf(((int)(value*100))/100.0);
                    ((TextView) view.findViewById(R.id.number)).setText(number + " " + aItme.unit);
                    ((TextView) view.findViewById(R.id.price)).setText(acPrice);
                    ((TextView) view.findViewById(R.id.discount)).setText(discount + "折");
                    ((TextView) view.findViewById(R.id.dan_jia)).setText(aItme.price+"￥/"+aItme.unit);
                }
                priceText.setText(orderDetail.actualPrice);
                carIDText.setText(orderDetail.carSerialID);
                carNameText.setText(orderDetail.carClass);
                carKmText.setText(orderDetail.nowKm);
                carNextKmText.setText(orderDetail.nextKm);
            }

            @Override
            public void failed() {
                MyToast.makeText(FinishedOrderActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();
            }
        });


    }
    @OnClick(R.id.client_phone)
    private void phoneClciked(View v){
        if (clientPhoneText.getText().length()==11){
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clientPhoneText.getText()));
            startActivity(intent);
        }
    }


}