package com.example.surfacerecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    boolean recording = false;
    public static final String LOG_TAG = "KD";

    private static final int REQUEST_PERMISSION = 10;
    private static final int REQUEST_CODE = 1234;

    public static TextView tvRecordText;
    public static Chronometer chronoTimer;
    public static ImageButton ibSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "MainActivity created");

        setContentView(R.layout.activity_main);

        tvRecordText = (TextView)findViewById(R.id.tvRecording);
        chronoTimer = (Chronometer)findViewById(R.id.cmTimer);
        ibSwitch =(ImageButton) findViewById(R.id.ibSwitch);

        ibSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Check if the Ext Storage Write and Audio Record permissions are granted */
                if(ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                        ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                    Manifest.permission.RECORD_AUDIO))
                    {
                        /* If Ext Storage Write or Audio Record both permissions are not granted,
                           show the Snackbar to request the permissions */
                        Snackbar.make(findViewById(android.R.id.content), R.string.permission_text,
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE", new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.RECORD_AUDIO},
                                                REQUEST_PERMISSION);
                            }
                        }).show();
                    }
                    else
                    {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.RECORD_AUDIO},
                                        REQUEST_PERMISSION);
                    }
                }
                else
                {

                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case REQUEST_PERMISSION:
            {
                if((grantResults.length > 0) &&
                        (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED))
                {

                }
                else
                {
                    Snackbar.make(findViewById(android.R.id.content), R.string.permission_text,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();

                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                            /* This intent opens the App Settings screen
                               where the permissions can be set by the user */
                            startActivity(intent);
                        }
                    }).show();
                }
            }break;

            default: break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {

            }
            else
            {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
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