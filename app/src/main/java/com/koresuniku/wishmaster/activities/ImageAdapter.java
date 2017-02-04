package com.koresuniku.wishmaster.activities;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.koresuniku.wishmaster.R;
import com.koresuniku.wishmaster.utilities.TouchImageView;

import java.util.ArrayList;

import static com.koresuniku.wishmaster.activities.SingleThreadActivity.fragmentCotainer;
import static com.koresuniku.wishmaster.activities.SingleThreadActivity.mediaPosition;
import static com.koresuniku.wishmaster.activities.SingleThreadActivity.pathsGeneral;

public class ImageAdapter extends PagerAdapter {
    private final static String LOG_TAG = ImageAdapter.class.getSimpleName();
    Context context;
    ArrayList<String> mPages;

    ImageAdapter(Context context) {
        this.context = context;
        fragmentCotainer.setVisibility(View.VISIBLE);
        fragmentCotainer.setOnClickListener(null);
    }

    @Override
    public int getCount() {
        return pathsGeneral.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((ImageView) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.i(LOG_TAG, "Inside instantiate");
//      ImageView imageView = new ImageView(context);
//      int padding = context.getResources().getDimensionPixelSize(R.dimen.padding_medium);
//      imageView.setPadding(padding, padding, padding, padding);
//      imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//      imageView.setImageResource(GalImages[position]);
//      ((ViewPager) container).addView(imageView, 0);

        String path = pathsGeneral.get(position);
        for (int i = 0; i < pathsGeneral.size(); i++) {
            if (path.equals(String.valueOf(pathsGeneral.get(i)))) {
                Log.i(LOG_TAG, "Got position!");
                mediaPosition = i;
            }
        }

        final TouchImageView newImage = new TouchImageView(context);

        Glide
                .with(context)
                .load(path)
                .placeholder(R.drawable.load_2)
                .dontAnimate()
                .into(newImage);
        //newImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //newImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        imageViewContainer.removeAllViews();
//        imageViewContainer.addView(newImage);
        ((ViewPager) container).addView(newImage, 0);

        Log.i(LOG_TAG, "Media position " + mediaPosition);
        Log.i(LOG_TAG, "Paths " + pathsGeneral);
        return newImage;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((ImageView) object);
    }
}
