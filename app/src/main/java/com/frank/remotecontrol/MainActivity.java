package com.frank.remotecontrol;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.frank.remotecontrol.model.Massage;
import com.frank.remotecontrol.service.SocketService;
import com.frank.remotecontrol.socket.SocketTransceiver;
import com.frank.remotecontrol.socket.TcpClient;
import com.frank.remotecontrol.utils.Constants;
import com.frank.remotecontrol.utils.ListDataSave;
import com.frank.remotecontrol.utils.SPUtils;
import com.frank.remotecontrol.utils.Util;
import com.frank.remotecontrol.utils.WifiHelp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BlankFragment.OnFragmentInteractionListener {
    private TabLayout mTabLayout;
    private ViewPager mVpContent;


    private int PORT = 8899;
    private Button bnConnect;
    private TextView txReceive;
    private EditText edIP, edPort, edData, edTimer,edSaveName;
    private static boolean isExit = false;

    private ListDataSave listDataSave;

    private Button btnLightOn, btnLightOff, btnLightQuick, btnLightSlow,btnSave,btnRead;

    public Button btnTimer;
    ScheduleNotification notification = null;
    private WifiChangeReceiver wifiChangeReceiver;
    //private ScheduleTaskReceiver scheduleTaskReceiver;
    public static final int SEND_DATA =1;
    private ScheduleNotification scheduleNotification;

    private ServiceConnection sc;
    public SocketService socketService;

    // public final static String ACTION_SCHEDULE_RECEIVER = "com.broadcast.schedule_receiver";

    private Handler handler = new Handler(Looper.getMainLooper());

    private Handler socketHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SEND_DATA:
                    break;
                default:
                    break;
            }
        }
    };
    private TcpClient client = new TcpClient() {

        @Override
        public void onConnect(SocketTransceiver transceiver) {
            refreshUI(true);
        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "连接断开",
                            Toast.LENGTH_SHORT).show();
                }
            });
            refreshUI(false);
        }


        @Override
        public void onConnectFailed(final String error) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "连接失败"+error,
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onReceive(SocketTransceiver transceiver, final String s) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    txReceive.append(s);
                }
            });
        }

        @Override
        public void sendFailed(String error) {
            Toast.makeText(MainActivity.this, "发送失败"+error,
                    Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

//        ArrayList fragmentList = new ArrayList<>();
//        ArrayList list_Title = new ArrayList<>();
//        fragmentList.add(new OneFragment());
//        fragmentList.add(new TwoFragment());
//        list_Title.add("one");
//        list_Title.add("two");
//        viewpager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(),HelpCenterActivity.this,fragmentList,list_Title));
//        tablayout.setupWithViewPager(viewpager);//此方法就是让tablayout和ViewPager联动


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        listDataSave = new ListDataSave(this, "data");

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        initView();
        initTab();
        registerBrodcastReceiver();

		String ip = WifiHelp.getWifiRouteIPAddress(this);
		//ip="10.86.35.77";
        ip="192.168.1.12";
		edIP.setText(ip);
		edPort.setText(Integer.toString(PORT));

        refreshUI(false);
        EventBus.getDefault().register(this);
        bindSocketService();

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(wifiChangeReceiver);
        client.disconnect();
        super.onDestroy();

        unbindService(sc);
        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        stopService(intent);
        EventBus.getDefault().unregister(this);
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
        switch (v.getId()) {
            case R.id.bn_connect:
                connect();
                break;
            case R.id.bn_send:
                sendStr();
                break;
            case R.id.tx_receive:
                clear();
                break;
            case R.id.lighton:
                sendBytes(new byte[]{0x01, 0x55});
                break;
            case R.id.lightoff:
                sendBytes(new byte[]{0x00, 0x55});
                break;
            case R.id.lightquick:
                sendBytes(new byte[]{0x03, 0x55});
                break;
            case R.id.lightslow:
                sendBytes(new byte[]{0x02, 0x55});
                break;
            case R.id.btn_timer:
                startTimeFn();
                break;
            case R.id.save_btn:
                saveCustomModel();
                break;
            case R.id.read_btn:
                readCustomModel();
                break;
        }
    }

    private void saveCustomModel(){
        String name = edSaveName.getText().toString();
        if("".equals(name)){

            Toast.makeText(MainActivity.this, "名字不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        Massage massage = new Massage();
        massage.setName(name);

//        SharedPreferences.Editor editor =getSharedPreferences("data",MODE_PRIVATE).edit();
//        editor.putString("name",name);
//
//        editor.apply();
       // List<String,Object> data = listDataSave.getDataList("name");
        String strJson =(String)SPUtils.get(this,"name","");
        Map<String,Massage> data = new Gson().fromJson(strJson, new TypeToken<Map<String,Massage>>() {}.getType());
        if(data ==null){
            data = new HashMap<String, Massage>();
        }
//        if(data.containsKey("name")){
//            data.put("name",massage);
//        }
        data.put(name,massage);
        SPUtils.put(this,"name",new Gson().toJson(data));
       // listDataSave.setDataList("name",data);
    }
    private void readCustomModel(){
//        SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
//        pref.getAll();
       // List<String> data = listDataSave.getDataList("name");
        String strJson =(String)SPUtils.get(this,"name","");
        Map<String,Massage> data = new Gson().fromJson(strJson, new TypeToken<Map<String,Massage>>() {}.getType());
        if(data==null || data.size()==0){
            Toast.makeText(MainActivity.this, "没有存储数据",Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<Massage> list = new ArrayList<Massage>(data.values());
        ArchiveWindow window = new ArchiveWindow(this,list);
        window.showAtBottom(btnRead);
       // window.showAtLocation(contentView,100,100,100);
    }
    private void startTimeFn() {
        String minutes = edTimer.getText().toString();
        if ("".equals(minutes)) {
            return;
        }

        sendBytes(new byte[]{0x05, 0x55});
        scheduleNotification = new ScheduleNotification(this).initAndNotify(Integer.parseInt(minutes));

        btnTimer.setEnabled(false);
//		new Thread(){
//            @Override
//            public void run() {
//                try {
//                    this.sleep(6000);
//                    scheduleNotification.cancel();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
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
                edPort.setEnabled(!isConnected);
                edIP.setEnabled( !isConnected);
                bnConnect.setText(isConnected ? "断开" : "连接");
            }
        });
    }

    /**
     * 设置IP和端口地址,连接或断开
     */
    public void connect() {
//        if (client.isConnected()) {
//            // 断开连接
//            client.disconnect();
//        } else {
//            try {
//                String hostIP = edIP.getText().toString();
//                int port = Integer.parseInt(edPort.getText().toString());
//                client.connect(hostIP, port);
//            } catch (NumberFormatException e) {
//                Toast.makeText(this, "端口错误", Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * 发送数据
     */
    public void sendStr() {
        try {
//			if(!client.isConnected()){
//				Toast.makeText(MainActivity.this, "未连接",Toast.LENGTH_SHORT).show();
//			}

            String data = edData.getText().toString();
			//byte[] buf = Util.hexStringToByteArray(data);
           // client.getTransceiver().send(buf);
            socketService.sendOrder(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendBytes(byte[] buff) {
        try {
			if(!client.isConnected()){
				Toast.makeText(MainActivity.this, "未连接",Toast.LENGTH_SHORT).show();
			}
            client.getTransceiver().send(buff);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        this.findViewById(R.id.bn_send).setOnClickListener(this);
        bnConnect = (Button) this.findViewById(R.id.bn_connect);
        bnConnect.setOnClickListener(this);

        edIP = (EditText) this.findViewById(R.id.ed_ip);
        edPort = (EditText) this.findViewById(R.id.ed_port);
        edData = (EditText) this.findViewById(R.id.ed_dat);
        txReceive = (TextView) this.findViewById(R.id.tx_receive);
        txReceive.setOnClickListener(this);

        btnLightOn = (Button) this.findViewById(R.id.lighton);
        btnLightOn.setOnClickListener(this);

        btnLightOff = (Button) this.findViewById(R.id.lightoff);
        btnLightOff.setOnClickListener(this);

        btnLightQuick = (Button) this.findViewById(R.id.lightquick);
        btnLightQuick.setOnClickListener(this);

        btnLightSlow = (Button) this.findViewById(R.id.lightslow);
        btnLightSlow.setOnClickListener(this);

        edTimer = (EditText) this.findViewById(R.id.timer);
        btnTimer = (Button) this.findViewById(R.id.btn_timer);
        btnTimer.setOnClickListener(this);

        edSaveName = (EditText) this.findViewById(R.id.save_name);
        btnSave = (Button) this.findViewById(R.id.save_btn);
        btnRead = (Button) this.findViewById(R.id.read_btn);

        btnSave.setOnClickListener(this);
        btnRead.setOnClickListener(this);

    }

    private void initTab(){
        mTabLayout = (TabLayout) findViewById(R.id.tablayout);
        mVpContent = (ViewPager) findViewById(R.id.viewpager);

        List mTitles =new ArrayList();
        mTitles.add("tab1");
        mTitles.add("tab2");
        mTitles.add("tab3");
        ArrayList mFragments = new ArrayList();
        for (int i = 0; i < mTitles.size(); i++) {
            BlankFragment fragment = new BlankFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString(ContentFragment.TEXT, mTitles[i]);
//            fragment.setArguments(bundle);
            mFragments.add(fragment);//添加到fragment中
        }

        TabPagerAdapter tabAdapter = new TabPagerAdapter(getSupportFragmentManager(),MainActivity.this, mFragments, mTitles);
        mVpContent.setAdapter(tabAdapter);//为viewPager设置adapter
        mTabLayout.setupWithViewPager(mVpContent);//将TabLayout和ViewPager关联

    }
    /**
     * 清空接收框
     */
    private void clear() {
        new AlertDialog.Builder(this).setTitle("确认清除?")
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txReceive.setText("");
                    }
                }).show();
    }



    private void registerBrodcastReceiver(){
        wifiChangeReceiver = new WifiChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        registerReceiver(wifiChangeReceiver,intentFilter);


//        IntentFilter scIntent = new IntentFilter();
//        scIntent.addAction(ACTION_SCHEDULE_RECEIVER);
//        scheduleTaskReceiver = new ScheduleTaskReceiver();
//        registerReceiver(scheduleTaskReceiver,scIntent);


    }
    private static void onDownLoadBtnClick() {
      //  sendBytes(new byte[]{0x06, 0x55});
       // notification.cancel();

//        if (isPlay) {
//            //当前是进行中，则暂停
//            isPlay = false;
//            contentView.setTextViewText(R.id.btn_download,"开始");
//        }else {
//            //当前暂停，则开始
//            isPlay = true;
//            contentView.setTextViewText(R.id.btn_download,"停止");
//        }
        //notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public class WifiChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
           if(WifiHelp.isWifiConnect(MainActivity.this)){
               if(!client.isConnected()){
                   connect();
               }
           } else{
               Toast.makeText(MainActivity.this, "连接已断开",Toast.LENGTH_SHORT).show();
           }
        }
    }
//    public class ScheduleTaskReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals(ScheduleNotification.ACTION_BUTTON)) {
//                //通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
//                int buttonId = intent.getIntExtra(ScheduleNotification.INTENT_BUTTONID_TAG, 0);
//                switch (buttonId) {
//                    case ScheduleNotification.BUTTON_PALY_ID:
//                        Log.d(ScheduleNotification.TAG, "点击播放/暂停按钮");
//                        onDownLoadBtnClick();
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }
//    }

    private void bindSocketService() {
        String hostIP = edIP.getText().toString();
        String port =edPort.getText().toString();

        Intent startIntent = new Intent(this,SocketService.class);
        startIntent.putExtra(Constants.INTENT_IP,hostIP);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        /* Do something */
        txReceive.append(event.getMsg());
    };

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
