
package com.sprd.validationtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.os.SystemProperties;
import android.util.Log;
import android.view.ViewConfiguration;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.Camera.CameraInfo;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.hardware.fingerprint.FingerprintManager;
import android.location.LocationManager;

import com.sprd.validationtools.camera.Camera3DCalibrationActivity;
import com.sprd.validationtools.camera.CameraTestActivity;
import com.sprd.validationtools.camera.CameraVerificationActivity;
import com.sprd.validationtools.camera.FrontCameraTestActivity;
import com.sprd.validationtools.camera.FrontCameraVerificationActivity;
import com.sprd.validationtools.camera.SecondaryCameraTestActivity;
import com.sprd.validationtools.fingerprint.FingerprintTestActivity;
import com.sprd.validationtools.itemstest.*;

//import com.sprd.validationtools.itemstest.FMTest;

public class Const {
    private static String TAG = "Const";

    public static boolean DEBUG = true;

//    public static final String ENG_ENGTEST_DB = "/productinfo/engtest.db";
    /*SPRD bug 830277:Change DB name.mmitest.db is used by APK MMI only.*/
    public static final String ENG_ENGTEST_DB = "/productinfo/mmitest.db";
    public static final String LED_PATH = "/sys/class/leds/red/brightness";
    public static final String CALIBRATOR_CMD = "/sys/class/sprd_sensorhub/sensor_hub/calibrator_cmd";
    public static final String CALIBRATOR_DATA = "/sys/class/sprd_sensorhub/sensor_hub/calibrator_data";
    public static final String CAMERA_FLASH = "/sys/devices/virtual/misc/sprd_flash/test";
    public static final String ENG_STRING2INT_TABLE = "str2int";
    public static final String ENG_STRING2INT_NAME = "name";
    public static final String ENG_STRING2INT_VALUE = "value";
    public static final String ENG_GROUPID_VALUE = "groupid";
    public static final int ENG_ENGTEST_VERSION = 2;
    public static final String RESULT_TEST_NAME = "result";

    public static final String INTENT_PARA_TEST_NAME = "testname";
    public static final String INTENT_PARA_TEST_INDEX = "testindex";
    public static final String INTENT_BACKGROUND_TEST_RESULT = "bgtestresult";
    public static final String INTENT_RESULT_TYPE = "resulttype";

    public static final int RESULT_TYPE_FOR_SYSTEMTEST = 0;
    public static final int RESULT_TYPE_NORMAL = 1;

    public final static int TEST_ITEM_DONE = 0;
    public final static int TEST_ITEM_DONE_RE = 1;
    public static final boolean IS_SUPPORT_LED_TEST = fileIsExists(LED_PATH);
    public static final boolean IS_SUPPORT_CALIBTATION_TEST = fileIsExists(CALIBRATOR_CMD);
    /*SRPD bug 776983:Add OTG*/
    public static final String OTG_PATH = "/sys/class/dual_role_usb/sprd_dual_role_usb/supported_modes";
    /*@}*/
    public static final boolean IS_SUPPORT_DUALCAMERA_CALIBRATION = isSupportBoardByName("sp9832e_1h10");

    /* SPRD Bug 746453:Some product don't support blue indicator light. @{ */
    public static final boolean DISABLE_BLUE_LED = isBoardISharkL210c10();
    /*@}*/

