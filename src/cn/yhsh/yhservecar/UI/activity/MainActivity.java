package cn.yhsh.yhservecar.UI.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import cn.yhsh.yhservecar.Core.*;
import cn.yhsh.yhservecar.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Xujc on 2015/1/22.
 */
public class MainActivity extends BindActivity implements StatusService.StatusListener {
    final private WaitingFragment waitingFragment=new WaitingFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ViewUtils.inject(this);

        getFragmentManager().beginTransaction()
                .add(R.id.fragment, waitingFragment)
                .commit();

        APIs.getVersion(new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    int version = Integer.parseInt(data.getString("version"));

                    PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                    int nowVersion = pi.versionCode;

                    if (nowVersion < version) {
                        UpdateManager manager = new UpdateManager(MainActivity.this);
                        manager.checkUpdateInfo();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @Override
    protected void onServiceConnected(StatusService myService) {
        myService.setListener(this);
        waitingFragment.onConnected(myService);
    }

    @Override
    protected void onServiceDisconnected(StatusService myService) {
        myService.setListener(null);
        waitingFragment.onDisconnected(myService);
    }

    @Override
    public void connectionChanged() {
        waitingFragment.connectionChanged();
    }

    @Override
    public void orderListChanged() {
        waitingFragment.orderChanged();
    }

    @Override
    public void statusChanged() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            exitBy2Click();      //调用双击退出函数
        }
        return false;
    }
    /**
     * 双击退出函数
     */
    private Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            MyToast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
        }
    }

    @OnClick(R.id.taken_orders)
    private void takenOrdersClicked(View v){
        startActivity(new Intent(this, OrderListActivity.class));
    }

}