package com.learn.heddy.awesomemovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.learn.heddy.awesomemovies.data.MovieContract;

/**
 * Created by hyeryungpark on 9/23/16.
 */
public class FavoriteMovieTask extends AsyncTask<Movie, Void, Boolean> {

    static final String LOG_TAG = FavoriteMovieTask.class.getSimpleName();

    private final Context mContext;
    private final boolean mIsAdd;
    private final boolean mIsDelete;
    private Movie mMovie;

    public FavoriteMovieTask(Context context, boolean isAdd, boolean isDelete) {

        Log.v(LOG_TAG, "Add task class constructor..");
        mContext = context;
        mIsAdd = isAdd;
        mIsDelete = isDelete;
    }

    @Override
    protected Boolean doInBackground(Movie... movies) {
        long newRowId = -1;

        mMovie = movies[0];

        // Following codes executed if Adding the favorite movie
        if (mIsAdd) {
            //If the movie id already exists, do not insert - DB error can crash the app
            Uri queryUri = MovieContract.MovieEntry.buildMovieUriWithId(Long.valueOf(mMovie.id));
            Cursor c = mContext.getContentResolver().query(queryUri, null, null, null, null);
            if (c.getCount() > 0) {
                Log.d(LOG_TAG, "Exists in the Database.");
                return true;
            } else {

                //addToGoodMovieDatabase
                ContentValues values = new ContentValues();

                values.put(MovieContract.MovieEntry.COLUMN_ID, mMovie.id);
                values.put(MovieContract.MovieEntry.COLUMN_POSTERPATH, mMovie.posterpath);
                values.put(MovieContract.MovieEntry.COLUMN_TITLE, mMovie.title);
                values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, mMovie.overview);
                values.put(MovieContract.MovieEntry.COLUMN_RATING, mMovie.rating);
                values.put(MovieContract.MovieEntry.COLUMN_RELEASEDATE, mMovie.releasedate);

                Uri uri = mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);
                newRowId = ContentUris.parseId(uri);

                if (ContentUris.parseId(uri) != -1) {
                    Log.d(LOG_TAG, "uri inserted " + uri.toString());

                /* target is inner class, Picasso fetches the poster image from online
                   and saves into a target which is an image file of the internal storage
                    - I did not try another storage, but after trying to use External storage that did not work
                   it is an option that worked with my current very limited knowledge of the Android Emulator
                */
                    //Picasso.with(mContext).load(mMovie.posterpath).into(target);
                } else {
                    Log.e(LOG_TAG, "ERROR INSERTING FAVORITES.");
                }
            }

            return newRowId != -1;
        } else if (mIsDelete) {
            int deletedNum = 0;
            final String sMovieByMovieIdSelection =
                    MovieContract.MovieEntry.TABLE_MOVIE +
                            "." + MovieContract.MovieEntry.COLUMN_ID + " = ? ";

            deletedNum = mContext.getContentResolver().delete(
                    MovieContract.MovieEntry.CONTENT_URI,
                    sMovieByMovieIdSelection,
                    new String[]{mMovie.id}
            );

            return deletedNum != 0;

        } else {
            return true;
        }
    }


    @Override
    protected void onPostExecute(Boolean success) {
        if (success){
            if (mIsAdd) {
                Toast.makeText(mContext, "Added to Favorite Movie Collection!", Toast.LENGTH_SHORT).show();
            } else if (mIsDelete){
                Toast.makeText(mContext, "Removed from the Favorite Movies!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "FavoriteMovieTask failed.", Toast.LENGTH_LONG).show();
        }
    }
}