    public static final boolean IS_KD003 = true;

/*    public static final int[] ALL_TEST_ITEM_STRID = {
            R.string.version_test, R.string.rf_cali_test, R.string.rtc_test,
            R.string.backlight_test, R.string.lcd_test,
            R.string.touchpoint_test, R.string.muti_touchpoint_test,
            R.string.vibrator_test, R.string.phone_loopback_test,
            R.string.phone_call_test, R.string.gravity_sensor_test,
            R.string.oritention_sensor_test, R.string.proximity_sensor_test,R.string.proximity_sensor_noise_test,
            R.string.magnetic_test, R.string.gyroscope_test,
            R.string.pressure_test, R.string.a_sensor_calibration, R.string.g_sensor_calibration,
            R.string.m_sensor_calibration,R.string.prox_sensor_calibration,
            R.string.nfc_test,
            R.string.secondary_camera_title_text,
            R.string.front_camera_title_text,
            R.string.back_camera_title_text,
            R.string.camera_verification_test,
            R.string.camera_verification_test,
            R.string.front_camera_verification_test,
            R.string.finger_print_test,
            R.string.key_test,
            R.string.battery_title_text, R.string.headset_test,
            R.string.fm_test,R.string.soundtrigger_test ,R.string.status_indicator_red,
            R.string.status_indicator_green, R.string.status_indicator_blue,
            R.string.bt_test, R.string.wifi_test, R.string.gps_test,
            R.string.sdcard_test, R.string.sim_test,
            R.string.otg_test,
            R.string.TestResultTitleString,
    };*/
public static final int[] ALL_TEST_ITEM_STRID = {
        R.string.version_test, R.string.display_test_title,
        /* R.string.touchpoint_test, */R.string.muti_touchpoint_test,
        R.string.recording_test_title, R.string.tv_horn_test_title,
        R.string.gravity_sensor_test, R.string.front_camera_title_text,
        R.string.back_camera_title_text, R.string.fm_test,
        R.string.bt_test, R.string.wifi_test,
        R.string.gps_test, R.string.signal_test_title,
        R.string.sdcard_test, /*R.string.sim_test,*/
        R.string.battery_title_text,/* R.string.reversing_detection_title_test,*/
        R.string.acc_title_test, R.string.save_test_results_title,
        R.string.TestResultTitleString,
};

/*    public static final String[] ALL_TEST_ITEM_NAME = {
            "Version", "Lcd test", "TP test",
            "Multi-TP test", "Melody test", "Phone loopback test",
            "PhoneCall test", "Gsensor test", "Msensor test", "Proximity test", "Proximity noise test",
            "Magntic test", "Gyroscope test", "Pressure test", "Asensor calibration",
            "Gsensor calibration","Msensor calibration","Proxsensor calibration",
            "NFC Test",
            "SecondayCamera test",
            "FrontCamera test", "Camera test",
            "CameraVerification test",
            "CameraVerification test",
            "FrontCameraVerification test",
            "Fingerprint test",
            "Key test", "Charger test",
            "Headset test", "FM test", "soundtrigger test","RedLed test", "GreenLed test",
            "BlueLed test", "Bluetooth test", "Wifi test", "Gps test",
            "SDcard test", "SIMcard test",
            "OTG test",
            RESULT_TEST_NAME
    };*/
public static final String[] ALL_TEST_ITEM_NAME = {
        "Version", "Screen test",
        /*"TP test",*/ "Multi-TP test",
        "Recording test", "Horn test",
        "Gsensor test", "FrontCamera test",
        "BackCamera test", "FM test",
        "Bluetooth test", "Wifi test",
        "Gps test", "4g signal test",
        "SDcard test", /*"SIMcard test",*/
        "Charger test",/* "Reversing detection test",*/
        "ACC detection test", "Storage result",
        RESULT_TEST_NAME
};
    public static final String[] AUTO_TEST_ITEM_NAME = {
            "Version", "Screen test",
            /*"TP test",*/ "Multi-TP test",
            "Recording test", "Horn test",
            "Gsensor test", "FrontCamera test",
            "BackCamera test", "FM test",
            "Bluetooth test", "Wifi test",
            /*"Gps test",*/ "4g signal test",
            "SDcard test", /*"SIMcard test",*/
            "Charger test",/* "Reversing detection test",*/
            "ACC detection test", "Storage result",
            RESULT_TEST_NAME
    };

/*    public static final Class[] ALL_TEST_ITEM = {
            SystemVersionTest.class, RFCALITest.class, RTCTest.class,
            BackLightTest.class, ScreenColorTest.class,
            SingleTouchPointTest.class, MutiTouchTest.class, MelodyTest.class,
            PhoneLoopBackTest.class, PhoneCallTestActivity.class,
            GsensorTestActivity.class, CompassTestActivity.class,
            PsensorTestActivity.class, LsensorNoiseTestActivity.class,
            MagneticTestActivity.class,
            GyroscopeTestActivity.class, PressureTestActivity.class,
            ASensorCalibrationActivity.class, GSensorCalibrationActivity.class,
            MSensorCalibrationActivity.class,ProxSensorCalibrationActivity.class,
            NFCTestActivity.class,
            SecondaryCameraTestActivity.class,
            FrontCameraTestActivity.class, CameraTestActivity.class,
            Camera3DCalibrationActivity.class,
            CameraVerificationActivity.class,
            *//*SPRD bug 760136:Add FrontCameraVerification*//*
            FrontCameraVerificationActivity.class,
            FingerprintTestActivity.class,
            KeyTestActivity.class, ChargerTest.class, HeadSetTest.class,
            FMTest.class, SoundTriggerTestActivity.class ,RedLightTest.class, GreenLightTest.class,
            BlueLightTest.class, BluetoothTestActivity.class,
            WifiTestActivity.class, GpsTestActivity.class, SDCardTest.class,
            SIMCardTestActivity.class,
            OTGTest.class,
            TestResultActivity.class
    };*/
public static final Class[] ALL_TEST_ITEM = {
        SystemVersionTest.class, ScreenColorTest.class,
        /*SingleTouchPointTest.class,*/ /*ScreenTouchTestActivity.class,*/SingleTouchPointTest.class,
        RecordingTestActivity.class, HornTestActivity.class,/*PhoneLoopBackTest.class,*/
        GsensorTestActivity.class, FrontCameraTestActivity.class,
        CameraTestActivity.class, FMTestActivity.class,
        BluetoothTestActivity.class, WifiTestActivity.class,
        GpsTestActivity.class, SignalTestActivity.class,
        SDCardTest.class, /*SIMCardTestActivity.class,*/
        ChargerTest.class, /*ReversingDetectionTestActivity.class,*/
        ACCTestActivity.class, StorageTestActivity.class,
        TestResultActivity.class
};
    public static final Class[] AUTO_TEST_ITEM = {
            SystemVersionTest.class, ScreenColorTest.class,
            /*SingleTouchPointTest.class,*/ /*ScreenTouchTestActivity.class,*/SingleTouchPointTest.class,
            RecordingTestActivity.class, HornTestActivity.class,/*PhoneLoopBackTest.class,*/
            GsensorTestActivity.class, FrontCameraTestActivity.class,
            CameraTestActivity.class, FMTestActivity.class,
            BluetoothTestActivity.class, WifiTestActivity.class,
            /*GpsTestActivity.class,*/ SignalTestActivity.class,
            SDCardTest.class, /*SIMCardTestActivity.class,*/
            ChargerTest.class, /*ReversingDetectionTestActivity.class,*/
            ACCTestActivity.class, StorageTestActivity.class,
            TestResultActivity.class
    };

/*    public static final Class[] DEFAULT_UNIT_TEST_ITEMS = {
            SystemVersionTest.class, RFCALITest.class, RTCTest.class,
            BackLightTest.class, ScreenColorTest.class,
            SingleTouchPointTest.class, MutiTouchTest.class, MelodyTest.class,
            PhoneLoopBackTest.class, PhoneCallTestActivity.class,
            GsensorTestActivity.class, CompassTestActivity.class,
            PsensorTestActivity.class, LsensorNoiseTestActivity.class,
            MagneticTestActivity.class,
            GyroscopeTestActivity.class, PressureTestActivity.class,
            ASensorCalibrationActivity.class, GSensorCalibrationActivity.class,
            MSensorCalibrationActivity.class,ProxSensorCalibrationActivity.class,
            NFCTestActivity.class,
            SecondaryCameraTestActivity.class,
            FrontCameraTestActivity.class, CameraTestActivity.class,
            Camera3DCalibrationActivity.class,
            CameraVerificationActivity.class,
            *//*SPRD bug 760136:Add FrontCameraVerification*//*
            FrontCameraVerificationActivity.class,
            FingerprintTestActivity.class,
            KeyTestActivity.class, ChargerTest.class, HeadSetTest.class,
            FMTest.class, GreenLightTest.class,SoundTriggerTestActivity.class, BluetoothTestActivity.class,
            WifiTestActivity.class, GpsTestActivity.class, SDCardTest.class,
            SIMCardTestActivity.class,
            OTGTest.class,
            TestResultActivity.class
    };*/
public static final Class[] DEFAULT_UNIT_TEST_ITEMS = {
        SystemVersionTest.class, ScreenColorTest.class,
        /*SingleTouchPointTest.class,*/ /*ScreenTouchTestActivity.class,*/SingleTouchPointTest.class,
        RecordingTestActivity.class, HornTestActivity.class,/*PhoneLoopBackTest.class,*/
        GsensorTestActivity.class, FrontCameraTestActivity.class,
        CameraTestActivity.class, FMTestActivity.class,
        BluetoothTestActivity.class, WifiTestActivity.class,
        GpsTestActivity.class, SignalTestActivity.class,
        SDCardTest.class, /*SIMCardTestActivity.class,*/
        ChargerTest.class, /*ReversingDetectionTestActivity.class,*/
        ACCTestActivity.class, StorageTestActivity.class,
        TestResultActivity.class
};

