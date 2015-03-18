package cn.yhsh.yhservecar.Core;

import android.util.Log;
import com.lidroid.xutils.http.RequestParams;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/1/19.
 */
public class APIs {
    private static final String SERVER = "http://app.lhkb.cn:88/index.php/Server/";
//    private static final String SERVER = "http://192.168.1.99/www/index.php/Server/";

    //01
    private static final String LOGIN = "Sget/login";
    //02
    private static final String CHANGE_ACCOUNT_INFO = "Sverify/changeinfo";
    //03
    private static final String GET_ACCOUNT_INFO = "Sverify/getinfo";
    //04
    private static final String CHANGE_PASSWORD = "Sverify/changepassword";
    //05
    private static final String SEND_POSITION ="Sverify/synclocation" ;
    //06
    private static final String GET_STATUS ="Sverify/getstatus" ;
    //07
    private static final String BECOME_OFFLINE = "Sverify/logout";
    //08
    private static final String GET_ORDER_DETAIL = "Sverify/getindent";
    //09
    private static final String REPLY_ORDER_REQUEST= "Sverify/answerindent";
    //10
    private static final String GET_CAR_CLASS = "Sverify/getcarclass";
    //11
    private static final String GET_GOOD_CLASS = "Sverify/getgoods";
    //12
    private static final String FINISH_ORDER="Sverify/finishindent";
    //13
    private static final String CANCEL_ORDER="Sverify/cancelindent";
    //14
    private static final String GET_USER_CARS="Sverify/getusercars";
    //15
    private static final String ADD_USER_CAR="Sverify/addusercar";
    //16
    private static final String GET_ORDER_FOR_ME="Sverify/checkreqindent";
    //17
    private static final String UPDATE_GETUI_ID="Sverify/updategetuicid";
    //18
    private static final String GET_TAKEN_ORDERS="Sverify/checkansindent";
    //19
    private static final String SERVE_ORDER="Sverify/serveorder";
    //20
    private static final String CHANGE_APPOINTMENT_TIME="Sverify/changeordertime";
    //21
    private static final String GET_SERVING_ORDERS = "Sverify/checkservingindent";

    private static String getUrl(String subUrl) {
        return SERVER+subUrl;
    }

