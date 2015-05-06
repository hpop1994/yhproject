package cn.yhsh.yhservecar.UI.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
public class FinishActivity extends BackActivity {
    @ViewInject(R.id.client_name)
    private EditText clientNameText;

    @ViewInject(R.id.client_phone)
    private EditText clientPhoneText;

    @ViewInject(R.id.client_type)
    private EditText clientTypeText;

    @ViewInject(R.id.position)
    private TextView positionText;

    @ViewInject(R.id.order_time)
    private TextView timeText;

    @ViewInject(R.id.appointment_time)
    private TextView appointmentText;

    @ViewInject(R.id.items)
    private TextView itemsText;

    @ViewInject(R.id.itemList)
    private LinearLayout listView;

    @ViewInject(R.id.selectCarBtn)
    private Button selectCarBtn;

    @ViewInject(R.id.price)
    private TextView priceEditText;

    @ViewInject(R.id.remark)
    private EditText remarkText;

    @ViewInject(R.id.distance)
    private EditText distanceEditText;

    private LayerSelectorView itemSelector;
    private LayerSelectorView carAdder;
    private PopupWindow popupWindow;
    private View dialogView;

    private ArrayList<ListItem> itemListData = new ArrayList<ListItem>();
//    private MyAdapter adapter;

    private LoadLocker loadLocker;

    private ArrayList<Car> cars = new ArrayList<Car>();
    private int selectedcarid = -1;
    private LayoutInflater inflater;
    private RelativeLayout layout;

    private int orderID;
    private Order order;


    class Car {
        int id;
        String carCode;
        String desc;
        public String classid;

        public Car(int id, String carCode, String desc,String classid) {
            this.id = id;
            this.carCode = carCode;
            this.desc = desc;
            this.classid=classid;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finish_order_activity);
        ViewUtils.inject(this);

        orderID=getIntent().getIntExtra("orderID",0);

