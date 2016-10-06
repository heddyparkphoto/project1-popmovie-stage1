package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by hyeryungpark on 9/15/16.
 *
 * Fragment class that shows the Movie details selected in the main screen
 */
public class DetailFragment extends Fragment {

    public static final String INTENT_PARCEL = "INTENT_PARCEL";
    public static final String MOVIE_PARCEL = "MOVIE_PARCEL";
    private static final String TEST_INT = "TEST_INT";

    private Movie mMovie;
    private ImageView mPosterImageView;
    private TextView mTitleView;
    private TextView mSynopsisPlotView;
    private TextView mRatingView;
    private TextView mReleaseDateView;
    private TextView mReviewsLinkView;
    private Button mFavoriteButtonView;
    private TextView mTrailerTitleView;
    private TextView mRequestReviewLinkView;



    //Trailers and Reviews Array
    ArrayAdapter<String> mTrailersAdapter; //trailers are another JSONARRAY, so for now, test with a string -just concat couple fields as String
    //ArrayAdapter<String> mReviewsAdapter;

    // Flag to make extra api calls for trailers and reviews
    private boolean needExtraFetch;
    // Set up some Constants for the extra api calls
    static final String YOUTUBE_URL_BEGIN = "https://www.youtube.com/watch";
    static final String YOUTUBE_V_FIELD = "v";

    ArrayList<String> mTrailerKeyArrayList;
    static private ArrayList<String> trailerApiResult;    // trailers api result collection
    static private ArrayList<String> reviewApiResult;     // reviews api result collection

    private final String LOG_TAG = DetailFragment.class.getSimpleName();


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        // finish Views
        mPosterImageView = (ImageView) rootView.findViewById(R.id.posterImage);
        mTitleView = (TextView) rootView.findViewById(R.id.titleText);
        mSynopsisPlotView = (TextView) rootView.findViewById(R.id.synopsisPlotText);
        mRatingView = (TextView) rootView.findViewById(R.id.ratingText);
        mReleaseDateView = (TextView) rootView.findViewById(R.id.releaseDateText);
        mTrailerTitleView = (TextView) rootView.findViewById(R.id.trailer_title);
        mReviewsLinkView = (TextView) rootView.findViewById(R.id.readReviewsLink);

        if (savedInstanceState != null && savedInstanceState.containsKey(TEST_INT)){
            Log.v(LOG_TAG, "well, i'll be ...");
        }

        Intent intent = getActivity().getIntent();

        if (null != intent && null != intent.getBundleExtra(INTENT_PARCEL)) {
            Bundle bundle = intent.getBundleExtra(INTENT_PARCEL);
            if (bundle.getParcelable(MOVIE_PARCEL) != null) {
                mMovie = bundle.getParcelable(MOVIE_PARCEL);
                Log.d(LOG_TAG, "Intent MOVIE_PARCEL not null!");
            } else {
                Log.d(LOG_TAG, "Bad::::: MOVIE_PARCEL is NULL!");
            }
        } else if (getArguments()!= null){
            Bundle args = getArguments();
            if (args != null){
                mMovie = args.getParcelable(DetailFragment.MOVIE_PARCEL);
                Log.d(LOG_TAG, "getArguments mMovie not null!");
            } else {
                Log.d(LOG_TAG, "Bad::getArguments --- mMovie is NULL!");
            }
        }

//        if (savedInstanceState!=null){
//            if (savedInstanceState.getParcelable(MOVIE_PARCEL)!=null){
//                mMovie = savedInstanceState.getParcelable(MOVIE_PARCEL);
//            }
//
//            Log.d(LOG_TAG, "savedInstanceState not null");
//        }

        if (null == mMovie) {
         //   Log.e(LOG_TAG, "Movie was null on Intent.  Nothing to parse.");  // Should I do more here??
            return rootView;
        }

        // Set up for trailers list section
        ListView trailersList = (ListView) rootView.findViewById(R.id.listview_trailer);
        mTrailersAdapter = new ArrayAdapter<String>(getActivity(), R.layout.trailer_item); //layout not the view
        trailersList.setAdapter(mTrailersAdapter);

        trailersList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String urlAsString = "";
                        //String keyName = mTrailersAdapter.getItem(position);
                        //int delimPos = keyName.indexOf(Utility.getTrailerDelimeter());
                        if (mTrailerKeyArrayList != null && mTrailerKeyArrayList.size() > position) {
                            //     String urlAsString = keyName.substring(0, delimPos);
                            urlAsString = mTrailerKeyArrayList.get(position);

                            //Log.d(LOG_TAG, "whole " + keyName + " trailer key " + urlAsString);

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
                }
        );

