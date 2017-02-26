package com.koresuniku.wishmaster.asynktasks;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koresuniku.wishmaster.R;
import com.koresuniku.wishmaster.activities.ErrorActivity;
import com.koresuniku.wishmaster.activities.SingleThreadActivity;
import com.koresuniku.wishmaster.activities.ThreadsActivity;
import com.koresuniku.wishmaster.makaba_markup.CommentLinkMovementMethod;
import com.koresuniku.wishmaster.makaba_markup.CommonMakabaMarkupHandle;
import com.koresuniku.wishmaster.utilities.CommentTagHandler;
import com.koresuniku.wishmaster.utilities.Constants;
import com.koresuniku.wishmaster.makaba_markup.CustomLinkMovementMethod;
import com.koresuniku.wishmaster.utilities.NetworkUtilities;
import com.koresuniku.wishmaster.utilities.SwipeRefreshLayoutBottom;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.koresuniku.wishmaster.utilities.Constants.JSON_PAGES;
import static com.koresuniku.wishmaster.utilities.Constants.actionBarView;


public class ThreadsActivityAsynktasks {
    private final static String LOG_TAG = ThreadsActivityAsynktasks.class.getSimpleName();

    public static class GetPagesOnBoardTask extends AsyncTask<Void, Void, Void> {
        ThreadsActivity activity;

