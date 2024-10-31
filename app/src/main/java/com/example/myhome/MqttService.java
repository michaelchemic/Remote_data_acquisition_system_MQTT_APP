package com.example.myhome;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

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
    private MqttClient mqttClient; //创建一个mqtt客户端
    //mqtt相关参数设置
    private static final String CLIENT_ID = "k1swohbzYjV.Android_MQTT1|securemode=2,signmethod=hmacsha256,timestamp=1730170066886|";
    private static final String USERNAME = "Android_MQTT1&k1swohbzYjV";
    private static final String PASSWORD = "731908e40fe035601d9c46c4b4e84bebc3ad2da17c39ef2355f47bd29022ad82";
    private static final String MQTT_HOST_URL = "tcp://iot-06z00cb7ba4rgrx.mqtt.iothub.aliyuncs.com:1883";
    private static final String TOPIC = "/k1swohbzYjV/Android_MQTT1/user/get";

    //存从mqtt订阅来的数据
    private List<String> messageList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        connectAndSubscribe();
    }

    private void connectAndSubscribe() {
        try {
            //创建客户端
            mqttClient = new MqttClient(MQTT_HOST_URL, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            //配置mqtt参数
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            mqttClient.connect(options);
            Log.d(TAG, "订阅成功");
            Toast.makeText(this, "订阅成功", Toast.LENGTH_SHORT).show(); // 提示用户

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "连接丢失: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws MqttException {
                    // 获取当前时间戳
                    String timestamp = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
                    // 信息拼接
                    String msg = String.format("%s: %s", timestamp, new String(message.getPayload()));
                    //获取正常就打印输出在logcat查看
                    Log.d(TAG, msg);
                    //添加数据到messagelist
                    messageList.add(msg);
                    // 发送广播，通知活动更新
                    Intent broadcastIntent = new Intent("mqtt_message_received");

                    broadcastIntent.putExtra("message", msg); // 将消息放入Intent中
                    sendBroadcast(broadcastIntent); // 发送广播

                    message.setQos(1); // 设置 QoS
                    mqttClient.publish(TOPIC, message);//成功发送后回调deliverComplete函数打印调试信息

                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "消息发送完成");
                }

            });
            //一直订阅话题
            mqttClient.subscribe(TOPIC);
        } catch (MqttException e) {
            Log.e(TAG, "连接或订阅时出现异常", e);
        }
    }

    //监测网络，app使用不能挂VPN
    private boolean isNetworkAvailable() {
        return true; // 这里暂时返回 true
    }

    //不绑定服务，丢后台使用。
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 不需要绑定
    }
}