        getActionBar().setTitle("完成订单");
        selectCarBtn.setText("选择用户车辆");
        inflater = getLayoutInflater();
        loadLocker = new LoadLocker(this);
        loadLocker.setCancalable(false);
//        adapter = new MyAdapter();
//        listView.setAdapter(adapter);

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
                try {
                    ((TextView) v.findViewById(R.id.price)).setText(((JSONObject) listItem.getBaseData()).getString("price"));
                    ((TextView) v.findViewById(R.id.unit)).setText(((JSONObject) listItem.getBaseData()).getString("unit"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                addItem(item);
//                adapter.notifyDataSetChanged();
//                setListViewHeightBasedOnChildren(listView);
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
                        APIs.addUserCar(order.uid, item.getId(), carID, desc,Account.getInstance(FinishActivity.this ), new NetworkCallback(FinishActivity.this) {
                            @Override
                            protected void onSuccess(JSONObject data) {

                            }
                        });
                        APIs.getUserCars(order.uid, Account.getInstance(FinishActivity.this), new NetworkCallback(FinishActivity.this) {
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
                                                object.getString("desc"),
                                                String.valueOf(item.getId())
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

        loadLocker.start("正在加载");
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
                    setInfo(order);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loadLocker.jobFinished();
                makeText("加载成功");
            }

            @Override
            protected void onFailed() {
                loadLocker.jobFinished();
                finish();
                makeText("加载失败");
            }
        });
        reSum();
    }

    private void addItem(ListItem item) {
        inflater.inflate(R.layout.item_selected_item, listView);
        final View view = listView.getChildAt(listView.getChildCount() - 1);
        view.setTag(item);
        TextView nameText = (TextView) view.findViewById(R.id.name);
        TextView unitText = (TextView) view.findViewById(R.id.unit);
        TextView unitText2 = (TextView) view.findViewById(R.id.unit2);
        TextView price = (TextView) view.findViewById(R.id.price);
        EditText numEdit=(EditText) view.findViewById(R.id.number);
        EditText discount=(EditText) view.findViewById(R.id.discount);
        ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.delete);
        nameText.setText(item.getItemName());
        try {
            unitText.setText(((JSONObject) item.getBaseData()).getString("unit"));
            unitText2.setText(((JSONObject) item.getBaseData()).getString("unit"));
            price.setText(((JSONObject) item.getBaseData()).getString("price"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListItem aItem=(ListItem) view.getTag();
                listView.removeView(view);
                itemListData.remove(aItem);
                reSum();
            }
        });
        numEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                reSum();
            }
        });
        discount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                reSum();
            }
        });
        reSum();
    }

    private void setInfo(Order order) {
        clientNameText.setText(order.name);
        if (order.name.length()>0){
            clientNameText.setFocusable(false);
        }
        clientPhoneText.setText(order.phone);
        if (clientPhoneText.length()==11){
            clientPhoneText.setFocusable(false);
        }

        //todo client type
        positionText.setText(order.address);
        timeText.setText(order.time);
        itemsText.setText(order.item);
        appointmentText.setText(order.appointmentTime);

        APIs.getUserCars(order.uid, Account.getInstance(this), new NetworkCallback(FinishActivity.this) {
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
                                object.getString("desc"),
                                object.getString("class")
                        ));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (cars.size()!=0){
                    final Car c=cars.get(0);
                    selectedcarid = c.id;
                    selectCarBtn.setText("正在读取");
                    APIs.getCarClassName(c.classid, Account.getInstance(FinishActivity.this), new NetworkCallback(FinishActivity.this) {
                        @Override
                        protected void onSuccess(JSONObject data) {
                            try {
                                selectedcarid = c.id;
                                selectCarBtn.setText(c.carCode + "\n" + data.getString("carclass"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                selectedcarid = c.id;
                                selectCarBtn.setText(c.carCode);
                            }
                        }

                        @Override
                        protected void onFailed() {
                            super.onFailed();
                            selectCarBtn.setText(c.carCode);
                        }
                    });
                } else {
                    selectCarBtn.setText("没有车辆");
                }
                loadLocker.jobFinished();
            }

            @Override
            protected void onFailed() {
                loadLocker.jobFinished();
            }
        });
    }


    private void reSum(){
        try {
            double sum=0;
            for (int i = 0; i < itemListData.size(); i++) {
                double num = Double.parseDouble(((EditText) listView.getChildAt(i).findViewById(R.id.number)).getText().toString().trim());
                double discount = Double.parseDouble(((EditText) listView.getChildAt(i).findViewById(R.id.discount)).getText().toString().trim());
                double price= ((JSONObject) itemListData.get(i).getBaseData()).getDouble("price");
                sum+=price * discount * num;
            }
            priceEditText.setText(""+(((int) (sum * 100))/100.0));
        } catch (NumberFormatException e) {
            priceEditText.setText("");
            e.printStackTrace();
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            priceEditText.setText("");
        }
    }

    @OnClick(R.id.addItemBtn)
    private void addItemBtnClicked(View v) {
        layout = (RelativeLayout) findViewById(R.id.finish_order_layout);
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int height=getWindowManager().getDefaultDisplay().getHeight()-statusBarHeight;
        popupWindow.setHeight(height);
        popupWindow.setWidth(layout.getWidth());
        itemSelector.startWithRootLayer(new ListItem(true, 0, "root", null));
        popupWindow.setContentView(itemSelector);
        popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, 0, statusBarHeight);
    }

    @OnClick(R.id.addCarBtn)
    private void addCarBtnClicked(View v) {
        layout = (RelativeLayout) findViewById(R.id.finish_order_layout);
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int height=getWindowManager().getDefaultDisplay().getHeight()-statusBarHeight;
        popupWindow.setHeight(height);
        popupWindow.setWidth(layout.getWidth());
        carAdder.startWithRootLayer(new ListItem(true, 0, "root", null));
        popupWindow.setContentView(carAdder);
        popupWindow.showAtLocation(carAdder, Gravity.NO_GRAVITY, 0, statusBarHeight);
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
                final Car c = cars.get(which);
                selectedcarid = cars.get(which).id;
                final String carCode = cars.get(which).carCode;
                selectCarBtn.setText("正在读取");
                APIs.getCarClassName(c.classid, Account.getInstance(FinishActivity.this), new NetworkCallback(FinishActivity.this) {
                    @Override
                    protected void onSuccess(JSONObject data) {
                        try {
                            selectCarBtn.setText(carCode + "\n" + data.getString("carclass"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            selectCarBtn.setText(carCode);
                        }
                    }

                    @Override
                    protected void onFailed() {
                        super.onFailed();
                        selectCarBtn.setText(carCode);
                    }
                });
                dialog.dismiss();
            }
        });
        builder.show();
    }


    @OnClick(R.id.confirm)
    private void confirmClicked(View v) {
        String s = priceEditText.getText().toString();
        if (s.length() == 0) {
            MyToast.makeText(this, "价格不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        double price = 0;
        try {
            price = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            MyToast.makeText(this, "价格不正确", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        if (price < 0) {
            MyToast.makeText(this, "价格不能为负", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedcarid==-1){
            MyToast.makeText(this, "请选择用户车辆", Toast.LENGTH_SHORT).show();
            return;
        }
        String distanc=distanceEditText.getText().toString().trim();
        if (distanc.length()==0){
            MyToast.makeText(this, "里程数没有填写", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject goodsObj = new JSONObject();
        try {
            for (int i = 0; i < itemListData.size(); i++) {
                double num = Double.parseDouble(((EditText) listView.getChildAt(i).findViewById(R.id.number)).getText().toString().trim());
                double discount = Double.parseDouble(((EditText) listView.getChildAt(i).findViewById(R.id.discount)).getText().toString().trim());
                if(discount>10 || discount <=0){
                    MyToast.makeText(this, "项目价格不正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    JSONObject object=new JSONObject();
                    object.put("num",num);
                    object.put("discount",discount);
                    goodsObj.put(String.valueOf(itemListData.get(i).getId()), object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (NumberFormatException e) {
            MyToast.makeText(this, "项目价格不正确", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        loadLocker.start("正在完成");

        APIs.finishOrder(orderID, selectedcarid, goodsObj, price
                , clientNameText.getText().toString().trim()
                , clientPhoneText.getText().toString().trim()
                , clientTypeText.getText().toString().trim()
                , remarkText.getText().toString().trim()
                , distanc
                , Account.getInstance(this), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                MyToast.makeText(FinishActivity.this, "完成订单成功", Toast.LENGTH_SHORT).show();
                loadLocker.jobFinished();
                setResult(1);
                finish();
            }

            @Override
            protected void onFailed() {
                loadLocker.jobFinished();
            }
        });
    }
}