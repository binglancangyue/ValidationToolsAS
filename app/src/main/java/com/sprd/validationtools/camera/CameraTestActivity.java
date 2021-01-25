package com.sprd.validationtools.camera;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.Debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.ShellUtils;
import com.sprd.validationtools.sqlite.EngSqlite;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.widget.Toast;
import android.os.EnvironmentEx;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

public class CameraTestActivity extends BaseActivity implements TextureView.SurfaceTextureListener {
    private static final String TAG = "CameraTestActivity";
    private CameraScreenNailProxy mCameraScreenNailProxy;
    public static final int CAMERA_START = 0;
    private int isFullTest = 0;
    private int fullTestActivityId;
    private Camera mCamera = null;
    private int mCameraId = 1;
    private SurfaceView mSurfaceView = null;
    private TextureView mTextureView = null;
    private SurfaceTexture mSurfaceTexture = null;
    private PreviewFrameLayout mPreviewFrameLayout;
    private TextView mLightMsg = null;
    private SurfaceHolder holder = null;
    private static final int PREVIEW_WIDTH = 320;
    private static final int PREVIEW_HEIGHT = 240;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private ComboPreferences mPreferences;
    private boolean mFlag = false;
    private static final int BACK_CAMERA = 0;
    private static final int FRONT_CAMERA = 1;
    private final String testCaseName = "Camera test";
    private int groupId;
    private boolean isSurportCameraFlash = false;
    private static final int CAMERA_FLASH_LIGHT = 0;
    private static final int CAMERA_SECOND_FLASH_LIGHT = 1;
    private static final int CAMERA_FLASH_LIGHT_CLOSE = 2;
    private static final int CAMERA_SECOND_FLASH_LIGHT_CLOSE = 3;
    private static final int CAMERA_TIME_OUT = 4;
    private boolean mIsPause = false;
    private static final boolean IS_SUPPORT_FLASHLIGHT = true;
//    private boolean isWhaleSupport = Const.isWhale2Support() || Const.isSharkL2Support() || Const.isIWhale2Support() || Const.isISharkL2Support()
//            || Const.isSharkLESupport();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CAMERA_FLASH_LIGHT:
                openFlashLight(CAMERA_FLASH_LIGHT);
                break;
            case CAMERA_SECOND_FLASH_LIGHT:
                openFlashLight(CAMERA_SECOND_FLASH_LIGHT);
                break;
            case CAMERA_FLASH_LIGHT_CLOSE:
                closeFlashLight(CAMERA_FLASH_LIGHT);
                break;
            case CAMERA_SECOND_FLASH_LIGHT_CLOSE:
                closeFlashLight(CAMERA_SECOND_FLASH_LIGHT);
                break;
            case CAMERA_TIME_OUT:
                CameraTestActivity.this.storeRusult(false);
                CameraTestActivity.this.finish();
                break;
            }
            super.handleMessage(msg);
        }
    };

    private Runnable mR = new Runnable() {
        public void run() {
            showResultDialog(getString(R.string.camera_test_result_check));
        }
    };

    private Runnable mTimeOut = new Runnable() {
        public void run() {
        }
    };

    private Button mTakePhotoBtn;
    protected class CameraScreenNailProxy{
        private static final String TAG = "CameraScreenNailProxy";

        public static final int KEY_SIZE_PICTURE = 0;
        public static final int KEY_SIZE_PREVIEW = 1;

        private Tuple<Integer, Integer> mScreenSize;
        protected CameraScreenNailProxy(){
            initializeScreenSize();
        }

        private void initializeScreenSize(){
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            mScreenSize = new Tuple<Integer, Integer>(
                metrics.widthPixels, metrics.heightPixels);
            Log.d(TAG,
               String.format("screen size = { %dx%d }",
                  new Object[] { mScreenSize.first, mScreenSize.second }));
        }
        protected Tuple<Integer, Integer>
            getOptimalSize(int key, ComboPreferences pref){

            Tuple<Integer, Integer> result = null;
            Size size = null;
            boolean b_full_screen = getScreenState(pref);
            int orientation = getOrientation();
            int
                width = mScreenSize.first,
                height = mScreenSize.second;
            Camera.Parameters mParameters = null;
            mParameters = mCamera.getParameters();

            if (KEY_SIZE_PICTURE == key) {
                size = mParameters.getPictureSize();
                width = size.width;
                height = size.height;
                result = Util.getOptimalSize(
                    mScreenSize.first, mScreenSize.second, width, height, b_full_screen);
                width = result.first;
                height = result.second;
                if (orientation % 180 == 0) {
                    int tmp = width;
                    width = height;
                    height = tmp;
                }
            }

            if (KEY_SIZE_PREVIEW == key) {
                size = mParameters.getPreviewSize();
                width = size.width;
                height = size.height;
                result = Util.getOptimalSize(
                    mScreenSize.first, mScreenSize.second, width, height, b_full_screen);
                width = result.first;
                height = result.second;
                if (orientation % 180 == 0){
                    int tmp = width;
                    width = height;
                    height = tmp;
                }
            }

            result = new Tuple<Integer, Integer>(width, height);
            Log.d(TAG,
                String.format("get optimal size: key = %d, is_full_screen = %b, size = { %dx%d }",
                    new Object[] { key, b_full_screen, result.first, result.second }));
            return result;
        }

        private int getOrientation() {
            return getCameraDisplayOrientation(mCameraId, mCamera);
        }
    }
    protected boolean getScreenState(ComboPreferences pref) {
        boolean result = false;
        if (pref != null) {
            String str_on = getString(R.string.pref_entry_value_on);
            String str_val = pref.getString("pref_camera_video_full_screen_key", null);
            result = (str_val != null && str_val.equals(str_on));
        }
        return result;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int isFullTest = getIntent().getIntExtra("isFullTest", 0);
        int fullTestActivityId = getIntent().getIntExtra("fullTestActivityId", 0);
        setIsFullTest(isFullTest, ++fullTestActivityId);
        groupId = this.getIntent().getIntExtra("groupId", 0);
        Log.d(TAG, "groupId" + groupId);
        setTitle(getResources().getText(R.string.back_camera_title_text));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.back_camera_result);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setTitle(R.string.camera_test_title);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mCameraScreenNailProxy = new CameraScreenNailProxy();
        mPreviewFrameLayout = (PreviewFrameLayout) findViewById(R.id.frame);
        /*SPRD: fix bug349132 change the SurfaceView to TextureView @{*/