    //01
    public static void login(String name,String password,NetworkCallback callback){
        Log.i("POST_BACK","USING 1 : login()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("name",name);
        params.addBodyParameter("password",password);
        Http.postPublic(getUrl(LOGIN), params, callback);
    }

    //02
    public static void changeAccountInfo(String realname,String phone,Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 2 : changeAccountInfo()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("realname",realname);
        params.addBodyParameter("phonenum",phone);
        Http.postLogined(getUrl(CHANGE_ACCOUNT_INFO), params, account,callback);
    }

    //03
    public static void getAccountInfo(Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 3 : getAccountInfo()");
        Http.postLogined(getUrl(GET_ACCOUNT_INFO),account, callback);
    }

    //04
    public static void changePassword(String newpassword,Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 4 : changePassword()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("newpassword",newpassword);
        Http.postLogined(getUrl(CHANGE_PASSWORD), params,account,callback);
    }

    //05
    public static void sendLocation(double lat, double lon,Account account, NetworkCallback callback){
        Log.i("POST_BACK","USING 5 : sendLoaction()");
        RequestParams params = new RequestParams();
        params.addBodyParameter("lat", String.valueOf(lat));
        params.addBodyParameter("lon", String.valueOf(lon));
        Http.postLogined(getUrl(SEND_POSITION),params, account, callback);
    }

    //06
    public static void getStatus(Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 6 : getStatus()");
        Http.postLogined(getUrl(GET_STATUS),account,callback);
    }

    //07
    public static void becomeOffline(Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 7 : becomeOffline()");
        Http.postLogined(getUrl(BECOME_OFFLINE),account, callback);
    }

    //08
    public static void getOrderDetail(int orderID,Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 8 : getOrderDetail()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("order_id", String.valueOf(orderID));
        Http.postLogined(getUrl(GET_ORDER_DETAIL),params,account, callback);
    }

    //09
    public static void replyOrderRequest(int orderID, boolean taken, Account account, NetworkCallback callback){
        Log.i("POST_BACK","USING 9 : replyOrderRequest()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("order_id", String.valueOf(orderID));
        params.addBodyParameter("taken", String.valueOf(taken));
        Http.postLogined(getUrl(REPLY_ORDER_REQUEST),params,account, callback);
    }

    //10
    public static void getCarClass(int pid,Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 10 : getCarClass()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("pid",String.valueOf(pid));
        Http.postLogined(getUrl(GET_CAR_CLASS),params,account, callback);
    }

    //11
    public static void getGoodClass(int pid,Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 11 : getGoodClass()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("pid", String.valueOf(pid));
        Http.postLogined(getUrl(GET_GOOD_CLASS), params, account, callback);
    }

    //12
    public static void finishOrder(
            int orderID,int carID,JSONObject goods
            ,double price,String name,String phone
            ,String type,String remark,String distance
            ,Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 12 : finishOrder()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("order_id", String.valueOf(orderID));
        params.addBodyParameter("carid", String.valueOf(carID));
        params.addBodyParameter("goods",goods.toString());
        params.addBodyParameter("price", String.valueOf(price));
        params.addBodyParameter("client_name",name);
        params.addBodyParameter("client_phone",phone);
        params.addBodyParameter("client_type",type);
        params.addBodyParameter("remark",remark);
        params.addBodyParameter("distance",distance);
        Http.postLogined(getUrl(FINISH_ORDER),params,account,callback);
    }

    //13
    public static void cancelOrder(int orderID,Account account,NetworkCallback callback) {
        Log.i("POST_BACK","USING 13 : cancelOrder()");
        RequestParams params = new RequestParams();
        params.addBodyParameter("order_id", String.valueOf(orderID));
        Http.postLogined(getUrl(CANCEL_ORDER),params, account, callback);
    }

    //14
    public static void getUserCars(int uid,Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 14 : getUserCars()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("uid", String.valueOf(uid));
        Http.postLogined(getUrl(GET_USER_CARS),params, account, callback);
    }

    //15
    public static void addUserCar(int uid,int carClass,String carCode,String desc
            ,Account account,NetworkCallback callback) {
        Log.i("POST_BACK","USING 15 : addUserCar()");
        RequestParams params = new RequestParams();
        params.addBodyParameter("uid", String.valueOf(uid));
        params.addBodyParameter("class", String.valueOf(carClass));
        params.addBodyParameter("carid", carCode);
        params.addBodyParameter("desc",desc);
        Http.postLogined(getUrl(ADD_USER_CAR),params, account, callback);
    }

    //16
    public static void getOrderForMe(Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 16 : getOrderForMe()");
        Http.postLogined(getUrl(GET_ORDER_FOR_ME),account,callback);
    }

    //17
    public static void updateGetuiID(String getuiID,Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 17 : updateGetuiID()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("getuicid",getuiID);
        Http.postLogined(getUrl(UPDATE_GETUI_ID),params,account,callback);
    }

    //18
    public static void getTakenOrders(Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 18 : getTakenOrders()");
        Http.postLogined(getUrl(GET_TAKEN_ORDERS),account,callback);
    }

    //19
    public static void serveOrder(int orderID,Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 19 : serveOrder()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("order_id", String.valueOf(orderID));
        Http.postLogined(getUrl(SERVE_ORDER),params,account,callback);
    }

    //20
    public static void changeAppointmentTime(int orderID,String newTime,Account account,NetworkCallback callback){
        Log.i("POST_BACK","USING 20 : changeAppointmentTime()");
        RequestParams params=new RequestParams();
        params.addBodyParameter("order_id", String.valueOf(orderID));
        params.addBodyParameter("appointment_time", newTime);
        Http.postLogined(getUrl(CHANGE_APPOINTMENT_TIME),params,account,callback);
    }

    //21
    public static void getServingOrders(Account account, NetworkCallback callback) {
        Log.i("POST_BACK","USING 21 : getServingOrders()");
        Http.postLogined(getUrl(GET_SERVING_ORDERS),account,callback);
    }
}
