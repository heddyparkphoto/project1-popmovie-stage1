package com.learn.heddy.awesomemovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hyeryungpark on 9/17/16.
 */
public class ReviewsActivity extends ActionBarActivity {

    static final String LOG_TAG = ReviewsActivity.class.getSimpleName();

    ArrayAdapter<String> mReviewsAdapter;

    static private ArrayList<String> reviewApiResult;     // reviews api result collection
    private Movie mMovie;
    private static final String TITLE_STATE = "TITLE_STATE";
    private static final String REVIEW_RESULT_STATE = "REVIEW_RESULT_STATE";

    TextView mTitleView;
    String mFullTitle;
    ListView mReviewsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        // find views
        mTitleView = (TextView) findViewById(R.id.reviews_title);
        mReviewsList = (ListView) findViewById(R.id.listview_reviews);
        mReviewsAdapter = new ArrayAdapter<String>(this, R.layout.review_item); //layout not the view
        mReviewsList.setAdapter(mReviewsAdapter);

        if (savedInstanceState == null) {
            Intent intent = getIntent();

            if (null != intent && null != intent.getBundleExtra(DetailFragment.INTENT_PARCEL)) {
                Bundle bundle = intent.getBundleExtra(DetailFragment.INTENT_PARCEL);
                if (bundle.getParcelable(DetailFragment.MOVIE_PARCEL) != null) {
                    mMovie = bundle.getParcelable(DetailFragment.MOVIE_PARCEL);
                }
            }

            if (mMovie!=null) {
                // add the Movie Title to the Review title
                mFullTitle = mTitleView.getText() + "   " + mMovie.title;
                mTitleView.setText(mFullTitle);

                handleReviewsView();
            }
        } else {
            if (savedInstanceState.containsKey(TITLE_STATE) && mTitleView!=null){
                mTitleView.setText(savedInstanceState.getString(TITLE_STATE));
            }
            if (savedInstanceState.getStringArrayList(REVIEW_RESULT_STATE)!=null){
                for (String s: reviewApiResult){
                    mReviewsAdapter.add(s);
                }
            }
        }
    }

    private void handleReviewsView() {

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TITLE_STATE, mFullTitle);
        outState.putStringArrayList(REVIEW_RESULT_STATE, reviewApiResult);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState!=null){
            mFullTitle = savedInstanceState.getString(TITLE_STATE);
            reviewApiResult = savedInstanceState.getStringArrayList(REVIEW_RESULT_STATE);
        }
    }
}
