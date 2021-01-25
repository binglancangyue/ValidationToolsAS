package com.sprd.validationtools.itemstest;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class HornTestActivity extends BaseActivity {
    private static final String TAG = "HornTestActivity";
    private TextView tvTime;
    private Button btnPlay;
    private Timer mPlayTimer;
    private MediaPlayer player;
    private int time = 0;
    private static final String VOICE_PATH = "/data/data/com.sprd.validationtools/recordingTestFile.m4a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.removeButton();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horn_test);
        initView();
    }

    private void initView() {
        tvTime = findViewById(R.id.tv_voice_time);
        btnPlay = findViewById(R.id.btn_play_voice);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlayVoice();
            }
        });
    }

    private void startPlayVoice() {
        mPlayTimer = new Timer();
        playVoice();
        mPlayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                time++;
                if (time == 6) {
                    mPlayTimer.purge();
                    mPlayTimer.cancel();
                    mPlayTimer = null;
                    time = 0;
                    btnPlay.setEnabled(true);
                    player.stop();
                }
                updateTime();
            }
        }, 100, 1000);
    }

    private void updateTime() {
        tvTime.post(new Runnable() {
            @Override
            public void run() {
                String times = String.format("%02d:%02d", time / 60 % 60, time % 60);
                tvTime.setText(times);
            }
        });
    }

    private void playVoice() {
        try {
            player = new MediaPlayer();
            btnPlay.setEnabled(false);
            if (checkFile()) {
                Log.d(TAG, "playVoice: ");
                player.setDataSource(VOICE_PATH);
            } else {
                Log.d(TAG, "playVoice:create ");
                player = MediaPlayer.create(this, R.raw.soundtriggermp);
            }
            player.prepare();
            player.start();
//            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    if (player != null) {
//                        player.release();
//                        player = null;
//                    }
//                }
//            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkFile() {
        File file = new File(VOICE_PATH);
        if (file.exists()) {
            return true;
        }
        return false;
    }

}
