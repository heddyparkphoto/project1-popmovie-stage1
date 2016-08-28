package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by hyeryungpark on 8/27/16.
 */
public class OneMovieAdapter extends ArrayAdapter<Movie>  {
    private Context mContext;

    /*
        Custom ArrayAdapter uses constructor ??
        one_movie_item_textview is in the layout xml for one view and
        populated from the title String of a Movie object that has a more fields
        for Detail Activity View.  Movie object overrides toString() method in order
        to fulfill ArrayAdapter TextView representation requirement ... check and test
        to be sure that is the case.
     */

    public OneMovieAdapter(Context context, List<Movie> posterarray) {
        super(context, R.layout.one_movie_item, R.id.one_movie_item_textview, posterarray);

        mContext = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View oneView = inflater.inflate(R.layout.one_movie_item, parent, false);

        ImageView myimage = (ImageView) oneView.findViewById(R.id.one_movie_item_imageview);
        TextView mytext = (TextView) oneView.findViewById(R.id.one_movie_item_textview);

        //PosterView myimage = new PosterView(mContext);
        //Grid layout enhance with a Sample GridView layout
        //myimage.setAdjustViewBounds(true);
        //myimage.setLayoutParams(new GridView.LayoutParams(parent.getMeasuredWidth(), parent.getMeasuredHeight()));
        myimage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        myimage.setPadding(2, 2, 2, 2);

        //layout helper methods - fit 2 columns if portrait or small width device-later 3 columns
//        ViewGroup.LayoutParams params = parent.getLayoutParams();
//        params.width = parent.getWidth() / 2;
//        myimage.setLayoutParams(params);

        Picasso.with(mContext).load(getItem(position).posterpath).into(myimage);

        mytext.setText(getItem(position).title);
        //mytext.setText(getItem(position).toString());  //JUST EXPERIMENT ... mandatory String representation in ArrayAdapter
        return oneView;
    }

}
