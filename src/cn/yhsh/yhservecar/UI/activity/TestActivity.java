package cn.yhsh.yhservecar.UI.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.*;
import cn.yhsh.yhservecar.Core.APIs;
import cn.yhsh.yhservecar.Core.Account;
import cn.yhsh.yhservecar.Core.NetworkCallback;
import cn.yhsh.yhservecar.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Xujc on 2015/1/20.
 */
public class TestActivity extends Activity {
    private Account account;
    private int nextToTest = 0;
    private ListView listView;
    private EditText editText1;
    private EditText editText2;
    private EditText editText3;
    private EditText editText4;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button acButton;

    private Button addButton;
    private EditText addEditText;

    private AlertDialog dialog;
    private EditText which;

    private ArrayList<String> array = new ArrayList<String>();

    //01
    private static final int LOGIN = 1;
    //02
    private static final int CHANGE_ACCOUNT_INFO = 2;
    //03
    private static final int GET_ACCOUNT_INFO = 3;
    //04
    private static final int CHANGE_PASSWORD = 4;
    //05
    private static final int SEND_POSITION = 5;
    //06
    private static final int GET_STATUS = 6;
    //07
    private static final int BECOME_OFFLINE = 7;
    //08
    private static final int GET_ORDER_DETAIL = 8;
    //09
    private static final int REPLY_ORDER_REQUEST = 9;
    //10
    private static final int GET_CAR_CLASS = 10;
    //11
    private static final int GET_GOOD_CLASS = 11;
    //12
    private static final int FINISH_ORDER = 12;
    //13
    private static final int CANCEL_ORDER = 13;
    //14
    private static final int GET_USER_CARS = 14;
    //15
    private static final int ADD_USER_CAR = 15;
    //16
    private static final int GET_ORDER_FOR_ME = 16;
    //17
    private static final int UPDATE_GETUI_ID=17;

