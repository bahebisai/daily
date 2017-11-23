package com.leebai.daily.recorder;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.leebai.daily.R;
import com.leebai.daily.xrichtext.SDCardUtil;

import java.io.IOException;

public class SoundRecorderActivity extends AppCompatActivity {

    private Chronometer mChronometer;
    private ImageButton mStopRecordBtn;

    private MediaRecorder mRecorder;

    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_recorder);

        mChronometer = findViewById(R.id.chronometer);
        mStopRecordBtn = findViewById(R.id.stop_record);
        mStopRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });

        mRecorder = new MediaRecorder();

//        mChronometer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startRecording();
//            }
//        });
        startRecording();
    }

    private void startRecording() {
        if (mRecorder == null) {
            return;
        }
        mFilePath = getFilePath();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mFilePath);
        try {
            mRecorder.prepare();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        mRecorder.start();


        mChronometer.setBase(SystemClock.elapsedRealtime());
        int hour = (int) ((SystemClock.elapsedRealtime() - mChronometer.getBase()) / 1000 / 60);
        mChronometer.setFormat(String.valueOf(hour) + ":%s");
        mChronometer.start();

    }

    private void stopRecord() {
        mChronometer.stop();

        mRecorder.stop();
        mRecorder.release();

        Intent intent = new Intent();
        intent.putExtra("path", mFilePath);
        setResult(RESULT_OK, intent);
        finish();
    }

    private String getFilePath() {
        String filePath;
        filePath = SDCardUtil.getSoundRecordDir() + System.currentTimeMillis() + ".3gp";
        return filePath;
    }
}
