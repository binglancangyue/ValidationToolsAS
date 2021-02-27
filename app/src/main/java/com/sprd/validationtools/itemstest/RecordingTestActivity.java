package com.sprd.validationtools.itemstest;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class RecordingTestActivity extends BaseActivity {
    private MediaRecorder mMediaRecorder;
    private String fileName;
    private String filePath;
    private static final String PHONE_STORAGE_PATH = "/data/data/com.sprd.validationtools/";
    private TextView tvRecordingTime;
    private Button btnRecord;
    private Timer mRecordingTimer;
    private int time = 0;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.removeButton();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_voice);
        mContext = this;
        initView();
    }

    private void start() {
        startRecord();
        mRecordingTimer = new Timer();
        mRecordingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                btnRecord.setEnabled(false);
                time++;
                if (time == 6) {
                    time = 0;
                    btnRecord.setEnabled(true);
                    stopRecord();
                    releaseTimer();
                }
                updateTime();
            }
        }, 100, 1000);
    }

    private void updateTime() {
        tvRecordingTime.post(new Runnable() {
            @Override
            public void run() {
                String times = String.format("%02d:%02d", time / 60 % 60, time % 60);
                tvRecordingTime.setText(times);
            }
        });
    }

    private void initView() {
        tvRecordingTime = findViewById(R.id.tv_record_time);
        btnRecord = findViewById(R.id.btn_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
    }

    public void startRecord() {
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            deleteFile();
            fileName = "recordingTestFile" + ".m4a";
            filePath = PHONE_STORAGE_PATH + fileName;
            File file = new File(PHONE_STORAGE_PATH, fileName);
            /* ②setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            /* ③准备 */
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            mMediaRecorder.prepare();
            /* ④开始 */
            mMediaRecorder.start();
        } catch (IllegalStateException | IOException e) {
            Log.e("RecordingTestActivity", "startRecord:e " + e.getMessage());
        }
    }

    private void deleteFile() {
        if (filePath == null) {
            return;
        }
        File file = new File(filePath);
        if (file != null && file.exists()) {
            file.delete();
        }
        filePath = "";
    }

    private void releaseTimer() {
        if (mRecordingTimer != null) {
            mRecordingTimer.purge();
            mRecordingTimer.cancel();
            mRecordingTimer = null;
        }
    }

    public void stopRecord() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
                filePath = "";
            }
        } catch (RuntimeException e) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseTimer();
        stopRecord();
    }
}
