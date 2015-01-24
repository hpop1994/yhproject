package cn.yhsh.yhservecar.UI.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import cn.yhsh.yhservecar.Core.APIs;
import cn.yhsh.yhservecar.Core.Account;
import cn.yhsh.yhservecar.Core.NetworkCallback;
import cn.yhsh.yhservecar.R;
import cn.yhsh.yhservecar.UI.component.LoadLocker;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/1/16.
 */
public class ChangePasswordAcivity extends Activity {
    @ViewInject(R.id.old_password)
    private EditText oldPasswordText;

    @ViewInject(R.id.new_password)
    private EditText newPasswordText;

    @ViewInject(R.id.password_again)
    private EditText passwordAgainText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_activity);

        ViewUtils.inject(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("更改密码");
    }

    @OnClick(R.id.give_up)
    private void giveUpClicked(View v){
        finish();
    }

    @OnClick(R.id.confirm)
    private void confirmClicked(View v){
        final Account accountInfo = Account.getInstance(this);
        if (!accountInfo.getPassword().equals(oldPasswordText.getText().toString().trim())){
            Toast.makeText(this, "密码错误",Toast.LENGTH_SHORT).show();
            return;
        }
        final String newPassword=newPasswordText.getText().toString().trim();
        String passwordAgain=passwordAgainText.getText().toString().trim();
        if (newPassword.length()<6 || newPassword.length()>30){
            Toast.makeText(this, "密码长度应该在6到30位之间",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(passwordAgain)){
            Toast.makeText(this, "两次输入密码不一致",Toast.LENGTH_SHORT).show();
            return;
        }
        final LoadLocker loadLocker=new LoadLocker(this);
        loadLocker.setCancalable(false);
        loadLocker.start("正在更改...");
        APIs.changePassword(newPassword, accountInfo, new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject object) {
                loadLocker.jobFinished();
                accountInfo.setPassword(newPassword);
                accountInfo.saveStatus();
                makeText("更改密码成功");
                finish();
            }

            @Override
            protected void onFailed() {
                loadLocker.jobFinished();
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}