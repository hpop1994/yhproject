package cn.yhsh.yhservecar.Core.entry;

import cn.yhsh.yhservecar.Core.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Xujc on 2015/5/13 013.
 */
public class OrderDetail {
    public String id;
    public String uid;
    public int status;
    public String serialId;
    public String clientName;
    public String clientPhone;
    public String clientType;
    public String address;
    public double lat;
    public double lon;
    public String makeOrderTime;
    public String appointmentTime;
    public String appointmentItem;
    public String cancelReason;

    //taken

    //finished
    public String remark;
    public ArrayList<ActualCareItemEntry> actualCareItemList;
    public String actualPrice;
    public String carClass;
    public String carSerialID;
    public String nowKm;
    public String nextKm;

    public static class ActualCareItemEntry{
        public String name;
        public String price;
        public String number;
        public String unit;
        public String discount;
    }

    public static OrderDetail getOrderDetail(JSONObject object) throws JSONException{
        OrderDetail orderDetail=new OrderDetail();

        orderDetail.id=object.getString("realname");
        orderDetail.uid=object.getString("uid");
        orderDetail.status=object.getInt("status");
        orderDetail.serialId=object.getString("iid");
        orderDetail.clientName=object.getString("realname");
        orderDetail.clientPhone=object.getString("phonenum");
        orderDetail.clientType=object.getString("class");
        orderDetail.address=object.getString("address");
        orderDetail.lat=object.getDouble("lat");
        orderDetail.lon=object.getDouble("lon");
        orderDetail.makeOrderTime=object.getString("ordertime");
        orderDetail.appointmentTime=object.getString("time");
        orderDetail.appointmentItem=object.getString("item");
        orderDetail.cancelReason=object.getString("refuse");

        if (orderDetail.status== Status.FINISHED){
            JSONObject history = object.getJSONObject("history");
            JSONObject user = history.getJSONObject("user");
            orderDetail.clientName=user.getString("realname");
            orderDetail.clientPhone=user.getString("phonenum");
            orderDetail.clientType=user.getString("class");

            orderDetail.remark=history.getString("remark");
            ArrayList<ActualCareItemEntry> actualCareItemEntries = new ArrayList<ActualCareItemEntry>();
            JSONArray itemArray=history.getJSONArray("goods");
            for (int i=0;i<itemArray.length();i++){
                JSONObject aItem=itemArray.getJSONObject(i);
                ActualCareItemEntry entry=new ActualCareItemEntry();
                entry.name=aItem.getString("name");
                entry.price=aItem.getString("price");
                entry.number=aItem.getString("num");
                entry.unit=aItem.getString("unit");
                entry.discount=aItem.getString("discount");
                actualCareItemEntries.add(entry);
            }
            orderDetail.actualCareItemList= actualCareItemEntries;
            orderDetail.actualPrice=history.getString("price");
            JSONObject carObject=history.getJSONObject("car");
            orderDetail.carClass = carObject.getString("classname");
            orderDetail.carSerialID=carObject.getString("carid");
            orderDetail.nowKm=history.getJSONObject("indent").getString("km");
            orderDetail.nextKm=history.getJSONObject("indent").getString("nextkm");
        }

        return orderDetail;
    }
}
