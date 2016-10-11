package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import static com.learn.heddy.awesomemovies.ListFavoritesFragment.*;

/**
 * Created by hyeryungpark on 9/15/16.
 *
 * Fragment class that shows the Movie details selected in the main screen
 */
public class DetailFragment extends Fragment {

    public static final String INTENT_PARCEL = "INTENT_PARCEL";
    public static final String MOVIE_PARCEL = "MOVIE_PARCEL";

    private Movie mMovie;
    private ImageView mPosterImageView;
    private TextView mTitleView;
    private TextView mSynopsisPlotView;
    private TextView mRatingView;
    private TextView mReleaseDateView;
    private TextView mReviewsLinkView;
    private Button mFavoriteButtonView;
    private TextView mTrailerTitleView;

    //Trailers ArrayAdapter
    ArrayAdapter<String> mTrailersAdapter; //trailers are another JSONARRAY, so for now, test with a string -just concat couple fields as String

    // Flag to make extra api calls for trailers and reviews
    private boolean needExtraFetch;
    // Set up some Constants for the extra api calls
    static final String YOUTUBE_URL_BEGIN = "https://www.youtube.com/watch";
    static final String YOUTUBE_V_FIELD = "v";

    ArrayList<String> mTrailerKeyArrayList;
    static private ArrayList<String> trailerApiResult;    // trailers api result collection

    private final String LOG_TAG = DetailFragment.class.getSimpleName();

