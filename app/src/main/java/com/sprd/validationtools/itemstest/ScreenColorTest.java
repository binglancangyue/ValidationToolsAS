
package com.sprd.validationtools.itemstest;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

public class ScreenColorTest extends BaseActivity {
    private String TAG = "ScreenColorTest";
    TextView mContent;
    int mIndex = 0, mCount = 0;
    private Handler mUiHandler = new Handler();

    private Runnable mRunnable;
    private String text;
    private static final int[] COLOR_ARRAY = new int[]{
            Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW
    };
    private static final int[] COLOR_NAME = new int[]{
            R.string.color_white, R.string.color_red, R.string.color_green,
            R.string.color_blue, R.string.color_yellow
    };
    private static final int TIMES = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.removeButton();
        super.onCreate(savedInstanceState);
/*        mContent = new TextView(this);
        mRunnable = new Runnable() {
            public void run() {
                mContent.setBackgroundColor(COLOR_ARRAY[mIndex]);
                mIndex++;
                mCount++;
                setBackground();
            }
        };
        setBackground();
        setTitle(R.string.lcd_test);
        setContentView(mContent);
        mContent.setGravity(Gravity.CENTER);
        mContent.setTextSize(35);*/
        setContentView(R.layout.activity_screen_color_test);
        mContent = findViewById(R.id.tv_color);
        text = getResources().getString(R.string.please_click_screen);
        updateColor();
        mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUiHandler.post(mRunnable);
            }
        });
        mRunnable = new Runnable() {
            public void run() {
                mIndex++;
                if (mIndex > TIMES) {
                    mIndex = 0;
                }
                updateColor();
            }
        };
    }

    private void updateColor() {
        String str = text + " " + mIndex + " : " + getResources().getString(COLOR_NAME[mIndex]);
        mContent.setText(str);
        mContent.setBackgroundColor(COLOR_ARRAY[mIndex]);
    }

    private void setBackground() {
        if (mIndex >= COLOR_ARRAY.length) {
            //showResultDialog(getString(R.string.lcd_max));
            super.createButton(true);
            super.createButton(false);
            return;
        }
        mContent.setBackgroundColor(COLOR_ARRAY[mIndex]);

        /* SPRD Bug 771296:LCD screen test, white screen continue 3 seconds. @{ */
        if (Const.isBoardISharkL210c10()) {
            if (mIndex == 0) {
                mUiHandler.postDelayed(mRunnable, 3000);
            } else {
                mUiHandler.postDelayed(mRunnable, 1000);
            }
        } else {
            mUiHandler.postDelayed(mRunnable, 600);
        }
        /* @} */
    }

    @Override
    public void onDestroy() {
        mUiHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }
}
