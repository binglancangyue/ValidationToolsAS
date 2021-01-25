package com.sprd.validationtools.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ShortBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.SprdCaptureResult;
import android.hardware.camera2.SprdCaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.EnvironmentEx;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;


public class Camera3DCalibrationActivity extends BaseActivity {

    private static final String TAG = "Camera3DCalibrationActivity";
    private static final String SPRD_3D_CALIBRATION_PATH = "/productinfo/sprd_3d_calibration/";
    // /storage/emulated/0/mmi
    private static final String YUV_TMP_PATH = EnvironmentEx
            .getInternalStoragePath().getAbsolutePath() + "/mmi/";
    // private static final String SPRD_3D_CALIBRATION_PATH =
    // "/data/local/tmp/sprd_3d_calibration/";
    private static final String LEFT_JPEG_PATH = SPRD_3D_CALIBRATION_PATH
            + "jpeg_left.jpeg";
    private static final String RIGHT_JPEG_PATH = SPRD_3D_CALIBRATION_PATH
            + "jpeg_right.jpeg";
    private static final String LEFT_YUV_PATH = YUV_TMP_PATH + "yuv_left.yuv";
    private static final String RIGHT_YUV_PATH = YUV_TMP_PATH + "yuv_right.yuv";

    private static final String LEFT_CAMERA_ID = "8";
    private static final String RIGHT_CAMERA_ID = "2";

    private static final int LEFT_CAMERA_CAPTURE_WIDTH = 4224;
    private static final int LEFT_CAMERA_CAPTURE_HEIGHT = 3136;
    private static final int RIGHT_CAMERA_CAPTURE_WIDTH = 2592;
    private static final int RIGHT_CAMERA_CAPTURE_HEIGHT = 1944;

    private int mLeftCaptureWidth = LEFT_CAMERA_CAPTURE_WIDTH;
    private int mLeftCaptureHeight = LEFT_CAMERA_CAPTURE_HEIGHT;
    private int mRightCaptureWidth = RIGHT_CAMERA_CAPTURE_WIDTH;
    private int mRightCaptureHeight = RIGHT_CAMERA_CAPTURE_HEIGHT;

    private Handler mHandler1;
    private HandlerThread mThreadHandler1;
    private Handler mHandler2;
    private HandlerThread mThreadHandler2;

    private CaptureRequest.Builder mPreviewBuilder1;
    private CaptureRequest.Builder mPreviewBuilder2;

    private TextureView mPreviewView1;
    private TextureView mPreviewView2;

    private CameraDevice mCameraDevice1;
    private CameraDevice mCameraDevice2;

    private List<Surface> outputSurfaces = new ArrayList<Surface>(2);

    private CameraCaptureSession mSession1;
    private CameraCaptureSession mSession2;

    private Size mPreviewSize;

    private ImageReader mImageReader1;
    private ImageReader mImageReader11;
    private ImageReader mImageReader2;
    private ImageReader mImageReader22;

    private int mState;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_CAPTURE = 1;
    private TextView mTextView1;

    private boolean picReady1 = false;
    private boolean picReady2 = false;
    private boolean mIsPrev1Finish = false;

    private MediaActionSound mCameraSound;

