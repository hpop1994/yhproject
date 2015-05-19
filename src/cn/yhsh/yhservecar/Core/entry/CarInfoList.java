package cn.yhsh.yhservecar.Core.entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Xujc on 2015/5/13 013.
 */
public class CarInfoList {
    public static class Entry{
        public String id;
        public String carSerialId;
        public String carClass;
    }
    public ArrayList<Entry> list=new ArrayList<Entry>();

    public static CarInfoList getCarInfoList(JSONObject object) throws JSONException{
        CarInfoList carInfoList=new CarInfoList();
        JSONArray cars=object.getJSONArray("cars");
        for (int i=0;i<cars.length();i++){
            JSONObject aCar=cars.getJSONObject(i);
            Entry entry=new Entry();
            entry.id=aCar.getString("id");
            entry.carSerialId=aCar.getString("carid");
            entry.carClass=aCar.getString("name");
            carInfoList.list.add(entry);
        }
        return carInfoList;
    }
}
