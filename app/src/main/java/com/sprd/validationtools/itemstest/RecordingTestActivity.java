package com.sprd.validationtools.itemstest;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
    private boolean mRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.removeButton();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recording_voice);
        mContext = this;
        initView();
//        micLoopBack();
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

    private void micLoopBack() {
        //这两句话的作用是打开设备扬声器
        AudioManager service = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        service.setSpeakerphoneOn(true);
        int SAMPLE_RATE = 8000;
        int BUF_SIZE = 1024;

        //计算缓冲区尺寸
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        bufferSize = Math.max(bufferSize,
                AudioTrack.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));
        bufferSize = Math.max(bufferSize, BUF_SIZE);
        byte[] buffer = new byte[bufferSize];

        //创建音频采集设备，输入源是麦克风
        AudioRecord m_record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        //创建音频播放设备
        AudioTrack m_track = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM);

        m_track.setPlaybackRate(SAMPLE_RATE);

        //一边采集，一边播放
        m_record.startRecording();
        m_track.play();

        //需要停止的时候，把mRunning置为false即可。
        while (mRunning) {
            int readSize = m_record.read(buffer, 0, bufferSize);
            if (readSize > 0)
                m_track.write(buffer, 0, readSize);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRunning = false;
        releaseTimer();
        stopRecord();
    }
}
