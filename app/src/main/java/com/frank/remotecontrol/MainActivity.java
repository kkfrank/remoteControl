package com.frank.remotecontrol;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.frank.remotecontrol.socket.SocketTransceiver;
import com.frank.remotecontrol.socket.TcpClient;
import com.frank.remotecontrol.utils.Util;
import com.frank.remotecontrol.utils.WifiHelp;

import java.util.Timer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int PORT = 8899;
    private Button bnConnect;
    private TextView txReceive;
    private EditText edIP, edPort, edData, edTimer;
    private static boolean isExit = false;

    private Button btnLightOn, btnLightOff, btnLightQuick, btnLightSlow;

    public Button btnTimer;
    ScheduleNotification notification = null;
    private WifiChangeReceiver wifiChangeReceiver;
    //private ScheduleTaskReceiver scheduleTaskReceiver;

    private ScheduleNotification scheduleNotification;
   // public final static String ACTION_SCHEDULE_RECEIVER = "com.broadcast.schedule_receiver";

    private Handler handler = new Handler(Looper.getMainLooper());

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
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();
        registerBrodcastReceiver();

		String ip = WifiHelp.getWifiRouteIPAddress(this);
		//ip="10.86.35.77";
		edIP.setText(ip);
		edPort.setText(Integer.toString(PORT));

        refreshUI(false);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(wifiChangeReceiver);
        client.disconnect();
        super.onDestroy();
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
        }
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
        if (client.isConnected()) {
            // 断开连接
            client.disconnect();
        } else {
            try {
                String hostIP = edIP.getText().toString();
                int port = Integer.parseInt(edPort.getText().toString());
                client.connect(hostIP, port);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "端口错误", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送数据
     */
    public void sendStr() {
        try {
			if(!client.isConnected()){
				Toast.makeText(MainActivity.this, "未连接",Toast.LENGTH_SHORT).show();
			}
            String data = edData.getText().toString();
			byte[] buf = Util.hexStringToByteArray(data);
            client.getTransceiver().send(buf);
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

}
