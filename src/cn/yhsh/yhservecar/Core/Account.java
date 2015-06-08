package cn.yhsh.yhservecar.Core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Xujc on 2015/1/19.
 */
public class Account {
    private static Account instance=null;
    public static Account getInstance(Context context) {
        if (instance==null){
            instance=new Account(context);
        }
        return instance;
    }
    private Account(Context context){
        this.context=context;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        logined = sp.getBoolean("ACCOUNT_logined", false);
        if (logined) {
            id = sp.getString("ACCOUNT_id", "");
            name = sp.getString("ACCOUNT_name", "");
            password = sp.getString("ACCOUNT_password", "");
        }
    }

    private Context context;

    private boolean logined=false;
    private String  id;
    private String name;
    private String password;


    public void login(String id,String name,String password){
        logined=true;
        this.id=id;
        this.name=name;
        this.password=password;
        saveStatus();
    }

    public void logout(){
        logined=false;
        id="";
        name="";
        password="";
        saveStatus();
    }

    public void saveStatus(){
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("ACCOUNT_logined", logined)
                .putString("ACCOUNT_id", id)
                .putString("ACCOUNT_name", name)
                .putString("ACCOUNT_password", password)
                .apply();
    }

    public boolean isLogined() {
        return logined;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
