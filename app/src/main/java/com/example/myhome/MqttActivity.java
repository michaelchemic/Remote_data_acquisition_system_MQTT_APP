package com.example.myhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MqttActivity extends AppCompatActivity {
    private boolean isReceiverRegistered = false;
    private static final String TAG = "MqttActivity";
    private MqttMessageAdapter adapter;
    private List<String> messageList;
    private SQLiteHelper dbHelper;
    private MessageViewModel messageViewModel; // ViewModel
    private Set<String> messageSet = new HashSet<>(); // 使用 Set 来避免重复消息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_config);
        // 初始化数据库助手
        dbHelper = new SQLiteHelper(this);

        // 初始化 RecyclerView
        messageList = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new MqttMessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        // 初始化 ViewModel
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);

        //启动mqtt服务
        Intent serviceIntent = new Intent(this, MqttService.class);

        // 注册广播接收器，用来接收MQTT Activity的广播。
        IntentFilter filter = new IntentFilter("mqtt_message_received");
        registerReceiver(mqttReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        //返回按钮,没用，不要了。
        Button button5 = findViewById(R.id.button5);
        button5.setOnClickListener(v -> {
            Toast.makeText(this, "直接按Home键", Toast.LENGTH_SHORT).show(); // 提示用户
        });

        //MQTT订阅按钮，订阅完成变成取消订阅服务，实现一个按键两个用处。
        Button button4 = findViewById(R.id.button4);
        button4.setText("启动订阅服务");

        button4.setOnClickListener(v -> {
            String currentText = button4.getText().toString();
                    if(currentText.equals("启动订阅服务")){

                        startService(serviceIntent);//启动mqtt服务
                        button4.setText("取消订阅服务");

                    }else{
                        Toast.makeText(this, "取消订阅服务", Toast.LENGTH_SHORT).show(); // 提示用户
                        stopService(serviceIntent);//断开mqtt服务
                        button4.setText("启动订阅服务");

                    }
        });

        // 清空按钮
        Button button6 = findViewById(R.id.button6);
        button6.setOnClickListener(v -> clearDatabase());

        // 观察 LiveData
        messageViewModel.getMessages().observe(this, messages -> {
            messageList.clear();
            messageList.addAll(messages);
            adapter.notifyDataSetChanged();
        });
        // 从数据库加载消息，加载之前的消息。
        loadMessagesFromDatabase();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("mqtt_message_received");
        registerReceiver(mqttReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    // 清空数据库的方法
    private void clearDatabase() {
        dbHelper.clearMessages(); // 调用清空数据库的方法
        messageList.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "数据库已清空", Toast.LENGTH_SHORT).show(); // 提示用户
    }

    //具体接收广播方法
    public BroadcastReceiver mqttReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message != null && messageSet.add(message)) {
                Log.d(TAG, "接收到广播消息: " + message);
                messageList.add(message);
                adapter.notifyItemInserted(messageList.size() - 1);
                dbHelper.insertMessage(message);
                messageViewModel.addMessage(message);
            }
        }
    };

    private void loadMessagesFromDatabase() {
        // 从数据库加载消息
        List<String> messages = dbHelper.getAllMessages();
        for (String message : messages) {
            messageViewModel.addMessage(message); // 添加到 ViewModel,从数据库更新数据。
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mqttReceiver);// 注销接收器
    }
}
