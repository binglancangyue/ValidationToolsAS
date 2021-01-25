
package com.sprd.validationtools.itemstest;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.R;

public class BlueLightTest extends BaseActivity {
    private static final String TAG = "BlueLightTest";
    private TextView mContent;
    private PhaseCheckParse mPhaseCheckParse = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContent = new TextView(this);
        setBackground();
        setContentView(mContent);
        mContent.setGravity(Gravity.CENTER);
        mContent.setTextSize(35);
        mPhaseCheckParse = new PhaseCheckParse();
        if(mPhaseCheckParse != null){
            mPhaseCheckParse.writeLedlightSwitch(8, 1);
        }
        //writeLedlightSwitch(1);
    }

    private void setBackground() {
        mContent.setBackgroundColor(Color.BLUE);
        mContent.setText(getString(R.string.status_indicator_blue));
    }

    /*public boolean writeLedlightSwitch(int value) {
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInt(value);
            mBinder.transact(8, data, reply, 0);
            Log.e(TAG, "writeLedlightSwitch blue light data = " + reply.readString() + " SUCESS!!");
            data.recycle();
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "Exception ", ex);
            return false;
        }
    }*/

    @Override
    protected void onDestroy() {
        //writeLedlightSwitch(0);
        if(mPhaseCheckParse != null){
            mPhaseCheckParse.writeLedlightSwitch(8, 0);
        }
        super.onDestroy();
    }

}
