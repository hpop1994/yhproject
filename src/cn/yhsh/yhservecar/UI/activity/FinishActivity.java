package cn.yhsh.yhservecar.UI.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cn.yhsh.yhservecar.Core.*;
import cn.yhsh.yhservecar.R;
import cn.yhsh.yhservecar.UI.component.LayerSelectorView;
import cn.yhsh.yhservecar.UI.component.ListItem;
import cn.yhsh.yhservecar.UI.component.LoadLocker;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Xujc on 2015/1/23.
 */
public class FinishActivity extends BindActivity {
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

    @ViewInject(R.id.itemList)
    private ListView listView;

    @ViewInject(R.id.selectCarBtn)
    private Button selectCarBtn;

    @ViewInject(R.id.price)
    private EditText priceEditText;

    private LayerSelectorView itemSelector;
    private LayerSelectorView carAdder;
    private PopupWindow popupWindow;
    private View dialogView;

    private ArrayList<ListItem> itemListData = new ArrayList<ListItem>();
    private MyAdapter adapter;

    private LoadLocker loadLocker;

    private ArrayList<Car> cars = new ArrayList<Car>();
    private int selectedcarid = 0;
    private LayoutInflater inflater;
    private RelativeLayout layout;


    class Car {
        int id;
        String carCode;
        String desc;

        public Car(int id, String carCode, String desc) {
            this.id = id;
            this.carCode = carCode;
            this.desc = desc;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finish_order_activity);
        ViewUtils.inject(this);
        selectCarBtn.setText("尚未选择");
        inflater = getLayoutInflater();
        loadLocker = new LoadLocker(this);
        adapter = new MyAdapter();
        listView.setAdapter(adapter);

        itemSelector = new LayerSelectorView(this);

        itemSelector.setItemDrawer(new LayerSelectorView.ItemDrawer() {
            @Override
            public View getNodeView(ListItem listItem) {
                View v = inflater.inflate(R.layout.selector_node, null);
                ((TextView) v.findViewById(R.id.name)).setText(listItem.getItemName());
                return v;
            }

            @Override
            public View getLeafView(ListItem listItem) {
                View v = inflater.inflate(R.layout.selector_leaf, null);
                ((TextView) v.findViewById(R.id.name)).setText(listItem.getItemName());
                return v;
            }
        });

