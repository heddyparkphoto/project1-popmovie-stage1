package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
                        mParcel.putParcelable(DetailActivity.DetailFragment.MOVIE_PARCEL, mm);

                        intent.putExtra(DetailActivity.DetailFragment.INTENT_PARCEL, mParcel);
                        startActivity(intent);
                    }
                }
        );
        return rootView;
    }

    /*
          -- Udacity note on using Picasso to load movie thumbs into ImageView instance

          "You can use Picasso to easily load album art thumbnails into your views using:
          Picasso.with(context).load("http://i.imgur.com/DvpvklR.png").into(imageView);
          Picasso will handle loading the images on a background thread,
          image decompression and caching the images."

        */
    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected Movie[] doInBackground(String... optionBy) {


            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                // Utilize Uri helper class and set new BASE URL - Refer to the answer about the difference between two URLs
                // on the movie api site "/movie/popular may return the same result, but /discover/movie? offers slew of filters.."
                final String MOVIES_API_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String MY_KEY_PARAM = "api_key";
                final String MY_KEY = BuildConfig.THE_MOVIE_DB_API_KEY;  //CAUTION!!!! DO NOT DISTRIBUTE THE KEY TO PUBLIC@@@@@@

                Uri builtUri = Uri.parse(MOVIES_API_BASE_URL).buildUpon()
                        .appendPath(optionBy[0])
                        .appendQueryParameter(MY_KEY_PARAM, MY_KEY)
                        .build();
                URL url;
                url = new URL(builtUri.toString());

                // Create the request to themoviedb api, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } catch (Exception e) {
                Log.e(LOG_TAG, "Other Exception ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            //do Movie data parsing
            try {
                return getMovieDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            if (movies != null) {
                mMoviePosterAdapter.clear();
                mMoviePosterAdapter.addAll(movies);
            }
        }

        private Movie[] getMovieDataFromJson(String jsonStr) throws JSONException {

            final String KEY_RESULTS = "results";
            final String KEY_ID = "id";
            final String KEY_POSTER_PATH = "poster_path";
            final String KEY_TITLE = "title";
            final String KEY_PLOT = "overview";
            final String KEY_RATING_PART1 = "vote_average";
            final String KEY_RATING_PART2 = "vote_count";
            final String KEY_RELEASE_DATE = "release_date";

            final String W185 = "w185"; // Recommendation of the SIZE value for a phone

            //build per instruction for absolute url  http://image.tmdb.org/t/p/size..value..here/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
            final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
            Uri uriToBuild = Uri.parse(POSTER_BASE_URL).buildUpon()
                    .appendEncodedPath(W185)
                    .build();

            String uriAsString = uriToBuild.toString();

            JSONObject obj = new JSONObject(jsonStr);
            JSONArray resultArray = obj.getJSONArray(KEY_RESULTS);

            JSONObject movie;
            Movie[] movieArray;

            int size = resultArray.length();
            String postersource = "";
            movieArray = new Movie[size];

            for (int i = 0; i < size; i++) {
                movie = (JSONObject) resultArray.get(i);
                postersource = movie.getString(KEY_POSTER_PATH);
                movieArray[i] = new Movie();
                movieArray[i].id = movie.getString(KEY_ID);
                movieArray[i].overview = movie.getString(KEY_PLOT);
                movieArray[i].posterpath = uriAsString.concat(postersource);
                movieArray[i].title = movie.getString(KEY_TITLE);
                movieArray[i].rating = movie.getString(KEY_RATING_PART1) + " / " + (movie.getString(KEY_RATING_PART2));
                movieArray[i].releasedate = movie.getString(KEY_RELEASE_DATE);
            }

            return movieArray;
        }
    }

    private void updateAwesomeMovies(){

        // Preference sort option from SharedPreferences
        String prefSortOption;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefSortOption = sharedPreferences.getString(
                getString(R.string.pref_sort_by_key), getString(R.string.pref_default_sort_by));

        if (isOnLine()) {
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            // sort by user Settings preference
            fetchMoviesTask.execute(prefSortOption);
        } else {
            Toast.makeText(getActivity(),
                    "No network connection.  Could not load a new set.  Please check your network connection.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean isOnLine(){

        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return (netInfo != null && netInfo.isConnectedOrConnecting());
    }
}
