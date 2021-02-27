package com.sprd.validationtools.itemstest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

import java.lang.ref.WeakReference;

public class SignalTestActivity extends BaseActivity {
    private static final String TAG = "SignalTestActivity";
    private StatusChangeBroadcastReceiver mBroadcastReceiver;
    private TelephonyManager telephonyManager;
    private String netWorkType = "";
    private int signalLevel = 0;
    private String yunYingShan = "";
    private TextView tvSignal;
    private TextView tvYunYing;
    private TextView tvType;
    private MyHandle myHandle;
    private String signalLevelString = "";
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal);
        initView();
        mBroadcastReceiver = new StatusChangeBroadcastReceiver();
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        myHandle = new MyHandle(this);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        getPhoneState(this);
        initBroadcastReceiver();
        getOperator();
        myHandle.sendEmptyMessageDelayed(2, 5000);
    }

    private void initView() {
        tvSignal = findViewById(R.id.tv_signal);
        tvType = findViewById(R.id.tv_type);
        tvYunYing = findViewById(R.id.tv_yunying);
    }

    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private static class StatusChangeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive:action " + action);
            if (action == null) {
                return;
            }
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo info =
                        intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info == null) {
                    ConnectivityManager connectivityManager =
                            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    info = connectivityManager.getActiveNetworkInfo();
                    Log.d(TAG, "onReceive: info==null");
                }

                if (info == null) {
                    Log.d(TAG, "onReceive: info2==null");
                    return;
                }
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    Log.d(TAG,
                            "onReceive:getState " + info.getState() + " isConnected " + info.isConnected());
                    if (NetworkInfo.State.CONNECTED == info.getState() && info.isConnected()) {

                    } else {

                    }
                }
            }
        }
    }

    public void getPhoneState(Context context) {
        PhoneStateListener MyPhoneListener = new PhoneStateListener() {
            @Override
            //获取对应网络的ID，这个方法在这个程序中没什么用处
            public void onCellLocationChanged(CellLocation location) {
                if (location instanceof GsmCellLocation) {
                    int CID = ((GsmCellLocation) location).getCid();
                } else if (location instanceof CdmaCellLocation) {
                    int ID = ((CdmaCellLocation) location).getBaseStationId();
                }
            }

            //系统自带的服务监听器，实时监听网络状态
            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);
            }

            //这个是我们的主角，就是获取对应网络信号强度
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                getOperator();
                //这个ltedbm 是4G信号的值
                String signalinfo = signalStrength.toString();
                String[] parts = signalinfo.split(" ");
                int ltedbm = Integer.parseInt(parts[9]);
                //这个dbm 是2G和3G信号的值
                int asu = signalStrength.getGsmSignalStrength();
                int dbm = -113 + 2 * asu;
                signalLevel = dbm;
                switch (telephonyManager.getNetworkType()) {
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        netWorkType = "4G";
                        if (dbm > -75) {
                            signalLevelString = "网络很好";
                        } else if (dbm > -85) {
                            signalLevelString = "网络不错";
                        } else if (dbm > -95) {
                            signalLevelString = "网络还行";
                        } else if (dbm > -100) {
                            signalLevelString = "网络很差";
                        } else {
                            signalLevelString = "网络错误";
                        }
                        Log.i("NetWorkUtil", "网络：4G 信号强度：" + ltedbm + "==Detail:" + signalinfo);
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        netWorkType = "3G";
                        String bin;
                        if (dbm > -75) {
                            bin = "网络很好";
                        } else if (dbm > -85) {
                            bin = "网络不错";
                        } else if (dbm > -95) {
                            bin = "网络还行";
                        } else if (dbm > -100) {
                            bin = "网络很差";
                        } else {
                            bin = "网络错误";
                        }
                        signalLevelString = bin;
                        Log.i("NetWorkUtil", "网络：3G 信号值：" + dbm + "==强度：" + bin + "==Detail:" + signalinfo);
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        netWorkType = "2G";
                        String bin1;
                        if (asu < 0 || asu >= 99) {
                            bin1 = "网络错误";
                        } else if (asu >= 16) {
                            bin1 = "网络很好";
                        } else if (asu >= 8) {
                            bin1 = "网络不错";
                        } else if (asu >= 4) {
                            bin1 = "网络还行";
                        } else {
                            bin1 = "网络很差";
                        }
                        signalLevelString = bin1;
                        Log.i("NetWorkUtil", "网络：2G：" + dbm + "==强度：" + bin1 + "==Detail:" + signalinfo);
                        break;
//                    default:
//                        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
//                        String strSubTypeName = info.getSubtypeName();
//                        if (strSubTypeName.equalsIgnoreCase("TD-SCDMA")
//                                || strSubTypeName.equalsIgnoreCase("WCDMA")
//                                || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
//                            netWorkType = "3G";
//                            String bin3G;
//                            if (dbm > -75) {
//                                bin3G = "网络很好";
//                            } else if (dbm > -85) {
//                                bin3G = "网络不错";
//                            } else if (dbm > -95) {
//                                bin3G = "网络还行";
//                            } else if (dbm > -100) {
//                                bin3G = "网络很差";
//                            } else {
//                                bin3G = "网络错误";
//                            }
//                            signalLevelString = bin3G;
//                            Log.i("NetWorkUtil", " 网络：default 信号值：" + dbm + "==强度：" + bin3G + "==Detail:" + signalinfo);
//                        } else {
//                            netWorkType = "Unknown";
//                            signalLevelString = "";
//                        }
//                        break;
                }
                super.onSignalStrengthsChanged(signalStrength);
                myHandle.sendEmptyMessage(1);
            }
        };
        telephonyManager.listen(MyPhoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    private void getOperator() {
        String imsi = telephonyManager.getSubscriberId();//获取SIM卡的IMSI
        if (imsi != null) {
            if (imsi.startsWith("46000")
                    || imsi.startsWith("46002")
                    || imsi.startsWith("46004")
                    || imsi.startsWith("46007")) {
                //因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
                yunYingShan = "中国移动";
            } else if (imsi.startsWith("46001")
                    || imsi.startsWith("46006")
                    || imsi.startsWith("46009")) {//中国联通
                yunYingShan = "中国联通";
            } else if (imsi.startsWith("46003")
                    || imsi.startsWith("46005")
                    || imsi.startsWith("46011")) {//中国电信
                yunYingShan = "中国电信";
            }
        } else {
            String operator = telephonyManager.getSimOperator();
            if (operator == null) {
                yunYingShan = "";
                return;
            }
            if (operator.startsWith("46000")
                    || operator.startsWith("46002")
                    || operator.startsWith("46004")
                    || operator.startsWith("46007")) {
                //因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
                yunYingShan = "中国移动";
            } else if (operator.startsWith("46001")
                    || operator.startsWith("46006")
                    || operator.startsWith("46009")) {//中国联通
                yunYingShan = "中国联通";
            } else if (operator.startsWith("46003")
                    || operator.startsWith("46005")
                    || operator.startsWith("46011")) {//中国电信
                yunYingShan = "中国电信";
            }
        }
    }

    private void updateInfo() {
        if (tvYunYing != null) {
            tvYunYing.setText(yunYingShan);
        }
        if (tvType != null) {
            tvType.setText(netWorkType);
        }
        if (tvSignal != null) {
            tvSignal.setText(signalLevel + " " + signalLevelString);
        }
    }

    private static class MyHandle extends Handler {
        private SignalTestActivity mActivity;

        MyHandle(SignalTestActivity activity) {
            WeakReference<SignalTestActivity> reference =
                    new WeakReference<>(activity);
            this.mActivity = reference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mActivity.updateInfo();
            }
            if (msg.what == 2) {
                mActivity.setResult();
            }
        }
    }

    private void setResult() {
        if (yunYingShan.equals("")) {
            Toast.makeText(SignalTestActivity.this, R.string.text_fail,
                    Toast.LENGTH_SHORT).show();
            storeRusult(false);
        } else {
            Toast.makeText(SignalTestActivity.this, R.string.text_pass,
                    Toast.LENGTH_SHORT).show();
            storeRusult(true);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

}
