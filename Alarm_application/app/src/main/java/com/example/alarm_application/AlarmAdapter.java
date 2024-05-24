package com.example.alarm_application;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;

public class AlarmAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> alarmList;

    public AlarmAdapter(@NonNull Context context, ArrayList<String> alarmList) {
        super(context, R.layout.alarm_item_layout, alarmList);
        this.context = context;
        this.alarmList = alarmList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.alarm_item_layout, parent, false);
        }

        TextView tvAlarmTime = convertView.findViewById(R.id.tvAlarmTime);
        Button btnDeleteAlarm = convertView.findViewById(R.id.btnDeleteAlarm);

        String alarmTime = alarmList.get(position);
        tvAlarmTime.setText(alarmTime);

        btnDeleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmList.remove(position);
                notifyDataSetChanged();
                saveAlarmsToPreferences();
            }
        });

        return convertView;
    }

    private void saveAlarmsToPreferences() {
        context.getSharedPreferences(MainActivity.ALARM_PREFERENCES_KEY, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(MainActivity.ALARM_PREFERENCES_KEY, new HashSet<>(alarmList))
                .apply();
    }
}
