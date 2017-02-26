package com.koresuniku.wishmaster.makaba_markup;


import android.app.Activity;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.koresuniku.wishmaster.activities.ThreadsActivity;

import java.util.ArrayList;

public class CommentLinkMovementMethod extends LinkMovementMethod {
    private static final String LOG_TAG = CommentLinkMovementMethod.class.getSimpleName();
    private Activity activity;
    private int commentPosition;

    public CommentLinkMovementMethod(Activity ac, int cp) {
        activity = ac;
        commentPosition = cp;
    }


    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        return super.onTouchEvent(widget, buffer, event);
    }

    @Override
    public void initialize(TextView widget, Spannable text) {
        setQuotingSpan(widget, text);
        setSpoilerSpans(commentPosition, text);
        super.initialize(widget, text);
    }

    private void setQuotingSpan(TextView widget, Spannable text) {
        String[] lines = widget.getText().toString().split("\n");
        int start = 0;
        int end = 0;
        for (int i = 0; i < lines.length; i++) {
            //Log.i(LOG_TAG, "Inside for");

            if (lines[i].length() < 2) {
                //Log.i(LOG_TAG, "Line less than 2");
                start += lines[i].length() + 1;
                end += lines[i].length() + 1;
            } else if (lines[i].substring(0, 1).equals(">") && !(lines[i].substring(1, 2).equals(">"))) {
                //Log.i(LOG_TAG, "Line bigger than 2");
                //Log.i(LOG_TAG, "Line as it " + lines[i]);
                Log.i(LOG_TAG, "GOT QUOTE");
                int localEnd = start + lines[i].length();
                //Log.i(LOG_TAG, "Start " + start);
                //Log.i(LOG_TAG, "End " + end);
                text.setSpan(new ForegroundColorSpan(0xFF6B8E23), start, localEnd, Spanned.SPAN_COMPOSING);
                start += lines[i].length() + 1;
                end += lines[i].length() + 1;
            } else {
                start += lines[i].length() + 1;
                end += lines[i].length() + 1;
            }
        }
    }

    private Spannable setSpoilerSpans(int position, Spannable text) {
        ArrayList<String> spoilersArray = new ArrayList<>();
        if (activity instanceof ThreadsActivity) {
            spoilersArray = ((ThreadsActivity) activity).spoilersLocations.get(position);
        }
        //ArrayList<String> spoilersArray = Constants.SPOILERS_LOCATIONS.get(position);
        if (spoilersArray != null) {
            for (String spoiler : spoilersArray) {
                String[] locals = spoiler.split(" ");
                int start = Integer.parseInt(locals[0]);
                int end = Integer.parseInt(locals[1]);
                Log.i(LOG_TAG, "BEFORE SETTING SPAN " + start + " " + end);
                text.setSpan(new BackgroundColorSpan(Color.parseColor("#b4b4b4")),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.setSpan(new ForegroundColorSpan(Color.parseColor("#00ffffff")),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
        } else {
            Log.i(LOG_TAG, "spoilersArray IS NULL");
        }
        return text;
    }
}
