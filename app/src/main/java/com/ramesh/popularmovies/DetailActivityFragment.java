package com.ramesh.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ImageView posterIV = (ImageView)rootView.findViewById(R.id.poster);
        TextView titleTV = (TextView)rootView.findViewById(R.id.movie_title);
        TextView ratingTV = (TextView)rootView.findViewById(R.id.movie_rating);
        TextView releaseDateTV = (TextView)rootView.findViewById(R.id.release_date);
        TextView overviewTV = (TextView)rootView.findViewById(R.id.overview);

        Intent intent = getActivity().getIntent();
        Movie movie = (Movie)intent.getSerializableExtra("movie");

        titleTV.setText(movie.getTitle());
        ratingTV.setText(""+movie.getRating());
        releaseDateTV.setText(movie.getReleaseDate());
        overviewTV.setText(movie.getOverview());

        Picasso.with(getActivity())
                .load(movie.getImageUrl())
                .placeholder(R.drawable.ic_launcher)
                .into(posterIV);

        return rootView;
    }
}
