package com.ramesh.popularmovies;

import java.io.Serializable;

/**
 * Created by RAMESH on 31-03-2016.
 */
public class Movie implements Serializable{
    private String title;
    private String imageUrl;
    private String overview;
    private double rating;
    private String releaseDate;

    public Movie(String title, String imageUrl, String overview, double rating, String releaseDate) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
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
