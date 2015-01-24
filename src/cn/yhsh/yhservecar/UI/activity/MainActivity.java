package cn.yhsh.yhservecar.UI.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.yhsh.yhservecar.Core.APIs;
import cn.yhsh.yhservecar.Core.NetworkCallback;
import cn.yhsh.yhservecar.Core.StatusService;
import cn.yhsh.yhservecar.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
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
    final private ServingFragment servingFragment=new ServingFragment();
    private ServiceFragment nowFragment;

    @ViewInject(R.id.name)
    private TextView nameText;

    @ViewInject(R.id.realname)
    private TextView realNameText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ViewUtils.inject(this);

        getFragmentManager().beginTransaction()
                .add(R.id.fragment, waitingFragment)
                .add(R.id.fragment, servingFragment)
                .commit();
        getFragmentManager().beginTransaction()
                .hide(waitingFragment)
                .hide(servingFragment)
                .commit();

    }

    @Override
    protected void onServiceConnected(StatusService myService) {
        myService.setListener(this);
        nameText.setText(myService.getAccount().getName());
        APIs.getAccountInfo(myService.getAccount(), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    realNameText.setText(data.getString("realname"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    dealServerFormatError();
                }
            }
        });
        nowFragment=null;
        getFragmentManager().beginTransaction()
                .hide(waitingFragment)
                .hide(servingFragment)
                .commit();
        goToStatus(myService.getStatus());
        nowFragment.onConnected(myService);
    }

    @Override
    protected void onServiceDisconnected(StatusService myService) {
        myService.setListener(null);
        nowFragment.onDisconnected(myService);
    }

    @Override
    public void connectionChanged() {
        if (nowFragment!=waitingFragment){
            return;
        }
        waitingFragment.connectionChanged();
    }

    @Override
    public void orderListChanged() {
        if (nowFragment!=waitingFragment){
            return;
        }
        waitingFragment.orderChanged();
    }

    @Override
    public void statusChanged() {
        goToStatus(getService().getStatus());
    }

    private void goToStatus(int status){
        if (status==StatusService.OFF_SERVICE && nowFragment!=waitingFragment){
            if (nowFragment!=null){
                getFragmentManager().beginTransaction().hide((Fragment)  nowFragment).commit();
            }
            nowFragment=waitingFragment;
            getFragmentManager().beginTransaction().show(waitingFragment).commit();
        } else if (status==StatusService.WAITING && nowFragment!=waitingFragment){
            if (nowFragment!=null){
                getFragmentManager().beginTransaction().hide((Fragment) nowFragment).commit();
            }
            nowFragment=waitingFragment;
            getFragmentManager().beginTransaction().show(waitingFragment).commit();
        } else if (status==StatusService.SERVING && nowFragment!=servingFragment){
            if (nowFragment!=null){
                getFragmentManager().beginTransaction().hide((Fragment)  nowFragment).commit();
            }
            nowFragment=servingFragment;
            getFragmentManager().beginTransaction().show(servingFragment).commit();
        }
    }

    @OnClick(R.id.my_action_bar)
    private void myActionBarClicked(View v){
        Intent intent=new Intent(this, UserInfoActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.refresh)
    private void refreshClicked(View v){
        if (isBind()){
            getService().getNewestStatus(new StatusService.BackgroundJobListener() {
                @Override
                public void jobSuccess() {
                    if (getService().getStatus()==StatusService.WAITING){
                        getService().getAndNotifyOrders();
                    }
                }

                @Override
                public void jobFailed() {

                }
            });
        }
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
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
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
}