package com.example.koresuniku.a2chclient.utilities;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
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

import com.example.koresuniku.a2chclient.activities.SingleThreadActivity;

import java.util.ArrayList;

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
    private static boolean mIsBody = true;
    private static boolean mBodyToAlter = false;
    private static SingleThreadActivity mActivity;
    private static String uniqueNumber;

    @Override
    public void initialize(final TextView widget, Spannable text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        String[] lines = widget.getText().toString().split("\n");
        ArrayList<Integer[]> arrayList = new ArrayList<>();


        if (backgroundColorSpan != null) {
            //text.removeSpan(backgroundColorSpan);
        }
        if (!mIsBody) {
            text.setSpan(new ForegroundColorSpan(0xFFFF7000), 0, text.length(), Spanned.SPAN_COMPOSING);
            super.initialize(widget, text);
        }

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
        int action = event.getAction();

        mIsBody = true;
        //Log.i(LOG_TAG, "action " + action);

        defaultBuffer = buffer;

        switch (action) {
            case 1: {

                buffer.removeSpan(backgroundColorSpan);
               // widget.setText(buffer);


                //Log.i(LOG_TAG, "bAndE " + bAndE.toString());

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

                    widget.setText(buffer);

                }
                break;
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


    public static MovementMethod getInstance(Context c, boolean isBody, SingleThreadActivity activity, String number) {
        mIsBody = isBody;
        movementContext = c;
        mActivity = activity;
        uniqueNumber = number;
        return linkMovementMethod;
    }
}
