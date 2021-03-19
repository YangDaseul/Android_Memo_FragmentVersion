package com.example.memo_fragmentversion;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static String DATABASE_NAME = "memo.db";
    public static int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "list";
    public static final String LIST_ID = "_id";
    public static final String LIST_PICTURE = "picture";
    public static final String LIST_CONTENTS = "contents";
    public static final String LIST_DATE = "date";


    public static final String[] ALL_COLUMNS = {LIST_ID, LIST_PICTURE, LIST_CONTENTS, LIST_DATE};

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists list("
                + " _id integer PRIMARY KEY autoincrement, "
                + " picture text, "
                + " contents text, "
                + " date text)";

        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        println("onUpgrade 호출됨: " + oldVersion + " -> " + newVersion);

        if(newVersion > 1){
            db.execSQL("DROP TABLE IF EXISTS list");
        }
    }

    public void println(String data){
        Log.d("DatabaseHelper", data);
    }







}
