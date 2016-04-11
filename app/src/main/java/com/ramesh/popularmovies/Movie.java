package com.ramesh.popularmovies;

import java.io.Serializable;

/**
 * Created by RAMESH on 31-03-2016.
 */
public class Movie implements Serializable{
    private long id;
    private String title;
    private String imageUrl;
    private String overview;
    private double rating;
    private String releaseDate;
    private int duration;

    public Movie(long id, String title, String imageUrl, String overview, double rating, String releaseDate, int duration) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getOverview() {
        return overview;
    }

    public double getRating() {
        return rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }
}
