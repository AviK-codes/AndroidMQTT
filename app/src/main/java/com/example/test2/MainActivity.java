package com.example.test2;

import android.os.Bundle;
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
    final String url = "ssl://by94pk.messaging.internetofthings.ibmcloud.com:8883";
    //final String url = "tcp://by94pk.messaging.internetofthings.ibmcloud.com:1883";
    final String username = "a-by94pk-uexpeh6kli";
    final String password = "z2N6b4m0GCC-C&)7Si";
    final String clientId = "a:by94pk:APP1";
    //final String clientId = "a:by94pk:dsg4";
    String subtopic = "iot-2/type/aweHGH7/id/irri555/evt/+/fmt/json";
    public MqttAndroidClient mqttAndroidClient;
/*
    final String url = "tcp://broker.emqx.io";
    final String username = "";
    final String password = "";
    String clientId = "aaa";
    String subtopic = "/sensor/temp";
*/
    boolean link_status=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int CONNECTION_TIMEOUT = 60;
        final int KEEP_ALIVE_INTERVAL = 200;

        JSONObject jsonObject = new JSONObject();


        JSONArray timedarray = new JSONArray();
        timedarray.put("12");
        timedarray.put("10");
        timedarray.put("5");
        try {
            jsonObject.put("Sunday", timedarray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.w("jsonarray", jsonObject.toString());

        dataReceived = (TextView) findViewById(R.id.dataReceived);
        //startMqtt();
        mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(),url , clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection was lost!");
                Log.w("Mqtt", "Connection was lost!");
                link_status=false;
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("Message Arrived!: " + topic + ": " + new String(message.getPayload()));
                Log.w("MqttRecv", message.toString());
                //dataReceived.setText(message.toString());

                // https://www.tutorialspoint.com/android/android_json_parser.htm
                JSONObject reader = new JSONObject(message.toString());
                String day = reader.getString("Sunday");
                dataReceived.setText(day);
                Log.w("jsonTxt1",day);

                JSONObject jsonObj = new JSONObject(message.toString());
                JSONArray timeinday = jsonObj.getJSONArray("Sunday");
                String b = timeinday.getString(0); // Hour
                Log.w("jsonTxt2",b);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("Delivery Complete!");
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
            mqttAndroidClient.connect(mqttConnectOptions,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    System.out.println("Connection Success!");
                    Log.w("Mqtt", "Connection Success!");
                    try {
                        System.out.println("Subscribing to");
                        Log.w("Mqtt", "Subscribing to");
                        Log.w("Mqtt",subtopic);
                        mqttAndroidClient.subscribe(subtopic, 0);
                        System.out.println("Subscribed to topic");
                        System.out.println("Publishing message..");
                        Log.w("Mqtt", "Publishing message..");
                        link_status=true;
                        // Publish to topic                                  iot-2/type/device_type/id/device_id/cmd/command_id/fmt/format_string /type/aweHGH7/id/irri555/
                        //mqttAndroidClient.publish("iot-2/type/aweHGH7/id/irri555/cmd/solenoid1daytimeduration/fmt/json", new MqttMessage("{'Sunday' : ['11','21','31'], 'Monday' : ['22','58', '2'], 'Tuesday' : ['16','30','120'], 'wednesday' : ['15','24','34']}".getBytes()));
                        mqttAndroidClient.publish("iot-2/type/aweHGH7/id/irri555/cmd/getsolenoidssettings/fmt/json", new MqttMessage("{'solenoid':'1'}".getBytes()));
                    } catch (MqttException ex) {

                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Connection Failure!");
                    Log.w("Mqtt", "Connection Failure!");
                    link_status=false;
                }
            });
        } catch (MqttException ex) {

        }
    }


    public void convertCurrency(View view) {
        EditText dollarText = findViewById(R.id.dollarText);
        TextView textView = findViewById(R.id.euroValue);
        if (!dollarText.getText().toString().equals("")) {
            float dollarValue = Float.valueOf(dollarText.getText().toString());
            float euroValue = dollarValue * 0.85F;
            textView.setText(String.format(Locale.getDefault(), "%.2f", euroValue));
            try {
                if (link_status==true) //mqttAndroidClient.publish("iot-2/type/aweHGH7/id/irri555/cmd/getsolenoidsstatus/fmt/json", new MqttMessage("{'Solenoids' : '?'}".getBytes()));
                    mqttAndroidClient.publish("iot-2/type/aweHGH7/id/irri555/cmd/getdevicetime/fmt/json", new MqttMessage("{'Solenoids' : '?'}".getBytes()));
            } catch (MqttException e) {
                e.printStackTrace();
            }

        } else {
            textView.setText(R.string.no_value_string);
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

