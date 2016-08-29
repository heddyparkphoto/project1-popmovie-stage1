package com.learn.heddy.awesomemovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by hyeryungpark on 8/27/16.
 */
public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detailcontainer, new DetailFragment())
                    .commit();
        }
    }

   /*
     *  Fragment class that shows the Movie details selected in the main screen
     */
    public static class DetailFragment extends Fragment {

        public static final String INTENT_PARCEL = "INTENT_PARCEL";
        public static final String MOVIE_PARCEL = "MOVIE_PARCEL";

        String movieid;
        String posterpathTxt;
        String titleTxt;
        String plotTxt;
        String ratingTxt;
        String releasedateTxt;

        public DetailFragment(){
            setHasOptionsMenu(true);
        }


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            // finish Views
            ImageView poster = (ImageView)rootView.findViewById(R.id.posterImage);
            TextView titleText = (TextView)rootView.findViewById(R.id.titleText);
            TextView synopsisPlotText = (TextView) rootView.findViewById(R.id.synopsisPlotText);
            TextView ratingText = (TextView) rootView.findViewById(R.id.ratingText);
            TextView releaseDateText = (TextView) rootView.findViewById(R.id.releaseDateText);

            Intent intent = getActivity().getIntent();

            if (null!=intent && null!=intent.getExtras()){
                if (null!=intent.getExtras().get("MOVIE_POSTERPATH")){
                    String posterpath = intent.getExtras().get("MOVIE_POSTERPATH").toString();
                    poster = (ImageView)rootView.findViewById(R.id.posterImage);
                    Picasso.with(getActivity()).load(posterpath).into(poster);
                }
                if (null!=intent.getExtras().get("MOVIE_TITLE")){
                    String title = intent.getExtras().get("MOVIE_TITLE").toString();
                    titleText = (TextView)rootView.findViewById(R.id.titleText);
                    titleText.setText(title);
                }

                // Test Parceable Movie
                if (null!=intent.getBundleExtra(INTENT_PARCEL)){
                    Bundle bundle = intent.getBundleExtra(INTENT_PARCEL);
                    if (bundle.getParcelable(MOVIE_PARCEL)!= null) {
                        Movie movieForDetail = bundle.getParcelable(MOVIE_PARCEL);
                        String posterpath = movieForDetail.posterpath;
                        Picasso.with(getActivity()).load(posterpath).into(poster);
                        titleText.setText(movieForDetail.title);
                        synopsisPlotText.setText(movieForDetail.overview);
                        releaseDateText.setText(movieForDetail.releasedate);
                        ratingText.setText(movieForDetail.rating);
                    }
                }
            }

            return rootView;
        }
    }
}
