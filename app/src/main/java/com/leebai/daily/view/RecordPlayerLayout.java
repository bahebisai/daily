package com.leebai.daily.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.leebai.daily.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Format;

/**
 * Created by lingfei.li on 17-11-17.
 */

public class RecordPlayerLayout extends FrameLayout {
    private Chronometer mTime;
    private SeekBar mSeekBar;
    private ImageButton mPlayBtn;
    private TextView mFileNameTV;
    private TextView mFileSizeTV;

    LinearLayout mFileInfoLayout;
    LinearLayout mPlayInfoLayout;

    private MediaPlayer mMediaPlayer;
    private static boolean mIsPlaying;
    private static boolean mIsInPlayStatus;

    private String mFilePath;

    private final int START_PLAY_RECORD = 2;
    private final int PAUSE_RECORD = 3;
    private final int RESUME_RECORD = 1;
    private final int UPDATE = 4;

    public RecordPlayerLayout(Context context) {
        this(context, null);
    }

    public RecordPlayerLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordPlayerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecordPlayerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);


    }
//
//    public void bindData(String path) {
//        mFilePath = path;
//    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPlayBtn = findViewById(R.id.play_record);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playBtnOnclick();
            }
        });

        mTime = findViewById(R.id.time);
        mSeekBar = findViewById(R.id.progress);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mMediaPlayer.seekTo(mSeekBar.getProgress());
                mTime.setBase(SystemClock.elapsedRealtime() - mSeekBar.getProgress());
            }
        });

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mIsPlaying = false;
                stopAndUpdateUI();
            }
        });

        mFileInfoLayout = findViewById(R.id.file_info);
        mPlayInfoLayout = findViewById(R.id.play_info);

        mFileNameTV = findViewById(R.id.audio_name);
        mFileSizeTV = findViewById(R.id.audio_size);

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case START_PLAY_RECORD:
                    mPlayBtn.setImageResource(R.drawable.ic_pause_black_24dp);
                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();
                    }
                    try {
                        mMediaPlayer.setDataSource(mFilePath);
                        mMediaPlayer.prepare();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    int duration = mMediaPlayer.getDuration();
                    Log.d("leeee", "duration = " + duration);
                    mSeekBar.setMax(duration);
                    mTime.setBase(SystemClock.elapsedRealtime());

                    mMediaPlayer.start();
                    mTime.start();

                    mFileInfoLayout.setVisibility(View.GONE);
                    mPlayInfoLayout.setVisibility(VISIBLE);
                    sendEmptyMessageDelayed(UPDATE, 500);
                    break;

                case PAUSE_RECORD:
                    mMediaPlayer.pause();
                    mTime.stop();
                    mIsPlaying = false;
                    mPlayBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    break;

                case RESUME_RECORD:
                    mMediaPlayer.start();
                    mTime.start();
                    mIsPlaying = true;
                    mPlayBtn.setImageResource(R.drawable.ic_pause_black_24dp);
                    break;

                case UPDATE:
                    if (mIsPlaying) {
                        if (mMediaPlayer == null) {
                            return;
                        }
                        mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                        sendEmptyMessageDelayed(UPDATE, 200);
                        Log.d("leee", "update seekbar " + mMediaPlayer.getCurrentPosition());
                    } else {
                        removeMessages(UPDATE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void playBtnOnclick() {
        if (mFilePath == null) {
            return;
        }
        mIsPlaying = !mIsPlaying;
        if (mIsPlaying) {
            mPlayBtn.setImageResource(R.drawable.ic_pause_black_24dp);
            mHandler.sendEmptyMessage(RESUME_RECORD);
        } else {
            mPlayBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            mHandler.sendEmptyMessage(PAUSE_RECORD);
        }

    }

    public String getPath() {
        return mFilePath;
    }

    public void setPath(String path) {
        mFilePath = path;

        File file = new File(mFilePath);
        if (file.exists()) {
            mFileNameTV.setText(file.getName());
            try {
                long size = getFileSize(file);
                mFileSizeTV.setText(Formatter.formatFileSize(getContext(), size));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public boolean isInPlayStatus() {
        return mIsInPlayStatus;
    }

    //// TODO: 17-11-21
    public void stopAndUpdateUI() {
        if (mIsPlaying) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        mMediaPlayer = null;
        mIsPlaying = false;

        mTime.setBase(SystemClock.elapsedRealtime());
        mTime.stop();
        mSeekBar.setProgress(0);
        mPlayBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        mFileInfoLayout.setVisibility(View.VISIBLE);
        mPlayInfoLayout.setVisibility(GONE);
        mIsInPlayStatus = false;
    }

    public void startAndUpdateUI() {
        mIsPlaying = true;
        mIsInPlayStatus = true;
        mHandler.sendEmptyMessage(START_PLAY_RECORD);
    }

    /**
     * 获取指定文件大小(单位：字节)
     *
     * @param file
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        if (file == null) {
            return 0;
        }
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        return size;
    }
}
