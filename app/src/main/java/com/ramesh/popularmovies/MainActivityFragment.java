package com.ramesh.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    public MainActivityFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = sp.getString(getString(R.string.pref_sort_by_key), getString(R.string.pref_sort_by_most_popular));

        String moviesAPIUri = "https://api.themoviedb.org/3/movie/popular";
        if(sortBy.equals(getString(R.string.pref_sort_by_rating))) {
            moviesAPIUri = "https://api.themoviedb.org/3/movie/top_rated";
        }

        new FetchMovieDB().execute(moviesAPIUri);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMovieAdapter = new MovieAdapter(getActivity());

        GridView movieGridView = (GridView)rootView.findViewById(R.id.movie_grid_view);
        movieGridView.setAdapter(mMovieAdapter);

        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailActivity = new Intent(getActivity(), DetailActivity.class);
                detailActivity.putExtra("movie", mMovieAdapter.getItem(position));
                startActivity(detailActivity);
            }
        });

        return rootView;
    }

    private class FetchMovieDB extends AsyncTask<String, Void, List<Movie>> {

        private List<Movie> getMovieDataFromJson (String moviesJson, String imageBaseUrl) {

            try {
                List<Movie> moviesList = new ArrayList<>();
                JSONObject jsonObject = new JSONObject(moviesJson);
                JSONArray moviesArray = jsonObject.getJSONArray("results");
                for (int index = 0 ; index < moviesArray.length(); index++) {
                    JSONObject movie = moviesArray.getJSONObject(index);
                    String title = movie.getString("original_title");
                    String releaseDate = movie.getString("release_date");
                    String overview = movie.getString("overview");
                    String posterUrl = imageBaseUrl + movie.getString("poster_path");
                    double rating = movie.getDouble("vote_average");

                    moviesList.add(new Movie(title, posterUrl, overview, rating, releaseDate));
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
                JSONArray posterSizeArray = imagesObject.getJSONArray("poster_sizes");
                String size = posterSizeArray.getString(0);
                return baseUrl + size;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected List<Movie> doInBackground(String... params) {
            Uri configAPIResource = Uri.parse("http://api.themoviedb.org/3/configuration").buildUpon()
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
                Log.i(LOG_TAG, "There are no movies to show");
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

            if (convertView == null) {
                imageView = new ImageView(ctx);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            }
            else
            {
                imageView = (ImageView) convertView;
            }
            Picasso.with(getActivity())
                    .load(movieList.get(position).getImageUrl())
                    .placeholder(R.drawable.ic_launcher)
                    .into(imageView);

            return imageView;
        }
    }
}
