package cn.yhsh.yhservecar.UI.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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

    @ViewInject(R.id.give_up)
    private Button giveUpButton;

    @ViewInject(R.id.confirm)
    private Button confirmButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ViewUtils.inject(this);
    }

    @OnClick(R.id.give_up)
    private void giveUpClicked(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("您确认要退出应用么?");
        builder.show();
    }

    @OnClick(R.id.confirm)
    private void confirmClicked(View v) {
        String name = nameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (password.length() < 6 || password.length() > 30) {
            Toast.makeText(this, "密码长度应该在6到30位之间", Toast.LENGTH_SHORT).show();
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