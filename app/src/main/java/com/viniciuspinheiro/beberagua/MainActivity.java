package com.viniciuspinheiro.beberagua;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_NOTIFY = "KEY_NOTIFY";
    private static final String KEY_INTERVAL = "KEY_INTERVAL";
    private static final String KEY_HOUR = "KEY_HOUR";
    private static final String KEY_MINUTE = "KEY_MINUTE";
    private SharedPreferences storage;

    private Button btnNotify;
    private EditText editMinutes;
    private TimePicker timePicker;

    private boolean activated;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage = getSharedPreferences("storage", Context.MODE_PRIVATE);

        btnNotify = findViewById(R.id.btn_notify);
        editMinutes = findViewById(R.id.edit_text_number_intervalo);
        timePicker = findViewById(R.id.time_picker);

        activated = storage.getBoolean(KEY_NOTIFY, false);

        setupUI(activated, storage);

        timePicker.setIs24HourView(true);

        btnNotify.setOnClickListener(notifyListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Start test", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Resume test", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Pause test", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Stop test", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Destroy test", "onDestroy");
    }

    private void alert(int resId) {
        Toast.makeText(MainActivity.this, resId, Toast.LENGTH_LONG).show();
    }

    private void setupUI(boolean activated, SharedPreferences storage) {
        if (activated) {
            btnNotify.setText(R.string.pause);
            btnNotify.setBackgroundResource(R.drawable.bg_button_background);

            int interval = storage.getInt(KEY_INTERVAL, 0);
            int hour = storage.getInt(KEY_HOUR, timePicker.getCurrentHour());
            int minute = storage.getInt(KEY_MINUTE, timePicker.getCurrentMinute());

            editMinutes.setText(String.valueOf(interval));

            timePicker.setCurrentHour(hour);

            timePicker.setCurrentMinute(minute);
        } else {
            btnNotify.setText("Notificar");
            btnNotify.setBackgroundResource(R.drawable.bg_button_background_accent);
        }

    }

    private void updateStorage(boolean added, int interval, int hour, int minute) {
        SharedPreferences.Editor editor = storage.edit();
        editor.putBoolean("activated", added);

        if (added) {
            editor.putInt(KEY_INTERVAL, interval);
            editor.putInt(KEY_HOUR, hour);
            editor.putInt(KEY_MINUTE, minute);
        } else {
            editor.remove("interval");
            editor.remove("hour");
            editor.remove("minute");
        }

        editor.apply();
    }

    private void setupNotification(boolean added, int interval, int hour, int minute) {
        Intent notificationIntent = new Intent(MainActivity.this, NotificationPublisher.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (added) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION_ID, 1);
            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION, "Hora de beber água !");

            PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);


            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval * 60 * 1000, broadcast);
        } else {

            PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent, 0);

            alarmManager.cancel(broadcast);

        }
    }

    private boolean intervalIsValid() {
        String sInterval = editMinutes.getText().toString();
        if (sInterval.isEmpty()) {
            alert(R.string.validation);
            return false;
        }
        if (sInterval.equals("0")) {
            alert(R.string.zero_value);
            return false;
        }
        return true;
    }

    private View.OnClickListener notifyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!activated) {

                if (!intervalIsValid()) return;

                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();
                int interval = Integer.parseInt(editMinutes.getText().toString());

                updateStorage(true, interval, hour, minute);
                setupUI(true, storage);
                setupNotification(true, interval, hour, minute);
                alert(R.string.notified);

                activated = true;
            } else {
                updateStorage(false, 0, 0, 0);
                setupUI(false, storage);
                setupNotification(false, 0, 0, 0);
                alert(R.string.notified_pause);

                activated = false;
            }
        }
    };

}


