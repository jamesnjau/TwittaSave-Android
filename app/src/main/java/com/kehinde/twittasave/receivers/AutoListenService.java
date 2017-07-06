package com.kehinde.twittasave.receivers;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.kehinde.twittasave.R;
import com.kehinde.twittasave.activities.MainActivity;
import com.kehinde.twittasave.utils.Constant;
import com.kehinde.twittasave.utils.ServiceUtil;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AutoListenService extends Service {


    private Context context;
    private NotificationManager notificationManager;
    private Notification vNotification;
    private android.support.v4.app.NotificationCompat.Builder vNotification1;
    private ClipboardManager mClipboard;
    private ClipboardManager.OnPrimaryClipChangedListener listener;

    @Override
    public void onCreate() {
        super.onCreate();

        context=getApplicationContext();

        mClipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);


        listener = new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                performClipboardCheck();
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent openActivityIntent=new Intent(context, MainActivity.class);
        openActivityIntent.putExtra("service_on",true);
        openActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent openActivityPIntent = PendingIntent.getActivity(context, Constant.AUTO_REQUEST_CODE, openActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent stopAutoIntent = new Intent(context, StopAutoListenReceiver.class);
        stopAutoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent stopAutoPIntent = PendingIntent.getBroadcast(context, Constant.REQUEST_CODE, stopAutoIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            vNotification = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentTitle("TwittaSave AutoListen Running...")
                    .setContentText("Copy tweet URL to start downloading video or gif")
                    .setContentIntent(openActivityPIntent)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .addAction(R.drawable.ic_stop_black_24dp, "STOP", stopAutoPIntent)
                    .build();
            vNotification.flags=Notification.FLAG_NO_CLEAR;

            notificationManager.notify(Constant.NOTI_IDENTIFIER, vNotification);
        }else{
            vNotification1 = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setContentTitle("TwittaSave AutoListen Running...")
                    .setContentText("Copy tweet URL to start downloading video or gif")
                    .setContentIntent(openActivityPIntent)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_stop_black_24dp, "STOP", stopAutoPIntent);

            notificationManager.notify(Constant.NOTI_IDENTIFIER, vNotification1.build());

        }



        mClipboard.addPrimaryClipChangedListener(listener);
        Toast.makeText(context, "TwittaSave AutoListen Enabled", Toast.LENGTH_SHORT).show();


        return super.onStartCommand(intent, flags, startId);
    }

    private void performClipboardCheck() {
        if (mClipboard.hasPrimaryClip()){
            String copiedURL = mClipboard.getPrimaryClip().getItemAt(0).getText().toString();
            ServiceUtil.fetchTweet(copiedURL,context);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (notificationManager!=null) {
            notificationManager.cancel(Constant.NOTI_IDENTIFIER);
        }

        if (mClipboard!=null && listener!=null){
            this.mClipboard.removePrimaryClipChangedListener(listener);
            Toast.makeText(context, "TwittaSave AutoListen Disabled", Toast.LENGTH_SHORT).show();

        }
    }
}