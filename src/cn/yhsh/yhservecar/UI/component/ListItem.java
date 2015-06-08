package cn.yhsh.yhservecar.UI.component;

/**
 * Created by Xujc on 2015/1/20.
 */
public class ListItem{
    private boolean isNode;
    //可以相同，
    private int id;
    private String itemName;
    private Object baseData;

    public ListItem(boolean isNode, int id, String itemName, Object baseData) {
        this.isNode = isNode;
        this.id = id;
        this.itemName = itemName;
        this.baseData = baseData;
    }

    public boolean isNode() {
        return isNode;
    }

    public int getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public Object getBaseData() {
        return baseData;
    }
}
