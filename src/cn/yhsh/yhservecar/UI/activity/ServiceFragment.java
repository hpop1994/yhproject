package cn.yhsh.yhservecar.UI.activity;

import cn.yhsh.yhservecar.Core.StatusService;

/**
 * Created by Xujc on 2015/1/23.
 */
public interface ServiceFragment {
    public void onConnected(StatusService service);
    public void onDisconnected(StatusService service);
}
