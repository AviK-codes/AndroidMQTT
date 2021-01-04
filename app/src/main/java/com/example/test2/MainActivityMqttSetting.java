package com.example.test2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.view.View;
import android.util.Log;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

public class MainActivityMqttSetting extends AppCompatActivity
{
    TextView[] tvTimessp1 = new TextView[7];
    TextView[] tvTimessp2 = new TextView[7];
    TextView[] tvTimessp3 = new TextView[7];

    TimePickerDialog timePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_mqtt_setting);
        String spname;

        for (int i=0 ; i<7; i++)
        {
         spname="textViewsp1d"+Integer.toString(i);
         int id = getResources().getIdentifier(spname, "id", getPackageName());
         if (id != 0 )
            {
             tvTimessp1[i] = (TextView) findViewById(id);
             tvTimessp1[i].setText("Set Time");
            }
        }

        for (int i=0 ; i<7; i++)
        {
            spname="textViewsp2d"+Integer.toString(i);
            int id = getResources().getIdentifier(spname, "id", getPackageName());
            if (id != 0 )
            {
                tvTimessp2[i] = (TextView) findViewById(id);
                tvTimessp2[i].setText("Set Time");
            }
        }

        for (int i=0 ; i<7; i++)
        {
            spname="textViewsp3d"+Integer.toString(i);
            int id = getResources().getIdentifier(spname, "id", getPackageName());
            if (id != 0 )
            {
                tvTimessp3[i] = (TextView) findViewById(id);
                tvTimessp3[i].setText("Set Time");
            }
        }
    }

    public void TimeForSprinklersStart(View view)
    {
     TimePickerDialog mTimePicker;
     TextView sptime = (TextView) findViewById(view.getId());

     Log.w("clicking","aaa");
     mTimePicker = new TimePickerDialog(MainActivityMqttSetting.this, new TimePickerDialog.OnTimeSetListener()
     {
         @Override
         public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
         {
          sptime.setText( selectedHour + ":" + selectedMinute);
         }
      }, 24, 0,true);

     mTimePicker.setTitle("Select Time");
     mTimePicker.show();
    }
}