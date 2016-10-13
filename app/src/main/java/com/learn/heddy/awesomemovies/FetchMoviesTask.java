package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hyeryungpark on 9/4/16.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private ArrayAdapter<Movie> mMoviePosterAdapter;
    private Context mContext;

    public FetchMoviesTask(Context context, ArrayAdapter<Movie> arrayAdapter) {
        mContext = context;
        mMoviePosterAdapter = arrayAdapter;
    }

    @Override
    protected Movie[] doInBackground(String... optionBy) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        try {

            final String MY_KEY_PARAM = "api_key";
            final String MY_KEY = BuildConfig.THE_MOVIE_DB_API_KEY;  //CAUTION!!!! DO NOT DISTRIBUTE THE KEY TO PUBLIC@@@@@@

            Uri.Builder builder = new Uri.Builder();

            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(optionBy[0])
                    .appendQueryParameter(MY_KEY_PARAM, MY_KEY);

            URL url;
            url = new URL(builder.build().toString());

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
            Log.e(LOG_TAG, "Error "+ e.toString(), e);
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
        Uri.Builder builder = new Uri.Builder();

        builder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendEncodedPath(W185);

        String uriAsString = builder.build().toString();

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

