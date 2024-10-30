package com.example.myhome;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

// SQLiteHelper 类用于管理 SQLite 数据库的创建、升级和操作
public class SQLiteHelper extends SQLiteOpenHelper {

    // 数据库名称和版本
    private static final String DATABASE_NAME = "mqtt_messages.db";
    private static final int DATABASE_VERSION = 1;

    // 数据表和列名
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_MESSAGE = "message";

    // 构造函数，初始化数据库
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 创建数据库时调用的方法
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建消息表的 SQL 语句
        String createTable = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MESSAGE + " TEXT)";
        db.execSQL(createTable); // 执行创建表的 SQL 语句
    }

    // 数据库版本升级时调用的方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 删除旧的消息表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db); // 创建新的消息表
    }

    // 插入消息到数据库
    public void insertMessage(String message) {
        SQLiteDatabase db = this.getWritableDatabase(); // 获取可写的数据库实例
        ContentValues values = new ContentValues(); // 创建 ContentValues 对象
        values.put(COLUMN_MESSAGE, message); // 将消息添加到 ContentValues
        db.insert(TABLE_MESSAGES, null, values); // 插入消息
        db.close(); // 关闭数据库连接
    }

    // 获取所有消息
    @SuppressLint("Range") // SuppressLint 注解，用于忽略 "Range" 警告
    public List<String> getAllMessages() {
        List<String> messages = new ArrayList<>(); // 创建存储消息的列表
        SQLiteDatabase db = this.getReadableDatabase(); // 获取可读的数据库实例
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MESSAGES, null); // 查询所有消息

        if (cursor.moveToFirst()) { // 如果查询结果不为空
            do {
                // 将消息添加到列表中
                messages.add(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE)));
            } while (cursor.moveToNext()); // 继续遍历结果集
        }
        cursor.close(); // 关闭游标
        db.close(); // 关闭数据库连接
        return messages; // 返回所有消息
    }
}
