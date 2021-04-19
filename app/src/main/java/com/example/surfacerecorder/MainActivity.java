package com.example.surfacerecorder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    boolean recording = false;
    public static final String LOG_TAG = "KD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "MainActivity created");

        setContentView(R.layout.activity_main);

        ImageButton ibSwitch =(ImageButton) findViewById(R.id.ibSwitch);

        ibSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(true == recording){
                    recording = false;
                    stopRecorderService();
                    ibSwitch.setImageResource(R.drawable.ic_start_record);
                }else {
                    recording = true;
                    startRecorderService();
                    ibSwitch.setImageResource(R.drawable.ic_stop_record);
                }
            }
        });
    }

    public void startRecorderService(){
        Intent serviceIntent = new Intent(this, RecorderService.class);
        serviceIntent.putExtra("ACTION", "start");
        startService(serviceIntent);
    }

    public void stopRecorderService(){
        Intent serviceIntent = new Intent(this, RecorderService.class);
        serviceIntent.putExtra("ACTION", "stop");
        stopService(serviceIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(LOG_TAG, "MainActivity started");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "MainActivity paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "MainActivity resumed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "MainActivity destroyed");
    }
}