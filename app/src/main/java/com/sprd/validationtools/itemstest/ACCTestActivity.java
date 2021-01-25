package com.sprd.validationtools.itemstest;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

import java.util.Timer;
import java.util.TimerTask;

public class ACCTestActivity extends BaseActivity {
    private Context mContext;
    private int accState = -1;
    private static final String TAG = "ACCTestActivity";
    private Timer mTimer;
    private int time = 0;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        super.removeButton();
        mContext = this;
        mHandler = new Handler();
        setContentView(R.layout.activity_acc);
        startTime();
        registerACCContentObserver();
    }

    private final ContentObserver accContentObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mContext.getContentResolver().unregisterContentObserver(accContentObserver);
                    Toast.makeText(mContext, R.string.acc_test_success, Toast.LENGTH_SHORT).show();
                    cancelTimer();
                    storeRusult(true);
                    int accState = Settings.Global.getInt(mContext.getContentResolver(),
                            "bx_acc_state", 1);
                    Log.d(TAG, "onChange:accState " + accState);
                    finish();
                }
            });
        }
    };

    private void registerACCContentObserver() {
        mContext.getContentResolver().registerContentObserver(
                Settings.Global.getUriFor("bx_acc_state"),
                false, accContentObserver);
    }

    private void startTime() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                time++;
                if (time == 8) {
                    time = 0;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, R.string.acc_test_fail, Toast.LENGTH_SHORT).show();
                        }
                    });
                    cancelTimer();
                    storeRusult(false);
                    finish();
                }
            }
        }, 100, 1000);
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.purge();
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext.getContentResolver().unregisterContentObserver(accContentObserver);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        cancelTimer();
    }

}
