package com.learn.heddy.awesomemovies.data;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.learn.heddy.awesomemovies.data.MovieContract.MovieEntry;

/**
 * Created by hyeryungpark on 9/2/16.
 */
public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        //super.setUp();
        deleteTheDatabase();
    }

    /*
     This test checks to make sure that the content provider is registered correctly.
  */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    private void deleteTheDatabase(){
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    public void testInsert(){

//        ContentValues expectedValues = TestUtility.createMovieTestData();
//        ContentResolver contentResolver = this.getContext().getContentResolver();
//
//          insert with movie content uri and test values...
//         assert a cursor is returned and not empty, query with that cursor, validate all values match
//        Uri insertUri = contentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, expectedValues);
//
//        assertTrue(insertUri != null);
//        long _id = ContentUris.parseId(insertUri);
//        assertTrue(_id != -1);
//
//        Cursor c = contentResolver.query(MovieContract.MovieEntry.CONTENT_URI,
//                                null,
//                                null,
//                                null,
//                                null,
//                                null);
//
//        assertTrue(c.moveToFirst());  // Move to the first valid row, which also validates there is a first row
//
//        TestUtility.validateCurrentRecord("Have TestUtility validate data returned.",
//                                        c,
//                                        expectedValues
//                );
//
//        c.close();
    }

    public void testDelete(){

//        final String movieByMovieIdSelection = MovieContract.MovieEntry.TABLE_MOVIE +
//                "." + MovieContract.MovieEntry.COLUMN_ID + " = ? ";
//
//        final String deleteColId = "8888888";
//        final String deleteColTitle = "FALL to FALL 2";
//
//        ContentValues expectedValues = TestUtility.createMovieTestData();
//
//        Tweak for a second row to use in delete
//        ContentValues secondRowValues = TestUtility.createMovieTestData();
//        secondRowValues.put(MovieContract.MovieEntry.COLUMN_ID, deleteColId);
//        secondRowValues.put(MovieContract.MovieEntry.COLUMN_TITLE, deleteColTitle);
//
//        ContentResolver contentResolver = this.getContext().getContentResolver();
//
//          insert with movie content uri and test values...
//         assert a cursor is returned and not empty, query with that cursor, validate all values match
//        Uri insertUri = contentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, expectedValues);
//
//        assertTrue(insertUri != null);
//        long _id = ContentUris.parseId(insertUri);
//        assertTrue(_id != -1);
//
//         insert both rows
//        insertUri = contentResolver.insert(MovieContract.MovieEntry.CONTENT_URI, secondRowValues);
//
//        assertTrue(insertUri != null);
//        _id = ContentUris.parseId(insertUri);
//        assertTrue(_id != -1);
//
//        int deletedRowsNum = contentResolver.delete(MovieContract.MovieEntry.CONTENT_URI,
//                movieByMovieIdSelection,
//                new String[]{deleteColId}
//                );
//
//        assertTrue(deletedRowsNum == 1);
//
//         no rows should be found if we query that movie id
//        Cursor c = contentResolver.query(MovieContract.MovieEntry.CONTENT_URI,
//                null,
//                movieByMovieIdSelection,
//                new String[]{deleteColId},
//                null,
//                null);
//
//        assertFalse(c.moveToFirst());
//
//        c.close();
    }

    public void testUpdate(){

        final String movieByMovieIdSelection = MovieEntry.TABLE_MOVIE +
                "." + MovieEntry.COLUMN_ID + " = ? ";

        ContentValues firstValues = TestUtility.createMovieTestData();

        String updateColId = firstValues.getAsString(MovieEntry.COLUMN_ID);
        final String updateColTitle = "Star Wars 3";

        //Tweak for a second row to use in update the first row
        ContentValues secondRowValues = TestUtility.createMovieTestData();
        secondRowValues.put(MovieEntry.COLUMN_ID, updateColId);
        secondRowValues.put(MovieEntry.COLUMN_TITLE, updateColTitle);

        ContentResolver contentResolver = this.getContext().getContentResolver();

        // Register a content observer for our location delete.
        TestUtility.TestContentObserver movieObserver = TestUtility.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        //  insert with movie content uri and test values...
        // assert a cursor is returned and not empty, query with that cursor, validate all values match
        Uri insertUri = contentResolver.insert(MovieEntry.CONTENT_URI, firstValues);

        // Students: If this fails, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // insert.
        movieObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertTrue(insertUri != null);
        long _id = ContentUris.parseId(insertUri);
        assertTrue(_id != -1);

        // Register a content observer for our location delete.
        movieObserver = TestUtility.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        int updatedRowsNum = contentResolver.update(MovieEntry.CONTENT_URI,
                secondRowValues,
                movieByMovieIdSelection,
                new String[]{updateColId}
        );

        // Students: If this fails, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // update.  (only if the insertReadProvider is succeeding)
        movieObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(movieObserver);

        assertTrue(updatedRowsNum == 1);

        // no rows should be found if we query that movie id
        Cursor c = contentResolver.query(MovieEntry.CONTENT_URI,
                null,
                movieByMovieIdSelection,
                new String[]{updateColId},
                null,
                null);

        assertTrue(c.moveToFirst());

        TestUtility.validateCurrentRecord("CALL TestUtility to validate update.", c, secondRowValues);

        c.close();
    }
}
