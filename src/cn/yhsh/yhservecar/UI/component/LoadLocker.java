package cn.yhsh.yhservecar.UI.component;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

/**
 * Created by Xujc on 2015/1/16.
 */
public class LoadLocker {
    private Context context;
    private boolean userCanceled;
    private boolean cancalable=true;

    private ProgressDialog dialog;

    public LoadLocker(Context context) {
        this.context = context;
        dialog=new ProgressDialog(context);
        userCanceled=false;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK) {
                    userCanceled=true;
                    return cancalable;
                } else {
                    return false;
                }
            }
        });
    }

    public void start(String msg){
        userCanceled=false;
        dialog.setMessage(msg);
        dialog.show();
    }

    public void jobFinished(){
        if (dialog.isShowing()) {
            dialog.cancel();
        }
    }

    public void setCancalable(boolean b){
        cancalable=b;
    }

    public boolean isUserCanceled() {
        return userCanceled;
    }
}