    private static final String[] name = {
            "1:LOGIN \n" +
                    "\t(String name, String password)",
            "2:CHANGE_ACCOUNT_INFO \n" +
                    "\t(String realname, String phone)",
            "3:GET_ACCOUNT_INFO \n" +
                    "\t()",
            "4:CHANGE_PASSWORD \n" +
                    "\t(String newpassword)",
            "5:SEND_POSITION \n" +
                    "\t(double lat, double lon)",
            "6:GET_STATUS \n" +
                    "\t()",
            "7:BECOME_OFFLINE \n" +
                    "\t()",
            "8:GET_ORDER_DETAIL \n" +
                    "\t(int orderID)",
            "9:REPLY_ORDER_REQUEST \n" +
                    "\t(int orderID, boolean taken)",
            "10:GET_CAR_CLASS \n" +
                    "\t(int pid)",
            "11:GET_GOOD_CLASS \n" +
                    "\t(int pid)",
            "12:FINISH_ORDER \n"+
                    "\t(int orderID, int carID, JSONArray goods ,double price)",
            "13:CANCEL_ORDER \n" +
                    "\t(int orderID)",
            "14:GET_USER_CARS\n" +
                    "\t(int uid)",
            "15:ADD_USER_CAR\n" +
                    "\t(int uid,int carClass,String carCode,String desc)",
            "16:GET_ORDER_FOR_ME\n" +
                    "\t()",
            "17:UPDATE_GETUI_ID\n" +
                    "\t(String getuiID)"

    };
    private NetworkCallback networkCallback;
    private DialogInterface.OnClickListener listener;
    private AlertDialog.Builder builder;
    private DialogInterface.OnClickListener deleteListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        listView = (ListView) findViewById(R.id.listView2);
        editText1 = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        editText4 = (EditText) findViewById(R.id.editText4);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        addButton = (Button) findViewById(R.id.addbutton);
        addEditText = (EditText) findViewById(R.id.addEditText);
        acButton = (Button) findViewById(R.id.acButton);
        account = Account.getInstance(this);
        acButton.setText(account.isLogined() ? "in" : "out");
        acButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account.isLogined()) {
                    account.logout();
                    acButton.setText("out");
                } else {
                    final String name = editText1.getText().toString().trim();
                    final String password = editText2.getText().toString().trim();
                    APIs.login(name, password, new NetworkCallback(TestActivity.this) {
                        @Override
                        protected void onSuccess(JSONObject data) {
                            try {
                                account.login(data.getString("id"), name, password);
                                acButton.setText("in");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
        acButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(TestActivity.this, "id:" + account.getId() + " name:" + account.getName() + " pwd:" + account.getPassword(), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        Button clearBtn = (Button) findViewById(R.id.button);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText1.setText("");
                editText2.setText("");
                editText3.setText("");
                editText4.setText("");
            }
        });
        clearBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(TestActivity.this, StartActivity.class));
                return true;
            }
        });
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> set = sharedPreferences.getStringSet("list", new LinkedHashSet<String>());
        array.addAll(set);
        builder = new AlertDialog.Builder(this);
        listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TestActivity.this.which.setText(array.get(which));
                dialog.dismiss();
            }
        };
        builder.setItems(array.toArray(new String[array.size()]), listener);
        dialog = builder.create();


        builder.setItems(array.toArray(new String[array.size()]), listener);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                array.add(addEditText.getText().toString());
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(TestActivity.this);
                TreeSet<String> set = new TreeSet<String>(array);
                sp.edit().putStringSet("list", set).commit();
                builder.setItems(array.toArray(new String[array.size()]), listener);
                dialog = builder.create();
                addEditText.setText("");
            }
        });
        deleteListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                array.remove(which);
                builder.setItems(array.toArray(new String[array.size()]), listener);
                TestActivity.this.dialog = builder.create();
                dialog.dismiss();
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(TestActivity.this);
                TreeSet<String> set = new TreeSet<String>(array);
                sp.edit().putStringSet("list", set).commit();
            }
        };
        addButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                builder.setItems(array.toArray(new String[array.size()]), deleteListener);
                builder.show();
                return true;
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                which = editText1;
                dialog.show();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                which = editText2;
                dialog.show();
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                which = editText3;
                dialog.show();
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                which = editText4;
                dialog.show();
            }
        });

        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.test_method_item, R.id.textView2, name));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                nextToTest = position + 1;
                try {
                    test();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        networkCallback = new NetworkCallback(this) {
            @Override
            protected void onSuccess(JSONObject data) {
                makeLog("-------------SUCCESS----------------");
            }

            @Override
            protected void onFailed() {
                super.onFailed();
                makeLog("--------------FAILED-----------");
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    private void test() throws Exception {
        String s1 = editText1.getText().toString().trim();
        String s2 = editText2.getText().toString().trim();
        String s3 = editText3.getText().toString().trim();
        String s4 = editText4.getText().toString().trim();

        makeLog("================START==================");
        logWhatIsTested(s1, s2, s3, s4);
        switch (nextToTest) {
            case LOGIN:
                APIs.login(s1, s2, getCallback());
                break;
            case CHANGE_ACCOUNT_INFO:
                APIs.changeAccountInfo(s1, s2, account, getCallback());
                break;
            case GET_ACCOUNT_INFO:
                APIs.getAccountInfo(account, getCallback());
                break;
            case CHANGE_PASSWORD:
                APIs.changePassword(s1, account, getCallback());
                break;
            case SEND_POSITION:
                APIs.sendLocation(Double.valueOf(s1),Double.valueOf(s2),account, getCallback());
                break;
            case GET_STATUS:
                APIs.getStatus(account, getCallback());
                break;
            case BECOME_OFFLINE:
                APIs.becomeOffline(account, getCallback());
                break;
            case GET_ORDER_DETAIL:
                APIs.getOrderDetail(Integer.parseInt(s1), account, getCallback());
                break;
            case REPLY_ORDER_REQUEST:
                APIs.replyOrderRequest(Integer.parseInt(s1), Boolean.parseBoolean(s2), account, getCallback());
                break;
            case GET_CAR_CLASS:
                APIs.getCarClass(Integer.parseInt(s1), account, getCallback());
                break;
            case GET_GOOD_CLASS:
                APIs.getGoodClass(Integer.parseInt(s1), account, getCallback());
                break;
            case FINISH_ORDER:
                APIs.finishOrder(Integer.parseInt(s1), Integer.parseInt(s2), new JSONArray(), Integer.parseInt(s3), account, getCallback());
                break;
            case CANCEL_ORDER:
                APIs.cancelOrder(Integer.parseInt(s1), account, getCallback());
                break;
            case GET_USER_CARS:
                APIs.getUserCars(Integer.parseInt(s1), account, getCallback());
                break;
            case ADD_USER_CAR:
                APIs.addUserCar(Integer.parseInt(s1), Integer.parseInt(s2), s3, s4, account, getCallback());
                break;
            case GET_ORDER_FOR_ME:
                APIs.getOrderForMe(account, getCallback());
                break;
            case UPDATE_GETUI_ID:
                APIs.updateGetuiID(s1,account, getCallback());
                break;
        }

    }

    private void login() {
        account.login("2", "222", "123456");
    }

    private void logout() {
        account.logout();
    }

    private void logWhatIsTested(String s1, String s2, String s3, String s4) {
        Log.i("POST_BACK", "Testing : "+ name[nextToTest - 1] + "(" + s1 + "," + s2 + "," + s3 + "," + s4 + ")");
    }

    private void makeLog(String s) {
        Log.i("POST_BACK", s);
    }

    private NetworkCallback getCallback() {

        return networkCallback;
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        TreeSet<String> set = new TreeSet<String>(array);
        sp.edit().putStringSet("list", set).commit();
        super.onDestroy();
    }
}