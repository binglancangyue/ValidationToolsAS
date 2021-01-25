package com.sprd.validationtools.camera;

import android.util.Log;

public class NativeCameraCalibration {
	private static final String TAG = "NativeCameraCalibration";

    static {
        try {
            System.loadLibrary("jni_dualcameraverify");
        } catch (UnsatisfiedLinkError e) {
            Log.d(TAG, " #loadLibrary jni_dualcameraverify failed  ");
            e.printStackTrace();
        }
    }
    
    public static native int native_dualCameraVerfication(String leftImage, String rightImage, String otpPath);

    /*SPRD bug 759782 : Display RMS value*/
    public static native double native_getCameraVerficationRMS();
    /*@}*/
}