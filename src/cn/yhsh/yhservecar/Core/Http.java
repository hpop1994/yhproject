package cn.yhsh.yhservecar.Core;

import android.util.Log;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/1/19.
 */
public class Http {
    private  static HttpUtils utils = new HttpUtils();
    public static void get(String url, final NetworkCallback viewCallback) {
        utils.send(HttpRequest.HttpMethod.GET, url, getCallback(viewCallback));
    }

    public static void postPublic(String url,final NetworkCallback viewCallback){
        RequestParams params=new RequestParams();
        postPublic(url, params, viewCallback);
    }

    public static void postPublic(String url, RequestParams params, final NetworkCallback viewCallback) {
        utils.send(HttpRequest.HttpMethod.POST, url, params, getCallback(viewCallback));
    }

    public static void postLogined(String url, RequestParams params, Account account, final NetworkCallback viewCallback) {
        HttpUtils utils = new HttpUtils();
        params.addBodyParameter("id", String.valueOf(account.getId()));
        params.addBodyParameter("name", account.getName());
        params.addBodyParameter("password", account.getPassword());

        utils.send(HttpRequest.HttpMethod.POST, url, params, getCallback(viewCallback));
    }

    public static void postLogined(String url, Account account, final NetworkCallback viewCallback) {
        HttpUtils utils = new HttpUtils();
        RequestParams params = new RequestParams();
        postLogined(url, params, account, viewCallback);
    }

    private static RequestCallBack<String> getCallback(final NetworkCallback viewCallback) {
        return new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                //todo : delete;
                Log.i("POST", responseInfo.result);
                try {
                    JSONObject object = new JSONObject(responseInfo.result);
                    ReplyStatus.check(object, viewCallback);
                } catch (JSONException e) {
                    e.printStackTrace();
                    viewCallback.dealServerFormatError();
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                viewCallback.dealNetworkError();
            }
        };
    }
}
