package com.learn.heddy.awesomemovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by hyeryungpark on 9/15/16.
 *
 * Fragment class that shows the Movie details selected in the main screen
 */
public class DetailFragment extends Fragment {

    public static final String INTENT_PARCEL = "INTENT_PARCEL";
    public static final String MOVIE_PARCEL = "MOVIE_PARCEL";

    Movie mMovie;
    ImageView mPosterImageView;
    TextView mTitleView;
    TextView mSynopsisPlotView;
    TextView mRatingView;
    TextView mReleaseDateView;

    //Trailers and Reviews Array
    ArrayAdapter<String> mTrailersAdapter; //trailers are another JSONARRAY, so for now, test with a string -just concat couple fields as String
    ArrayAdapter<String> mReviewsAdapter;

    String mMovieid;
    String mPosterpathTxt;
    String mTitleTxt;
    String mPlotTxt;
    String mRatingTxt;
    String mReleasedateTxt;

    // Flag to make extra api calls for trailers and reviews
    private boolean needExtraFetch;
    // Set up some Constants for the extra api calls
    static final String YOUTUBE_URL_BEGIN = "https://www.youtube.com/watch";
    static final String YOUTUBE_V_FIELD = "v";

    static private ArrayList<String> testnamesResult;
    static private ArrayList<String> reviewnamesResult;


    private final String LOG_TAG = DetailFragment.class.getSimpleName();

    public DetailFragment(){
        setHasOptionsMenu(true);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        // finish Views
        mPosterImageView = (ImageView)rootView.findViewById(R.id.posterImage);
        mTitleView = (TextView)rootView.findViewById(R.id.titleText);
        mSynopsisPlotView = (TextView) rootView.findViewById(R.id.synopsisPlotText);
        mRatingView = (TextView) rootView.findViewById(R.id.ratingText);
        mReleaseDateView = (TextView) rootView.findViewById(R.id.releaseDateText);

        Intent intent = getActivity().getIntent();

        if (null!=intent && null!=intent.getBundleExtra(INTENT_PARCEL)) {
            Bundle bundle = intent.getBundleExtra(INTENT_PARCEL);
            if (bundle.getParcelable(MOVIE_PARCEL)!= null) {
                mMovie = bundle.getParcelable(MOVIE_PARCEL);
            }
        }

        if (null == mMovie) {
            Log.e(LOG_TAG, "Movie was null on Intent.  Nothing to parse.");  // Should I do more here??
            return rootView;
        }

        // Set up for trailers list section
        ListView trailersList = (ListView)rootView.findViewById(R.id.listview_trailer);
        mTrailersAdapter = new ArrayAdapter<String>(getActivity(), R.layout.trailer_item); //layout not the view
        trailersList.setAdapter(mTrailersAdapter);

        trailersList.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String urlAsString = mTrailersAdapter.getItem(position);
                        Log.d(LOG_TAG, "trailer key "+urlAsString);

                        Uri uri = Uri.parse(YOUTUBE_URL_BEGIN).buildUpon()
                                .appendQueryParameter(YOUTUBE_V_FIELD, urlAsString)
                                .build();
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        } else {
                            //Display error message
                            Toast.makeText(getActivity(), "Sorry, cannot play video", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        // TRY HERE then move to Utility class
        // Preference sort option from SharedPreferences
//        String prefSortOption;
//
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        prefSortOption = sharedPreferences.getString(
//                getString(R.string.pref_sort_by_key), getString(R.string.pref_default_sort_by));
//
//        if ("favorites" != prefSortOption){
//            setNeedExtraFetch(true);
//        }
        setNeedExtraFetch(Utility.needExtraFetch(getActivity()));

        // Set up basic Detail Fragment
        String posterpath = mMovie.posterpath;

        /*
         2 ways of getting the poster depending on the Preference settings
         If needExtraFetch, poster's coming in from real-time online,
         if not, poster's coming from file saved in memory using movie title as file name.
        */
        if (needExtraFetch) {
            Picasso.with(getActivity()).load(posterpath).into(mPosterImageView);
        } else {
            // CODE AFTER THE DB is done
        }
        mTitleView.setText(mMovie.title);
        mSynopsisPlotView.setText(mMovie.overview);
        mReleaseDateView.setText(mMovie.releasedate);
        mRatingView.setText(mMovie.rating);

        // Fetch data
        String[] result;
        FetchMovieExtras myfetch;

        mMovieid = mMovie.id;

        if (needExtraFetch) {
            //trailers Async
            String[] videoParams = new String[2];
            videoParams[0] = mMovie.id;
            videoParams[1] = "videos";

            myfetch = (FetchMovieExtras) new FetchMovieExtras().execute(videoParams);
            // testnamesResult = myfetch.trailersName; // trailerName was blank

            try {
                testnamesResult = myfetch.get(); // trailerName was good here with the get(), but disappears soon, so extract data here!!
                if (null != testnamesResult) {
                    Log.v(LOG_TAG, " testnamesResult length from async " + testnamesResult.size());


                    for (String s : testnamesResult) {
                        mTrailersAdapter.add(s);
                    }

//                    if (testnamesResult != null && testnamesResult.size() > 0) {
//                        mSharedTrailerUri = testnamesResult.get(0);
//                    }
                } else {
                    Log.v(LOG_TAG, " testnamesResult is null still!");
                }

            } catch (Exception allEx) {
                Log.e(LOG_TAG, " Trailers async task exception " + allEx);
            }
        }

        return rootView;
    }

    public void setNeedExtraFetch(boolean boolValue) {
        needExtraFetch = boolValue;
    }

    public boolean getNeedExtraFetch(){
        return needExtraFetch;
    }


}
