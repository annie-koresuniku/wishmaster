package com.koresuniku.wishmaster.makaba_markup;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.koresuniku.wishmaster.activities.SingleThreadActivity;
import com.koresuniku.wishmaster.utilities.Constants;

import java.util.ArrayList;

import static com.koresuniku.wishmaster.utilities.Constants.ANSWER_NUMBER_OPENED;
import static com.koresuniku.wishmaster.utilities.Constants.SPOILERS_LOCATIONS;

public class CustomLinkMovementMethod extends LinkMovementMethod {
    private static final String LOG_TAG = CustomLinkMovementMethod.class.getSimpleName();
    Spannable defaultBuffer;

    private static Context movementContext;
    private static String mComment;
    private static int mId;
    private static View viewToDispaly;
    private static int mode;
    private static CustomLinkMovementMethod linkMovementMethod = new CustomLinkMovementMethod();
    private BackgroundColorSpan backgroundColorSpan;
    private static BackgroundColorSpan spoilerBackgroundColorSpan;
    private static ForegroundColorSpan spoilerForegroundColorSpan;
    private static boolean mIsBody = true;
    private static boolean mBodyToAlter = false;
    private static SingleThreadActivity mActivity;
    private static String uniqueNumber;
    private static int position;
    private static ArrayList<String> spoilers;
    private static ArrayList<String> spoilersLocalizations;

