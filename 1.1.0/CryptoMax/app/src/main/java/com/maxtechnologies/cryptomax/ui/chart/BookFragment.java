package com.maxtechnologies.cryptomax.ui.chart;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.exchange.book.Book;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.misc.StaticVariables;


public class BookFragment extends Fragment {

    //Index declaration
    private int index;

    //UI declarations
    private boolean bookDrawn;
    private int numLines;
    private ConstraintLayout bookLayout;
    private LinearLayout askLayout;
    private TextView ticker;
    private LinearLayout bidLayout;
    private ProgressBar progress;


    public static BookFragment newInstance(int index) {
        BookFragment fragment = new BookFragment();
        Bundle args = new Bundle();
        args.putInt("INDEX", index);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_book, container, false);

        Bundle bundle = getArguments();
        index = bundle.getInt("INDEX");

        //UI definitions
        bookDrawn = false;
        bookLayout = (ConstraintLayout) rootView.findViewById(R.id.book_layout);
        askLayout = (LinearLayout) rootView.findViewById(R.id.ask_layout);
        ticker = (TextView) rootView.findViewById(R.id.ticker);
        bidLayout = (LinearLayout) rootView.findViewById(R.id.bid_layout);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);


        //Setup the draw listener for the book
        ViewTreeObserver vto = rootView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                drawBook();
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        });


        return rootView;
    }



    public void drawBook() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int parent = Math.round(askLayout.getHeight() / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        numLines = (int) Math.floor(parent / 24);

        askLayout.setWeightSum(numLines);
        for (int i = 0; i < numLines; i++) {
            View bookLineView = getLayoutInflater().inflate(R.layout.book_entry, null);
            TextView price = (TextView) bookLineView.findViewById(R.id.price);
            price.setTextColor(Color.rgb(230, 0, 0));
            bookLineView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ask_border));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            bookLineView.setLayoutParams(params);
            askLayout.addView(bookLineView);
        }


        bidLayout.setWeightSum(numLines);
        for (int i = 0; i < numLines; i++) {
            View bookLineView = getLayoutInflater().inflate(R.layout.book_entry, null);
            TextView price = (TextView) bookLineView.findViewById(R.id.price);
            price.setTextColor(Color.rgb(0, 185, 9));
            price.setTextColor(Color.rgb(0, 185, 9));
            bookLineView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bid_border));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            bookLineView.setLayoutParams(params);
            bidLayout.addView(bookLineView);
        }

        bookDrawn = true;
    }



    public void updateBook(Book book) {
        if(bookDrawn) {
            progress.setVisibility(View.INVISIBLE);
            bookLayout.setVisibility(View.VISIBLE);

            ticker.setText(Exchange.fiatString(StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).price, false, true, false));

            for (int i = numLines - 1; i >= 0; i--) {
                if (numLines - i - 1 < book.asks.size()) {
                    TextView price = askLayout.getChildAt(i).findViewById(R.id.price);
                    price.setText(Exchange.fiatString(book.asks.get(numLines - i - 1).price, false, false, false));
                    TextView amount = askLayout.getChildAt(i).findViewById(R.id.amount);
                    amount.setText(Exchange.coinString(book.asks.get(numLines - i - 1).totalQuantity, index, false, false));
                }
                else {
                    break;
                }
            }

            for (int i = 0; i < numLines; i++) {
                if (i < book.bids.size()) {
                    TextView price = bidLayout.getChildAt(i).findViewById(R.id.price);
                    price.setText(Exchange.fiatString(book.bids.get(i).price, false, false, false));
                    TextView amount = bidLayout.getChildAt(i).findViewById(R.id.amount);
                    amount.setText(Exchange.coinString(book.bids.get(i).totalQuantity, index, false, false));
                }
                else {
                    break;
                }
            }
        }
    }



    public void updateTicker(float price) {
        if(bookDrawn) {
            ticker.setText(Exchange.fiatString(price, true, true, false));
        }
    }
}
