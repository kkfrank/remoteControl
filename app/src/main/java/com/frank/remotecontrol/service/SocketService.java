package com.frank.remotecontrol.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import com.frank.remotecontrol.MessageEvent;
import com.frank.remotecontrol.utils.Constants;
import com.frank.remotecontrol.utils.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

public class SocketService extends Service{
    private Socket socket;
    /*连接线程*/
    private Thread connectThread;
    private Timer timer = new Timer();
    private OutputStream outputStream;
    private InputStream inputStream;
    private SocketBinder sockerBinder = new SocketBinder();
    private String ip;
    private String port;
    private TimerTask task;
    private boolean runFlag;

    /*默认重连*/
    private boolean isReConnect = true;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
       // return null;
        return sockerBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ip = intent.getStringExtra(Constants.INTENT_IP);
        port = intent.getStringExtra(Constants.INTENT_PORT);

        /*初始化socket*/
        initSocket();
        return super.onStartCommand(intent, flags, startId);
    }

    /*初始化socket*/
    private void initSocket() {
        if (socket == null && connectThread == null) {
            connectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    socket = new Socket();
                    try {
                        /*超时时间为2秒*/
                        socket.connect(new InetSocketAddress(ip, Integer.valueOf(port)), 2000);
                        /*连接成功的话  发送心跳包*/
                        if (socket.isConnected()) {
                            /*因为Toast是要运行在主线程的  这里是子线程  所以需要到主线程哪里去显示toast*/
                            toastMsg("socket已连接");

                            /*发送连接成功的消息*/
                            MessageEvent msg = new MessageEvent();
                            msg.setTag(Constants.CONNET_SUCCESS);
                            EventBus.getDefault().post(msg);
                            /*发送心跳数据*/
                            //sendBeatData();
                            listeningSocket();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e instanceof SocketTimeoutException) {
                            toastMsg("连接超时，正在重连");
                            releaseSocket();
                        } else if (e instanceof NoRouteToHostException) {
                            toastMsg("该地址不存在，请检查");
                            stopSelf();
                        } else if (e instanceof ConnectException) {
                            toastMsg("连接异常或被拒绝，请检查");
                            stopSelf();
                        }
                    }
                }
            });
            /*启动连接线程*/
            connectThread.start();
        }
    }
    private void listeningSocket(){
        try {
            runFlag=true;
            inputStream = new DataInputStream(socket.getInputStream());
            //outputStream = new DataOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            //this.sendFailed(e.getMessage());
            toastMsg("getInputStream "+e.getMessage());
            e.printStackTrace();
            runFlag = false;
        }
        while (runFlag) {
            try {
                byte [] bytes = new byte[1024];
                //in.readUTF();
                int len = inputStream.read(bytes);
                if(len != -1){
                    //String s = new String(bytes,0,len);
                    String s = Util.bytesToHex(new String(bytes,0,len).getBytes());
                    /*发送连接成功的消息*/
                    MessageEvent msg = new MessageEvent();
                    msg.setTag(Constants.RECEIVE_SUCCESS);
                    msg.setMsg(s);
                    EventBus.getDefault().post(msg);
                   // this.onReceive(addr, s);
                }else{
                    runFlag = false;
                }
            } catch (IOException e) {
                // 连接被断开(被动)
                toastMsg("receive message fail "+e.getMessage());
                runFlag = false;
            }
        }
    }
   /*发送数据*/
    public void sendOrder(final String order) {
        if (socket != null && socket.isConnected()) {
            /*发送指令*/
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStream = new DataOutputStream(socket.getOutputStream());
                       // outputStream = socket.getOutputStream();
                        if (outputStream != null) {
                            byte[] orderBuf = Util.hexStringToByteArray(order);
                            outputStream.write(orderBuf,0,orderBuf.length);
                            //outputStream.write((order).getBytes("gbk"));
                            outputStream.flush();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        } else {
            toastMsg("socket连接错误,请重试");
        }
    }

    //private
    /*定时发送数据*/
    private void sendBeatData() {
        if (timer == null) {
            timer = new Timer();
        }
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        outputStream = socket.getOutputStream();
                        /*这里的编码方式根据你的需求去改*/
                        outputStream.write("123".getBytes(),0,"123".getBytes().length);
                       // outputStream.write(("test").getBytes("gbk"));
                        outputStream.flush();
                    } catch (Exception e) {
                        /*发送失败说明socket断开了或者出现了其他错误*/
                        toastMsg("连接断开，正在重连"+e.getMessage());
                        /*重连*/
                        releaseSocket();
                        e.printStackTrace();
                    }
                }
            };
        }
        timer.schedule(task, 0, 2000);
    }


    /*释放资源*/
    private void releaseSocket() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
        if (outputStream != null) {
            try {
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
            socket = null;
        }
        if (connectThread != null) {
            connectThread = null;
        }
        /*重新初始化socket*/
        if (isReConnect) {
            initSocket();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SocketService", "onDestroy");
        isReConnect = false;
        releaseSocket();
    }

    /*因为Toast是要运行在主线程的   所以需要到主线程哪里去显示toast*/
    private void toastMsg(final String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //git checkout -b feature/user_service_socket
    public class SocketBinder extends Binder {
        /*返回SocketService 在需要的地方可以通过ServiceConnection获取到SocketService  */
        public SocketService getService() {
            return SocketService.this;
        }
    }
}
