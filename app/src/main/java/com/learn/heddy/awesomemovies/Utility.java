package com.learn.heddy.awesomemovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by hyeryungpark on 9/16/16.
 */
public class Utility {

    private static final String COMMA = ",";

    public static boolean needExtraFetch(Context context) {
        String prefSortOption;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        prefSortOption = sharedPreferences.getString(
                context.getString(R.string.pref_sort_by_key), context.getString(R.string.pref_default_sort_by));

        return !"favorites".equalsIgnoreCase(prefSortOption);
    }

    public static String formatTrailerString(String key, String name) {

        String retVal = key.concat(COMMA).concat(name);

        return retVal;
    }

    public static String getTrailerDelimeter() {
        return COMMA;
    }

    public static String getPreferredSortOption(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_by_key),
                context.getString(R.string.pref_default_sort_by));
    }
}
