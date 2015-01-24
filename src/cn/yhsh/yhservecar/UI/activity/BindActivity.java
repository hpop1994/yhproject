package cn.yhsh.yhservecar.UI.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.MenuItem;
import cn.yhsh.yhservecar.Core.StatusService;

/**
 * Created by Xujc on 14-2-6.
 */
public abstract class BindActivity extends Activity {

    private StatusService myService;
    private boolean myBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StatusService.LocalBinder binder = (StatusService.LocalBinder) service;
            myService = binder.getService();
            myBound = true;

            BindActivity.this.onServiceConnected(myService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myBound = false;

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected abstract void onServiceConnected(StatusService myService);

    protected abstract void onServiceDisconnected(StatusService myService);

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, StatusService.class), connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (myBound) {
            unbindService(connection);
            myBound = false;
            BindActivity.this.onServiceDisconnected(myService);
        }
    }

    public StatusService getService() {
        return myService;
    }

    public boolean isBind(){
        return myBound;
    }
}
