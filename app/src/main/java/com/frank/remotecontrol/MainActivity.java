package com.frank.remotecontrol;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.frank.remotecontrol.fragment.FragmentSecond;
import com.frank.remotecontrol.fragment.FragmentThird;
import com.frank.remotecontrol.model.Massage;
import com.frank.remotecontrol.service.SocketService;
import com.frank.remotecontrol.utils.Constants;
import com.frank.remotecontrol.utils.SPUtils;
import com.frank.remotecontrol.utils.WifiHelp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.frank.remotecontrol.fragment.FragmentFirst;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, FragmentFirst.OnFragmentInteractionListener, FragmentSecond.OnFragmentInteractionListener,
FragmentThird.OnFragmentInteractionListener{
    private TabLayout mTabLayout;
    public CustomViewpager mVpContent;

    private static boolean isExit = false;
    private int PORT = 8899;
//
//    private TextView txReceive;
//    private EditText edIP, edPort, edData, edTimer,edSaveName;
//    private Button btnLightOn, btnLightOff, btnLightQuick, btnLightSlow,btnSave,btnRead,bnConnect,btnTimer;

    ScheduleNotification notification = null;
    private WifiChangeReceiver wifiChangeReceiver;
    public static final int SEND_DATA =1;
    private ScheduleNotification scheduleNotification;

    private ServiceConnection sc;
    public SocketService socketService;


    private Handler handler = new Handler(Looper.getMainLooper());


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        initView();
        initTab();
        registerBrodcastReceiver();



        refreshUI(false);
       // EventBus.getDefault().register(this);

        String ip = WifiHelp.getWifiRouteIPAddress(this);
        //ip="10.86.35.77";
        ip ="192.168.1.12"; //edIP.getText().toString();
        String port ="8899";//edPort.getText().toString();

        bindSocketService(ip,port);

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(wifiChangeReceiver);
        super.onDestroy();

        unbindService(sc);
        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        stopService(intent);
       // EventBus.getDefault().unregister(this);
    }

    Handler handlerExit = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            //利用handler延迟发送更改状态信息
            handlerExit.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            //System.exit(0);
        }
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.bn_connect:
//                //connect();
//                sendStr();
//                break;
//            case R.id.bn_send:
//                sendStr();
//                break;
//            case R.id.tx_receive:
//                clear();
//                break;
//            case R.id.lighton:
//              //  sendBytes(new byte[]{0x01, 0x55});
//                break;
//            case R.id.lightoff:
//              //  sendBytes(new byte[]{0x00, 0x55});
//                break;
//            case R.id.lightquick:
//              //  sendBytes(new byte[]{0x03, 0x55});
//                break;
//            case R.id.lightslow:
//             //   sendBytes(new byte[]{0x02, 0x55});
//                break;
//            case R.id.btn_timer:
//                startTimeFn();
//                break;
//            case R.id.save_btn:
//                saveCustomModel();
//                break;
//            case R.id.read_btn:
//                readCustomModel();
//                break;
//        }
    }

    private void saveCustomModel(){
        String name = "";//edSaveName.getText().toString();
        if("".equals(name)){

            Toast.makeText(MainActivity.this, "名字不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        Massage massage = new Massage();
        massage.setName(name);

        String strJson =(String)SPUtils.get(this,"name","");
        Map<String,Massage> data = new Gson().fromJson(strJson, new TypeToken<Map<String,Massage>>() {}.getType());
        if(data ==null){
            data = new HashMap<String, Massage>();
        }

        data.put(name,massage);
        SPUtils.put(this,"name",new Gson().toJson(data));
    }
    private void readCustomModel(){
        String strJson =(String)SPUtils.get(this,"name","");
        Map<String,Massage> data = new Gson().fromJson(strJson, new TypeToken<Map<String,Massage>>() {}.getType());
        if(data==null || data.size()==0){
            Toast.makeText(MainActivity.this, "没有存储数据",Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<Massage> list = new ArrayList<Massage>(data.values());
        ArchiveWindow window = new ArchiveWindow(this,list);
       // window.showAtBottom(btnRead);
       // window.showAtLocation(contentView,100,100,100);
    }
    private void startTimeFn() {
//        String minutes = edTimer.getText().toString();
//        if ("".equals(minutes)) {
//            return;
//        }
//
//        //sendBytes(new byte[]{0x05, 0x55});
//        scheduleNotification = new ScheduleNotification(this).initAndNotify(Integer.parseInt(minutes));
//
//        btnTimer.setEnabled(false);
    }

    /**
     * 刷新界面显示
     *
     * @param isConnected
     */
    private void refreshUI(final boolean isConnected) {
        handler.post(new Runnable() {
            @Override
            public void run() {
//                edPort.setEnabled(!isConnected);
//                edIP.setEnabled( !isConnected);
//                bnConnect.setText(isConnected ? "断开" : "连接");
            }
        });
    }

    /**
     * 发送数据
     */
    public void sendStr() {
        try {
          //  String data = edData.getText().toString();
           // socketService.sendOrder(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
//        this.findViewById(R.id.bn_send).setOnClickListener(this);
//        bnConnect = (Button) this.findViewById(R.id.bn_connect);
//        bnConnect.setOnClickListener(this);
//
//        edIP = (EditText) this.findViewById(R.id.ed_ip);
//        edPort = (EditText) this.findViewById(R.id.ed_port);
//        edData = (EditText) this.findViewById(R.id.ed_dat);
//        txReceive = (TextView) this.findViewById(R.id.tx_receive);
//        txReceive.setOnClickListener(this);
//
//        btnLightOn = (Button) this.findViewById(R.id.lighton);
//        btnLightOn.setOnClickListener(this);
//
//        btnLightOff = (Button) this.findViewById(R.id.lightoff);
//        btnLightOff.setOnClickListener(this);
//
//        btnLightQuick = (Button) this.findViewById(R.id.lightquick);
//        btnLightQuick.setOnClickListener(this);
//
//        btnLightSlow = (Button) this.findViewById(R.id.lightslow);
//        btnLightSlow.setOnClickListener(this);
//
//        edTimer = (EditText) this.findViewById(R.id.timer);
//        btnTimer = (Button) this.findViewById(R.id.btn_timer);
//        btnTimer.setOnClickListener(this);
//
//        edSaveName = (EditText) this.findViewById(R.id.save_name);
//        btnSave = (Button) this.findViewById(R.id.save_btn);
//        btnRead = (Button) this.findViewById(R.id.read_btn);
//
//        btnSave.setOnClickListener(this);
//        btnRead.setOnClickListener(this);
//
    }

    private void initTab(){
        mTabLayout = (TabLayout) findViewById(R.id.tablayout);
        mVpContent = (CustomViewpager) findViewById(R.id.viewpager);

        List mTitles =new ArrayList();
        mTitles.add("顺序");
        mTitles.add("间隔");
        mTitles.add("强度");
        ArrayList mFragments = new ArrayList();

        FragmentFirst fragment = new FragmentFirst();
        mFragments.add(fragment);

        FragmentSecond fragmentSecond = new FragmentSecond();
        mFragments.add(fragmentSecond);

        FragmentThird fragment1 = new FragmentThird();
        mFragments.add(fragment1);

//        for (int i = 0; i < mTitles.size(); i++) {
//            FragmentFirst fragment = new FragmentFirst();
////            Bundle bundle = new Bundle();
////            bundle.putString(ContentFragment.TEXT, mTitles[i]);
////            com.frank.remotecontrol.fragment.setArguments(bundle);
//            mFragments.add(fragment);//添加到fragment中
//        }

        TabPagerAdapter tabAdapter = new TabPagerAdapter(getSupportFragmentManager(),MainActivity.this, mFragments, mTitles);
        mVpContent.setAdapter(tabAdapter);//为viewPager设置adapter
       // mVpContent.setScrollX(1);
        mVpContent.setPagingEnabled(true);
        mTabLayout.setupWithViewPager(mVpContent);//将TabLayout和ViewPager关联

    }
    /**
     * 清空接收框
     */
//    private void clear() {
//        new AlertDialog.Builder(this).setTitle("确认清除?")
//                .setNegativeButton("取消", null)
//                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //txReceive.setText("");
//                    }
//                }).show();
//    }



    private void registerBrodcastReceiver(){
        wifiChangeReceiver = new WifiChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
      //  intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(wifiChangeReceiver,intentFilter);
    }

    public class WifiChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
           if(WifiHelp.isWifiConnect(MainActivity.this)){
               String ip = WifiHelp.getWifiRouteIPAddress(MainActivity.this);
//               //ip="10.86.35.77";
//               ip ="192.168.1.12"; //edIP.getText().toString();
//               String port ="8899";//edPort.getText().toString();
//               ip =edIP.getText().toString();
//               String port =edPort.getText().toString();
//               bindSocketService(ip,port);
           } else{
               Toast.makeText(MainActivity.this, "连接已断开",Toast.LENGTH_SHORT).show();
           }
        }
    }

    public void bindSocketService(String ip,String port) {

        Intent startIntent = new Intent(this,SocketService.class);
        startIntent.putExtra(Constants.INTENT_IP,ip);
        startIntent.putExtra(Constants.INTENT_PORT,port);
        startService(startIntent);

        /*通过binder拿到service*/
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                SocketService.SocketBinder binder = (SocketService.SocketBinder) iBinder;
                socketService = binder.getService();
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };

        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        bindService(intent, sc, BIND_AUTO_CREATE);
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(MessageEvent event) {
//        /* Do something */
//     //   txReceive.append(event.getMsg());
//    };

    @Override
    public void onFragmentInteraction(int id) {

    }
}
