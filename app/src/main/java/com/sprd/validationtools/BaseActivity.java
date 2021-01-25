
package com.sprd.validationtools;

import android.graphics.Color;
import com.sprd.validationtools.sqlite.EngSqlite;
import com.sprd.validationtools.utils.WcndUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BaseActivity extends Activity implements OnClickListener{
    private static final String TAG = "BaseActivity";
    private AlertDialog mResultDialog;
    private String mTestname = null;
    private EngSqlite mEngSqlite;
    public static boolean shouldCanceled = true;

    protected Button mPassButton;
    protected Button mFailButton;
    private TextView textView;
    private static final int TEXT_SIZE = 30;
    protected boolean canPass = true;
    protected WindowManager mWindowManager;
    protected long time;
    private View addView;
    private View addHeadView;
    private PhaseCheckParse mPhaseCheckParse = null;
    //private static final String STATION_MMIT1_VALUE = "MMI";
    //private static final String STATION_MMIT2_VALUE = "MMI";
    private static final String STATION_MMIT_VALUE = "MMI";
    private static final String STATION_AGING_VALUE = "AGING";
    private static final boolean SUPPORT_WRITE_STATION = Const.isBoardISharkL210c10();
    private static final int MISCDATA_USERSETION_OFFSET_BASE = 768*1024;
    private static final int MISCDATA_USERSETION_OFFSET_AGING = MISCDATA_USERSETION_OFFSET_BASE+44;
    private static final int MISCDATA_USERSETION_OFFSET_RING_TUNE = MISCDATA_USERSETION_OFFSET_BASE+74;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        super.onCreate(savedInstanceState);

        mTestname = this.getIntent().getStringExtra("testname");
        time = System.currentTimeMillis();

        mEngSqlite = EngSqlite.getInstance(this);

        mWindowManager = getWindowManager();
//        createButton(true);
//        createButton(false);
        createHeadLayout();
        createBtnLayout();
        Log.d(TAG, "onCreate SUPPORT_WRITE_STATION="+SUPPORT_WRITE_STATION);
        if(SUPPORT_WRITE_STATION){
            mPhaseCheckParse = new PhaseCheckParse();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*SPRD bug 782439:Dump cp log.*/
        try {
            AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    // TODO Auto-generated method stub
                    if(mTestname != null){
                        if(mTestname.equals("Wifi test") || mTestname.equals("Bluetooth test") || mTestname.equals("FM test")){
                            WcndUtils.sendWcndCmdFlushLog();
                            WcndUtils.sendSlogModemCmdFlushLog();
                        }
                    }
                    return null;
                }
            };
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null,null,null);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        if(mTestname != null) {
            Log.d("APK_MMI", "*********** " + mTestname + " Time: " + (System.currentTimeMillis() - time) / 1000 + "s ***********");
        }
    }

    public void createButton(boolean isPassButton) {
//        int buttonSize = getResources().getDimensionPixelSize(R.dimen.pass_fail_button_size);
        if (isPassButton) {
            mPassButton = new Button(this);
            mPassButton.setText(R.string.text_pass);
            mPassButton.setTextColor(Color.WHITE);
            mPassButton.setTextSize(TEXT_SIZE);
            mPassButton.setBackgroundColor(Color.RED);
            mPassButton.setOnClickListener(this);
            mPassButton.setPadding(20, 20, 20, 20);
        } else {
            mFailButton = new Button(this);
            mFailButton.setText(R.string.text_fail);
            mFailButton.setTextColor(Color.WHITE);
            mFailButton.setTextSize(TEXT_SIZE);
            mFailButton.setBackgroundColor(Color.RED);
            mFailButton.setOnClickListener(this);
            mFailButton.setPadding(20, 20, 20, 20);
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
//                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        lp.gravity = isPassButton ? Gravity.LEFT | Gravity.BOTTOM : Gravity.RIGHT | Gravity.BOTTOM;
        lp.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowManager.addView(isPassButton ? mPassButton : mFailButton, lp);
    }

    public void createHeadLayout() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
//                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        addHeadView = LayoutInflater.from(this).inflate(R.layout.float_window_head, null);
        lp.gravity = Gravity.TOP;
        lp.height = 30;
        lp.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowManager.addView(addHeadView, lp);
    }

    public void createBtnLayout() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
