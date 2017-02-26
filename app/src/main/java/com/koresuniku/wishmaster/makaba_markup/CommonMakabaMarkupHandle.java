package com.koresuniku.wishmaster.makaba_markup;


import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.koresuniku.wishmaster.activities.ThreadsActivity;
import com.koresuniku.wishmaster.utilities.Constants;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonMakabaMarkupHandle {
    private static final String LOG_TAG = CommonMakabaMarkupHandle.class.getSimpleName();

    public static void getSpoilers(Activity activity, int position) {
        //Log.i(LOG_TAG, "getSpoilers(), " + position);
        ArrayList<String> spoilersLocations = new ArrayList<>();
        ArrayList<String> spoilers = new ArrayList<>();
        Pattern p = Pattern
                .compile("(<span[^>]+class\\s*=\\s*(\"|')spoiler\\2[^>]*>)[^<]*(</span>)");
        Matcher m = p.matcher("");
        if (activity instanceof ThreadsActivity) {
            m = p.matcher(((ThreadsActivity) activity).unformattedComments.get(position));
        }
        //Log.i(LOG_TAG, "position received " + position);
        //Log.i(LOG_TAG, "unformattedCommentn " + unformattedComments.get(position));
        while (m.find()) {
            String match = m.group();
            String spoiler = "";
            for (int i = 0; i < match.length(); i++) {
                String ch = match.substring(i, i + 1);
                if (ch.equals(">")) {
                    i++;
                    String locals = i + " ";
                    if (i + 1 >= match.length()) break;
                    while (!match.substring(i, i + 1).equals("<")) {
                        spoiler += match.substring(i, i + 1);
                        i++;
                    }
                    locals += i;
                    spoilers.add(spoiler);
                    break;
                }
            }
        }
        //Log.i(LOG_TAG, "spoilers " + spoilers);
        if (spoilers.size() > 0) {
            String commentFormatted = "";
            if (activity instanceof ThreadsActivity) {
                commentFormatted = ((ThreadsActivity) activity).formattedTextsGeneral.get(position);
            }
            int start = 0;
            for (String spoiler : spoilers) {
                int loopCounter = 0;
                for (int i = start; i < commentFormatted.length() - spoiler.length() + 1; i++) {
                    //Log.i(LOG_TAG, "search spoiler " + commentFormatted.substring(i, i + spoiler.length()));
                    //Log.i(LOG_TAG, "loopCounter " + loopCounter);
                    loopCounter++;
                    //Log.i(LOG_TAG, "spoiler length " + spoiler.length() + ", search length " + commentFormatted.substring(i, i + spoiler.length()).length());
                    if (commentFormatted.substring(i, i + spoiler.length()).equals(spoiler)) {
                        //Log.i(LOG_TAG, "got spoiler " + commentFormatted.substring(i, i + spoiler.length()));
                        int end = i + spoiler.length();
                        //Log.i(LOG_TAG, "end " + end);
                        spoilersLocations.add(i + " " + end);
                        start = i + spoiler.length();
                        break;
                    }
                }
            }

            if (activity instanceof ThreadsActivity) {
                if (((ThreadsActivity) activity).spoilersLocations.get(position) == null
                        || ((ThreadsActivity) activity).spoilersLocations.size() == 0) {
                    ((ThreadsActivity) activity).spoilersLocations.put(position, spoilersLocations);
                }
            }
//                if (Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.get(position) == null) {
//                    Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.put(position, spoilersLocations);
//                }
//                if (Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.get(position).size() == 0) {
//                    Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.put(position, spoilersLocations);
//                }
        }
    }

}
