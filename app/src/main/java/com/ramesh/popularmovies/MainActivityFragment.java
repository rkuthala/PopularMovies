package com.ramesh.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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
public class MainActivityFragment extends Fragment {
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private MovieAdapter mMovieAdapter;
    private GridView mMovieGridView;
    private int mPosition = GridView.INVALID_POSITION;
    private static final String POSITION_KEY = "selected_position";

    public interface  Callback {
        public void onItemSelected (Movie movie);
    }

    public MainActivityFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = sp.getString(getString(R.string.pref_sort_by_key), getString(R.string.pref_sort_by_most_popular));

        if(sortBy.equals(getString(R.string.pref_sort_by_favourites))) {
            fetchFavouriteMovies();
        } else {

            if(Utility.isNetworkAvailable(getActivity())) {
                String moviesAPIUri = Utility.API_POPULAR_MOVIES_URL;
                if(sortBy.equals(getString(R.string.pref_sort_by_rating))) {
                    moviesAPIUri = Utility.API_TOP_RATED_MOVIES_URL;
                }

                new FetchMovieDB().execute(moviesAPIUri);
            } else {
                Toast.makeText(getActivity(), "Unable to connect to internet. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition != GridView.INVALID_POSITION)
            outState.putInt(POSITION_KEY, mPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMovieAdapter = new MovieAdapter(getActivity());

        mMovieGridView = (GridView)rootView.findViewById(R.id.movie_grid_view);
        mMovieGridView.setAdapter(mMovieAdapter);

        mMovieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Callback) getActivity()).onItemSelected(mMovieAdapter.getItem(position));
                mPosition = position;
            }
        });

        if(savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
        }

        return rootView;
    }

    private void fetchFavouriteMovies() {

        final String[] FAVOURITE_COLUMNS = {
                MovieContract.FavouriteEntry._ID,
                MovieContract.FavouriteEntry.COLUMN_MOVIE_TITLE,
                MovieContract.FavouriteEntry.COLUMN_POSTER_PATH,
                MovieContract.FavouriteEntry.COLUMN_OVERVIEW,
                MovieContract.FavouriteEntry.COLUMN_RATING,
                MovieContract.FavouriteEntry.COLUMN_RELEASE_DATE,
                MovieContract.FavouriteEntry.COLUMN_DURATION
        };
        final int COL_ID = 0;
        final int COL_MOVIE_TITLE = 1;
        final int COL_POSTER_PATH = 2;
        final int COL_OVERVIEW = 3;
        final int COL_RATING = 4;
        final int COL_RELEASE_YEAR = 5;
        final int COL_DURATION = 6;

        ArrayList<Movie> moviesList = new ArrayList<>();
        try{
            Cursor cur = getActivity().getContentResolver().query(MovieContract.FavouriteEntry.CONTENT_URI, FAVOURITE_COLUMNS, null, null, null);
            if(cur != null && cur.moveToFirst()) {
                do {
                    long id = cur.getLong(COL_ID);
                    String title = cur.getString(COL_MOVIE_TITLE);
                    String releaseDate = cur.getString(COL_RELEASE_YEAR);
                    String overview = cur.getString(COL_OVERVIEW);
                    String posterUrl = cur.getString(COL_POSTER_PATH);
                    double rating = cur.getDouble(COL_RATING);
                    int duration = cur.getInt(COL_DURATION);
                    moviesList.add(new Movie(id, title, posterUrl, overview, rating, releaseDate, duration));
                } while (cur.moveToNext());
                cur.close();
            }
            if(moviesList.size() == 0)
                Toast.makeText(getActivity(), "There are no movies in your favourites list", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error in querying db, reason: " + ex.getMessage());
            Toast.makeText(getActivity(), "Error in fetching favourites movies. Please try again later", Toast.LENGTH_SHORT).show();
        }
        mMovieAdapter.updateMoviesList(moviesList);
    }

    private class FetchMovieDB extends AsyncTask<String, Void, List<Movie>> {

        private List<Movie> getMovieDataFromJson (String moviesJson, String imageBaseUrl) {

            try {
                List<Movie> moviesList = new ArrayList<>();
                JSONObject jsonObject = new JSONObject(moviesJson);
                JSONArray moviesArray = jsonObject.getJSONArray("results");
                for (int index = 0 ; index < moviesArray.length(); index++) {
                    JSONObject movie = moviesArray.getJSONObject(index);
                    long id = movie.getLong("id");
                    String title = movie.getString("original_title");
                    String releaseDate = movie.getString("release_date");
                    String overview = movie.getString("overview");
                    String posterUrl = imageBaseUrl + movie.getString("poster_path");
                    double rating = movie.getDouble("vote_average");

                    moviesList.add(new Movie(id, title, posterUrl, overview, rating, releaseDate, -1)); // Duration will be fetched on detailed view
                }

                return moviesList;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String getBaseUrlFromJson (String jsonInput) {
            try {
                JSONObject jsonObject = new JSONObject(jsonInput);
                JSONObject imagesObject = jsonObject.getJSONObject("images");
                String baseUrl = imagesObject.getString("base_url");
                return baseUrl + "w185";
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected List<Movie> doInBackground(String... params) {
            Uri configAPIResource = Uri.parse(Utility.API_MOVIE_DB_CONFIG_URL).buildUpon()
                    .appendQueryParameter("api_key", BuildConfig.APP_KEY).build();

            Uri popularMoviesAPIResource = Uri.parse(params[0]).buildUpon()
                    .appendQueryParameter("api_key", BuildConfig.APP_KEY).build();

            OkHttpClient httpClient = new OkHttpClient();

            String responseString = null;
            String imageBaseUrl = null;
            try {
                Request request = new Request.Builder().url(configAPIResource.toString()).build();
                Response response = httpClient.newCall(request).execute();
                responseString = response.body().string();
                imageBaseUrl = getBaseUrlFromJson(responseString);

                request = new Request.Builder().url(popularMoviesAPIResource.toString()).build();
                response = httpClient.newCall(request).execute();
                responseString = response.body().string();

                return getMovieDataFromJson(responseString, imageBaseUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            if(movies != null) {
                mMovieAdapter.updateMoviesList(movies);
            } else {
                Toast.makeText(getActivity(), "No movies to show. Please try after some time", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class MovieAdapter extends BaseAdapter {

        private Context ctx;
        private List<Movie> movieList;

        MovieAdapter(Context ctx) {
            this.ctx = ctx;
            this.movieList = new ArrayList<>();
        }

        public void updateMoviesList(List<Movie> newMovieList) {
            movieList = newMovieList;
            notifyDataSetChanged();

            if(mPosition != GridView.INVALID_POSITION) {
                mMovieGridView.setSelection(mPosition);
                mMovieGridView.smoothScrollToPosition(mPosition);
            }
        }

        @Override
        public int getCount() {
            return movieList.size();
        }

        @Override
        public Movie getItem(int position) {
            return movieList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            View view;
            if (convertView == null) {
                view = LayoutInflater.from(ctx).inflate(R.layout.movie_item, parent, false);
                imageView = (ImageView)view.findViewById(R.id.movie_poster);
            } else {
                view = convertView;
                imageView = (ImageView) view.findViewById(R.id.movie_poster);
            }
            Picasso.with(ctx)
                    .load(movieList.get(position).getImageUrl())
                    .placeholder(R.drawable.ic_launcher)
                    .into(imageView);

            /*Glide.with(getActivity())
                    .load(movieList.get(position).getImageUrl())
//                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher)
//                    .crossFade()
                    .into(imageView);*/

            return view;
        }
    }
}
