package com.ramesh.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ramesh.popularmovies.data.MovieContract;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private Movie movie = null;
    private ShareActionProvider mShareActionProvider = null;
    private String mTrailerUrl = null;

    private LinearLayout movieTrailersView;
    private LinearLayout movieReviewsView;
    private ImageView posterIV;
    private TextView titleTV;
    private TextView ratingTV;
    private TextView releaseDateTV;
    private TextView overviewTV;
    private TextView durationTV;
    private Button favouriteButton;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if(mTrailerUrl != null)
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        else
            mShareActionProvider.setShareIntent(null);
    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }else{
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTrailerUrl);
        return shareIntent;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle args = getArguments();
        if(args != null)
            movie = (Movie)args.getSerializable("movie");

        movieTrailersView = (LinearLayout) rootView.findViewById(R.id.trailer_list_view);
        movieReviewsView = (LinearLayout) rootView.findViewById(R.id.reviews_list_view);
        posterIV = (ImageView)rootView.findViewById(R.id.movie_poster);
        titleTV = (TextView)rootView.findViewById(R.id.movie_title);
        ratingTV = (TextView)rootView.findViewById(R.id.movie_rating);
        releaseDateTV = (TextView)rootView.findViewById(R.id.movie_year);
        overviewTV = (TextView)rootView.findViewById(R.id.movie_overview);
        durationTV = (TextView)rootView.findViewById(R.id.movie_duration);
        favouriteButton = (Button)rootView.findViewById(R.id.button_favourite);

        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues cv = new ContentValues();

                cv.put(MovieContract.FavouriteEntry._ID, movie.getId());
                cv.put(MovieContract.FavouriteEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
                cv.put(MovieContract.FavouriteEntry.COLUMN_OVERVIEW, movie.getOverview());
                cv.put(MovieContract.FavouriteEntry.COLUMN_POSTER_PATH, movie.getImageUrl());
                cv.put(MovieContract.FavouriteEntry.COLUMN_RATING, movie.getRating());
                cv.put(MovieContract.FavouriteEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                cv.put(MovieContract.FavouriteEntry.COLUMN_DURATION, movie.getDuration());

                try {
                    getActivity().getContentResolver().insert(MovieContract.FavouriteEntry.CONTENT_URI, cv);
                    favouriteButton.setClickable(false);
                    favouriteButton.setEnabled(false);
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), "Error in adding movie to favourites list. Please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mTrailerUrl = null;
        updateDetailedUI();

        return rootView;
    }

    private void updateDetailedUI() {
        // URL to share is not know at this point
        if(mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(null);
        }

        movieTrailersView.removeAllViews();
        movieReviewsView.removeAllViews();

        if(movie == null)
            return;

        favouriteButton.setClickable(true);
        favouriteButton.setEnabled(true);

        try {
            Cursor cur = getActivity().getContentResolver().query(MovieContract.FavouriteEntry.buildFavouriteMoviesUri(movie.getId()),
                    null, null, null, null, null);
            if(cur != null) {
                if(cur.getCount() != 0) {
                    favouriteButton.setClickable(false);
                    favouriteButton.setEnabled(false);
                }
                cur.close();
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error in querying db, reason: " + ex.getMessage() );
        }

        if(movie.getDuration() == -1)
            new UpdateMovieDuration().execute(Long.toString(movie.getId()));
        else
            durationTV.setText(movie.getDuration() + "min");

        titleTV.setText(movie.getTitle());
        ratingTV.setText(movie.getRating() + "/10");
        releaseDateTV.setText(movie.getReleaseDate().split("-")[0]);
        overviewTV.setText(movie.getOverview());

        Picasso.with(getActivity())
                .load(movie.getImageUrl())
                .placeholder(R.drawable.ic_launcher)
                .into(posterIV);
        if(Utility.isNetworkAvailable(getActivity())) {
            new PopulateTrailers().execute();
            new PopulateReviews().execute();
        }
        else
            Toast.makeText(getActivity(), "No internet connection. Unable to fetch trailers and reviews.", Toast.LENGTH_SHORT).show();
    }

    private class UpdateMovieDuration extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Uri configAPIResource = Uri.parse(Utility.API_MOVIE_DETAILS_URL + params[0]).buildUpon()
                    .appendQueryParameter("api_key", BuildConfig.APP_KEY).build();

            OkHttpClient httpClient = new OkHttpClient();
            try {
                String responseString = null;
                Request request = new Request.Builder().url(configAPIResource.toString()).build();
                Response response = httpClient.newCall(request).execute();
                responseString = response.body().string();

                JSONObject responseObj = new JSONObject(responseString);
                return responseObj.getInt("runtime");

            } catch (IOException e) {
                Log.e(LOG_TAG, "error in fetching movie details");
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer s) {
            super.onPostExecute(s);
            String result;
            if(s == null || s == -1) {
                result = "N/A";
                movie.setDuration(-1);
            } else {
                result = s + "min";
                movie.setDuration(s);
            }
            durationTV.setText(result);
        }
    }

    private class PopulateTrailers extends AsyncTask<Void, Void, List<String>> {
        LayoutInflater inflater;

        PopulateTrailers() {
            inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View v = inflater.inflate(R.layout.movie_trailer_item, null);
            v.findViewById(R.id.icon_play).setVisibility(View.GONE);
            ((TextView)v.findViewById(R.id.trailer_name)).setText("Fetching trailers, Please wait...");
            movieTrailersView.addView(v);
        }

        List<String> parseTrailersLinksFromJson(String inputJson) {
            if(inputJson == null) return null;
            List<String> trailersList = new ArrayList<>();
            try {
                JSONObject responseObj = new JSONObject(inputJson);
                JSONArray resultsArray = responseObj.getJSONArray("results");
                for (int index = 0; index < resultsArray.length(); index++) {
                    JSONObject tempTrailer = resultsArray.getJSONObject(index);
                    if(tempTrailer.getString("type").equals("Trailer"))
                        trailersList.add("http://www.youtube.com/watch?v=" + tempTrailer.getString("key"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return trailersList;
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            Uri configAPIResource = Uri.parse(Utility.API_MOVIE_DETAILS_URL + Long.toString(movie.getId()) + Utility.API_TRAILER_RESOURCE).buildUpon()
                    .appendQueryParameter("api_key", BuildConfig.APP_KEY).build();

            OkHttpClient httpClient = new OkHttpClient();
            String responseString = null;
            try {
                Request request = new Request.Builder().url(configAPIResource.toString()).build();
                Response response = httpClient.newCall(request).execute();
                responseString = response.body().string();
            } catch (IOException e) {
                Log.e(LOG_TAG, "error in reading trailers");
            }
            return parseTrailersLinksFromJson(responseString);
        }

        @Override
        protected void onPostExecute(List<String> trailerList) {
            super.onPostExecute(trailerList);

            movieTrailersView.removeAllViews();

            if(trailerList == null || trailerList.size() == 0) {
                View v = inflater.inflate(R.layout.movie_trailer_item, null);
                v.findViewById(R.id.icon_play).setVisibility(View.GONE);
                ((TextView)v.findViewById(R.id.trailer_name)).setText("There are no trailers available");
                movieTrailersView.addView(v);
                return;
            }
            mTrailerUrl = trailerList.get(0);
            if(mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareTrailerIntent());
            }

            for (int index = 0; index < trailerList.size(); index++ ) {
                View v = inflater.inflate(R.layout.movie_trailer_item, null);
                TextView trailerName = (TextView)v.findViewById(R.id.trailer_name);
                trailerName.setText("Trailer " + (index + 1));
                final String trailerURL = trailerList.get(index);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerURL));
                            getActivity().startActivity(intent);
                        }catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(getActivity(), "Unable to play Trailer", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                movieTrailersView.addView(v, index);
            }
        }
    }

    private class PopulateReviews extends AsyncTask<Void, Void, List<String>> {
        private LayoutInflater inflater;

        PopulateReviews() {
            inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View v = inflater.inflate(R.layout.movie_review_item, null);
            ((TextView)v.findViewById(R.id.review_name)).setText("Fetching trailers, Please wait...");
            movieReviewsView.addView(v);
        }

        List<String> parseTrailersLinksFromJson(String inputJson) {
            if(inputJson == null) return null;
            List<String> trailersList = new ArrayList<>();
            try {
                JSONObject responseObj = new JSONObject(inputJson);
                JSONArray resultsArray = responseObj.getJSONArray("results");
                for (int index = 0; index < resultsArray.length(); index++) {
                    JSONObject tempTrailer = resultsArray.getJSONObject(index);
                    trailersList.add(tempTrailer.getString("content"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return trailersList;
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            Uri configAPIResource = Uri.parse(Utility.API_MOVIE_DETAILS_URL + Long.toString(movie.getId()) + Utility.API_REVIEW_RESOURCE).buildUpon()
                    .appendQueryParameter("api_key", BuildConfig.APP_KEY).build();

            OkHttpClient httpClient = new OkHttpClient();
            String responseString = null;
            try {
                Request request = new Request.Builder().url(configAPIResource.toString()).build();
                Response response = httpClient.newCall(request).execute();
                responseString = response.body().string();
            } catch (IOException e) {
                Log.e(LOG_TAG, "error in reading reviews");
                e.printStackTrace();
            }
            return parseTrailersLinksFromJson(responseString);
        }

        @Override
        protected void onPostExecute(List<String> trailerList) {
            super.onPostExecute(trailerList);

            movieReviewsView.removeAllViews();

            if(trailerList == null || trailerList.size() == 0) {
                Log.e(LOG_TAG, "There are no reviews");
                View v = inflater.inflate(R.layout.movie_review_item, null);
                ((TextView)v.findViewById(R.id.review_name)).setText("There are no reviews available");
                movieReviewsView.addView(v);
                return;
            }

            for (int index = 0; index < trailerList.size(); index++ ) {
                View v = inflater.inflate(R.layout.movie_review_item, null);
                TextView trailerName = (TextView)v.findViewById(R.id.review_name);
                trailerName.setText(trailerList.get(index));
                movieReviewsView.addView(v, index);
            }
        }
    }

    public void updateSelectedMovie(Movie movie) {
        this.movie = movie;
        updateDetailedUI();
    }
}
