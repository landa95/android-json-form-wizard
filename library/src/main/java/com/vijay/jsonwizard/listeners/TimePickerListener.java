package com.vijay.jsonwizard.listeners;


import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.rey.material.app.Dialog;
import com.rey.material.app.TimePickerDialog;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.customviews.MaterialTextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class TimePickerListener implements View.OnFocusChangeListener, View.OnClickListener {

    private MaterialTimePicker d;
    private MaterialTextInputLayout timeText;
    private static final String TAG = "TimePickerListener";
    private String formatString;
    private static FragmentManager fragmentManager;

    public TimePickerListener(MaterialTextInputLayout materialTextInputLayout, String formatString, FragmentManager fragmentManager) {
        this.timeText = materialTextInputLayout;
        this.formatString = formatString;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void onFocusChange(View view, boolean focus) {
        if (focus) {
            openTimePicker(view);
        }
    }

    @Override
    public void onClick(View view) {
        openTimePicker(view);
    }

    private void openTimePicker(View view) {
        int hour = 0;
        int minute = 0;
        String timeStr = timeText.getEditText().getText().toString();
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
        builder.setTheme(R.style.TimePicker);
        d = builder.build();

    /*  d.addOnPositiveButtonClickListener(new View.OnClickListener(){


          @Override
          public void onClick(View view) {
              d.dismiss();
          }
      });
        d = builder.build();

        d.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();
            }
        });*/

        //d.positiveAction("OK").negativeAction("CANCEL");
        d.show(this.fragmentManager, TAG);
    }
}