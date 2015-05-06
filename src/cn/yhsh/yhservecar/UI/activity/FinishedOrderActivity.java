package cn.yhsh.yhservecar.UI.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.yhsh.yhservecar.Core.APIs;
import cn.yhsh.yhservecar.Core.Account;
import cn.yhsh.yhservecar.Core.NetworkCallback;
import cn.yhsh.yhservecar.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/1/12.
 */
public class FinishedOrderActivity extends Activity  {

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

        APIs.getOrderDetail(orderID, Account.getInstance(this), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    int status = data.getInt("status");
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    if (status == FINISHED) {
                        orderStatus.setText("结算完成");
                        clientNameText.setText(data.getString("client_name"));
                        clientPhoneText.setText(data.getString("client_phone"));
                        clientTypeText.setText(data.getString("client_type"));
                        positionText.setText(data.getString("address"));
                        remarkText.setText(data.getString("remark"));

                        serveTimeText.setText(data.getString("ordertime"));
                        finishTimeText.setText(data.getString("time"));

                        JSONArray array = data.getJSONArray("good");
                        int length = array.length();
                        for (int i = 0; i < length; i++) {
                            inflater.inflate(R.layout.care_item_without_checkbox, itemList);
                            View view = itemList.getChildAt(i);
                            JSONObject jsonObject = array.getJSONObject(i);
                            ((TextView) view.findViewById(R.id.text)).setText(jsonObject.getString("name"));
                            double aprice = jsonObject.getDouble("price");
                            int number = jsonObject.getInt("num");
                            int discount = jsonObject.getInt("discount");
                            ((TextView) view.findViewById(R.id.number)).setText(String.valueOf(number) + " " + jsonObject.getString("unit"));
                            ((TextView) view.findViewById(R.id.price)).setText(String.valueOf(aprice));
                            ((TextView) view.findViewById(R.id.discount)).setText(String.valueOf(discount)+"折");
                        }
                        priceText.setText(String.valueOf(data.getDouble("price")));

                        carIDText.setText(data.getJSONObject("usercar").getString("carid"));
                        carNameText.setText(data.getJSONObject("usercar").getString("name"));
                        carKmText.setText(data.getString("km"));
                    } else if (status == CANCELED) {
                        orderStatus.setText("已取消");
                        scarNameLayout.setVisibility(View.GONE);
                        positionText.setText(data.getString("address"));
                        serveTimeText.setText(data.getString("time"));
                        finishTimeText.setText(data.getString("time").split("\\s")[0]);
                        carArea.setVisibility(View.GONE);
                        remarkText.setText(data.getString("remark"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void addInfoItem(String title, String content, LayoutInflater inflater) throws JSONException {
                View v = inflater.inflate(R.layout.info_item, infoLayout);
                ((TextView) v.findViewById(R.id.title)).setText(title);
                ((TextView) v.findViewById(R.id.info)).setText(content);
            }


            @Override
            protected void onFailed() {
                makeText("获取信息失败");
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

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}