        itemSelector.setListener(new LayerSelectorView.LayerSelectorListener() {
            @Override
            public void onAskForLayerData(ListItem item, final ArrayList<ListItem> oldData) {
                itemSelector.showInProgress();
                APIs.getGoodClass(item.getId(), Account.getInstance(FinishActivity.this), new NetworkCallback(FinishActivity.this) {
                    @Override
                    protected void onSuccess(JSONObject data) {
                        oldData.clear();
                        try {
                            JSONArray goodsArray = data.getJSONArray("gooditems");
                            JSONArray classitem = data.getJSONArray("classitem");
                            for (int i = 0; i < classitem.length(); i++) {
                                JSONObject object = classitem.getJSONObject(i);
                                ListItem listItem = new ListItem(true, object.getInt("id"), object.getString("name"), null);
                                oldData.add(listItem);
                            }
                            for (int i = 0; i < goodsArray.length(); i++) {
                                JSONObject object = goodsArray.getJSONObject(i);
                                ListItem listItem = new ListItem(false, object.getInt("id"), object.getString("name"), object);
                                oldData.add(listItem);
                            }
                            itemSelector.showData();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onSelectLeaf(ListItem item, ArrayList<ListItem> oldData, int position) {
                popupWindow.dismiss();
                itemListData.add(item);
                adapter.notifyDataSetChanged();
                setListViewHeightBasedOnChildren(listView);
            }
        });

        carAdder = new LayerSelectorView(this);
        carAdder.setItemDrawer(new LayerSelectorView.ItemDrawer() {
            @Override
            public View getNodeView(ListItem listItem) {
                View v = inflater.inflate(R.layout.selector_node, null);
                ((TextView) v.findViewById(R.id.name)).setText(listItem.getItemName());
                return v;
            }

            @Override
            public View getLeafView(ListItem listItem) {
                View v = inflater.inflate(R.layout.selector_node, null);
                ((TextView) v.findViewById(R.id.name)).setText(listItem.getItemName());
                return v;
            }
        });
        carAdder.setListener(new LayerSelectorView.LayerSelectorListener() {
            @Override
            public void onAskForLayerData(final ListItem item, final ArrayList<ListItem> oldData) {
                APIs.getCarClass(item.getId(), Account.getInstance(FinishActivity.this), new NetworkCallback(FinishActivity.this) {
                    @Override
                    protected void onSuccess(JSONObject data) {
                        carAdder.showInProgress();
                        try {
                            JSONArray items = data.getJSONArray("items");

                            oldData.clear();
                            for (int i = 0; i < items.length(); i++) {
                                JSONObject object = items.getJSONObject(i);
                                ListItem listItem = new ListItem(true, object.getInt("id"), object.getString("name"), null);
                                oldData.add(listItem);
                            }
                            carAdder.showData();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void onFailed() {
                        popupWindow.dismiss();
                        onSelectLeaf(item, oldData, 0);
                    }
                });
            }

            @Override
            public void onSelectLeaf(final ListItem item, ArrayList<ListItem> oldData, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FinishActivity.this);
                dialogView = inflater.inflate(R.layout.add_user_car, null);
                builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        popupWindow.dismiss();
                        dialog.dismiss();
                        loadLocker.start("添加车辆");
                        EditText caridText = (EditText) dialogView.findViewById(R.id.carid);
                        EditText descText = (EditText) dialogView.findViewById(R.id.desc);
                        String carID = caridText.getText().toString().trim();
                        String desc = descText.getText().toString().trim();
                        APIs.addUserCar(getService().getServingOrder().uid, item.getId(), carID, desc, getService().getAccount(), new NetworkCallback(FinishActivity.this) {
                            @Override
                            protected void onSuccess(JSONObject data) {

                            }
                        });
                        APIs.getUserCars(getService().getServingOrder().uid, getService().getAccount(), new NetworkCallback(FinishActivity.this) {
                            @Override
                            protected void onSuccess(JSONObject data) {
                                try {
                                    JSONArray array = data.getJSONArray("cars");
                                    cars.clear();
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject object = array.getJSONObject(i);
                                        cars.add(new Car(
                                                object.getInt("id"),
                                                object.getString("carid"),
                                                object.getString("desc")
                                        ));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                loadLocker.jobFinished();
                            }

                            @Override
                            protected void onFailed() {
                                loadLocker.jobFinished();
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
                builder.setView(dialogView);
                builder.show();
            }
        });

        popupWindow = new PopupWindow(this);

        popupWindow.setBackgroundDrawable(new ColorDrawable(0xa33d3d3d));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
    }
    public void reFresh(final StatusService statusService) {
        if (statusService==null){
            return;
        }
        APIs.getOrderDetail(statusService.getOrderID(), statusService.getAccount(), new NetworkCallback(statusService) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    Order order = new Order();
                    order.orderID = statusService.getOrderID();
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
                    setInfo(statusService);
                } catch (JSONException e) {
                    e.printStackTrace();
                    dealServerFormatError();
                }
            }

        });
    }

    @Override
    protected void onServiceConnected(StatusService myService) {
        if (myService.getServingOrder()==null){
            reFresh(myService);
        }
        setInfo(myService);
    }

    private void setInfo(StatusService myService) {
        clientNameText.setText(myService.getServingOrder().name);
        clientPhoneText.setText(myService.getServingOrder().phone);
        positionText.setText(myService.getServingOrder().address);
        timeText.setText(myService.getServingOrder().time);
        itemsText.setText(myService.getServingOrder().item);

        APIs.getUserCars(getService().getServingOrder().uid, getService().getAccount(), new NetworkCallback(FinishActivity.this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    JSONArray array = data.getJSONArray("cars");
                    cars.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        cars.add(new Car(
                                object.getInt("id"),
                                object.getString("carid"),
                                object.getString("desc")
                        ));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loadLocker.jobFinished();
            }

            @Override
            protected void onFailed() {
                loadLocker.jobFinished();
            }
        });
    }

    @Override
    protected void onServiceDisconnected(StatusService myService) {

    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return itemListData.size();
        }

        @Override
        public ListItem getItem(int position) {
            return itemListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int iposition=position;
            View v = inflater.inflate(R.layout.item_selected_item, null);
            TextView nameText = (TextView) v.findViewById(R.id.name);
            TextView unitText = (TextView) v.findViewById(R.id.unit);
            Button deleteBtn = (Button) v.findViewById(R.id.delete);
            nameText.setText(getItem(position).getItemName());
            try {
                unitText.setText(((JSONObject) getItem(position).getBaseData()).getString("unit"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemListData.remove(iposition);
                    notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(listView);
                }
            });
            return v;
        }
    }

    @OnClick(R.id.addItemBtn)
    private void addItemBtnClicked(View v) {
        layout = (RelativeLayout) findViewById(R.id.finish_order_layout);
        popupWindow.setHeight(layout.getHeight());
        popupWindow.setWidth(layout.getWidth() * 3 / 4);
        itemSelector.startWithRootLayer(new ListItem(true, 0, "root", null));
        popupWindow.setContentView(itemSelector);
        popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, layout.getWidth() / 4, getActionBar().getHeight());
    }

    @OnClick(R.id.addCarBtn)
    private void addCarBtnClicked(View v) {
        layout = (RelativeLayout) findViewById(R.id.finish_order_layout);
        popupWindow.setHeight(layout.getHeight());
        popupWindow.setWidth(layout.getWidth() * 3 / 4);
        carAdder.startWithRootLayer(new ListItem(true, 0, "root", null));
        popupWindow.setContentView(carAdder);
        popupWindow.showAtLocation(carAdder, Gravity.NO_GRAVITY, layout.getWidth() / 4, getActionBar().getHeight());
    }

    @OnClick(R.id.selectCarBtn)
    private void selectDarBtnClicked(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择车辆");
        final String[] carStirngs = new String[cars.size()];
        for (int i = 0; i < cars.size(); i++) {
            carStirngs[i] = cars.get(i).carCode + " " + cars.get(i).desc;
        }
        builder.setItems(carStirngs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedcarid = cars.get(which).id;
                selectCarBtn.setText(cars.get(which).carCode + " " + cars.get(which).desc);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @OnClick(R.id.give_up)
    private void giveUpClicked(View v) {
        finish();
    }

    @OnClick(R.id.confirm)
    private void confirmClicked(View v) {
        String s = priceEditText.getText().toString().trim();
        if (s.length() == 0) {
            Toast.makeText(this, "价格不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        double price = 0;
        try {
            price = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "价格不正确", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        if (price < 0) {
            Toast.makeText(this, "价格不能为负", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONArray array = null;
        try {
            array = new JSONArray();
            for (int i = 0; i < itemListData.size(); i++) {
                JSONObject object = new JSONObject();
                double num = Double.parseDouble(((EditText) listView.getChildAt(i).findViewById(R.id.number)).getText().toString().trim());
                try {
                    object.put(String.valueOf(itemListData.get(i).getId()), num);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                array.put(object);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "项目价格不正确", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        loadLocker.start("正在完成");
        getService().finishOrder(getService().getOrderID()
                , selectedcarid, array, price, new StatusService.BackgroundJobListener() {
            @Override
            public void jobSuccess() {
                Toast.makeText(FinishActivity.this, "完成订单成功", Toast.LENGTH_SHORT).show();
                loadLocker.jobFinished();
                finish();
            }

            @Override
            public void jobFailed() {
                loadLocker.jobFinished();
            }
        });
    }
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        if(listView == null) return;

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}