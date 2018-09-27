package com.github.mikephil.charting.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;


/**
 * Created by Colman on 26/04/2018.
 */

public abstract class StaticMarkerView extends RelativeLayout {

    private int widthPx;
    private int heightPx;

    public StaticMarkerView(Context context, int layoutResource) {
        super(context);
        setupLayoutResource(layoutResource);
    }



    private void setupLayoutResource(int layoutResource) {
        View inflated = LayoutInflater.from(getContext()).inflate(layoutResource, this);

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        widthPx = (int) (375 * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        heightPx = (int) (245 * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));

        inflated.measure(MeasureSpec.makeMeasureSpec(widthPx, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightPx, MeasureSpec.EXACTLY));
        inflated.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }



    public abstract void refreshContent(float x);



    public Bitmap convertToBitmap(int width) {
        Bitmap bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return Bitmap.createScaledBitmap(bitmap, width, width * heightPx / widthPx, false);
    }



    public void draw(Canvas canvas, float posX, float posY, Bitmap bitmap) {

    }

    /*
    if(previousWidth != width) {
            measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(width * 245 / 375, MeasureSpec.EXACTLY));
            layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
            previousWidth = width;
        }

        int saveId = canvas.save();
        canvas.translate(posX, posY);
        draw(canvas);
        canvas.restoreToCount(saveId);
     */
}

