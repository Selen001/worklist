
package com.sakshi.worklist;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class ReminderSettingsActivity extends Activity {

    private Button btnWakeTime;
    private RadioGroup rgReminderDelay;
    private TextView tvReminderPreview;

    private int wakeHour = 7;
    private int wakeMinute = 0;
    private int delayMinutes = 30;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);

        btnWakeTime = findViewById(R.id.btnWakeTime);
        rgReminderDelay = findViewById(R.id.rgReminderDelay);
        tvReminderPreview = findViewById(R.id.tvReminderPreview);

        prefs = getSharedPreferences("WorkListSettings", MODE_PRIVATE);

        loadSettings();

        btnWakeTime.setOnClickListener(v -> {
            new TimePickerDialog(
                    ReminderSettingsActivity.this,
                    (view, hourOfDay, minute) -> {
                        wakeHour = hourOfDay;
                        wakeMinute = minute;
                        updateWakeTimeButton();
                        updateReminderPreview();
                    },
                    wakeHour,
                    wakeMinute,
                    false
            ).show();
        });

        rgReminderDelay.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb15Minutes) {
                delayMinutes = 15;
            } else if (checkedId == R.id.rb30Minutes) {
                delayMinutes = 30;
            } else if (checkedId == R.id.rb1Hour) {
                delayMinutes = 60;
            } else if (checkedId == R.id.rb90Minutes) {
                delayMinutes = 90;
            }

            updateReminderPreview();
        });

        findViewById(R.id.btnSaveReminderSettings).setOnClickListener(v -> {
            prefs.edit()
                    .putInt("wakeHour", wakeHour)
                    .putInt("wakeMinute", wakeMinute)
                    .putInt("delayMinutes", delayMinutes)
                    .apply();

            Toast.makeText(
                    this,
                    "Saved! Morning reminder: " + getReminderTime(),
                    Toast.LENGTH_LONG
            ).show();
        });

        updateWakeTimeButton();
        updateReminderPreview();
    }

    private void loadSettings() {
        wakeHour = prefs.getInt("wakeHour", 7);
        wakeMinute = prefs.getInt("wakeMinute", 0);
        delayMinutes = prefs.getInt("delayMinutes", 30);

        if (delayMinutes == 15) {
            rgReminderDelay.check(R.id.rb15Minutes);
        } else if (delayMinutes == 60) {
            rgReminderDelay.check(R.id.rb1Hour);
        } else if (delayMinutes == 90) {
            rgReminderDelay.check(R.id.rb90Minutes);
        } else {
            delayMinutes = 30;
            rgReminderDelay.check(R.id.rb30Minutes);
        }
    }

    private void updateWakeTimeButton() {
        btnWakeTime.setText(
                "Wake-up time: " +
                        String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                wakeHour,
                                wakeMinute
                        )
        );
    }

    private String getReminderTime() {
        int totalMinutes =
                wakeHour * 60 + wakeMinute + delayMinutes;

        totalMinutes %= 24 * 60;

        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                totalMinutes / 60,
                totalMinutes % 60
        );
    }

    private void updateReminderPreview() {
        tvReminderPreview.setText(
                "Your morning reminder: " + getReminderTime()
        );
    }
}