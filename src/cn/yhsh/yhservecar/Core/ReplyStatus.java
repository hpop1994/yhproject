package cn.yhsh.yhservecar.Core;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Xujc on 2015/1/16.
 */
public class ReplyStatus {
    public static final int SUCCESS = 0;
    public static final int GENERAL_FAILED = 1;
    public static final int UNEXPECTED_STATUS = 2;

    public static final int PASSWORD_FAILED = 3;
    public static final int USER_NOT_EXIST = 4;
    public static final int INPUT_FORMAT_ERROR = 5;


    public static void check(JSONObject object,NetworkCallback viewCallback) {
        try {
            int status_code = object.getInt("status_code");
            switch (status_code) {
                case SUCCESS:
                    viewCallback.onSuccess(object);
                    break;
                case GENERAL_FAILED:
                    viewCallback.onFailed();
                    break;
                case PASSWORD_FAILED:
                case USER_NOT_EXIST:
                    viewCallback.dealAccountError();
                    break;
                case UNEXPECTED_STATUS:
                    viewCallback.dealUnexpectedError();
                case INPUT_FORMAT_ERROR:
                    viewCallback.dealClientFormatError();
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            viewCallback.dealServerFormatError();
        }
    }
}
