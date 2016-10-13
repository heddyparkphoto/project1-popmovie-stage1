package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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
    private Movie mMovie;
    private static final String MOVIE_FRAG_PARCEL = "MOVIE_FRAG_PARCEL";
    private boolean isTwoPane;

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
                        mMovie = mMoviePosterAdapter.getItem(position);

                        ((OnMainMovieItemSelectedListener) getActivity()).OnMainMovieItemClick(mMovie);
                    }
                }
        );

       return rootView;
    }

    private void updateAwesomeMovies(){

        // Preference sort option from SharedPreferences
        String prefSortOption = Utility.getPreferredSortOption(getActivity());
        FetchMoviesTask fetchMoviesTask;
        Movie[] defaultMovieArray = null;

       if (getContext().getString(R.string.favorites).compareTo(prefSortOption) != 0) {
           try {
               if (isOnLine()) {

                   defaultMovieArray = new FetchMoviesTask(getActivity(), mMoviePosterAdapter).execute(prefSortOption).get();
               } else {
                   Toast.makeText(getActivity(),
                           "No network connection.  Could not load a new set.  Please check your network connection.",
                           Toast.LENGTH_LONG).show();
               }
           } catch (Exception allEx){
               Log.e(LOG_TAG, "Exceptions during AsyncTask " + allEx.getMessage());
           }
        }

        if (isTwoPane) {
            if (mMovie == null && defaultMovieArray != null && defaultMovieArray.length > 0) {
                // user hasn't started yet, default to the first movie
                mMovie = defaultMovieArray[0];
                ((OnMainMovieItemSelectedListener) getActivity()).OnMainMovieItemClick(mMovie);
            }
        }
    }

    private boolean isOnLine(){

        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }

    public void setTwoPane(boolean twoPane) {
        isTwoPane = twoPane;
    }

    /*
        Movie DetailFragment Callback for when an item has been selected.
     */
    public interface OnMainMovieItemSelectedListener {

        public void OnMainMovieItemClick(Movie movieItem);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMovie!=null){
            outState.putParcelable(MOVIE_FRAG_PARCEL, mMovie);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState!=null){
            mMovie = savedInstanceState.getParcelable(MOVIE_FRAG_PARCEL);
        }
    }
}
