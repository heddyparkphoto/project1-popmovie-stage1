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

    //public String[] trailersName = new String[0];
    //public String[] reviewsName = new String[0];
    public String[] resultsArray;
    public ArrayList<String> returnList;

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String extrasJsonStr = null;

        //query holders
        String[] firstResponsesArray = null; //will need further process in order to get posters

        try {

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendEncodedPath(params[0])
                    .appendEncodedPath(params[1])
                    .appendQueryParameter("api_key", BuildConfig.THE_MOVIE_DB_API_KEY);

//            Uri builtUri = Uri.parse(MOVIES_API_BASE_URL).buildUpon()
//                    .appendEncodedPath(params[0])
//                    .appendEncodedPath(params[1])
//                    .appendQueryParameter(MY_KEY_PARAM, MY_KEY)
//                    .build();

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

        //debug for now
        Log.v(LOG_TAG, extrasJsonStr);

        //do Movie data parsing
        try {
            getMovieExtrasFromJson(extrasJsonStr, params[1]); //either videos or reviews
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
                    returnList.add(n.getString("key")); //further parse once I learn how to play a video in different ways - only key needed for Intent.ACTION_VIEW
                }
            }
        } else if ("reviews".compareToIgnoreCase(name) == 0){
            JSONObject jsonObject = new JSONObject(extrasJsonStr);
            JSONArray results = jsonObject.getJSONArray(KEY_RESULTS);

            resultsArray = new String[results.length()];
            returnList = new ArrayList<String>();

            for (int i=0; i < results.length(); i++){
                JSONObject n = results.getJSONObject(i);
                String authors = n.getString("author");
                String fullcontent = n.getString("content");
                if (null==fullcontent) {
                    fullcontent = "";
                } else if (fullcontent.length() > 18){
                    fullcontent = fullcontent.substring(0,18);
                }
                returnList.add(fullcontent); //further parse once I learn what to do with the database and files
            }
        } else {

        }

        return returnList;
    }

}