    @Override
    public void initialize(final TextView widget, Spannable text) {

//        if (mActivity == null) {
//            Log.i(LOG_TAG, "FROM THREADS ACTIVITY");
//
//        } else {
//
//        }
//        SpannableStringBuilder builder = new SpannableStringBuilder(text);
//        String[] lines = widget.getText().toString().split("\n");
//        ArrayList<Integer[]> arrayList = new ArrayList<>();
//        spoilersLocalizations = new ArrayList<>();
//        spoilers = new ArrayList<>();
//
//        if (mActivity != null) {
//            String textToFindPosition = widget.getText().toString();
//            for (int i = 0; i < SingleThreadActivity.formattedTextGeneral.size(); i++) {
//                if (textToFindPosition.equals(SingleThreadActivity.formattedTextGeneral.get(i))) {
//                    Log.i(LOG_TAG, "Current textview position " + position);
//                    position = i;
//                    break;
//                }
//            }
//        }
//
        if (!mIsBody) {
            text.setSpan(new ForegroundColorSpan(0xFFFF7000), 0, text.length(), Spanned.SPAN_COMPOSING);
            super.initialize(widget, text);
        }

        BackgroundColorSpan[] bcss = text.getSpans(0, text.length(), BackgroundColorSpan.class);
        if (bcss.length > 0) {
            //Log.i(LOG_TAG, "OMG ITS NOT 0, ITS " + bcss.length);
        }
//
////        Pattern p = Pattern.compile("(<span[^>]+class\\s*=\\s*(\"|')spoiler\\2[^>]*>)[^<]*(</span>)");
////        Matcher m;
////        if (mActivity == null) {
////            m = p.matcher(ThreadsActivity.unformattedPageComments.get(position));
////        }
////        else {
////            m = p.matcher(SingleThreadActivity.unformattedTextGeneral.get(position));
////            Log.i(LOG_TAG, "initialization, position " + position);
////        }
////
////        while (m.find()) {
////            Log.i(LOG_TAG, "inside m.find() spoiler groups found " + m.group());
////            String match = m.group();
////            String spoiler = "";
////            for (int i = 0; i < match.length(); i++) {
////                String ch = match.substring(i, i + 1);
////                if (ch.equals(">")) {
////                    i++;
////                    String locals = i + " ";
////                    if (i + 1 >= match.length()) break;
////                    while (!match.substring(i, i + 1).equals("<")) {
////                        spoiler += match.substring(i, i + 1);
////                        i++;
////                    }
////                    locals += i;
////                    spoilers.add(spoiler);
////                    break;
//////                    break;
////                }
////            }
////        }
////
////        Log.i(LOG_TAG, "Current position " + position + " spoilers.size " + spoilers.size());
////        //Log.i(LOG_TAG, "raw comment " + SingleThreadActivity.unformattedTextGeneral.get(position));
////        if (spoilers.size() > 0 && mIsBody) {
////            for (String spoiler : spoilers) {
////                Pattern pattern = Pattern.compile(spoiler.replace(")", "\\)").replace("(", "\\("));
////                Matcher matcher;
////                if (mActivity == null) {
////                    matcher = pattern.matcher(ThreadsActivity.unformattedComments.get(position));
////                } else {
////                    matcher = pattern.matcher(SingleThreadActivity.unformattedTextGeneral.get(position));
////                }
////                Log.i(LOG_TAG, "pattern spoiler " + spoiler);
////                Log.i(LOG_TAG, "matcher text " + widget.getText());
////
////                if (matcher.matches()) {
////                    int start = matcher.start();
////                    int end = matcher.end();
////                    spoilersLocalizations.add(start + " " + end);
////                    text.setSpan(new BackgroundColorSpan(Color.parseColor("#b4b4b4")), start, end, Spanned.SPAN_COMPOSING);
////                    text.setSpan(new ForegroundColorSpan(Color.parseColor("#00ffffff")), start, end, Spanned.SPAN_COMPOSING);
////                    break;
////                }
////
////                if (matcher.find()) {
////                    int start = matcher.start();
////                    int end = matcher.end();
////                    Log.i(LOG_TAG, "start " + start + ", end " + end);
////                    spoilersLocalizations.add(start + " " + end);
////                    text.setSpan(new BackgroundColorSpan(Color.parseColor("#b4b4b4")), start, end, Spanned.SPAN_COMPOSING);
////                    text.setSpan(new ForegroundColorSpan(Color.parseColor("#00ffffff")), start, end, Spanned.SPAN_COMPOSING);
////
////                }
////            }
////            Log.i(LOG_TAG, "spoilersLocalizations init " + spoilersLocalizations.size());
////            if (Constants.SPOILERS_LOCALIZATIONS.get(position) == null) {
////                Log.i(LOG_TAG, "NEED TO ADD (NULL)");
////                Constants.SPOILERS_LOCALIZATIONS.put(position, spoilersLocalizations);
////            }
////            if (Constants.SPOILERS_LOCALIZATIONS.get(position).size() == 0) {
////                Log.i(LOG_TAG, "NEED TO ADD (0)");
////                Constants.SPOILERS_LOCALIZATIONS.put(position, spoilersLocalizations);
////            }
////        }
//        Log.i(LOG_TAG, "current comment is " + widget.getText().toString());
//        if (mActivity != null) {
//            //Log.i(LOG_TAG, "getting array for position " + position);
//            ArrayList<String> spoilersArray = Constants.SPOILERS_LOCALIZATIONS.get(position);
//            if (spoilersArray != null) {
//                for (String spoiler : spoilersArray) {
//                    String[] locals = spoiler.split(" ");
//                    int start = Integer.parseInt(locals[0]);
//                    int end = Integer.parseInt(locals[1]);
//
//                    Log.i(LOG_TAG, "current comment is " + widget.getText().toString());
//                    //BackgroundColorSpan[] bcss = text.getSpans(start, end, BackgroundColorSpan.class);
//                    if (!text.toString().equals("comment") && !text.toString().equals("") && mIsBody) {
//                        // text = new SpannableString(SingleThreadActivity.formattedTextGeneral.get(position));
//                        Log.i(LOG_TAG, "BEFORE SETTING SPAN");
//                        BackgroundColorSpan[] bcss = text.getSpans(start, end, BackgroundColorSpan.class);
//                        Log.i(LOG_TAG, "BCSS LENGTH " + bcss.length);
//                        if (bcss.length == 0) {
//                            text.setSpan(new BackgroundColorSpan(Color.parseColor("#b4b4b4")),
//                                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                            text.setSpan(new ForegroundColorSpan(Color.parseColor("#00ffffff")),
//                                    start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                        }
//                    }
//                }
//            } else {
//                Log.i(LOG_TAG, "spoilersArray IS NULL");
//            }
//        }
//
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
                //Log.i(LOG_TAG, "GOT QUOTE");
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

        super.initialize(widget, text);
    }


    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {

        if (mActivity == null) return false;
        int action = event.getAction();

        mIsBody = true;
        //Log.i(LOG_TAG, "action " + action);

        defaultBuffer = buffer;

        switch (action) {
            case 1: {

                buffer.removeSpan(backgroundColorSpan);

                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();
                x += widget.getScrollX();
                y += widget.getScrollY();

                // Locate the URL text
                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);

                if (link.length != 0) {
                    String numberToTransfer = "";
                    String url = link[0].getURL();
                    if (url.substring(0, 1).equals("/")) {
                        for (int i = 0; i < url.length() - 1; i++) {
                            if (url.substring(i, i + 1).equals("#")) {
                                numberToTransfer += url.substring(i + 1, url.length());
                            }
                        }
                        mActivity.addAnswerView(numberToTransfer);
                    }
                    if (url.substring(0, 4).equals("http")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(url));

                        int position = 0;

                        for (int i = 0; i < SingleThreadActivity.formattedTextGeneral.size(); i++) {
                            if (SingleThreadActivity.formattedTextGeneral.get(i).equals(widget.getText().toString())) {
                                position = i;
                            }
                        }

                        SingleThreadActivity.mListViewPosition = position;
                        SingleThreadActivity.mListViewPosition = position;
                        movementContext.startActivity(intent);
                    }
                }
                break;
            }

