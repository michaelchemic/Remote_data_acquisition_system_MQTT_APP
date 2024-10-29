package com.example.myhome;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.content.SharedPreferences;

public class MqttActivity extends AppCompatActivity {

    private static final String TAG = "MqttActivity";
    private MqttClient mqttClient;

    private MqttMessageAdapter adapter;
    private List<String> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_config);
        Button button5 = findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectAndSubscribe();  // 点击按钮时连接并订阅
            }

        });


        // 初始化 RecyclerView
        messageList = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new MqttMessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void connectAndSubscribe() {
        // MQTT 连接参数
        String clientId = "k1swohbzYjV.Android_MQTT1|securemode=2,signmethod=hmacsha256,timestamp=1730170066886|";
        String username = "Android_MQTT1&k1swohbzYjV";
        String password = "731908e40fe035601d9c46c4b4e84bebc3ad2da17c39ef2355f47bd29022ad82";
        String mqttHostUrl = "tcp://iot-06z00cb7ba4rgrx.mqtt.iothub.aliyuncs.com:1883";
        String topic = "/k1swohbzYjV/Android_MQTT1/user/get"; // 订阅的主题

        try {
            mqttClient = new MqttClient(mqttHostUrl, clientId, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            mqttClient.connect(options);
            Log.d(TAG, "连接成功");

            mqttClient.subscribe(topic, (string, message) -> {
                //Log.d(TAG, "收到消息 - 主题: " + string + ", 内容: " + new String(message.getPayload()));
                // 获取当前时间戳
                String timestamp = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
                String msg = timestamp + new String(message.getPayload());
                Log.d(TAG, msg);
                runOnUiThread(() -> {
                    messageList.add(msg);
                    adapter.notifyItemInserted(messageList.size() - 1);
                });

            });

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "连接丢失: " + cause.getMessage());
                }


                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    //String msg = "主题: " + topic + ", 内容: " + new String(message.getPayload());
                    //Log.d(TAG, msg);
                    // 更新消息列表并刷新适配器
//                    runOnUiThread(() -> {
//                        messageList.add(msg);
//                        adapter.notifyItemInserted(messageList.size() - 1);
//                    });
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "消息发送完成");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "连接或订阅时出现异常", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            Log.e(TAG, "断开连接失败", e);
        }
    }


}





