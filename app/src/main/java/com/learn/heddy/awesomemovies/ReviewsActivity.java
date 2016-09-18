package com.learn.heddy.awesomemovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by hyeryungpark on 9/17/16.
 */
public class ReviewsActivity extends ActionBarActivity {

    static final String LOG_TAG = ReviewsActivity.class.getSimpleName();

    Movie mMovie;
    ArrayAdapter<String> mReviewsAdapter;

    static private ArrayList<String> reviewApiResult;     // reviews api result collection

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        if (savedInstanceState == null) {
            Intent intent = getIntent();

            if (null != intent && null != intent.getBundleExtra(DetailFragment.INTENT_PARCEL)) {
                Bundle bundle = intent.getBundleExtra(DetailFragment.INTENT_PARCEL);
                if (bundle.getParcelable(DetailFragment.MOVIE_PARCEL) != null) {
                    mMovie = bundle.getParcelable(DetailFragment.MOVIE_PARCEL);
                }
            }

            if (null == mMovie) {
                Log.e(LOG_TAG, "Movie was null on Intent.  Nothing to parse.");  // Should I do more here??
                //return;
            }

            // Show Movie Title
            TextView titleView = (TextView) findViewById(R.id.reviews_title);
            String moreTitle = titleView.getText() + "   " + mMovie.title;
            titleView.setText(moreTitle);

            // Set up for reviews list section
            ListView reviewsList = (ListView) findViewById(R.id.listview_reviews);
            mReviewsAdapter = new ArrayAdapter<String>(this, R.layout.review_item); //layout not the view
            reviewsList.setAdapter(mReviewsAdapter);
            //Toast.makeText(this, "Review Activity yo!", Toast.LENGTH_LONG).show();
            handleReviewsView();
        }
    }

    private void handleReviewsView() {

        Toast.makeText(this, "Review Activity fetching reviews!", Toast.LENGTH_LONG).show();
        String[] result;
        FetchMovieExtras myfetch;

        String[] reviewsParams = new String[2];
        reviewsParams[0] = mMovie.id;
        reviewsParams[1] = "reviews";

        myfetch = (FetchMovieExtras) new FetchMovieExtras().execute(reviewsParams);

        try {
            reviewApiResult = myfetch.get(); // trailerName was good here with the get(), but disappears soon, so extract data here!!
            if (null != reviewApiResult && reviewApiResult.size() > 0) {
                Log.v(LOG_TAG, " reviewApiResult length from async " + reviewApiResult.size());

                for (String s: reviewApiResult){
                    mReviewsAdapter.add(s);
                }

            } else {
                Log.v(LOG_TAG, " reviewApiResult is null still!");
            }

        } catch (Exception allEx) {
            Log.e(LOG_TAG, " Reviews async task  exception " + allEx);
        }

    }
}
