package cn.yhsh.yhservecar.Core.entry;

import cn.yhsh.yhservecar.Core.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Xujc on 2015/5/13 013.
 */
public class OrderList {
    public static class Entry{
        public String id;
        public String serialId;
        public int status;
        public String clientName;
        public String address;
        public String appointmentTime;
        public String appointmentItem;

        public String carSerialID;
    }

    public ArrayList<Entry> list=new ArrayList<Entry>();

    public static OrderList getOrderList(JSONObject object) throws JSONException{
        OrderList orderList=new OrderList();

        JSONArray indentArray= null;
        try {
            indentArray = object.getJSONArray("indent");
        } catch (JSONException e) {
            return orderList;
        }
        for (int i=0;i<indentArray.length();i++){
            JSONObject entryObject=indentArray.getJSONObject(i);
            Entry entry=new Entry();
            entry.id=entryObject.getString("id");
            entry.serialId=entryObject.getString("iid");
            entry.status= Integer.parseInt(entryObject.getString("status"));
            entry.clientName=entryObject.getString("username");
            entry.address=entryObject.getString("address");
            entry.appointmentTime=entryObject.getString("time");
            entry.appointmentItem=entryObject.getString("item");

            entry.carSerialID=entryObject.getJSONObject("car").getString("carid");
            if (entry.status == Status.FINISHED){
                entry.carSerialID=entryObject.getJSONObject("history").getJSONObject("car").getString("carid");
            }
            orderList.list.add(entry);
        }


        return orderList;
    }
}