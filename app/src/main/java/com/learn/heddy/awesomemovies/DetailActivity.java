package com.learn.heddy.awesomemovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;

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

}
