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
   //RadioGroup radioGroup;
    RadioButton[] rbsprinklers = new RadioButton[3];
    RadioButton[] rbdayofweek = new RadioButton [7];

    TimePickerDialog timePickerDialog;
   TextView chooseTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_mqtt_setting);

        rbsprinklers[0]=(RadioButton) findViewById(R.id.radio_one_spr);
        rbsprinklers[1]=(RadioButton) findViewById(R.id.radio_two_spr);
        rbsprinklers[2]=(RadioButton) findViewById(R.id.radio_three_spr);

        rbdayofweek[0]=(RadioButton) findViewById(R.id.rbDayOfWeek0);
        rbdayofweek[1]=(RadioButton) findViewById(R.id.rbDayOfWeek1);
        rbdayofweek[2]=(RadioButton) findViewById(R.id.rbDayOfWeek2);
        rbdayofweek[3]=(RadioButton) findViewById(R.id.rbDayOfWeek3);
        rbdayofweek[4]=(RadioButton) findViewById(R.id.rbDayOfWeek4);
        rbdayofweek[5]=(RadioButton) findViewById(R.id.rbDayOfWeek5);
        rbdayofweek[6]=(RadioButton) findViewById(R.id.rbDayOfWeek6);

        chooseTime=findViewById(R.id.editTextTimeSprinkler);
        chooseTime.setText("Set Time");
    }

    public void SprinklerscheckButton(View view)
    {
     for (int i=0 ; i<3; i++)
     {
      if (rbsprinklers[i].isChecked())
             Log.w("radio", "Sprinkler: " + Integer.toString(i));
     }
    }

    public void WeekDaysCheckButton(View view)
    {
        for (int i=0 ; i<7; i++)
        {
            if (rbdayofweek[i].isChecked())
                Log.w("radio", "Day of week: " + Integer.toString(i));
        }
    }

    public void TimeForSprinklersStart(View view)
    {
     TimePickerDialog mTimePicker;

     Log.w("clicking","aaa");
     mTimePicker = new TimePickerDialog(MainActivityMqttSetting.this, new TimePickerDialog.OnTimeSetListener()
     {
         @Override
         public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
         {
          chooseTime.setText( selectedHour + ":" + selectedMinute);
         }
      }, 24, 0,true);
     mTimePicker.setTitle("Select Time");
     mTimePicker.show();
    }
}