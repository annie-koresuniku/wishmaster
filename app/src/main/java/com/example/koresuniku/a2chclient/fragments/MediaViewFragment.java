package com.example.koresuniku.a2chclient.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.koresuniku.a2chclient.R;
import com.squareup.picasso.Picasso;

public class MediaViewFragment extends Fragment {
    private String intentMedia;
    private ImageView mShowImage;
    private FrameLayout mImageContainer;
    //private FrameLayout mMainContainer;

    public MediaViewFragment(String media) {
        intentMedia = media;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.media_fragment, container, false);

        mImageContainer = (FrameLayout) rootView.findViewById(R.id.image_view_container);
        //mMainContainer = (FrameLayout) rootView.findViewById(R.id.main_container);

        mShowImage = new ImageView(getActivity());
        Picasso.with(getActivity()).load(intentMedia).into(mShowImage);

        mImageContainer.addView(mShowImage);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.media_fragment_menu, menu);
    }
}
