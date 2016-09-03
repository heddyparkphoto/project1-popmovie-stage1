package com.learn.heddy.awesomemovies.data;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.learn.heddy.awesomemovies.data.MovieContract.MovieEntry;
import com.learn.heddy.awesomemovies.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by hyeryungpark on 9/2/16.
 */
public class TestUtility extends AndroidTestCase {

    public static ContentValues createMovieTestData() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(MovieEntry.COLUMN_ID, "11000087");
        contentValues.put(MovieEntry.COLUMN_POSTERPATH, "/XYZMoviePath");
        contentValues.put(MovieEntry.COLUMN_TITLE, "EVE's Clowns");
        contentValues.put(MovieEntry.COLUMN_OVERVIEW, "Great thinking movie.");
        contentValues.put(MovieEntry.COLUMN_RATING, "8.7/4888");
        contentValues.put(MovieEntry.COLUMN_RELEASEDATE, "08/14/2625");

        return contentValues;
    }

    @TargetApi(11)
    static void validateCurrentRecord(String errorMsg, Cursor cursor, ContentValues expectedValueSet) {
        Set<Map.Entry<String, Object>> knownValueSet = expectedValueSet.valueSet();

        for (Map.Entry<String, Object> knownEntry : knownValueSet) {
            int idx = cursor.getColumnIndex(knownEntry.getKey());

            assertTrue("Column " + knownEntry.getKey() + " not found." + errorMsg, idx != -1);

            assertEquals("Data unmatched.  Bad! " + errorMsg, knownEntry.getValue().toString(), cursor.getString(idx));
        }
    }

    // ADD POLLING CHECK CODES FROM Sunshine app!!!
       /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
