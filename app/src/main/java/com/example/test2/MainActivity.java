package com.example.test2;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
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

import static android.os.SystemClock.sleep;

//import helpers.MqttHelper;

public class MainActivity extends AppCompatActivity {

    //MqttHelper mqttHelper;
    MqttAndroidClient client;
    TextView dataReceived;

    /*
    For the application connected to IBM MQTT broker:

    API Key: a-by94pk-uexpeh6kli
    Authentication Token: z2N6b4m0GCC-C&)7Si
    User ID: a:by94pk:APP1
     */
    //final String url = "ssl://by94pk.messaging.internetofthings.ibmcloud.com:8883";
    //final String username = "a-by94pk-uexpeh6kli";
    //final String password = "z2N6b4m0GCC-C&)7Si";
    //final String clientId = "a:by94pk:APP1";

    //String subtopic = "iot-2/type/aweHGH7/id/irri555/evt/+/fmt/json";

    final String org_id="u1gsdw";
    final String device_type="IrriCntrl";
    final String device_id="irri546";
    final String password = "m7OP37gK8tOi6ETPW&";

    final String url = "ssl://"+org_id+".messaging.internetofthings.ibmcloud.com:8883";
    final String username = "a-"+org_id+"-aljwpdhub2";
    final String clientId = "a:"+org_id+":APP1";

    String subtopic = "iot-2/type/"+device_type+"/id/"+device_id+"/evt/+/fmt/json";
    String topic_solenoid1daytimeduration="iot-2/type/"+device_type+"/id/"+device_id+"/cmd/solenoid1daytimeduration/fmt/json";
    String topic_solenoid2daytimeduration="iot-2/type/"+device_type+"/id/"+device_id+"/cmd/solenoid2daytimeduration/fmt/json";
    String topic_solenoid3daytimeduration="iot-2/type/"+device_type+"/id/"+device_id+"/cmd/solenoid3daytimeduration/fmt/json";
    String topic_setsolenoidsstatus = "iot-2/type/"+device_type+"/id/"+device_id+"/cmd/setsolenoidsstatus/fmt/json";
    String topic_setirrigationstate= "iot-2/type/"+device_type+"/id/"+device_id+"/cmd/setirrigationstate/fmt/json";

    public MqttAndroidClient mqttAndroidClient;
    String devicedata="";
    private static MainActivity instance;
    boolean DeviceIrrigationStatus= true;
    String mqttmsgirrigationstat="";
    /*
        final String url = "tcp://broker.emqx.io";
        final String username = "";
        final String password = "";
        String clientId = "aaa";
        String subtopic = "/sensor/temp";
    */
    boolean link_status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        final int CONNECTION_TIMEOUT = 600;
        final int KEEP_ALIVE_INTERVAL = 200;

        dataReceived = (TextView) findViewById(R.id.dataReceived);
        //dataReceived.setMovementMethod(new ScrollingMovementMethod());
        //dataReceived.setElegantTextHeight(true);
        //dataReceived.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //dataReceived.setSingleLine(false);
        dataReceived.setMovementMethod(new ScrollingMovementMethod());
        //startMqtt();
        mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(), url, clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.w("Mqtt", "Connection was lost!");
                link_status = false;
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

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setConnectionTimeout(CONNECTION_TIMEOUT);
        mqttConnectOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    link_status = true;
                    Log.w("Mqtt", "Connection Success!");
                    try {
                        Log.w("Mqtt", "Subscribing to");
                        Log.w("Mqtt", subtopic);
                        mqttAndroidClient.subscribe(subtopic, 0);
                        Log.w("Mqtt","Subscribed to topic"); // subscribe to all topics coming from device
                    } catch (MqttException ex) {

                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Connection Failure!");
                    link_status = false;
                }
            });
        } catch (MqttException ex) {

        }
    }

    public void getDeviceSettings(View view)
    {
        pollDeviceForItsSettings();
    }

    public void pollDeviceForItsSettings()
    {
        //TextView textView = findViewById(R.id.dataReceived);
        String topic_getsolenoidsstatus="iot-2/type/"+device_type+"/id/"+device_id+"/cmd/getsolenoidsstatus/fmt/json";
        String topic_getsolenoidssettings="iot-2/type/"+device_type+"/id/"+device_id+"/cmd/getsolenoidssettings/fmt/json";
        String topic_getdevicetime="iot-2/type/"+device_type+"/id/"+device_id+"/cmd/getdevicetime/fmt/json";

        devicedata="";
        try {
            if (link_status == true)
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

     JSONObject reader = new JSONObject(data.toString());

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
        devicedata += "\nIrrigation is: "+mqttmsgirrigationstat+"\nSprinklers current status:\nSprinkler 1 is: " + sprinkler1stat + ", Sprinkler 2 is: " + sprinkler2stat + ", Sprinkler 3 is: " + sprinkler3stat;
      }

     if (!mqttmsgirrigationstat.isEmpty())
     {
      if (mqttmsgirrigationstat.equalsIgnoreCase("Enabled")) DeviceIrrigationStatus = true;
      else DeviceIrrigationStatus = false;
      MainActivityMqttSetting aa=MainActivityMqttSetting.getInstance();
      if (aa!=null)
      {
          MainActivityMqttSetting.getInstance().tbutton.setChecked(DeviceIrrigationStatus);
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
            if (link_status == true)
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
}
/*
    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext());

        mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Debug","Connected");
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.w("Debug","connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug",mqttMessage.toString());
                dataReceived.setText(mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
*/

