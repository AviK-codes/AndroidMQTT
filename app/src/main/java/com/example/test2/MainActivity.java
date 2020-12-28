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
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Locale;

//import helpers.MqttHelper;

public class MainActivity extends AppCompatActivity {

    //MqttHelper mqttHelper;
    MqttAndroidClient client;
    TextView dataReceived;
    //final String username = "android.utility@gmail.com";
    //final String password = "bilbi1234";
/*
    final String url = "tcp://www.maqiatto.com:1883";
    final String username = "android.utility@gmail.com";
    final String password = "bilbi1234";
    final String clientId = "ExampleAndroidClient";
*/
    final String url = "tcp://broker.emqx.io";
    final String username = "";
    final String password = "";
    String clientId = "aaa";
    String subtopic = "/sensor/temp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataReceived = (TextView) findViewById(R.id.dataReceived);
        //startMqtt();
        final MqttAndroidClient mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(),url , clientId);
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection was lost!");
                Log.w("Mqtt", "Connection was lost!");

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("Message Arrived!: " + topic + ": " + new String(message.getPayload()));
                Log.w("Mqtt", message.toString());
                dataReceived.setText(message.toString());
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
                        mqttAndroidClient.subscribe(subtopic, 0);
                        System.out.println("Subscribed to topic");
                        System.out.println("Publishing message..");
                        Log.w("Mqtt", "Publishing message..");
                        mqttAndroidClient.publish("/test", new MqttMessage("Hello world testing..!".getBytes()));
                    } catch (MqttException ex) {

                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Connection Failure!");
                    Log.w("Mqtt", "Connection Failure!");
                }
            });
        } catch (MqttException ex) {

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
    public void convertCurrency(View view) {
        EditText dollarText = findViewById(R.id.dollarText);
        TextView textView = findViewById(R.id.euroValue);
        if (!dollarText.getText().toString().equals("")) {
            float dollarValue = Float.valueOf(dollarText.getText().toString());
            float euroValue = dollarValue * 0.85F;
            textView.setText(String.format(Locale.getDefault(), "%.2f", euroValue));
        } else {
            textView.setText(R.string.no_value_string);
        }
    }
}