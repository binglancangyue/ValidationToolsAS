package com.sprd.validationtools.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import java.nio.ByteBuffer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.Debug;
import android.Manifest;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.sqlite.EngSqlite;
import com.sprd.validationtools.utils.Native;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import android.widget.Toast;
import android.os.EnvironmentEx;
import android.media.MediaActionSound;
import android.media.ImageReader;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class FrontCameraVerificationActivity extends BaseActivity implements
        TextureView.SurfaceTextureListener {
    private static final String TAG = "FrontCameraVerificationActivity";
    private Handler mHandler;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    private CaptureRequest.Builder mPreviewBuilder;
    private TextureView mPreviewView;
    private CameraDevice mCameraDevice;
    private List<Surface> outputSurfaces = new ArrayList<Surface>(2);
    private String mCameraID = "2";

    private CameraCaptureSession mSession;
    private Size mPreviewSize;
    private ImageReader mImageReader;
    private int mState;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_CAPTURE = 1;
    private static final int START_VERIFICATION = 2;
    private static final int VERIFICATION_COMPELETE = 3;
    private MediaActionSound mCameraSound;
    private Button mTakePhotoBtn;
    private static final String LEFT_IMAGE = "/storage/emulated/0/20150101053124_1600x1200_main.NV21";
    private static final String RIGHT_IMAGE = "/storage/emulated/0/20150101053124_1600x1200_sub.NV21";
    private static final String OTP_PATH = "/storage/emulated/0/otp_original.txt";
    protected boolean mNeedThumb = false;
    protected ImageReader mThumbnailReader;
    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case START_VERIFICATION:
                startVerification();
                break;
            case VERIFICATION_COMPELETE:
                int res = (Integer) msg.obj;
                doResult(res);
                break;
            }
            super.handleMessage(msg);
        }

    };
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the
     * camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "onImageAvailable");
            // mHandler.post(new ImageSaver(reader.acquireNextImage(), new
            // File(LEFT_IMAGE)));

            FrontCameraVerificationActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(FrontCameraVerificationActivity.this,
                            "Camera verifying...", Toast.LENGTH_SHORT).show();
                }
            });
            if(mCameraSound != null){
                mCameraSound.play(MediaActionSound.SHUTTER_CLICK); 
            }
            if(mHandler != null){
                mHandler.post(new ImageAnalysis(reader.acquireNextImage()));
            }
            if(mCameraSound != null){
                mCameraSound.play(MediaActionSound.SHUTTER_CLICK);
            }
        }
    };
    private Runnable mTimeOut = new Runnable() {
        public void run() {
            FrontCameraVerificationActivity.this.storeRusult(false);
            FrontCameraVerificationActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.back_camera_result);
        mPreviewView = (TextureView) findViewById(R.id.surfaceView);
        mPreviewView.setSurfaceTextureListener(this);
        TextView mLightMsg = (TextView) findViewById(R.id.light_msg);
        mLightMsg.setText(R.string.secondary_msg_text);
        mLightMsg.setVisibility(View.GONE);
        String type = SystemProperties.get("persist.sys.cam3.type", "unknown");
        Log.d(TAG, "onCreate cam3.type=" + type);
        if("front_blur".equals(type) || "front_sbs".equals(type)) {
            mCameraID = SystemProperties.get("persist.sys.cam3.multi.cam.id", "2");
        }
        Log.d(TAG, "onCreate mCameraID=" + mCameraID);
        mTakePhotoBtn = (Button) findViewById(R.id.start_take_picture);
        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //captureStillPicture();
                autoFocus();
            }
        });
        //removeButton();
        startBackgroundThread();
        if(mPassButton != null){
           mPassButton.setVisibility(View.GONE);
        }
        if(mCameraSound == null){
            mCameraSound = new MediaActionSound();
            mCameraSound.load(MediaActionSound.SHUTTER_CLICK);
        }
    }
    
    public void onCameraSoundReleased() {
        if (mCameraSound != null) {
            mCameraSound.release();
            mCameraSound = null;
        }
    }

    /*SPRD bug 759782 : Display RMS value*/
    private double mCurrentRMS = 0.0;
    private static boolean ENABLE_RMS = true;
    private AlertDialog mDialog = null;
    private static boolean ENABLE_KEY_CAPTURE = true;
    private long mPreCurrentTime = 0l;

    private void showDialog(String text){
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
            mDialog = null;
        }
        if(isFinishing() || isDestroyed()){
            Log.w(TAG, "showDialog activity isDestroyed!");
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(FrontCameraVerificationActivity.this)
        .setTitle("RMS")
        .setMessage(text)
        .setCancelable(false)
        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                FrontCameraVerificationActivity.this.finish();
            }
        });
        mDialog = dialog.create();
        mDialog.show();
    }
    /*@}*/

    /*SPRD bug 760156:Capture picture on KEYCODE_VOLUME_DOWN*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onKeyDown keyCode="+keyCode+",ENABLE_KEY_CAPTURE="+ENABLE_KEY_CAPTURE);
        if(!ENABLE_KEY_CAPTURE){
            return super.onKeyDown(keyCode, event);
        }
        long curentTime = SystemClock.currentThreadTimeMillis();
        Log.d(TAG, "onKeyDown curentTime="+curentTime+",mPreCurrentTime="+mPreCurrentTime);
        if(curentTime - mPreCurrentTime < 10){
            return true;
        }
        mPreCurrentTime = curentTime;
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            //captureStillPicture();
            autoFocus();
        }
        return true;
    }
    /*@}*/

    protected void doResult(final int res) {
        if (res == 0) {
            FrontCameraVerificationActivity.this
                    .runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            storeRusult(true);
                            Toast.makeText(
                                    FrontCameraVerificationActivity.this,
                                    "Camera verification test success",
                                    Toast.LENGTH_SHORT).show();
                            /*SPRD bug 759782 : Display RMS value*/
                            if(ENABLE_RMS){
                                Log.d(TAG, "storeRusult mCurrentRMS="+mCurrentRMS);
                                DecimalFormat df = new DecimalFormat("0.0000");
                                String rms = df.format(mCurrentRMS);
                                Log.d(TAG, "storeRusult rms1="+rms);
                                rms = "rms : " + rms;
                                Log.d(TAG, "storeRusult rms2="+rms);
                                showDialog(rms);
                                Log.d(TAG, "storeRusult true");
                            }else{
                                FrontCameraVerificationActivity.this.finish();
                            }
                            /*@}*/
                        }
                    });
        } else {
            FrontCameraVerificationActivity.this
            .runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    storeRusult(false);
//                    FrontCameraVerificationActivity.this.finish();
                    Toast.makeText(
                            FrontCameraVerificationActivity.this,
                            "Camera verification test fail, error code:"
                                    + res, Toast.LENGTH_SHORT)
                            .show();
                    /*SPRD bug 759782 : Display RMS value*/
                    if(ENABLE_RMS){
                        Log.d(TAG, "storeRusult mCurrentRMS="+mCurrentRMS);
                        DecimalFormat df = new DecimalFormat("0.0000");
                        String rms = df.format(mCurrentRMS);
                        Log.d(TAG, "storeRusult rms1="+rms);
                        rms = "rms : " + rms;
                        Log.d(TAG, "storeRusult rms2="+rms);
                        showDialog(rms);
                        Log.d(TAG, "storeRusult true");
                    }else{
                        FrontCameraVerificationActivity.this.finish();
                    }
                    /*@}*/
                    Log.d(TAG, "storeRusult false");
                }
            });
        }
    }

    protected void startVerification() {
        Log.d(TAG, "native dualCameraVerfication begin...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int res = NativeCameraCalibration.native_dualCameraVerfication(LEFT_IMAGE,
                        RIGHT_IMAGE, OTP_PATH);
                Log.d(TAG, "native dualCameraVerfication result:" + res);
                /*SPRD bug 759782 : Display RMS value*/
                double rms = NativeCameraCalibration.native_getCameraVerficationRMS();
                mCurrentRMS = rms;
                Log.d(TAG, "native dualCameraVerfication rms:" + rms);
                /*@}*/
                Message msg = mMainHandler.obtainMessage(VERIFICATION_COMPELETE, res);
                mMainHandler.sendMessage(msg);
            }
        }).start();
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mHandler = new Handler(mBackgroundThread.getLooper());
        mHandler.postDelayed(mTimeOut, 120000);
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void captureStillPicture() {
        try {
            final CaptureRequest.Builder captureBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
//            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
//                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                        CaptureRequest request, TotalCaptureResult result) {
                    session.close();
                    session = null;
                }
            };
            Log.d(TAG, "capture  in camera");
            SystemProperties.set("persist.sys.cam.tool.debug", "1");
            mSession.capture(captureBuilder.build(), CaptureCallback, mHandler);
        } catch (Exception e) {
            Log.d(TAG, "capture a picture1 fail" + e.toString());
        }
    }

    private boolean mFocusing = false;
    
    private void autoFocus() {
        try {
            if(mFocusing){
                Log.d(TAG, "autoFocus mFocusing="+mFocusing);
                return;
            }
//          final CaptureRequest.Builder captureBuilder = mCameraDevice
//                  .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//          captureBuilder.addTarget(mImageReader.getSurface());
			final CaptureRequest.Builder captureBuilder = mPreviewBuilder;
			captureBuilder.set(CaptureRequest.CONTROL_MODE,
					CaptureRequest.CONTROL_MODE_AUTO);
			captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
					CaptureRequest.CONTROL_AF_MODE_AUTO);
			captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
					CaptureRequest.CONTROL_AF_TRIGGER_START);
