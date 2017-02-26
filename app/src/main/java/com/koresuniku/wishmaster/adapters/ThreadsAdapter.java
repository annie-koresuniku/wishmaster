package com.koresuniku.wishmaster.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.koresuniku.wishmaster.activities.ThreadsActivity;
import com.koresuniku.wishmaster.utilities.Constants;

import java.util.ArrayList;
import java.util.Map;

public class ThreadsAdapter extends BaseAdapter {
    private static final String LOG_TAG = ThreadsAdapter.class.getSimpleName();

    private ThreadsActivity activity;

    public ThreadsAdapter(ThreadsActivity a, ArrayList<Map<String, String>> list) {
        activity = a;
    }

    @Override
    public int getCount() {
        return activity.threadsList.size();
    }

    @Override
    public View getItem(int i) {
        return activity.itemViews.get(i);
    }

    @Override
    public long getItemId(int i) {
        if (activity.itemViews.size() - 1 <= i) return -1;
        View item = activity.itemViews.get(i);
        return activity.itemViews.indexOf(item);
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (activity.itemViews.size() - 1 <= i) return new View(activity);
        view = activity.itemViews.get(i);
        return view;
    }


//    public SpannableString setSpoilerSpans(int position, SpannableString ss) {
//        int preCount = 0;
//        for (int i = 0; i < activity.chosenPage; i++) {
//            //Map threadsPage = threadsList.get(i);
//            //Log.i(LOG_TAG, "threadsPAge " + threadsPage.keySet());
//            preCount += activity.threadsList.size();
//        }
//        //Log.i(LOG_TAG, "preCount -- " + preCount);
//        ArrayList<String> spoilersArray = activity.spoilersLocations.get(preCount + position);
//        if (spoilersArray != null) {
//            for (String spoiler : spoilersArray) {
//                String[] locals = spoiler.split(" ");
//                int start = Integer.parseInt(locals[0]);
//                int end = Integer.parseInt(locals[1]);
//                Log.i(LOG_TAG, "BEFORE SETTING SPAN");
//                Log.i(LOG_TAG, "spoiler as it " + spoiler);
//                Log.i(LOG_TAG, "ss to span " + ss.toString());
//                Log.i(LOG_TAG, "ss length " + ss.length());
//                ss.setSpan(new BackgroundColorSpan(Color.parseColor("#b4b4b4")),
//                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                ss.setSpan(new ForegroundColorSpan(Color.parseColor("#00ffffff")),
//                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//        } else {
//            Log.i(LOG_TAG, "spoilersArray IS NULL");
//        }
//        return ss;
//    }
}