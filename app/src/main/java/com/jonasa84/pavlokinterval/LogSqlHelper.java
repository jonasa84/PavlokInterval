package com.jonasa84.pavlokinterval;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jonas on 2016-07-04.
 */
public class LogSqlHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LogDb";
    private static final int DATABASE_VERSION = 1;
    public static final String LOG_TABLE_NAME = "log";
    public static final String LOG_MESSAGE_COL_NAME = "message";
    public static final String LOG_TYPE_COL_NAME = "logType";
    private static final String LOG_TABLE_CREATE =
            "CREATE TABLE " + LOG_TABLE_NAME + "(" +
                    "id INTEGER PRIMARY KEY ASC, " +
                    LOG_MESSAGE_COL_NAME + " TEXT," +
                    LOG_TYPE_COL_NAME + " TEXT," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);";

    public LogSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOG_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