//          CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
//              @Override
//              public void onCaptureCompleted(CameraCaptureSession session,
//                      CaptureRequest request, TotalCaptureResult result) {
//                  session.close();
//                  session = null;
//              }
//          };
            Log.d(TAG, "autoFocus capture  in camera");
            // SystemProperties.set("persist.sys.cam.tool.debug", "1");
            // mSession.capture(captureBuilder.build(), CaptureCallback,
            // mHandler);
            mSession.capture(captureBuilder.build(), deferredCallbackSetter,
                    mHandler);
            captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
            mFocusing = true;
        } catch (Exception e) {
            Log.d(TAG, "autoFocus capture a picture1 fail" + e.toString());
        }
    }

    CameraCaptureSession.CaptureCallback deferredCallbackSetter = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(CameraCaptureSession session,
                CaptureRequest request, CaptureResult result) {
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session,
                CaptureRequest request, TotalCaptureResult result) {
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session,
                CaptureRequest request, CaptureFailure failure) {
            Log.e(TAG, "Focusing failed with reason " + failure.getReason());
        }
    };


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
            int height) {
        Log.d(TAG, "onSurfaceTextureAvailable");
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;
        try {
            characteristics = cameraManager.getCameraCharacteristics(mCameraID);
        } catch (CameraAccessException e) {
            Log.d(TAG, "p1 in" + e.toString());
            Toast.makeText(FrontCameraVerificationActivity.this, R.string.text_fail,
                    Toast.LENGTH_SHORT).show();
            storeRusult(false);
            finish();
            return;
        }
        StreamConfigurationMap map = characteristics
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        mPreviewSize = Collections.max(
                Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                new CompareSizesByArea());
        mImageReader = ImageReader.newInstance(2592, 1944, ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,
                mHandler);

        try {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(FrontCameraVerificationActivity.this,
                        "not permission", Toast.LENGTH_LONG).show();
                return;
            }
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                Toast.makeText(FrontCameraVerificationActivity.this, "no CloseLock",
                        Toast.LENGTH_LONG).show();
                return;
            }
            Log.d(TAG, "onSurfaceTextureAvailable openCamera mCameraID:"
                    + mCameraID);
            cameraManager.openCamera(mCameraID, mCameraDeviceStateCallback,
                    mHandler);
            Log.d(TAG, "onSurfaceTextureAvailable openCamera end");
        } catch (CameraAccessException e) {
            Log.d(TAG, "CameraAccessException" + e.toString());
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException" + e.toString());
        }

    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight()
                    - (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
            int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            Log.i(TAG, "CameraDevice.StateCallback onOpened in");
            mCameraDevice = camera;
            startPreview(camera);
            Log.i(TAG, "CameraDevice.StateCallback onOpened out");
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            Log.i(TAG, "CameraDevice.StateCallback1 stay onDisconnected");
            if (mThumbnailReader != null) {
                Log.i(TAG, "mThumbnailReader.close");
                mThumbnailReader.close();
                mThumbnailReader = null;
            }
            mNeedThumb = false;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            Log.i(TAG, "CameraDevice.StateCallback1 stay onError");
        }
    };

    private void startPreview(CameraDevice camera) {
        Log.i(TAG, "start preview ");
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
        texture.setDefaultBufferSize(960, 720);
        Surface surface = new Surface(texture);
        try {

            mPreviewBuilder = camera
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // mPreviewBuilder.set("srpd3dVerificationEnable", 1);
            // mPreviewBuilder.set(CaptureRequest.ANDROID_SPRD_3DCALIBRATION_ENABLED,
            // 1);
            //Auto focus
//            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
//            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
//                    CaptureRequest.CONTROL_AF_TRIGGER_START);
        } catch (CameraAccessException e) {
            Log.i(TAG, e.toString());
        }
        mPreviewBuilder.addTarget(surface);

        if (mThumbnailReader != null) {
            mThumbnailReader.close();
        }
        try {
            if (mNeedThumb) {
                mThumbnailReader = ImageReader.newInstance(320, 240,
                        ImageFormat.YUV_420_888, 1);
                camera.createCaptureSession(Arrays.asList(surface,
                        mImageReader.getSurface(),
                        mThumbnailReader.getSurface()),
                        mCameraCaptureSessionStateCallback, mHandler);
            } else {
                camera.createCaptureSession(
                        Arrays.asList(surface, mImageReader.getSurface()),
                        mCameraCaptureSessionStateCallback, mHandler);
            }
        } catch (CameraAccessException ex) {
            Log.e(TAG, "Failed to create camera capture session", ex);
        }
    }

    private CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                mSession = session;
                mSession.setRepeatingRequest(mPreviewBuilder.build(),
                        mSessionCaptureCallback, mHandler);
            } catch (CameraAccessException e) {
                Log.i(TAG, e.toString());
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }

        @Override
        public void onActive(CameraCaptureSession session) {

        }
    };
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session,
                CaptureRequest request, TotalCaptureResult result) {
            mSession = session;
            Integer afStateMaybe = result.get(CaptureResult.CONTROL_AF_STATE);
            Log.d(TAG, "onCaptureCompleted afStateMaybe=" + afStateMaybe);
            mFocusing = false;
            if (afStateMaybe == CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED
                    || afStateMaybe == CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED 
                    /*|| afStateMaybe == CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN*/) {
                if (mCameraSound != null) {
                    mCameraSound.play(MediaActionSound.FOCUS_COMPLETE);
                }
                captureStillPicture();
            }
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session,
                CaptureRequest request, CaptureResult partialResult) {
            mSession = session;
        }
    };

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        onCameraSoundReleased();
        //Makesure
		/*SPRD bug 778246:Make sure properties reset.*/
        SystemProperties.set("persist.sys.cam.tool.debug", "0");
        /*@}*/
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mTimeOut);
        }
        /*SPRD bug 759782 : Display RMS value*/
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
            mDialog = null;
        }
        /*@}*/
        super.onDestroy();
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mSession) {
                mSession.close();
                mSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException" + e.toString());
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void getSprdSrRealBokehData(byte[] data) {
        Log.d(TAG, "getSprdSrRealBokehData()");
        try {
            byte[] content = data;
            // byte[] content = streamToByte(new FileInputStream(LEFT_IMAGE));
            if (content != null) {
                int mainYuvLength = 0;
                int subYuvLength = 0;
                int otpLength = 0;
                String bokehFlag = bytesToChar(content, content.length - 1 * 4); // 1
                                                                                    // bokeh
                                                                                    // Flag
                                                                                    // ->
                                                                                    // VERI
                int otpSize = getIntValue(content, 2); // 2 otpSize
                int imageSize = getIntValue(content, 3); // 3 main & sub yuv
                                                            // size
                int height = getIntValue(content, 4); // 12 MainHeightData
                int width = getIntValue(content, 5); // 13 MainWidthData
                Log.d(TAG, "bokehFlag:" + bokehFlag + " otpSize:" + otpSize
                        + " imageSize:" + imageSize + " height:" + height
                        + " width:" + width);

                otpLength = content.length - 5 * 4 - otpSize;
                // otp byte[]
                byte[] otpByte = new byte[otpSize];
                for (int i = 0; i < otpSize; i++) {
                    otpByte[i] = content[otpLength + i];
                }
                dumpDate(otpByte, OTP_PATH);
                Log.d(TAG, "otp dump complete.." + otpByte.length);

                subYuvLength = otpLength - imageSize;
                // subyuv byte[]
                byte[] subYuvByte = new byte[imageSize];
                for (int i = 0; i < imageSize; i++) {
                    subYuvByte[i] = content[subYuvLength + i];
                }
                dumpDate(subYuvByte, RIGHT_IMAGE);
                Log.d(TAG, "subYuvByte dump complete.." + subYuvByte.length);

                mainYuvLength = subYuvLength - imageSize;
                // mainyuv byte[]
                byte[] mainYuvByte = new byte[imageSize];
                for (int i = 0; i < imageSize; i++) {
                    mainYuvByte[i] = content[mainYuvLength + i];
                }
                dumpDate(mainYuvByte, LEFT_IMAGE);
                Log.d(TAG, "mainYuvByte dump complete.." + mainYuvByte.length);
            }
        } catch (Exception e) {
            Log.e(TAG, "sprd SR-real-bokeh refocus Exception!", e);
            e.printStackTrace();
        }
    }

    private void dumpDate(byte[] data, String path) {
        FileOutputStream fileOutput = null;
        try {
            fileOutput = new FileOutputStream(path);
            fileOutput.write(data);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutput != null) {
                try {
                    fileOutput.close();
                } catch (Exception t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public byte[] streamToByte(InputStream inStream) {
        byte[] data = null;
        try {
            Log.d(TAG, "streamToByte start.");
            data = new byte[inStream.available()];
            inStream.read(data);
            inStream.close();
            Log.d(TAG, "streamToByte end.");
        } catch (IOException e) {
            Log.e(TAG, "streamToByte Exception ", e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Throwable t) {
                    // do nothing
                }
            }
        }
        return data;
    }

    public static String bytesToChar(byte[] bytes, int offset) {
        char a = (char) (bytes[offset] & 0xFF);
        char b = (char) (bytes[offset + 1] & 0xFF);
        char c = (char) (bytes[offset + 2] & 0xFF);
        char d = (char) (bytes[offset + 3] & 0xFF);
        String s = new String(new char[] { a, b, c, d });
        return s;
    }

    private int getIntValue(byte[] content, int position) {
        int value = bytesToInt(content, content.length - position * 4);
        return value;
    }

    private static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16) | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    private class ImageAnalysis implements Runnable {

        private final Image mImage;

        public ImageAnalysis(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            Log.d(TAG, "ImageAnalysis....run...");
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            getSprdSrRealBokehData(bytes);

            closeCamera();
            SystemProperties.set("persist.sys.cam.tool.debug", "0");
            mMainHandler.sendEmptyMessage(START_VERIFICATION);
        }
    }

    private static class ImageSaver implements Runnable {

        private final Image mImage;

        private final File mFile;

        public ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                if (!mFile.getParentFile().exists()) {
                    mFile.getParentFile().mkdirs();
                }
                if (!mFile.exists()) {
                    mFile.createNewFile();
                }
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
