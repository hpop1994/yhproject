package cn.yhsh.yhservecar.UI.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import cn.yhsh.yhservecar.Core.StatusService;
import cn.yhsh.yhservecar.R;

public class StartActivity extends BindActivity  {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        startService(new Intent(this,StatusService.class));
    }


    @Override
    protected void onServiceConnected(StatusService myService) {
        final Intent next;
        if (myService.getAccount().isLogined()){
            next=new Intent(this,MainActivity.class);
        } else {
            next=new Intent(this,LoginActivity.class);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(next);
                finish();
            }
        }, 2000);
    }

    @Override
    protected void onServiceDisconnected(StatusService myService) {
    }
}
