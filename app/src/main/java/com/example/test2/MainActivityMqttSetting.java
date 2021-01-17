package com.example.test2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.util.Log;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.NumberPicker;
import android.widget.Toast;

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

    public TextView previous_sptime;
    boolean start_time = true;
    public int first_hour = 0, first_minute = 0, second_hour, second_minute;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_mqtt_setting);
        String spname;
        boolean is_file = false;

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
       previous_sptime =  (TextView) findViewById(R.id.textView89); // assign the comparison pointer to an unused textview field
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
    public void ComposeMsgToDevice(View view)
    {
        String daysofweek[]={"Sunday","Monday","Tuesday","wednesday","Thursday","Friday","Saturday"};

        JSONArray timedarray1 = new JSONArray();
        JSONArray timedarray2 = new JSONArray();
        JSONArray timedarray3 = new JSONArray();

        for (int i=0; i<7; i++)
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

        if (createFile(this, "sprinkler1",jsonObjSprinkler1.toString())) Log.w("file1","OK");
        if (createFile(this, "sprinkler2",jsonObjSprinkler2.toString())) Log.w("file2","OK");
        if (createFile(this, "sprinkler3",jsonObjSprinkler3.toString())) Log.w("file3","OK");
        MainActivity.getInstance().SetDeviceSettings("iot-2/type/aweHGH7/id/irri555/cmd/solenoid1daytimeduration/fmt/json",jsonObjSprinkler1.toString());
        MainActivity.getInstance().SetDeviceSettings("iot-2/type/aweHGH7/id/irri555/cmd/solenoid2daytimeduration/fmt/json",jsonObjSprinkler2.toString());
        MainActivity.getInstance().SetDeviceSettings("iot-2/type/aweHGH7/id/irri555/cmd/solenoid3daytimeduration/fmt/json",jsonObjSprinkler3.toString());
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

}