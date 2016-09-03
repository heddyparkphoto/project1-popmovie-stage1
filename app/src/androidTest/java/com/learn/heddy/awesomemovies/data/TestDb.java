package com.learn.heddy.awesomemovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

/**
 * Created by hyeryungpark on 9/2/16.
 */
public class TestDb extends AndroidTestCase {

    private void deleteTheDatabase(){
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }
    @Override
    protected void setUp() throws Exception {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {

        // Verify our table
        HashSet<String> tableNames = new HashSet<String>();
        tableNames.add(MovieContract.MovieEntry.TABLE_MOVIE);

        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();
        assertTrue(db.isOpen());

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master where type = 'table'", null);
        assertTrue("Error: No tables created!", c.moveToFirst());

        do {
            tableNames.remove(c.getString(0));
        } while (c.moveToNext());

        assertTrue("Error: database was created without movie table.", tableNames.isEmpty());

        // Verify name of the columns
        HashSet<String> columnNames = new HashSet<String>();

        columnNames.add(MovieContract.MovieEntry._ID);
        columnNames.add(MovieContract.MovieEntry.COLUMN_ID);
        columnNames.add(MovieContract.MovieEntry.COLUMN_POSTERPATH);
        columnNames.add(MovieContract.MovieEntry.COLUMN_TITLE);
        columnNames.add(MovieContract.MovieEntry.COLUMN_OVERVIEW);
        columnNames.add(MovieContract.MovieEntry.COLUMN_RATING);
        columnNames.add(MovieContract.MovieEntry.COLUMN_RELEASEDATE);

        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_MOVIE + ")", null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        int name_column = c.getColumnIndex("name");
        do {
            String columnName = c.getString(name_column);
            columnNames.remove(columnName);
        } while (c.moveToNext());

        assertTrue("Error: movie table was created without all columns.", columnNames.isEmpty());

        c.close();
        db.close();
    }

    public void testMovieTable(){
        ContentValues testValues = TestUtility.createMovieTestData();

        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long validId = db.insert(MovieContract.MovieEntry.TABLE_MOVIE, null, testValues);

        assertTrue("Insert did not work.", validId != -1);
        String validIdStr = Long.toString(validId);
        String[] selctionArgs = new String[1];
        selctionArgs[0] = validIdStr;

        //Verify the record
        Cursor c = db.query(MovieContract.MovieEntry.TABLE_MOVIE,
                null,
                MovieContract.MovieEntry._ID + "=?",
                selctionArgs,
                null,
                null,
                null);

        assertTrue("Error: no rows with the test movie id", c.moveToFirst());  // Move the cursor to a valid database row as well in one step!
        TestUtility.validateCurrentRecord("Error validating data.", c, testValues); //NOTE: cursor c already pointing to first valid row

        c.close();
        db.close();
    }

    @Override
    protected void tearDown() throws Exception {
        //deleteDatabase();
        super.tearDown();
    }
}
