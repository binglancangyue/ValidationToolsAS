package com.sprd.validationtools.fingerprint.microarray;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.fpsensor.fpsensorManager.fpSensorManager;
import com.fpsensor.intf.IFpSensorTestTool;
import com.fpsensor.intf.IImageCaptureTool;
import com.fpsensor.intf.INavigatorTool;
import com.fpsensor.intf.IFingerDetectListener;

public class FactoryTestImpl implements IFactoryTestImpl {
    private static final String TAG = "factory_test_impl";

    fpSensorManager theFpSensorManager = null;
    IFpSensorTestTool theFpSensorTestTool = null;
    IImageCaptureTool theImageCaptureTool = null;
    INavigatorTool theNavigatorTool = null;

    ISprdFingerDetectListener fd_listener = null;

    boolean bNavigatorStatus = false;
    Context mCtx = null;

    public FactoryTestImpl(Context ctx) {
        mCtx = ctx;
    }

    // init variable
    public int factory_init() {
        theFpSensorManager = fpSensorManager.getFpManager(mCtx);
        if (theFpSensorManager != null) {
            theNavigatorTool = theFpSensorManager.getFpSensorNavigatorService();
            theFpSensorTestTool = theFpSensorManager
                    .getFpSensorTestToolService();
            theImageCaptureTool = theFpSensorManager
                    .getImageCaptureToolService();
        }
        if (theFpSensorTestTool == null || theImageCaptureTool == null) {
            return -1;
        }

        if (theNavigatorTool != null) {
            try {
                int iStatus = theNavigatorTool.getNavigatorStatus();
                bNavigatorStatus = (iStatus != 0);
                Log.d(TAG, " current navigator status is :" + bNavigatorStatus);
                if (bNavigatorStatus) {
                    Log.d(TAG, " disable navigator");
                    theNavigatorTool.enableNavigator(false);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                return -1;
            }
        }

        return 0;
    }

    // Restore navigator
    public int factory_exit() {
        if (bNavigatorStatus) {
            if (theNavigatorTool != null) {
                try {
                    theNavigatorTool.enableNavigator(true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    // fpSensorSelfTest perform spi test
    public int spi_test() {
        Log.d(TAG, "spi_test test invoked");
        int ret = -1;
        if (theFpSensorTestTool != null) {
            try {
                ret = theFpSensorTestTool.fpSensorSelfTest();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "spi_test test ret = " + ret);
        return ret;
    }

    // fpSensorSelfTest perform interrupt test
    public int interrupt_test() {
        return spi_test();
    }

    public int deadpixel_test() {
        Log.d(TAG, "deadpixel_test test invoked");
        int ret = -1;
        if (theFpSensorTestTool != null) {
            try {
                ret = theFpSensorTestTool.checkBoardTest();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "deadpixel_test test ret = " + ret);
        return ret;
    }

    IFingerDetectListener listener = new IFingerDetectListener.Stub() {
        @Override
        public void onFingerDetected(int arg0) throws RemoteException {
            Log.d(TAG, "the finger detect result is:" + arg0);
            if (fd_listener != null)
                fd_listener.on_finger_detected(arg0);
        }
    };

    public int finger_detect(ISprdFingerDetectListener theL) {
        Log.d(TAG, "finger_detect test invoked");
        fd_listener = theL;
        int ret = -1;
        try {
            ret = theImageCaptureTool.fingerDetect(listener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "finger_detect test ret = " + ret);
        return ret;
    }
}
