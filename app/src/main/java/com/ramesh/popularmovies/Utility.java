package com.ramesh.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by RAMESH on 10-04-2016.
 */
public class Utility {
    public static String API_MOVIE_BASE_URL = "http://api.themoviedb.org/3/";
    public static String API_MOVIE_DB_CONFIG_URL = API_MOVIE_BASE_URL + "configuration";
    public static String API_MOVIE_DETAILS_URL = API_MOVIE_BASE_URL + "movie/";
    public static String API_POPULAR_MOVIES_URL = API_MOVIE_DETAILS_URL + "popular";
    public static String API_TOP_RATED_MOVIES_URL = API_MOVIE_DETAILS_URL + "top_rated";
    public static String API_TRAILER_RESOURCE = "/videos";
    public static String API_REVIEW_RESOURCE = "/reviews";

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
