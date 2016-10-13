package com.learn.heddy.awesomemovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
 * Created by hyeryungpark on 9/15/16.
 */
public class FetchMovieExtras extends AsyncTask<String, Void, ArrayList<String>> {
    private final String LOG_TAG = FetchMovieExtras.class.getSimpleName();

    public String[] resultsArray;
    public ArrayList<String> returnList;

    @Override
    protected ArrayList<String> doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String extrasJsonStr = null;

        try {

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendEncodedPath(params[0])
                    .appendEncodedPath(params[1])
                    .appendQueryParameter("api_key", BuildConfig.THE_MOVIE_DB_API_KEY);

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
            extrasJsonStr = buffer.toString();
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
            getMovieExtrasFromJson(extrasJsonStr, params[1]); // params[1] is either videos or reviews
        } catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return returnList;
    }

    private ArrayList<String> getMovieExtrasFromJson(String extrasJsonStr, String name) throws JSONException {

        final String KEY_RESULTS = "results";

        if ("videos".compareToIgnoreCase(name) == 0){
            JSONObject jsonObject = new JSONObject(extrasJsonStr);
            JSONArray results = jsonObject.getJSONArray(KEY_RESULTS);

            //trailersName = new String[results.length()];
            resultsArray = new String[results.length()];
            returnList = new ArrayList<String>();

            for (int i=0; i < results.length(); i++){
                JSONObject n = results.getJSONObject(i);
                String checksite = n.getString("site");
                String checktype = n.getString("type");
                if ((null != checksite && "youtube".equalsIgnoreCase(checksite))
                        && (null != checktype && checktype.toLowerCase().indexOf("trailer") >= 0))
                {
                    // Format a String with 2 fields for convenience - key to play the Video, and user-friendly name to display in the UI
                    returnList.add(Utility.formatTrailerString(n.getString("key"), n.getString("name")));
                }
            }
        } else if ("reviews".compareToIgnoreCase(name) == 0){
            JSONObject jsonObject = new JSONObject(extrasJsonStr);
            JSONArray results = jsonObject.getJSONArray(KEY_RESULTS);

            resultsArray = new String[results.length()];
            returnList = new ArrayList<String>();

            for (int i=0; i < results.length(); i++){
                JSONObject n = results.getJSONObject(i);
                String author = n.getString("author");
                if (null==author){
                    author="";
                }
                String fullcontent = n.getString("content");
                if (null==fullcontent) {
                    fullcontent = "";
                }

                returnList.add(fullcontent + "\nAuthor: "+author);
            }
        } else {
            Log.e(LOG_TAG, "Unknown request: only videos or reviews allowed.");
        }

        return returnList;
    }

}
