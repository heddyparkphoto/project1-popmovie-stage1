package com.learn.heddy.awesomemovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements MoviesFragment.OnMainMovieItemSelectedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DETAILFRAGMENT";
    public boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String sortOption = Utility.getPreferredSortOption(this);

//        if (savedInstanceState == null){
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.mainactivity_container, new MoviesFragment())
//                    .commit();
//         }

        if (findViewById(R.id.detailcontainer) != null){
            mTwoPane = true;

            if (savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detailcontainer, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }

        if (savedInstanceState == null) {
            if (sortOption.equalsIgnoreCase(getString(R.string.favorites))){
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.mainactivity_container, new ListFavoritesFragment())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.mainactivity_container, new MoviesFragment())
                        .commit();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds to the action bar if present
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int menuId = item.getItemId();

        if (menuId == R.id.action_settings){

            // Launch Sort Option Preference
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();

        /*
            If "favorites" sort option, switch to ListFavoritesFragment, if not api call to popular or top_rated.
         */
        String sort_option = Utility.getPreferredSortOption(this);

        /*
            Set up for future DetailFragment UI update
         */
        DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
        if (df != null){
            df.onSortOptionChanged("new sort movie!"); //Just test for now.
        }

        if (getString(R.string.favorites).compareToIgnoreCase(sort_option) != 0) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(
                    R.id.mainactivity_container, new MoviesFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(
                    R.id.mainactivity_container, new ListFavoritesFragment());
            transaction.addToBackStack(null);
            transaction.commit();

        }
    }

    @Override
    public void OnMainMovieItemClick(Movie movieItem) {
        boolean needExtraFetch = Utility.needExtraFetch(this);

        if (mTwoPane){

            DetailFragment df = new DetailFragment();
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.MOVIE_PARCEL, movieItem);

            Intent intent = new Intent();
            intent.putExtra(DetailFragment.INTENT_PARCEL, args);

            df.setArguments(args);
            df.setNeedExtraFetch(needExtraFetch);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.detailcontainer, df);
            transaction.addToBackStack(null);

            transaction.commit();
        } else {
            /*
                This snippet replaces what the MovieFragment was doing
             */
            Intent intent = new Intent(this, DetailActivity.class);
            Bundle mParcel = new Bundle();
            mParcel.putParcelable(DetailFragment.MOVIE_PARCEL, movieItem);

            intent.putExtra(DetailFragment.INTENT_PARCEL, mParcel);
            startActivity(intent);
        }
    }

}
