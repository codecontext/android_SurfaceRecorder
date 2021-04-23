package com.example.surfacerecorder;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int RECORDER_NOT_RUNNING = 0;
    private static final int RECORDER_RUNNING = 1;

    private static final int MIC_ON = 2;
    private static final int MIC_OFF = 3;

    private static int recorderState = RECORDER_NOT_RUNNING;
    private static int micOn = MIC_ON;

    private static final String DISPLAY_NAME = "VirtualSurface";
    public static final String LOG_TAG = "KD";
    public static final boolean DEBUG_ENABLED = true;

    private static final int REQUEST_PERMISSION = 10;
    private static final int REQUEST_CODE = 1234;

    public static TextView tvRecordText;
    public static Chronometer chronoTimer;
    public static ImageButton ibSwitch;

    private int screenDensity;
    private MediaRecorder mediaRecorder;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjectionCallback mediaProjectionCallback;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;

    private static int DISPLAY_WIDTH;
    private static int DISPLAY_HEIGHT;

    private String videoUrl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +
            "/KD_" + new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss").format(new Date()) + ".mp4";

    private static final int VIDEO_ENCODING_BITRATE = 512*10000;
    private static final int VIDEO_FRAMERATE = 24;

    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static
    {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "MainActivity created");

        setContentView(R.layout.activity_main);

        tvRecordText = findViewById(R.id.tvRecording);
        chronoTimer = findViewById(R.id.cmTimer);
        ibSwitch = findViewById(R.id.ibSwitch);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        DISPLAY_WIDTH = metrics.widthPixels;
        DISPLAY_HEIGHT = metrics.heightPixels;

        screenDensity = metrics.densityDpi;
        mediaRecorder = new MediaRecorder();
        mediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        ibSwitch.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                /* Check if the Ext Storage Write and Audio Record permissions are granted */
                if(ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                        ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                            ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                    Manifest.permission.RECORD_AUDIO))
                    {
                        /* If Ext Storage Write or Audio Record both permissions are not granted,
                           show the Snackbar to request the permissions */
                        Snackbar.make(findViewById(android.R.id.content), R.string.device_permission_text,
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

                        recorderState = RECORDER_NOT_RUNNING;
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
                    ExecuteSurfaceRecord();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
                    if(DEBUG_ENABLED) {
                        Log.v(LOG_TAG, "onRequestPermissionsResult()  recorderState = " + recorderState);
                    }

                    ExecuteSurfaceRecord();
                }
                else
                {
                    Snackbar.make(findViewById(android.R.id.content), R.string.device_permission_text,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v) {
                            /*Intent intent = new Intent();

                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                            *//* This intent opens the App Settings screen
                               where the permissions can be set by the user *//*
                            startActivity(intent);*/

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.RECORD_AUDIO},
                                            REQUEST_PERMISSION);
                        }
                    }).show();
                }
            }break;

            default: break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                if(DEBUG_ENABLED) {
                    Log.v(LOG_TAG, "onActivityResult()  recorderState = " + recorderState);
                }

                mediaProjectionCallback = new MediaProjectionCallback();
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                mediaProjection.registerCallback(mediaProjectionCallback, null);

                if(RECORDER_NOT_RUNNING == recorderState) {
                    prepareRecorder();
                    startScreenSharing();
                }

                tvRecordText.setVisibility(View.VISIBLE);
            }
            else
            {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void ExecuteSurfaceRecord()
    {
        if(RECORDER_NOT_RUNNING == recorderState)
        {
            if(DEBUG_ENABLED){
                Log.v(LOG_TAG, "ExecuteSurfaceRecord()  recorderState = "+recorderState);
            }

            prepareRecorder();
            startScreenSharing();
        }
        else if(RECORDER_RUNNING == recorderState)
        {
            if(DEBUG_ENABLED){
                Log.v(LOG_TAG, "ExecuteSurfaceRecord() else recorderState = "+recorderState);
            }

            chronoTimer.stop();
            chronoTimer.setBase(SystemClock.elapsedRealtime());

            stopScreenSharing();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay()
    {
        VirtualDisplay display = null;

        try {
            display = mediaProjection.createVirtualDisplay( DISPLAY_NAME,
                                                            DISPLAY_WIDTH,
                                                            DISPLAY_HEIGHT,
                                                            screenDensity,
                                                            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                                                            mediaRecorder.getSurface(),
                                                    null,
                                                    null);
        }catch (Exception e){
            e.printStackTrace();
        }

        return display;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startScreenSharing()
    {
        if(DEBUG_ENABLED){
            Log.v(LOG_TAG, "startScreenSharing()  recorderState = "+recorderState);
        }

        if(mediaProjection == null) {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }

        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();

        chronoTimer.setBase(SystemClock.elapsedRealtime());
        chronoTimer.start();

        recorderState = RECORDER_RUNNING;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void stopScreenSharing()
    {
        tvRecordText.setVisibility(View.INVISIBLE);

        mediaRecorder.stop();
        mediaRecorder.reset();

        if(virtualDisplay != null)
        {
            virtualDisplay.release();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                destroyMediaProjection();
            }
        }

        recorderState = RECORDER_NOT_RUNNING;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void destroyMediaProjection()
    {
        if(mediaProjection != null)
        {
            mediaProjection.unregisterCallback(mediaProjectionCallback);
            mediaProjection.stop();
            mediaProjection = null;
        }

        Toast.makeText(this, "Recording Saved", Toast.LENGTH_LONG).show();
    }

    private void prepareRecorder()
    {
        if(DEBUG_ENABLED){
            Log.v(LOG_TAG, "startScreenSharing()  recorderState = "+recorderState);
        }

        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(videoUrl);
            mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoEncodingBitRate(VIDEO_ENCODING_BITRATE);
            mediaRecorder.setVideoFrameRate(VIDEO_FRAMERATE);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATION.get(rotation + 90);

            mediaRecorder.setOrientationHint(orientation);
            mediaRecorder.prepare();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionCallback extends MediaProjection.Callback
    {
        public void onStop()
        {
            if(DEBUG_ENABLED) {
                Log.v(LOG_TAG, "MediaProjectionCallback()  recorderState = " + recorderState);
            }

            if(RECORDER_RUNNING == recorderState)
            {
                recorderState = RECORDER_NOT_RUNNING;
                mediaRecorder.stop();
                mediaRecorder.reset();
            }

            mediaProjection = null;
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

        if(DEBUG_ENABLED) {
            Log.v(LOG_TAG, "MainActivity started");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(DEBUG_ENABLED) {
            Log.v(LOG_TAG, "MainActivity paused");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(DEBUG_ENABLED) {
            Log.v(LOG_TAG, "MainActivity resumed");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(DEBUG_ENABLED) {
            Log.v(LOG_TAG, "MainActivity destroyed");
        }
    }
}