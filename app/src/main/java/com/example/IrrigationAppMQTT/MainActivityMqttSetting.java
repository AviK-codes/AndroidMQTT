package com.example.IrrigationAppMQTT;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.util.Log;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivityMqttSetting extends AppCompatActivity
{
    TextView[] tvTimessp1 = new TextView[7];
    TextView[] tvTimessp2 = new TextView[7];
    TextView[] tvTimessp3 = new TextView[7];

    int[][] Soleniod1DayTimeDuration = new int[7][3];
    int[][] Soleniod2DayTimeDuration = new int[7][3];
    int[][] Soleniod3DayTimeDuration = new int[7][3];

    JSONObject jsonObjSprinkler1 = new JSONObject();
    JSONObject jsonObjSprinkler2 = new JSONObject();
    JSONObject jsonObjSprinkler3 = new JSONObject();
    JSONObject jsonObjSprinkler11;
    JSONObject jsonObjSprinkler22;
    JSONObject jsonObjSprinkler33;
    CheckBox chkbox;
    private NumberPicker picker_manual_time;

    public TextView previous_sptime;
    boolean start_time = true;
    public int first_hour = 0, first_minute = 0, second_hour, second_minute;
    private static MainActivityMqttSetting instance;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_mqtt_setting);
        instance = this;
        String spname;
        boolean is_file = false;
        chkbox= (CheckBox) findViewById(R.id.checkBoxDisEna);
        picker_manual_time = findViewById(R.id.numberpicker_main_picker);
        picker_manual_time.setMaxValue(60);
        picker_manual_time.setMinValue(1);

        picker_manual_time.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                int valuePicker1 = picker_manual_time.getValue();
                Log.d("picker value", String. valueOf(valuePicker1));
            }
        });

        final Button button = (Button) findViewById(R.id.buttonConfigDevice);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ComposeMsgToDevice();
            }
        });

        try
        {
            String sprinkler1data = readFile(this, "sprinkler1");
            if (sprinkler1data != null)
            {
                is_file = true;
                jsonObjSprinkler11 = new JSONObject(sprinkler1data);
                jsonObjSprinkler22 = new JSONObject(readFile(this, "sprinkler2"));
                jsonObjSprinkler33 = new JSONObject(readFile(this, "sprinkler3"));
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        for (int i = 0; i < 7; i++)
        {
            spname = "textViewsp1d" + Integer.toString(i);
            int id = getResources().getIdentifier(spname, "id", getPackageName());
            if (id != 0)
            {
              tvTimessp1[i] = (TextView) findViewById(id); // pointers to textview of days of week Sunday (0) to Saturday (6) for Sprinkler 1
              tvTimessp1[i].setText("Set Time");
              if (is_file) SetTimesUiFields(tvTimessp1[i],jsonObjSprinkler11,Soleniod1DayTimeDuration,i);
            }
        }
        for (int i = 0; i < 7; i++)
        {
            spname = "textViewsp2d" + Integer.toString(i);
            int id = getResources().getIdentifier(spname, "id", getPackageName());
            if (id != 0)
            {
              tvTimessp2[i] = (TextView) findViewById(id); // pointers to textview of days of week Sunday (0) to Saturday (6) for Sprinkler 2
              tvTimessp2[i].setText("Set Time");
              if (is_file) SetTimesUiFields(tvTimessp2[i],jsonObjSprinkler22,Soleniod2DayTimeDuration,i);
            }
        }

        for (int i = 0; i < 7; i++)
        {
            spname = "textViewsp3d" + Integer.toString(i);
            int id = getResources().getIdentifier(spname, "id", getPackageName());
            if (id != 0)
            {
              tvTimessp3[i] = (TextView) findViewById(id); // pointers to textview of days of week Sunday (0) to Saturday (6) for Sprinkler 3
              tvTimessp3[i].setText("Set Time");
              if (is_file) SetTimesUiFields(tvTimessp3[i],jsonObjSprinkler33,Soleniod3DayTimeDuration,i);
            }
        }
    }

    void SetTimesUiFields(TextView tv, JSONObject js, int[][] timedayarray, int i)
    {
     JSONArray timeinday;
     String daysofweek[] = {"Sunday", "Monday", "Tuesday", "wednesday", "Thursday", "Friday", "Saturday"};

     try
     {
            int hour2 = 0, minute2 = 0;
            timeinday = js.getJSONArray(daysofweek[i]);
            int hour = timeinday.getInt(0);
            int minute = timeinday.getInt(1);
            int duration = timeinday.getInt(2);
            if (duration==0) return;
            if ((minute + duration) >= 60)
            {
                hour2 = hour + 1;
                minute2 = (minute + duration) - 60;
            } else
            {
                hour2 = hour;
                minute2 = minute + duration;
            }
            timedayarray[i][0]=hour;
            timedayarray[i][1]=minute;
            timedayarray[i][2]=duration;
            tv.setText(hour + ":" + minute + "->" + hour2 + ":" + minute2);
            tv.setTextColor(Color.rgb(0, 200, 0)); // green

     } catch(JSONException e)
     {
         e.printStackTrace();
     }
    }

    public void TimeForSprinklersStart(View view)
    {
     TimePickerDialog mTimePicker;

     TextView sptime = (TextView) findViewById(view.getId());

     if (sptime==previous_sptime)
      {
       Toast.makeText(getApplicationContext(), "Enter the end time for the sprinkler",Toast.LENGTH_LONG).show();
       previous_sptime =  (TextView) findViewById(R.id.textViewSunday); // assign the comparison pointer to an unused textview field
       start_time=false;
      }
     else
      {
       Toast.makeText(getApplicationContext(), "Enter the start time for the sprinkler", Toast.LENGTH_LONG).show();
       previous_sptime=sptime;
       start_time=true;
      }

     mTimePicker = new TimePickerDialog(MainActivityMqttSetting.this, new TimePickerDialog.OnTimeSetListener()
     {
         int delta;

         @Override
         public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
         {
          if (start_time)
          {
           sptime.setText(selectedHour + ":" + selectedMinute);
           first_hour=selectedHour;
           first_minute=selectedMinute;
           sptime.setTextColor(Color.rgb(200,0,0));
          }
          else
          {
           second_hour=selectedHour;
           second_minute=selectedMinute;
           delta = (second_hour*60+second_minute)-(first_hour*60+first_minute);
             if ((delta>60)||(delta <0))
             {
              //delta = 60;
              //second_hour=first_hour+1;
              if (delta>60) Toast.makeText(getApplicationContext(), "Irrigation time can't exceed 60 minutes, please renter", Toast.LENGTH_LONG).show();
              if (delta<0) Toast.makeText(getApplicationContext(), "Start time must be earlier to end time, please renter", Toast.LENGTH_LONG).show();
              sptime.setTextColor(Color.rgb(200,0,0)); // red
             }
             else
             {
               sptime.setText(first_hour + ":" + first_minute + "->" + second_hour + ":" + second_minute);
               sptime.setTextColor(Color.rgb(0, 200, 0)); // green

               // add the code for array assignment
               for (int i = 0; i < 7; i++)
               {
                   if (tvTimessp1[i] == sptime)
                   {
                       Soleniod1DayTimeDuration[i][0] = first_hour;
                       Soleniod1DayTimeDuration[i][1] = first_minute;
                       Soleniod1DayTimeDuration[i][2] = delta;
                   }
                   if (tvTimessp2[i] == sptime)
                   {
                       Soleniod2DayTimeDuration[i][0] = first_hour;
                       Soleniod2DayTimeDuration[i][1] = first_minute;
                       Soleniod2DayTimeDuration[i][2] = delta;
                   }
                   if (tvTimessp3[i] == sptime)
                   {
                       Soleniod3DayTimeDuration[i][0] = first_hour;
                       Soleniod3DayTimeDuration[i][1] = first_minute;
                       Soleniod3DayTimeDuration[i][2] = delta;
                   }
               }
             }
          }

         }
      }, first_hour, first_minute,true);

     mTimePicker.setTitle("Select Time");
     mTimePicker.show();
    }

    // prepare json data for sending it to the remote device
    public void ComposeMsgToDevice()
    {
        String daysofweek[]={"Sunday","Monday","Tuesday","wednesday","Thursday","Friday","Saturday"};

        JSONArray timedarray1 = new JSONArray();
        JSONArray timedarray2 = new JSONArray();
        JSONArray timedarray3 = new JSONArray();
        CheckBox chkbox=findViewById(R.id.checkBoxDisEna);
        boolean on = ((CheckBox) chkbox).isChecked();
        if (on)
        {
            MainActivity.getInstance().SetDeviceSettings(MainActivity.getInstance().topic_setirrigationstate,"{'Irrigation' : 'ENABLE'}");
            Log.w("Irrigation enabled", "Irri");

            for (int i = 0; i < 7; i++)
            {
                timedarray1.put(Integer.toString(Soleniod1DayTimeDuration[i][0]));
                timedarray1.put(Integer.toString(Soleniod1DayTimeDuration[i][1]));
                timedarray1.put(Integer.toString(Soleniod1DayTimeDuration[i][2]));

                timedarray2.put(Integer.toString(Soleniod2DayTimeDuration[i][0]));
                timedarray2.put(Integer.toString(Soleniod2DayTimeDuration[i][1]));
                timedarray2.put(Integer.toString(Soleniod2DayTimeDuration[i][2]));

                timedarray3.put(Integer.toString(Soleniod3DayTimeDuration[i][0]));
                timedarray3.put(Integer.toString(Soleniod3DayTimeDuration[i][1]));
                timedarray3.put(Integer.toString(Soleniod3DayTimeDuration[i][2]));
                try
                {
                    jsonObjSprinkler1.put(daysofweek[i], timedarray1);
                    jsonObjSprinkler2.put(daysofweek[i], timedarray2);
                    jsonObjSprinkler3.put(daysofweek[i], timedarray3);
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
                timedarray1 = new JSONArray();
                timedarray2 = new JSONArray();
                timedarray3 = new JSONArray();
            }
            Log.w("jsonarray Sprink 1", jsonObjSprinkler1.toString());
            Log.w("jsonarray Sprink 2", jsonObjSprinkler2.toString());
            Log.w("jsonarray Sprink 3", jsonObjSprinkler3.toString());

            if (createFile(this, "sprinkler1", jsonObjSprinkler1.toString())) Log.w("file1", "OK");
            if (createFile(this, "sprinkler2", jsonObjSprinkler2.toString())) Log.w("file2", "OK");
            if (createFile(this, "sprinkler3", jsonObjSprinkler3.toString())) Log.w("file3", "OK");

            MainActivity.getInstance().SetDeviceSettings(MainActivity.getInstance().topic_solenoid1daytimeduration, jsonObjSprinkler1.toString());
            MainActivity.getInstance().SetDeviceSettings(MainActivity.getInstance().topic_solenoid2daytimeduration, jsonObjSprinkler2.toString());
            MainActivity.getInstance().SetDeviceSettings(MainActivity.getInstance().topic_solenoid3daytimeduration, jsonObjSprinkler3.toString());

        }
        else
        {
            MainActivity.getInstance().SetDeviceSettings(MainActivity.getInstance().topic_setirrigationstate, "{'Irrigation' : 'DISABLE'}");
            Log.w("Irrigation disabled", "Irri");
        }
    }

    private String readFile(Context context, String fileName)
    {
        try
        {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                sb.append(line);
            }
            return sb.toString();
        }
        catch (FileNotFoundException fileNotFound)
        {
            return null;
        }
        catch (IOException ioException)
        {
            return null;
        }
    }

    // one version of file create
    private boolean createFile(Context context, String fileName, String jsonString)
    {
        String FILENAME = "storage.json";
        try
        {
            FileOutputStream fos = context.openFileOutput(fileName,Context.MODE_PRIVATE);
            if (jsonString != null)
            {
                fos.write(jsonString.getBytes());
            }
            fos.close();
            return true;
        }
        catch (FileNotFoundException fileNotFound)
        {
            return false;
        }
        catch (IOException ioException)
        {
            return false;
        }
    }

    // second version of file create
    public void createFile()
    {
        try
        {
            File checkFile = new File(getApplicationInfo().dataDir + "/new_directory_name/");
            if (!checkFile.exists())
            {
                checkFile.mkdir();
            }
            FileWriter file = new FileWriter(checkFile.getAbsolutePath() + "/filename");
            file.write("what you want to write in internal storage");
            file.flush();
            file.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void ManualToogleSprinkler(View view)
    {
        ToggleButton simpleToggleButton = (ToggleButton) findViewById(view.getId());
        if (simpleToggleButton == findViewById(R.id.toggleButton1))
        {
            boolean on = ((ToggleButton) view).isChecked();
            if (on)
            {
             String mqttjsonmsg = "{'Solenoid1' : ['ENABLE' , '"+ String.valueOf(picker_manual_time.getValue()) + "' ]}";
             MainActivity.getInstance().SetDeviceSettings(MainActivity.getInstance().topic_setsolenoidsstatus,mqttjsonmsg);
             Log.w("toogle on", "spr1");
            }
            else
            {
             MainActivity.getInstance().SetDeviceSettings(MainActivity.getInstance().topic_setsolenoidsstatus, "{'Solenoid1' : ['DISABLE' , '0']}");
             Log.w("toogle off", "spr1");
            }
        }
        if (simpleToggleButton == findViewById(R.id.toggleButton2))
        {
            boolean on = ((ToggleButton) view).isChecked();
            if (on)
            {
             String mqttjsonmsg = "{'Solenoid2' : ['ENABLE' , '"+ String.valueOf(picker_manual_time.getValue()) + "' ]}";
             MainActivity.getInstance().SetDeviceSettings(MainActivity.getInstance().topic_setsolenoidsstatus,mqttjsonmsg);
             Log.w("toogle on", "spr2");
            }
            else
            {
             MainActivity.getInstance().SetDeviceSettings(MainActivity.getInstance().topic_setsolenoidsstatus,"{'Solenoid2' : ['DISABLE', '0']}");
             Log.w("toogle off", "spr2");
            }
        }
        if (simpleToggleButton == findViewById(R.id.toggleButton3))
        {
            boolean on = ((ToggleButton) view).isChecked();
            if (on)
            {
             String mqttjsonmsg = "{'Solenoid3' : ['ENABLE' , '"+ String.valueOf(picker_manual_time.getValue()) + "' ]}";
             MainActivity.getInstance().SetDeviceSettings(MainActivity.getInstance().topic_setsolenoidsstatus,mqttjsonmsg);
             Log.w("toogle on", "spr3");
            }
            else
            {
             MainActivity.getInstance().SetDeviceSettings(MainActivity.getInstance().topic_setsolenoidsstatus,"{'Solenoid3' : ['DISABLE', '0']}");
             Log.w("toogle off", "spr3");
            }
        }
    }

    public static MainActivityMqttSetting getInstance()
    {
        return instance;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            MainActivity.getInstance().pollDeviceForItsSettings();
            //return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void setDisablechkbox(boolean state)
    {
        chkbox.setChecked(state);
    }

    public void setManualIrriToggSwitchState(String solenoid1, String solenoid2, String solenoid3)
    {
        ToggleButton simpleToggleButton;
        simpleToggleButton = findViewById(R.id.toggleButton1);

        if (solenoid1.equalsIgnoreCase("ON")) simpleToggleButton.setChecked(true);
            else simpleToggleButton.setChecked(false);
        simpleToggleButton = findViewById(R.id.toggleButton2);

        if (solenoid2.equalsIgnoreCase("ON")) simpleToggleButton.setChecked(true);
            else simpleToggleButton.setChecked(false);

        simpleToggleButton = findViewById(R.id.toggleButton3);
        if (solenoid3.equalsIgnoreCase("ON")) simpleToggleButton.setChecked(true);
            else simpleToggleButton.setChecked(false);
    }
}