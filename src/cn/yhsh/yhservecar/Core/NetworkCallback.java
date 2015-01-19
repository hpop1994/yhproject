package cn.yhsh.yhservecar.Core;

import android.content.Context;
import android.widget.Toast;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/1/19.
 */
public abstract class NetworkCallback {
    private Context context;

    public NetworkCallback(Context context) {
        this.context = context;
    }
    //status 0
    public abstract void onSuccess(JSONObject object);

    //status else and network problems
    public final void onFailed(){

    }

    public final void dealAccountError(){
        Toast.makeText(context, "用户名或密码错误", Toast.LENGTH_SHORT).show();
        onFailed();
    }

    public final void dealClientFormatError(){
        Toast.makeText(context, "客户端发送格式错误", Toast.LENGTH_SHORT).show();
        onFailed();
    }

    public final void dealServerFormatError(){
        Toast.makeText(context, "服务器发送格式错误", Toast.LENGTH_SHORT).show();
        onFailed();
    }

    public final void dealNetworkError(){
        Toast.makeText(context, "网络错误,无法连接到服务器",Toast.LENGTH_SHORT).show();
        onFailed();
    }

    public final void dealUnexpectedError(){
        makeText("未知错误");
        onFailed();
    }

    protected final void makeText(String s){
        Toast.makeText(context, s,Toast.LENGTH_SHORT).show();
    }
}
