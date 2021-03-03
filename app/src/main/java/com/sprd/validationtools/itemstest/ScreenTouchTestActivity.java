package com.sprd.validationtools.itemstest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.TouchTestGridView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;

import javax.security.auth.callback.Callback;

public class ScreenTouchTestActivity extends BaseActivity {
    private static final String TAG = "ScreenTouchTestActivity";
    private Context mContext;
    private int red;
    private int count;
    private ArrayList<Integer> countList = new ArrayList<>();
    private int rowCount;
    private int columnCount;
    private Timer mPlayTimer;
    private int time = 0;
    private MyHandle myHandle;
    private View onclickV;

    public static final int TIME_IN_FRAME = 50;
    Paint mPaint = null;
    Paint mTextPaint = null;
    SurfaceHolder mSurfaceHolder = null;
    boolean mRunning = false;
    Canvas mCanvas = null;
    private Path mPath;
    private float mPosX, mPosY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.removeButton();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_test);
        Settings.System.putInt(getContentResolver(),
                Settings.System.POINTER_LOCATION, 1);
//        new MyView(this)
//        super.removeButton();
//        init();
    }

    private void init() {
        mContext = this;
        myHandle = new MyHandle(this);
        red = getResources().getColor(R.color.colorRed);//指定一种颜色
        TouchTestGridView layout = findViewById(R.id.gv_touch);
        rowCount = layout.getRowCount();
        columnCount = layout.getColumnCount();
        Log.d(TAG, "onCreate: rowCount " + rowCount + " column " + columnCount);
        //GridLayout的自动填充
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                TextView textview = new TextView(this);
//                textview.setOnTouchListener(this); //每个textview都监听触摸事件
                textview.setOnClickListener(this);
                textview.setTag(getIndex(i, j));
                GridLayout.Spec rowSpec = GridLayout.spec(i, 1.0f); //行坐标和比重rowweight,用float表示的
                GridLayout.Spec columnSpec = GridLayout.spec(j, 1.0f);//列坐标和columnweight
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
                layout.addView(textview, params);
            }
        }
        count = layout.getChildCount();
//        startPlayVoice();
        myHandle.sendEmptyMessageDelayed(1, 20000);
    }

    /**
     * 获取当前View的index
     *
     * @param i 行数
     * @param j 列数
     * @return index
     */
    private int getIndex(int i, int j) {
        int a = i * columnCount;
        int b = j + 1;
        return a + b;
    }

    private void checkResult() {
        if (countList.size() >= count) {
            if (myHandle != null) {
                myHandle.removeCallbacksAndMessages(null);
                myHandle = null;
            }
            Toast.makeText(this, R.string.text_pass, Toast.LENGTH_SHORT).show();
            finish();
            storeRusult(true);
        }
    }

    private static class MyHandle extends Handler {
        private final ScreenTouchTestActivity mTouchTestActivity;

        public MyHandle(ScreenTouchTestActivity activity) {
            WeakReference<ScreenTouchTestActivity> weakReference = new WeakReference<>(activity);
            mTouchTestActivity = weakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mTouchTestActivity.setResult();
            }
            if (msg.what == 2) {
                mTouchTestActivity.updateView();
            }
        }
    }

    private void setResult() {
        if (countList.size() >= count) {
            Toast.makeText(mContext, R.string.text_pass, Toast.LENGTH_SHORT).show();
            finish();
            storeRusult(true);
        } else {
            Toast.makeText(mContext, R.string.text_fail, Toast.LENGTH_SHORT).show();
            finish();
            storeRusult(false);
        }
    }

    /*    @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "onTouch: " + v.getTag());
                    v.setEnabled(false);
                    countList.add(1);
                    sendMessage(v);
    //                checkResult();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        }*/

/*    @Override
    public void onClick(View v) {
        Log.d(TAG, "onTouch: " + v.getTag());
        onclickV = v;
        v.setEnabled(false);
        countList.add(1);
//        myHandle.sendEmptyMessage(2);
    }*/

    private void updateView() {
        onclickV.setBackgroundColor(red);
        checkResult();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Settings.System.putInt(getContentResolver(),
                Settings.System.POINTER_LOCATION, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayTimer != null) {
            mPlayTimer.purge();
            mPlayTimer.cancel();
            mPlayTimer = null;
        }
        countList = null;
        mContext = null;
        if (myHandle != null) {
            myHandle.removeCallbacksAndMessages(null);
            myHandle = null;
        }
    }

    /*public class MyView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
        public static final int TIME_IN_FRAME = 50;
        Paint mPaint = null;
        Paint mTextPaint = null;
        SurfaceHolder mSurfaceHolder = null;
        boolean mRunning = false;
        Canvas mCanvas = null;
        private Path mPath;
        private float mPosX, mPosY;

        public MyView(Context context) {
            super(context);
            this.setFocusable(true);
            this.setFocusableInTouchMode(true);
            mSurfaceHolder = this.getHolder();
            mSurfaceHolder.addCallback(this);
            mCanvas = new Canvas();
            mPaint = new Paint();
            mPaint.setColor(Color.RED);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(6);
            mPath = new Path();
            mTextPaint = new Paint();
            mTextPaint.setColor(Color.BLACK);
            mTextPaint.setTextSize(22);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            float x = event.getX();
            float y = event.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mPath.moveTo(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    mPath.quadTo(mPosX, mPosY, x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    //mPath.reset();
                    break;
            }
            //记录当前触摸点得当前得坐标
            mPosX = x;
            mPosY = y;
            return true;
        }

        private void drawLine() {
            mCanvas.drawColor(Color.WHITE);
            //绘制曲线
            mCanvas.drawPath(mPath, mPaint);
            mCanvas.drawText("当前触笔X：" + mPosX, 20, 60, mTextPaint);
            mCanvas.drawText("当前触笔Y:" + mPosY, 20, 80, mTextPaint);
        }

        // TODO Auto-generated method stub
        public void run() {
            while (mRunning) {
                long startTime = System.currentTimeMillis();
                synchronized (mSurfaceHolder) {
                    mCanvas = mSurfaceHolder.lockCanvas();
                    drawLine();
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
                long endTime = System.currentTimeMillis();
                int diffTime = (int) (endTime - startTime);
                while (diffTime <= TIME_IN_FRAME) {
                    diffTime = (int) (System.currentTimeMillis() - startTime);
                    Thread.yield();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            // TODO Auto-generated method stub
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mRunning = true;
            new Thread(this).start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            mRunning = false;
        }

    }*/

}
