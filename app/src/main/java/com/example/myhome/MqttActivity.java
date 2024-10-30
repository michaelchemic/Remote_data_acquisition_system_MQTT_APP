package com.example.myhome;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.ArrayList;
import java.util.List;

public class MqttActivity extends AppCompatActivity {

    private static final String TAG = "MqttActivity";
    private MqttClient mqttClient;

    private MqttMessageAdapter adapter;
    private List<String> messageList;
    private SQLiteHelper dbHelper;
    // MQTT 连接参数
    private static final String CLIENT_ID = "k1swohbzYjV.Android_MQTT1|securemode=2,signmethod=hmacsha256,timestamp=1730170066886|";
    private static final String USERNAME = "Android_MQTT1&k1swohbzYjV";
    private static final String PASSWORD = "731908e40fe035601d9c46c4b4e84bebc3ad2da17c39ef2355f47bd29022ad82";
    private static final String MQTT_HOST_URL = "tcp://iot-06z00cb7ba4rgrx.mqtt.iothub.aliyuncs.com:1883";
    private static final String TOPIC = "/k1swohbzYjV/Android_MQTT1/user/get";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_config);

        // 初始化数据库助手
        dbHelper = new SQLiteHelper(this);

        Button button5 = findViewById(R.id.button5);
        button5.setOnClickListener(v -> finish());

        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(v -> connectAndSubscribe());

        // 初始化 RecyclerView，中间显示数据的循环View
        messageList = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new MqttMessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 从数据库加载消息
        loadMessagesFromDatabase();
    }


    private void loadMessagesFromDatabase() {
        messageList.clear();
        messageList.addAll(dbHelper.getAllMessages());
        adapter.notifyDataSetChanged();
    }

    private void connectAndSubscribe() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "请检查网络连接", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mqttClient = new MqttClient(MQTT_HOST_URL, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            mqttClient.connect(options);
            Log.d(TAG, "连接成功");

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "连接丢失: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    // 获取当前时间戳
                    String timestamp = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
                    String msg = String.format("%s: %s", timestamp, new String(message.getPayload()));
                    Log.d(TAG, msg);
                    runOnUiThread(() -> {
                        messageList.add(msg);
                        adapter.notifyItemInserted(messageList.size() - 1);
                        dbHelper.insertMessage(msg); // 存储到数据库
                    });
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "消息发送完成");
                }
            });

            mqttClient.subscribe(TOPIC);
        } catch (MqttException e) {
            Log.e(TAG, "连接或订阅时出现异常", e);
        }
    }

    private boolean isNetworkAvailable() {
        // TODO: Implement network check logic
        return true; // 这里暂时返回 true，实际实现可以用 ConnectivityManager 检查网络状态
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectMqttClient();
    }

    private void disconnectMqttClient() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        } catch (MqttException e) {
            Log.e(TAG, "断开连接失败", e);
        }
    }
}
