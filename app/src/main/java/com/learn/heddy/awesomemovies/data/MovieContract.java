package com.learn.heddy.awesomemovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by hyeryungpark on 9/2/16.
 */
public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.learn.heddy.awesomemovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // URIs we will support - com.learn.heddy.awesomemovies/movie/ is a valid path
    public static final String PATH_MOVIE = "movie";

    public static final class MovieEntry implements BaseColumns {

        //table name
        public static final String TABLE_MOVIE = "movie";

        //columns to display Favorite movie detail
        public static final String _ID = "_id";
        public static final String COLUMN_ID = "movieid";
        public static final String COLUMN_POSTERPATH = "posterpath";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASEDATE = "releasedate";

        // create content uri
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        // create cursor of base type directory for multiple entries
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

		/*
			WATCH OUT the return type of Uri - insert does not need the id, however the follow-up steps
			such as notify, or a case of a chain-reaction of another table needing the auto-generated _id seen in the Location and Weather
			app made the android programmers enforced to return Uri for practical applications
			Sample signature of the insert method below in the ContentProvider classes
			===========================================================
			public Uri insert(Uri uri, ContentValues values){
			===========================================================
		*/

        public static Uri buildMovieUriWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getOneMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
