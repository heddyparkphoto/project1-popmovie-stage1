package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by hyeryungpark on 8/27/16.
 */
public class MoviesFragment extends Fragment {

    private final String LOG_TAG = MoviesFragment.class.getSimpleName();

    private ArrayAdapter<Movie> mMoviePosterAdapter;

    public MoviesFragment() {

    }

    @Override
    public void onStart() {
        super.onStart();
        updateAwesomeMovies();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMoviePosterAdapter = new OneMovieAdapter(getActivity(), new ArrayList<Movie>());

        //Get the GridView from the fragment_main.xml so that the adapter can be set on it
        GridView gridView = (GridView)rootView.findViewById(R.id.gridview_movies);

        gridView.setAdapter(mMoviePosterAdapter);

        gridView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Movie mm = mMoviePosterAdapter.getItem(position);

                        //Set on Bundle the Parcelable Movie object
                        //Use Explicit Intent for Part 1 project
                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        Bundle mParcel = new Bundle();
                        mParcel.putParcelable(DetailFragment.MOVIE_PARCEL, mm);

                        intent.putExtra(DetailFragment.INTENT_PARCEL, mParcel);
                        startActivity(intent);
                    }
                }
        );
        return rootView;
    }

    private void updateAwesomeMovies(){

        // Preference sort option from SharedPreferences
        String prefSortOption = Utility.getPreferredSortOption(getActivity());

        if (getContext().getString(R.string.favorites).compareTo(prefSortOption) != 0) {
            if (isOnLine()) {
                FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(getActivity(), mMoviePosterAdapter);
                // sort by user Settings preference
                fetchMoviesTask.execute(prefSortOption);
            } else {
                Toast.makeText(getActivity(),
                        "No network connection.  Could not load a new set.  Please check your network connection.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isOnLine(){

        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    /*
        Movie DetailFragment Callback for when an item has been selected.
     */
    public interface OnMainMovieItemSelectedListener {

        public void OnMainMovieItemClick(Movie movieItem);


    }
}
