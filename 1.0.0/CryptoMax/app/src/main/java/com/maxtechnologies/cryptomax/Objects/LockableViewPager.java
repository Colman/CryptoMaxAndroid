package com.maxtechnologies.cryptomax.Objects;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Colman on 05/05/2018.
 */

public class LockableViewPager extends ViewPager {

    //Swipe declaration
    private boolean swipeEnabled;


    public LockableViewPager(Context context) {
        super(context);
    }



    public LockableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.swipeEnabled = true;
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.swipeEnabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.swipeEnabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }



    public void setSwipeEnabled(boolean swipeEnabled) {
        this.swipeEnabled = swipeEnabled;
    }
}
