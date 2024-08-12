package com.anshu.insurancenotifier;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationService extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundService();
        scheduleNotifications();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundService() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Insurance Notifier")
                .setContentText("Monitoring your insurance renewal dates.")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(1, builder.build());
    }

    private void scheduleNotifications() {
        // Example data for scheduling. Replace with real data fetching.
        String renewalDate = "2024-08-20"; // Fetch this from SharedPreferences or Firebase

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            Date renewal = sdf.parse(renewalDate);

            calendar.setTime(renewal);
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            Date sevenDaysBefore = calendar.getTime();

            calendar.setTime(renewal);
            calendar.add(Calendar.DAY_OF_YEAR, -3);
            Date threeDaysBefore = calendar.getTime();

            calendar.setTime(renewal);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            Date oneDayBefore = calendar.getTime();

            scheduleNotification(this, sevenDaysBefore, "Renewal in 7 days!", 1);
            scheduleNotification(this, threeDaysBefore, "Renewal in 3 days!", 2);
            scheduleNotification(this, oneDayBefore, "Renewal in 1 day!", 3);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void scheduleNotification(Context context, Date notifyDate, String contentText, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("contentText", contentText);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyDate.getTime(), pendingIntent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Foreground Service Channel";
            String description = "Channel for Foreground Service";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}

