package com.maxtechnologies.cryptomax.api;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Colman on 02/04/2018.
 */

public class Article implements Serializable {
    private int id;
    private String source;
    private String headline;
    private Bitmap image;
    private String author;
    private Date date;
    private String body;
    private int views;
    private int votes;
    private int myVote;


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



    public int getId() {
        return id;
    }



    public void setId(int id) {
        this.id = id;
    }



    public String getSource() {
        return source;
    }



    public void setSource(String source) {
        this.source = source;
    }



    public String getHeadline() {
        return headline;
    }



    public void setHeadline(String headline) {
        this.headline = headline;
    }



    public Bitmap getImage() {
        return image;
    }



    public void setImage(Bitmap image) {
        this.image = image;
    }



    public String getAuthor() {
        return author;
    }



    public void setAuthor(String author) {
        this.author = author;
    }



    public Date getDate() {
        return date;
    }



    public void setDate(Date date) {
        this.date = date;
    }



    public String getBody() {
        return body;
    }



    public void setBody(String body) {
        this.body = body;
    }



    public int getViews() {
        return views;
    }



    public void setViews(int views) {
        this.views = views;
    }



    public int getVotes() {
        return votes;
    }



    public void setVotes(int votes) {
        this.votes = votes;
    }



    public int getMyVote() {
        return myVote;
    }



    public void setMyVote(int myVote) {
        this.myVote = myVote;
    }
}
