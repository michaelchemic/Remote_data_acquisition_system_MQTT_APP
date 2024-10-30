package com.example.myhome;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

public class MqttService extends Service {

    private static final String TAG = "MqttService";
    private MqttClient mqttClient;

    private static final String CLIENT_ID = "k1swohbzYjV.Android_MQTT1|securemode=2,signmethod=hmacsha256,timestamp=1730170066886|";
    private static final String USERNAME = "Android_MQTT1&k1swohbzYjV";
    private static final String PASSWORD = "731908e40fe035601d9c46c4b4e84bebc3ad2da17c39ef2355f47bd29022ad82";
    private static final String MQTT_HOST_URL = "tcp://iot-06z00cb7ba4rgrx.mqtt.iothub.aliyuncs.com:1883";
    private static final String TOPIC = "/k1swohbzYjV/Android_MQTT1/user/get";

    private List<String> messageList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        connectAndSubscribe();
    }

    private void connectAndSubscribe() {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "请检查网络连接");
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
                    messageList.add(msg);

                    // 发送广播，通知活动更新
                    Intent broadcastIntent = new Intent("mqtt_message_received");
                    broadcastIntent.putExtra("message", msg); // 将消息放入Intent中
                    sendBroadcast(broadcastIntent); // 发送广播
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
        return true; // 这里暂时返回 true
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

    @Override
    public IBinder onBind(Intent intent) {
        return null; // 不需要绑定
    }

    public List<String> getMessageList() {
        return messageList;
    }

}
