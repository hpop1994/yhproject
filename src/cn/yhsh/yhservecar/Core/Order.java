package cn.yhsh.yhservecar.Core;

/**
 * Created by Xujc on 2015/1/21.
 */
public class Order {
    public int orderID;
    public int uid;
    public String name;
    public String phone;
    public String address;
    public String time;
    public double lat;
    public double lon;
    public String item;
    public String appointmentTime;

    public int inTime =0;

    @Override
    public boolean equals(Object o) {
        return orderID==((Order)o).orderID;
    }

}
