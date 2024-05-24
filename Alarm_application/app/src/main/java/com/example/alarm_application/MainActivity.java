package com.example.alarm_application;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.example.alarm_application.AlarmAdapter;
import com.example.alarm_application.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ListView alarmListView;
    private Button btnAddAlarm;
    private ArrayList<String> alarmList;
    private AlarmAdapter adapter;
    private NotificationManager notificationManager;
    public static final String CHANNEL_ID = "alarm_channel";
    public static final String ALARM_PREFERENCES_KEY = "alarm_preferences";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmListView = findViewById(R.id.alarmListView);
        btnAddAlarm = findViewById(R.id.btnAddAlarm);

        alarmList = new ArrayList<>();
        adapter = new AlarmAdapter(this, alarmList);
        alarmListView.setAdapter(adapter);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        btnAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        // Restore alarms from SharedPreferences
        Set<String> savedAlarms = getSharedPreferences(ALARM_PREFERENCES_KEY, Context.MODE_PRIVATE).getStringSet(ALARM_PREFERENCES_KEY, new HashSet<>());
        alarmList.addAll(savedAlarms);
        adapter.notifyDataSetChanged();

        startAlarmCheckLoop();
    }

    private void startAlarmCheckLoop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(60000);
                        checkAlarms();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void checkAlarms() {
        long currentTimeInMillis = System.currentTimeMillis();
        for (String alarmTime : alarmList) {
            String[] timeParts = alarmTime.split(":");
            int alarmHour = Integer.parseInt(timeParts[0]);
            int alarmMinute = Integer.parseInt(timeParts[1]);

            Calendar alarmCalendar = Calendar.getInstance();
            alarmCalendar.set(Calendar.HOUR_OF_DAY, alarmHour);
            alarmCalendar.set(Calendar.MINUTE, alarmMinute);
            alarmCalendar.set(Calendar.SECOND, 0);
            long alarmTimeInMillis = alarmCalendar.getTimeInMillis();

            if (Math.abs(currentTimeInMillis - alarmTimeInMillis) <= 60 * 1000) {
                showNotification("Alarm", "It's time to wake up!");
            }
        }
    }

    private void showTimePickerDialog() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = String.format("%02d:%02d", hourOfDay, minute);
                        alarmList.add(time);
                        adapter.notifyDataSetChanged();
                        saveAlarmsToPreferences();
                    }
                }, hour, minute, true);

        timePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Channel";
            String description = "Channel for alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri customSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm_sound);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(customSoundUri);

        notificationManager.notify(1, builder.build());
    }

    private void saveAlarmsToPreferences() {
        getSharedPreferences(ALARM_PREFERENCES_KEY, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(ALARM_PREFERENCES_KEY, new HashSet<>(alarmList))
                .apply();
    }
}
