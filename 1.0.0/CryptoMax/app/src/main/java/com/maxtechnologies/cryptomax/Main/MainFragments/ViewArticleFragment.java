package com.maxtechnologies.cryptomax.Main.MainFragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Objects.Article;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Colman on 02/04/2018.
 */

public class ViewArticleFragment extends Fragment {

    //Article declaration
    private Article article;

    //UI declarations
    private int previousVote;
    private ImageView upArrow;
    private TextView votes;
    private ImageView downArrow;


    public static ViewArticleFragment newInstance(Article article) {
        ViewArticleFragment fragment = new ViewArticleFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("ARTICLE", article);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_view_article, container, false);

        Bundle bundle = getArguments();
        article = (Article) bundle.getSerializable("ARTICLE");

        //UI definitions
        previousVote = article.myVote;
        upArrow = (ImageView) rootView.findViewById(R.id.up_arrow);
        votes = (TextView) rootView.findViewById(R.id.vote_count);
        downArrow = (ImageView) rootView.findViewById(R.id.down_arrow);
        ImageView image = (ImageView) rootView.findViewById(R.id.image);
        TextView headline = (TextView) rootView.findViewById(R.id.headline);
        TextView author = (TextView) rootView.findViewById(R.id.author);
        TextView date = (TextView) rootView.findViewById(R.id.date);
        ImageView eye = (ImageView) rootView.findViewById(R.id.eye);
        TextView views = (TextView) rootView.findViewById(R.id.view_count);
        TextView body = (TextView) rootView.findViewById(R.id.body);


        //Set the UI values
        if(article.image != null) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            image.setLayoutParams(new ConstraintLayout.LayoutParams(displaymetrics.widthPixels, (int) (displaymetrics.heightPixels * 0.4f)));
            image.setImageBitmap(article.image);
        }
        setVoteUI(article.myVote);
        headline.setText(article.headline);
        author.setText(article.author);

        if(Settings.theme == 0) {
            eye.setColorFilter(Color.parseColor("#000000"));
        }
        else {
            eye.setColorFilter(Color.parseColor("#FFFFFF"));
        }
        views.setText(String.valueOf(article.views));

        SimpleDateFormat format;
        if(Settings.times == 0) {
             format = new SimpleDateFormat("EEE MMM d h:mm a", Locale.US);
        }
        else {
            format = new SimpleDateFormat("EEE MMM d HH:mm", Locale.US);
        }
        date.setText(format.format(article.date));

        Document document = Jsoup.parse(article.body);
        Elements elements = document.body().children().get(0).children();
        Element element = null;
        for(Element e : elements) {
            if(e.tag().getName().equals("script")) {
                element = e;
            }
        }
        if(element != null) {
            elements.remove(element);
        }
        body.setText(Html.fromHtml(elements.toString()));


        //Send the view for the article
        CryptoMaxApi.addView(article.id);


        //Setup the listener for the up arrow
        upArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeVote(1, true);
                sendVote();
            }
        });


        //Setup the listener for the down arrow
        downArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeVote(-1, true);
                sendVote();
            }
        });


        return rootView;
    }



    private void changeVote(int newVote, boolean showToast) {
        if(newVote == -1) {
            if(article.myVote == -1) {
                article.votes += 1;
                article.myVote = 0;

                if(showToast) {
                    Toast newToast = Toast.makeText(getContext(),
                            getContext().getResources().getString(R.string.vote_removed_message),
                            Toast.LENGTH_LONG);
                    newToast.show();
                }
            }

            else if(article.myVote == 0) {
                article.votes -= 1;
                article.myVote = -1;

                if(showToast) {
                    Toast newToast = Toast.makeText(getContext(),
                            getContext().getResources().getString(R.string.down_vote_message),
                            Toast.LENGTH_LONG);
                    newToast.show();
                }
            }

            else if(article.myVote == 1) {
                article.votes -= 2;
                article.myVote = -1;

                if(showToast) {
                    Toast newToast = Toast.makeText(getContext(),
                            getContext().getResources().getString(R.string.down_vote_message),
                            Toast.LENGTH_LONG);
                    newToast.show();
                }
            }
        }

        else if(newVote == 0) {
            if(article.myVote == -1) {
                article.votes += 1;
                article.myVote = 0;
            }

            else if(article.myVote == 1){
                article.votes -= 1;
                article.myVote = 0;
            }
        }

        else if(newVote == 1) {
            if(article.myVote == -1) {
                article.votes += 2;
                article.myVote = 1;

                if(showToast) {
                    Toast newToast = Toast.makeText(getContext(),
                            getContext().getResources().getString(R.string.up_vote_message),
                            Toast.LENGTH_LONG);
                    newToast.show();
                }
            }

            else if(article.myVote == 0) {
                article.votes += 1;
                article.myVote = 1;

                if(showToast) {
                    Toast newToast = Toast.makeText(getContext(),
                            getContext().getResources().getString(R.string.up_vote_message),
                            Toast.LENGTH_SHORT);
                    newToast.show();
                }
            }

            else if(article.myVote == 1) {
                article.votes -= 1;
                article.myVote = 0;

                if(showToast) {
                    Toast newToast = Toast.makeText(getContext(),
                            getContext().getResources().getString(R.string.vote_removed_message),
                            Toast.LENGTH_SHORT);
                    newToast.show();
                }
            }
        }

        setVoteUI(article.myVote);
    }



    private void setVoteUI(int vote) {
        if(vote == -1) {
            upArrow.setColorFilter(Color.parseColor("#858C93"));
            downArrow.setColorFilter(getResources().getColor(R.color.colorAccent));
        }

        else if(vote == 0) {
            upArrow.setColorFilter(Color.parseColor("#858C93"));
            downArrow.setColorFilter(Color.parseColor("#858C93"));
        }

        else if(vote == 1) {
            upArrow.setColorFilter(getResources().getColor(R.color.colorAccent));
            downArrow.setColorFilter(Color.parseColor("#858C93"));
        }

        votes.setText(String.valueOf(article.votes));

        if(Settings.theme == 0) {
            if(vote != 0) {
                votes.setTextColor(getResources().getColor(R.color.colorAccent));
            }

            else {
                votes.setTextColor(Color.parseColor("#000000"));
            }
        }

        else {
            if(vote != 0) {
                votes.setTextColor(getResources().getColor(R.color.colorDraculaAccent));
            }

            else {
                votes.setTextColor(Color.parseColor("#FFFFFF"));
            }
        }
    }



    private void sendVote() {
        CryptoMaxApi.addVote(article.id, article.myVote);
        previousVote = article.myVote;
    }
}
