package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.learn.heddy.awesomemovies.data.MovieContract;

/**
 * Created by hyeryungpark on 10/10/16.
 */
public class FavoriteDefaultMovieTask extends AsyncTask<Void, Void, Cursor> {

    static final String LOG_TAG = FavoriteDefaultMovieTask.class.getSimpleName();

    private final Context mContext;

    public FavoriteDefaultMovieTask(Context context) {

        mContext = context;
    }

    @Override
    protected Cursor doInBackground(Void... params) {

        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        String sortOrderParm = MovieContract.MovieEntry.COLUMN_RATING + " DESC ";

        Cursor cursor = mContext.getContentResolver().query(
                uri,
                ListFavoritesFragment.FAVORITE_COLUMNS,
                null,
                null,
                sortOrderParm
        );

        if (cursor!=null && cursor.moveToFirst()) {

            return cursor;
        } else {
            return null;
        }
    }
}
