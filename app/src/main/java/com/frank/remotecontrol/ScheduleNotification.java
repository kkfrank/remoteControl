package com.frank.remotecontrol;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class ScheduleNotification {
    public final static String INTENT_BUTTONID_TAG = "ButtonId";
    /**
     * 通知栏按钮点击事件对应的ACTION（标识广播）
     */
    public final static String ACTION_BROADCAST = "com.notification.intent.action.ButtonClick";
    /**
     * 标识按钮状态：是否在播放
     */
    public boolean isPlay = false;
    /**
     * 通知栏按钮广播
     */
    public ButtonBroadcastReceiver receiver;

    /**
     * 播放/暂停 按钮点击 ID
     */
    public final static int BUTTON_PALY_ID = 1;
    private final int NOTIFICATION_ID = 0xa01;
    private final int REQUEST_CODE = 0xb01;

    private Context context;

    private NotificationManager notificationManager;
    private RemoteViews contentView;
    private Notification notification;

    private Timer timer;
    private int totalTime;
    private int totalSec;
    public ScheduleNotification(Context context) {
        this.context = context;
    }
    private String createTitle(int time){
        return time+"秒后计时结束";
    }

    public ScheduleNotification initAndNotify(int time) {
        totalTime=time;
        totalSec=time*60;

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification);
        contentView.setTextViewText(R.id.txt_appName, createTitle(totalSec));
        contentView.setTextViewText(R.id.txt_network_speed, "倒计时进行中" );

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(context)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.icon)
                .build();

        //注册广播
        receiver = new ButtonBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BROADCAST);
        context.registerReceiver(receiver, intentFilter);

        //设置点击的事件
        Intent buttonIntent = new Intent(ACTION_BROADCAST);
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PALY_ID);
        PendingIntent intent_paly = PendingIntent.getBroadcast(context, BUTTON_PALY_ID, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        contentView.setOnClickPendingIntent(R.id.btn_stop_schedule, intent_paly);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                totalSec-=1;
                if(totalSec == -1){
                    ScheduleNotification.this.cancel();
                }else{
                    contentView.setTextViewText(R.id.txt_appName, createTitle(totalSec));
                    notificationManager.notify(NOTIFICATION_ID,notification);
                }

            }
        },0,1000);

        notification.contentView = contentView;

        // 需要注意的是，作为选项，此处可以设置MainActivity的启动模式为singleTop，避免重复新建onCreate()。
        Intent intent = new Intent(context, MainActivity.class);
        // 当用户点击通知栏的Notification时候，切换回TaskDefineActivity。
        PendingIntent pi = PendingIntent.getActivity(context, REQUEST_CODE,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentIntent = pi;
        isPlay=true;
        // 发送到手机的通知栏
        //notificationManager.cancel(NOTIFICATION_ID);
        notificationManager.notify(NOTIFICATION_ID, notification);
        return this;
    }
    /**
     * （通知栏中的点击事件是通过广播来通知的，所以在需要处理点击事件的地方注册广播即可）
     * 广播监听按钮点击事件
     */
//
    public class ButtonBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_BROADCAST)) {
                //通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
                int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
                switch (buttonId) {
                    case BUTTON_PALY_ID:
                        ((MainActivity)context).sendBytes(new byte[]{0x02, 0x55});

                        contentView.setTextViewText(R.id.btn_stop_schedule,"停止");
                       // ((MainActivity)context).connect();
                        //onDownLoadBtnClick();
                        cancel();
                        break;
                    default:
                        break;
                }
            }
        }
    }
    private void onDownLoadBtnClick() {
        if (isPlay) {
            //当前是进行中，则暂停
            isPlay = false;
            contentView.setTextViewText(R.id.btn_stop_schedule,"开始");
        }else {
            //当前暂停，则开始
            isPlay = true;
            contentView.setTextViewText(R.id.btn_stop_schedule,"停止");
        }
        ((MainActivity)context).connect();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * 关闭通知
     */
    public void cancel() {
        if(timer!=null){
            timer.cancel();
            timer.purge();
        }
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
        contentView.setTextViewText(R.id.txt_appName, "已结束");
        contentView.setTextViewText(R.id.txt_network_speed, "倒计时结束" );
        contentView.setViewVisibility(R.id.btn_stop_schedule,View.GONE );
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
            //notificationManager.cancel(NOTIFICATION_ID);
        }
       // ((MainActivity)context).btnTimer.setEnabled(true);//to do
       // Toast.makeText(context, "计时结束",Toast.LENGTH_LONG).show();
    }
}