//        // Set up for reviews list section
//        ListView reviewsList = (ListView)rootView.findViewById(R.id.listview_reviews);
//        mReviewsAdapter = new ArrayAdapter<String>(getActivity(), R.layout.review_item); //layout not the view
//        reviewsList.setAdapter(mReviewsAdapter);
//
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
            //mPosterImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            File posterfile = getFileInInternalStorage(mMovie.title);
            if (posterfile != null) {
                Picasso.with(getActivity()).load(posterfile).into(mPosterImageView);
            } else {
                Log.e(LOG_TAG, "Image for poster not found.");
            }
        }

        mTitleView.setText(mMovie.title);
        mSynopsisPlotView.setText(mMovie.overview);
        mReleaseDateView.setText(mMovie.releasedate);
        mRatingView.setText(mMovie.rating);

        // Set up Action Button
        handleMarkFavorites(rootView);

        if (needExtraFetch) {

            handleTrailers(rootView);
            handleReviews(rootView);
        }

        return rootView;
    }

    public void setNeedExtraFetch(boolean boolValue) {
        needExtraFetch = boolValue;
    }

    public boolean getNeedExtraFetch() {
        return needExtraFetch;
    }

    // Awesome Movies Trailers
    private void handleTrailers(View parent) {

        String[] result;
        FetchMovieExtras myfetch;

        //trailers Async
        String[] videoParams = new String[2];
        videoParams[0] = mMovie.id;
        videoParams[1] = "videos";

        myfetch = (FetchMovieExtras) new FetchMovieExtras().execute(videoParams);
        // testnamesResult = myfetch.trailersName; // trailerName was blank

        try {
            trailerApiResult = myfetch.get(); // trailerName was good here with the get(), but disappears soon, so extract data here!!
            if (null != trailerApiResult) {
                int tSize = trailerApiResult.size();
              //  Log.v(LOG_TAG, " testnamesResult length from async " + trailerApiResult.size());

                mTrailerKeyArrayList = new ArrayList<String>(tSize);

                String delim = Utility.getTrailerDelimeter();
                int delimPos = 0;

                for (String s : trailerApiResult) {
                    delimPos = s.indexOf(delim);
                    mTrailerKeyArrayList.add(s.substring(0, delimPos));
                    mTrailersAdapter.add(s.substring(delimPos + 1));
                }

//                    if (trailerApiResult != null && trailerApiResult.size() > 0) {
//                        mSharedTrailerUri = testnamesResult.get(0);
//                    }
            } else {
        //        Log.v(LOG_TAG, " trailerApiResult is null still!");
            }

        } catch (Exception allEx) {
            Log.e(LOG_TAG, " Trailers async task exception " + allEx);
        }
    }


    // Awesome Movies Reviews
    private void handleReviews(View parent) {
        mReviewsLinkView = (TextView) parent.findViewById(R.id.readReviewsLink);
        mReviewsLinkView.setClickable(true);
        mReviewsLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set on Bundle the Parcelable Movie object
                //Use Explicit Intent to ReviewsActivity
                Intent intent = new Intent(getActivity(), ReviewsActivity.class);
                Bundle mParcel = new Bundle();
                mParcel.putParcelable(DetailFragment.MOVIE_PARCEL, mMovie);

                intent.putExtra(DetailFragment.INTENT_PARCEL, mParcel);
                startActivity(intent);


                //Toast.makeText(getActivity(), "Got it!!", Toast.LENGTH_LONG).show();
//                String[] result;
//                FetchMovieExtras myfetch;
//
//                String[] reviewsParams = new String[2];
//                reviewsParams[0] = mMovie.id;
//                reviewsParams[1] = "reviews";
//
//                myfetch = (FetchMovieExtras) new FetchMovieExtras().execute(reviewsParams);
//
//                try {
//                    reviewApiResult = myfetch.get(); // trailerName was good here with the get(), but disappears soon, so extract data here!!
//                    if (null != reviewApiResult && reviewApiResult.size() > 0) {
//                        Log.v(LOG_TAG, " reviewApiResult length from async " + reviewApiResult.size());
//
//                            for (String s: reviewApiResult){
//                                mReviewsAdapter.add(s);
//                            }
//
//                    } else {
//                        Log.v(LOG_TAG, " reviewApiResult is null still!");
//                    }
//
//                } catch (Exception allEx) {
//                    Log.e(LOG_TAG, " Reviews async task  exception " + allEx);
//                }


            }
        });
    }

    // Favorites
    private void handleMarkFavorites(View parent) {

        final boolean isAdd;
        final boolean isRemove;

        // Set up Mark-favorite Button - this action needs to Database action
        mFavoriteButtonView = (Button) parent.findViewById(R.id.mark_favorite);
        if (!needExtraFetch) {
            //mFavoriteButtonView.setVisibility(View.INVISIBLE);
            mFavoriteButtonView.setText("Remove");
            mFavoriteButtonView.setBackgroundColor(Color.LTGRAY);
            isAdd = false;
            isRemove = true;

            if (null != mTrailerTitleView) {
                mTrailerTitleView.setVisibility(View.INVISIBLE);
            }
            if (null != mReviewsLinkView) {
                mReviewsLinkView.setVisibility(View.INVISIBLE);
            }
        } else {
            isAdd = true;
            isRemove = false;
        }

        mFavoriteButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){

                //FavoriteMovieTask asyncAddTask = new FavoriteMovieTask(getActivity(), true, false);
                FavoriteMovieTask asyncAddTask = new FavoriteMovieTask(getActivity(), isAdd, isRemove);

                asyncAddTask.execute(mMovie);

                // Save the poster image to a File system to save space in the Database
                Picasso.with(getActivity()).load(mMovie.posterpath).into(target);
            }
        });

        Log.v(LOG_TAG, " Good so far?? ");
    }

    public void onSortOptionChanged(String newMovieId){
        // For now just log it
        Log.d(LOG_TAG, "newMovieId "+newMovieId);
    }

        /*
        Poster image stored in a File: made public to use Detail Fragment View for the Favorite Detail as well.
     */

    public File getFileInInternalStorage(String titleAsName){

        File folder = getContext().getDir(POSTER_FOLDER, Context.MODE_PRIVATE);

        if (!folder.exists()){
            Log.e(LOG_TAG, "Error folder not found.");
            return null;
        }

        return new File(folder + File.separator + titleAsName + ".jpg");
    }


    /*
    Save poster image to a file
 */
    public final static String POSTER_FOLDER = "movieposters";
    File file;

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    File file = getFileInInternalStorage(mMovie.title);
                    try {
                        file.createNewFile();
                        FileOutputStream ostream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
                        ostream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            if (placeHolderDrawable != null) {
            }
        }
    };
}
