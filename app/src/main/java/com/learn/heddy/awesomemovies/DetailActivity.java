package com.learn.heddy.awesomemovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

/**
 * Created by hyeryungpark on 8/27/16.
 */
public class DetailActivity extends ActionBarActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            Movie mm = null;

            Bundle args = new Bundle();
            // in order to de-couple from either MainActivity or Main Fragment, use bundle arguments
            if (getIntent().hasExtra(DetailFragment.MOVIE_PARCEL)) {
                mm = (Movie) getIntent().getExtras().get(DetailFragment.MOVIE_PARCEL);
                args.putParcelable(DetailFragment.INTENT_PARCEL, mm);  //just copy over the Movie object
            }

            if (null == mm) {    // Later add default to the first movie detail in TwoPane first screen...
                //mm = MoviesFragment.firstMovie;
                Log.d(LOG_TAG, "mm still null... must be in TwoPane movie not clicked yet!");
            }

            DetailFragment df = new DetailFragment();
            df.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detailcontainer, df)
                    .commit();
        }

    }

}
