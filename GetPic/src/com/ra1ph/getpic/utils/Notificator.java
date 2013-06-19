package com.ra1ph.getpic.utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import com.ra1ph.getpic.ChatActivity;
import com.ra1ph.getpic.R;
import com.ra1ph.getpic.users.User;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 17.06.13
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */
public class Notificator {

    static Notificator instance;
    private final NotificationManager manager;
    private final HashMap<Integer, Notification> notifications;
    private Context context;
    private int lastId = 0;
    private Vibrator v;


    public static Notificator getInstance(Context context) {
        if (instance == null) instance = new Notificator(context);
        return instance;
    }

    private Notificator(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifications = new HashMap<Integer, Notification>();
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public int createImageNotification(String user_id) {
        String message = context.getResources().getText(R.string.image_notify_text) + " " + NickUtils.getNickFromId(user_id);
        Intent notificationIntent = new Intent(context, ChatActivity.class); // по клику на уведомлении откроется HomeActivity
        notificationIntent.putExtra(User.USER_ID,user_id);
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher) //иконка уведомления
                .setAutoCancel(true) //уведомление закроется по клику на него
                .setTicker(message) //текст, который отобразится вверху статус-бара при создании уведомления
                .setContentText(message) // Основной текст уведомления
                .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setWhen(System.currentTimeMillis()) //отображаемое время уведомления
                .setContentTitle("GetPic") //заголовок уведомления
                .setDefaults(Notification.DEFAULT_ALL); // звук, вибро и диодный индикатор выставляются по умолчанию

        Notification notification = nb.getNotification(); //генерируем уведомление
        manager.notify(lastId, notification); // отображаем его пользователю.
        notifications.put(lastId, notification); //теперь мы можем обращаться к нему по id
        return lastId++;
    }

    public void notifyImage(String user_id){
        if(isApplicationBroughtToBackground())createImageNotification(user_id);
        v.vibrate(100);
    }

    public int createTextNotification(String user_id,String text) {
        String message = NickUtils.getNickFromId(user_id)+":"+text;
        Intent notificationIntent = new Intent(context, ChatActivity.class); // по клику на уведомлении откроется HomeActivity
        notificationIntent.putExtra(User.USER_ID,user_id);
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher) //иконка уведомления
                .setAutoCancel(true) //уведомление закроется по клику на него
                .setTicker(message) //текст, который отобразится вверху статус-бара при создании уведомления
                .setContentText(message) // Основной текст уведомления
                .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setWhen(System.currentTimeMillis()) //отображаемое время уведомления
                .setContentTitle("GetPic") //заголовок уведомления
                .setDefaults(Notification.DEFAULT_ALL); // звук, вибро и диодный индикатор выставляются по умолчанию

        Notification notification = nb.getNotification(); //генерируем уведомление
        manager.notify(lastId, notification); // отображаем его пользователю.
        notifications.put(lastId, notification); //теперь мы можем обращаться к нему по id
        return lastId++;
    }

    public void notifyText(String user_id,String message){
        if(isApplicationBroughtToBackground())createTextNotification(user_id,message);
        v.vibrate(100);
    }

    private boolean isApplicationBroughtToBackground() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }

        return false;
    }
}
