package com.maxtechnologies.cryptomax.ui.drawer.news;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.api.Article;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.misc.Settings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Created by Colman on 02/04/2018.
 */

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    //Adapter declarations
    private FragmentActivity activity;
    public ArrayList<Article> articleArrayList;


    public ArticleAdapter(FragmentActivity activity, ArrayList<Article> articleArrayList) {
        this.activity = activity;
        this.articleArrayList = articleArrayList;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View articleView = inflater.inflate(R.layout.article_entry, parent, false);

        return new ViewHolder(articleView, this);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Article article = articleArrayList.get(position);

        if(article.image != null) {
            holder.image.setImageBitmap(article.image);
            holder.image.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        else {
            Drawable drawable = activity.getResources().getDrawable(activity.getResources().getIdentifier(
                    "full_logo", "drawable", activity.getPackageName()));
            holder.image.setImageDrawable(drawable);
        }

        holder.headline.setText(article.headline);
        Drawable drawable = activity.getResources().getDrawable(activity.getResources().getIdentifier(
                article.source.toLowerCase(), "drawable", activity.getPackageName()));
        holder.logo.setImageDrawable(drawable);
        holder.name.setText(article.source);

        long currTime = System.currentTimeMillis();
        long articleTime = article.date.getTime();
        long diffTime = (currTime - articleTime) / 1000;
        SimpleDateFormat format;
        String extra = "";
        if(diffTime >= 365L * 24 * 60 * 60) {
            format = new SimpleDateFormat("yyyy", Locale.US);
        }
        else if(diffTime >= 7L * 24 * 60 * 60 && diffTime < 365L * 24 * 60 * 60) {
            format = new SimpleDateFormat("MMM d", Locale.US);
        }
        else {
            Calendar currCalendar = Calendar.getInstance();
            currCalendar.setTime(new Date());
            Calendar articleCalendar = Calendar.getInstance();
            articleCalendar.setTime(article.date);

            int daysAgo = currCalendar.get(Calendar.DAY_OF_WEEK) - articleCalendar.get(Calendar.DAY_OF_WEEK);
            if (daysAgo < 0) {
                daysAgo += 7;
            }

            if (daysAgo == 0 || daysAgo == 1) {
                if (daysAgo == 0) {
                    extra = "Today ";

                }
                else {
                    extra = "Yesterday ";
                }

                if (Settings.times == 0) {
                    format = new SimpleDateFormat("h:mm a", Locale.US);
                }
                else {
                    format = new SimpleDateFormat("HH:mm", Locale.US);
                }
            }
            else {
                if (Settings.times == 0) {
                    format = new SimpleDateFormat("EEE h:mm a", Locale.US);
                }
                else {
                    format = new SimpleDateFormat("EEE HH:mm", Locale.US);
                }
            }
        }
        holder.date.setText(extra + format.format(article.date));
    }



    @Override
    public int getItemCount() {
        return articleArrayList.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ArticleAdapter adapter;
        public ImageView image;
        public TextView headline;
        public ImageView logo;
        public TextView name;
        public TextView date;

        public ViewHolder(View itemView, ArticleAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            image = (ImageView) itemView.findViewById(R.id.image);
            headline = (TextView) itemView.findViewById(R.id.headline);
            logo = (ImageView) itemView.findViewById(R.id.logo);
            name = (TextView) itemView.findViewById(R.id.name);
            date = (TextView) itemView.findViewById(R.id.date);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Article article = adapter.articleArrayList.get(position);

            FragmentManager fragmentManager = adapter.activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ViewArticleFragment fragment = ViewArticleFragment.newInstance(article);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.content, fragment);
            fragmentTransaction.commit();
        }
    }
}
