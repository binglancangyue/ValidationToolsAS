package com.sprd.validationtools.fingerprint.microarray;

import android.util.Log;

public class FingerprintTest {

    static {
        /* SPRD bug 753816 : Add all test item to 10c10 */
        // Maybe cause remote UnsatisfiedLinkError.
        try {
            System.loadLibrary("fprint-x86_64");
            Log.d("FingerprintTest", "library name: fprint-x86_64");
        } catch (UnsatisfiedLinkError e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        /* @} */
    }

    static native public int nativeSet2FactoryMode();

    static native public int nativeSet2NormalMode();

    static native public int nativeSPICommunicate();

    static native public int nativeBadImage();

    static native public int nativeInterrupt();

    static native public int nativePress();

    static native public int nativeRemove();

    static native public String nativeGetVendor();
}

