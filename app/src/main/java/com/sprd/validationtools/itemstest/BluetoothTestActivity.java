
package com.sprd.validationtools.itemstest;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;
import com.sprd.validationtools.engtools.BtTestUtil;

public class BluetoothTestActivity extends BaseActivity {
    private static final String TAG = "BluetoothTestActivity";

    private List<BluetoothDevice> mBluetoothDeviceList = new ArrayList<BluetoothDevice>();

    private TextView tvBtAddr = null;
    private TextView tvBtState = null;
    private TextView tvBtDeviceList = null;

    private BtTestUtil btTestUtil = null;
    private StringBuffer deviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.w(TAG, "+++++++enter bt+++++++++");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.bluetooth_result);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        btTestUtil = new BtTestUtil() {

            public void btStateChange(int newState) {
                switch (newState) {
                    case BluetoothAdapter.STATE_ON:
                        tvBtState.setText(R.string.bt_state_on);
                        // SPRD: update bluetooth address when bt power on
                        tvBtAddr.setText(btTestUtil.getBluetoothAdapter().getAddress());
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        tvBtState.setText(R.string.bt_state_closing);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        tvBtState.setText(R.string.bt_state_off);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        tvBtState.setText(R.string.bt_state_opening);
                        break;
                    default:
                        tvBtState.setText(R.string.bt_state_unknown);
                        break;
                }
            }

            public void btDeviceListAdd(BluetoothDevice device) {
                if (mBluetoothDeviceList.contains(device)) {
                    return;
                }
                if (device != null) {
                    mBluetoothDeviceList.add(device);
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        String name = device.getName();
                        if (name == null || name.isEmpty()) {
                            name = "No name";
                        }
                        deviceInfo = new StringBuffer();
                        deviceInfo.append("device name: ");
                        deviceInfo.append(name);
                        deviceInfo.append("\n");
                        Log.w(TAG, "======find bluetooth device => name : " + name
                                + "\n address :" + device.getAddress());
                        myHandle.sendEmptyMessage(1);
                    }
                } else {
                    Log.d(TAG, "btDeviceListAdd: device==null");
                }
            }

            public void btDiscoveryFinished() {
                btTestUtil.stopTest();
                for (BluetoothDevice s : mBluetoothDeviceList) {
                    Log.d(TAG, "btDiscoveryFinished: " + s.getName());
                }
                if (mBluetoothDeviceList != null
                        && mBluetoothDeviceList.size() > 0) {
                    Toast.makeText(BluetoothTestActivity.this, R.string.text_pass,
                            Toast.LENGTH_SHORT).show();
                    storeRusult(true);

                } else {
                    Toast.makeText(BluetoothTestActivity.this, R.string.text_fail,
                            Toast.LENGTH_SHORT).show();
                    storeRusult(false);

                }
                finish();
            }
        }

        ;

        tvBtAddr = (TextView)

                findViewById(R.id.bt_addr_content);

        tvBtState = (TextView)

                findViewById(R.id.bt_state_content);

        tvBtDeviceList = (TextView)

                findViewById(R.id.tv_bt_device_list);

        /*SPRD bug 817253:Maybe cause NullPointerException.*/
        if (btTestUtil.getBluetoothAdapter() != null) {
            tvBtAddr.setText(btTestUtil.getBluetoothAdapter().getAddress());
        } else {
            tvBtAddr.setText("NA");
            Log.w(TAG, "onCreate mBluetoothAdapter == null");
        }
        //tvBtAddr.setText(btTestUtil.getBluetoothAdapter().getAddress() + "\n");
    }

    public void onClick(View v) {
        btTestUtil.stopTest();
        super.onClick(v);
    }

    @Override
    protected void onResume() {
        super.onResume();
        btTestUtil.startTest(this);
    }

    /*SPRD: fix bug408662 stop bluetooth rest on pause @{ */
    @Override
    protected void onPause() {
        super.onPause();
        btTestUtil.stopTest();
    }
    /* @}*/

    public void onBackPressed() {
        btTestUtil.stopTest();
        super.onBackPressed();
    }

    private Handler myHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvBtDeviceList.append(deviceInfo.toString());
        }
    };
}


