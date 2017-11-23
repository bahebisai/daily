package com.leebai.daily.recorder;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by liting.bai on 17-11-20.
 */

public class AudioPlayer {
    private MediaPlayer mMediaPlayer;
    private String mFilePath;

    public AudioPlayer() {

    }

    private MediaPlayer getMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        return mMediaPlayer;
    }

    public int prepareAndStart(String path) {
        mFilePath = path;
        try {
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
        } catch (IOException ex) {
            ex.printStackTrace();
//            isPlaying = false;
        }
        mMediaPlayer.start();
        int duration = mMediaPlayer.getDuration();
        return duration;
    }

    public String getAudioPath() {
        return mFilePath;
    }
}
