package com.jonasa84.pavlokinterval;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;


public class TimerService extends Service {
    // constant
    public static int ID = 777;
    public static String ACTION_START = "com.jonasa84.pavlokinterval.TimerService.StartAction";
    public static String ACTION_STOP = "com.jonasa84.pavlokinterval.TimerService.StopAction";
    private static String ACTION = "com.jonasa84.pavlokinterval.TimerService.MainAction";
    private static long _interval = 10 * 1000;
    private static StimuliType _stimuli = StimuliType.Beep;
    private static int _stimuliIntensity = 255;
    private static String _username = "";
    private static String _password = "";

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;
    private LogService logService;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // cancel if already existed
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }

        if(logService == null)
            logService = new LogService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(ACTION_START)) {

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Intent stopIntent = new Intent(this, TimerService.class);
            stopIntent.setAction(ACTION_STOP);
            PendingIntent pStopIntent = PendingIntent.getService(this, 0, stopIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.pavlok_icon);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("PavlokInterval")
                    .setTicker("PavlokInterval")
                    .setContentText("Interval running")
                    .setSmallIcon(R.drawable.pavlok_icon)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();

            // schedule task
            mTimer.scheduleAtFixedRate(new SendStimuliTimerTask(), 0, _interval);

            startForeground(ID, notification);
        }
        else if(intent.getAction().equals(ACTION_STOP)){
            if (mTimer != null) {
                mTimer.cancel();
            }

            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        if (mTimer != null) {
            mTimer.cancel();
        }
        super.onDestroy();
    }

    public static void setOptions(long interval, StimuliType stimuli, int intensity){
        _interval = interval;
        _stimuli = stimuli;
        _stimuliIntensity = intensity;
    }

    public static void setCredentials(String username, String password){
        _username = username;
        _password = password;
    }

    class SendStimuliTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    sendStimuli();
                }

            });
        }

        private void sendStimuli() {

            Log.d("TimerService", "logging in...");

            RequestParams params = new RequestParams();
            params.put("grant_type", "password");
            params.put("username", _username);
            params.put("password", _password);

            String stimuli = _stimuli.toString();
            final String errorMessage = "Sending " + stimuli + " failed during log in";

            PavlokRestClient.post("api/v1/sign_in", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        String token = response.getString("access_token");

                        Log.d("TimerService", "Logged in successfully");

                        sendStimuli(token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("TimerService", "Exception while logging in");
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    onFailure();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    onFailure();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    onFailure();
                }

                private void onFailure(){
                    Log.d("TimerService", errorMessage);
                    showToast(errorMessage);
                    logService.Log(errorMessage, LogType.Error);
                }
            });
        }

        private void sendStimuli(String token) {

            String stimuli = _stimuli.toString();
            final String successMessage = stimuli + " sent successfully";
            final String errorMessage = "Sending " + stimuli + " failed";
            Log.d("TimerService", "sending " + _stimuli.toString() + "...");

            PavlokRestClient.post("api/v1/stimuli/" + getStimuliUrl() + "/" + _stimuliIntensity, new RequestParams("access_token", token), new JsonHttpResponseHandler(false) {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    onSuccess();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    onSuccess();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    onFailure();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    onFailure();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    onFailure();
                }

                private void onSuccess(){
                    Log.d("TimerService", successMessage);
                    showToast(successMessage);
                    logService.Log(successMessage, LogType.Success);
                }

                private void onFailure(){
                    Log.d("TimerService", errorMessage);
                    showToast(errorMessage);
                    logService.Log(errorMessage, LogType.Error);
                }
            });
        }

        private String getStimuliUrl() {
            return _stimuli.toString().toLowerCase();
        }

        private void showToast(String message){
            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
