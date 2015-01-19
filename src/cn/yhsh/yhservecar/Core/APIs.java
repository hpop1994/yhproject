package cn.yhsh.yhservecar.Core;

import com.lidroid.xutils.http.RequestParams;

/**
 * Created by Xujc on 2015/1/19.
 */
public class APIs {
    private static final String SERVER = "";

    private static final String LOGIN = "";
    private static final String CHANGE_ACCOUNT_INFO = "";
    private static final String GET_ACCOUNT_INFO = "";
    private static final String CHANGE_PASSWORD = "";
    private static final String SEND_POSITION_AND_LOCAL_STATUS ="" ;
    private static final String GET_STATUS ="" ;
    private static final String CHANGE_STATUS = "";
    private static final String GET_ORDER_DETAIL = "";
    private static final String REPLY_ORDER_REQUEST= "";
    private static final String GET_CAR_CLASS = "";
    private static final String GET_GOOD_CLASS = "";
    private static final String FINISH_ORDER="";
    private static final String CANCEL_ORDER="";


    private static String getUrl(String subUrl) {
        return SERVER+subUrl;
    }

    public static void login(String name,String password,NetworkCallback callback){
        RequestParams params=new RequestParams();
        params.addBodyParameter("name",name);
        params.addBodyParameter("password",password);
        Http.postPublic(getUrl(LOGIN), params, callback);
    }

    public static void changeAccountInfo(String realname,String phone,NetworkCallback callback){
        RequestParams params=new RequestParams();
        params.addBodyParameter("realname",realname);
        params.addBodyParameter("phone",phone);
        Http.postPublic(getUrl(LOGIN), params, callback);
    }
}
