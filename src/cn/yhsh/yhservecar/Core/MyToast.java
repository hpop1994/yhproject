package cn.yhsh.yhservecar.Core;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Xujc on 2015/5/6 006.
 */
public class MyToast {
    public static Toast makeText(Context context, String s, int lengthShort) {
        Toast result = Toast.makeText(context, s, lengthShort);
        TextView tv = (TextView) ((LinearLayout)result.getView()).getChildAt(0);
        tv.setTextSize(18);
        return result;
    }
}
