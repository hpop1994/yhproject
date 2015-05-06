package cn.yhsh.yhservecar.UI.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.yhsh.yhservecar.Core.MyToast;
import cn.yhsh.yhservecar.Core.StatusService;
import cn.yhsh.yhservecar.R;
import cn.yhsh.yhservecar.UI.component.LoadLocker;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

/**
 * Created by Xujc on 2015/1/21.
 */
public class LoginActivity extends BindActivity {
    @ViewInject(R.id.name)
    private EditText nameEditText;

    @ViewInject(R.id.password)
    private EditText passwordEditText;

    @ViewInject(R.id.confirm)
    private Button confirmButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ViewUtils.inject(this);
    }

    @OnClick(R.id.confirm)
    private void confirmClicked(View v) {
        String name = nameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (password.length() < 6 || password.length() > 30) {
            MyToast.makeText(this, "密码长度应该在6到30位之间", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isBind()) {
            return;
        }
        final LoadLocker loadLocker = new LoadLocker(this);
        loadLocker.setCancalable(false);
        loadLocker.start("正在登陆");
        getService().login(name, password, new StatusService.BackgroundJobListener() {
            @Override
            public void jobSuccess() {
                loadLocker.jobFinished();
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();
            }

            @Override
            public void jobFailed() {
                loadLocker.jobFinished();
            }
        });


    }

    @Override
    protected void onServiceConnected(StatusService myService) {

    }

    @Override
    protected void onServiceDisconnected(StatusService myService) {

    }
}