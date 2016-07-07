package com.jonasa84.pavlokinterval;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;

public class MainActivity extends AppCompatActivity {

    static final String STATE_HOURS = "hours";
    static final String STATE_MINUTES = "minutes";
    static final String STATE_SECONDS = "seconds";
    static final String STATE_STIMULI = "stimuli";
    static final String STATE_INTENSITY = "intensity";
    static final String STATE_RUNNING = "running";

    private SharedPreferences prefs;

    private NumberPicker hoursPicker;
    private NumberPicker minutesPicker;
    private NumberPicker secondsPicker;
    private Spinner spinner;
    private NumberPicker intensityPicker;

    private FloatingActionButton startButton;
    private FloatingActionButton stopButton;

    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = getPreferences(0);

        isRunning = prefs.getBoolean(STATE_RUNNING, false);

        initPickers();
        initSpinner();
        initButtons();

        if(isRunning)
            setRunningState();
    }

    @Override
    protected void onPause() {
        super.onPause();

        storeState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else if (id == R.id.action_log) {
            startActivity(new Intent(this, LogActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initPickers(){
        hoursPicker = (NumberPicker) findViewById(R.id.hoursPicker);
        minutesPicker = (NumberPicker) findViewById(R.id.minutesPicker);
        secondsPicker = (NumberPicker) findViewById(R.id.secondsPicker);
        intensityPicker = (NumberPicker) findViewById(R.id.intensityPicker);

        NumberPicker.OnValueChangeListener eventListener = new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                startButton.setEnabled(getIntervalInMilliseconds() > 0);
            }
        };

        hoursPicker.setOnValueChangedListener(eventListener);
        minutesPicker.setOnValueChangedListener(eventListener);
        secondsPicker.setOnValueChangedListener(eventListener);

        hoursPicker.setMaxValue(24);
        hoursPicker.setValue(prefs.getInt(STATE_HOURS, 0));

        minutesPicker.setMaxValue(59);
        minutesPicker.setValue(prefs.getInt(STATE_MINUTES, 10));

        secondsPicker.setMaxValue(59);
        secondsPicker.setValue(prefs.getInt(STATE_SECONDS, 0));

        intensityPicker.setMinValue(1);
        intensityPicker.setMaxValue(255);
        intensityPicker.setValue(prefs.getInt(STATE_INTENSITY, 255));
    }

    private void initSpinner(){
        spinner = (Spinner) findViewById(R.id.stimuliSpinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new String[] {
                "Beep", "LED", "Shock", "Vibration"
        });
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(StimuliType.valueOf(adapter.getItem(position)) == StimuliType.LED)
                    intensityPicker.setMaxValue(4);
                else
                    intensityPicker.setMaxValue(255);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });


        spinner.setSelection(adapter.getPosition(prefs.getString(STATE_STIMULI, "Beep")));
    }

    private void initButtons() {

        startButton = (FloatingActionButton) findViewById(R.id.start);
        stopButton = (FloatingActionButton) findViewById(R.id.stop);

        final Context context = this;

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setRunningState();
                storeState();

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                String username = sharedPref.getString("pref_username", "");
                String password = sharedPref.getString("pref_password", "");

                TimerService.setCredentials(username, password);
                TimerService.setOptions(getIntervalInMilliseconds(), getStimuli(), getIntensity());

                Intent startIntent = new Intent(MainActivity.this, TimerService.class);
                startIntent.setAction(TimerService.ACTION_START);
                startService(startIntent);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent stopIntent = new Intent(MainActivity.this, TimerService.class);
                stopIntent.setAction(TimerService.ACTION_STOP);
                startService(stopIntent);

                setPausedState();
                storeState();
            }
        });
    }

    private void setRunningState(){
        isRunning = true;

        startButton.hide();
        stopButton.show();

        setEnabledInputs(false);
    }

    private void setPausedState(){
        isRunning = false;

        startButton.show();
        stopButton.hide();

        setEnabledInputs(true);
    }

    private void setEnabledInputs(boolean enabled){
        hoursPicker.setEnabled(enabled);
        minutesPicker.setEnabled(enabled);
        secondsPicker.setEnabled(enabled);
        intensityPicker.setEnabled(enabled);
        spinner.setEnabled(enabled);
    }

    private void storeState(){
        SharedPreferences.Editor ed = prefs.edit();

        ed.putInt(STATE_HOURS, hoursPicker.getValue());
        ed.putInt(STATE_MINUTES, minutesPicker.getValue());
        ed.putInt(STATE_SECONDS, secondsPicker.getValue());
        ed.putString(STATE_STIMULI, (String)spinner.getSelectedItem());
        ed.putInt(STATE_INTENSITY, intensityPicker.getValue());
        ed.putBoolean(STATE_RUNNING, isRunning);

        ed.commit();
    }

    private long getIntervalInMilliseconds(){
        return (hoursPicker.getValue() * 3600 + minutesPicker.getValue() * 60 + secondsPicker.getValue()) * 1000;
    }

    private StimuliType getStimuli(){
        String value = (String) spinner.getSelectedItem();
        return StimuliType.valueOf(value);
    }

    private int getIntensity() {
        return intensityPicker.getValue();
    }
}