	//RedLightTest.class, BlueLightTest.class,
/*    public static final Class[] DEFAULT_AUTO_TEST_ITEMS = {
    		SystemVersionTest.class, *//*RFCALITest.class, RTCTest.class,*//*
            ASensorCalibrationActivity.class, GSensorCalibrationActivity.class,
            MSensorCalibrationActivity.class,ProxSensorCalibrationActivity.class,
            BackLightTest.class, ScreenColorTest.class,
            SingleTouchPointTest.class, MutiTouchTest.class,
            PhoneLoopBackTest.class, MelodyTest.class,
            GsensorTestActivity.class, CompassTestActivity.class,
            PsensorTestActivity.class, LsensorNoiseTestActivity.class,
            MagneticTestActivity.class,
            GyroscopeTestActivity.class, PressureTestActivity.class,
            NFCTestActivity.class,
            SecondaryCameraTestActivity.class,
            FrontCameraTestActivity.class, CameraTestActivity.class,
            //Camera3DCalibrationActivity.class,
            *//*SPRD bug 760136:Add FrontCameraVerification*//*
            //CameraVerificationActivity.class,
            //FrontCameraVerificationActivity.class,
            *//*@}*//*
            FingerprintTestActivity.class,
            KeyTestActivity.class, ChargerTest.class, HeadSetTest.class,
            FMTest.class,GreenLightTest.class,
            PhoneCallTestActivity.class,
			OTGTest.class,
            TestResultActivity.class
    };*/
    public static final Class[] DEFAULT_AUTO_TEST_ITEMS = {
            SystemVersionTest.class, ScreenColorTest.class,
            /*SingleTouchPointTest.class,*/ /*ScreenTouchTestActivity.class,*/SingleTouchPointTest.class,
            RecordingTestActivity.class, HornTestActivity.class,/*PhoneLoopBackTest.class,*/
            GsensorTestActivity.class, FrontCameraTestActivity.class,
            CameraTestActivity.class, FMTestActivity.class,
            BluetoothTestActivity.class, WifiTestActivity.class,
            /*GpsTestActivity.class,*/ SignalTestActivity.class,
            SDCardTest.class, /*SIMCardTestActivity.class,*/
            ChargerTest.class, /*ReversingDetectionTestActivity.class,*/
            ACCTestActivity.class, StorageTestActivity.class,
            TestResultActivity.class
    };

