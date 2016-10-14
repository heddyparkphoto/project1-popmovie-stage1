package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by hyeryungpark on 9/19/16.
 */
public class FavoriteMovieAdapter extends CursorAdapter {

    private static final String LOG_TAG = FavoriteMovieAdapter.class.getSimpleName();
    private Context mContext;

    public FavoriteMovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    public static class FavoriteViewHolder {
        //Class view tags for convenient binding
        public final ImageView imageView;

        public FavoriteViewHolder(View v){
            imageView = (ImageView) v.findViewById(R.id.one_movie_item_imageview);
        }
    }

    /*
       Remember that these views are reused as needed.
    */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.one_movie_item, parent, false);

        //this holder is a convenient obj with all database fields to quickly map in bindView
        FavoriteViewHolder holder = new FavoriteViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        FavoriteViewHolder holder = (FavoriteViewHolder)view.getTag();

        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        File posterfile = getFileInInternalStorage(cursor.getString(ListFavoritesFragment.COL_TITLE));
        if (posterfile != null) {
            Picasso.with(context).load(posterfile).into(holder.imageView);
        } else {
            Log.e(LOG_TAG, "File for poster image not found.");
        }
    }

    /*
        Poster image stored in a File: DetailFragment when viewing a Favorite Movie and this Adapter must use the same folder.
     */
    public File getFileInInternalStorage(String titleAsName){

        File folder = mContext.getDir(DetailFragment.POSTER_FOLDER, Context.MODE_PRIVATE);

        if (!folder.exists()){
            Log.e(LOG_TAG, "Error folder not found.");
            return null;
        }

        return new File(folder + File.separator + titleAsName + ".jpg");
    }
}
