package com.example.IrrigationAppMQTT;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity {

    MqttAndroidClient client;
    TextView dataReceived;

    final String url = "tcp://152.67.67.153:1883"; // RabbitMQ mqtt broker installed on Oracle free cloud
    final String username = "avik";
    final String password = "Fg532ccv&*a1!a0";

    final String clientId = "AndroidApplication";

    String subtopic = "iot-2/evt/+/fmt/json"; // subscribing for messages sent by the irrigation system
    String topic_solenoid1daytimeduration="iot-2/cmd/solenoid1daytimeduration/fmt/json";
    String topic_solenoid2daytimeduration="iot-2/cmd/solenoid2daytimeduration/fmt/json";
    String topic_solenoid3daytimeduration="iot-2/cmd/solenoid3daytimeduration/fmt/json";
    String topic_setsolenoidsstatus = "iot-2/cmd/setsolenoidsstatus/fmt/json";
    String topic_setirrigationstate= "iot-2/cmd/setirrigationstate/fmt/json";

    public MqttAndroidClient mqttAndroidClient;
    String devicedata="";
    private static MainActivity instance;
    String mqttmsgirrigationstat="";

    Thread checklinkstatus=null; // thread to check the link status every 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checklinkstatus = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(5000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updatelinkstatus();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        instance = this;

        dataReceived = (TextView) findViewById(R.id.dataReceived);
        dataReceived.setMovementMethod(new ScrollingMovementMethod());

        ConnectToMQTTServer(null);
        checklinkstatus.start(); // run the thread to check the link status every 5 seconds
    }

    public void getDeviceSettings(View view)
    {
        devicedata="";
        //dataReceived.setText("");
        pollDeviceForItsSettings();
    }

    public void pollDeviceForItsSettings()
    {
        String topic_getsolenoidsstatus="iot-2/cmd/getsolenoidsstatus/fmt/json";
        String topic_getsolenoidssettings="iot-2/cmd/getsolenoidssettings/fmt/json";
        String topic_getdevicetime="iot-2/cmd/getdevicetime/fmt/json";

        try {
            if ((mqttAndroidClient!=null)&&mqttAndroidClient.isConnected())
            {
                Log.w("Mqtt", "Publishing message..");
                mqttAndroidClient.publish(topic_getdevicetime, new MqttMessage("{'time':'?'}".getBytes()));
                mqttAndroidClient.publish(topic_getsolenoidsstatus, new MqttMessage("{'Solenoids' : '?'}".getBytes()));
                mqttAndroidClient.publish(topic_getsolenoidssettings, new MqttMessage("{'solenoid':'1'}".getBytes()));
                mqttAndroidClient.publish(topic_getsolenoidssettings, new MqttMessage("{'solenoid':'2'}".getBytes()));
                mqttAndroidClient.publish(topic_getsolenoidssettings, new MqttMessage("{'solenoid':'3'}".getBytes()));
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // parse the settings data json received for the device in order to present it on Android activity screen
    public void SednDeviceDataToScreen(String topic, String data) throws JSONException
    {
     // https://www.tutorialspoint.com/android/android_json_parser.htm
     JSONArray timeinday;
     String daysofweek[]={"Sunday","Monday","Tuesday","wednesday","Thursday","Friday","Saturday"};

     SetButtonsText(true);

     JSONObject reader = new JSONObject(data.toString());
     MainActivityMqttSetting settingform=MainActivityMqttSetting.getInstance(); // retrieve pointer of the second activity which contains the setting forms

     if (topic.contains("devicetimestatus"))
      {
       devicedata = "Device time: " + reader.getString("Day") + ", " + reader.getString("Date") + ", " + reader.getString("Hour") + ":" + reader.getString("Minute") + "\n";
      }

     if (topic.contains("reportsolenoidsstatus"))
      {
        Log.w("jsonporblem",reader.toString());
        String sprinkler1stat = reader.optString("Solenoid1"); // using optString instead of getString saves the exception in case a key value is missing
        String sprinkler2stat = reader.optString("Solenoid2");
        String sprinkler3stat = reader.optString("Solenoid3");
        mqttmsgirrigationstat = reader.optString("Irrigation");


          if (settingform!=null) // check if MainActivityMqttSetting was created
          {
           settingform.getInstance().setManualIrriToggSwitchState(sprinkler1stat,sprinkler2stat,sprinkler3stat); // update manual irrigation toggle switches
          }

        devicedata += "\nIrrigation is: "+mqttmsgirrigationstat+"\nSprinklers current status:\nSprinkler 1 is: " + sprinkler1stat + ", Sprinkler 2 is: " + sprinkler2stat + ", Sprinkler 3 is: " + sprinkler3stat;

        if (!mqttmsgirrigationstat.isEmpty())
          {
           boolean deviceirrigationstatus= true;
           if (mqttmsgirrigationstat.equalsIgnoreCase("Enabled")) deviceirrigationstatus = true;
           else deviceirrigationstatus = false;

            if (settingform!=null) // check if MainActivityMqttSetting was created
              {
               settingform.getInstance().setDisablechkbox(deviceirrigationstatus);
              }
          }
      }

     if (topic.contains("reportsolenoidssettings"))
       {
        try
         {
          //JSONObject reader = new JSONObject(data.toString());
          String solenoidnum=reader.getString("solenoid");

          if (solenoidnum.equals("1"))
           {
            devicedata += "\n\nSprinkler 1, Open time and duration:\n\n";
            for (int i = 0; i < 7; i++)
             {
              timeinday = reader.getJSONArray(daysofweek[i]);
              devicedata += daysofweek[i] + ", opens at: " + timeinday.getString(0) + ":" + timeinday.getString(1) + " for " + timeinday.getString(2) + " Minutes\n";
             }
           }

          if (solenoidnum.equals("2"))
           {
            devicedata += "\nSprinkler 2, Open time and duration:\n\n";
            for (int i = 0; i < 7; i++)
             {
              timeinday = reader.getJSONArray(daysofweek[i]);
              devicedata += daysofweek[i] + ", opens at: " + timeinday.getString(0) + ":" + timeinday.getString(1) + " for " + timeinday.getString(2) + " Minutes\n";
             }
           }

           if (solenoidnum.equals("3"))
            {
             devicedata += "\nSprinkler 3, Open time and duration:\n\n";
             for (int i = 0; i < 7; i++)
              {
               timeinday = reader.getJSONArray(daysofweek[i]);
               devicedata += daysofweek[i] + ", opens at: " + timeinday.getString(0) + ":" + timeinday.getString(1) + " for " + timeinday.getString(2) + " Minutes\n";
              }
            }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.w("jsonTxt2", "Not found");
            }
        }
        dataReceived.setText(devicedata);
    }

    public void setDeviceConfiguration(View view)
    {
     pollDeviceForItsSettings();
     Intent intent = new Intent(MainActivity.this, MainActivityMqttSetting.class);
     startActivity(intent);
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public void SetDeviceSettings(String msg_header, String msg)
    {
        devicedata="";
        try {
            if (mqttAndroidClient.isConnected())
            {
                Log.w("Mqtt", "Publishing message..");
                Log.w("jsonarray Sprink header tx", msg_header);
                Log.w("jsonarray Sprink msg tx", msg);
                mqttAndroidClient.publish(msg_header, new MqttMessage(msg.getBytes()));
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

   public void ConnectToMQTTServer(View view)
   {
       final int CONNECTION_TIMEOUT = 600;
       final int KEEP_ALIVE_INTERVAL = 300;
       devicedata="";

       stopService(new Intent(this, MqttService.class));
       startService(new Intent(this, MqttService.class));

       MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
       mqttConnectOptions.setAutomaticReconnect(true);
       //mqttConnectOptions.setAutomaticReconnect(false); // was true but automatic reconnect didn't work thus I've disabled it, only manual connection is working fine
       mqttConnectOptions.setCleanSession(false);
       mqttConnectOptions.setConnectionTimeout(CONNECTION_TIMEOUT);
       mqttConnectOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
       mqttConnectOptions.setUserName(username);
       mqttConnectOptions.setPassword(password.toCharArray());

       mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(), url, clientId);
       mqttAndroidClient.setCallback(new MqttCallback() {
           @Override
           public void connectionLost(Throwable cause) {
               Log.w("Mqtt", "Connection was lost!");
           }

           @Override
           public void messageArrived(String topic, MqttMessage message) throws Exception {
               Log.w("Mqtt","Message Arrived!: " + topic + ": " + new String(message.getPayload()));
               Log.w("Mqtt", message.toString());
               SednDeviceDataToScreen(topic, message.toString());
           }

           @Override
           public void deliveryComplete(IMqttDeliveryToken token) {
               Log.w("Mqtt", "Delivery Complete");
           }
       });

       try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
               @Override
               public void onSuccess(IMqttToken asyncActionToken) {

                   Log.w("Mqtt", "Connection Success!");

                   pollDeviceForItsSettings();

                       try
                       {
                               Log.w("Mqtt", "Subscribing to");
                               Log.w("Mqtt", subtopic);
                               mqttAndroidClient.subscribe(subtopic, 0);
                               Log.w("Mqtt", "Subscribed to topic"); // subscribe to all topics coming from device
                       } catch (MqttException ex)
                       {
                           Log.w("Mqtt problem", ex.getMessage());
                       }
               }

               @Override
               public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                   Log.w("Mqtt", "Connection Failure!");
                   String msg="not connected due to failure";
                   Log.w("Mqtt",msg);
               }
           });
       } catch (MqttException ex) {
           Log.w("Mqtt problem", ex.getMessage());
       }
   }

   void SetButtonsText(boolean set)
   {
     if (set)
     {
         Button button1 = (Button) findViewById(R.id.button_getDeviceSettings);
         button1.setText("קבל נתוני השקיה");
         Button button2 = (Button) findViewById(R.id.button_configDeviceSettings);
         button2.setText("עדכן זמני השקיה");
     }
     else
     {
         Button button1 = (Button) findViewById(R.id.button_getDeviceSettings);
         button1.setText("אין שרת...");
         Button button2 = (Button) findViewById(R.id.button_configDeviceSettings);
         button2.setText("אין שרת...");
     }
   }
   void updatelinkstatus()
   {
       if (mqttAndroidClient.isConnected())
       {
        SetButtonsText(true);
        Log.w("checklinkstatus", "connected");
       }
       else
       {
        SetButtonsText(false);
        Log.w("checklinkstatus", "disconnected");
       }

   }
    @Override
    protected void onPause()
    {
     super.onPause();
     checklinkstatus.interrupted();
    }
}

