package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by hyeryungpark on 8/27/16.
 */
public class OneMovieAdapter extends ArrayAdapter<Movie>  {

    /*
        Custom ArrayAdapter constructor invokes super constructor with layout resource '0', then,
        getView() invokes the inflate the ArrayAdapter view layout one_movie_item.xml
        Populate only the poster of a Movie object that has more fields than posterpath
        for Detail Activity View when item clicked.
     */

    public OneMovieAdapter(Context context, List<Movie> posterarray) {
        super(context, 0, posterarray);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = getContext();

        LayoutInflater inflater = LayoutInflater.from(context);
        View oneView = inflater.inflate(R.layout.one_movie_item, parent, false);

        ImageView myimage = (ImageView) oneView.findViewById(R.id.one_movie_item_imageview);

        Picasso.with(context).load(getItem(position).posterpath).into(myimage);

        return oneView;
    }

}
