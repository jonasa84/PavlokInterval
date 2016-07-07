package com.jonasa84.pavlokinterval;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
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

        // schedule task
        mTimer.scheduleAtFixedRate(new SendStimuliTimerTask(), 0, _interval);
    }

    @Override
    public void onDestroy(){
        if (mTimer != null) {
            mTimer.cancel();
        }
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