            case 0: {
                String text = widget.getText().toString();
                for (int i = 0; i < SingleThreadActivity.formattedTextGeneral.size(); i++) {
                    if (SingleThreadActivity.formattedTextGeneral.get(i).equals(text)) {
                        mId = i;
                        break;
                    }
                }
                ArrayList<String> bAndE = new ArrayList<>();

                for (int i1 = 0; i1 < text.length() - 1; i1++) {
                    String ch = String.valueOf(text.substring(i1, i1 + 2));
                    if (ch.equals(">>")) {

                        for (int v = i1; v < text.length(); v++) {
                            char c = text.charAt(v);
                            if (c == '>') continue;
                            if (c == ' ' || !(c >= '0' && c <= '9')) {
                                bAndE.add(i1 + " " + v);
                                i1 = v;
                                break;
                            }
                        }
                    }
                }

                for (int i1 = 0; i1 < text.length() - 3; i1++) {
                    String ch = String.valueOf(text.substring(i1, i1 + 4));
                    if (ch.equals("http")) {
                        for (int v = i1 + 3; v < text.length(); v++) {
                            char c = text.charAt(v);
                            if (c == ' ' || (c == '\n') || (c == ',')) {
                                Log.i(LOG_TAG, "Inside general link ending");
                                bAndE.add(i1 + " " + v);
                                if (c == ',') {
                                    Log.i(LOG_TAG, "Inside ,");
                                    bAndE.add(i1 + " " + (v - 1));
                                }
                                i1 = v;
                                break;
                            }
                        }
                    }
                }

                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();
                x += widget.getScrollX();
                y += widget.getScrollY();

                // Locate the URL text
                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                String[] linkToBeColored = new String[2];
                for (int i = 0; i < bAndE.size(); i++) {
                    String item = bAndE.get(i);
                    String[] bAndESplitted = item.split(" ");
                    for (int index = Integer.parseInt(bAndESplitted[0]); index < Integer.parseInt(bAndESplitted[1]); index++) {
                        if (off == index) {
                            linkToBeColored[0] = bAndESplitted[0];
                            linkToBeColored[1] = bAndESplitted[1];
                        }
                    }
                }

                if (linkToBeColored[0] != null) {
                    backgroundColorSpan = new BackgroundColorSpan(0x2c0066ff);
                    buffer.setSpan(backgroundColorSpan, Integer.parseInt(linkToBeColored[0]), Integer.parseInt(linkToBeColored[1]), Spanned.SPAN_COMPOSING);
                    buffer.setSpan(new ForegroundColorSpan(0xFFFF7000), Integer.parseInt(linkToBeColored[0]), Integer.parseInt(linkToBeColored[1]), Spanned.SPAN_COMPOSING);
                    buffer.setSpan(new UnderlineSpan(), Integer.parseInt(linkToBeColored[0]), Integer.parseInt(linkToBeColored[1]), Spanned.SPAN_COMPOSING);

                    //Log.i(LOG_TAG, "link is colored");
                    widget.setText(buffer);
                }
                //break;

                String[] spoilerToUnhide;
                if (mActivity != null) {
                    String textToFindPosition = widget.getText().toString();
                    for (int i = 0; i < SingleThreadActivity.formattedTextGeneral.size(); i++) {
                        if (textToFindPosition.equals(SingleThreadActivity.formattedTextGeneral.get(i))) {
                            position = i;
                            break;
                        }
                    }
                }
                //Log.i(LOG_TAG, "position " + position);
                //Log.i(LOG_TAG, "content description " + widget.getContentDescription());
                //Log.i(LOG_TAG, "SPOILERS_LOCALIZATIONS " + Constants.SPOILERS_LOCALIZATIONS.toString());


                if (mActivity != null) {
                    if (widget.getContentDescription() != null) {
                        position = Integer.parseInt(String.valueOf(widget.getContentDescription()));

                    } else {
                        Log.i(LOG_TAG, "widget.getContentDescription() == null");
//                        Log.i(LOG_TAG, "uniqueNumber " + uniqueNumber);
//                        for (int i = 0; i < numbersGeneral.size(); i++) {
//                            if (numbersGeneral.get(i).equals(uniqueNumber)) {
////                                View mainView = mThreadsListView.getAdapter().getView(i, null, null);
////                                TextView commentTextView = (TextView) mainView.findViewById(R.id.thread_item_body);
////                                position = Integer.parseInt(String.valueOf(commentTextView.getContentDescription()));
//                                Log.i(LOG_TAG, "got position " + position);
//                                position = i;
//                                break;
//                            }
//                        }
                        position = ANSWER_NUMBER_OPENED;
                        Log.i(LOG_TAG, "position " + position);
                    }

                    if (Constants.SPOILERS_LOCATIONS.get(position) == null) break;
                    ArrayList<String> spanLocals = SPOILERS_LOCATIONS.get(position);
                    //Log.i(LOG_TAG, "spanLocals " + spanLocals);
                    for (String singleSpoilerLocalization : spanLocals) {
                        //Log.i(LOG_TAG, "Inside single " + singleSpoilerLocalization);
                        //Log.i(LOG_TAG, "off " + off);
                        //Log.i(LOG_TAG, "comment length " + widget.getText().toString().length());
                        spoilerToUnhide = singleSpoilerLocalization.split(" ");
                        //Log.i(LOG_TAG, "spoiler to unhide[0] " + spoilerToUnhide[0]);
                        // Log.i(LOG_TAG, "spoiler to unhide[1] " + spoilerToUnhide[1]);
                        for (int i = Integer.parseInt(spoilerToUnhide[0]); i < Integer.parseInt(spoilerToUnhide[1]); i++) {
                            if (off == i) {
                                //Log.i(LOG_TAG, "got offset!");
                                ForegroundColorSpan[] bcsx = buffer.getSpans(
                                        Integer.parseInt(spoilerToUnhide[0]),
                                        Integer.parseInt(spoilerToUnhide[1]),
                                        ForegroundColorSpan.class);
                                if (bcsx.length == 0) {
                                    buffer.setSpan(new ForegroundColorSpan(Color.parseColor("#00ffffff")),
                                            Integer.parseInt(spoilerToUnhide[0]),
                                            Integer.parseInt(spoilerToUnhide[1]),
                                            Spanned.SPAN_COMPOSING
                                    );
                                } else {
                                    for (ForegroundColorSpan s : bcsx) {
                                        // Log.i(LOG_TAG, "SPAN REMOVED");
                                        buffer.removeSpan(s);
                                    }
                                }
                                break;
                            }
                        }

                    }
                    break;
                }
            }

//            case 0: {
//                buffer.removeSpan(backgroundColorSpan);
//                widget.setText(buffer);
//            }
            case 3: {
                buffer.removeSpan(backgroundColorSpan);
                widget.setText(buffer);
            }

        }
        return true;
    }


    public static MovementMethod getInstance(Context c, boolean isBody, SingleThreadActivity activity, String number, int pos, SpannableString ss) {
        //Log.i(LOG_TAG, "getInstance");
        mIsBody = isBody;
        movementContext = c;
        mActivity = activity;
        uniqueNumber = number;
        position = pos;

        Constants.HIDDEN_STATE.put(position, 0);
        spoilerForegroundColorSpan = new ForegroundColorSpan(Color.parseColor("#00ffffff"));

        return linkMovementMethod;
    }
}
