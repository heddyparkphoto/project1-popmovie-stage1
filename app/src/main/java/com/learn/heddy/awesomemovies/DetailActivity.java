package com.learn.heddy.awesomemovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;

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

            // in order to de-couple from either MainActivity or MovieFragment/ListFavoritesFragment, use bundle arguments
            Intent intent = getIntent();
            if (null != intent && null != intent.getBundleExtra(DetailFragment.INTENT_PARCEL)) {
                Bundle bundle = intent.getBundleExtra(DetailFragment.INTENT_PARCEL);
                if (bundle.getParcelable(DetailFragment.MOVIE_PARCEL) != null) {
                    mm = bundle.getParcelable(DetailFragment.MOVIE_PARCEL);
                } else {
                    // Nothing to show
                }
            }

            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.INTENT_PARCEL, mm);

            DetailFragment df = new DetailFragment();
            df.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detailcontainer, df)
                    .commit();
        }
    }
}
