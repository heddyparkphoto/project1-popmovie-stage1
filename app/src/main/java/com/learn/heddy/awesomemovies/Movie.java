package com.learn.heddy.awesomemovies;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by hyeryungpark on 8/27/16.
 *
 * Object that populates all the information for the Movie app UI
 *
 * AsyncTask creates an instance of this object and set all fields when it does json string parsing
 *
 * The fields are:
 *     final String KEY_ID = "id";
 *  final String KEY_POSTER_PATH = "poster_path";
 *  final String KEY_TITLE = "title";
 *  final String KEY_PLOT = "overview";
 *  final String KEY_RATING_PART1 = "vote_average";
 *  final String KEY_RATING_PART2 = "vote_count";
 *  final String KEY_RELEASE_DATE   = "release_date";
 */
public class Movie implements Parcelable {

    String id;
    String posterpath;
    String title;
    String overview;
    String rating; //vote_average/vote_count makes up the rating
    String releasedate;

    @Override
    public String toString() {

        // Movie title will be good for String representation of the Movie object
        return this.title;
    }

    // no argument constructor
    public Movie(){
        super();
    }
    /*
            Code assist generated the Parcelable constructor and
            the Creator<Movie> class
         */
    protected Movie(Parcel in) {
        id = in.readString();
        posterpath = in.readString();
        title = in.readString();
        overview = in.readString();
        rating = in.readString();
        releasedate = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {

            Log.v("Movie", "Creating ...");
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            Log.v("Movie", "in newArrar ...");
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.v("Movie", "WRITING Zzzzzzz");

        dest.writeString(id);
        dest.writeString(posterpath);
        dest.writeString(title);
        dest.writeString(overview);
        dest.writeString(rating);
        dest.writeString(releasedate);
    }
}
