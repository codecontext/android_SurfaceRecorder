package com.example.surfacerecorder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        startService(serviceIntent);

        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
    }

    public void stopRecorderService(){
        Intent serviceIntent = new Intent(this, RecorderService.class);
        stopService(serviceIntent);

        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
    }
}