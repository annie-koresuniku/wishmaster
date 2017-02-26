package com.koresuniku.wishmaster.adapters;


import android.app.ActionBar;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.koresuniku.wishmaster.activities.ThreadsActivity;
import com.koresuniku.wishmaster.fragments.MediaPagerFragment;


public class MediaFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private static final String LOG_TAG = MediaFragmentPagerAdapter.class.getSimpleName();
    private boolean isItemAnImage;
    private String number;
    private Activity activity;


    public MediaFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public MediaFragmentPagerAdapter(FragmentManager fm, Activity a, boolean b, String s) {
        super(fm);
        Log.i(LOG_TAG, "MFPA");
        number = s;
        activity = a;
        isItemAnImage = b;
    }


    @Override
    public Fragment getItem(int position) {
        Log.i(LOG_TAG, "getItem()");
        MediaPagerFragment mpf = new MediaPagerFragment(activity,
                isItemAnImage,
                number,
                position);
        return mpf;
//            return MediaPagerFragment.newInstance(
//                    activity,
//                    isItemAnImage,
//                    number,
//                    position);
    }

//        @Override
//        public long getItemId(int position) {
//            return position;
//        }

    @Override
    public int getCount() {
        int count;
        if (activity instanceof ThreadsActivity) {
            count = ((ThreadsActivity) activity).pathsToMediaFiles.get(number).size();
        } else {
            count = 0;
        }
        return count;
    }

}
