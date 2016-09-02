package com.learn.heddy.awesomemovies.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by hyeryungpark on 9/2/16.
 */
public class TestMovieContract  extends AndroidTestCase {

    private static final long TEST_MOVIE_ID = 7899090000L;
    private static final String TEST_MOVIE_CONTENT_URL_STRING = "content://com.learn.heddy.pickgoodmovie/movie";
    /*
        private static final String MOVIE_CONTENT_URI = "content://com.learn.heddy.pickgoodmovie/movie";
        private static final String MOVIE_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.learn.heddy.pickgoodmovie/movie";
    */
    public void testBuildMovieUriWithId() {
        Uri testUri = MovieContract.MovieEntry.buildMovieUriWithId(TEST_MOVIE_ID);

        assertEquals("Movie id Error.", testUri.getLastPathSegment(), String.valueOf(TEST_MOVIE_ID));
    }

//    public void testBaseContentUris() throws Throwable {
//        assertEquals("Error: Movie content uri", MovieContract.MovieEntry.CONTENT_URI.toString(), MOVIE_CONTENT_URI);
//
//        This one doesn't seem to be tested to match the hard-coded value.
//        assertEquals(MovieContract.MovieEntry.CONTENT_ITEM_TYPE, MOVIE_CONTENT_ITEM_TYPE);
//    }

    public void testGetOneMovieIdFromUri(){
        Uri uri = Uri.parse(TEST_MOVIE_CONTENT_URL_STRING).buildUpon().appendPath(String.valueOf(TEST_MOVIE_ID)).build();

        String movieId = MovieContract.MovieEntry.getOneMovieIdFromUri(uri);

        assertEquals("Failed retrieving one movie id", TEST_MOVIE_ID, movieId);
    }
}
