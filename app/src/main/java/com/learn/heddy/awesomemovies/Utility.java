package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by hyeryungpark on 9/16/16.
 */
public class Utility {

    public static boolean needExtraFetch(Context context) {
        String prefSortOption;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        prefSortOption = sharedPreferences.getString(
                context.getString(R.string.pref_sort_by_key), context.getString(R.string.pref_default_sort_by));

        return !"favorites".equalsIgnoreCase(prefSortOption);
    }

}
