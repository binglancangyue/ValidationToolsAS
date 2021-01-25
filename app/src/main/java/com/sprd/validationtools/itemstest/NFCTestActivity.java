
package com.sprd.validationtools.itemstest;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.sprd.validationtools.R;
import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;

public class NFCTestActivity extends BaseActivity {
    private static final String TAG = "NFCTestActivity";
    TextView mContent;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    public static final int ACTION_TIME_OUT = 0;
    private static final int TIME_OUT = 20000;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case ACTION_TIME_OUT:
                /*SPRD bug 760913:Test can pass/fail must click button*/
                if(Const.isBoardISharkL210c10()){
                    Log.d("", "isBoardISharkL210c10 is return!");
                    return;
                }
                /*@}*/
                Log.d(TAG, "time out");
                storeRusult(false);
                finish();
                break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContent = new TextView(this);
        mContent.setGravity(Gravity.CENTER);
        mContent.setTextSize(20);
        setTitle(R.string.nfc_test);
        setContentView(mContent);
        boolean isSupportNfc = Const.isSupportNfc(this);
        mContent.setText("NFC Support:" + isSupportNfc);
        if(isSupportNfc) {
            mContent.append("\n Please close to the nfc card!");
        }
        Log.d(TAG, "onCreate");
        onNewIntent (getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter != null && !mNfcAdapter.isEnabled()) {
            mNfcAdapter.enable();
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(detectedTag != null) {
            mContent.setText(detectedTag.toString());
            Log.d(TAG, "tag:" + detectedTag.toString());
            Toast.makeText(this, R.string.text_pass, Toast.LENGTH_SHORT).show();
            /*SPRD bug 760913:Test can pass/fail must click button*/
            if(Const.isBoardISharkL210c10()){
                Log.d("", "isBoardISharkL210c10 is return!");
                return;
            }
            /*@}*/
            storeRusult(true);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessageDelayed(ACTION_TIME_OUT, TIME_OUT);
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(ACTION_TIME_OUT);
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            mNfcAdapter.disable();
        }
    }
}
