package com.sprd.validationtools.utils;

import android.util.Log;

public class Native {

    static {
        try {
            System.loadLibrary("jni_validationtools");
        } catch (UnsatisfiedLinkError e) {
            Log.d("ValidationToolsNative", " #loadLibrary jni_validationtools failed  ");
            e.printStackTrace();
        }
    }

    /**
     * send AT cmd to modem 
     *
     * @return (String: the return value of send cmd, "OK":sucess, "ERROR":fail)
     */
    public static native String native_sendATCmd(int phoneId, String cmd);

    public static native boolean native_hashValueWrited();

    public static native int native_get_rootflag();
}