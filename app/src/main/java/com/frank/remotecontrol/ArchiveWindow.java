package com.frank.remotecontrol;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.frank.remotecontrol.model.Massage;

import java.util.List;
import java.util.Map;

public class ArchiveWindow extends PopupWindow implements View.OnClickListener {
    private Context context;
    private View view;
    private LinearLayout scan;
    private LinearLayout add;

    private ListView listView;
    private List<Massage> data;
    //private Map<String,Object> data;

    public ArchiveWindow(Context context, List<Massage> data){
        this(context, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,data);
    }
    public ArchiveWindow(Context context, int width, int height,List<Massage> data) {
        this.context = context;
        setWidth(width);
        setHeight(height);
        setFocusable(true); //设置可以获得焦点
        setTouchable(true);  //设置弹窗内可点击
        setOutsideTouchable(true); //设置弹窗外可点击
        setBackgroundDrawable(new ColorDrawable(0));//点击周围消失
       // setBackgroundDrawable(new BitmapDrawable());
        view = LayoutInflater.from(context).inflate(R.layout.archive_popwin,null);
        setContentView(view);
       // setAnimationStyle(R.style.archive_item);
        this.data=data;
        initLisView();
    }
    public void showAtBottom(View view) {
        //弹窗位置设置
        showAsDropDown(view, Math.abs((view.getWidth() - getWidth()) / 2), 10);
        //showAtLocation(view, Gravity.TOP | Gravity.RIGHT, 10, 110);//有偏差
    }

    private void initLisView() {
        listView = (ListView) view.findViewById(R.id.listview_archive);
        //设置列表的适配器
        listView.setAdapter(new ArrayAdapter<Massage>(context,R.layout.archive_item,data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Massage massage = getItem(position);
                View view;
                ViewHolder viewHolder;
                if(convertView==null){
                    view = LayoutInflater.from(context).inflate(R.layout.archive_item,parent,false);
                    viewHolder = new ViewHolder();
                    viewHolder.name=(TextView) view.findViewById(R.id.item_archive_name);
                    view.setTag(viewHolder);
                }else{
                    view = convertView;
                    viewHolder = (ViewHolder)view.getTag();
                }

                viewHolder.name.setText(massage.getName());
                return view;
            }
            class ViewHolder{
                TextView name;
            }

        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Massage massage  = data.get(position);
                ArchiveWindow.this.dismiss();
                Toast.makeText(context,massage.getName(),Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.scan:
//
//                break;
//            case R.id.add:
//
//                break;
//        }
    }
}
