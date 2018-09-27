package com.maxtechnologies.cryptomax.ui.chart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.misc.BasicCallback;
import com.maxtechnologies.cryptomax.api.ProfilesCallback;
import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.exchange.asset.Asset;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.exchange.candle.Candle;
import com.maxtechnologies.cryptomax.ui.misc.AlertController;
import com.maxtechnologies.cryptomax.R;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.StaticMarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.highlight.StaticHighlight;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.maxtechnologies.cryptomax.ui.chart.utils.FormatUtils;
import com.maxtechnologies.cryptomax.ui.chart.utils.IndicatorUtils;
import com.maxtechnologies.cryptomax.ui.chart.utils.MarkerUtils;
import com.maxtechnologies.cryptomax.wallets.Wallet;

import java.util.ArrayList;

/**
 * Created by Colman on 20/04/2018.
 */

public class Chart extends CombinedChart {

    //Transactions declaration
    private ArrayList<ChartTransaction> transactions;

    //Time declarations
    private Runnable runnable;
    public Handler handler;
    public int widthSec;
    private long lastTime;

    //Ticker line declaration
    private int tickerLineLen;

    //Data declarations
    private CombinedData data;
    private ArrayList<CandleEntry> candles;
    private int candleSetIndex;
    private float avgCandleHeight;
    private ArrayList<ArrayList<Integer>> sigCandles;
    private int sigIndex;
    private MPPointF localMin;
    private MPPointF localMax;
    private ArrayList<Float> volume;
    private ArrayList<Float> rsi;

    //UI declarations
    public float centerX;
    public float centerY;


    public Chart(Context context) {
        super(context);
    }

