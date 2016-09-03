package com.learn.heddy.awesomemovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by hyeryungpark on 9/3/16.
 */
public class TestUriMatcher extends AndroidTestCase {

    private static final String TEST_MOVIE_ID = "3940055000";
    private final int DIR = MovieProvider.MOVIE;
    private final int ITEM = MovieProvider.FAVORITE_WITH_MOVIE_ID;

    public void testMovieUriMatcher(){
        UriMatcher matcher = MovieProvider.buildUriMatcher();
        Uri testTypeItem = MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(TEST_MOVIE_ID).build();

        assertEquals(matcher.match(MovieContract.MovieEntry.CONTENT_URI), DIR);
        assertEquals(matcher.match(testTypeItem), ITEM);
    }
}
