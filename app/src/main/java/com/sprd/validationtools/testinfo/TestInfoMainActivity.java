
package com.sprd.validationtools.testinfo;

import java.util.ArrayList;
import java.util.List;
//import com.android.internal.telephony.PhoneFactory;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.telephony.TelephonyManager;

import com.sprd.validationtools.R;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.IATUtils;
import com.sprd.validationtools.TestItem;
import com.sprd.validationtools.engtools.BtTestUtil;
import com.sprd.validationtools.engtools.WifiTestUtil;
import com.sprd.validationtools.itemstest.BluetoothTestActivity;
import com.sprd.validationtools.sqlite.EngSqlite;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.ValidationToolsMainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class TestInfoMainActivity extends Activity {
    private final static String TAG = "TestInfoMainActivity";
    private TextView mSNtxt, mSNtxtInfo;
    private TextView mIMEItxt, mIMEItxtInfo;
    private TextView mBLTtxt, mBLTtxtInfo;
    private TextView mWIFItxt, mWIFItxtInfo;
    private TextView mCHECKtxt, mCHECKtxtInfo;
    private TextView mTesttxt, mTesttxtInfo;
    private TextView mUid;

    private final int GET_SN = 0;
    private final int GET_IMEI = 1;
    private final int GET_WIFI = 3;
    private final int GET_PCHECK = 4;
    private final int GET_TESTR = 5;

    private  boolean mIsTested = false;

    Handler mTestInfoHandler = null;
    Handler mUiHandler = new UiHandler();

    /*SPRD bug 753913: Show BT off while BT close.*/
    private boolean mShowBTOff = /*Const.isBoardISharkL210c10()*/false;

    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive---------getAction="+intent.getAction());
            switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int blueState = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, 0);
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                        .getDefaultAdapter();
                String localAddress = bluetoothAdapter.getAddress();
                /*SPRD bug 753913: Show BT off while BT close.*/
                Log.d(TAG, "onReceive localAddress="+localAddress+",mShowBTOff="+mShowBTOff);
                if (mShowBTOff && blueState != BluetoothAdapter.STATE_ON) {
                    mBLTtxtInfo.setText("BT off!");
                } else {
                    mBLTtxtInfo.setText(localAddress);
                }
                /*@}*/
                break;
            }
        }
    };
    /*@}*/

    class UiHandler extends Handler {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SN:
                    Log.d(TAG, "UiHandler" + (String) msg.obj);
                    mSNtxtInfo.setText((String) msg.obj);
                    break;

                case GET_IMEI:
                    Log.d(TAG, "UiHandler" + (String) msg.obj);
                    mIMEItxtInfo.setText((String) msg.obj);
                    break;

                case GET_PCHECK:
                    Log.d(TAG, "UiHandler" + (String) msg.obj);
                    mCHECKtxtInfo.setText((String) msg.obj);
                    break;

                case GET_TESTR:
                    Log.d(TAG, "UiHandler" + (String) msg.obj);
                    mTesttxtInfo.setText((String) msg.obj);
                    break;
            }
        }
    }

    class TestInfoHandler extends Handler {
        public TestInfoHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String result = null;
            switch (msg.what) {
                case GET_SN: {
                    PhaseCheckParse parse = new PhaseCheckParse();
                    /* SPRD:435125  The serial number shows invalid in ValidationTools @{*/
                    mUiHandler.sendMessage(mUiHandler.obtainMessage(msg.what, 0, 0, parse.getSn()));
                    /* @}*/
                }
                    break;

                case GET_IMEI: {
                    TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    int phoneCnt = tm.getPhoneCount();
                    Log.d(TAG, "GET_IMEI===phoneCnt: " + phoneCnt);
                    String imei = "";

                    for (int i = 0; i < phoneCnt; i++) {
                        imei += "imei";
                        imei += String.valueOf(i + 1);
                        imei += ":";
                        if (getSystemService(TELEPHONY_SERVICE) != null) {
                            imei += ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
                                    .getDeviceId(i);
                        }
                        imei += "\n";
                    }
                    mUiHandler.sendMessage(mUiHandler.obtainMessage(msg.what, 0, 0, imei));
                }
                    break;

                case GET_PCHECK: {
                    PhaseCheckParse parse = new PhaseCheckParse();
                    mUiHandler.sendMessage(mUiHandler.obtainMessage(msg.what, 0, 0,
                            parse.getPhaseCheck()));
                }
                    break;

                case GET_TESTR: {
                    EngSqlite engSqlite = EngSqlite.getInstance(TestInfoMainActivity.this);
                    int failCount = engSqlite.queryFailCount();

                    if (mIsTested) {
                        if (failCount>0) {
                            result = "";
                            ArrayList<TestItem> supportList = Const.getSupportList(false, TestInfoMainActivity.this);
                            int index = 0;
                            for (int i = 0; i < supportList.size(); i++) {
                                if (Const.FAIL == engSqlite.getTestListItemStatus(supportList.get(i).getTestname())) {
                                    index = i + 1;
                                    result += index + "  " + supportList.get(i).getTestname()
                                            + "  Failed"
                                            + "\n";
                                }
                            }

                        } else{
                            result = "All Pass";
                        }
                    } else {
                        result = "Not Test";
                    }
                    mUiHandler.sendMessage(mUiHandler.obtainMessage(msg.what, 0, 0, result));
                }
                    break;
            }
        }
    }

    private BtTestUtil mBtTestUtil = null;
    private WifiTestUtil mWifiTestUtil = null;
    private WifiManager mWifiManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_info);
        HandlerThread ht = new HandlerThread(TAG);
        ht.start();
        mTestInfoHandler = new TestInfoHandler(ht.getLooper());
        init();
        /*SPRD bug 753913: Show BT off while BT close.*/
        if(mShowBTOff){
            registerReceiver(mReceiver, makeFilter());
        }
        /*@}*/
        mWifiManager = (WifiManager) getSystemService(this.WIFI_SERVICE);
        mBtTestUtil = new BtTestUtil() {

            public void btStateChange(int newState) {
                switch (newState) {
                case BluetoothAdapter.STATE_ON:
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                            .getDefaultAdapter();
                    String localAddress = bluetoothAdapter.getAddress();
                    /* SPRD bug 753913: Show BT off while BT close. */
                    Log.d(TAG, "btStateChange localAddress=" + localAddress
                            + ",mShowBTOff=" + mShowBTOff);
                    mBLTtxtInfo.setText(localAddress);
                    /* @} */
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    break;
                case BluetoothAdapter.STATE_OFF:
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    break;
                default:
                    break;
                }
            }

            public void btDeviceListAdd(BluetoothDevice device) {
            }

            public void btDiscoveryFinished() {
            }
        };
        mWifiTestUtil = new WifiTestUtil(mWifiManager) {

            public void wifiStateChange(int newState) {
                switch (newState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                        String macAddress = wifiInfo == null ? "Wlan off!" : wifiInfo.getMacAddress();
                        mWIFItxtInfo.setText(macAddress);
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                    default:
                        // do nothing
                        break;

                }
            }

            public void wifiDeviceListChange(List<ScanResult> wifiDeviceList) {
            }
        };
        if(mBtTestUtil != null){
            mBtTestUtil.startTest(this);
        }
        if(mWifiTestUtil != null){
            mWifiTestUtil.startTest(this);
        }
    }

    private void init() {
        mSNtxt = (TextView) findViewById(R.id.testinfo_sn);
        mSNtxtInfo = (TextView) findViewById(R.id.test_info_sn_dec);
        mIMEItxt = (TextView) findViewById(R.id.testinfo_imei);
        mIMEItxtInfo = (TextView) findViewById(R.id.test_info_imei_dec);
        mBLTtxt = (TextView) findViewById(R.id.testinfo_blt);
        mBLTtxtInfo = (TextView) findViewById(R.id.test_info_blt_dec);
        mWIFItxt = (TextView) findViewById(R.id.testinfo_wifi);
        mWIFItxtInfo = (TextView) findViewById(R.id.test_info_wifi_dec);
        mCHECKtxt = (TextView) findViewById(R.id.testinfo_phase);
        mCHECKtxtInfo = (TextView) findViewById(R.id.test_info_phase_dec);
        mTesttxt = (TextView) findViewById(R.id.testinfo_test);
        mTesttxtInfo = (TextView) findViewById(R.id.test_info_test_dec);
        mUid= (TextView) findViewById(R.id.uid);

        Intent intent = getIntent();
        if (intent != null)
            {
                mIsTested = intent.getBooleanExtra(ValidationToolsMainActivity.IS_SYSTEM_TESTED, false);
            }

        setTxtInfo();
    }

    private void setTxtInfo() {
        mTestInfoHandler.sendEmptyMessage(GET_SN);
        mTestInfoHandler.sendEmptyMessage(GET_IMEI);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        String localAddress = bluetoothAdapter.getAddress();
        /*SPRD bug 753913: Show BT off while BT close.*/
        Log.d(TAG, "setTxtInfo localAddress="+localAddress+",mShowBTOff="+mShowBTOff);
        //mBLTtxtInfo.setText(localAddress);
        if (mShowBTOff && bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
            mBLTtxtInfo.setText("BT off!");
        } else {
            mBLTtxtInfo.setText(localAddress);
        }
        /*@}*/

        WifiManager mWifiManager = (WifiManager) getSystemService(this.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String macAddress = wifiInfo == null ? "Wlan off!" : wifiInfo.getMacAddress();
        mWIFItxtInfo.setText(macAddress);

        mTestInfoHandler.sendEmptyMessage(GET_PCHECK);
        mTestInfoHandler.sendEmptyMessage(GET_TESTR);
        if(Const.fileIsExists("sys/class/misc/sprd_efuse_otp/uid")) {
            String uid = Const.readFile("sys/class/misc/sprd_efuse_otp/uid");
            if(uid != null && uid.contains(":")) {
                mUid.setText(uid.substring(uid.indexOf(":") + 1));
            } else {
                mUid.setText(uid);
            }
        } else {
            String uid = Const.readFile("sys/class/misc/sprd_otp_ap_efuse/uid");
            if(uid != null && uid.contains(":")) {
                mUid.setText(uid.substring(uid.indexOf(":") + 1));
            } else {
                mUid.setText(uid);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mTestInfoHandler != null) {
            Log.d(TAG, "HandlerThread has quit");
            mTestInfoHandler.getLooper().quit();
        }
        /*SPRD bug 753913: Show BT off while BT close.*/
        if(mShowBTOff && mReceiver != null){
            unregisterReceiver(mReceiver);
        }
        /*@}*/
        if(mBtTestUtil != null){
            mBtTestUtil.stopTest();
        }
        if(mWifiTestUtil != null){
            mWifiTestUtil.stopTest();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