    public static final Class[] DEFAULT_SYSTEM_TEST_ITEMS = {
            BackLightTest.class, ScreenColorTest.class,
            SingleTouchPointTest.class, MutiTouchTest.class,
            PhoneLoopBackTest.class, MelodyTest.class,
            GsensorTestActivity.class, CompassTestActivity.class,
            PsensorTestActivity.class, LsensorNoiseTestActivity.class,
            MagneticTestActivity.class,
            GyroscopeTestActivity.class, PressureTestActivity.class,
            ASensorCalibrationActivity.class, GSensorCalibrationActivity.class,
            MSensorCalibrationActivity.class,ProxSensorCalibrationActivity.class,
            NFCTestActivity.class,
            SecondaryCameraTestActivity.class,
            FrontCameraTestActivity.class, CameraTestActivity.class,
            Camera3DCalibrationActivity.class,
            CameraVerificationActivity.class,
            /*SPRD bug 760136:Add FrontCameraVerification*/
            FrontCameraVerificationActivity.class,
            FingerprintTestActivity.class,
            KeyTestActivity.class, ChargerTest.class, HeadSetTest.class,
            FMTest.class,
            // PhoneCallTestActivity.class,
            BluetoothTestActivity.class, WifiTestActivity.class,
            GpsTestActivity.class, SDCardTest.class, SIMCardTestActivity.class,
    };

    public static final String[][] ALL_TEST_ITEM_NAME_FLAG = {
            {"Version", null},
            {"RF CALI", null},
            {"RTC test", null},
            {"Backlight test", null},
            {"Lcd test", "70"},
            {"TP test", "80"},
            {"Multi-TP test", "80"},
            {"Melody test", "79"}, //74,79
            {"Phone loopback test", "75"}, //75,74
            {"PhoneCall test", null},
            {"Gsensor test", "82"},
            {"Msensor test", "82"},
            {"Proximity test", "82"},
            {"Magntic test", "82"},
            {"Gyroscope test", "82"},
            {"Pressure test", "82"},
            {"Asensor calibration", null},
            {"Gsensor calibration", null},
            {"Msensor calibration", null},
            {"Proxsensor calibration", null},
            {"NFC Test", "85"},
            {"SecondayCamera test", "72"},
            {"FrontCamera test", "73"},
            {"Camera test", "72"},
            {"Fingerprint test", "84"},
            {"Key test", "78"},
            {"Charger test", "86"},
            {"Headset test", "76"},
            {"FM test", "77"},
            {"soundtrigger test", null},
            {"RedLed test", null},
            {"GreenLed test", null},
            {"BlueLed test", null},
            {"Bluetooth test", null},
            {"Wifi test", null},
            {"Gps test", null},
            {"SDcard test", "90"},
            {"SIMcard test", "81"},
            {"OTG test", "91"},
            {RESULT_TEST_NAME, null}
    };

