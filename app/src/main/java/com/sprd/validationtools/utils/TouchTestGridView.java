package com.sprd.validationtools.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridLayout;

public class TouchTestGridView extends GridLayout {

    public TouchTestGridView(Context context) {
        super(context);
    }

    public TouchTestGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchTestGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TouchTestGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /*将所有要分发的MotionEvent的Action都改为MotionEvent.ACTION_DOWN*/
/*    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        MotionEvent e = MotionEvent.obtain(ev);
        e.setAction(MotionEvent.ACTION_DOWN);
        return super.dispatchTouchEvent(e);
    }*/
}
