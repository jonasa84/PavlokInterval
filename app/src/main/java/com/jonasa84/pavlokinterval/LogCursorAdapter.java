package com.jonasa84.pavlokinterval;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Jonas on 2016-07-04.
 */
public class LogCursorAdapter extends CursorAdapter {
    public LogCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_log, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView timeStampTextView = (TextView) view.findViewById(R.id.timestampTextView);
        TextView messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        LinearLayout container = (LinearLayout) view.findViewById(R.id.logItemLayout);

        String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
        String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
        String logType = cursor.getString(cursor.getColumnIndexOrThrow("logType"));
        LogType type = logType != null ? LogType.valueOf(logType) : LogType.Info;

        timeStampTextView.setText(timestamp);
        messageTextView.setText(message);

        if(type == LogType.Error)
            container.setBackgroundColor(Color.RED);
        else if(type == LogType.Success)
            container.setBackgroundColor(Color.GREEN);
    }
}
