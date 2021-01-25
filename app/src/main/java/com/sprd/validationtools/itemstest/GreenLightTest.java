
package com.sprd.validationtools.itemstest;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.util.Log;
import com.sprd.validationtools.Const;

public class GreenLightTest extends BaseActivity {
    private static final String TAG = "GreenLightTest";
    private TextView mContent;
//  private IBinder mBinder;
//  private AdaptBinder mBinder;
    private PhaseCheckParse mPhaseCheckParse = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContent = new TextView(this);
        setBackground();
        setContentView(mContent);
        mContent.setGravity(Gravity.CENTER);
        mContent.setTextSize(35);
        //mBinder = ServiceManager.getService("phasechecknative");
//        mBinder = new AdaptBinder();
//        if (mBinder != null)
//            Log.e(TAG, "Get The service connect!");
//        else
//            Log.e(TAG, "connect Error!!");
//        writeLedlightSwitch(1);
        mPhaseCheckParse = new PhaseCheckParse();
        if(mPhaseCheckParse != null){
            mPhaseCheckParse.writeLedlightSwitch(9, 1);
        }

    }

    private void setBackground() {
        mContent.setBackgroundColor(Color.GREEN);
        mContent.setText(getString(R.string.status_indicator_green));
    }

    /*public boolean writeLedlightSwitch(int value) {
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInt(value);
            mBinder.transact(9, data, reply, 0);
            Log.e(TAG, "writeLedlightSwitch green light data = " + reply.readString() + " SUCESS!!");
            data.recycle();
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "Exception ", ex);
            return false;
        }
    }*/

    @Override
    protected void onDestroy() {
//        writeLedlightSwitch(0);
        if(mPhaseCheckParse != null){
            mPhaseCheckParse.writeLedlightSwitch(9, 0);
        }
        super.onDestroy();
    }

}
