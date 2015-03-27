package cn.yhsh.yhservecar.UI.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import cn.yhsh.yhservecar.Core.APIs;
import cn.yhsh.yhservecar.Core.Account;
import cn.yhsh.yhservecar.Core.NetworkCallback;
import cn.yhsh.yhservecar.Core.StatusService;
import cn.yhsh.yhservecar.R;
import cn.yhsh.yhservecar.UI.component.LoadLocker;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/1/21.
 */
public class UserInfoActivity extends BindActivity {
    @ViewInject(R.id.name)
    private TextView nameText;

    @ViewInject(R.id.name_edit)
    private EditText nameEdit;

    @ViewInject(R.id.realname)
    private TextView realnameText;

    @ViewInject(R.id.phone)
    private TextView phoneText;

    @ViewInject(R.id.phone_edit)
    private EditText phoneEdit;

    @ViewInject(R.id.edit)
    private ImageButton editButton;

    private boolean editing = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_activity);
        ViewUtils.inject(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("用户信息");

        APIs.getAccountInfo(Account.getInstance(this), new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                try {
                    Account account = Account.getInstance(UserInfoActivity.this);
                    nameText.setText(account.getName());
                    realnameText.setText(data.getString("realname"));
                    phoneText.setText(data.getString("phonenum"));
                } catch (JSONException e) {
                    dealServerFormatError();
                    e.printStackTrace();
                }
            }
        });
    }

    @OnClick(R.id.change_password)
    private void changePasswordClicked(View v) {
        startActivity(new Intent(this, ChangePasswordAcivity.class));
    }

    @OnClick(R.id.edit)
    private void editClicked(View v) {
        if (!editing) {
            editing = true;
            editButton.setImageResource(R.drawable.ok);
            realnameText.setVisibility(View.GONE);
            phoneText.setVisibility(View.GONE);
            nameEdit.setVisibility(View.VISIBLE);
            phoneEdit.setVisibility(View.VISIBLE);
            nameEdit.setText(realnameText.getText());
            phoneEdit.setText(phoneText.getText());
        } else {
            final String newName = nameEdit.getText().toString().trim();
            if (newName.length() == 0) {
                Toast.makeText(this, "不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            final String newPhone = phoneEdit.getText().toString().trim();
            if (newPhone.length() == 0) {
                Toast.makeText(this, "电话不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            editButton.setImageResource(R.drawable.edit);
            final LoadLocker loadLocker = new LoadLocker(this);
            loadLocker.setCancalable(false);
            loadLocker.start("正在更改");
            APIs.changeAccountInfo(newName, newPhone, Account.getInstance(this), new NetworkCallback(this) {
                @Override
                protected void onSuccess(JSONObject object) {
                    loadLocker.jobFinished();
                    switchMode();
                    realnameText.setText(newName);
                    phoneText.setText(newPhone);

                }

                private void switchMode() {
                    nameEdit.setVisibility(View.GONE);
                    realnameText.setVisibility(View.VISIBLE);
                    phoneEdit.setVisibility(View.GONE);
                    phoneText.setVisibility(View.VISIBLE);
                    editing = false;
                }

                @Override
                protected void onFailed() {
                    loadLocker.jobFinished();
                    switchMode();
                }
            });

        }
    }

    @OnClick(R.id.logout)
    private void logoutClicked(View v) {
        if (!isBind()) {
            return;
        }
        if (!getService().getOrdersAsked().isEmpty()) {
            Toast.makeText(this, "有尚未处理的订单", Toast.LENGTH_SHORT).show();
            return;
        }

        getService().logout();
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    protected void onServiceConnected(StatusService myService) {

    }

    @Override
    protected void onServiceDisconnected(StatusService myService) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED,null);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}