package com.sprd.validationtools.itemstest;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

public class ScreenTouchTestActivity extends BaseActivity implements View.OnClickListener {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_test);
        super.removeButton();
        init();
    }

    private void init() {
        mContext = this;
        myHandle = new MyHandle(this);
        red = getResources().getColor(R.color.colorRed);//指定一种颜色
        TouchTestGridView layout = findViewById(R.id.gv_touch);
        rowCount = layout.getRowCount();
        columnCount = layout.getColumnCount();
        Log.d(TAG, "onCreate: rowCount " + rowCount + " column " + columnCount);
        /*GridLayout的自动填充*/
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

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onTouch: " + v.getTag());
        onclickV = v;
        v.setEnabled(false);
        countList.add(1);
        myHandle.sendEmptyMessage(2);
//        v.setBackgroundColor(red);
    }

    private void updateView() {
        onclickV.setBackgroundColor(red);
        checkResult();
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

}