    private Button mTakePhotoBtn = null;

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener1 = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            // Toast.makeText(getApplicationContext(),
            mHandler1.post(new ImageSaver(reader.acquireNextImage(), new File(
                    LEFT_YUV_PATH)));
        }

    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener11 = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            mHandler1.post(new ImageSaver(reader.acquireNextImage(), new File(
                    LEFT_JPEG_PATH)));
        }

    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener2 = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            // Toast.makeText(getApplicationContext(),
            mHandler2.post(new ImageSaver(reader.acquireNextImage(), new File(
                    RIGHT_YUV_PATH)));
        }

    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener22 = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            mHandler2.post(new ImageSaver(reader.acquireNextImage(), new File(
                    RIGHT_JPEG_PATH)));
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_3d_calibration);
        mTextView1 = (TextView) findViewById(R.id.txt_left);
        mTextView1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // startCalibration();
            }
        });
        mTakePhotoBtn = (Button) findViewById(R.id.start_take_picture);
        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "mTakePhotoBtn take picture 1 is start");
                //captureWorker();
                //We will wait for AE state is convered
                mState = STATE_WAITING_CAPTURE;
            }
        });
        if (mCameraSound == null) {
            mCameraSound = new MediaActionSound();
            mCameraSound.load(MediaActionSound.SHUTTER_CLICK);
        }

        try {
            FileUtils.deleteFile(new File(DST_RESULT_BIN_FILE));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        Log.d(TAG, "I'm 10.4");

    }

    private void captureWorker(){
        Log.d(TAG, "captureWorker take picture 1 is start");
        captureStillPicture();
        Log.d(TAG, "captureWorker take picture 1 is finish");
        Log.d(TAG, "captureWorker take picture 2 is start");
        captureStillPicture2();
        Log.d(TAG, "captureWorker take picture 2 is finish");
    }

    private boolean masterIsOk = false;

    public void startTest() {
        mPreviewView1
                .setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

                    @Override
                    public void onSurfaceTextureAvailable(
                            SurfaceTexture surface, int width, int height) {
                        Log.d(TAG, "p1 in");
                        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                        CameraCharacteristics characteristics = null;
                        try {
                            characteristics = cameraManager
                                    .getCameraCharacteristics(LEFT_CAMERA_ID);
                        } catch (CameraAccessException e) {
                            Log.d(TAG, "p1 in" + e.toString());
                            Toast.makeText(getApplicationContext(),
                                    R.string.text_fail, Toast.LENGTH_SHORT)
                                    .show();
                            storeRusult(false);
                            finish();
                            return;
                        }
                        StreamConfigurationMap map = characteristics
                                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                        // mPreviewSize =
                        // map.getOutputSizes(SurfaceTexture.class)[0];
                        mPreviewSize = Collections.max(Arrays.asList(map
                                .getOutputSizes(ImageFormat.JPEG)),
                                new CompareSizesByArea());
//                        mImageReader1 = ImageReader.newInstance(LEFT_CAMERA_CAPTURE_WIDTH, LEFT_CAMERA_CAPTURE_HEIGHT,
//                                ImageFormat.YUV_420_888, 1);
//                        mImageReader1.setOnImageAvailableListener(
//                                mOnImageAvailableListener1, mHandler1);

                        // mImageReader11 =
                        // ImageReader.newInstance(1600,1200,ImageFormat.JPEG,1);
                        // mImageReader11.setOnImageAvailableListener(
                        // mOnImageAvailableListener11, mHandler1);
                        try {
                            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(getApplicationContext(),
                                        "not permission", Toast.LENGTH_LONG)
                                        .show();
                                return;
                            }
                            cameraManager.openCamera(LEFT_CAMERA_ID,
                                    mCameraDeviceStateCallback1, mHandler1);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(
                            SurfaceTexture surface, int width, int height) {

                    }

                    @Override
                    public boolean onSurfaceTextureDestroyed(
                            SurfaceTexture surface) {
                        Log.i(TAG,
                                "mPreviewView1.setSurfaceTextureListener stay onSurfaceTextureDestroyed");
                        return false;
                    }

                    @Override
                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                    }
                });

        mPreviewView2
                .setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

                    @Override
                    public void onSurfaceTextureAvailable(
                            SurfaceTexture surface, int width, int height) {
                        Log.d(TAG, "p2 in");
                        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                        CameraCharacteristics characteristics = null;
                        try {
                            characteristics = cameraManager
                                    .getCameraCharacteristics(RIGHT_CAMERA_ID);
                        } catch (CameraAccessException e) {
                            Log.d(TAG, "p2 in" + e.toString());
                            Toast.makeText(getApplicationContext(),
                                    R.string.text_fail, Toast.LENGTH_SHORT)
                                    .show();
                            storeRusult(false);
                            finish();
                            return;
                        }

                        StreamConfigurationMap map = characteristics
                                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                        mPreviewSize = Collections.max(Arrays.asList(map
                                .getOutputSizes(ImageFormat.JPEG)),
                                new CompareSizesByArea());

//                        mImageReader2 = ImageReader.newInstance(RIGHT_CAMERA_CAPTURE_WIDTH, RIGHT_CAMERA_CAPTURE_HEIGHT,
//                                ImageFormat.YUV_420_888, 1);
//                        mImageReader2.setOnImageAvailableListener(
//                                mOnImageAvailableListener2, mHandler2);

                        // mImageReader22 =
                        // ImageReader.newInstance(1600,1200,ImageFormat.JPEG,1);
                        // mImageReader22.setOnImageAvailableListener(
                        // mOnImageAvailableListener22, mHandler2);

                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(
                            SurfaceTexture surface, int width, int height) {

                    }

                    @Override
                    public boolean onSurfaceTextureDestroyed(
                            SurfaceTexture surface) {
                        Log.i(TAG,
                                "mPreviewView2.setSurfaceTextureListener stay onSurfaceTextureDestroyed");
                        return false;
                    }

                    @Override
                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                    }
                });

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
    public void onResume() {
        super.onResume();

        initLooper();
        mPreviewView1 = (TextureView) findViewById(R.id.sur_left);
        mPreviewView2 = (TextureView) findViewById(R.id.sur_right);
        startTest();
    }

    @Override
    public void onPause() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                closeCamera();
