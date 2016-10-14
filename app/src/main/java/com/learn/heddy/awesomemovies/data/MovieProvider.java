package com.learn.heddy.awesomemovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import com.learn.heddy.awesomemovies.data.MovieContract.MovieEntry;

/**
 * Created by hyeryungpark on 9/2/16.
 */
public class MovieProvider  extends ContentProvider {

    private static final String LOG_TAG = MovieProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mMovieDbHelper;

    public static final int MOVIE = 100;
    public static final int FAVORITE_WITH_MOVIE_ID = 200;

    private static final SQLiteQueryBuilder sMovieByMovieIdQueryBuilder;

    static {
        sMovieByMovieIdQueryBuilder = new SQLiteQueryBuilder();

        sMovieByMovieIdQueryBuilder.setTables(
                MovieEntry.TABLE_MOVIE);
    }

    private static final String sMovieByMovieIdSelection =
            MovieContract.MovieEntry.TABLE_MOVIE +
                    "." + MovieContract.MovieEntry.COLUMN_ID + " = ? ";

    private Cursor getMovieByMovieId(Uri uri, String[] projection, String sortOrder){

        String idFromUri = MovieContract.MovieEntry.getOneMovieIdFromUri(uri);

        return mMovieDbHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_MOVIE,
                projection,
                sMovieByMovieIdSelection,
                new String[]{idFromUri},
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {

        mMovieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    /*
     This UriMatcher will match each URI to the MOVIE or FAVORITE_MOVIE_ID
     integer constants defined above.
 */
    static UriMatcher buildUriMatcher() {

        UriMatcher myMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        myMatcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        myMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", FAVORITE_WITH_MOVIE_ID);

        return myMatcher;
    }

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match){
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;

            case FAVORITE_WITH_MOVIE_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown Uri matcher " + match);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int matched = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (matched){
            case MOVIE:
                retCursor = mMovieDbHelper.getReadableDatabase().query(MovieContract.MovieEntry.TABLE_MOVIE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            case FAVORITE_WITH_MOVIE_ID:

                retCursor = getMovieByMovieId(uri, projection, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unmatched uri "+uri);
        }

        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int matched = sUriMatcher.match(uri);
        Uri returnUri;

        switch (matched){
            case MOVIE:
                long _id = db.insert(MovieEntry.TABLE_MOVIE, null, values);
                if (_id > 0) {
                    returnUri = MovieContract.MovieEntry.buildMovieUriWithId(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert movie into uri "+uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unmatched uri "+uri);
        }

        getContext().getContentResolver().notifyChange(uri, null); // Lesson 4.b. emphasizes uri, not returnUri, notifies all uris
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int matched = sUriMatcher.match(uri);
        int deletedRowNum = 0;

        // Syntax:  if selection is null, all rows are deleted, right?  setting it to 1 is useful
        if (selection == null){
            selection = "1";
        }

        switch (matched){
            case MOVIE:
                deletedRowNum = db.delete(MovieEntry.TABLE_MOVIE,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unmatched uri "+uri);
        }

        if (deletedRowNum != 0 ){
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            Log.e(LOG_TAG, "Delete FAILED.");
        }
        return deletedRowNum;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int updatedRowNum;

        switch (match) {
            case MOVIE:
                updatedRowNum = db.update(MovieEntry.TABLE_MOVIE, values, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (updatedRowNum != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updatedRowNum;
    }

    // Instructor note from Sunshine app
    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mMovieDbHelper.close();
        super.shutdown();
    }
}