//      mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mTextureView = (TextureView) findViewById(R.id.surfaceView);
        mTextureView.setSurfaceTextureListener(this);
        /* @}*/
        mLightMsg = (TextView)findViewById(R.id.light_msg);
        /*BEGIN 2016/04/13 zhijie.yang BUG535005 mmi add take photes of camera test */
        mTakePhotoBtn = (Button) findViewById(R.id.start_take_picture);
        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCamera != null) {
                    mTakePhotoBtn.setEnabled(false);
                    mCamera.takePicture(shutterCallback, null, mPicture);

                } else {
                    Log.d(TAG, "mCamera is null.");
                    Toast.makeText(getApplicationContext(),
                            CameraTestActivity.this.getString(R.string.back_camera_fail_tips), Toast.LENGTH_SHORT)
                            .show();
                    storeRusult(false);
                    finish();
                }
            }
        });;
        Log.d(TAG, "oncreate IS_SUPPORT_FLASHLIGHT="+IS_SUPPORT_FLASHLIGHT);
        if(IS_SUPPORT_FLASHLIGHT) {
            startDoubleFlashLightTest();
        }
        /* @} */
        mHandler.sendEmptyMessageDelayed(CAMERA_TIME_OUT, 120000);
        startBackgroundThread();
    }
    
    private Handler mCameraHandler;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mCameraHandler = new Handler(mBackgroundThread.getLooper());
        mCameraHandler.postDelayed(mTimeOut, 120000);
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        try {
            mBackgroundThread.quitSafely();
            mBackgroundThread.join();
            mBackgroundThread = null;
            mCameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* SPRD Bug 744113:adjust the test plan of the flashlight. @{ */
    private void openFlashLightByCameraId(int cameraId) {
        if(mIsPause) return;
        Log.d(TAG, "openFlashLightByCameraId cameraId="+cameraId);
        if(cameraId == CameraCharacteristics.LENS_FACING_BACK) {
            writeFlashDev("0x20");
        } else {
            writeFlashDev("0x10");
        }
    }
    /* @} */

    private void startDoubleFlashLightTest() {
        mHandler.sendEmptyMessageDelayed(CAMERA_FLASH_LIGHT, 300);
    }

    private void openFlashLight(int type) {
        if(mIsPause) return;
        if(type == CAMERA_FLASH_LIGHT) {
            writeFlashDev("0x10");
            mHandler.sendEmptyMessageDelayed(CAMERA_FLASH_LIGHT_CLOSE, 1000);
        } else {
            writeFlashDev("0x20");
            mHandler.sendEmptyMessageDelayed(CAMERA_SECOND_FLASH_LIGHT_CLOSE, 1000);
        }
    }

    private void closeFlashLight(int type) {
        if(type == CAMERA_FLASH_LIGHT) {
            writeFlashDev("0x11");
            mHandler.sendEmptyMessageDelayed(CAMERA_SECOND_FLASH_LIGHT, 1000);
        } else {
            writeFlashDev("0x21");
            mHandler.sendEmptyMessageDelayed(CAMERA_FLASH_LIGHT, 1000);
        }
    }

    private void writeFlashDev(String cmd) {
        ShellUtils.writeFile(Const.CAMERA_FLASH, cmd);
    }

    private ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
        }
    };

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            String filePath = EnvironmentEx.getInternalStoragePath()
                    .getAbsolutePath() + "/mmi/" + "backphoto.jpg" ;
            File pictureFile = new File(filePath);
            try {
                    if (!pictureFile.getParentFile().exists()) {
                        pictureFile.getParentFile().mkdirs();
                    }
                    if (!pictureFile.exists()) {
                        pictureFile.createNewFile();
                    }
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                android.media.MediaScannerConnection.scanFile(getApplicationContext(), new String[]{ pictureFile.getAbsolutePath() }, null, null);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            //Bug 614121
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    if(mCamera != null) {
                        mCamera.startPreview();
                        mTakePhotoBtn.setEnabled(true);
                    }
                }
            }, 1000);
        }
    };
    /*END 2016/04/13 zhijie.yang BUG535005 mmi add take photes of camera test */

    private boolean isCameraFlashEnable() {
        PackageManager pm = (PackageManager) this.getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        for (FeatureInfo f : features) {
            if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCameraFrontEnable() {
        PackageManager pm = (PackageManager) this.getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        for (FeatureInfo f : features) {
            if (PackageManager.FEATURE_CAMERA_FRONT.equals(f.name)) {
                return true;
            }
        }
        return false;
    }
    private void startCamera() {
        if (mFlag) {
            Log.e(TAG, "stop & close");
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mFlag = false;
            }
        }
        try {
            Log.e(TAG, "open");
            mCamera = Camera.open(mCameraId);
        } catch (RuntimeException e) {
            Log.e(TAG, "fail to open camera");
            e.printStackTrace();
            mCamera = null;
        }
        if (mCamera != null) {
            setCameraDisplayOrientation(mCameraId, mCamera);
            Camera.Parameters parameters = null;
            parameters = mCamera.getParameters();
            Size size = parameters.getPictureSize();
            List<Size> sizes = parameters.getSupportedPreviewSizes();
            Size optimalSize = getOptimalPreviewSize(this, sizes, (double) size.width / size.height);
            Size original = parameters.getPreviewSize();
            if (!original.equals(optimalSize)) {
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            }
            Log.v(TAG, "Preview size is " + optimalSize.width + "x" + optimalSize.height);
            mPreviewWidth = optimalSize.width;
            mPreviewHeight = optimalSize.height;
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.set("orientation", "portrait");
//            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            //Maybe cause exception while setFocusMode not support continuous-picture
            //parameters.setFocusMode("continuous-picture");
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            boolean mFocusContinuousSupported = isSupported(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, supportedFocusModes);
            if (mFocusContinuousSupported) {
                parameters.setFocusMode("continuous-picture");
            }
            mCamera.setParameters(parameters);
            try {
                /*SPRD: fix bug349132 change the SurfaceView to TextureView @{*/
//              mCamera.setPreviewDisplay(holder);
                mCamera.setPreviewTexture(mSurfaceTexture);
                /* @}*/
                Log.e(TAG, "start preview");
                mCamera.startPreview();
                mFlag = true;
                initializeCameraOpenAfter();
            } catch (Exception e) {
                mCamera.release();
            }
        }
    }

    private boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }

    public static Size getOptimalPreviewSize(Activity currentActivity,
            List<Size> sizes, double targetRatio) {
        final double ASPECT_TOLERANCE = 0.001;
        if (sizes == null) return null;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        Display display = currentActivity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int targetHeight = Math.min(point.x, point.y);
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
                break;
            }
        }
        if (optimalSize == null) {
            Log.w(TAG, "No preview size match the aspect ratio");
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    private void failureIntent() {
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
            int height) {
        Log.d(TAG, "onSurfaceTextureAvailable");
        mSurfaceTexture = surface;
        startCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
            int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    /*SPRD: fix bug349132 change the SurfaceView to TextureView @{*/
    /*public void surfaceChanged(SurfaceHolder sholder, int format, int width, int height) {
    }
    public void surfaceCreated(SurfaceHolder sholder) {
        startCamera();
    }
    public void surfaceDestroyed(SurfaceHolder sholder) {
    }*/
    /* @}*/

    @Override
    protected void onResume() {
        super.onResume();
        mIsPause = false;
        /*SPRD: fix bug349132 change the SurfaceView to TextureView @{*/
        /*holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);*/
        /* @}*/
        if(mFlag) {
        	mCameraHandler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					 startCamera();
				}
			});
            //startCamera();
        }
    }

    private void cameraNumber(){
        if(mCameraId == BACK_CAMERA){
            mCameraId = FRONT_CAMERA;
        if(mLightMsg!=null){
            mLightMsg.setVisibility(View.GONE);
                }
        }else{
            mCameraId = BACK_CAMERA;
         if(mLightMsg!=null && isSurportCameraFlash){
             mLightMsg.setVisibility(View.VISIBLE);
            }
       }
       Log.d("donglin", "switched camera id = " + mCameraId);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mIsPause = true;
        try {
            if (mCamera != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setIsFullTest(int isFullTest, int fullTestActivityId){
        this.isFullTest = isFullTest;
        this.fullTestActivityId = fullTestActivityId;
    }

    public static int getDisplayRotation() {
        return 0;
    }
    public static void setCameraDisplayOrientation(
            int cameraId, Camera camera) {
        int result = getCameraDisplayOrientation(cameraId,camera);
        camera.setDisplayOrientation(result);
    }
    public static int getCameraDisplayOrientation(
            int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = getDisplayRotation();
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
          return result;
    }

    private void initializeCameraOpenAfter() {
        // SPRD:Fixbug454827,The preview picture of take photo has some defective.
        Tuple<Integer, Integer> size =
            mCameraScreenNailProxy.getOptimalSize(
                CameraScreenNailProxy.KEY_SIZE_PREVIEW, mPreferences);
        if (mPreviewFrameLayout != null) {
            mPreviewFrameLayout.setAspectRatio((double) size.first / (double) size.second, true);
        }
    }

    @Override
    public void onDestroy(){
        mHandler.removeCallbacks(mR);
        mHandler.removeMessages(CAMERA_TIME_OUT);
        if(IS_SUPPORT_FLASHLIGHT) {
            mHandler.removeMessages(CAMERA_FLASH_LIGHT);
            mHandler.removeMessages(CAMERA_SECOND_FLASH_LIGHT);
            mHandler.removeMessages(CAMERA_FLASH_LIGHT_CLOSE);
            mHandler.removeMessages(CAMERA_SECOND_FLASH_LIGHT_CLOSE);
            /*SPRD bug 753892 : Flashlight maybe not close*/
            //writeFlashDev("0x11");
            //writeFlashDev("0x21");
            /*@}*/
        }
        /* SPRD bug 753892 : Flashlight maybe not close */
        try {
            writeFlashDev("0x11");
            writeFlashDev("0x21");
        } catch (Throwable e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        /* @} */
        stopBackgroundThread();
        super.onDestroy();
    }
}
