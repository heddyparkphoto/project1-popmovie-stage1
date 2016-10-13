package com.learn.heddy.awesomemovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements MoviesFragment.OnMainMovieItemSelectedListener,
                                                                DetailFragment.RemovedNotificationListener
{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DETAILFRAGMENT";
    private static final String MOVIEFRAGMENT_TAG = "MOVIEFRAGMENT";
    private static final String FAVORITESFRAGMENT_TAG = "FAVORITESFRAGMENT";

    public boolean mTwoPane;
    private String mSortOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSortOption = Utility.getPreferredSortOption(this);

        if (findViewById(R.id.detailcontainer) != null){
            mTwoPane = true;

            DetailFragment df = new DetailFragment();
            df.setTwoPane(true);
            if (savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detailcontainer, df, DETAILFRAGMENT_TAG)
                        .commit();
            }
        }

        if (savedInstanceState == null) {
            if (mSortOption.equalsIgnoreCase(getString(R.string.favorites))){
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.mainactivity_container, new ListFavoritesFragment(), FAVORITESFRAGMENT_TAG)
                        .commit();
            } else {
                MoviesFragment mf = new MoviesFragment();
                if (mTwoPane){
                    mf.setTwoPane(true);
                }

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.mainactivity_container, mf, MOVIEFRAGMENT_TAG)
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

        String sort_option = Utility.getPreferredSortOption(this);
        if (sort_option!=null && sort_option.compareTo(mSortOption)!= 0) {

            if (getString(R.string.favorites).compareToIgnoreCase(sort_option) != 0) {
                MoviesFragment mf = new MoviesFragment();
                if (mTwoPane) {
                    mf.setTwoPane(true);
                } else {
                    mf.setTwoPane(false);
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(
                        R.id.mainactivity_container, mf, MOVIEFRAGMENT_TAG);
                transaction.addToBackStack(null);
                transaction.commit();

            } else {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(
                        R.id.mainactivity_container, new ListFavoritesFragment(), FAVORITESFRAGMENT_TAG);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            /*
                DetailFragment UI update if Tablet
             */
            if (mTwoPane) {
                DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
                if (df != null) {
                    df.setTwoPane(true);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detailcontainer, df, DETAILFRAGMENT_TAG)
                            .commit();
                }
            }
        }

        // Update with the current Preference option
        mSortOption = sort_option;
    }

    @Override
    public void OnMainMovieItemClick(Movie movieItem) {
        boolean needExtraFetch = Utility.needExtraFetch(this);

        if (mTwoPane){

            DetailFragment df = new DetailFragment();
            df.setTwoPane(true);

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
                For Phone UI, use explicit Intent
             */
            Intent intent = new Intent(this, DetailActivity.class);
            Bundle mParcel = new Bundle();
            mParcel.putParcelable(DetailFragment.MOVIE_PARCEL, movieItem);

            intent.putExtra(DetailFragment.INTENT_PARCEL, mParcel);
            startActivity(intent);
        }
    }

    @Override
    public void OnRemovedItem() {

        if (mTwoPane) {

            // User just removed the Movie in Detail panel, replace with a brand new (initial movie) DetailFragment
            DetailFragment df = new DetailFragment();
            df.setTwoPane(true);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detailcontainer, df, DETAILFRAGMENT_TAG)
                    .commit();

        }

        // Check if we are showing the ListFavoriteFragment, and if so, replace with a brand new one
        ListFavoritesFragment fragment = (ListFavoritesFragment) getSupportFragmentManager().findFragmentByTag(FAVORITESFRAGMENT_TAG);
        if (null!=fragment) {
            if (mSortOption.compareTo(getString(R.string.favorites)) == 0) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(
                        R.id.mainactivity_container, new ListFavoritesFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    }

}
