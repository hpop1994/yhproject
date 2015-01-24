package cn.yhsh.yhservecar.UI.component;

import android.content.Context;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cn.yhsh.yhservecar.R;
import com.todddavies.components.progressbar.ProgressWheel;

import java.util.ArrayList;

/**
 * Created by Xujc on 2015/1/20.
 */
public class LayerSelectorView extends RelativeLayout {
    private final LinearLayout layerIndicator;
    private final ListView listView;
    private final ProgressWheel progressWheel;
    private final HorizontalScrollView scrollView;

    private LayerSelectorListener mListener;

    private final ArrayList<ListItem> mCurrentData=new ArrayList<ListItem>();

    private ItemDrawer mItemDrawer;

    private final ListAdapter mAdapter;

    private int nowLayer=-1;

    private final ArrayList<Pair<String,ListItem>> indicatorData=new ArrayList<Pair<String, ListItem>>();
    private Runnable r;

    public LayerSelectorView(final Context context) {
        super(context);

        inflate(context, R.layout.layer_selector,this);
        layerIndicator= (LinearLayout) findViewById(R.id.layer_indicator);
        listView=(ListView) findViewById(R.id.listView);
        progressWheel=(ProgressWheel) findViewById(R.id.pw_spinner);
        scrollView=(HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        r = new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        };

        mAdapter=new ListAdapter();
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item=mAdapter.getItem(position);
                if (item.isNode()){
                    addIndicator(item.getItemName(), item);
                    mListener.onAskForLayerData(item,mCurrentData);
                } else {
                    mListener.onSelectLeaf(item, mCurrentData, position);
                }
            }
        });
    }

    private void addIndicator(String itemName, final ListItem listItem) {
        nowLayer++;
        indicatorData.add(new Pair<String, ListItem>(itemName,listItem));

        final int thisLayerNum=nowLayer;

        inflate(getContext(), R.layout.layer_indicator_item,layerIndicator);
        View v=layerIndicator.getChildAt(nowLayer);
        TextView nameText=(TextView) v.findViewById(R.id.name);
        nameText.setText(itemName);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoLayer(listItem,thisLayerNum);
            }
        });
        Handler handler = new Handler();

        handler.post(r);

    }

    private void setUpRoodIndicator(final ListItem item) {
        nowLayer=0;
        indicatorData.add(new Pair<String, ListItem>("root",item));

        inflate(getContext(), R.layout.layer_root_indicator_item,layerIndicator);
        View v=layerIndicator.getChildAt(0);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoLayer(item,0);
            }
        });
    }

    private void gotoLayer(ListItem listItem,int layerNum) {
        if (layerNum==nowLayer){
            return;
        }
        int min = layerNum + 1;
        for (int i=nowLayer;i>= min;i--){
            indicatorData.remove(i);
            layerIndicator.removeViewAt(i);
        }
        nowLayer=layerNum;
        mListener.onAskForLayerData(listItem,mCurrentData);
    }

    public void setListener(LayerSelectorListener listener){
        mListener=listener;
    }
    public interface LayerSelectorListener {
        public void onAskForLayerData(final ListItem item,final ArrayList<ListItem> oldData);
        public void onSelectLeaf(final ListItem item,final ArrayList<ListItem> oldData,int  position);

    }

    public void showInProgress(){
        listView.setVisibility(INVISIBLE);
        progressWheel.setVisibility(VISIBLE);
        progressWheel.spin();
    }

    public void showData(){
        progressWheel.stopSpinning();
        progressWheel.setVisibility(INVISIBLE);
        listView.setVisibility(VISIBLE);

        mAdapter.notifyDataSetChanged();
    }

    public void setItemDrawer(ItemDrawer itemDrawer){
        mItemDrawer=itemDrawer;
    }

    public void clear(){
        mCurrentData.clear();
        mListener=null;
        mItemDrawer=null;
        indicatorData.clear();
        nowLayer=-1;
        layerIndicator.removeAllViews();
    }

    public void startWithRootLayer(ListItem item){
        mCurrentData.clear();
        nowLayer=-1;
        indicatorData.clear();
        layerIndicator.removeAllViews();
        mCurrentData.add(item);
        setUpRoodIndicator(item);
        mListener.onAskForLayerData(item, mCurrentData);
    }

    public interface ItemDrawer{
        public View getNodeView(ListItem listItem);
        public View getLeafView(ListItem listItem);
    }

    private class ListAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return mCurrentData.size();
        }

        @Override
        public ListItem getItem(int position) {
            return mCurrentData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItem item = getItem(position);
            boolean isNode= item.isNode();
            if (isNode){
                return mItemDrawer.getNodeView(item);
            } else {
                return mItemDrawer.getLeafView(item);
            }
        }
    }
}

