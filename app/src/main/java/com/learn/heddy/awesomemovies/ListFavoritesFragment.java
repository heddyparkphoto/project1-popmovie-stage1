package com.learn.heddy.awesomemovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.learn.heddy.awesomemovies.data.MovieContract;

/**
 * Created by hyeryungpark on 9/19/16.
 */
public class ListFavoritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = ListFavoritesFragment.class.getSimpleName();

    /* Cursor variables */
    public static final String[] FAVORITE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_ID,
            MovieContract.MovieEntry.COLUMN_POSTERPATH,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASEDATE
    };

    /* These indices are tied to FAVORITE_COLUMNS.  If FAVORITE_COLUMNS changes, these
       must change.
    */
    public static final int COL_ID = 0;  //INTEGER
    public static final int COL_MOVIE_ID = 1;  //INTEGER
    public static final int COL_POSTER_FILE_PATH = 2; //TEXT
    public static final int COL_TITLE = 3; //TEXT
    public static final int COL_OVERVIEW = 4; //TEXT
    public static final int COL_RATING = 5; //REAL
    public static final int COL_RELEASEDATE = 6; //INTEGER

    private static final int LOADER_ID_FAVORITES = 11;
    FavoriteMovieAdapter mAdapter;

    public ListFavoritesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID_FAVORITES, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);// Populate the main screen with Favorite movies

        //Get the GridView from the fragment_main.xml so that the adapter can be set on it
        GridView gridView = (GridView)rootView.findViewById(R.id.gridview_movies);

        mAdapter = new FavoriteMovieAdapter(getActivity(), null, 0);
        gridView.setAdapter(mAdapter);

        gridView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                        //CursorLoader returns the cursor on the position, null if it cannot seek to that position
                        Cursor itemCursor = (Cursor) adapterView.getItemAtPosition(position);
                        if (null!=itemCursor) {
                            //itemCursor is pointing to fields.  Build into a Movie object.
                            Movie mm = new Movie();

                            mm.id = Integer.toString(itemCursor.getInt(COL_MOVIE_ID));
                            mm.posterpath = itemCursor.getString(COL_POSTER_FILE_PATH);
                            mm.title = itemCursor.getString(COL_TITLE);
                            mm.overview = itemCursor.getString(COL_OVERVIEW);
                            mm.rating = itemCursor.getString(COL_RATING);
                            mm.releasedate = itemCursor.getString(COL_RELEASEDATE);

                            ((MoviesFragment.OnMainMovieItemSelectedListener) getActivity()).OnMainMovieItemClick(mm);
                        }
                    }
                }
        );

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri pickUri = MovieContract.MovieEntry.CONTENT_URI;
        String sortOrderParm = MovieContract.MovieEntry.COLUMN_RATING + " DESC ";

        CursorLoader c = new CursorLoader(
                getActivity(),
                pickUri,
                FAVORITE_COLUMNS,
                null,
                null,
                sortOrderParm
        );

        return c;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
