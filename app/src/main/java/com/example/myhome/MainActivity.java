package com.example.myhome;
// TODO: 30/10/2024 有个问题，回到MainActivity，MqttActivity里面广播收不到数据。

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //配置mqtt服务，防止结束activity导致的通信断连。
        Intent serviceIntent = new Intent(this, MqttService.class);
        startService(serviceIntent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Log.d(TAG, "MainActivity OK");

        Button button2 = findViewById(R.id.button2);//历史数据按钮
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
        Button button = findViewById(R.id.button);//MQTT 配置界面按钮
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MqttActivity.class);
                startActivity(intent);
            }
        });


    }
}
