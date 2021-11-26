package com.vijay.jsonwizard.listeners;


import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.customviews.MaterialTextInputLayout;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class TimePickerListener {

    private MaterialTimePicker d;
    private TextInputEditText timeText;
    private static final String TAG = "TimePickerListener";
    private String formatString;
    private static FragmentManager fragmentManager;

    public TimePickerListener(TextInputEditText textInputEditText, String formatString, FragmentManager fragmentManager) {
        this.timeText = textInputEditText;
        this.formatString = formatString;
        this.fragmentManager = fragmentManager;
    }


    public void openTimePicker(View view) {
        int hour = 0;
        int minute = 0;
        String timeStr = timeText.getText().toString();
        String pattern = (String) timeText.getTag(R.id.v_pattern);
        if (timeStr != null && !"".equals(timeStr)) {
            try {
                SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
                Calendar c = Calendar.getInstance();
                c.setTime(dateFormatter.parse(timeStr));
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);

            } catch (ParseException e) {
                Log.e(TAG, "Error parsing " + timeStr + ": " + e.getMessage());
            }
        } else {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(hour).setMinute(minute);
        builder.setTheme(R.style.widget_material_timepicker);
        d = builder.build();

        d.addOnPositiveButtonClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               timeText.setText(String.format("%02d:%02d", d.getHour(), d.getMinute()));
               d.dismiss();
           }
        });
        
        d.show(this.fragmentManager, TAG);
    }
}