    // add status for test item
    public static final int FAIL = 0;
    public static final int SUCCESS = 1;
    public static final int DEFAULT = 2;

    // add the filter here
    private static boolean isSupport(Class className, Context context) {
        if (FrontCameraTestActivity.class == className) {
            int mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
            CameraInfo[] mInfo = new CameraInfo[mNumberOfCameras];
            for (int i = 0; i < mNumberOfCameras; i++) {
                mInfo[i] = new CameraInfo();
                android.hardware.Camera.getCameraInfo(i, mInfo[i]);
                if (mInfo[i].facing == CameraInfo.CAMERA_FACING_FRONT) {
                    return true;
                }
            }

            return false;
        } else if (CameraTestActivity.class == className) {
            int mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
            CameraInfo[] mInfo = new CameraInfo[mNumberOfCameras];
            for (int i = 0; i < mNumberOfCameras; i++) {
                mInfo[i] = new CameraInfo();
                android.hardware.Camera.getCameraInfo(i, mInfo[i]);
                if (mInfo[i].facing == CameraInfo.CAMERA_FACING_BACK) {
                    return true;
                }
            }

            return false;
        } else if (OTGTest.class == className) {
            /*SRPD bug 776983:Add OTG*/
            /*BufferedReader bReader = null;
            InputStream inputStream = null;
            try {
                Log.d(TAG, "OTGTest  OTG_PATH:"+OTG_PATH);
                inputStream = new FileInputStream(
                        OTG_PATH);
                bReader = new BufferedReader(new InputStreamReader(inputStream));
                String str = bReader.readLine();
                Log.d(TAG, "OTGTest  str:"+str);
                if (str.contains("ufp")) {
                    return true;
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "getSupportList()  Exception happens:");
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                Log.e(TAG, "getSupportList()  Exception happens:");
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (bReader != null) {
                        bReader.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "getSupportList()  Exception happens:");
                }

                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "getSupportList()  Exception happens:");
                }
            } */
            return false;
            /*@}*/
        } else if (CompassTestActivity.class == className) {
            return false;
        } else if (PsensorTestActivity.class == className) {
            SensorManager sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) == null) {
                return false;
            }
        } else if (GyroscopeTestActivity.class == className) {
            SensorManager sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null) {
                return false;
            }
        } else if (MagneticTestActivity.class == className) {
            SensorManager sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null) {
                return false;
            }
        } else if (PressureTestActivity.class == className) {
            SensorManager sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null) {
                return false;
            }
        } else if (ASensorCalibrationActivity.class == className) {
            SensorManager sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null
                    || !IS_SUPPORT_CALIBTATION_TEST) {
                return false;
            }
        } else if (GSensorCalibrationActivity.class == className) {
            SensorManager sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) == null
                    || !IS_SUPPORT_CALIBTATION_TEST) {
                return false;
            }
        /** BEGIN BUG479359 zhijie.yang 2016/5/5 MMI add the magnetic sensors and the prox sensor calibration**/
        } else if (MSensorCalibrationActivity.class == className) {
            SensorManager sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null
                    || !IS_SUPPORT_CALIBTATION_TEST) {
                return false;
            }
        } else if (ProxSensorCalibrationActivity.class == className) {
            SensorManager sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) == null
                    || !IS_SUPPORT_CALIBTATION_TEST) {
                return false;
            }
        /*BEGIN BUG555701 zhijie.yang 2016/05/21*/
        } else if (SecondaryCameraTestActivity.class == className) {
            int mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
            if (mNumberOfCameras <= 2) {
                return false;
            }
        } else if (SoundTriggerTestActivity.class == className) {
            if(!isWhale2Support() || isSharkL2Support()){
                return false;
            }
        } else if (NFCTestActivity.class == className) {
            return isSupportNfc(context);
        } else if (FingerprintTestActivity.class == className) {
            FingerprintManager mFingerprintManager = (FingerprintManager) context
                    .getSystemService(Context.FINGERPRINT_SERVICE);
            Log.d(TAG, "mFingerprintManager="+mFingerprintManager);
            if(mFingerprintManager == null){
                Log.d(TAG, "mFingerprintManager=null");
                return false;
            }
            if (mFingerprintManager != null && !mFingerprintManager.isHardwareDetected()) {
                Log.d(TAG, "mFingerprintManager=false");
                return false;
            }
        } else if (GpsTestActivity.class == className) {
            boolean result = isSupportGPS(context);
            Log.d(TAG, "isSupportGPS:" + result);
            return result;
        }
        /*END BUG555701 zhijie.yang 2016/05/21*/
        /** END BUG479359 zhijie.yang 2016/5/5 MMI add the magnetic sensors and the prox sensor calibration**/
        //SPRD: Modify for bug538349, open the RGB color indicator test.
        else if (RedLightTest.class == className) {
            if (!IS_SUPPORT_LED_TEST) {
                return false;
            }
        } else if (GreenLightTest.class == className) {
            if (!IS_SUPPORT_LED_TEST) {
                return false;
            }
        } else if (BlueLightTest.class == className) {
            if (!IS_SUPPORT_LED_TEST) {
                return false;
            }
            /* SPRD Bug 746453:Some product don't support blue indicator light. @{ */
            if (DISABLE_BLUE_LED) {
                return false;
            }
            /*@}*/
        }
        /*SPRD bug 743720:Add LsensorNoiseTestActivity*/
        else if (LsensorNoiseTestActivity.class == className) {
            SensorManager sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) == null) {
                Log.d(TAG, "LsensorNoiseTestActivity false");
                return false;
            }
            /*SPRD bug 820257:Display test item by file exists.*/
            if(fileIsExists(LsensorNoiseTestActivity.SensorNoiseFile)){
                Log.d(TAG, "LsensorNoiseTestActivity true");
                return true;
            }
            Log.d(TAG, "LsensorNoiseTestActivity false");
            return false;
            /*@}*/
        }
        /*@}*/
        else if (CameraVerificationActivity.class == className) {
            // boolean result = isSupportCameraVerification();
            // Log.d(TAG, "isSupportCameraVerification:" + result);
            /* SPRD bug 760136:Add FrontCameraVerification */
            int mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
            String cam3type = SystemProperties.get("persist.sys.cam3.type",
                    "unknown");
            Log.d(TAG, "CameraVerificationActivity mNumberOfCameras="
                    + mNumberOfCameras + ",cam3type=" + cam3type);
            if (mNumberOfCameras <= 2) {
                return false;
            }
            if (mNumberOfCameras == 3) {
                if (cam3type.equals("back_sbs") && IS_SUPPORT_DUALCAMERA_CALIBRATION) {
                    return true;
                } else {
                    return false;
                }
            }
            /* @} */
            // return result;
            return false;
        } else if (FrontCameraVerificationActivity.class == className) {
            /* SPRD bug 760136:Add FrontCameraVerification */
            int mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
            String cam3type = SystemProperties.get("persist.sys.cam3.type",
                    "unknown");
            Log.d(TAG, "FrontCameraVerificationActivity mNumberOfCameras="
                    + mNumberOfCameras + ",cam3type=" + cam3type);
            if (mNumberOfCameras <= 2) {
                return false;
            }
            if (mNumberOfCameras == 3) {
                if (cam3type.equals("front_sbs") && IS_SUPPORT_DUALCAMERA_CALIBRATION) {
                    return true;
                } else {
                    return false;
                }
            }
            /* @} */
            return false;
        }
        else if (Camera3DCalibrationActivity.class == className) {
            String calibraionSupport = SystemProperties.get("persist.sys.3d.calibraion",
                    "0");
            Log.d(TAG, "Camera3DCalibrationActivity calibraionSupport="+ calibraionSupport);
            if(calibraionSupport != null && calibraionSupport.equals("0")){
                return false;
            }
            int mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
            Log.d(TAG, "Camera3DCalibrationActivity mNumberOfCameras="
                    + mNumberOfCameras);
            if (mNumberOfCameras <= 2) {
                return false;
            }
        }
        return true;
    }

    /* SPRD: Modify for bug464743,Some phones don't support Led lights test.{@ */
    public static ArrayList<TestItem> getSupportList(
            boolean withResultActivity, Context context) {
        ArrayList<TestItem> supportArray = new ArrayList<TestItem>();
        for (int i = 0; i < DEFAULT_UNIT_TEST_ITEMS.length; i++) {
/*            for (int j = 0; j < ALL_TEST_ITEM.length; j++) {
                if (DEFAULT_UNIT_TEST_ITEMS[i].hashCode() == ALL_TEST_ITEM[j]
                        .hashCode() && isSupport(ALL_TEST_ITEM[j], context)) {
                    if (!withResultActivity
                            && ALL_TEST_ITEM[j]
                                    .equals(TestResultActivity.class)) {
                        continue;
                    }
                    TestItem item = new TestItem(j);
                    supportArray.add(item);
                }
            }*/
            TestItem item = new TestItem(i);
            supportArray.add(item);
        }
        return supportArray;
    }

    /**
     * by lym
     *
     * @param withResultActivity
     * @param context
     * @return
     */
    public static ArrayList<TestItem> getResultSupportList(
            boolean withResultActivity, Context context) {
        ArrayList<TestItem> supportArray = new ArrayList<TestItem>();
        for (int i = 0; i < DEFAULT_UNIT_TEST_ITEMS.length; i++) {
            if (!ALL_TEST_ITEM_NAME[i].equals(RESULT_TEST_NAME)) {
                TestItem item = new TestItem(i);
                supportArray.add(item);
            }
        }
        return supportArray;
    }

    /*SPRD bug 760913:Remove some test in 10c10*/
    public static ArrayList<TestItem> getSupportList10C10(
            boolean withResultActivity, Context context) {
        ArrayList<TestItem> supportArray = new ArrayList<TestItem>();

        for (int i = 0; i < DEFAULT_UNIT_TEST_ITEMS.length; i++) {
            for (int j = 0; j < ALL_TEST_ITEM.length; j++) {
                if (DEFAULT_UNIT_TEST_ITEMS[i].hashCode() == ALL_TEST_ITEM[j]
                        .hashCode() && isSupport(ALL_TEST_ITEM[j], context)) {
                    if (!withResultActivity
                            && ALL_TEST_ITEM[j]
                                    .equals(TestResultActivity.class)) {
                        continue;
                    }
                    if(ALL_TEST_ITEM[j].equals(RFCALITest.class)
                            || ALL_TEST_ITEM[j].equals(RTCTest.class)
                            || ALL_TEST_ITEM[j].equals(BluetoothTestActivity.class)
                            || ALL_TEST_ITEM[j].equals(WifiTestActivity.class)
                            || ALL_TEST_ITEM[j].equals(GpsTestActivity.class)){
                        continue;
                    }
                    TestItem item = new TestItem(j);
                    supportArray.add(item);
                }
            }
        }
        return supportArray;
    }
    /*@}*/
    public static ArrayList<TestItem> getSupportAutoTestList(Context context) {
        ArrayList<TestItem> supportArray = new ArrayList<TestItem>();
/*        for (int i = 0; i < DEFAULT_AUTO_TEST_ITEMS.length; i++) {
            for (int j = 0; j < ALL_TEST_ITEM.length; j++) {
                if (DEFAULT_AUTO_TEST_ITEMS[i].hashCode() == ALL_TEST_ITEM[j]
                        .hashCode() && isSupport(ALL_TEST_ITEM[j], context)) {
                    TestItem item = new TestItem(j);
                    supportArray.add(item);
                }
            }
        }*/
        for (int j = 0; j < DEFAULT_AUTO_TEST_ITEMS.length; j++) {
            TestItem item = new TestItem(j);
            Log.d(TAG, "getSupportAutoTestList: " + item.getAutoTestTestName());
            supportArray.add(item);
        }
        return supportArray;
    }

    public static boolean isCameraSupport() {
        int mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
        CameraInfo[] mInfo = new CameraInfo[mNumberOfCameras];
        for (int i = 0; i < mNumberOfCameras; i++) {
            mInfo[i] = new CameraInfo();
            android.hardware.Camera.getCameraInfo(i, mInfo[i]);
            if (mInfo[i].facing == CameraInfo.CAMERA_FACING_BACK) {
                return true;
            }
        }

        return false;
    }

    public static boolean isCmmbSupport() {
        boolean isSupport = SystemProperties.getBoolean(
                "ro.config.hw.cmmb_support", true);
        Log.d(TAG, "hw cmmb is support:" + isSupport);
        return isSupport;
    }

    public static boolean isHomeSupport(Context context) {
        /*
         * SPRD: modify 20140529 Spreadtrum of 305634 MMI test,lack of button which is on the right
         * 0f "Home" @{
         */
        boolean isSupport = ViewConfiguration.get(context)
                .hasPermanentMenuKey();
        // boolean isSupport =
        // SystemProperties.getBoolean("ro.config.hw.home_support", true);
        /* @} */
        Log.d(TAG, "hw home is support:" + isSupport);
        /* SPRD: modify 20150527 Spreadtrum of 440597 @{ */
        // return isSupport;
        return true;
        /* @} */
    }

    public static boolean isBackSupport(Context context) {
        /*
         * SPRD: modify 20140529 Spreadtrum of 305634 MMI test,lack of button which is on the right
         * 0f "Home" @{
         */
        boolean isSupport = ViewConfiguration.get(context)
                .hasPermanentMenuKey();
        // boolean isSupport =
        // SystemProperties.getBoolean("ro.config.hw.back_support", true);
        /* @} */
        Log.d(TAG, "hw Back is support:" + isSupport);
        /* SPRD: modify 20150527 Spreadtrum of 440597 @{ */
        // return isSupport;
        return true;
        /* @} */
    }

    public static boolean isMenuSupport(Context context) {
        // boolean isSupport =
        // SystemProperties.getBoolean("ro.config.hw.menu_support", true);
        boolean isSupport = ViewConfiguration.get(context)
                .hasPermanentMenuKey();
        Log.d(TAG, "hw menu is support:" + isSupport);
        /* SPRD: modify 20150527 Spreadtrum of 440597 @{ */
        // return isSupport;
        return true;
        /* @} */
    }

    public static boolean isVolumeUpSupport() {
        boolean isSupport = SystemProperties.getBoolean(
                "ro.config.hw.vol_up_support", true);
        Log.d(TAG, "hw VolumeUp is support:" + isSupport);
        return isSupport;
    }

    public static boolean isVolumeDownSupport() {
        boolean isSupport = SystemProperties.getBoolean(
                "ro.config.hw.vol_down_support", true);
        Log.d(TAG, "hw VolumeDown is support:" + isSupport);
        return isSupport;
    }

    /*
     * SPRD:Modify Bug 464743, check LED light /sys/class/leds/red/brightness
     * /sys/class/leds/green/brightness /sys/class/leds/blue/brightness
     * @{
     */
    public static boolean fileIsExists(String path) {
        try {
            File file = new File(path);
            Log.d(TAG, "fileIsExists path=" + path);
            if (!file.exists()) {
                Log.d(TAG, path + " fileIsExists false");
                return false;
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.d(TAG, path + " fileIsExists Exception e = " + e);
            return false;
        }
        Log.d(TAG, path + " fileIsExists true");
        return true;
    }

    /* @} */

    /*
    * SPRD:Modify Bug 537923, Judgment is not whale 2
    * @{
    */
    public static boolean isWhale2Support() {
        String hardware = SystemProperties.get("ro.boot.hardware", "unknown");
        /**BEGIN Bug 558940 zhijie.yang 2016/5/3 modify:phone loopback test fail **/
        if (hardware.contains("9860")) {
        /**END Bug 558940 zhijie.yang 2016/5/3 modify:phone loopback test fail **/
            return true;
        }
        return false;
    }

    public static boolean isIWhale2Support() {
        String hardware = SystemProperties.get("ro.boot.hardware", "unknown");
        if (hardware.contains("9861")) {
            return true;
        }
        return false;
    }

    public static boolean isISharkL2Support() {
        String hardware = SystemProperties.get("ro.boot.hardware", "unknown");
        if (hardware.contains("9853")) {
            return true;
        }
        return false;
    }

    public static boolean isSharkL2Support() {
        String hardware = SystemProperties.get("ro.boot.hardware", "unknown");
        if (hardware.contains("9850")) {
            return true;
        }
        return false;
    }

    /*SPRD bug 769258 : Sharkle support*/
    public static boolean isSharkLESupport() {
        String hardware = SystemProperties.get("ro.boot.hardware", "unknown");
        if (hardware.contains("9832e")) {
            return true;
        }
        return false;
    }
    /*@}*/

    /*SRPD bug 762371:Add new function*/
    public static boolean isSupportBoardByName(String boardName) {
        String board = SystemProperties.get("ro.product.board", "unknown");
        Log.d(TAG, "isSupportBoardByName board="+board+",boardName="+boardName);
        if (board != null && board.equals(boardName)) {
            return true;
        }
        return false;
    }
    /*@}*/

    public static boolean isSupportNfc(Context context) {
        boolean result = false;
        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null) {
            result = true;
        }
        return result;
    }

    public static boolean isRefMicSupport() {
        return !SystemProperties.getBoolean("ro.factory.remove.refmic", false);
    }

    public static boolean isSupportGPS(Context context) {
        LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(mgr == null)
            return false;
        List<String> providers = mgr.getAllProviders();
        if(providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    /*SPRD bug 746456:Is board ISharkL210c10*/
    public static boolean isBoardISharkL210c10() {
        String board = SystemProperties.get("ro.product.board", "unknown");
        Log.d(TAG, "isBoardISharkL210c10 board="+board);
        if (board != null && board.equals("sp9853i_10c10_vmm")) {
            return true;
        }
        if (board != null && board.equals("sp9832e_10c10_32b")) {
            return true;
        }
        return false;
    }
    /* @} */

    public static synchronized String readFile(String path) {
        File file = new File(path);
        String str = new String("");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                str = str + line;
            }
        } catch (Exception e) {
            Log.d(TAG, "Read file error!!!");
            str = "readError";
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        Log.d(TAG, "read " + path + " value is " + str.trim());
        return str.trim();
    }
}
