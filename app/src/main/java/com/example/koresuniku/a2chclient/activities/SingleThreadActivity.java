package com.example.koresuniku.a2chclient.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.example.koresuniku.a2chclient.R;
import com.example.koresuniku.a2chclient.fragments.PostFragment;
import com.example.koresuniku.a2chclient.utilities.Constants;
import com.example.koresuniku.a2chclient.utilities.CustomLinkMovementMethod;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingleThreadActivity extends AppCompatActivity {
    private final static String LOG_TAG = SingleThreadActivity.class.getSimpleName();

    private static Context activityContext;
    private static SingleThreadActivity activity;
    public static int mListViewPosition;
    private boolean isErrorContent;
    private boolean isRefreshed;
    private boolean firstTimeLoaded;
    private boolean isWebm;

    public static String intentThreadNumber;
    public static String intentBoard;
    private static String intentPage;
    private static String fromThread;
    private String defaultOpName;
    private String subjectOfThread;
    private String opName;
    private String date;
    private String number;
    private String thumbs;
    private String comment;
    private String op;
    private String parent;
    private String size;
    private String width;
    private String height;
    private String duration;
    private String path;
    private String email;
    private String currentPath;
    public static int mediaPosition;
    private int beforeCount;
    private int afterCount;
    private int postionG;
    private int utilPosition;
    private int theOnlyPositionToLoad;
    private int webmViewsCounter;
    private boolean nextClicked;
    private int currentPlayerViewPosition;
    private boolean needToCloseMediaViewer;
    private boolean firstTimePlayerTurnedOn;
    private static boolean needToCloseContextView;
    private boolean imageAdapterIsFinalized;
    private static boolean postingFragmentAvailable;

    ViewPager viewPager;
    ImageAdapter adapterI;

    public static PostFragment pf;
    private static ArrayList<Map<String, String>> threadItems;
    private ArrayList<String> parentsGeneral;
    public static ArrayList<String> numbersGeneral;
    private Map<String, ArrayList<String>> answersGeneral;
    public static ArrayList<String> formattedTextGeneral;
    public static ArrayList<String> unformattedTextGeneral;
    public static ArrayList<View> itemViews;
    public static ArrayList<String> pathsGeneral;
    private static ArrayList<String> thumbsGeneral;
    private Map<Integer, View> playerViews;
    private Map<Integer, SimpleExoPlayer> openedPlayersToRelease;

    private LayoutInflater mLayoutInflater;
    public static ListView mThreadsListView;
    public static FrameLayout mThreadContextItem;
    private FrameLayout mainLayoutContainer;
    public static FrameLayout tintView;
    public static ArrayList<View> openedAnswers;
    public static ScrollView itemContextScrollView;
    private static boolean isScrolled = false;
    private static ArrayList<Map<String, String>> copyOfThreadItems;
    private static ThreadsAdapter adapter;
    private static Context mContextActivity;
    private ProgressBar progressBar;
    private FrameLayout mThreadsLayoutContainer;
    public static FrameLayout fragmentCotainer;
    public static RelativeLayout imageViewContainer;
    private FrameLayout left;
    private FrameLayout right;
    public static Menu mMenu;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "Inside onResume " + mListViewPosition);
        mThreadsListView.setSelection(mListViewPosition);
        mThreadsListView.smoothScrollToPosition(mListViewPosition);


    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle("");
        setContentView(R.layout.thread_single_layout);

        isErrorContent = false;
        firstTimeLoaded = true;
        needToCloseMediaViewer = false;
        firstTimeLoaded = true;
        needToCloseContextView = false;
        imageAdapterIsFinalized = true;
        postingFragmentAvailable = false;
        utilPosition = 0;

        pf = new PostFragment(getApplicationContext(), this);

        playerViews = new HashMap<>();
        openedPlayersToRelease = new HashMap<>();
        pathsGeneral = new ArrayList<>();
        thumbsGeneral = new ArrayList<>();
        answersGeneral = new HashMap<>();

        intentThreadNumber = getIntent().getStringExtra(Constants.NUMBER);
        intentBoard = getIntent().getStringExtra(Constants.BOARD);
        intentPage = getIntent().getStringExtra(Constants.PAGE);
        fromThread = getIntent().getStringExtra(Constants.FROM_THREAD);

        Log.i(LOG_TAG, "num " + intentThreadNumber);
        Log.i(LOG_TAG, "brd " + intentBoard);
        Log.i(LOG_TAG, "pg " + intentPage);
        activity = this;
        activityContext = getApplicationContext();
        if (intentThreadNumber.equals("435999")) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.JILL_STRING, String.valueOf(Constants.JILL_COUNTER++));
            editor.apply();
            editor.commit();
        }
        itemViews = new ArrayList<>();
        openedAnswers = new ArrayList<>();

        mListViewPosition = 0;
        mainLayoutContainer = (FrameLayout) findViewById(R.id.threads_layout_containe_inner);

        mContextActivity = getApplicationContext();


        fragmentCotainer = (FrameLayout) findViewById(R.id.fragment_container);
        imageViewContainer = (RelativeLayout) findViewById(R.id.image_view_container);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        left = (FrameLayout) findViewById(R.id.left);
        left.setOnClickListener(leftListener);
        right = (FrameLayout) findViewById(R.id.right);
        right.setOnClickListener(rightListener);
        mThreadsLayoutContainer = (FrameLayout) findViewById(R.id.threads_layout_container);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mThreadsListView = (ListView) findViewById(R.id.threads_listview);
        mThreadContextItem = (FrameLayout) findViewById(R.id.context_item);
        tintView = (FrameLayout) findViewById(R.id.tint_view);
        tintView.setOnClickListener(onBackTintClickListener);
        itemContextScrollView = (ScrollView) findViewById(R.id.scroll_item_context);

        mLayoutInflater = getLayoutInflater();

        ThreadTask threadTask = new ThreadTask();
        threadTask.execute();


        if (intentThreadNumber.equals("435999")) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.JILL_STRING, String.valueOf(Constants.JILL_COUNTER++));
            editor.apply();
            editor.commit();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    private View.OnClickListener leftListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (mediaPosition >= 0) {
                if (mediaPosition != 0) {
                    mediaPosition--;
                    addFullMedia(pathsGeneral.get(mediaPosition));

                }
            }
        }
    };

    private View.OnClickListener rightListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (mediaPosition < pathsGeneral.size() - 1) {
                if (mediaPosition != pathsGeneral.size() - 1) {
                    mediaPosition++;
                    addFullMedia(pathsGeneral.get(mediaPosition));
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.media_fragment_menu, menu);
        mMenu = menu;
        MenuItem saveItem = menu.findItem(R.id.action_save);
        saveItem.setVisible(false);
        MenuItem closeItem = menu.findItem(R.id.action_close);
        closeItem.setVisible(false);
        MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        refreshItem.setVisible(false);
        MenuItem actionAttach = menu.findItem(R.id.action_attach);
        actionAttach.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
            case R.id.action_close: {
                if (postingFragmentAvailable) {
                    closePostingFragment();
                    postingFragmentAvailable = false;
                } else {
                    closeFullMedia();
                }
                break;
            }
            case R.id.action_refresh: {
                beforeCount = numbersGeneral.size() - 1;
                isRefreshed = true;
                ThreadTask tt = new ThreadTask();
                tt.execute();
                break;
            }
            case R.id.action_refresh_single: {
                refreshThread();
                break;
            }
            case R.id.action_write: {
                postingFragmentAvailable = true;
                MenuItem actionClose = mMenu.findItem(R.id.action_close);
                MenuItem actionRefreshSingle = mMenu.findItem(R.id.action_refresh_single);
                MenuItem actionWrite = mMenu.findItem(R.id.action_write);
                MenuItem actionAttach = mMenu.findItem(R.id.action_attach);
                actionAttach.setVisible(true);
                actionRefreshSingle.setVisible(false);
                actionClose.setVisible(true);
                actionWrite.setVisible(false);
                FrameLayout fragmentPostContainer = (FrameLayout) findViewById(R.id.fragment_post_container);
                fragmentCotainer.setVisibility(View.VISIBLE);
                fragmentCotainer.setOnClickListener(null);
                fragmentPostContainer.setVisibility(View.VISIBLE);
                pf = new PostFragment(getApplicationContext(), this);
                getFragmentManager().beginTransaction()
                        .add(R.id.fragment_post_container, pf)
                        .commit();
            }
        }
        return true;
    }

    public void refreshThread() {
        beforeCount = numbersGeneral.size() - 1;
        isRefreshed = true;
        ThreadTask tt = new ThreadTask();
        tt.execute();
    }

    public void closePostingFragment() {
        getFragmentManager().beginTransaction()
                .remove(pf)
                .commit();
        MenuItem saveItem = this.mMenu.findItem(R.id.action_save);
        saveItem.setVisible(false);
        MenuItem closeItem = this.mMenu.findItem(R.id.action_close);
        closeItem.setVisible(false);
        MenuItem refreshItem = this.mMenu.findItem(R.id.action_refresh);
        refreshItem.setVisible(false);
        MenuItem actionRefreshSingle = this.mMenu.findItem(R.id.action_refresh_single);
        actionRefreshSingle.setVisible(true);
        MenuItem actionWrite = this.mMenu.findItem(R.id.action_write);
        actionWrite.setVisible(true);
        MenuItem actionAttach = this.mMenu.findItem(R.id.action_attach);
        actionAttach.setVisible(false);
        fragmentCotainer.setVisibility(View.GONE);
        //fragmentCotainer.setOnClickListener(null);
    }

    public void onBackPressed() {
        if (postingFragmentAvailable) {
            closePostingFragment();
            postingFragmentAvailable = false;
            return;
        }

        if (needToCloseContextView) {
            onBackTintListenerBackPressed();
            return;
        }
        if (isErrorContent) {
            isErrorContent = false;
        }
        if (needToCloseMediaViewer) {
            closeFullMedia();
            needToCloseMediaViewer = false;
            return;
        }
        super.onBackPressed();
    }



    private class ViewHolder {
        TextView mThreadItemHeader;
        ImageView mThreadItemImage;
        TextView mThreadItemBody;

        ImageView imageContainer1;
        ImageView imageContainer2;
        ImageView imageContainer3;
        ImageView imageContainer4;

        LinearLayout mThreadAnswersContainer;


    }

    private class ThreadsAdapter extends BaseAdapter {
        private Context mContext;
        private ViewHolder viewHolder;
        private int counterId = -1;

        public ThreadsAdapter(Context context) {
            mContext = context;
            //threadsList = list;
        }

        @Override
        public int getCount() {
            return threadItems.size();
        }

        @Override
        public Map<String, String> getItem(int i) {
            return threadItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            Map<String, String> item = threadItems.get(i);
            return threadItems.indexOf(item);
        }

        private View.OnClickListener mediaClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = String.valueOf(view.getContentDescription());
                path = "https://2ch.hk/" + path;
                Log.i(LOG_TAG, "on click path " + path);

                addFullMedia(path);
                MenuItem save = mMenu.findItem(R.id.action_save);
                MenuItem close = mMenu.findItem(R.id.action_close);
                MenuItem refresh = mMenu.findItem(R.id.action_refresh);
                MenuItem refreShSingle = mMenu.findItem(R.id.action_refresh_single);
                MenuItem actionWrite = mMenu.findItem(R.id.action_write);

                actionWrite.setVisible(false);
                refreShSingle.setVisible(false);
                refresh.setVisible(true);
                save.setVisible(true);
                close.setVisible(true);
            }
        };

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int layoutMode = -1;
            final ViewHolder viewHolder;
            final Map<String, String> item = getItem(i);

            String date = item.get(Constants.DATE);
            String number = item.get(Constants.NUMBER_SINGLE_THREAD);
            String comment = item.get(Constants.COMMENT);
            String op = item.get(Constants.OP);
            String subjectOfThread = item.get(Constants.SUBJECT_OF_THREAD);
            String opName = item.get(Constants.OP_NAME);
            String size = item.get(Constants.SIZE);
            String width = item.get(Constants.WIDTH);
            String height = item.get(Constants.HEIGHT);
            String duration = item.get(Constants.DURATION);
            String path = item.get(Constants.PATH);
            String email = item.get(Constants.EMAIL);

            String[] splittedPath = null;
            if (path != null) {
                splittedPath = path.split(" ");
            }

                viewHolder = new ViewHolder();
                String thumb = item.get(Constants.THUMB);

                if (thumb != null) {
                    String[] splittedThumbs = thumb.split(" ");
                    if (splittedThumbs.length == 1) {
                        view = mLayoutInflater
                                .inflate(R.layout.thread_item_single_image_full, null, false);

                        layoutMode = 0;
                        viewHolder.mThreadItemImage = (ImageView) view.findViewById(R.id.thread_item_image);

                        Picasso.with(mContext).load("https://2ch.hk/" + splittedThumbs[0])
                                .into(viewHolder.mThreadItemImage);
                        viewHolder.mThreadItemImage.setContentDescription(path.substring(0, path.length() - 1));
                        viewHolder.mThreadItemImage.setOnClickListener(mediaClickListener);
                        splittedThumbs = null;
                    } else {
                        view = mLayoutInflater
                                .inflate(R.layout.thread_item_multi_image, null, false);
                        layoutMode = 1;
                        view.setTag(viewHolder);
                        viewHolder.imageContainer1 = (ImageView) view.findViewById(R.id.image_container_1);
                        viewHolder.imageContainer2 = (ImageView) view.findViewById(R.id.image_container_2);
                        viewHolder.imageContainer3 = (ImageView) view.findViewById(R.id.image_container_3);
                        viewHolder.imageContainer4 = (ImageView) view.findViewById(R.id.image_container_4);
                        view.setTag(viewHolder);
                        for (int index = 0; index < splittedThumbs.length; index++) {
                            switch (index) {
                                case 0:
                                    Picasso.with(mContext).load("https://2ch.hk/" + splittedThumbs[index])
                                            .into(viewHolder.imageContainer1);
                                    viewHolder.imageContainer1.setOnClickListener(mediaClickListener);
                                    viewHolder.imageContainer1.setContentDescription(splittedPath[0]);
                                    break;
                                case 1:
                                    Picasso.with(mContext).load("https://2ch.hk/" + splittedThumbs[index])
                                            .into(viewHolder.imageContainer2);
                                    viewHolder.imageContainer2.setOnClickListener(mediaClickListener);
                                    viewHolder.imageContainer2.setContentDescription(splittedPath[1]);
                                    break;
                                case 2:
                                    Picasso.with(mContext).load("https://2ch.hk/" + splittedThumbs[index])
                                            .into(viewHolder.imageContainer3);
                                    viewHolder.imageContainer3.setOnClickListener(mediaClickListener);
                                    viewHolder.imageContainer3.setContentDescription(splittedPath[2]);
                                    break;
                                case 3:
                                    Picasso.with(mContext).load("https://2ch.hk/" + splittedThumbs[index])
                                            .into(viewHolder.imageContainer4);
                                    viewHolder.imageContainer4.setOnClickListener(mediaClickListener);
                                    viewHolder.imageContainer4.setContentDescription(splittedPath[3]);
                                    break;
                            }
                        }
                        splittedThumbs = null;
                    }
                } else {
                    view = mLayoutInflater
                            .inflate(R.layout.thread_item_multi_image, null, false);
                    layoutMode = 1;
                }
                viewHolder.mThreadItemHeader =
                        (TextView) view.findViewById(R.id.thread_item_header);
                viewHolder.mThreadAnswersContainer =
                        (LinearLayout) view.findViewById(R.id.answers_container);
                viewHolder.mThreadItemBody =
                        (TextView) view.findViewById(R.id.thread_item_body);



            if (size != null) {
                String[] splittedSize = size.split(" ");
                String[] splittedWidth = width.split(" ");
                String[] splittedHeight = height.split(" ");
                String[] splittedDuration = null;
                if (duration != null) {
                    splittedDuration = duration.split(" ");
                } else {
                    splittedDuration = null;
                }

                if (layoutMode == 0) {
                    TextView shortInfoTextView = (TextView) view.findViewById(R.id.short_info_textview);
                    String shortInfo = "(" + splittedSize[0] + "Кб, " + splittedWidth[0] + "x" + splittedHeight[0] + ")";
                    shortInfoTextView.setText(shortInfo);

                    if (duration != null){
                        shortInfo = "(" + splittedSize[0] + "Кб, " + splittedWidth[0] + "x" + splittedHeight[0] + ", " + duration.substring(0, duration.length() - 1) + ")";
                        shortInfoTextView.setText(shortInfo);
                        ImageView webmImageview = (ImageView) view.findViewById(R.id.webm_imageview);
                        webmImageview.setOnClickListener(mediaClickListener);
                        webmImageview.setContentDescription(path.substring(0, path.length() - 1));
                    } else {
                        ImageView webmImageview = (ImageView) view.findViewById(R.id.webm_imageview);
                        webmImageview.setVisibility(View.GONE);
                    }
                } else {

                    for (int iInfo = 0; iInfo < splittedSize.length; iInfo++) {
                        if (splittedDuration == null) break;
                        switch (iInfo) {
                            case 0: {
                                if (splittedDuration.length == 1) break;
                                if (splittedDuration.length > 0) {
                                    TextView shortInfoTextView = new TextView(mContext);
                                    shortInfoTextView.setTextSize(11);
                                    shortInfoTextView.setTextColor(Color.parseColor("#8a000000"));
                                    String shortInfo = "(" + splittedSize[iInfo] + "Кб, " + splittedWidth[iInfo] + "x" + splittedHeight[iInfo] + ", "
                                              + splittedDuration[iInfo] + ")";
                                    shortInfoTextView.setText(shortInfo);

                                    shortInfoTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    shortInfoTextView.setMaxWidth(200);
                                    shortInfoTextView.setGravity(Gravity.CENTER);

                                    FrameLayout shInfoCon1 = (FrameLayout) view.findViewById(R.id.sh_info_con_1);
                                    shInfoCon1.addView(shortInfoTextView);

                                    ImageView webmImageview = (ImageView) view.findViewById(R.id.webm_imageview_1);
                                    webmImageview.setVisibility(View.VISIBLE);
                                    webmImageview.setOnClickListener(mediaClickListener);
                                    webmImageview.setContentDescription(splittedPath[0]);

                                } else {
                                    TextView shortInfoTextView = new TextView(mContext);
                                    shortInfoTextView.setTextSize(11);
                                    shortInfoTextView.setTextColor(Color.parseColor("#8a000000"));
                                    String shortInfo = "(" + splittedSize[iInfo] + "Кб, " + splittedWidth[iInfo] + "x" + splittedHeight[iInfo] + ")";
                                    shortInfoTextView.setText(shortInfo);


                                    shortInfoTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    shortInfoTextView.setMaxWidth(200);
                                    shortInfoTextView.setGravity(Gravity.CENTER);
                                    FrameLayout shInfoCon1 = (FrameLayout) view.findViewById(R.id.sh_info_con_1);
                                    shInfoCon1.addView(shortInfoTextView);

                                }
                                break;
                            }
                            case 1: {
                                if (splittedDuration.length > 1) {
                                    TextView shortInfoTextView = new TextView(mContext);
                                    shortInfoTextView.setTextSize(11);
                                    shortInfoTextView.setTextColor(Color.parseColor("#8a000000"));
                                    String shortInfo = "(" + splittedSize[iInfo] + "Кб, " + splittedWidth[iInfo] + "x" + splittedHeight[iInfo] + ", "
                                            + splittedDuration[iInfo] + ")";
                                    shortInfoTextView.setText(shortInfo);
                                    shortInfoTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    shortInfoTextView.setMaxWidth(200);
                                    shortInfoTextView.setGravity(Gravity.CENTER);
                                    FrameLayout shInfoCon1 = (FrameLayout) view.findViewById(R.id.sh_info_con_2);
                                    shInfoCon1.addView(shortInfoTextView);
                                    ImageView webmImageview = (ImageView) view.findViewById(R.id.webm_imageview_2);
                                    webmImageview.setVisibility(View.VISIBLE);
                                    webmImageview.setOnClickListener(mediaClickListener);
                                    webmImageview.setContentDescription(splittedPath[1]);

                                } else {
                                    TextView shortInfoTextView = new TextView(mContext);
                                    shortInfoTextView.setTextSize(11);
                                    shortInfoTextView.setTextColor(Color.parseColor("#8a000000"));
                                    String shortInfo = "(" + splittedSize[iInfo] + "Кб, " + splittedWidth[iInfo] + "x" + splittedHeight[iInfo] + ")";
                                    shortInfoTextView.setText(shortInfo);
                                    shortInfoTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    shortInfoTextView.setMaxWidth(200);
                                    shortInfoTextView.setGravity(Gravity.CENTER);
                                    FrameLayout shInfoCon1 = (FrameLayout) view.findViewById(R.id.sh_info_con_2);
                                    shInfoCon1.addView(shortInfoTextView);

                                }
                                break;
                            }
                            case 2: {
                                if (splittedDuration.length > 2) {
                                    TextView shortInfoTextView = new TextView(mContext);
                                    shortInfoTextView.setTextSize(11);
                                    shortInfoTextView.setTextColor(Color.parseColor("#8a000000"));
                                    String shortInfo = "(" + splittedSize[iInfo] + "Кб, " + splittedWidth[iInfo] + "x" + splittedHeight[iInfo] + ", "
                                            + splittedDuration[iInfo] + ")";
                                    shortInfoTextView.setText(shortInfo);
                                    shortInfoTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    shortInfoTextView.setMaxWidth(200);
                                    shortInfoTextView.setGravity(Gravity.CENTER);
                                    FrameLayout shInfoCon1 = (FrameLayout) view.findViewById(R.id.sh_info_con_3);
                                    shInfoCon1.addView(shortInfoTextView);
                                    ImageView webmImageview = (ImageView) view.findViewById(R.id.webm_imageview_3);
                                    webmImageview.setVisibility(View.VISIBLE);
                                    webmImageview.setOnClickListener(mediaClickListener);
                                    webmImageview.setContentDescription(splittedPath[2]);
                                } else {
                                    TextView shortInfoTextView = new TextView(mContext);
                                    shortInfoTextView.setTextSize(11);
                                    shortInfoTextView.setTextColor(Color.parseColor("#8a000000"));
                                    String shortInfo = "(" + splittedSize[iInfo] + "Кб, " + splittedWidth[iInfo] + "x" + splittedHeight[iInfo] + ")";
                                    shortInfoTextView.setText(shortInfo);
                                    shortInfoTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    shortInfoTextView.setMaxWidth(200);
                                    shortInfoTextView.setGravity(Gravity.CENTER);
                                    FrameLayout shInfoCon1 = (FrameLayout) view.findViewById(R.id.sh_info_con_3);
                                    shInfoCon1.addView(shortInfoTextView);

                                }
                                break;
                            }
                            case 3: {
                                if (splittedDuration.length > 4) {
                                    TextView shortInfoTextView = new TextView(mContext);
                                    shortInfoTextView.setTextSize(11);
                                    shortInfoTextView.setTextColor(Color.parseColor("#8a000000"));
                                    String shortInfo = "(" + splittedSize[iInfo] + "Кб, " + splittedWidth[iInfo] + "x" + splittedHeight[iInfo] + ", "
                                            + splittedDuration[iInfo] + ")";
                                    shortInfoTextView.setText(shortInfo);
                                    shortInfoTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    shortInfoTextView.setMaxWidth(200);
                                    shortInfoTextView.setGravity(Gravity.CENTER);
                                    FrameLayout shInfoCon1 = (FrameLayout) view.findViewById(R.id.sh_info_con_4);
                                    shInfoCon1.addView(shortInfoTextView);
                                    ImageView webmImageview = (ImageView) view.findViewById(R.id.webm_imageview_4);
                                    webmImageview.setVisibility(View.VISIBLE);
                                    webmImageview.setOnClickListener(mediaClickListener);
                                    webmImageview.setContentDescription(splittedPath[3]);
                                } else {
                                    TextView shortInfoTextView = new TextView(mContext);
                                    shortInfoTextView.setTextSize(11);
                                    shortInfoTextView.setTextColor(Color.parseColor("#8a000000"));
                                    String shortInfo = "(" + splittedSize[iInfo] + "Кб, " + splittedWidth[iInfo] + "x" + splittedHeight[iInfo] + ")";
                                    shortInfoTextView.setText(shortInfo);
                                    shortInfoTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    shortInfoTextView.setMaxWidth(200);
                                    shortInfoTextView.setGravity(Gravity.CENTER);
                                    FrameLayout shInfoCon1 = (FrameLayout) view.findViewById(R.id.sh_info_con_4);
                                    shInfoCon1.addView(shortInfoTextView);

                                }
                                break;
                            }
                        }
                    }

                    LinearLayout generalImCons = (LinearLayout) view.findViewById(R.id.general_im_cons);
                    generalImCons.setPadding(0, 0, 0, 16);
                }
            }


            if (op.equals("0")) {
                op = "";
            } else {
                op = "<font color=\"#008000\"># OP</font>";
            }
            SpannableStringBuilder builderHeader = new SpannableStringBuilder();
            if (subjectOfThread.equals("")
                    || intentBoard.equals("b") || subjectOfThread.equals(" ")) {
                subjectOfThread = "";
            } else {
                subjectOfThread = "<b><font color=\"#002249\">" + subjectOfThread + "</font></b>";
            }
            //Log.i(LOG_TAG, "opname " +opName);
            if (opName.equals("")) {
                //Log.i(LOG_TAG, "inside \"\"");
                opName = defaultOpName;

            }
            //Log.i(LOG_TAG, "email " + email);
            if (email.equals("mailto:sage")) {
                //Log.i(LOG_TAG, "is sage");
                opName = "<u><font color=\"#ff7000\">" + opName + "</font></u>";
            }
            if (i >= 1) {
                //Log.i(LOG_TAG, "Inside number post");
                builderHeader.append("<font color=\"#008000\">#" + i + "</font>" + " ");
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

            viewHolder.mThreadItemBody.getLinksClickable();
            viewHolder.mThreadItemBody.setMovementMethod(
                    CustomLinkMovementMethod.getInstance(mContext, true, activity, number));

            if (builderBody.toString().length() == 0) {
                viewHolder.mThreadItemBody.setVisibility(View.GONE);
                if (layoutMode == 1) {
                    Space spaceAfterComment = (Space) view.findViewById(R.id.space_after_comment);
                    spaceAfterComment.setVisibility(View.GONE);
                }
            } else {
                viewHolder.mThreadItemBody.setText(
                        Html.fromHtml(builderBody.toString()), TextView.BufferType.SPANNABLE);
            }


            ArrayList<String> answersArrayList = answersGeneral.get(number);
            String pre = "<i><font color=\"#696969\">"
                    + "Ответы:" + "\t" + "</font></i>";
            SpannableStringBuilder builderP = new SpannableStringBuilder();
            builderP.append(pre);

            TextView preTextView = new TextView(mContext);
            preTextView.setEnabled(false);
            preTextView.setText(Html.fromHtml(builderP.toString()), TextView.BufferType.SPANNABLE);

            viewHolder.mThreadAnswersContainer.addView(preTextView);

            SpannableStringBuilder builder = new SpannableStringBuilder();

            for (int index = 0; index < answersArrayList.size(); index++) {

                builder.append("<a href=\"/#" + answersArrayList.get(index) + "\">>>");
                builder.append(answersArrayList.get(index));
                builder.append("\t");
                builder.append("</a>");

            }

            TextView textView = new TextView(mContext);

            textView.setMovementMethod(
                    CustomLinkMovementMethod.getInstance(mContext, false, activity, number));
            textView.getLinksClickable();
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(Html.fromHtml(builder.toString()), TextView.BufferType.SPANNABLE);

            viewHolder.mThreadAnswersContainer.addView(textView);
            viewHolder.mThreadAnswersContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if (textView.getText().length() == 0) {
                viewHolder.mThreadAnswersContainer.setVisibility(View.GONE);
                if (layoutMode == 0) {
                    Space space = (Space) view.findViewById(R.id.space_if_delete);
                    space.setVisibility(View.GONE);
                }
            }

            itemViews.add(view);
            return view;
        }
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (!isScrolled) {
                threadItems = copyOfThreadItems;
                adapter.notifyDataSetChanged();
            }
            isScrolled = true;
        }
    };

    public void addAnswerView(String number) {
        boolean answerExists = false;
        needToCloseContextView = true;
        for (int i = 0; i < numbersGeneral.size(); i++) {

            if (numbersGeneral.get(i).equals(number)) {
                answerExists = true;
                Log.i(LOG_TAG, "Is copy equals original " + (copyOfThreadItems == threadItems));
                if (copyOfThreadItems != threadItems) {
                    threadItems = copyOfThreadItems;
                    adapter.notifyDataSetChanged();
                }

                View view;

                if (!(itemViews.size() - 1 >= i)) {
                    View newView = mThreadsListView.getAdapter().getView(i, null, null);
                    view = newView;
                } else {
                    view = mThreadsListView.getAdapter().getView(i, null, null);
                }

                mThreadContextItem.removeAllViews();

                tintView.setVisibility(View.VISIBLE);
                mThreadContextItem.addView(view);
                openedAnswers.add(view);
                mThreadsListView.setEnabled(false);
                break;
            }
        }

        if(!answerExists) {
            Intent intent = new Intent(getApplicationContext(), SingleThreadActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.BOARD, intentBoard);
            intent.putExtra(Constants.PAGE, intentPage);
            intent.putExtra(Constants.NUMBER, number);
            intent.putExtra(Constants.FROM_THREAD, intentThreadNumber);
            mContextActivity.startActivity(intent);
        }
    }

    public static void onBackTintListenerBackPressed() {

        if (openedAnswers.size() == 1) {
            mThreadsListView.setEnabled(true);
            mThreadContextItem.removeAllViews();
            tintView.setVisibility(View.GONE);
            openedAnswers = new ArrayList<>();
            needToCloseContextView = false;
        } else {
            mThreadContextItem.removeAllViews();
            View viewBack = openedAnswers.get(openedAnswers.size() - 2);
            //if (viewBack != null) {
                openedAnswers.remove(openedAnswers.size() - 1);
                mThreadContextItem.addView(viewBack);
            //}
        }
    }

    private View.OnClickListener onBackTintClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            if (openedAnswers.size() == 1) {
//                mThreadsListView.setEnabled(true);
//                mThreadContextItem.removeAllViews();
//                tintView.setVisibility(View.GONE);
//                openedAnswers = new ArrayList<>();
//            } else {
//                mThreadContextItem.removeAllViews();
//                View viewBack = openedAnswers.get(openedAnswers.size() - 2);
//                openedAnswers.remove(openedAnswers.size() - 1);
//                mThreadContextItem.addView(viewBack);
//            }
            onBackTintListenerBackPressed();
        }
    };

    private void closeFullMedia() {
        fragmentCotainer.setVisibility(View.GONE);
        try {
            adapterI.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        viewPager = null;

        deleteCache(getApplicationContext());
        MenuItem close = mMenu.findItem(R.id.action_close);
        close.setVisible(false);
        MenuItem save = mMenu.findItem(R.id.action_save);
        save.setVisible(false);
        MenuItem refreshSingle = mMenu.findItem(R.id.action_refresh_single);
        refreshSingle.setVisible(true);
        MenuItem refresh = mMenu.findItem(R.id.action_refresh);
        refresh.setVisible(false);
        MenuItem actionWrite = mMenu.findItem(R.id.action_write);
        actionWrite.setVisible(true);
    }

    private void addFullMedia(String path) {
        if (path.equals("refresh")) {
            Log.i(LOG_TAG, "Overall paths " + pathsGeneral);
            path = currentPath;
            fragmentCotainer.setVisibility(View.VISIBLE);
            fragmentCotainer.setOnClickListener(null);
            for (int i = 0; i < pathsGeneral.size(); i++) {
                if (pathsGeneral.get(i).equals(path)) {
                    postionG = i;
                    //currentPath = path;
                    Log.i(LOG_TAG, "Got path position " + postionG);
                    break;
                }
            }
            //viewPager = null;
            //adapterI = null;
            adapterI.notifyDataSetChanged();
        } else {
            fragmentCotainer.setVisibility(View.VISIBLE);
            fragmentCotainer.setOnClickListener(null);
            for (int i = 0; i < pathsGeneral.size(); i++) {
                if (pathsGeneral.get(i).equals(path)) {
                    postionG = i;
                    currentPath = path;
                    Log.i(LOG_TAG, "Got path position " + postionG);
                    break;
                }
            }

            viewPager = (ViewPager) findViewById(R.id.view_pager);
            adapterI = new ImageAdapter(this);
            viewPager.setOffscreenPageLimit(1);
            theOnlyPositionToLoad = postionG;
            currentPlayerViewPosition = postionG;
            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    Log.i(LOG_TAG, "Page selected " + position);
                    theOnlyPositionToLoad = position;
                    // if (isWebm) {
                    if (playerViews.size() != 0) {
                        if (position > currentPlayerViewPosition) {
                            View v = (View) playerViews.get(position - 1);
                            if (v != null) {
                                Log.i(LOG_TAG, "Inside disabling");
                                SimpleExoPlayerView player = (SimpleExoPlayerView) v.findViewById(R.id.simpleExoPlayerView);
                                player.getPlayer().setPlayWhenReady(false);
                            }
                            View vCurrent = (View) playerViews.get(position);
                            if (vCurrent != null) {
                                Log.i(LOG_TAG, "Inside enabling");
                                SimpleExoPlayerView player = (SimpleExoPlayerView) vCurrent.findViewById(R.id.simpleExoPlayerView);
                                player.getPlayer().setPlayWhenReady(true);
                            }
                            currentPlayerViewPosition = position;
                        } else {
                            Log.i(LOG_TAG, "Selected back ");

                            View v = (View) playerViews.get(position + 1);
                            if (v != null) {
                                Log.i(LOG_TAG, "Inside disabling");
                                SimpleExoPlayerView player = (SimpleExoPlayerView) v.findViewById(R.id.simpleExoPlayerView);
                                player.getPlayer().setPlayWhenReady(false);
                            }
                            View vCurrent = (View) playerViews.get(position);
                            if (vCurrent != null) {
                                Log.i(LOG_TAG, "Inside enabling");
                                SimpleExoPlayerView player = (SimpleExoPlayerView) vCurrent.findViewById(R.id.simpleExoPlayerView);
                                player.getPlayer().setPlayWhenReady(true);
                            }
                            currentPlayerViewPosition = position;
                        }
                    }
                    currentPlayerViewPosition = position;
                    //   }

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            viewPager.setAdapter(adapterI);
            viewPager.setCurrentItem(postionG);
        }
    }

    private class ThreadTask extends AsyncTask<Void, Void, Void> {
        private final String LOG_TAG = ThreadTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL("https://2ch.hk/makaba/mobile.fcgi?task=get_thread&board="
                        + intentBoard + "&thread=" + intentThreadNumber + "&post=0");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int responseCode = connection.getResponseCode();
                Log.v(LOG_TAG, "Response Code " + connection.getResponseCode());
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    String rawJSON = builder.toString();
                    threadItems = formatJSON(rawJSON);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

            @Override
        protected void onPostExecute(Void aVoid) {
            makeAnswersGeneral();
            copyOfThreadItems = threadItems;
            //Log.i(LOG_TAG, "Before for");
            threadItems = new ArrayList<>();
                if (copyOfThreadItems == null) {
                    setContentView(R.layout.error_404);
                    ImageView errorImage = (ImageView) findViewById(R.id.error_image);
                    Random random = new Random(new Date().getTime());
                    int randomInt = random.nextInt(Constants.ERROR_IMAGES.length);
                    errorImage.setImageResource(Constants.ERROR_IMAGES[randomInt]);
                    isErrorContent = false;
                } else if (firstTimeLoaded) {
                    if (copyOfThreadItems.size() <= 5) {
                        threadItems = copyOfThreadItems;
                        isScrolled = true;
                    } else {
                        for (int i = 0; i < 5; i++) {
                            threadItems.add(copyOfThreadItems.get(i));
                        }
                    }

                    isScrolled = false;
                    mThreadsListView.setClickable(false);
                    mThreadsListView.setFastScrollEnabled(true);

                    adapter = new ThreadsAdapter(getApplicationContext());
                    mThreadsListView.setAdapter(adapter);
                    if (isRefreshed) {
                        afterCount = copyOfThreadItems.size() - 1;
                        Log.i(LOG_TAG, "afterCont exists " + afterCount);
                        Log.i(LOG_TAG, "beforeCont exists " + beforeCount);
                        int diff = afterCount - beforeCount;
                        //Toast toast = Toast.makeText(getApplicationContext(), String.valueOf(diff), Toast.LENGTH_SHORT);
                        //toast.show();
                        isRefreshed = false;
                    }
                    progressBar.setVisibility(View.GONE);
                    mThreadsListView.setOnItemClickListener(onItemClickListener);
                    mThreadsListView.setOnScrollListener(onScrollListener);

                    SpannableString ss = new SpannableString(threadItems.get(0).get(Constants.SUBJECT_OF_THREAD));
                    setTitle(Html.fromHtml(ss.toString()));
                    firstTimeLoaded = false;
                    //pathsGeneral = null;
                } else {
                    threadItems = copyOfThreadItems;
                    isScrolled = false;
                    mThreadsListView.setClickable(false);
                    mThreadsListView.setFastScrollEnabled(true);
                    adapter.notifyDataSetChanged();
                    if (adapterI != null) {
                        adapterI.notifyDataSetChanged();
                    }
//                    if (adapterI != null) {
//                        adapterI.notifyDataSetChanged();
//                    }
                    if (!imageAdapterIsFinalized) {
                        //addFullMedia("refresh");
                    }
//                    adapter = new ThreadsAdapter(getApplicationContext());
//                    mThreadsListView.setAdapter(adapter);
                    if (isRefreshed) {
                        afterCount = copyOfThreadItems.size() - 1;
                        Log.i(LOG_TAG, "afterCont exists " + afterCount);
                        Log.i(LOG_TAG, "beforeCont exists " + beforeCount);
                        int diff = afterCount - beforeCount;
                        String iToShow = "";
                        if (String.valueOf(diff).length() == 1) {
                            switch (diff) {
                                case 1: {
                                    iToShow = diff + " новый пост";
                                    break;
                                }
                                case 2: {
                                    iToShow = diff + " новых поста";
                                    break;
                                }
                                case 3: {
                                    iToShow = diff + " новых поста";
                                    break;
                                }
                                case 4: {
                                    iToShow = diff + " новых поста";
                                    break;
                                }
                                case 0: {
                                    iToShow = "Новых постов нет";
                                    break;
                                }
                                default: {
                                    iToShow = diff + " новых постов";
                                    break;
                                }
                            }
                        } else {
                            String util = String.valueOf(diff);
                            boolean isDouble = false;
                            for (int i = 0; i < 10; i++) {
                                if (util.substring(util.length() - 2, util.length() - 1).equals(util.substring(util.length() - 1, util.length()))) {
                                    isDouble = true;
                                    break;
                                }
                            }
                            if (isDouble) {
                                iToShow = diff + " новых постов";
                            } else {
                                switch (diff) {
                                    case 1: {
                                        iToShow = diff + " новый пост";
                                        break;
                                    }
                                    case 2: {
                                        iToShow = diff + " новых поста";
                                        break;
                                    }
                                    case 3: {
                                        iToShow = diff + " новых поста";
                                        break;
                                    }
                                    case 4: {
                                        iToShow = diff + " новых поста";
                                        break;
                                    }

                                    default: {
                                        iToShow = diff + " новых постов";
                                        break;
                                    }
                                }
                            }

                        }
                        Toast toast = Toast.makeText(getApplicationContext(), iToShow, Toast.LENGTH_SHORT);
                        toast.show();
                        isRefreshed = false;
                    }
                    progressBar.setVisibility(View.GONE);
                    mThreadsListView.setOnItemClickListener(onItemClickListener);
                    mThreadsListView.setOnScrollListener(onScrollListener);

                    SpannableString ss = new SpannableString(threadItems.get(0).get(Constants.SUBJECT_OF_THREAD));
                    setTitle(Html.fromHtml(ss.toString()));
                   // pathsGeneral = null;
                }


        }

        private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if (!isScrolled) {
                    threadItems = copyOfThreadItems;
                    adapter.notifyDataSetChanged();
                }
                isScrolled = true;
            }
        };

        private void makeAnswersGeneral() {
            answersGeneral = new HashMap<>();

            for (int v = 0; v < numbersGeneral.size(); v++) {
                String item = numbersGeneral.get(v);
                answersGeneral.put(item, new ArrayList<String>());
            }

            for (int i = 0; i < formattedTextGeneral.size(); i++) {
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(formattedTextGeneral.get(i));
                while (matcher.find()) {
                    String match = matcher.group();
                    if (answersGeneral.containsKey(match)) {
                        ArrayList<String> itemToAlter = answersGeneral.get(match);
                        itemToAlter.add(numbersGeneral.get(i));
                        answersGeneral.put(match, itemToAlter);
                    }
                }
            }
        }
        private ArrayList<Map<String,String>> formatJSON(String rawJSON) {
            ArrayList<Map<String, String>> result = new ArrayList<>();
            parentsGeneral = new ArrayList<>();
            numbersGeneral = new ArrayList<>();
            formattedTextGeneral = new ArrayList<>();
            unformattedTextGeneral = new ArrayList<>();
            pathsGeneral = new ArrayList<>();

            Map<String, String> item;
            try {
                JSONArray main = new JSONArray(rawJSON);
                for (int v = 0; v < main.length(); v++) {
                    JSONObject thread = main.getJSONObject(v);

                    number = thread.getString("num");
                    date = thread.getString("date");
                    op = thread.getString("op");
                    comment = thread.getString("comment");
                    subjectOfThread = thread.getString("subject");
                    opName = thread.getString("name");
                    parent = thread.getString("parent");
                    email = thread.getString("email");

                    JSONArray filesArray = thread.getJSONArray("files");
                    if (!(filesArray.length() == 0)) {
                        for (int i = 0; i < filesArray.length(); i++) {
                            JSONObject fileItem = filesArray.getJSONObject(i);
                            //Log.v("Receiver Thumbnail", fileItem.getString("thumbnail"));
                            if (thumbs == null) {
                                thumbsGeneral.add("https://2ch.hk/" + fileItem.getString("thumbnail"));
                                thumbs = fileItem.getString("thumbnail") + " ";
                            } else {
                                thumbsGeneral.add("https://2ch.hk/" + fileItem.getString("thumbnail"));
                                thumbs += fileItem.getString("thumbnail") + " ";
                            }
                            if (size == null) {
                                size = fileItem.getString("size") + " ";
                            } else {
                                size += fileItem.getString("size") + " ";
                            }
                            if (width == null) {
                                width = fileItem.getString("width") + " ";
                            } else {
                                width += fileItem.getString("width") + " ";
                            }
                            if (height == null) {
                                height = fileItem.getString("height") + " ";
                            } else {
                                height += fileItem.getString("height") + " ";
                            }

                            if (path == null) {
                                pathsGeneral.add("https://2ch.hk/" + fileItem.getString("path"));
                                path = fileItem.getString("path") + " ";
                                if (path.substring(path.length() - 5, path.length() - 1).equals("webm")) {
                                    Log.i(LOG_TAG, "webm found");
                                    duration = fileItem.getString("duration") + " ";
                                }
                            } else {
                                pathsGeneral.add("https://2ch.hk/" + fileItem.getString("path"));
                                path += fileItem.getString("path") + " ";
                                if (path.substring(path.length() - 5, path.length() - 1).equals("webm")) {
                                    Log.i(LOG_TAG, "webm found");
                                    duration += fileItem.getString("duration") + " ";
                                }
                            }


                        }
                    } else {
                        thumbs = null;
                    }

                    item = new HashMap<>();
                    item.put(Constants.NUMBER_SINGLE_THREAD, number);
                    item.put(Constants.DATE, date);
                    item.put(Constants.OP, op);
                    item.put(Constants.COMMENT, comment);
                    item.put(Constants.THUMB, thumbs);
                    item.put(Constants.SUBJECT_OF_THREAD, subjectOfThread);
                    item.put(Constants.OP_NAME, opName);
                    item.put(Constants.PARENT, parent);
                    item.put(Constants.SIZE, size);
                    item.put(Constants.WIDTH, width);
                    item.put(Constants.HEIGHT, height);
                    item.put(Constants.DURATION, duration);
                    item.put(Constants.PATH, path);
                    item.put(Constants.EMAIL, email);

                    parentsGeneral.add(parent);
                    numbersGeneral.add(number);
                    unformattedTextGeneral.add(comment);

                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                    spannableStringBuilder.append(comment);
                    String commentFormatted = String.valueOf(Html.fromHtml(spannableStringBuilder.toString()));

                    formattedTextGeneral.add(commentFormatted);
                    thumbs = null;
                    size = null;
                    width = null;
                    height = null;
                    path = null;
                    duration = null;

                    result.add(item);
                }
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class ImageAdapter extends PagerAdapter {
        private final String LOG_TAG = com.example.koresuniku.a2chclient.activities.ImageAdapter.class.getSimpleName();
        Context context;
        private ViewGroup items;

        ImageAdapter(Context context){
            this.context=context;

            webmViewsCounter = 0;
            items = new ViewPager(getApplicationContext());
            needToCloseMediaViewer = true;
            imageAdapterIsFinalized = false;
        }
        @Override
        public int getCount() {
            return pathsGeneral.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        @Override
        protected void finalize() throws Throwable {
            Log.i(LOG_TAG, "Inside finalize(), items " + items.getChildCount());
            imageAdapterIsFinalized = true;
            firstTimePlayerTurnedOn = false;
            for (int i = 0; i < items.getChildCount(); i++) {
                View v = items.getChildAt(i);
                SimpleExoPlayerView simpleExoPlayerView = (SimpleExoPlayerView) v.findViewById(R.id.simpleExoPlayerView);

                if (simpleExoPlayerView != null) {
                    simpleExoPlayerView.getPlayer().stop();
                    simpleExoPlayerView.getPlayer().release();
                }
            }
            items = new ViewPager(getApplicationContext());
            super.finalize();

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
                Log.i(LOG_TAG, "Items " + items.getChildCount());
                deleteCache(getApplicationContext());
                webmViewsCounter++;
                items = container;
                Log.i(LOG_TAG, "Inside instantiate " + position);
                LayoutInflater layoutinflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = layoutinflater.inflate(R.layout.context_item_layout, null);

                String path = pathsGeneral.get(position);
                for (int i = 0; i < pathsGeneral.size(); i++) {
                    if (path.equals(String.valueOf(pathsGeneral.get(i)))) {
                        Log.i(LOG_TAG, "Got position!");
                        mediaPosition = i;
                    }
                }

                if (path.substring(path.length() - 4, path.length()).equals("webm")) {
                    Log.i(LOG_TAG, "Webm found");
                    Log.i(LOG_TAG, "Current position " + position);
                    Log.i(LOG_TAG, "Loading ");
                    Handler mainHandler = new Handler();
                    BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                    TrackSelection.Factory videoTrackSelectionFactory =
                            new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
                    TrackSelector trackSelector =
                            new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
                    LoadControl loadControl = new DefaultLoadControl();
                    SimpleExoPlayer player =
                            ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
                    SimpleExoPlayerView simpleExoPlayerView = (SimpleExoPlayerView) v.findViewById(R.id.simpleExoPlayerView);
                    simpleExoPlayerView.setVisibility(View.VISIBLE);
                    simpleExoPlayerView.setPlayer(player);
                    TouchImageView newImage = (TouchImageView) v.findViewById(R.id.imageViews);
                    newImage.setVisibility(View.GONE);
                    DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                            Util.getUserAgent(context, "yourApplicationName"), (TransferListener<? super DataSource>) bandwidthMeter);
                    ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                    MediaSource videoSource = new ExtractorMediaSource(Uri.parse(path),
                            dataSourceFactory, extractorsFactory, null, null);
                    Log.i(LOG_TAG, "Before prepare");
                    player.prepare(videoSource);
                    player.seekTo(0);
                    if (!firstTimePlayerTurnedOn) {
                        player.setPlayWhenReady(true);
                        firstTimePlayerTurnedOn = true;
                    } else {
                        player.setPlayWhenReady(false);
                    }
                    isWebm = true;
                    ((ViewPager) items).addView(v, 0);

                    playerViews.put(position, v);
                } else {
                    final TouchImageView newImage = (TouchImageView) v.findViewById(R.id.imageViews);
                    Log.i(LOG_TAG, "Path " + path.substring(path.length() - 3, path.length()));
                    if (path.substring(path.length() - 3, path.length()).equals("gif")) {
                        Log.i(LOG_TAG, "Path " + path.substring(path.length() - 3, path.length()));
                        Glide.with(context).load(path).asGif().placeholder(R.drawable.load_2).diskCacheStrategy(DiskCacheStrategy.SOURCE).error(R.drawable.error11).into(newImage);
                    } else {
                        Glide.with(context).load(path).placeholder(R.drawable.load_2).dontAnimate().into(newImage);
                    }
                    isWebm = false;
                    ((ViewPager) items).addView(v, 0);
                }

                return v;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
           if (isWebm) {
                Log.i(LOG_TAG, "Inside destroyitem() webm " + position);

               SimpleExoPlayerView view = (SimpleExoPlayerView) ((View)object).findViewById(R.id.simpleExoPlayerView);
               if (view.getVisibility() == View.VISIBLE) {
                   view.getPlayer().release();
               }
            }
            Log.i(LOG_TAG, "Inside destroyitem()" + position);
            ((ViewPager) items).removeView((View) object);
        }
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}

