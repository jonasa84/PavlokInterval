package com.jonasa84.pavlokinterval;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Jonas on 2016-07-04.
 */
public class LogService {
    private LogSqlHelper logSqlHelper;

    public LogService(Context context){
        logSqlHelper = new LogSqlHelper(context);
    }

    public void Log(String message, LogType type){
        SQLiteDatabase db = logSqlHelper.getWritableDatabase();
        ContentValues logValues = new ContentValues();
        logValues.put(LogSqlHelper.LOG_MESSAGE_COL_NAME, message);
        logValues.put(LogSqlHelper.LOG_TYPE_COL_NAME, type.name());
        db.insert(LogSqlHelper.LOG_TABLE_NAME, null, logValues);
    }

    public Cursor getCursor(){
        SQLiteDatabase db = logSqlHelper.getReadableDatabase();

        return db.rawQuery("SELECT id AS _id, * FROM " + LogSqlHelper.LOG_TABLE_NAME + " ORDER BY id DESC", null);
    }
}
