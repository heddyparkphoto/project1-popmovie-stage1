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

import static com.learn.heddy.awesomemovies.ListFavoritesFragment.COL_MOVIE_ID;
import static com.learn.heddy.awesomemovies.ListFavoritesFragment.COL_OVERVIEW;
import static com.learn.heddy.awesomemovies.ListFavoritesFragment.COL_POSTER_FILE_PATH;
import static com.learn.heddy.awesomemovies.ListFavoritesFragment.COL_RATING;
import static com.learn.heddy.awesomemovies.ListFavoritesFragment.COL_RELEASEDATE;
import static com.learn.heddy.awesomemovies.ListFavoritesFragment.COL_TITLE;

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

    // Flag to make extra api calls for trailers and reviews
    private boolean needExtraFetch;

    private boolean isTwoPane;

    // Trailers ArrayAdapter
    ArrayAdapter<String> mTrailersAdapter; // User-friendly names of trailers from the videos api

    ArrayList<String> mTrailerKeyArrayList;
    static private ArrayList<String> trailerApiResult;
    private static final String PLAY = ">>  ";  // Simple characters that mimic Play icon.

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
                if (mShareActionTrailerUri != null) {
                    mShareActionProvider.setShareIntent(createMoviesShareIntent());
                } else {
                    // Warn that the Share Action Provider was null
                    Log.w(LOG_TAG, "Share Action Provider was null....");
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        // get Views
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
            }
        } else if (getArguments()!= null){
            Bundle args = getArguments();
            if (args != null){
                mMovie = args.getParcelable(DetailFragment.MOVIE_PARCEL);
            }
        }

        if (null == mMovie) {

            // If it is the first time after the Favorites Collection is Selected in Tablet UI, mMovie is still null.
            // When that happens, show the first movie returned from the Favorites database with the highest rating order.
            String sort_option = Utility.getPreferredSortOption(getActivity());
            if (getString(R.string.favorites).compareTo(sort_option)==0) {
                try {

                    FavoriteDefaultMovieTask asyncTask = new FavoriteDefaultMovieTask(getActivity());
                    Cursor data = asyncTask.execute().get();

                    mMovie = new Movie();
                    mMovie.id = Integer.toString(data.getInt(COL_MOVIE_ID));
                    mMovie.posterpath = data.getString(COL_POSTER_FILE_PATH);
                    mMovie.title = data.getString(COL_TITLE);
                    mMovie.overview = data.getString(COL_OVERVIEW);
                    mMovie.rating = data.getString(COL_RATING);
                    mMovie.releasedate = data.getString(COL_RELEASEDATE);

                    // Poster image is loaded from a File system
                    Picasso.with(getActivity()).load(mMovie.posterpath).into(target);
                } catch (Exception allEx) {
                    Log.e(LOG_TAG, allEx.toString());
                }
            }
        } else if (savedInstanceState!=null){
            if (savedInstanceState.getParcelable(MOVIE_PARCEL) != null) {
                mMovie = savedInstanceState.getParcelable(MOVIE_PARCEL);
            }
        }

        if (mMovie!=null) {
            // Set up for trailers list section
            ListView trailersList = (ListView) rootView.findViewById(R.id.listview_trailer);
            mTrailersAdapter = new ArrayAdapter<String>(getActivity(), R.layout.trailer_item);
            trailersList.setAdapter(mTrailersAdapter);

            trailersList.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String urlAsString = "";
                            if (mTrailerKeyArrayList != null && mTrailerKeyArrayList.size() > position) {

                                urlAsString = mTrailerKeyArrayList.get(position);

                                Uri.Builder builder = new Uri.Builder();
                                builder.scheme("https")
                                        .authority("www.youtube.com")
                                        .appendPath("watch")
                                        .appendQueryParameter("v", urlAsString);

                                Uri uri = builder.build();
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
                    Log.e(LOG_TAG, "Poster file for movie poster not found.");
                }
            }

            // Populate the rest of the view
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

        FetchMovieExtras myfetch;

        //trailers Async
        String[] videoParams = new String[2];
        videoParams[0] = mMovie.id;
        videoParams[1] = "videos";

        myfetch = (FetchMovieExtras) new FetchMovieExtras().execute(videoParams);

        try {
            trailerApiResult = myfetch.get(); // Extract return values here before they disappear.
            if (null != trailerApiResult) {
                int tSize = trailerApiResult.size();

                mTrailerKeyArrayList = new ArrayList<String>(tSize);

                String delim = Utility.getTrailerDelimeter();
                int delimPos = 0;

                // mTrailerKeyArrayList holds the YouTube URIs
                // mTrailersAdapter holds user-friendly Trailer names.
                for (String s : trailerApiResult) {
                    delimPos = s.indexOf(delim);
                    mTrailerKeyArrayList.add(s.substring(0, delimPos));
                    mTrailersAdapter.add(PLAY+s.substring(delimPos + 1));
                }

                if (mTrailerKeyArrayList != null && mTrailerKeyArrayList.size() > 0) {
                    mShareActionTrailerUri = mTrailerKeyArrayList.get(0);

                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createMoviesShareIntent());
                    }
                }
            } else {
                // trailerApiResult is null.
            }
        } catch (Exception allEx) {
            Log.e(LOG_TAG, " Trailers async task exception " + allEx);
        }
    }


    // Reviews use Explicit Intent to ReviewsActivity
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

        // Set up Mark-favorite Button - these actions need Database calls
        mFavoriteButtonView = (Button) parent.findViewById(R.id.mark_favorite);
        if (!needExtraFetch) {
            mFavoriteButtonView.setText(getString(R.string.markRemove));
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

                // Callback to Update the Tablet UI where removed movie does not show up.
               if (isRemove && isTwoPane){
                    ((RemovedNotificationListener)getActivity()).OnRemovedItem();
                }
            }
        });
    }

    /*
        Poster image stored in a File
     */
    public final static String POSTER_FOLDER = "movieposters";

    public File getFileInInternalStorage(String titleAsName){

        File folder = getActivity().getDir(POSTER_FOLDER, Context.MODE_PRIVATE);

        if (!folder.exists()){
            Log.e(LOG_TAG, "Error movieposters folder not found.");
            return null;
        }

        return new File(folder + File.separator + titleAsName + ".jpg");
    }

    /*
        Used Picasso tutorial example to save the poster image to a file.
     */
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

    public void setTwoPane(boolean twoPane) {
        isTwoPane = twoPane;
    }

    /*
        Remove notification callback
     */
    public interface RemovedNotificationListener {
        public void OnRemovedItem();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMovie != null) {
            outState.putParcelable(MOVIE_PARCEL, mMovie);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.getParcelable(MOVIE_PARCEL) != null) {
                mMovie = savedInstanceState.getParcelable(MOVIE_PARCEL);
            }
        }
    }
}
