package com.jonasa84.pavlokinterval;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

public class LogActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        initListView();
    }

    private void initListView() {
        LogService logService = new LogService(this);
        Cursor logCursor = logService.getCursor();

        ListView lvItems = (ListView) findViewById(R.id.logListView);
        LogCursorAdapter logCursorAdapter = new LogCursorAdapter(this, logCursor);
        lvItems.setAdapter(logCursorAdapter);
    }
}
