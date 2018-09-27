package com.maxtechnologies.cryptomax.ui.misc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.maxtechnologies.cryptomax.R;

import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.core.ViewFinderView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Colman on 25/03/2018.
 */

public class CustomScannerView extends ZXingScannerView {


    public CustomScannerView(Context context) {
        super(context);
    }

    public CustomScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected IViewFinder createViewFinderView(Context context) {
        CustomViewFinderView finderView = new CustomViewFinderView(context);
        finderView.setLaserColor(0x00000000);
        finderView.setBorderColor(context.getResources().getColor(R.color.colorAccent));
        return finderView;
    }



    private static class CustomViewFinderView extends ViewFinderView {

        public CustomViewFinderView(Context context) {
            super(context);
        }


        @Override
        public void drawLaser(Canvas canvas) {
            //Do nothing
        }



        @Override
        public synchronized void updateFramingRect() {
            super.updateFramingRect();
            Rect originalRect = super.getFramingRect();

            int width = this.getWidth();
            int height = this.getHeight();
            if(width <= height) {
                int left = (int) (0.25f * width);
                int right = (int) (0.75f * width);
                int top = (int) (height / 2f - (right - left) / 2f);
                int bottom = (int) (height / 2f + (right - left) / 2f);
                originalRect.set(left, top, right, bottom);
            }

            else {
                int top = (int) (0.25f * height);
                int bottom = (int) (0.75f * height);
                int left = (int) (width / 2f - (bottom - top) / 2f);
                int right = (int) (width / 2f + (bottom - top) / 2f);
                originalRect.set(left, top, right, bottom);
            }
        }
    }
}
