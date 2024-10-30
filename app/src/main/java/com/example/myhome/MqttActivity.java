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
        //返回按钮。
        Button button5 = findViewById(R.id.button5);
        button5.setOnClickListener(v -> finish());
        //MQTT订阅按钮，留着不用。
        Button button4 = findViewById(R.id.button4);
        // 清空按钮
        Button button6 = findViewById(R.id.button6);
        button6.setOnClickListener(v -> clearDatabase());
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
        // 观察 LiveData
        messageViewModel.getMessages().observe(this, messages -> {
            messageList.clear();
            messageList.addAll(messages);
            adapter.notifyDataSetChanged();
        });
        // 从数据库加载消息，加载之前的消息。
        loadMessagesFromDatabase();
        // 注册广播接收器，用来接收MQTT Activity的广播。
        IntentFilter filter = new IntentFilter("mqtt_message_received");
        registerReceiver(mqttReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        IntentFilter filter = new IntentFilter("mqtt_message_received");
//        registerReceiver(mqttReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
//    }


    // 清空数据库的方法
    private void clearDatabase() {
        dbHelper.clearMessages(); // 调用清空数据库的方法
        Toast.makeText(this, "数据库已清空", Toast.LENGTH_SHORT).show(); // 提示用户
    }

    //具体接收广播方法
    private BroadcastReceiver mqttReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast received"); // 这条日志用于确认onReceive被调用
            String message = intent.getStringExtra("message");
            if (message != null && messageSet.add(message)) {
                Log.d(TAG, "接收到广播消息: " + message); // 打印接收到的消息
                messageList.add(message);
                adapter.notifyItemInserted(messageList.size() - 1);
                dbHelper.insertMessage(message); // 存储到数据库
                messageViewModel.addMessage(message); // 更新 ViewModel
            }
        }
    };

    private void loadMessagesFromDatabase() {
        // 从数据库加载消息
        List<String> messages = dbHelper.getAllMessages();
        for (String message : messages) {
            messageViewModel.addMessage(message); // 添加到 ViewModel
        }
    }

    private void connectAndSubscribe() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "请检查网络连接", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent serviceIntent = new Intent(this, MqttService.class);
        startService(serviceIntent);
    }

    private boolean isNetworkAvailable() {
        // TODO: Implement network check logic
        return true; // 这里暂时返回 true
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unregisterReceiver(mqttReceiver);
//    }

    @Override
    protected void onPause() {
        super.onPause();
        //查看activity是不是被销毁了
        Log.d(TAG, "MqttActivity is paused");
    }
}