    private final static String SHARE_ACTION_HASHTAG = "#AwesomeMoviesApp";
    private String mShareActionTrailerUri;
    ShareActionProvider mShareActionProvider;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (Utility.getPreferredSortOption(getActivity()) != getString(R.string.favorites)) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);
            MenuItem shareItem = menu.findItem(R.id.action_share);

            mShareActionProvider = (ShareActionProvider) MenuItemCompat
                    .getActionProvider(shareItem);

            if (mShareActionProvider != null) {
                // New condition during CursorLoader.Callbacks implmentation - Cursor can also set this text
                if (mShareActionTrailerUri != null) {
                    mShareActionProvider.setShareIntent(createMoviesShareIntent());
                } else {
                    // Warn that the Share Action Provider was null
                    Log.d(LOG_TAG, "Share Action Provider was null....");
                }
            }
        }
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

        if (null == mMovie) {
            Log.d(LOG_TAG, "Movie was null on Intent.  Nothing to parse.");  // Should I do more here??
            // Are we in the first screen of Favorites option?
            String sort_option = Utility.getPreferredSortOption(getActivity());
            if (getString(R.string.favorites).compareTo(sort_option)==0) {
                try {
                    // InitialLoad load with the Favorites database with the highest rating among them
                    FavoriteDefaultMovieTask asyncTask = new FavoriteDefaultMovieTask(getActivity());
                    Cursor data = asyncTask.execute().get();
                    Log.d(LOG_TAG, "fetch executed....");
                    mMovie = new Movie();
                    mMovie.id = Integer.toString(data.getInt(COL_MOVIE_ID));
                    mMovie.posterpath = data.getString(COL_POSTER_FILE_PATH);
                    mMovie.title = data.getString(COL_TITLE);
                    mMovie.overview = data.getString(COL_OVERVIEW);
                    mMovie.rating = data.getString(COL_RATING);
                    mMovie.releasedate = data.getString(COL_RELEASEDATE);

                    // Save the poster image to a File system to save space in the Database
                    Picasso.with(getActivity()).load(mMovie.posterpath).into(target);
                    // ((OnDetailRefreshListener)getActivity()).OnDetailRefresh(new DetailActivity());
                } catch (Exception allEx) {
                    // Failed retrieving the data
                    Log.e(LOG_TAG, allEx.toString());
                }
            }
        } else if (savedInstanceState!=null){
            if (savedInstanceState.getParcelable(MOVIE_PARCEL) != null) {
                Log.d(LOG_TAG, "MOVIE_PARCEL on the savedInstanceState");
                mMovie = savedInstanceState.getParcelable(MOVIE_PARCEL);
            }
        }

        if (mMovie!=null) {
            // Set up for trailers list section
            ListView trailersList = (ListView) rootView.findViewById(R.id.listview_trailer);
            mTrailersAdapter = new ArrayAdapter<String>(getActivity(), R.layout.trailer_item); //layout not the view
            trailersList.setAdapter(mTrailersAdapter);

            trailersList.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String urlAsString = "";
                            if (mTrailerKeyArrayList != null && mTrailerKeyArrayList.size() > position) {

                                urlAsString = mTrailerKeyArrayList.get(position);

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

            setNeedExtraFetch(Utility.needExtraFetch(getActivity()));

            // Populate Detail Fragment
            String posterpath = mMovie.posterpath;

            /* load Poster image;
             there are 2 ways of getting the poster depending on the Preference settings
             If needExtraFetch, poster's coming in from real-time online,
             if not, poster's coming from file saved in memory using movie title as file name.
            */
            if (needExtraFetch) {
                Picasso.with(getActivity()).load(posterpath).into(mPosterImageView);
            } else {
                File posterfile = getFileInInternalStorage(mMovie.title);
                if (posterfile != null) {
                    Picasso.with(getActivity()).load(posterfile).into(mPosterImageView);
                } else {
                    Log.e(LOG_TAG, "Image for poster not found.");
                }
            }

            // Populate more Details
            mTitleView.setText(mMovie.title);
            mSynopsisPlotView.setText(mMovie.overview);
            mReleaseDateView.setText(mMovie.releasedate);
            mRatingView.setText(mMovie.rating);

            // Set up Favorite Action Button
            handleMarkFavorites(rootView);

            // Set up Trailers and Reviews if we need movie api calls
            if (needExtraFetch) {
                handleTrailers(rootView);
                handleReviews(rootView);
            }
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

        try {
            trailerApiResult = myfetch.get(); // trailerName was good here with the get(), but disappears soon, so extract data here!!
            if (null != trailerApiResult) {
                int tSize = trailerApiResult.size();

                mTrailerKeyArrayList = new ArrayList<String>(tSize);

                String delim = Utility.getTrailerDelimeter();
                int delimPos = 0;

                for (String s : trailerApiResult) {
                    delimPos = s.indexOf(delim);
                    mTrailerKeyArrayList.add(s.substring(0, delimPos));
                    mTrailersAdapter.add(s.substring(delimPos + 1));
                }

                if (mTrailerKeyArrayList != null && mTrailerKeyArrayList.size() > 0) {
                    mShareActionTrailerUri = mTrailerKeyArrayList.get(0);

                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createMoviesShareIntent());
                    }
                }
            } else {
                Log.v(LOG_TAG, " trailerApiResult is null!");
            }
        } catch (Exception allEx) {
            Log.e(LOG_TAG, " Trailers async task exception " + allEx);
        }
    }


    // Awesome Movies Reviews use Explicit Intent to ReviewsActivity
    private void handleReviews(View parent) {
        mReviewsLinkView = (TextView) parent.findViewById(R.id.readReviewsLink);
        mReviewsLinkView.setClickable(true);
        mReviewsLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Set on Bundle the Parcelable Movie object
                Intent intent = new Intent(getActivity(), ReviewsActivity.class);
                Bundle mParcel = new Bundle();
                mParcel.putParcelable(DetailFragment.MOVIE_PARCEL, mMovie);

                intent.putExtra(DetailFragment.INTENT_PARCEL, mParcel);
                startActivity(intent);
            }
        });
    }

    // Favorites
    private void handleMarkFavorites(View parent) {

        final boolean isAdd;
        final boolean isRemove;

        // Set up Mark-favorite Button - these actions need to Database calls
        mFavoriteButtonView = (Button) parent.findViewById(R.id.mark_favorite);
        if (!needExtraFetch) {
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
            public void onClick (View v) {
                FavoriteMovieTask asyncAddTask = new FavoriteMovieTask(getActivity(), isAdd, isRemove);
                asyncAddTask.execute(mMovie);

                // Save the poster image to a File system to save space in the Database
                Picasso.with(getActivity()).load(mMovie.posterpath).into(target);
            }
        });
    }

    /*
        Poster image stored in a File: made public to use Detail Fragment View for the Favorite Detail as well.

        * Save poster image to a file
     */
    public final static String POSTER_FOLDER = "movieposters";


    public File getFileInInternalStorage(String titleAsName){

        File folder = getContext().getDir(POSTER_FOLDER, Context.MODE_PRIVATE);

        if (!folder.exists()){
            Log.e(LOG_TAG, "Error folder not found.");
            return null;
        }

        return new File(folder + File.separator + titleAsName + ".jpg");
    }
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

    private Intent createMoviesShareIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        String shareString = "";

        if (mShareActionTrailerUri!=null){
            shareString = String.format("%s%s", mShareActionTrailerUri, SHARE_ACTION_HASHTAG);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareString);

        return intent;
    }

   /*
            NOT HAVING TO SAVE THE mMovie for the Non-Favorite paths
            MovieFragment is saving the mm so that it does not overlay when rotated.
     */

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //if (!needExtraFetch) {
            if (mMovie != null) {
                Log.v(LOG_TAG, "onSaveInstanceState");
                outState.putParcelable(MOVIE_PARCEL, mMovie);
            }
     //   }

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
       // if (!needExtraFetch) {
            if (savedInstanceState != null) {
                if (savedInstanceState.getParcelable(MOVIE_PARCEL) != null) {
                    Log.v(LOG_TAG, "onViewStateRestored");
                    mMovie = savedInstanceState.getParcelable(MOVIE_PARCEL);
                }
        //    }
        }
        Log.v(LOG_TAG, "onViewStateRestored");

    }


}