//                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        addView = LayoutInflater.from(this).inflate(R.layout.float_btn, null);
        lp.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.height = 110;
        lp.gravity = Gravity.BOTTOM;
        mPassButton = addView.findViewById(R.id.btn_ok);
        mFailButton = addView.findViewById(R.id.btn_fail);
        mFailButton.setOnClickListener(this);
        mPassButton.setOnClickListener(this);
        mWindowManager.addView(addView, lp);
    }

    public void storeRusult(boolean isSuccess) {
        Log.d("BaseActivity", "storeResult " + mTestname);
        mEngSqlite.updateDB(mTestname, isSuccess ? Const.SUCCESS : Const.FAIL);
        Log.d(TAG, "onCreate storeRusult="+SUPPORT_WRITE_STATION);
        if(SUPPORT_WRITE_STATION){
            try {
                //storeItemOffset(mTestname);
                storePhaseCheck();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }

    private void storePhaseCheck() {
        String station = STATION_MMIT_VALUE;
        if(mPhaseCheckParse == null){
            return;
        }

        Log.d("BaseActivity", "storePhaseCheck: fail = "+mEngSqlite.queryFailCount() + ", NotTest = " + mEngSqlite.queryNotTestCount());
        /*
        if (mPhaseCheckParse.isStationTest(STATION_AGING_VALUE) && mPhaseCheckParse.isStationPass(STATION_AGING_VALUE)) {
          station = STATION_MMIT2_VALUE;
        }else {
          station = STATION_MMIT1_VALUE;
        }
        */
        mPhaseCheckParse.writeStationTested(station);
        if (mEngSqlite.queryFailCount() == 0 && mEngSqlite.queryNotTestCount()== 0) {
            mPhaseCheckParse.writeStationPass(station);
        }else {
            mPhaseCheckParse.writeStationFail(station);
        }
    }


    @Override
    public void finish() {
        removeButton();
        this.setResult(Const.TEST_ITEM_DONE, getIntent());
        super.finish();
    }

    protected void showResultDialog(String content) {
        LinearLayout resultDlgLayout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.result_dlg, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        content = "\n" + content + "\n";
        content += getString(R.string.alert_dialog_test);
        TextView title = (TextView) resultDlgLayout.findViewById(R.id.title);
        title.setText(getResources().getString(R.string.alert_dialog_title));
        TextView message = (TextView) resultDlgLayout.findViewById(R.id.message);
        message.setText(content);
        builder.setView(resultDlgLayout);
        Button passBtn = (Button) resultDlgLayout.findViewById(R.id.positiveButton);
        passBtn.setText(R.string.text_pass);
        passBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                storeRusult(true);
                finish();
            }
        });
        Button failBtn = (Button) resultDlgLayout.findViewById(R.id.negativeButton);
        failBtn.setText(R.string.text_fail);
        failBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                storeRusult(false);
                finish();
            }
        });
//        builder.setNegativeButton(getResources().getString(R.string.text_pass),
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        storeRusult(true);
//                        finish();
//                    }
//                });
//        builder.setPositiveButton(getResources().getString(R.string.text_fail),
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        storeRusult(false);
//                        finish();
//                    }
//                });

        if (mResultDialog != null) {
            mResultDialog.cancel();
        }

        mResultDialog = builder.create();
        mResultDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP && shouldCanceled) {
                    Intent intent = BaseActivity.this.getIntent();
                    mResultDialog.cancel();

                    BaseActivity.super.finish();
                    BaseActivity.this.startActivityForResult(intent,0);
                    return true;
                }
                shouldCanceled = true;
                return false;
            }
        });
        mResultDialog.setCanceledOnTouchOutside(false);
        if (mResultDialog != null && !mResultDialog.isShowing()) {
            mResultDialog.show();
        }
    }

    protected void removeButton() {
        try {
            if (addView != null) {
                mWindowManager.removeView(addView);
            }
            if (addHeadView != null) {
                mWindowManager.removeView(addHeadView);
            }
//            mWindowManager.removeView(mPassButton);
//            mWindowManager.removeView(mFailButton);
        } catch (Exception e) {
            //TODO
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }
    }

    @Override
    public void onBackPressed() {
        // showResultDialog(getString(R.string.alert_finish_test));
        Intent intent = BaseActivity.this.getIntent();
        BaseActivity.this.startActivityForResult(intent, 0);
        finish();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mPassButton) {
            if (canPass) {
                Log.d("onclick", "pass.." + this);
                storeRusult(true);
                finish();
                } else {
                Toast.makeText(this, R.string.can_not_pass, Toast.LENGTH_SHORT).show();
            }
        } else if (v == mFailButton) {
            storeRusult(false);
            finish();
        }
    }
}
