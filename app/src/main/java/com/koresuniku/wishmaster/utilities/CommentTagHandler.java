package com.koresuniku.wishmaster.utilities;


import android.graphics.Color;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.koresuniku.wishmaster.activities.SingleThreadActivity;
import com.koresuniku.wishmaster.activities.ThreadsActivity;

import org.xml.sax.XMLReader;

import java.util.ArrayList;

public class CommentTagHandler implements Html.TagHandler {
    private static final String LOG_TAG = CommentTagHandler.class.getSimpleName();

    private static final String SPAN_TAG = "span";
    private boolean spanSetted;
    private int mPosition;
    private boolean mFromSingleThreadActivity;
    private TextView mWidget;

    public CommentTagHandler(int position, boolean fromSingleThreadActivity, TextView widget) {
        // Log.i(LOG_TAG, "CommentTagHandler object created");
        //spanSetted = false;
        mWidget = widget;
        if (fromSingleThreadActivity) {
            //mPosition = SingleThreadActivity.formattedTextsGeneral.indexOf(widget.getText().toString());
            mPosition = position;
        }
        mFromSingleThreadActivity = fromSingleThreadActivity;
    }

    @Override
    public void handleTag(boolean b, String tag, Editable editable, XMLReader xmlReader) {

        //Log.i(LOG_TAG, "mFrom " + mFromSingleThreadActivity);

        if (mFromSingleThreadActivity) {
//                Log.i(LOG_TAG, "received index " + mPosition);
            //Log.i(LOG_TAG, "received text " + editable);

            mPosition = SingleThreadActivity.formattedTextsGeneral.indexOf(editable.toString());
            //Log.i(LOG_TAG, "received position " + mPosition);

            ArrayList<String> spoilers = Constants.SPOILERS_LOCATIONS.get(mPosition);

            if (spoilers == null) return;
            //Log.i(LOG_TAG, "BEFORE SETTING SPAN 1");

            for (String spoiler : spoilers) {
                String[] bAndE = spoiler.split(" ");
                int start = Integer.parseInt(bAndE[0]);
                int end = Integer.parseInt(bAndE[1]);
                if (editable.length() <= start || editable.length() <= end) continue;
                //Log.i(LOG_TAG, "BEFORE SETTING SPAN 2");
                editable.setSpan(new BackgroundColorSpan(Color.parseColor("#b4b4b4")),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                editable.setSpan(new ForegroundColorSpan(Color.parseColor("#00ffffff")),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        } else {
//            mPosition = ThreadsActivity.formattedTextsGeneral.indexOf(editable.toString());
            //Log.i(LOG_TAG, "received position " + mPosition);

            ArrayList<String> spoilers = Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.get(mPosition);

            if (spoilers == null) return;
            //Log.i(LOG_TAG, "BEFORE SETTING SPAN 1");

            for (String spoiler : spoilers) {
                String[] bAndE = spoiler.split(" ");
                int start = Integer.parseInt(bAndE[0]);
                int end = Integer.parseInt(bAndE[1]);
                // if (editable.length() <= start || editable.length() <= end) continue;
                //Log.i(LOG_TAG, "BEFORE SETTING SPAN 2");
                editable.setSpan(new BackgroundColorSpan(Color.parseColor("#b4b4b4")),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                editable.setSpan(new ForegroundColorSpan(Color.parseColor("#00ffffff")),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        }

    }
}
