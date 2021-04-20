package com.sprd.validationtools.itemstest;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class FMTestActivity extends BaseActivity {
    private AudioManager audioManager;
    private Context mContext;
    private static final String TAG = "FMTestActivity";
    public static final String fm_power_path = "/sys/class/QN8027/QN8027/power_state";
    public static final String fm_tunetoch_path = "/sys/class/QN8027/QN8027/tunetoch";
    public static final String BX_HEADSET_PATH = "/sys/kernel/headset/state";
    private MediaPlayer player;
    private static final String VOICE_PATH = "/data/data/com.sprd.validationtools/recordingTestFile.m4a";
    public static final String fm_power_state_path = "/sys/devices/platform/sprd-pcm-iis/bixin_pa_state";
    private Button btnPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fm_test);
        this.mContext = this;
        this.audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        initView();
        openOrCloseFM(true);
        setFmValue(9800);
    }

    private void initView() {
        btnPlay = findViewById(R.id.btn_fm_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVoice();
            }
        });
    }

    private void playVoice() {
        try {
            if (player == null) {
                player = new MediaPlayer();
            }
            if (player.isPlaying()) {
                player.stop();
            }
//            if (checkFile()) {
//                Log.d(TAG, "playVoice: ");
//                player.setDataSource(VOICE_PATH);
//            } else {
                Log.d(TAG, "playVoice:create ");
                player = MediaPlayer.create(this, R.raw.mixtone);
//            }
//            player.prepare();
            player.start();
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

    private void openOrCloseFM(boolean isOpen) {
        if (isOpen) {
            openFm();
            setFmState("on");
        } else {
            closeFm();
            setFmState("off");
        }
    }

    public void setFmValue(int value) {
        Writer fmTuneTouch = null;
        try {
            fmTuneTouch = new FileWriter(fm_tunetoch_path);
            fmTuneTouch.write(value + "");
            fmTuneTouch.flush();
            Log.d(TAG, "setFmValue:value " + value);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "setFmValue: " + e.getMessage());
        } finally {
            if (fmTuneTouch != null) {
                try {
                    fmTuneTouch.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void openFm() {
        try {
//            updateFMState(true);
            Writer fm_power = new FileWriter(fm_power_path);
            fm_power.write("on");
            fm_power.flush();
            fm_power.close();
            setSpeakerphoneOn(false);
            Log.d(TAG, "openFm:on ");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "closeFm: " + e.getMessage());
        }
    }

    public void closeFm() {
        try {
            Writer fm_power = new FileWriter(fm_power_path);
            fm_power.write("off");
            fm_power.flush();
            fm_power.close();
            setSpeakerphoneOn(true);
            Log.d(TAG, "openFm:on off");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "closeFm: " + e.getMessage());
        }
    }

    public void setFmState(String value) {
        Writer fmState = null;
        try {
            fmState = new FileWriter(BX_HEADSET_PATH);
            fmState.write(value);
            fmState.flush();
            Log.d(TAG, "setFmState:value " + value);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "setFmState: " + e.getMessage());
        } finally {
            if (fmState != null) {
                try {
                    fmState.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateFMState(boolean isOn) {
        Writer fm_power = null;
        try {
            fm_power = new FileWriter(fm_power_state_path);
            if (isOn) {
                fm_power.write("on");
            } else {
                fm_power.write("off");
            }
            fm_power.flush();
            fm_power.close();
            Log.d(TAG, "updateFMState:isOn " + isOn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSpeakerphoneOn(boolean b) {
        if (audioManager != null) {
            if (!b) {
                audioManager.setParameters("fm=1");
            } else {
                audioManager.setMode(AudioManager.MODE_NORMAL);
                audioManager.setParameters("fm=0");
            }
            audioManager.setSpeakerphoneOn(b);
            Log.d(TAG, "setSpeakerphoneOn: " + b);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
                player.release();
                player = null;
            }
        }
        openOrCloseFM(false);
    }

}
