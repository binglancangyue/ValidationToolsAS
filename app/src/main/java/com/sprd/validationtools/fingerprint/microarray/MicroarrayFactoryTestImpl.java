package com.sprd.validationtools.fingerprint.microarray;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

public class MicroarrayFactoryTestImpl implements IFactoryTestImpl {
    private static final String TAG = "factory_test_impl";

    public MicroarrayFactoryTestImpl(Context context) {

    }

    public int factory_init() {
        return FingerprintTest.nativeSet2FactoryMode();
    }

    public int factory_exit() {
        return FingerprintTest.nativeSet2NormalMode();
    }

    public int spi_test() {
        return FingerprintTest.nativeSPICommunicate();
    }

    public int interrupt_test() {
        return FingerprintTest.nativeInterrupt();
    }

    public int deadpixel_test() {
        return FingerprintTest.nativeBadImage();
    }

    public int finger_detect(ISprdFingerDetectListener listener) {
        int status = -1;
        int count = 0;
        if (listener != null) {
            while(status == -1 && count < 10){
                status = FingerprintTest.nativePress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                count++;
            }

            listener.on_finger_detected(status);
        }
        Log.d(TAG, "finger_detect test status = " + status);
        return status;
    }
}
