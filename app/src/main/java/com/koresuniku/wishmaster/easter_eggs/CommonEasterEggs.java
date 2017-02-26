package com.koresuniku.wishmaster.easter_eggs;


import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

import com.koresuniku.wishmaster.R;
import com.koresuniku.wishmaster.activities.MainActivity;
import com.koresuniku.wishmaster.utilities.Constants;

public class CommonEasterEggs {
    public static void checkIfGill(MainActivity activity) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        int jill = Integer.parseInt(sharedPreferences.getString(
                Constants.JILL_STRING, String.valueOf(Constants.JILL_COUNTER)));

        if (jill >= 1) {
            int orientation = activity.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.mMainLinearLayout.setBackgroundResource(R.drawable.jill_ver);
            } else {
                activity.mMainLinearLayout.setBackgroundResource(R.drawable.jill_land);
            }
        }
    }
}