//            }
//        }).start();
        Log.d(TAG, "onPause start!");
        closeCamera();
        stopLooper();
        if(mDialog != null && mDialog.isShowing()){
            mDialog.cancel();
            mDialog = null;
        }
        if(mHandler != null){
            mHandler.removeMessages(MSG_DISMISS_DIALOG);
            mHandler.removeMessages(MSG_FAIL);
            mHandler.removeMessages(MSG_PASS);
            mHandler = null;
        }
        Log.d(TAG, "onPause end!");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mCameraSound != null) {
            mCameraSound.release();
            mCameraSound = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    private void initLooper() {
        Log.i(TAG, "initLooper in");
        mThreadHandler1 = new HandlerThread("calibration1");
        mThreadHandler1.start();
        mHandler1 = new Handler(mThreadHandler1.getLooper());
        mThreadHandler2 = new HandlerThread("calibration2");
        mThreadHandler2.start();
        mHandler2 = new Handler(mThreadHandler2.getLooper());
        Log.i(TAG, "initLooper out");
    }

    private void stopLooper() {

        try {
            mThreadHandler1.quit();
            mThreadHandler1.join();
            mThreadHandler1 = null;
            mHandler1 = null;

            mThreadHandler2.quit();
            mThreadHandler2.join();
            mThreadHandler2 = null;
            mHandler2 = null;
        } catch (Exception e) {
            Log.d(TAG, "StopLooper" + e.toString());
        }

    }

    private void closeCamera() {
        try {
            if (null != mSession2) {
                mSession2.close();
                mSession2 = null;
            }
            if (null != mCameraDevice2) {
                mCameraDevice2.close();
                mCameraDevice2 = null;
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        try {
            if (null != mSession1) {
                mSession1.close();
                mSession1 = null;
            }
            if (null != mCameraDevice1) {
                mCameraDevice1.close();
                mCameraDevice1 = null;
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback1 = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            Log.i(TAG, "CameraDevice.StateCallback1 onOpened in");
            mCameraDevice1 = camera;
            startPreview1(camera);
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                }
                cameraManager.openCamera(RIGHT_CAMERA_ID,
                        mCameraDeviceStateCallback2, mHandler2);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "CameraDevice.StateCallback1 onOpened out");
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            Log.i(TAG, "CameraDevice.StateCallback1 stay onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            Log.i(TAG, "CameraDevice.StateCallback1 stay onError");
        }
    };

    private CameraDevice.StateCallback mCameraDeviceStateCallback2 = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            Log.i(TAG, "CameraDevice.StateCallback2 onOpened in");
            mCameraDevice2 = camera;
            startPreview2(camera);
            Log.i(TAG, "CameraDevice.StateCallback2 onOpened out");
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            Log.i(TAG, "mCameraDeviceStateCallback2 stay onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            Log.i(TAG, "mCameraDeviceStateCallback2 stay onError");
        }
    };

    private void checkState(CaptureResult result) {
        switch (mState) {
        case STATE_PREVIEW:
            // NOTHING
            break;
        case STATE_WAITING_CAPTURE:
            int aeState = result.get(CaptureResult.CONTROL_AE_STATE);
            if (aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                // Toast.makeText(getApplicationContext(),"AE is OK",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "checkState start aeState="+aeState);
                mState = 3;
            }
            break;
        case 3: {
            // Toast.makeText(getApplicationContext(), "take a picture",
            // Toast.LENGTH_SHORT).show();

            mState = STATE_PREVIEW;
            Log.d(TAG, "take picture 1 is start");
            captureStillPicture();
            Log.d(TAG, "take picture 1 is finish");
            Log.d(TAG, "take picture 2 is start");
            captureStillPicture2();
            Log.d(TAG, "take picture 2 is finish");
        }
            break;

        }
    }

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback1 = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session,
                CaptureRequest request, TotalCaptureResult result) {
            mSession1 = session;
            saveOtpFile(result);
            checkState(result);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session,
                CaptureRequest request, CaptureResult partialResult) {
            mSession1 = session;
        }

    };

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback2 = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(CameraCaptureSession session,
                CaptureRequest request, CaptureResult partialResult) {
            mSession2 = session;

        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session,
                CaptureRequest request, TotalCaptureResult result) {
            mSession2 = session;
            //saveOtpFile(result);
        }
    };

    private void saveOtpFile(TotalCaptureResult result){
        try {
            int mDualOtpFlag = result
                    .get(SprdCaptureResult.ANDROID_SPRD_DUAL_OTP_FLAG);
            if(mDualOtpFlag != 1){
                return;
            }
            boolean hasDstResultBin = checkFileExists(DST_RESULT_BIN_FILE);
            if(hasDstResultBin){
                return;
            }
            Log.i(TAG, "saveOtpFile 2 mDualOtpFlag = "
                    + mDualOtpFlag);
            Log.d(TAG, "saveOtpFile 2 hasDstResultBin="+hasDstResultBin);
            byte[] otpByte = result
                    .get(SprdCaptureResult.CONTROL_OTP_VALUE);
            dumpData(otpByte, DST_RESULT_BIN_FILE);
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback1 = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                mSession1 = session;
                mSession1.setRepeatingRequest(mPreviewBuilder1.build(),
                        mSessionCaptureCallback1, mHandler1);
             // SPRD:Add for Feature:3DCalibration
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

    private CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback2 = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                mSession2 = session;
                mSession2.setRepeatingRequest(mPreviewBuilder2.build(),
                        mSessionCaptureCallback2, mHandler2);
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

    /*SPRD bug 759782 : Display RMS value*/
    private double mCurrentRMS = 0.0;
    private static boolean ENABLE_RMS = true;
    private AlertDialog mRmsDialog = null;
    private static boolean ENABLE_KEY_CAPTURE = true;
    private long mPreCurrentTime = 0l;

    private void showRmsDialog(String text){
        if(mRmsDialog != null && mRmsDialog.isShowing()){
            mRmsDialog.dismiss();
            mRmsDialog = null;
        }
        if(isFinishing() || isDestroyed()){
            Log.w(TAG, "showDialog activity isDestroyed!");
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(Camera3DCalibrationActivity.this)
        .setTitle("RMS")
        .setMessage(text)
        .setCancelable(false)
        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Camera3DCalibrationActivity.this.finish();
            }
        });
        mRmsDialog = dialog.create();
        mRmsDialog.show();
    }
    /*@}*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // pass
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mState = STATE_WAITING_CAPTURE;
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
                Log.d(TAG, "onKeyDown take picture 1 is start");
                captureWorker();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startPreview1(CameraDevice camera) {
        Log.i(TAG, "start preview 1");

        SurfaceTexture texture = mPreviewView1.getSurfaceTexture();
        texture.setDefaultBufferSize(1600, 1200);
        Surface surface = new Surface(texture);
        try {

            mPreviewBuilder1 = camera
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewBuilder1.set(
                    SprdCaptureRequest.ANDROID_SPRD_3DCALIBRATION_ENABLED, 1);
            Log.i(TAG, "start preview set ANDROID_SPRD_3DCALIBRATION_ENABLED");
        } catch (CameraAccessException e) {
            Log.i(TAG, e.toString());
        }
        mPreviewBuilder1.addTarget(surface);
        try {
            int [] capturesize = mPreviewBuilder1.get(SprdCaptureRequest.ANDROID_SPRD_3DCALIBRATION_CAPTURE_SIZE);
            Log.d(TAG, "startPreview1 capturesize="+capturesize);
            if(capturesize != null && capturesize.length == 2){
                mLeftCaptureWidth = capturesize[0];
                mLeftCaptureHeight = capturesize[1];
            }
            Log.d(TAG, "startPreview1 mLeftCaptureWidth="+mLeftCaptureWidth+",mLeftCaptureHeight="+mLeftCaptureHeight);
            mImageReader1 = ImageReader.newInstance(mLeftCaptureWidth, mLeftCaptureHeight,
                    ImageFormat.YUV_420_888, 1);
            mImageReader1.setOnImageAvailableListener(
                    mOnImageAvailableListener1, mHandler1);
            // camera.createCaptureSession(Arrays.asList(surface,mImageReader1.getSurface(),mImageReader11.getSurface()),
            // mCameraCaptureSessionStateCallback1, mHandler1);
            camera.createCaptureSession(
                    Arrays.asList(surface, mImageReader1.getSurface()),
                    mCameraCaptureSessionStateCallback1, mHandler1);

        } catch (CameraAccessException e) {
            Log.i(TAG, e.toString());
        }
    }

    private void startPreview2(CameraDevice camera) {
        Log.i(TAG, "start preview 2");

        SurfaceTexture texture = mPreviewView2.getSurfaceTexture();
        texture.setDefaultBufferSize(1600, 1200);
        Surface surface = new Surface(texture);
        try {
            mPreviewBuilder2 = camera
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewBuilder2.set(
                    SprdCaptureRequest.ANDROID_SPRD_3DCALIBRATION_ENABLED, 1);
        } catch (CameraAccessException e) {
            Log.i(TAG, e.toString());
        }

        mPreviewBuilder2.addTarget(surface);
        try {
            int [] capturesize = mPreviewBuilder2.get(SprdCaptureRequest.ANDROID_SPRD_3DCALIBRATION_CAPTURE_SIZE);
            Log.d(TAG, "startPreview2 capturesize="+capturesize);
            if(capturesize != null && capturesize.length == 2){
                mRightCaptureWidth = capturesize[0];
                mRightCaptureHeight = capturesize[1];
            }
            Log.d(TAG, "startPreview2 mRightCaptureWidth="+mRightCaptureWidth+",mRightCaptureHeight="+mRightCaptureHeight);
            mImageReader2 = ImageReader.newInstance(mRightCaptureWidth, mRightCaptureHeight,
                    ImageFormat.YUV_420_888, 1);
            mImageReader2.setOnImageAvailableListener(
                    mOnImageAvailableListener2, mHandler2);
            // camera.createCaptureSession(Arrays.asList(surface,mImageReader2.getSurface(),mImageReader22.getSurface()),
            // mCameraCaptureSessionStateCallback2, mHandler2);
            camera.createCaptureSession(
                    Arrays.asList(surface, mImageReader2.getSurface()),
                    mCameraCaptureSessionStateCallback2, mHandler2);

        } catch (CameraAccessException e) {
            Log.i(TAG, e.toString());
        }
    }

    private void captureStillPicture() {
        try {
            final CaptureRequest.Builder captureBuilder = mCameraDevice1
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader1.getSurface());
            // captureBuilder.addTarget(mImageReader11.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                        CaptureRequest request, TotalCaptureResult result) {
                    // Toast.makeText(getApplicationContext(),
                    // "save pic success1", Toast.LENGTH_SHORT).show();
                    session.close();
                    session = null;
                    picReady1 = true;
                    if (picReady2 == true) {
                        startCalibration();
                    }
                }
            };
            // mSession1.stopRepeating();
            Log.d(TAG, "capture  in camera 1 ");
            mSession1.capture(captureBuilder.build(), CaptureCallback,
                    mHandler1);
            capture1IsSend = true;
        } catch (Exception e) {
            Log.d(TAG, "capture a picture1 fail" + e.toString());
        }
    }

    private boolean capture1IsSend = false;

    private void captureStillPicture2() {
        try {
            final CaptureRequest.Builder captureBuilder = mCameraDevice2
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader2.getSurface());
            // captureBuilder.addTarget(mImageReader22.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                        CaptureRequest request, TotalCaptureResult result) {
                    // Toast.makeText(getApplicationContext(),
                    // "save pic success2", Toast.LENGTH_SHORT).show();
                    session.close();
                    session = null;
                    picReady2 = true;
                    if (picReady1 == true) {
                        startCalibration();
                    }
                }

            };
            // mSession2.stopRepeating();
            Log.d(TAG, "capture  in camera 2 ");

            while (!capture1IsSend) {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {

                }
            }
            mSession2.capture(captureBuilder.build(), CaptureCallback,
                    mHandler2);
            // capture1IsSend = false;
        } catch (Exception e) {
            Log.d(TAG, "capture a picture2 fail" + e.toString());
        }
    }

    private class ImageSaver implements Runnable {
        private final Image mImage;
        private final File mFile;

        public ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            FileOutputStream output = null;
            try {
                if (!mFile.getParentFile().exists()) {
                    mFile.getParentFile().mkdirs();
                }
                if (!mFile.exists()) {
                    mFile.createNewFile();
                }
                Log.d(TAG, "ImageSaver mFile="+mFile.getPath());
                output = new FileOutputStream(mFile);
                ImageUtils.writeYuvImage(mImage,output);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case 1: {
            if (data == null) {
                Toast.makeText(getApplicationContext(), R.string.text_fail,
                        Toast.LENGTH_SHORT).show();
                storeRusult(false);
                finish();
                break;
            }
            String result = data.getExtras().getString("result");
            Log.d(TAG, "ang.li return result:" + result);
            if (result.equals("fail")) {
                String tip = data.getExtras().getString("tip");
                Log.d(TAG, "ang.li return tip:" + tip);
                Toast.makeText(getApplicationContext(),
                        R.string.text_fail + "fail info:" + tip,
                        Toast.LENGTH_SHORT).show();
                storeRusult(false);
                finish();
            } else {
                boolean flag = FileUtils.copyFile(YUV_TMP_PATH
                        + "calibration.data", SPRD_3D_CALIBRATION_PATH);
                if (flag) {
                    try {
                        Runtime.getRuntime().exec(
                                "chmod 777 " + SPRD_3D_CALIBRATION_PATH
                                        + "calibration.data");
                    } catch (Exception e) {
                        Log.d(TAG, "chmod " + SPRD_3D_CALIBRATION_PATH
                                + "calibration.data" + " fail:" + e.toString());
                    }
                    Toast.makeText(getApplicationContext(), R.string.text_pass,
                            Toast.LENGTH_SHORT).show();
                    storeRusult(true);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.text_fail,
                            Toast.LENGTH_SHORT).show();
                    storeRusult(false);
                    finish();
                }
            }
            break;
        }
        default:
            Toast.makeText(getApplicationContext(), R.string.text_fail,
                    Toast.LENGTH_SHORT).show();
            storeRusult(false);
            finish();
            break;
        }
    }

    private static final String SRC_RESULT_BIN_FILE = "/data/misc/cameraserver/dualcamera.bin";
    private static final String DST_RESULT_BIN_FILE = "/productinfo/dualcamera.bin";
    private static final String DST_RESULT_BIN_DIR = "productinfo/";
    private boolean checkFileExists(String filePath){
        File file = new File(filePath);
        boolean exists = file.exists();
        return exists;
    }

    private void copyResultBin(){
      try {
          boolean res = FileUtils.copyFile(SRC_RESULT_BIN_FILE, DST_RESULT_BIN_DIR);
          Log.d(TAG, "copyResultBin res="+res);
      } catch (Exception e) {
          // TODO: handle exception
          e.printStackTrace();
      }
    }

    private void startCalibration() {
        if (mCameraSound != null) {
            mCameraSound.play(MediaActionSound.SHUTTER_CLICK);
        }
        //copyResultBin();
        //1.Check result bin
        boolean hasDstResultBin = checkFileExists(DST_RESULT_BIN_FILE);
        Log.d(TAG, "startCalibration hasDstResultBin="+hasDstResultBin);
        hasDstResultBin = true;
        if(!hasDstResultBin){
            Log.w(TAG, "No result bin!");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Toast.makeText(Camera3DCalibrationActivity.this,
                            "Camera verifying FAIL!\nNo result bin!", Toast.LENGTH_SHORT).show();
                    storeRusult(false);
                    finish();
                }
            });
            return;
        }
        //2.do verify work
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                showDialog("Camera verifying...");
            }
        });

        Message message = Message.obtain();
        message.what = MSG_DISMISS_DIALOG;
        mHandler.sendMessageDelayed(message, 120000);

        final int result = NativeCameraCalibration.native_dualCameraVerfication(
                LEFT_YUV_PATH, RIGHT_YUV_PATH,DST_RESULT_BIN_FILE);
        checkResult(result);
        Log.d(TAG, "startCalibration result=" + result);
        try {
            final StringBuffer text = new StringBuffer();
            double rms = NativeCameraCalibration.native_getCameraVerficationRMS();
            Log.d(TAG, "startCalibration rms =" + rms);
            text.append(rms);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "startCalibration text.toString()=" + text.toString());
                    showRmsDialog(text.toString());
                }
            });
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private void checkResult(int result){
        Log.d(TAG, "checkResult result=" + result);
        if(result == 0){
            sendMessgae(true, result);
        }else{
            sendMessgae(false, result);
        }
    }

    private static final int MSG_PASS = 1;
    private static final int MSG_FAIL = 2;
    private static final int MSG_DISMISS_DIALOG = 3;

    private void sendMessgae(boolean pass,int result){
        Message message = Message.obtain();
        message.what = pass ? MSG_PASS : MSG_FAIL;
        message.arg1 = result;
        mHandler.sendMessage(message);
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case MSG_PASS:
                int result = msg.arg1;
                Toast.makeText(Camera3DCalibrationActivity.this,
                        "Camera verifying PASS!\n result=" + result, Toast.LENGTH_SHORT).show();
                storeRusult(true);
                //finish();
                break;
            case MSG_FAIL:
                result = msg.arg1;
                Toast.makeText(Camera3DCalibrationActivity.this,
                        "Camera verifying FAIL!\n result=" + result, Toast.LENGTH_SHORT).show();
                storeRusult(false);
                //finish();
                break;
            case MSG_DISMISS_DIALOG:
                if(mDialog != null && mDialog.isShowing()){
                    mDialog.cancel();
                }
                break;
            default:
                break;
            }
        };
    };

    private AlertDialog mDialog = null;
    private void showDialog(String text){
        if(mDialog != null && mDialog.isShowing()){
            mDialog.dismiss();
            mDialog = null;
        }
        if(isFinishing() || isDestroyed()){
            Log.w(TAG, "showDialog activity isDestroyed!");
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(Camera3DCalibrationActivity.this)
        //.setTitle("DualCamera Verifying")
        .setMessage(text)
        .setCancelable(false);
        mDialog = dialog.create();
        mDialog.show();
    }

    private void dumpData(byte[] data, String path) {
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
}