        public GetPagesOnBoardTask(ThreadsActivity a) {
            activity = a;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (NetworkUtilities.isOnline(activity)) {
                try {
                    URL url = new URL("https://2ch.hk/" + activity.intentBoard + "/index" + ".json");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 404) {
                        Intent intent = new Intent(activity.getApplicationContext(), ErrorActivity.class);
                        activity.startActivity(intent);
                        return null;
                    } else {
                        StringBuilder builder = new StringBuilder();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }

                        String rawJSON = builder.toString();
                        activity.pagesOnBoard = getResult(rawJSON);
                        getBanner(rawJSON);

                        for (int i = 0; i < Integer.parseInt(activity.pagesOnBoard) - 1; i++) {
                            if (i == 0) {
                                url = new URL("https://2ch.hk/" + activity.intentBoard + "/index" + ".json");
                            } else {
                                url = new URL("https://2ch.hk/" + activity.intentBoard + "/" + i + ".json");
                            }
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.connect();
                            builder = new StringBuilder();
                            reader = new BufferedReader(
                                    new InputStreamReader(connection.getInputStream()));
                            line = "";
                            while ((line = reader.readLine()) != null) {
                                builder.append(line);
                            }
                            JSON_PAGES.add(builder.toString());
                            Log.i(LOG_TAG, "url " + url);
                            if (i == 0) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(LOG_TAG, "starting threadTask...");
                                        ThreadsTask threadsTask = new ThreadsTask(activity);
                                        threadsTask.execute();
                                    }
                                });
                            }
                        }
                        return null;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
        }

        private String getResult(String rawJSON) {
            try {
                JSONObject main = new JSONObject(rawJSON);
                JSONArray pagesArray = main.getJSONArray("pages");
                return String.valueOf(pagesArray.length());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void getBanner(String rawJSON) {
            try {
                JSONObject main = new JSONObject(rawJSON);
                String pathImage = main.getString("board_banner_image");
                String pathLink = main.getString("board_banner_link");
                activity.banner[0] = pathImage;
                activity.banner[1] = pathLink;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ThreadsTask extends AsyncTask<Void, Void, ArrayList<ArrayList<Map<String, String>>>> {
        ThreadsActivity activity;

        ThreadsTask(ThreadsActivity a) {
            Log.i(LOG_TAG, "ThreadTask started...");
            activity = a;
        }

        private ArrayList<Map<String, String>> currentPageItem;

        @Override
        protected ArrayList<ArrayList<Map<String, String>>> doInBackground(Void... strings) {
            Log.i(LOG_TAG, "json pages " + JSON_PAGES.size());
            currentPageItem = formatJSON(JSON_PAGES.get(activity.chosenPage));
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<Map<String, String>>> maps) {
            CreateViewsTask cvt = new CreateViewsTask(activity);
            cvt.execute();
        }

//        private void getSpoilers(int position) {
//            //Log.i(LOG_TAG, "getSpoilers(), " + position);
//            ArrayList<String> spoilersLocations = new ArrayList<>();
//            ArrayList<String> spoilers = new ArrayList<>();
//            Pattern p = Pattern
//                    .compile("(<span[^>]+class\\s*=\\s*(\"|')spoiler\\2[^>]*>)[^<]*(</span>)");
//            Matcher m = p.matcher(unformattedComments.get(position));
//            //Log.i(LOG_TAG, "position received " + position);
//            //Log.i(LOG_TAG, "unformattedCommentn " + unformattedComments.get(position));
//            while (m.find()) {
//                String match = m.group();
//                String spoiler = "";
//                for (int i = 0; i < match.length(); i++) {
//                    String ch = match.substring(i, i + 1);
//                    if (ch.equals(">")) {
//                        i++;
//                        String locals = i + " ";
//                        if (i + 1 >= match.length()) break;
//                        while (!match.substring(i, i + 1).equals("<")) {
//                            spoiler += match.substring(i, i + 1);
//                            i++;
//                        }
//                        locals += i;
//                        spoilers.add(spoiler);
//                        break;
//                    }
//                }
//            }
//            //Log.i(LOG_TAG, "spoilers " + spoilers);
//            if (spoilers.size() > 0) {
//
//                String commentFormatted = formattedTextsGeneral.get(position);
//
//                int start = 0;
//                for (String spoiler : spoilers) {
//                    int loopCounter = 0;
//                    for (int i = start; i < commentFormatted.length() - spoiler.length() + 1; i++) {
//                        //Log.i(LOG_TAG, "search spoiler " + commentFormatted.substring(i, i + spoiler.length()));
//                        //Log.i(LOG_TAG, "loopCounter " + loopCounter);
//                        loopCounter++;
//                        //Log.i(LOG_TAG, "spoiler length " + spoiler.length() + ", search length " + commentFormatted.substring(i, i + spoiler.length()).length());
//                        if (commentFormatted.substring(i, i + spoiler.length()).equals(spoiler)) {
//                            //Log.i(LOG_TAG, "got spoiler " + commentFormatted.substring(i, i + spoiler.length()));
//                            int end = i + spoiler.length();
//                            //Log.i(LOG_TAG, "end " + end);
//                            spoilersLocations.add(i + " " + end);
//                            start = i + spoiler.length();
//                            break;
//                        }
//                    }
//                }
//                if (Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.get(position) == null) {
//                    Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.put(position, spoilersLocations);
//                }
//                if (Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.get(position).size() == 0) {
//                    Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.put(position, spoilersLocations);
//                }
//            }
//        }

        private ArrayList<Map<String, String>> formatJSON(String rawJSON) {
            ArrayList<Map<String, String>> result = new ArrayList<>();
            //temporaryThreadList = new ArrayList<>();
            Map<String, String> item;
            int threadsCount = 0;
            int counter = 0;
            try {
                JSONObject main = new JSONObject(rawJSON);
                Log.i(LOG_TAG, "JSONObject main is null " + (main == null));
                activity.boardName = main.getString("BoardName");
                activity.defaultOpName = main.getString("default_name");
                JSONArray threadsArray = main.getJSONArray("threads");

                for (int v = 0; v < threadsArray.length(); v++) {
                    JSONObject thread = threadsArray.getJSONObject(v);

                    activity.answersCount = thread.getString("posts_count");
                    activity.number = thread.getString("thread_num");
                    activity.filesCount = thread.getString("files_count");

                    JSONArray postArray = thread.getJSONArray("posts");
                    JSONObject post = postArray.getJSONObject(0);
                    activity.date = post.getString("date");
                    activity.op = post.getString("op");
                    activity.comment = post.getString("comment");
                    activity.subjectOfThread = post.getString("subject");
                    activity.opName = post.getString("name");

                    JSONArray filesArray = post.getJSONArray("files");
                    if (!(filesArray.length() == 0)) {
                        JSONObject file = filesArray.getJSONObject(0);
                        activity.thumb = file.getString("thumbnail");
                        activity.path = file.getString("path");
                        activity.size = file.getString("size");
                        activity.width = file.getString("width");
                        activity.height = file.getString("height");
                        if (activity.path.substring(activity.path.length() - 4, activity.path.length()).equals("webm")) {
                            activity.duration = file.getString("duration");
                        } else {
                            activity.duration = "";
                        }
                        Map<Integer, String> singleCommentPaths = new HashMap<>();
                        for (int i = 0; i < filesArray.length(); i++) {
                            JSONObject object = filesArray.getJSONObject(i);
                            singleCommentPaths.put(i, object.getString("path"));
                        }
                        activity.pathsToMediaFiles.put(thread.getString("thread_num"), singleCommentPaths);
                        //Log.i(LOG_TAG, "pathsToMediaFiles is null " + (activity.pathsToMediaFiles == null));

                    } else {
                        activity.thumb = "";
                    }

                    item = new HashMap<>();
                    item.put(Constants.ANSWERS_COUNT, activity.answersCount);
                    item.put(Constants.NUMBER, activity.number);
                    item.put(Constants.FILES_COUNT, activity.filesCount);
                    item.put(Constants.DATE, activity.date);
                    item.put(Constants.OP, activity.op);
                    item.put(Constants.COMMENT, activity.comment);
                    item.put(Constants.THUMB, activity.thumb);
                    item.put(Constants.SUBJECT_OF_THREAD, activity.subjectOfThread);
                    item.put(Constants.OP_NAME, activity.opName);
                    item.put(Constants.SIZE, activity.size);
                    item.put(Constants.HEIGHT, activity.height);
                    item.put(Constants.WIDTH, activity.width);
                    item.put(Constants.PATH, activity.path);
                    item.put(Constants.DURATION, activity.duration);

                    activity.unformattedComments.add(activity.comment);
                    activity.numbersGeneral.add(activity.number);

                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                    spannableStringBuilder.append(activity.comment);
                    String commentFormatted = String.valueOf(Html.fromHtml(spannableStringBuilder.toString()));
                    activity.formattedTextGeneral.put(counter, commentFormatted);
                    activity.formattedTextsGeneral.add(commentFormatted);
//                    getSpoilers(counter + Constants.THREADS_ITEMS_LOADED);
//                    counter++;

                    activity.threadsList.add(item);
                    activity.temporaryThreadList.add(item);
                    threadsCount++;
                }

                if (activity.firstTimeLoaded) {
                    activity.firstTimeLoaded = false;
                } else {
                    activity.threadItemsLoaded += threadsCount;
                }
//
//                for (int i = 0; i < temporaryThreadList.size(); i++) {
//                    getSpoilers(i + Constants.THREADS_ITEMS_LOADED);
//                }
//                Log.i(LOG_TAG, "Constants.THREADS_ITEMS_LOADED " + Constants.THREADS_ITEMS_LOADED);
//                Log.i(LOG_TAG, "unformattedComment.size() " + unformattedComments.size());
                return result;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private static class CreateViewsTask extends AsyncTask<Void, Void, Void> {
        ThreadsActivity activity;

        CreateViewsTask(ThreadsActivity a) {

            activity = a;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i(LOG_TAG, "threadList " + activity.threadsList.size());

            int additional = 0;
            if (activity.chosenPage != 0) {
                additional = activity.threadItemsLoaded;
                Log.i(LOG_TAG, "additional " + additional);
                Log.i(LOG_TAG, "threadsList.size " + activity.threadsList.size());
            }


            for (int i = additional; i < activity.threadsList.size(); i++) {

                Log.i(LOG_TAG, "i " + i);

                final ThreadsActivity.ViewHolder viewHolder = new ThreadsActivity.ViewHolder();

                final View rootView = activity.mLayoutInflater.inflate(R.layout.thread_item_single_image, null, false);

                Map<String, String> item = activity.threadsList.get(i);
                //Log.i(LOG_TAG, "arraylist size " + threadsList.get(chosenPage).size());
                String date = item.get(Constants.DATE);
                final String number = item.get(Constants.NUMBER);
                final String thumb = item.get(Constants.THUMB);
                String comment = item.get(Constants.COMMENT);
                String op = item.get(Constants.OP);
                String answersCount = item.get(Constants.ANSWERS_COUNT);
                String filesCount = item.get(Constants.FILES_COUNT);
                String subjectOfThread = item.get(Constants.SUBJECT_OF_THREAD);
                String opName = item.get(Constants.OP_NAME);
                String displayName = item.get(Constants.DISPLAY_NAME);
                String size = item.get(Constants.SIZE);
                String width = item.get(Constants.WIDTH);
                String height = item.get(Constants.HEIGHT);
                String path = item.get(Constants.PATH);
                String duration = item.get(Constants.DURATION);

                //Log.i(LOG_TAG, "comment " + comment);

                activity.unformattedPageComments.add(comment);

                rootView.setContentDescription(number);

                viewHolder.mThreadItemHeader =
                        (TextView) rootView.findViewById(R.id.thread_item_header);
                viewHolder.mThreadItemImage =
                        (ImageView) rootView.findViewById(R.id.thread_item_image);
                viewHolder.mThreadItemBody =
                        (TextView) rootView.findViewById(R.id.thread_item_body);
                viewHolder.mThreadItemAnswersAndFiles =
                        (TextView) rootView.findViewById(R.id.thread_item_answers_and_files);
                viewHolder.mThreadItemShortInfo =
                        (TextView) rootView.findViewById(R.id.short_info_view);
                viewHolder.mExpandOptions =
                        (ImageView) rootView.findViewById(R.id.expand_options);
                rootView.setTag(viewHolder);

                viewHolder.mExpandOptions.setContentDescription(number);
                viewHolder.mExpandOptions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        activity.showPopup(viewHolder.mExpandOptions);
                    }
                });

                String shortInfo = "(" + size + "Кб, " + width + "x" + height + ")";

                if (duration != null) {
                    if (!duration.equals("")) {
                        shortInfo = shortInfo.substring(0, shortInfo.length() - 1);
                        shortInfo += ", " + duration + ")";
                    }
                }
                viewHolder.mThreadItemShortInfo.setText(shortInfo);

                //Log.i(LOG_TAG, "text setted " + viewHolder.mThreadItemShortInfo.getText());
                if (op.equals("0")) {
                    op = "";
                } else {
                    op = "<font color=\"#008000\"># OP</font>";
                }
                SpannableStringBuilder builderHeader = new SpannableStringBuilder();
                if (subjectOfThread.equals("")
                        || activity.intentBoard.equals("b") || subjectOfThread.equals(" ")) {
                    subjectOfThread = "";
                } else {
                    subjectOfThread = "<b><font color=\"#002249\">" + subjectOfThread + "</font></b>";
                }
                if (opName.equals("")) {
                    opName = activity.defaultOpName;
                }
                builderHeader.append(subjectOfThread + " ");
                builderHeader.append(opName + " ");
                builderHeader.append(op + " ");
                builderHeader.append(date + " ");
                builderHeader.append(number);

                viewHolder.mThreadItemHeader.setText(
                        Html.fromHtml(builderHeader.toString()), TextView.BufferType.SPANNABLE);

                SpannableStringBuilder builderBody = new SpannableStringBuilder();
                builderBody.append(comment);


                if (!(thumb.equals(""))) {
                    if (path.substring(path.length() - 4, path.length()).equals("webm")) {
                        ImageView webmImageview = (ImageView) rootView.findViewById(R.id.webm_imageview);
                        webmImageview.setContentDescription(number);
                        webmImageview.setOnClickListener(activity.onThumbnailClickListener);
                        final int finalI = i;
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                try {
                                    URL url = new URL("https://2ch.hk/" + thumb);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setDoInput(true);
                                    connection.connect();
                                    InputStream input = connection.getInputStream();
                                    activity.addBitmapToMemoryCache(String.valueOf(finalI), BitmapFactory.decodeStream(input));
                                } catch (IOException e) {
                                    // Log exception
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void voids) {
//                            Picasso.with(getApplicationContext()).load(getBitmapFromMemCache(String.valueOf(i))
//                                    .into(mThreadItemImage);

                                viewHolder.mThreadItemImage.setImageBitmap(activity.getBitmapFromMemCache(String.valueOf(finalI)));
                                viewHolder.mThreadItemImage.setContentDescription(number);
                                viewHolder.mThreadItemImage.setOnClickListener(activity.onThumbnailClickListener);
                            }
                        }.execute();

                    } else {
                        ImageView webmImageview = (ImageView) rootView.findViewById(R.id.webm_imageview);
                        webmImageview.setVisibility(View.GONE);
                        final int finalI1 = i;
                        new AsyncTask<Void, Void, Bitmap>() {
                            @Override
                            protected Bitmap doInBackground(Void... voids) {
                                try {
                                    URL url = new URL("https://2ch.hk/" + thumb);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setDoInput(true);
                                    connection.connect();
                                    InputStream input = connection.getInputStream();
                                    activity.addBitmapToMemoryCache(String.valueOf(finalI1), BitmapFactory.decodeStream(input));
                                } catch (IOException e) {
                                    // Log exception
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Bitmap bitmap) {
//                            Picasso.with(getApplicationContext()).load("https://2ch.hk/" + thumb)
//                                    .into(mThreadItemImage);
                                viewHolder.mThreadItemImage.setImageBitmap(activity.getBitmapFromMemCache(String.valueOf(finalI1)));
                                viewHolder.mThreadItemImage.setContentDescription(number);
                                viewHolder.mThreadItemImage.setOnClickListener(activity.onThumbnailClickListener);
                            }
                        }.execute();
                    }
                } else {
                    LinearLayout imageContainer =
                            (LinearLayout) rootView.findViewById(R.id.image_item_container);
                    imageContainer.setVisibility(View.GONE);
                }


//                viewHolder.mThreadItemBody.setMovementMethod(CustomLinkMovementMethod.getInstance(
//                        activity.getApplicationContext(), true, null, null, i, null
//                ));
                viewHolder.mThreadItemBody.setMovementMethod(new CommentLinkMovementMethod(activity, i));
                viewHolder.mThreadItemBody.setFocusable(false);
                //viewHolder.mThreadItemBody.setClickable(false);
                viewHolder.mThreadItemBody.setLongClickable(false);

                viewHolder.mThreadItemBody.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // position = mThreadsListView.getPositionForView(view);

                        Intent intent = new Intent(activity.getApplicationContext(), SingleThreadActivity.class);
                        intent.putExtra(Constants.NUMBER, rootView.getContentDescription());
                        intent.putExtra(Constants.BOARD, activity.intentBoard);
                        intent.putExtra(Constants.PAGE, String.valueOf(activity.chosenPage));
                        Constants.FROM_SINGLE_THREAD = false;
                        activity.startActivity(intent);
                    }
                });

                CommonMakabaMarkupHandle.getSpoilers(activity, i);
                //SpannableString ss = new SpannableString(builderBody);

                //ss = activity.adapter.setSpoilerSpans(i, ss);
                //Log.i(LOG_TAG, "i + Constants.THREADS_ITEMS_LOADED " + (i + Constants.THREADS_ITEMS_LOADED));
                //CommentTagHandler commentTagHandler = new CommentTagHandler(i + activity.threadItemsLoaded, false, viewHolder.mThreadItemBody);
                //viewHolder.mThreadItemBody.setText(Html.fromHtml(ss.toString(), null, commentTagHandler), TextView.BufferType.SPANNABLE);
                //ss.setSpan(new BackgroundColorSpan(Color.BLACK), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                viewHolder.mThreadItemBody.setText(Html.fromHtml(builderBody.toString()), TextView.BufferType.SPANNABLE);
                int remainderAnswers = Integer.parseInt(
                        answersCount.substring(answersCount.length() - 1, answersCount.length()));
                int remainderFiles = Integer.parseInt(
                        filesCount.substring(filesCount.length() - 1, filesCount.length()));

                String missedPosts = "";
                switch (remainderAnswers) {
                    case 1: {
                        if (answersCount.length() >= 2) {
                            if (!answersCount.substring(
                                    answersCount.length() - 2, answersCount.length()).equals("11")) {
                                missedPosts = "Пропущен " + answersCount + " пост";
                                break;
                            } else {
                                missedPosts = "Пропущено " + answersCount + " постов";
                                break;
                            }
                        }
                        break;
                    }
                    case 2: {
                        if (answersCount.length() >= 2) {
                            if (!answersCount.substring(
                                    answersCount.length() - 2, answersCount.length()).equals("12")) {
                                missedPosts = "Пропущено " + answersCount + " поста";
                                break;
                            } else {
                                missedPosts = "Пропущено " + answersCount + " постов";
                                break;
                            }
                        }
                        break;
                    }
                    case 3: {
                        if (answersCount.length() >= 2) {
                            if (!answersCount.substring(
                                    answersCount.length() - 2, answersCount.length()).equals("13")) {
                                missedPosts = "Пропущено " + answersCount + " поста";
                                break;
                            } else {
                                missedPosts = "Пропущено " + answersCount + " постов";
                                break;
                            }
                        }
                        break;
                    }
                    case 4: {
                        if (answersCount.length() >= 2) {
                            if (!answersCount.substring(
                                    answersCount.length() - 2, answersCount.length()).equals("14")) {
                                missedPosts = "Пропущено " + answersCount + " поста";
                                break;
                            } else {
                                missedPosts = "Пропущено " + answersCount + " постов";
                                break;
                            }
                        }
                        break;
                    }
                    default: {
                        missedPosts = "Пропущено " + answersCount + " постов";
                    }
                }
                String filesNumber = "";
                switch (remainderFiles) {
                    case 1: {
                        if (answersCount.length() >= 2) {
                            if (!answersCount.substring(
                                    answersCount.length() - 2, answersCount.length()).equals("11")) {
                                filesNumber = ", " + filesCount + " файл";
                                break;
                            } else {
                                filesNumber = ", " + filesCount + " файлов";
                                break;
                            }
                        }
                        break;
                    }
                    case 2: {
                        if (answersCount.length() >= 2) {
                            if (!answersCount.substring(
                                    answersCount.length() - 2, answersCount.length()).equals("12")) {
                                filesNumber = ", " + filesCount + " файла";
                                break;
                            } else {
                                filesNumber = ", " + filesCount + " файлов";
                                break;
                            }
                        }
                        break;
                    }
                    case 3: {
                        if (answersCount.length() >= 2) {
                            if (!answersCount.substring(
                                    answersCount.length() - 2, answersCount.length()).equals("13")) {
                                filesNumber = ", " + filesCount + " файла";
                                break;
                            } else {
                                filesNumber = ", " + filesCount + " файлов";
                                break;
                            }
                        }
                        break;
                    }
                    case 4: {
                        if (answersCount.length() >= 2) {
                            if (!answersCount.substring(
                                    answersCount.length() - 2, answersCount.length()).equals("14")) {
                                filesNumber = ", " + filesCount + " файла";
                                break;
                            } else {
                                filesNumber = ", " + filesCount + " файлов";
                                break;
                            }
                        }
                        break;
                    }
                    default: {
                        filesNumber = ", " + filesCount + " файлов";
                    }
                }
                if (missedPosts.equals("") && !filesNumber.equals("")) {
                    filesNumber = filesNumber.substring(2, filesNumber.length());
                }

                if ((missedPosts + filesNumber).equals("")) {
                    viewHolder.mThreadItemAnswersAndFiles.setVisibility(View.GONE);
                } else {
                    viewHolder.mThreadItemAnswersAndFiles.setText(missedPosts + filesNumber);
                }


                rootView.setContentDescription(number);
                activity.itemViews.add(rootView);
                //activity.itemViews.add(activity.views);

            }
            //itemViews.put(chosenPage, views);
            //itemViews.addAll(views);
            Log.i(LOG_TAG, "itemViews.size() " + activity.itemViews.size());
            //Log.i(LOG_TAG, "numbersgeneral " + numbersGeneral.size());

//            for (int i = 0; i < activity.itemViews.size(); i++) {
//                if (activity.numbersGeneral.size() >= i - 1) {
//                    break;
//                }
//                if (Constants.COLLAPSED_THREADS.containsKey(activity.numbersGeneral.get(i))) {
//                    Constants.COLLAPSED_THREADS_POSITIONS.put(activity.numbersGeneral.get(i), i);
//                    //Log.i(LOG_TAG, "Found collapsed view " + numbersGeneral.get(i));
//                    //View viewToReplace = Constants.COLLAPSED_THREADS.get(numbersGeneral.get(i));
//                    int threadPosition = i;
//                    //Log.i(LOG_TAG, "threadPosition " + threadPosition);
//                    View collapsedItemView = activity.getLayoutInflater().inflate(R.layout.collapsed_thread, null, false);
//                    //collapsedItemView.setContentDescription(number);
//                    View itemToSave = activity.itemViews.get(threadPosition);
//                    TextView threadNumberTextView = (TextView) collapsedItemView.findViewById(R.id.collapsed_thread_number);
//                    TextView threadDescriptionTextView = (TextView) collapsedItemView.findViewById(R.id.collapsed_thread_description);
//                    threadNumberTextView.setText("№" + activity.numbersGeneral.get(i));
//                    //String desc = String.valueOf(((TextView) itemToSave.findViewById(R.id.thread_item_body)).getText());
//                    String desc = activity.formattedTextsGeneral.get(threadPosition);
//                    threadDescriptionTextView.setText("(" + desc + ")");
//
//                    collapsedItemView.setContentDescription(activity.numbersGeneral.get(i));
//                    activity.itemViews.remove(threadPosition);
//                    activity.itemViews.add(threadPosition, collapsedItemView);
//                }
//            }

            //Log.i(LOG_TAG, "Constants.COLLAPSED_THREADS " + COLLAPSED_THREADS.size());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(LOG_TAG, "Views have been created");
            Log.i(LOG_TAG, "Paths " + activity.pathsToMediaFiles);
            if (activity.chosenPage == 0) {
                activity.mThreadsListView.setAdapter(activity.adapter);
                activity.pb = new ProgressBar(activity.thisActivity, null, android.R.attr.progressBarStyle);
                activity.pb.setContentDescription("footer");
                activity.pb.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View view) {
                        activity.chosenPage++;
                        Log.i(LOG_TAG, "chosenPage " + activity.chosenPage);
                        ThreadsTask tt = new ThreadsTask(activity);
                        tt.execute();
                    }

                    @Override
                    public void onViewDetachedFromWindow(View view) {
                    }
                });
                activity.pb.setOnClickListener(null);
                activity.mThreadsListView.addFooterView(activity.pb);
                View headerBarFake = activity.getLayoutInflater().inflate(R.layout.banner_header_view, null);
                ImageView bannerImageView = (ImageView) headerBarFake.findViewById(R.id.banner_image_view);
                Glide
                        .with(activity)
                        .load(Constants.DVACH_AUTHORITY + activity.banner[0])
                        .into(bannerImageView);
                bannerImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ThreadsActivity ta = new ThreadsActivity();
                        Intent intent = new Intent(activity, ta.getClass());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra(Constants.BOARD, activity.banner[1]);
                        intent.putExtra(Constants.PAGE, "0");
                        activity.startActivity(intent);
                    }
                });
                headerBarFake.setLayoutParams(new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160)));
                //bannerImageView.setImageResource(R.drawable.l1);
                activity.mThreadsListView.addHeaderView(headerBarFake);


                Log.i(LOG_TAG, "banner path " + Constants.DVACH_AUTHORITY + activity.banner[0]);
                activity.frameLayoutInner.setVisibility(View.VISIBLE);
                activity.loadingImageContainer.setVisibility(View.GONE);
                activity.setTitle(activity.boardName);
            } else {
                activity.mThreadsListView.removeFooterView(activity.pb);
                activity.mMemoryCache.evictAll();
                System.gc();
                if (activity.chosenPage >= Integer.parseInt(activity.pagesOnBoard) - 2) {
                    return;
                }
                Log.i(LOG_TAG, "threadsList.size() " + activity.threadsList.size());
                activity.adapter.notifyDataSetChanged();
                if (Integer.parseInt(activity.pagesOnBoard) != activity.chosenPage) {
                    activity.pb = new ProgressBar(activity.thisActivity, null, android.R.attr.progressBarStyle);
                    activity.pb.setContentDescription("footer");
                    activity.pb.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                        @Override
                        public void onViewAttachedToWindow(View view) {
                            activity.chosenPage++;
                            Log.i(LOG_TAG, "chosenPage " + activity.chosenPage);
                            ThreadsTask tt = new ThreadsTask(activity);
                            tt.execute();
                        }

                        @Override
                        public void onViewDetachedFromWindow(View view) {
                        }
                    });
                    activity.pb.setOnClickListener(null);
                    activity.mThreadsListView.addFooterView(activity.pb);
                }
            }
        }
    }
}