    public Chart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Chart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void initChart() {
        //Ticker line definition
        tickerLineLen = 20;

        //Data definition
        avgCandleHeight = 1;
        sigIndex = 0;
        localMin = new MPPointF();
        localMax = new MPPointF();

        //UI definitions
        centerX = 0;
        centerX = 0;


        //Setup the chart
        setGridBackgroundColor(Color.BLACK);
        setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        setDrawGridBackground(true);
        setNoDataText("");
        setHighlightPerTapEnabled(false);
        setHighlightPerDragEnabled(false);
        setDoubleTapToZoomEnabled(false);
        //setMarker(new ExtremaMarkerView(getContext(), R.layout.extrema_marker));

        setStaticMarker(new TransactionMarkerView(getContext(), R.layout.transaction_marker));
        setStaticMarkerWidth(10f);
        setStaticMarkerLineWidth(0.1f);
        setStaticMarkerLineColor(Color.WHITE);
        setStaticMarkerCircleRadius(0.40f);

        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(new DateAxisFormatter());

        YAxis rightAxis = getAxisRight();
        rightAxis.setLabelCount(7, false);
        rightAxis.setTextColor(Color.WHITE);
        rightAxis.setValueFormatter(new PriceAxisFormatter());
        YAxis leftAxis = getAxisLeft();
        leftAxis.setEnabled(false);

        getDescription().setEnabled(false);
        getLegend().setEnabled(false);
        setMaxVisibleValueCount(0);
        setPinchZoom(true);

        DrawOrder[] order = getDrawOrder();
        order[2] = DrawOrder.CANDLE;
        order[3] = DrawOrder.LINE;
        setDrawOrder(order);


        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                stopScrolling = false;
                return false;
            }
        });


        setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                //Do nothing
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                //Do nothing
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
                //Do nothing
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
                //Do nothing
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
                //Do nothing
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
                //Do nothing
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                ViewPortHandler viewPortHandler = getViewPortHandler();
                MPPointD bottomLeft = getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentBottom(), YAxis.AxisDependency.RIGHT);
                MPPointD bottomRight = getValuesByTouchPoint(viewPortHandler.contentRight(), viewPortHandler.contentBottom(), YAxis.AxisDependency.RIGHT);

                calculateLabels((float) bottomLeft.x, (float) bottomRight.x);
                findExtrema();
                scaleUnderlays();
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                findExtrema();
                scaleUnderlays();
            }
        });
    }


    public void setCandles(ArrayList<Candle> candleEntries, ArrayList<Float> volumeEntries) {
        ArrayList<CandleEntry> newCandles = new ArrayList<>();
        for (int i = 0; i < candleEntries.size(); i++) {
            float open = candleEntries.get(i).open;
            float close = candleEntries.get(i).close;
            float high = candleEntries.get(i).high;
            float low = candleEntries.get(i).low;
            CandleEntry entry = new CandleEntry(i, high, low, open, close);
            newCandles.add(entry);
        }

        candles = newCandles;
        lastTime = candleEntries.get(candleEntries.size() - 1).time;
        volume = volumeEntries;

        setData(true);

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                addCandle();
                setData(false);

                long currTime = System.currentTimeMillis();
                handler.postDelayed(runnable, (lastTime + widthSec) * 1000 - currTime);
            }
        };
        long currTime = System.currentTimeMillis();
        handler.postDelayed(runnable, (lastTime + widthSec) * 1000 - currTime);
    }


    public void setData(final boolean reFocus) {
        clear();
        data = new CombinedData();
        resetTracking();

        addCandles();
        addTickerLine();

        addTransactions();

        avgCandleHeight = 0;
        for (int i = 0; i < candles.size(); i++) {
            avgCandleHeight += Math.abs(candles.get(i).getHigh() - candles.get(i).getLow());
        }
        avgCandleHeight /= candles.size();

        long tempTime = lastTime + tickerLineLen * widthSec;
        sigCandles = FormatUtils.getSigCandles(widthSec, candles.size() + tickerLineLen, tempTime);

        if (Settings.smaChecked) {
            for (int i = 0; i < 3; i++) {
                if (Settings.sma[i] > 0) {
                    float[] sma = IndicatorUtils.getSMA(candles, Settings.sma[i]);
                    addSMA(sma, Settings.sma[i], i);
                }
            }
        }

        if (Settings.emaChecked) {
            for (int i = 0; i < 3; i++) {
                if (Settings.ema[i] > 0) {
                    float[] ema = IndicatorUtils.getEMA(candles, Settings.ema[i]);
                    addEMA(ema, Settings.ema[i], i);
                }
            }
        }

        if (Settings.bolChecked) {
            if (Settings.bol[0] > 0 && Settings.bol[1] > 0) {
                float[][] bollinger = IndicatorUtils.getBollinger(candles, Settings.bol[0], Settings.bol[1]);
                addBollinger(bollinger, Settings.bol[0]);
            }
        }

        if (Settings.sarChecked) {
            if (Settings.sar[0] > 0 && Settings.sar[1] > 0) {
                float[] sar = IndicatorUtils.getSAR(candles, Settings.sar[0], Settings.sar[1]);
                addSAR(sar);
            }
        }

        if (Settings.volumeChecked) {
            addVolume();
        }

        setData(data);

        ViewPortHandler viewPortHandler = getViewPortHandler();
        setViewPortOffsets(0, 0, viewPortHandler.offsetRight(), viewPortHandler.offsetBottom());
        MPPointD topLeft = getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentTop(), YAxis.AxisDependency.RIGHT);
        MPPointD bottomRight = getValuesByTouchPoint(viewPortHandler.contentRight(), viewPortHandler.contentBottom(), YAxis.AxisDependency.RIGHT);
        centerX = (float) (topLeft.x + bottomRight.x) / 2;
        centerY = (float) (topLeft.y + bottomRight.y) / 2;
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setAspect(reFocus);
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        if (getAxisRight().getAxisMinimum() < 0) {
            getAxisRight().setAxisMinimum(0);
        }
        notifyDataSetChanged();
        invalidate();
    }


    public void updateData(float price) {
        if (getCandleData() != null && getLineData()!= null) {
            CandleEntry lastCandle = candles.get(candles.size() - 1);
            lastCandle.setClose(price);
            if (lastCandle.getLow() > price) {
                lastCandle.setLow(price);
            }
            if (lastCandle.getHigh() < price) {
                lastCandle.setHigh(price);
            }

            updateTickerLine();

            if (Settings.smaChecked) {
                for (int i = 0; i < 3; i++) {
                    if (Settings.sma[i] > 0) {
                        float[] sma = IndicatorUtils.getSMA(candles, Settings.sma[i]);

                        String label = "SMA" + String.valueOf(Settings.sma[i]);
                        ILineDataSet lineSet = data.getLineData().getDataSetByLabel(label, false);
                        lineSet.getEntryForIndex(lineSet.getEntryCount() - 1).setY(sma[sma.length - 1]);
                    }
                }
            }

            if (Settings.emaChecked) {
                for (int i = 0; i < 3; i++) {
                    if (Settings.ema[i] > 0) {
                        float[] ema = IndicatorUtils.getEMA(candles, Settings.ema[i]);

                        String label = "EMA" + String.valueOf(Settings.ema[i]);
                        ILineDataSet lineSet = data.getLineData().getDataSetByLabel(label, false);
                        lineSet.getEntryForIndex(lineSet.getEntryCount() - 1).setY(ema[ema.length - 1]);
                    }
                }
            }

            if (Settings.bolChecked) {
                if (Settings.bol[0] > 0 && Settings.bol[1] > 0) {
                    float[][] bollinger = IndicatorUtils.getBollinger(candles, Settings.bol[0], Settings.bol[1]);

                    for (int i = 0; i < 3; i++) {
                        String label = "BOL" + String.valueOf(i);
                        ILineDataSet lineSet = data.getLineData().getDataSetByLabel(label, false);
                        lineSet.getEntryForIndex(lineSet.getEntryCount() - 1).setY(bollinger[i][bollinger[i].length - 1]);
                    }
                }
            }

            if (Settings.sarChecked) {
                if (Settings.sar[0] > 0 && Settings.sar[1] > 0) {
                    float[] sar = IndicatorUtils.getSAR(candles, Settings.sar[0], Settings.sar[1]);

                    String label = "SAR";
                    IScatterDataSet scatterSet = data.getScatterData().getDataSetByLabel(label, false);
                    scatterSet.getEntryForIndex(scatterSet.getEntryCount() - 1).setY(sar[sar.length - 1]);
                }
            }

            if (Settings.volumeChecked) {
                //Get new volume
            }


            getCandleData().notifyDataChanged();
            getLineData().notifyDataChanged();
            if (getScatterData() != null) {
                getScatterData().notifyDataChanged();
            }
            notifyDataSetChanged();
            invalidate();
        }
    }


    private void addCandles() {
        CandleDataSet candleSet = new CandleDataSet(candles, "Candles");
        candleSet.setDrawHighlightIndicators(false);
        candleSet.setHighlightEnabled(true);
        candleSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        candleSet.setShadowColorSameAsCandle(true);
        candleSet.setShadowWidth(0.9f);
        candleSet.setDecreasingColor(Color.rgb(156, 0, 4));
        candleSet.setDecreasingPaintStyle(Paint.Style.FILL);
        candleSet.setIncreasingColor(Color.rgb(0, 185, 9));
        candleSet.setIncreasingPaintStyle(Paint.Style.STROKE);
        candleSet.setNeutralColor(Color.rgb(156, 0, 4));

        CandleData candleData = data.getCandleData();
        if (candleData == null) {
            candleData = new CandleData(candleSet);
        } else {
            candleData.addDataSet(candleSet);
        }


        data.setData(candleData);
    }


    private void addTickerLine() {
        YAxis yAxis = getAxisRight();
        CandleEntry candle = candles.get(candles.size() - 1);
        yAxis.setExtraYValue(candle.getClose());

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(candles.size() - 1, candle.getClose()));
        entries.add(new Entry(candles.size() + tickerLineLen, candle.getClose()));
        LineDataSet tickerSet = new LineDataSet(entries, "Ticker");
        tickerSet.setDrawCircles(false);
        if (candle.getClose() <= candle.getOpen()) {
            tickerSet.setColor(Color.rgb(156, 0, 4));
        } else {
            tickerSet.setColor(Color.rgb(0, 185, 9));
        }

        tickerSet.enableDashedLine(25, 25, 0);
        tickerSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        LineData lineData = data.getLineData();
        if (lineData == null) {
            lineData = new LineData(tickerSet);
        } else {
            lineData.addDataSet(tickerSet);
        }

        data.setData(lineData);
    }


    private void updateTickerLine() {
        CandleEntry candle = candles.get(candles.size() - 1);
        LineDataSet tickerSet = (LineDataSet) data.getLineData().getDataSetByLabel("Ticker", false);
        tickerSet.getEntryForIndex(0).setY(candle.getClose());
        tickerSet.getEntryForIndex(1).setY(candle.getClose());

        YAxis yAxis = getAxisRight();
        yAxis.setExtraYValue(candle.getClose());
        if (candle.getClose() <= candle.getOpen()) {
            tickerSet.setColor(Color.rgb(156, 0, 4));
        } else {
            tickerSet.setColor(Color.rgb(0, 185, 9));
        }
    }


    private void addSMA(float[] sma, int period, int number) {
        ArrayList<Entry> lineEntries = new ArrayList<>();
        for (int i = period - 1; i < sma.length; i++) {
            lineEntries.add(new Entry(i, sma[i]));
        }
        LineDataSet lineSet = new LineDataSet(lineEntries, "SMA" + String.valueOf(period));
        lineSet.setDrawCircles(false);

        if (number == 0) {
            lineSet.setColor(Color.parseColor("#CCCCCC"));
        } else if (number == 1) {
            lineSet.setColor(Color.parseColor("#00CCCC"));
        } else {
            lineSet.setColor(Color.parseColor("#F4A460"));
        }
        lineSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        LineData lineData = data.getLineData();
        if (lineData == null) {
            lineData = new LineData(lineSet);
        } else {
            lineData.addDataSet(lineSet);
        }

        data.setData(lineData);
    }


    private void addEMA(float[] ema, int period, int number) {
        ArrayList<Entry> lineEntries = new ArrayList<>();
        for (int i = period - 1; i < ema.length; i++) {
            lineEntries.add(new Entry(i, ema[i]));
        }
        LineDataSet lineSet = new LineDataSet(lineEntries, "EMA" + String.valueOf(period));
        lineSet.setDrawCircles(false);

        if (number == 0) {
            lineSet.setColor(Color.parseColor("#CCCCCC"));
        } else if (number == 1) {
            lineSet.setColor(Color.parseColor("#00CCCC"));
        } else {
            lineSet.setColor(Color.parseColor("#F4A460"));
        }
        lineSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        LineData lineData = data.getLineData();
        if (lineData == null) {
            lineData = new LineData(lineSet);
        } else {
            lineData.addDataSet(lineSet);
        }

        data.setData(lineData);
    }


    private void addBollinger(float[][] points, int period) {
        for (int i = 0; i < 3; i++) {
            ArrayList<Entry> lineEntries = new ArrayList<>();
            for (int j = period - 1; j < points[i].length; j++) {
                lineEntries.add(new Entry(j, points[i][j]));
            }
            LineDataSet lineSet = new LineDataSet(lineEntries, "BOL" + String.valueOf(i));
            lineSet.setDrawCircles(false);
            lineSet.setColor(Color.parseColor("#CCCCCC"));
            if (i == 1) {
                lineSet.enableDashedLine(25, 50, 0);
            }
            lineSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

            LineData lineData = data.getLineData();
            if (lineData == null) {
                lineData = new LineData(lineSet);
            } else {
                lineData.addDataSet(lineSet);
            }

            data.setData(lineData);
        }
    }


    private void addSAR(float[] points) {
        float min = candles.get(0).getLow();
        for (int i = 0; i < candles.size(); i++) {
            if (candles.get(i).getLow() < min) {
                min = candles.get(i).getLow();
            }
        }
        int startIndex = 0;
        for (int i = 0; i < points.length; i++) {
            if (points[i] > min) {
                startIndex = i;
                break;
            }
        }
        ArrayList<Entry> scatterEntries = new ArrayList<>();
        for (int i = startIndex; i < points.length; i++) {
            scatterEntries.add(new Entry(i, points[i]));
        }
        ScatterDataSet scatterSet = new ScatterDataSet(scatterEntries, "SAR");
        scatterSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        scatterSet.setScatterShapeSize(5f);
        scatterSet.setColor(Color.parseColor("#CCCCCC"));
        scatterSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        ScatterData scatterData = data.getScatterData();
        if (scatterData == null) {
            scatterData = new ScatterData(scatterSet);
        } else {
            scatterData.addDataSet(scatterSet);
        }

        data.setData(scatterData);
    }


    private void addRSI() {
        ArrayList<Entry> rsiEntries = scaleRSI();
        LineDataSet rsiSet = new LineDataSet(rsiEntries, "RSI");
        rsiSet.setDrawCircles(false);
        rsiSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        rsiSet.setColor(Color.parseColor("#CCCCCC"));

        LineData lineData = data.getLineData();
        if (lineData == null) {
            lineData = new LineData(rsiSet);
        } else {
            lineData.addDataSet(rsiSet);
        }
        data.setData(lineData);
    }


    private void addVolume() {
        ArrayList<CandleEntry> volumeEntries = scaleVolume();
        CandleDataSet volumeSet = new CandleDataSet(volumeEntries, "Volume");
        volumeSet.setDrawIcons(false);
        volumeSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        volumeSet.setShadowColorSameAsCandle(true);
        volumeSet.setShadowWidth(0.9f);
        volumeSet.setDecreasingColor(Color.parseColor("#44FFFFFF"));
        volumeSet.setDecreasingPaintStyle(Paint.Style.FILL_AND_STROKE);
        volumeSet.setIncreasingColor(Color.parseColor("#44FFFFFF"));
        volumeSet.setIncreasingPaintStyle(Paint.Style.FILL_AND_STROKE);
        volumeSet.setNeutralColor(Color.parseColor("#44FFFFFF"));

        CandleData candleData = data.getCandleData();
        if (candleData == null) {
            candleData = new CandleData(volumeSet);
        } else {
            candleData.addDataSet(volumeSet);
        }
        data.setData(candleData);
    }


    private void scaleUnderlays() {
        if (Settings.volumeChecked) {
            ArrayList<CandleEntry> entries = scaleVolume();
            ICandleDataSet candleSet = data.getCandleData().getDataSetByLabel("Volume", false);
            int length = candleSet.getEntryCount();
            for (int i = 0; i < length; i++) {
                candleSet.removeLast();
            }
            for (int i = 0; i < entries.size(); i++) {
                candleSet.addEntry(entries.get(i));
            }

            notifyDataSetChanged();
            invalidate();
        }
    }


    private ArrayList<Entry> scaleRSI() {
        int minX = (int) Math.floor(getLowestVisibleX());
        int maxX = (int) Math.floor(getHighestVisibleX());
        if (maxX > candles.size() - 1) {
            maxX = candles.size() - 1;
        }

        ArrayList<Entry> scaledRSI = new ArrayList<>();
        ViewPortHandler handler = getViewPortHandler();
        MPPointD topLeft = getValuesByTouchPoint(handler.contentLeft(), handler.contentTop(), YAxis.AxisDependency.RIGHT);
        MPPointD bottomRight = getValuesByTouchPoint(handler.contentRight(), handler.contentBottom(), YAxis.AxisDependency.RIGHT);
        float low = (float) bottomRight.y;
        float high = (float) topLeft.y;
        float maxHeight = (high - low) / 3;
        for (int i = minX; i <= maxX; i++) {
            float height = rsi.get(i) / 100f;
            Entry entry = new Entry(i, low + height * maxHeight);
            scaledRSI.add(entry);
        }

        return scaledRSI;
    }


    private ArrayList<CandleEntry> scaleVolume() {
        int minX = (int) Math.floor(getLowestVisibleX());
        int maxX = (int) Math.floor(getHighestVisibleX());
        if (maxX > candles.size() - 1) {
            maxX = candles.size() - 1;
        }

        float maxValue = 0;
        for (int i = minX; i <= maxX; i++) {
            float value = volume.get(i);
            if (value > maxValue) {
                maxValue = value;
            }
        }

        ArrayList<CandleEntry> scaledVolumes = new ArrayList<>();
        ViewPortHandler handler = getViewPortHandler();
        MPPointD topLeft = getTransformer(YAxis.AxisDependency.RIGHT).getValuesByTouchPoint(handler.contentLeft(), handler.contentTop());
        MPPointD bottomRight = getTransformer(YAxis.AxisDependency.RIGHT).getValuesByTouchPoint(handler.contentRight(), handler.contentBottom());
        float low = (float) bottomRight.y;
        float high = (float) topLeft.y;
        float min = getAxisRight().getAxisMinimum();
        float max = getAxisRight().getAxisMaximum();
        if (low < min) {
            low = min;
        }
        if (high > max) {
            high = max;
        }
        float maxHeight = (high - low) / 3;
        for (int i = minX; i <= maxX; i++) {
            float height = volume.get(i) / maxValue;
            CandleEntry entry = new CandleEntry(i, low + height * maxHeight, min, min, low + height * maxHeight);
            scaledVolumes.add(entry);
        }

        return scaledVolumes;
    }


    private void addCandle() {
        float lastClose = candles.get(candles.size() - 1).getClose();
        CandleEntry candle = new CandleEntry(candles.size(), lastClose, lastClose, lastClose, lastClose);
        lastTime = lastTime + widthSec;
        candles.add(candle);
    }


    public void setAspect(boolean reFocus) {
        setPinchZoom(false);
        fitScreen();
        float chartXRange = getXChartMax() - getXChartMin();
        float chartYRange = getYChartMax() - getYChartMin();
        float pixelAspect = getWidth() / getHeight();
        float visibleY = avgCandleHeight * 10f / pixelAspect;
        float portAspect = 20f / visibleY;

        int maxLabels = getXAxis().getLabelCount();
        int interval = 0;
        if (widthSec == 60) {
            interval = 30;
        }
        else if (widthSec == 180) {
            interval = 120;
        }
        else if (widthSec == 300) {
            interval = 72;
        }
        else if (widthSec == 900) {
            interval = 24;
        }
        else if (widthSec == 1800) {
            interval = 48;
        }
        else if (widthSec == 3600) {
            interval = 24;
        }
        else if (widthSec == 7200) {
            interval = 84;
        }
        else if (widthSec == 14400) {
            interval = 42;
        }
        else if (widthSec == 21600) {
            interval = 28;
        }
        else if (widthSec == 43200) {
            interval = 60;
        }
        else if (widthSec == 86400) {
            interval = 30;
        }
        else if (widthSec == 259200) {
            interval = 30;
        }
        else if (widthSec == 604800) {
            interval = 24;
        }
        float tempXRange = maxLabels * interval;
        if (tempXRange > chartXRange) {
            tempXRange = chartXRange;
        }

        float chartAspect = tempXRange / chartYRange;
        if (chartAspect > portAspect) {
            setVisibleXRangeMaximum(chartYRange * portAspect);
            setVisibleYRangeMaximum(chartYRange, YAxis.AxisDependency.RIGHT);
            tempXRange = chartYRange * portAspect;
        } else {
            setVisibleXRangeMaximum(tempXRange);
            setVisibleYRangeMaximum(tempXRange / portAspect, YAxis.AxisDependency.RIGHT);
        }

        if (tempXRange > 20f) {
            if (reFocus) {
                float close = candles.get(candles.size() - 1).getClose();
                zoom(tempXRange / 20f, tempXRange / 20f, 0, 0);
                centerViewTo(candles.size() - 1, close, YAxis.AxisDependency.RIGHT);
                calculateLabels(candles.size() - 11, candles.size() + 9);
            } else {
                zoom(tempXRange / 20f, tempXRange / 20f, 0, 0);
                centerViewTo(centerX, centerY, YAxis.AxisDependency.RIGHT);
                calculateLabels(centerX - 10, centerX + 10);
            }
        } else {
            calculateLabels(getXChartMin(), getXChartMax());
        }

        setPinchZoom(true);
        findExtrema();
    }



    private void calculateLabels(float left, float right) {
        XAxis xAxis = getXAxis();
        int startIndex = (int) Math.ceil(left);
        int endIndex = (int) Math.floor(right);

        for (int i = 0; i < sigCandles.size(); i++) {
            ArrayList<Integer> labelPositions = new ArrayList<>();
            for (int j = 0; j < sigCandles.get(i).size(); j++) {
                int index = sigCandles.get(i).get(j);
                if (index >= startIndex && index <= endIndex) {
                    labelPositions.add(index);
                }
                if (index > endIndex) {
                    break;
                }
            }

            if (labelPositions.size() <= xAxis.getLabelCount()) {
                sigIndex = i;
                xAxis.labelPositions = new float[sigCandles.get(i).size()];
                for (int j = 0; j < sigCandles.get(i).size(); j++) {
                    xAxis.labelPositions[j] = sigCandles.get(i).get(j);
                }
                break;
            } else {
                xAxis.labelPositions = null;
            }
        }
    }



    private void findExtrema() {
        int minX = (int) Math.ceil(getLowestVisibleX());
        int maxX = (int) Math.floor(getHighestVisibleX());
        if(maxX > candles.size() - 1) {
            maxX = candles.size() - 1;
        }

        if(minX > 0 && minX < candles.size()) {
            localMin.x = minX;
            localMin.y = candles.get(minX).getLow();
            localMax.x = minX;
            localMax.y = candles.get(minX).getLow();
        }
        else {
            return;
        }

        for(int i = minX; i <= maxX; i++) {
            float low = candles.get(i).getLow();
            float high = candles.get(i).getHigh();
            if(localMin.y > low) {
                localMin.x = i;
                localMin.y = low;
            }
            if(localMax.y < high) {
                localMax.x = i;
                localMax.y = high;
            }
        }
    }



    private void addTransactions() {
        int index = ((ChartActivity) getContext()).index;
        Asset coin = StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index);

        ArrayList<Wallet> wallets = new ArrayList<>();
        for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
            Wallet wallet = CryptoMaxApi.getWallet(i);
            if(Exchange.translateToSymbol(wallet.exchangeSymbol).equals(coin.symbol)) {
                wallets.add(wallet);
            }
        }

        getChartTransactions(wallets);
    }



    public void getChartTransactions(final ArrayList<Wallet> wallets) {
        transactions = new ArrayList<>();
        for(int i = 0; i < wallets.size(); i++) {
            final int finalI = i;
            wallets.get(i).getTransactions(new BasicCallback() {
                @Override
                public void onFailure(String reason) {
                    Log.e("Network", reason);
                    AlertController.networkError((ChartActivity) getContext(), true);
                }

                @Override
                public void onSuccess() {
                    ArrayList<Transaction> txs = wallets.get(finalI).transactions;
                    ArrayList<String> otherAddresses = new ArrayList<>();
                    long startTime = lastTime - (candles.size() - 1) * widthSec;

                    for(int j = 0; j < txs.size(); j++) {
                        Transaction transaction = txs.get(j);
                        long txTime = transaction.timeMined.getTime() / 1000;

                        if (txTime > startTime) {
                            int candleIndex = (int) Math.floor((txTime - startTime) / widthSec);
                            ChartTransaction tx = new ChartTransaction(candleIndex, null, true, transaction.amount);
                            String address = transaction.toAddress;

                            if(!address.equals(transaction.fromAddress)) {
                                tx.fromMe = false;
                                address = transaction.fromAddress;
                            }

                            otherAddresses.add(address);
                            transactions.add(tx);
                        }
                    }


                    StaticHighlight[] staticHighlights = new StaticHighlight[transactions.size()];
                    for(int i = 0; i < staticHighlights.length; i++) {
                        int x = transactions.get(i).candleIndex;
                        float y = candles.get(x).getY();
                        StaticHighlight highlight = new StaticHighlight(x, y);
                        staticHighlights[i] = highlight;
                    }

                    MarkerUtils.getHighlightPositions(staticHighlights, data, getStaticMarkerWidth(), getStaticMarkerWidth() * 245 / 375);
                    staticallyHighlightValues(staticHighlights);

                    postInvalidate();

                    getImages(otherAddresses);
                }
            });
        }
    }



    private void getImages(ArrayList<String> otherAddresses) {
        CryptoMaxApi.getProfiles(otherAddresses, new ProfilesCallback() {
            @Override
            public void onFailure(String reason) {
                Log.e("Network", "Failed to get images for reason: " + reason);
                AlertController.networkError((Activity) getContext(), false);
            }

            @Override
            public void onSuccess(String[] names, Bitmap[] images) {
                for(int i = 0; i < transactions.size(); i++) {
                    transactions.get(i).otherImage = images[i];
                }

                postInvalidate();
            }
        });
    }



    private class DateAxisFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            int candleIndex = (int) value;
            long time = lastTime - ((candles.size() - 1) - candleIndex) * widthSec;

            return FormatUtils.getChartDateString(time, widthSec, sigIndex);
        }
    }



    private class PriceAxisFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return Exchange.fiatString(value, true, false, false);
        }
    }



    private class ExtremaMarkerView extends MarkerView {

        public ExtremaMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
        }

        @Override
        public void refreshContent(Entry e, Highlight h) {

        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }



    private class TransactionMarkerView extends StaticMarkerView {

        //UI declarations
        private ImageView image1;
        private ImageView image2;
        private TextView you1;
        private TextView you2;
        private TextView amount;


        public TransactionMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);

            //UI definitions
            image1 = (ImageView) findViewById(R.id.image_1);
            image2 = (ImageView) findViewById(R.id.image_2);
            you1 = (TextView) findViewById(R.id.you_1);
            you2 = (TextView) findViewById(R.id.you_2);
            amount = (TextView) findViewById(R.id.amount);
        }


        @Override
        public void refreshContent(float x) {
            int index = Math.round(x);
            ChartTransaction tx = null;
            for(int i = 0; i < transactions.size(); i++) {
                if(transactions.get(i).candleIndex == index) {
                    tx = transactions.get(i);
                    break;
                }
            }

            if(tx == null)
                return;

            Context context = getContext();
            if(tx.fromMe) {
                if(CryptoMaxApi.getImage() == null) {
                    Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                            "default_profile_picture", "drawable", context.getPackageName()));
                    image1.setImageDrawable(drawable);
                }
                else {
                    image1.setImageBitmap(CryptoMaxApi.getImage());
                }
                if(tx.otherImage == null) {
                    Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                            "default_profile_picture", "drawable", context.getPackageName()));
                    image2.setImageDrawable(drawable);
                }
                else {
                    image2.setImageBitmap(tx.otherImage);
                }

                you1.setVisibility(VISIBLE);
                you2.setVisibility(INVISIBLE);
            }

            else {
                if(tx.otherImage == null) {
                    Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                            "default_profile_picture", "drawable", context.getPackageName()));
                    image1.setImageDrawable(drawable);
                }
                else {
                    image1.setImageBitmap(tx.otherImage);
                }
                if(CryptoMaxApi.getImage() == null) {
                    Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                            "default_profile_picture", "drawable", context.getPackageName()));
                    image2.setImageDrawable(drawable);
                }
                else {
                    image2.setImageBitmap(CryptoMaxApi.getImage());
                }

                you1.setVisibility(INVISIBLE);
                you2.setVisibility(VISIBLE);
            }

            int coinIndex = ((ChartActivity) getContext()).index;
            String amountStr = Exchange.coinString(tx.amount, coinIndex, true, true);
            amount.setText(amountStr);
        }
    }
}
