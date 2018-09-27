package com.maxtechnologies.cryptomax.Objects;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Colman on 02/04/2018.
 */

public class Article implements Serializable {
    public int id;
    public String source;
    public String headline;
    public Bitmap image;
    public String author;
    public Date date;
    public String body;
    public int views;
    public int votes;
    public int myVote;


    public Article(int id, String source, String headline, Bitmap image, String author, Date date, String body, int views, int votes, int myVote) {
        this.id = id;
        this.source = source;
        this.headline = headline;
        this.image = image;
        this.author = author;
        this.date = date;
        this.body = body;
        this.views = views;
        this.votes = votes;
        this.myVote = myVote;
    }
}
