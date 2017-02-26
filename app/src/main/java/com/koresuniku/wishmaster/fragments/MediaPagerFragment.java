package com.koresuniku.wishmaster.fragments;


import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.koresuniku.wishmaster.R;
import com.koresuniku.wishmaster.activities.ThreadsActivity;
import com.koresuniku.wishmaster.ui.UIUtilities;
import com.koresuniku.wishmaster.utilities.Constants;
import com.koresuniku.wishmaster.utilities.TouchImageView;
import com.squareup.picasso.Picasso;

public class MediaPagerFragment extends Fragment {
    private final String LOG_TAG = MediaPagerFragment.class.getSimpleName();
    private boolean isItemAnImage;
    private int position;
    private String number;
    private Activity activity;

    public MediaPagerFragment(Activity a, boolean b, String s, int i) {
        position = i;
        activity = a;
        isItemAnImage = b;
        number = s;
        Log.i(LOG_TAG, "position " + position);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        UIUtilities.setStatusBarTranslucent(activity, true);
        ///activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = activity.getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        View convertView;
        String path = getPath();
        if (isItemAnImage) {
            convertView = inflater.inflate(R.layout.view_pager_image_template, container, false);
            Log.i(LOG_TAG, "it works, suka");
            TouchImageView touchImageView = (TouchImageView) convertView.findViewById(R.id.touch_image_view);
            Picasso.with(activity.getApplicationContext()).load(Uri.parse(path)).into(touchImageView);
        } else {
            convertView = inflater.inflate(R.layout.view_pager_video_template, container, false);
        }
        return convertView;
    }

    private String getPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.DVACH_AUTHORITY);
        if (activity instanceof ThreadsActivity) {
            sb.append(((ThreadsActivity) activity).pathsToMediaFiles.get(number).get(position));
        }
        Log.i(LOG_TAG, "Path " + sb.toString());
        return sb.toString();
    }

}
