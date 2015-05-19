package cn.yhsh.yhservecar.Core;

/**
 * Created by Xujc on 2015/5/13 013.
 */
public class Status {
    public static final int NEED_TAKEN=0;
    public static final int TAKEN=1;
    public static final int CANCELED=2;
    public static final int FINISHED=3;
    public static final int PRE_ORDER=4;

    public static final String NAME_NEED_TAKEN="待接单";
    public static final String NAME_TAKEN="已接单";
    public static final String NAME_CANCELED="已取消";
    public static final String NAME_FINISHED="已结算";
    public static final String NAME_PRE_ORDER="待接单";

    public static String toName(int status){
        switch (status){
            case NEED_TAKEN:
                return NAME_NEED_TAKEN;
            case TAKEN:
                return NAME_TAKEN;
            case CANCELED:
                return NAME_CANCELED;
            case FINISHED:
                return NAME_FINISHED;
            case PRE_ORDER:
                return NAME_PRE_ORDER;
            default:
                return "错误状态";
        }
    }
}
