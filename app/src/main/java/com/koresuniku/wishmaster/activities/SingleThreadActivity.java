package com.koresuniku.wishmaster.activities;


import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.FileProvider;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.koresuniku.wishmaster.R;

import com.koresuniku.wishmaster.fragments.PostFragment;
import com.koresuniku.wishmaster.ui.UIUtilities;
import com.koresuniku.wishmaster.utilities.CacheReduce;
import com.koresuniku.wishmaster.utilities.CommentTagHandler;
import com.koresuniku.wishmaster.utilities.Constants;
import com.koresuniku.wishmaster.makaba_markup.CustomLinkMovementMethod;
import com.koresuniku.wishmaster.utilities.FetchPath;
import com.koresuniku.wishmaster.utilities.SwipeRefreshLayoutBottom;
import com.koresuniku.wishmaster.utilities.TouchImageView;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.koresuniku.wishmaster.utilities.SimpleExoPlayerView;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.koresuniku.wishmaster.utilities.CacheReduce.trimCache;

public class SingleThreadActivity extends AppCompatActivity implements SwipeRefreshLayoutBottom.OnRefreshListener, ObservableScrollViewCallbacks {
    private final static String LOG_TAG = SingleThreadActivity.class.getSimpleName();
    private static final int PICKFILE_RESULT_CODE = 1;

    private static Context activityContext;
    private Context mContext;
    private static SingleThreadActivity activity;
    public static int mListViewPosition;
    private boolean isErrorContent;
    private boolean isRefreshed;
    private boolean firstTimeLoaded;
    private boolean isWebm;

    public static String intentThreadNumber;
    public static String intentBoard;
    public static String intentPage;
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

    private String threadTitle;

    public static int mediaPosition;
    private int beforeCount;
    private int afterCount;
    private int postionG;
    private int utilPosition;
    private int theOnlyPositionToLoad;
    private int webmViewsCounter;
    private boolean nextClicked;
    private int currentListViewSize;
    private int currentPlayerViewPosition;
    private boolean needToCloseMediaViewer;
    private boolean firstTimePlayerTurnedOn;
    private static boolean needToCloseContextView;
    private boolean imageAdapterIsFinalized;
    public static boolean postingFragmentAvailable;
    private int pageSelected;
    private boolean firstTimeImageAdapterInitialized;
    private boolean cvtIsCancelled;

    ViewPager viewPager;
    ImageAdapter adapterI;

    public static PostFragment pf;
    private ArrayList<Map<String, String>> threadItems;
    private ArrayList<String> parentsGeneral;
    public static ArrayList<String> numbersGeneral;
    private Map<String, ArrayList<String>> answersGeneral;
    public static Map<Integer, String> formattedTextGeneral;
    public static ArrayList<String> unformattedTextGeneral;
    public static Map<Integer, SpannableString> spannedTextGeneral;
    public static ArrayList<String> formattedTextsGeneral = new ArrayList<>();
    public ArrayList<View> itemViews;
    public static ArrayList<String> pathsGeneral;
    private static ArrayList<String> thumbsGeneral;
    private Map<Integer, View> playerViews;
    private Map<Integer, SimpleExoPlayer> openedPlayersToRelease;

    private LayoutInflater mLayoutInflater;
    public static ObservableListView mThreadsListView;
    public static FrameLayout mThreadContextItem;
    private FrameLayout mainLayoutContainer;
    public static FrameLayout tintView;
    public static ArrayList<View> openedAnswers;
    public static ScrollView itemContextScrollView;
    private boolean isScrolled = false;

    private static ThreadsAdapter adapter;
    private static Context mContextActivity;
    private ProgressBar progressBar;
    private FrameLayout mThreadsLayoutContainer;
    public static FrameLayout fragmentCotainer;
    public static RelativeLayout imageViewContainer;
    public static Menu mMenu;
    private SingleThreadActivity thisActivity;
    public static FrameLayout fragmentPostContainer;
    private FrameLayout tintForMedia;
    private SwipeRefreshLayoutBottom swipeRefreshLayoutBottom;

    public static Animation fallingUp;
    public static Animation fallingDown;

    private LruCache<String, Bitmap> mMemoryCache;

    private ThreadTask tt;
    private CreateViewsTask cvt;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onRestoreInstanceState");
        intentThreadNumber = savedInstanceState.getString(Constants.NUMBER);
        intentBoard = savedInstanceState.getString(Constants.BOARD);
        intentPage = savedInstanceState.getString(Constants.PAGE);
        fromThread = savedInstanceState.getString(Constants.FROM_THREAD);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(LOG_TAG, "onSaveInstanceState");
        outState.putString(Constants.NUMBER, intentThreadNumber);
        outState.putString(Constants.BOARD, intentBoard);
        outState.putString(Constants.PAGE, intentPage);
        outState.putString(Constants.FROM_THREAD, fromThread);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);

//        getSupportActionBar().setHideOnContentScrollEnabled(true);
//        getSupportActionBar().setShowHideAnimationEnabled(true);


        setContentView(R.layout.thread_single_layout);
        mContext = getApplicationContext();

        createMemoryCacheForBitmaps();

        Constants.POSTING_FRAGMENT_IS_OPENED = false;
        spannedTextGeneral = new HashMap<>();

        pathsGeneral = new ArrayList<>();
        thumbsGeneral = new ArrayList<>();
        answersGeneral = new HashMap<>();

        //adapter = new ThreadsAdapter(getApplication());
        Log.i(LOG_TAG, "onCreate()");

        mainLayoutContainer = (FrameLayout) findViewById(R.id.threads_layout_containe_inner);


        if (savedInstanceState != null) {
            Log.i(LOG_TAG, "savedInstanceState " + savedInstanceState.toString());
            intentThreadNumber = savedInstanceState.getString(Constants.NUMBER);
            intentBoard = savedInstanceState.getString(Constants.BOARD);
            intentPage = savedInstanceState.getString(Constants.PAGE);
            fromThread = savedInstanceState.getString(Constants.FROM_THREAD);
        } else {
            intentThreadNumber = getIntent().getStringExtra(Constants.NUMBER);
            intentBoard = getIntent().getStringExtra(Constants.BOARD);
            intentPage = getIntent().getStringExtra(Constants.PAGE);
            fromThread = getIntent().getStringExtra(Constants.FROM_THREAD);
        }

        swipeRefreshLayoutBottom = (SwipeRefreshLayoutBottom) findViewById(R.id.srlb);
        mThreadsListView = (ObservableListView) findViewById(R.id.lv);

        mThreadsListView.setScrollViewCallbacks(this);
        mThreadsListView.setOnScrollListener(onScrollListener);
        mThreadsListView.setFastScrollEnabled(true);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Log.i(LOG_TAG, "progressBar is null " + (progressBar == null));

        itemViews = new ArrayList<>();
        mThreadsListView.setAdapter(adapter);
//
        fragmentCotainer = (FrameLayout) findViewById(R.id.fragment_container);
        fragmentPostContainer = (FrameLayout) findViewById(R.id.fragment_post_container);
        fragmentPostContainer.setVisibility(View.VISIBLE);

        tt = new ThreadTask();
        tt.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();


        Log.i(LOG_TAG, "onStart()");
        //setTitle("");
        if (threadTitle == null) {
            setTitle(threadTitle);
        }


        Constants.FROM_SINGLE_THREAD = true;
        isErrorContent = false;
        firstTimeLoaded = true;
        needToCloseMediaViewer = false;
        firstTimeLoaded = true;
        needToCloseContextView = false;
        imageAdapterIsFinalized = true;
        postingFragmentAvailable = false;
        utilPosition = 0;
        thisActivity = this;
        pageSelected = 0;
        firstTimeImageAdapterInitialized = true;

        intentThreadNumber = getIntent().getStringExtra(Constants.NUMBER);
        intentBoard = getIntent().getStringExtra(Constants.BOARD);
        intentPage = getIntent().getStringExtra(Constants.PAGE);
        fromThread = getIntent().getStringExtra(Constants.FROM_THREAD);
        //pf = new PostFragment(getApplicationContext(), this, null);


        playerViews = new HashMap<>();
        openedPlayersToRelease = new HashMap<>();


        activity = this;
        activityContext = getApplicationContext();


        //itemViewsCopy = new ArrayList<>();
        openedAnswers = new ArrayList<>();

        //mListViewPosition = 0;

        mContextActivity = getApplicationContext();

        imageViewContainer = (RelativeLayout) findViewById(R.id.image_view_container);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        mThreadsLayoutContainer = (FrameLayout) findViewById(R.id.threads_layout_container);


        mThreadContextItem = (FrameLayout) findViewById(R.id.context_item);
        tintView = (FrameLayout) findViewById(R.id.tint_view);
        tintView.setOnClickListener(onBackTintClickListener);
        itemContextScrollView = (ScrollView) findViewById(R.id.scroll_item_context);
        tintForMedia = (FrameLayout) findViewById(R.id.tint_for_media);
        fallingUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.falling_up);
        fallingDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.falling_down);


        mLayoutInflater = getLayoutInflater();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setShowHideAnimationEnabled(true);
        Log.i(LOG_TAG, "Inside onResume " + mListViewPosition);
        //mThreadsListView.setSelection(mListViewPosition);
        //mThreadsListView.smoothScrollToPosition(mListViewPosition);

        Log.i(LOG_TAG, "num " + intentThreadNumber);
        Log.i(LOG_TAG, "brd " + intentBoard);
        Log.i(LOG_TAG, "pg " + intentPage);


        if (Constants.POSTING_FRAGMENT_IS_OPENED) {
            actionWrite(null);
        }

        if (intentThreadNumber != null) {
            if (intentThreadNumber.equals("435999")) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.JILL_STRING, String.valueOf(Constants.JILL_COUNTER++));
                editor.apply();
                editor.commit();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "Inside onPause()");
        closeFullMedia();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop()");
        formattedTextsGeneral = new ArrayList<>();
        CacheReduce.trimCache(getApplicationContext());
        System.gc();
        //Constants.POSTING_FRAGMENT_IS_OPENED = false;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy()");
        try {
            trimCache(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.media_fragment_menu, menu);
        mMenu = menu;
        MenuItem saveItem = menu.findItem(R.id.action_save);
        saveItem.setVisible(false);
        MenuItem closeItem = menu.findItem(R.id.action_close);
        closeItem.setVisible(false);
        // MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        //refreshItem.setVisible(false);
        MenuItem actionAttach = menu.findItem(R.id.action_attach);
        actionAttach.setVisible(false);
        MenuItem actionSend = menu.findItem(R.id.action_send);
        actionSend.setVisible(false);

        //mMenu.findItem(R.id.action_refresh).setEnabled(false);
        // mMenu.findItem(R.id.action_refresh_single).setEnabled(false);

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
//            case R.id.action_refresh: {
//                beforeCount = numbersGeneral.size() - 1;
//                isRefreshed = true;
//                MenuItem actionRefreshSingle = mMenu.findItem(R.id.action_refresh_single);
//                MenuItem actionRefresh = mMenu.findItem(R.id.action_refresh);
//                actionRefresh.setEnabled(false);
//                actionRefresh.setEnabled(false);
//                ThreadTask tt = new ThreadTask();
//                tt.execute();
//                break;
//            }
//            case R.id.action_refresh_single: {
//                MenuItem actionRefreshSingle = mMenu.findItem(R.id.action_refresh_single);
//                MenuItem actionRefresh = mMenu.findItem(R.id.action_refresh);
//                actionRefreshSingle.setEnabled(false);
//                actionRefresh.setEnabled(false);
//                refreshThread();
//                break;
//            }
            case R.id.action_write: {
                actionWrite(null);
                break;
            }
            case R.id.action_save: {
                SaveFileTask sft = new SaveFileTask();
                sft.execute();
                break;
            }

        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICKFILE_RESULT_CODE: {
                if (resultCode == RESULT_OK) {
                    //Log.i(LOG_TAG, "raw path " + data.getData().getAuthority() + data.getData().getPath());

                    Log.i(LOG_TAG, "raw path " + data.getData());

                    // String filePath = FetchPath.getPath(getApplicationContext(), Uri.parse(data.getData().getAuthority() + data.getData().getPath()));
                    //String filePath = getRealPathFromURI(getApplicationContext(), data.getData());
                    //String pathToCheck = data.
                    //Log.i(LOG_TAG, "real filepath " + filePath);

                    String filePath = "";
                    Log.i(LOG_TAG, "sdk version " + Build.VERSION.SDK_INT);
                    if (Build.VERSION.SDK_INT >= 23) {
//                        Log.i(LOG_TAG, "raw path " + data.getDataString());
//                        String pathToCheck = getRealPathFromURI(getApplicationContext(), Uri.parse(data.getDataString()));
//                        Log.i(LOG_TAG, "pathToCheck " + pathToCheck);

                        filePath = FetchPath.getPath(getApplicationContext(), data.getData());
                        Constants.FILES_TO_ATTACH.add(filePath);

                    } else {
                        filePath = FetchPath.getPath(getApplicationContext(), data.getData());
                        Constants.FILES_TO_ATTACH.add(filePath);
                    }
                    String name = "";
                    try {
                        for (int i = filePath.length() - 1; i >= 0; i--) {
                            if (filePath.substring(i, i + 1).equals("/")) {
                                name = filePath.substring(i, filePath.length());
                            }
                        }
                    } catch (NullPointerException e) {
                        Log.i(LOG_TAG, "nullpointerexception");
                        Toast t = Toast.makeText(getApplicationContext(), "Непостабельный формат", Toast.LENGTH_SHORT);
                        t.show();
                    }
                    Log.i(LOG_TAG, "Received name " + name);
                    Constants.FILES_NAMES_TO_ATTACH.add(name);
                }
            }
        }
    }

    public void onBackPressed() {
        Constants.SPOILERS_LOCATIONS = new HashMap<>();

        if (needToCloseMediaViewer) {
            Log.i(LOG_TAG, "Inside onBackPressed() needToCloseMediaViewer");
            closeFullMedia();
            needToCloseMediaViewer = false;
            return;
        }

        if (postingFragmentAvailable) {
            Log.i(LOG_TAG, "Inside onBackPressed() postingFragmentAvailable");
            closePostingFragment();
            postingFragmentAvailable = false;
            return;
        }

        if (needToCloseContextView) {
            Log.i(LOG_TAG, "Inside onBackPressed() needToCloseContextView");
            onBackTintListenerBackPressed();
            return;
        }
        if (isErrorContent) {
            Log.i(LOG_TAG, "Inside onBackPressed() isErrorContent");
            isErrorContent = false;
        }

        Constants.FILES_TO_ATTACH = new ArrayList<>();
        Constants.FILES_NAMES_TO_ATTACH = new ArrayList<>();
        itemViews = new ArrayList<>();

        if (tt != null && !tt.isCancelled()) {
            tt.cancel(true);
            tt = null;
        }
        if (cvt != null && !cvt.isCancelled()) {
            Log.i(LOG_TAG, "cvt is cancelling");
            cvt.cancel(true);
            cvtIsCancelled = true;
            cvt = null;
        }

        super.onBackPressed();
        Log.i(LOG_TAG, "JSON_PAGES " + Constants.JSON_PAGES.size());
        CacheReduce.trimCache(getApplicationContext());
    }

    private void createMemoryCacheForBitmaps() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 16;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void showPopup(View v) {
        final String number = String.valueOf(v.getContentDescription());
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        PopupMenu popup = new PopupMenu(actionBar.getThemedContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_options_menu, popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.item_write: {
                        actionWrite(number);
                        break;
                    }
                    case R.id.item_collapse: {
                        actionCollapse(number);
                    }
                }
                return true;
            }
        });
    }

    private void actionWrite(String number) {
        postingFragmentAvailable = true;
        fragmentPostContainer.setVisibility(View.VISIBLE);
        Constants.LINK_TO_ANSWER = number;

        imageViewContainer.setVisibility(View.GONE);
        MenuItem actionClose = mMenu.findItem(R.id.action_close);
        // MenuItem actionRefreshSingle = mMenu.findItem(R.id.action_refresh_single);
        MenuItem actionWrite = mMenu.findItem(R.id.action_write);
        MenuItem actionAttach = mMenu.findItem(R.id.action_attach);
        MenuItem actionSend = mMenu.findItem(R.id.action_send);

        actionSend.setVisible(true);
        actionAttach.setVisible(true);
        // actionRefreshSingle.setVisible(false);
        actionClose.setVisible(false);
        actionWrite.setVisible(false);

        Constants.LISTVIEW_POSITION = mThreadsListView.getLastVisiblePosition();

        fragmentPostContainer.setAnimation(fallingUp);
        if (number != null) {
            pf = new PostFragment(getApplicationContext(), this, number);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_post_container, pf)
                    .commit();
        } else {
            pf = new PostFragment(getApplicationContext(), this, null);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_post_container, pf)
                    .commit();
        }
        //fragmentPostContainer.setVisibility(View.VISIBLE);
        fallingUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                fragmentCotainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fragmentPostContainer.startAnimation(fallingUp);

        fragmentCotainer.setOnClickListener(null);
    }

    public void closePostingFragment() {
        Constants.FILES_TO_ATTACH = new ArrayList<>();
        //mThreadsListView.scrollListBy(mThreadsListView.getCount() - 1);
        Constants.POSTING_FRAGMENT_IS_OPENED = false;
        Constants.POSTING_EMAIL = null;
        Constants.POSTING_COMMENT = null;
        Constants.POSTING_CAPTCHA_ID = null;
        Constants.POSTING_CAPTCHA_ANSWER = null;
        Constants.POSTING_IS_SAGE = false;
        Constants.POSTING_CAPTCHA_IMAGE = null;
        Constants.LINK_TO_ANSWER = null;

        fragmentPostContainer.setAnimation(fallingDown);
        Log.i(LOG_TAG, "Before start animation falling down");
        fragmentPostContainer.startAnimation(fallingDown);
        fallingDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                getSupportFragmentManager().beginTransaction()
                        .remove(pf)
                        .commit();
                MenuItem saveItem = mMenu.findItem(R.id.action_save);
                saveItem.setVisible(false);
                MenuItem closeItem = mMenu.findItem(R.id.action_close);
                closeItem.setVisible(false);
                MenuItem actionWrite = mMenu.findItem(R.id.action_write);
                actionWrite.setVisible(true);
                MenuItem actionAttach = mMenu.findItem(R.id.action_attach);
                actionAttach.setVisible(false);
                MenuItem actionSend = mMenu.findItem(R.id.action_send);
                actionSend.setVisible(false);
                fragmentCotainer.setVisibility(View.GONE);
                imageViewContainer.setVisibility(View.VISIBLE);
                fragmentPostContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        ////fragmentCotainer.setOnClickListener(null);
    }

    private void actionCollapse(String number) {
        Log.i(LOG_TAG, "itemViews.size() " + itemViews.size());
        Log.i(LOG_TAG, "inside actionCollapse() " + number);
        Log.i(LOG_TAG, "numbersGeneral " + this.numbersGeneral);
        int commentPosition;
        if (Constants.COLLAPSED_COMMENTS_POSITIONS.get(number) != null) {
            Log.i(LOG_TAG, "not null");
            commentPosition = Constants.COLLAPSED_COMMENTS_POSITIONS.get(number);
        } else {
            Log.i(LOG_TAG, "is null");
            commentPosition = numbersGeneral.indexOf(number);
        }
        Log.i(LOG_TAG, "threadPosition " + commentPosition);
        View collapsedItemView = getLayoutInflater().inflate(R.layout.collapsed_thread, null, false);
        View itemToSave = itemViews.get(commentPosition);
        TextView threadNumberTextView = (TextView) collapsedItemView.findViewById(R.id.collapsed_thread_number);
        TextView threadDescriptionTextView = (TextView) collapsedItemView.findViewById(R.id.collapsed_thread_description);
        threadNumberTextView.setText("№" + number);
        String desc = formattedTextsGeneral.get(commentPosition);
        threadDescriptionTextView.setText("(" + desc + ")");

        Constants.COLLAPSED_COMMENTS.put(number, itemToSave);
        itemViews.remove(commentPosition);
        itemViews.add(commentPosition, collapsedItemView);
        adapter.notifyDataSetChanged();
    }

    public void refreshThread() {
        beforeCount = numbersGeneral.size() - 1;
        isRefreshed = true;
        currentListViewSize = numbersGeneral.size();
        ThreadTask tt = new ThreadTask();
        tt.execute();
//        CreateViewsTask cvt = new CreateViewsTask();
//        cvt.execute();
    }

    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            //Log.i(LOG_TAG, "onScrollListener " + i);
            if (!isScrolled) {
                cvt = new CreateViewsTask();
                cvt.execute();
            }
            isScrolled = true;
        }

        @Override
        public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            //Log.i(LOG_TAG, "onScroll " + i2);

        }
    };

    public void addAnswerView(String number) {

        Log.i(LOG_TAG, "add answer view()");
        boolean answerExists = false;
        needToCloseContextView = true;


        for (int i = 0; i < numbersGeneral.size(); i++) {

            if (numbersGeneral.get(i).equals(number)) {
                answerExists = true;
//                Log.i(LOG_TAG, "Is copy equals original " + (copyOfThreadItems == threadItems));
//                if (copyOfThreadItems != threadItems) {
//                    threadItems = copyOfThreadItems;
//                    adapter.notifyDataSetChanged();
//                }

                View view;

                if (!(itemViews.size() - 1 >= i)) {
//                    View newView = mThreadsListView.getAdapter().getView(i, null, null);
//                    view = newView;
//
                    view = createSingleView(i, true);
                } else {
                    //view = mThreadsListView.getAdapter().getView(i, null, null);
                    view = createSingleView(i, true);
                }

                Log.i(LOG_TAG, "answer number opened " + i);
                Constants.ANSWER_NUMBER_OPENED = i;

                tintView.setVisibility(View.VISIBLE);

                //Log.i(LOG_TAG, "viewsCount in mThreadContextItem " + mThreadContextItem.getChildCount());
                itemContextScrollView.removeAllViews();
                itemContextScrollView.addView(view);

                openedAnswers.add(view);
                mThreadsListView.setEnabled(false);

                if (getSupportActionBar().isShowing()) {
                    UIUtilities.hideActionBar(getSupportActionBar());
                }

                break;
            }
        }

        if (!answerExists) {
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
            Log.i(LOG_TAG, "onBackTintListenerBackPressed() " + "1");
            Constants.ANSWER_NUMBER_OPENED = -1;
            itemContextScrollView.removeAllViews();
            openedAnswers = null;
            mThreadsListView.setEnabled(true);
            //itemContextScrollView.removeAllViews();
            tintView.setVisibility(View.GONE);
            openedAnswers = new ArrayList<>();
            needToCloseContextView = false;
        } else {
            Log.i(LOG_TAG, "onBackTintListenerBackPressed() " + "2");

            itemContextScrollView.removeAllViews();
            //itemContextScrollView.removeAllViews();
            View viewBack = openedAnswers.get(openedAnswers.size() - 2);
            //if (viewBack != null) {
            Constants.ANSWER_NUMBER_OPENED--;
            openedAnswers.remove(openedAnswers.size() - 1);
//            FrameLayout fl = new FrameLayout(activityContext);
//            fl.addView(viewBack);
//                itemContextScrollView.addView(fl);
            //}
            itemContextScrollView.addView(viewBack);
        }
    }

    private View.OnClickListener onBackTintClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onBackTintListenerBackPressed();
        }
    };

    public ViewGroup getActionBar(View view) {
        try {
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;

                if (viewGroup instanceof android.support.v7.widget.Toolbar) {
                    return viewGroup;
                }

                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    ViewGroup actionBar = getActionBar(viewGroup.getChildAt(i));

                    if (actionBar != null) {
                        return actionBar;
                    }
                }
            }
        } catch (Exception e) {
        }

        return null;
    }

    public View.OnClickListener mediaBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(LOG_TAG, "mediaBackListener");
            if (getSupportActionBar().isShowing()) {
                UIUtilities.hideActionBar(getSupportActionBar());
            } else {
                UIUtilities.showActionBar(getSupportActionBar());
            }
        }
    };

    private void addFullMedia(String path) {
        if (getSupportActionBar().isShowing()) {
            UIUtilities.hideActionBar(getSupportActionBar());
        }

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
                    pageSelected = position;
                    // if (isWebm) {
                    if (playerViews.size() != 0) {
                        if (position > currentPlayerViewPosition) {
                            View v = (View) playerViews.get(position - 1);
                            if (v != null) {
                                Log.i(LOG_TAG, "Inside disabling");
                                SimpleExoPlayerView player = (SimpleExoPlayerView) v.findViewById(R.id.simpleExoPlayerView);
                                if (player != null) {
                                    if (player.getPlayer() != null) {
                                        player.getPlayer().setPlayWhenReady(false);
                                    }
                                }
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
                                if (player.getPlayer() != null) {
                                    player.getPlayer().setPlayWhenReady(false);
                                }
                            }
                            View vCurrent = (View) playerViews.get(position);
                            if (vCurrent != null) {
                                Log.i(LOG_TAG, "Inside enabling");
                                SimpleExoPlayerView player = (SimpleExoPlayerView) vCurrent.findViewById(R.id.simpleExoPlayerView);
                                if (player != null) {
                                    if (player.getPlayer() != null) {
                                        player.getPlayer().setPlayWhenReady(false);
                                    }
                                }
                            }
                            currentPlayerViewPosition = position;
                        }
                    }
                    currentPlayerViewPosition = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            viewPager.setAdapter(adapterI);
            viewPager.setCurrentItem(postionG);
            ;
        }
    }

    private void closeFullMedia() {
        //getSupportActionBar().show();
        firstTimePlayerTurnedOn = true;
        fragmentCotainer.setVisibility(View.GONE);
        if (adapterI == null) return;
        try {
            adapterI.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        viewPager = null;

        CacheReduce.trimCache(getApplicationContext());
        MenuItem close = mMenu.findItem(R.id.action_close);
        close.setVisible(false);
        MenuItem save = mMenu.findItem(R.id.action_save);
        save.setVisible(false);
        //MenuItem refreshSingle = mMenu.findItem(R.id.action_refresh_single);
        // refreshSingle.setVisible(true);
        // MenuItem refresh = mMenu.findItem(R.id.action_refresh);
        //refresh.setVisible(false);
        MenuItem actionWrite = mMenu.findItem(R.id.action_write);
        actionWrite.setVisible(true);
        needToCloseMediaViewer = false;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayoutBottom.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayoutBottom.setRefreshing(true);
                refreshThread();

            }
        });
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

        if (scrollState == ScrollState.UP) {
            if (getSupportActionBar().isShowing()) {
                UIUtilities.hideActionBar(getSupportActionBar());
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!getSupportActionBar().isShowing()) {
                UIUtilities.showActionBar(getSupportActionBar());
            }
        }
    }

    private class ViewHolder {
        TextView mThreadItemHeader;
        ImageView mThreadItemImage;
        TextView mThreadItemBody;

        ImageView imageContainer1;
        ImageView imageContainer2;
        ImageView imageContainer3;
        ImageView imageContainer4;
        ImageView expandOptionsImageView;

        LinearLayout mThreadAnswersContainer;
    }

    private class ThreadsAdapter extends BaseAdapter {
        private Context mContext;
        private ViewHolder viewHolder;
        private int counterId = -1;

        public ThreadsAdapter(Context context) {
            mContext = context;
            //threadsList = list;
            progressBar.setVisibility(View.GONE);
            mThreadsListView.setLongClickable(true);
//            mThreadsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//                @Override
//                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                    actionWrite(numbersGeneral.get(i));
//                    return true;
//                }
//            });
        }

        @Override
        public int getCount() {
            return itemViews.size();
            //return 2;
        }

        @Override
        public View getItem(int i) {
            return itemViews.get(i);
        }

        @Override
        public long getItemId(int i) {
            View item = itemViews.get(i);
            return itemViews.indexOf(item);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
//            View convertView = getLayoutInflater().inflate(R.layout.answer_item_container, viewGroup, true);
//            FrameLayout fl = (FrameLayout) convertView.findViewById(R.id.container);
//            fl.addView(itemViews.get(i));
//            if (view != null && view.getContentDescription() == null) {
//                Log.i(LOG_TAG, "content description " + view.getContentDescription());
//                return view = itemViews.get(i);
//            }


            if (itemViews == null) {
                return new View(mContext);
            }
            if (itemViews.get(i) == null) {
                return new View(mContext);
            }
            if (cvtIsCancelled) {
                return new View(mContext);
            }
            view = itemViews.get(i);

//            Holder holder = new Holder();
//            holder.answersTextView = (TextView) view.findViewById(R.id.answers);
//            String number = numbersGeneral.get(i);
//            Log.i(LOG_TAG, "number " + number);
//            ArrayList<String> answersArrayList = answersGeneral.get(number);
//            Log.i(LOG_TAG, "answersGeneral " + answersGeneral);
//            Log.i(LOG_TAG, "answersArrayList is null " + (answersArrayList != null));
//            SpannableStringBuilder builderAnswers = new SpannableStringBuilder();
//            for (int index = 0; index < answersArrayList.size(); index++) {
//                builderAnswers.append("<a href=\"/#" + answersArrayList.get(index) + "\">>>");
//                builderAnswers.append(answersArrayList.get(index));
//                builderAnswers.append("\t");
//                builderAnswers.append("</a>");
//            }
//            holder.answersTextView.setMovementMethod(
//                    CustomLinkMovementMethod.getInstance(mContext, false, activity, number, i, null));
//            holder.answersTextView.setText(Html.fromHtml(builderAnswers.toString()), TextView.BufferType.SPANNABLE);

            // return view;
            final Map<String, String> item = threadItems.get(i);

            String[] splittedPath = null;
            if (path != null) {
                splittedPath = path.split(" ");
            }

            final String thumb = item.get(Constants.THUMB);

            final ImageView mThreadItemImage;

            if (thumb != null) {
                final String[] splittedThumbs = thumb.split(" ");
                if (splittedThumbs.length == 1) {
//                    view = mLayoutInflater
//                            .inflate(R.layout.thread_item_single_image_full, null, false);

                    //layoutMode = 0;
                    mThreadItemImage = (ImageView) view.findViewById(R.id.thread_item_image);

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
                                addBitmapToMemoryCache(String.valueOf(finalI), BitmapFactory.decodeStream(input));
                            } catch (IOException e) {
                                // Log exception
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void voids) {
//                            Picasso.with(getApplicationContext()).load(getBitmapFromMemCache(String.valueOf(i))
//                                    .into(mThreadItemImage);


                            mThreadItemImage.setImageBitmap(getBitmapFromMemCache(String.valueOf(finalI)));
                        }
                    }.execute();
//                        Picasso.with(mContext).load("https://2ch.hk/" + splittedThumbs[0])
//                                .into(viewHolder.mThreadItemImage);
//                    viewHolder.mThreadItemImage.setContentDescription(path.substring(0, path.length() - 1));
//                    viewHolder.mThreadItemImage.setOnClickListener(mediaClickListener);
                    //splittedThumbs = null;
                } else {
//                    view = mLayoutInflater
//                            .inflate(R.layout.thread_item_multi_image, null, false);
                    //layoutMode = 1;
                    //view.setTag(viewHolder);
                    final ImageView imageContainer1 = (ImageView) view.findViewById(R.id.image_container_1);
                    final ImageView imageContainer2 = (ImageView) view.findViewById(R.id.image_container_2);
                    final ImageView imageContainer3 = (ImageView) view.findViewById(R.id.image_container_3);
                    final ImageView imageContainer4 = (ImageView) view.findViewById(R.id.image_container_4);

                    final int finalI = i;
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                for (int index = 0; index < splittedThumbs.length; index++) {

                                    URL url = new URL("https://2ch.hk/" + splittedThumbs[index]);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setDoInput(true);
                                    connection.connect();
                                    InputStream input = connection.getInputStream();
                                    addBitmapToMemoryCache(String.valueOf(finalI) + index, BitmapFactory.decodeStream(input));
                                }
                            } catch (IOException e) {
                                // Log exception
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void voids) {
//                            Picasso.with(getApplicationContext()).load(getBitmapFromMemCache(String.valueOf(i))
//                                    .into(mThreadItemImage);

                            for (int index = 0; index < splittedThumbs.length; index++) {
                                switch (index) {
                                    case 0:
                                        imageContainer1.setOnClickListener(mediaClickListener);
                                        imageContainer1.setImageBitmap(getBitmapFromMemCache(String.valueOf(finalI) + index));
                                        break;
                                    case 1:
                                        imageContainer2.setOnClickListener(mediaClickListener);
                                        imageContainer2.setImageBitmap(getBitmapFromMemCache(String.valueOf(finalI) + index));

                                        break;
                                    case 2:
                                        imageContainer3.setOnClickListener(mediaClickListener);
                                        imageContainer3.setImageBitmap(getBitmapFromMemCache(String.valueOf(finalI) + index));

                                        break;
                                    case 3:
                                        imageContainer4.setOnClickListener(mediaClickListener);
                                        imageContainer4.setImageBitmap(getBitmapFromMemCache(String.valueOf(finalI) + index));

                                        break;
                                }
                            }
                        }
                    }.execute();
                }
            }


            return view;
        }

        public SpannableString setSpoilerSpans(int position, SpannableString ss) {
            ArrayList<String> spoilersArray = Constants.SPOILERS_LOCATIONS.get(position);
            if (spoilersArray != null) {
                for (String spoiler : spoilersArray) {
                    String[] locals = spoiler.split(" ");
                    int start = Integer.parseInt(locals[0]);
                    int end = Integer.parseInt(locals[1]);
                    Log.i(LOG_TAG, "BEFORE SETTING SPAN");
                    ss.setSpan(new BackgroundColorSpan(Color.parseColor("#b4b4b4")),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ss.setSpan(new ForegroundColorSpan(Color.parseColor("#00ffffff")),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                Log.i(LOG_TAG, "spoilersArray IS NULL");
            }
            return ss;
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

        private void getSpoilers(int position) {
            //Log.i(LOG_TAG, "getSpoilers(), " + position);
            ArrayList<String> spoilersLocations = new ArrayList<>();
            ArrayList<String> spoilers = new ArrayList<>();
            Pattern p = Pattern
                    .compile("(<span[^>]+class\\s*=\\s*(\"|')spoiler\\2[^>]*>)[^<]*(</span>)");
            Matcher m = p.matcher(unformattedTextGeneral.get(position));
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

                String commentFormatted = formattedTextGeneral.get(position);

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
                if (Constants.SPOILERS_LOCATIONS.get(position) == null) {
                    Constants.SPOILERS_LOCATIONS.put(position, spoilersLocations);
                }
                if (Constants.SPOILERS_LOCATIONS.get(position).size() == 0) {
                    Constants.SPOILERS_LOCATIONS.put(position, spoilersLocations);
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            makeAnswersGeneral();
            Log.i(LOG_TAG, "FINAL SPOILERS " + Constants.SPOILERS_LOCATIONS);
            //currentListViewSize = numbersGeneral.size();

            cvt = new CreateViewsTask();
            cvt.execute();


        }

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

        int counter = 0;

        private ArrayList<Map<String, String>> formatJSON(String rawJSON) {
            ArrayList<Map<String, String>> result = new ArrayList<>();
            parentsGeneral = new ArrayList<>();
            numbersGeneral = new ArrayList<>();
            formattedTextGeneral = new HashMap<>();
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
                                    //Log.i(LOG_TAG, "webm found");

                                    // Log.i(LOG_TAG, "duration " + fileItem.get("duration"));
                                    if (fileItem.getString("duration") == null) {
                                        duration = "null ";
                                    }
                                    if (!fileItem.getString("duration").equals("")) {
                                        duration = fileItem.getString("duration") + " ";
                                    }
                                } else {
                                    duration = "null ";
                                }
                            } else {
                                pathsGeneral.add("https://2ch.hk/" + fileItem.getString("path"));
                                path += fileItem.getString("path") + " ";
                                if (path.substring(path.length() - 5, path.length() - 1).equals("webm")) {
                                    //Log.i(LOG_TAG, "webm found");
                                    duration += fileItem.getString("duration") + " ";
                                } else {
                                    duration += "null ";
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

                    //Log.i(LOG_TAG, "comment unformatted " + comment);
                    formattedTextGeneral.put(counter, commentFormatted);
                    formattedTextsGeneral.add(commentFormatted);

                    getSpoilers(counter);
                    //getLinks(counter);

                    counter++;
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

    private View createSingleView(int i, boolean needToLoadImage) {
        Log.i(LOG_TAG, "createSingleView() at " + i);
        View view;
        int layoutMode = -1;
        final ViewHolder viewHolder;
        final Map<String, String> item = threadItems.get(i);
        final boolean[] firstTimeAnswerItemSpinner = {false};

        String date = item.get(Constants.DATE);
        final String number = item.get(Constants.NUMBER_SINGLE_THREAD);
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

//        String date = "test";
//        final String number = "0";
//        String comment = "test";
//        String op = "test";
//        String subjectOfThread = "test";
//        String opName = "test";
//        String size = "test";
//        String width = "test";
//        String height = "test";
//        String duration = "test";
//        String path = "test";
//        String email = "test";

        String[] splittedPath = null;
        if (path != null) {
            splittedPath = path.split(" ");
        }

        viewHolder = new ViewHolder();
        final String thumb = item.get(Constants.THUMB);
        //final String thumb = "test";

        if (thumb != null) {
            String[] splittedThumbs = thumb.split(" ");
            if (splittedThumbs.length == 1) {
                view = mLayoutInflater
                        .inflate(R.layout.thread_item_single_image_full, null, false);

                layoutMode = 0;
                viewHolder.mThreadItemImage = (ImageView) view.findViewById(R.id.thread_item_image);

                if (needToLoadImage) {
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
                                addBitmapToMemoryCache(String.valueOf(finalI), BitmapFactory.decodeStream(input));
                            } catch (IOException e) {
                                // Log exception
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void voids) {
//                            Picasso.with(getApplicationContext()).load(getBitmapFromMemCache(String.valueOf(i))
//                                    .into(mThreadItemImage);

                            viewHolder.mThreadItemImage.setImageBitmap(getBitmapFromMemCache(String.valueOf(finalI)));
                        }
                    }.execute();
                }

//                        Picasso.with(mContext).load("https://2ch.hk/" + splittedThumbs[0])
//                                .into(viewHolder.mThreadItemImage);
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

                if (needToLoadImage) {
                    final int finalI = i;
                    final String[] finalSplittedThumbs = splittedThumbs;
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                for (int index = 0; index < finalSplittedThumbs.length; index++) {

                                    URL url = new URL("https://2ch.hk/" + finalSplittedThumbs[index]);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setDoInput(true);
                                    connection.connect();
                                    InputStream input = connection.getInputStream();
                                    addBitmapToMemoryCache(String.valueOf(finalI) + index, BitmapFactory.decodeStream(input));
                                }
                            } catch (IOException e) {
                                // Log exception
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void voids) {
//                            Picasso.with(getApplicationContext()).load(getBitmapFromMemCache(String.valueOf(i))
//                                    .into(mThreadItemImage);

                            for (int index = 0; index < finalSplittedThumbs.length; index++) {
                                switch (index) {
                                    case 0:
                                        viewHolder.imageContainer1.setOnClickListener(mediaClickListener);
                                        viewHolder.imageContainer1.setImageBitmap(getBitmapFromMemCache(String.valueOf(finalI) + index));
                                        break;
                                    case 1:
                                        viewHolder.imageContainer2.setOnClickListener(mediaClickListener);
                                        viewHolder.imageContainer2.setImageBitmap(getBitmapFromMemCache(String.valueOf(finalI) + index));

                                        break;
                                    case 2:
                                        viewHolder.imageContainer3.setOnClickListener(mediaClickListener);
                                        viewHolder.imageContainer3.setImageBitmap(getBitmapFromMemCache(String.valueOf(finalI) + index));

                                        break;
                                    case 3:
                                        viewHolder.imageContainer4.setOnClickListener(mediaClickListener);
                                        viewHolder.imageContainer4.setImageBitmap(getBitmapFromMemCache(String.valueOf(finalI) + index));

                                        break;
                                }
                            }
                        }
                    }.execute();
                } else {
                    for (int index = 0; index < splittedThumbs.length; index++) {
                        switch (index) {
                            case 0:
//                                    Picasso.with(mContext).load("https://2ch.hk/" + splittedThumbs[index])
//                                            .into(viewHolder.imageContainer1);
                                viewHolder.imageContainer1.setOnClickListener(mediaClickListener);
                                viewHolder.imageContainer1.setContentDescription(splittedPath[0]);
                                break;
                            case 1:
//                                    Picasso.with(mContext).load("https://2ch.hk/" + splittedThumbs[index])
//                                            .into(viewHolder.imageContainer2);
                                viewHolder.imageContainer2.setOnClickListener(mediaClickListener);
                                viewHolder.imageContainer2.setContentDescription(splittedPath[1]);
                                break;
                            case 2:
//                                    Picasso.with(mContext).load("https://2ch.hk/" + splittedThumbs[index])
//                                            .into(viewHolder.imageContainer3);
                                viewHolder.imageContainer3.setOnClickListener(mediaClickListener);
                                viewHolder.imageContainer3.setContentDescription(splittedPath[2]);
                                break;
                            case 3:
//                                    Picasso.with(mContext).load("https://2ch.hk/" + splittedThumbs[index])
//                                            .into(viewHolder.imageContainer4);
                                viewHolder.imageContainer4.setOnClickListener(mediaClickListener);
                                viewHolder.imageContainer4.setContentDescription(splittedPath[3]);
                                break;
                        }
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
                //Log.i(LOG_TAG, "duration " + duration);
                splittedDuration = duration.split(" ");
                //Log.i(LOG_TAG, "splittedDurationlngth " + splittedDuration.length);
            } else {
                splittedDuration = null;
                //Log.i(LOG_TAG, "duration is null");
            }

            if (layoutMode == 0) {
                TextView shortInfoTextView = (TextView) view.findViewById(R.id.short_info_textview);
                String shortInfo = "(" + splittedSize[0] + "Кб, " + splittedWidth[0] + "x" + splittedHeight[0] + ")";
                shortInfoTextView.setText(shortInfo);

                //Log.i(LOG_TAG, "duration.equals(\"null \") " + duration.equals("null "));
                if (!duration.equals("null ")) {
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
                    //Log.i(LOG_TAG, "ssplittedDuration[iInfo] " + splittedDuration[iInfo]);
                    switch (iInfo) {
                        case 0: {
                            if (!splittedDuration[iInfo].equals("null")) {
                                if (splittedDuration.length > iInfo) {
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
                                }

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
                            if (!splittedDuration[iInfo].equals("null")) {
                                if (splittedDuration.length > iInfo) {
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
                                }

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
                            if (!splittedDuration[iInfo].equals("null")) {
                                if (splittedDuration.length > iInfo) {
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
                                }
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
                            if (!splittedDuration[iInfo].equals("null")) {
                                if (splittedDuration.length > iInfo) {
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
                                }
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
        if (opName.equals("")) {
            opName = defaultOpName;

        }
        //Log.i(LOG_TAG, "email " + email);
        if (email.equals("mailto:sage")) {
            //Log.i(LOG_TAG, "is sage");
            opName = "<u><font color=\"#ff7000\">" + opName + "</font></u>";
        }
        if (i >= 1) {
            //Log.i(LOG_TAG, "Inside number post");
            builderHeader.append("<font color=\"#008000\">#" + (i + 1) + "</font>" + " ");
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

        //spannedTextGeneral.put(i, new SpannableString(builderBody));

        viewHolder.mThreadItemBody.getLinksClickable();

        //getSpoilers(number);
        //Log.i(LOG_TAG, "SPOILERS_LOCALIZATIONS " + Constants.SPOILERS_LOCALIZATIONS);

        if (builderBody.toString().length() == 0) {
            viewHolder.mThreadItemBody.setVisibility(View.GONE);
            if (layoutMode == 1) {
                Space spaceAfterComment = (Space) view.findViewById(R.id.space_after_comment);
                spaceAfterComment.setVisibility(View.GONE);
            }
        } else {
            viewHolder.mThreadItemBody.setMovementMethod(
                    CustomLinkMovementMethod.getInstance(mContext, true, activity, number, i, new SpannableString(builderBody)));
            SpannableString ss = new SpannableString(formattedTextGeneral.get(i));
            ss = new SpannableString(builderBody);
            ss = setSpoilerSpans(i, ss);
            viewHolder.mThreadItemBody.setContentDescription(String.valueOf(i));
            CommentTagHandler commentTagHandler = new CommentTagHandler(i, true, viewHolder.mThreadItemBody);
            viewHolder.mThreadItemBody.setText(Html.fromHtml(builderBody.toString(), null, commentTagHandler), TextView.BufferType.SPANNABLE);
            //viewHolder.mThreadItemBody.setText(Html.fromHtml(builderBody.toString()), TextView.BufferType.SPANNABLE);
            //viewHolder.mThreadItemBody.setText(Html.fromHtml(ss.toString()), TextView.BufferType.SPANNABLE);
            //viewHolder.mThreadItemBody.setText(ss);


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
                CustomLinkMovementMethod.getInstance(mContext, false, activity, number, i, null));
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

        // itemViews.add(view);
        view.setContentDescription(number);

        viewHolder.expandOptionsImageView = (ImageView) view.findViewById(R.id.expand_options);
        viewHolder.expandOptionsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setContentDescription(number);
                showPopup(view);
            }
        });
        return view;
    }

    public SpannableString setSpoilerSpans(int position, SpannableString ss) {
        ArrayList<String> spoilersArray = Constants.SPOILERS_LOCATIONS.get(position);
        if (spoilersArray != null) {
            for (String spoiler : spoilersArray) {
                String[] locals = spoiler.split(" ");
                int start = Integer.parseInt(locals[0]);
                int end = Integer.parseInt(locals[1]);
                Log.i(LOG_TAG, "BEFORE SETTING SPAN");
                ss.setSpan(new BackgroundColorSpan(Color.parseColor("#b4b4b4")),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(Color.parseColor("#00ffffff")),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else {
            Log.i(LOG_TAG, "spoilersArray IS NULL");
        }
        return ss;
    }

    public View.OnClickListener mediaClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String path = String.valueOf(view.getContentDescription());
            path = "https://2ch.hk/" + path;
            //Log.i(LOG_TAG, "on click path " + path);

            addFullMedia(path);
            MenuItem save = mMenu.findItem(R.id.action_save);
            MenuItem close = mMenu.findItem(R.id.action_close);
            //MenuItem refresh = mMenu.findItem(R.id.action_refresh);
            //MenuItem refreShSingle = mMenu.findItem(R.id.action_refresh_single);
            MenuItem actionWrite = mMenu.findItem(R.id.action_write);

            actionWrite.setVisible(false);
            // refreShSingle.setVisible(false);
            //refresh.setVisible(false);
            save.setVisible(true);
            close.setVisible(true);
        }
    };


    private class CreateViewsTask extends AsyncTask<Void, View, Void> {

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i(LOG_TAG, "onCancelled()");
        }

        @SuppressLint("InflateParams")
        @Override
        protected Void doInBackground(Void... voids) {


            if (isRefreshed) {
                Log.i(LOG_TAG, "currentListViewSize" + currentListViewSize);
                //if (!currentListViewSize != numbersGeneral.size());
                for (int i = currentListViewSize; i < numbersGeneral.size(); i++) {
                    //Log.i(LOG_TAG, "inside for " + i);
                    View view = createSingleView(i, false);
                    itemViews.add(view);

                }
                //isRefreshed = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayoutBottom.setRefreshing(false);
                        //mMenu.findItem(R.id.action_refresh).setEnabled(true);
                        //mMenu.findItem(R.id.action_refresh_single).setEnabled(true);
                        int diff = itemViews.size() - currentListViewSize;
                        createNewPostsToast(diff);
                    }
                });
                return null;
            }
            if (isScrolled) {
                for (int i = 10; i < numbersGeneral.size(); i++) {
                    View view = createSingleView(i, false);
                    //viewPosition = i;
                    onProgressUpdate(view);
                }
            } else {
                //if (getTitle().equals("")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTitle(formattedTextsGeneral.get(0));
                        threadTitle = formattedTextGeneral.get(0);
                    }
                });
                //}

//                for (int i = 0; i < numbersGeneral.size(); i++) {
//                    View view = getLayoutInflater().inflate(R.layout.thread_item_void, null, false);
//                    itemViews.add(view);
//                }
                Log.i(LOG_TAG, "itemViews.size() " + itemViews.size());
                int maxLength;
                if (numbersGeneral.size() < 10) {
                    maxLength = numbersGeneral.size();
                } else {
                    maxLength = 10;
                }

                //maxLength = numbersGeneral.size();

                for (int i = 0; i < maxLength; i++) {
                    if (cvtIsCancelled) {
                        return null;
                    }
                    View view = createSingleView(i, false);
                    //  View view = createSingleViewVoid(i);

                    //itemViews.add(view);
                    //itemViews.remove(i);
                    //itemViews.add(i, view);
                    onProgressUpdate(view);
                }
            }
            return null;
        }

        int viewPosition;

        @Override
        protected void onProgressUpdate(final View... v) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    itemViews.add(v[0]);
                    //itemViews.remove(viewPosition);
                    //itemViews.add(viewPosition, v[0]);
                    //mThreadsListView.requestLayout();
                    if (adapter == null) {
                        Log.i(LOG_TAG, "creating new adapter");
                        adapter = new ThreadsAdapter(getApplicationContext());
                        mThreadsListView.setAdapter(adapter);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(LOG_TAG, "Created itemViews " + itemViews.size());
            swipeRefreshLayoutBottom = (SwipeRefreshLayoutBottom) findViewById(R.id.srlb);
            Log.i(LOG_TAG, "srlf is not null " + (swipeRefreshLayoutBottom != null));
            if (swipeRefreshLayoutBottom != null) {
                swipeRefreshLayoutBottom.setOnRefreshListener(thisActivity);
            }
            if (isScrolled) {
                adapter.notifyDataSetChanged();
                //mMenu.findItem(R.id.action_refresh).setEnabled(true);
                //mMenu.findItem(R.id.action_refresh_single).setEnabled(true);
            } else if (isRefreshed) {
                Log.i(LOG_TAG, "previous size " + currentListViewSize);
                Log.i(LOG_TAG, "current size " + numbersGeneral.size());

                isRefreshed = false;
            } else {
                adapter = new ThreadsAdapter(getApplicationContext());
                mThreadsListView.setAdapter(adapter);
            }

        }

        private void createNewPostsToast(int diff) {
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
        }
    }

    public class ImageAdapter extends PagerAdapter {
        private final String LOG_TAG = com.koresuniku.wishmaster.activities.ImageAdapter.class.getSimpleName();
        Context context;
        private ViewGroup items;

        ImageAdapter(Context context) {
            this.context = context;

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
            imageAdapterIsFinalized = true;
            firstTimePlayerTurnedOn = false;
            currentPlayerViewPosition = -1;
            for (int i = 0; i < items.getChildCount(); i++) {
                View v = items.getChildAt(i);
                SimpleExoPlayerView simpleExoPlayerView = (SimpleExoPlayerView) v.findViewById(R.id.simpleExoPlayerView);

                if (simpleExoPlayerView != null) {
                    simpleExoPlayerView.getPlayer().stop();
                    simpleExoPlayerView.getPlayer().release();
                    simpleExoPlayerView.setPlayer(null);
                }
            }
            items = new ViewPager(getApplicationContext());
            super.finalize();

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (firstTimeImageAdapterInitialized) {
                pageSelected = position;
                firstTimeImageAdapterInitialized = false;
            }

            CacheReduce.trimCache(getApplicationContext());
            webmViewsCounter++;
            items = container;
            //Log.i(LOG_TAG, "Inside instantiate " + position);
            LayoutInflater layoutinflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = layoutinflater.inflate(R.layout.context_item_layout, null);

            String path = pathsGeneral.get(position);
            for (int i = 0; i < pathsGeneral.size(); i++) {
                if (path.equals(String.valueOf(pathsGeneral.get(i)))) {
                    //Log.i(LOG_TAG, "Got position!");
                    mediaPosition = i;
                }
            }

            if (path.substring(path.length() - 4, path.length()).equals("webm")) {
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
                simpleExoPlayerView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        Log.i(LOG_TAG, "simpleExoPlayerView touched " + motionEvent.getAction());
                        if (motionEvent.getAction() == 1) {

                            if (getSupportActionBar().isShowing()) {
                                UIUtilities.hideActionBar(getSupportActionBar());
                            } else {
                                UIUtilities.showActionBar(getSupportActionBar());
                            }
                        }
                        return false;
                    }
                });

                simpleExoPlayerView.setVisibility(View.VISIBLE);
                simpleExoPlayerView.setPlayer(player);
                TouchImageView newImage = (TouchImageView) v.findViewById(R.id.imageViews);
                newImage.setVisibility(View.GONE);
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                        Util.getUserAgent(context, "yourApplicationName"),
                        (TransferListener<? super DataSource>) bandwidthMeter);
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                MediaSource videoSource = new ExtractorMediaSource(Uri.parse(path),
                        dataSourceFactory, extractorsFactory, null, null);
                Log.i(LOG_TAG, "Before prepare");
                player.prepare(videoSource);
                player.seekTo(0);
                if (!firstTimePlayerTurnedOn) {
                    Log.i(LOG_TAG, "setting player true");
                    player.setPlayWhenReady(true);
                    firstTimePlayerTurnedOn = true;
                } else {
                    Log.i(LOG_TAG, "setting player false");
                    player.setPlayWhenReady(false);
                }
                isWebm = true;
                // player.setPlayWhenReady(false);
                ((ViewPager) items).addView(v, 0);

                playerViews.put(position, v);
            } else {
                final TouchImageView newImage = (TouchImageView) v.findViewById(R.id.imageViews);
                Log.i(LOG_TAG, "Path " + path.substring(path.length() - 3, path.length()));
                if (path.substring(path.length() - 3, path.length()).equals("gif")) {
                    Log.i(LOG_TAG, "Path " + path.substring(path.length() - 3, path.length()));
                    Glide.with(context).load(path).asGif().placeholder(R.drawable.load_2).diskCacheStrategy(DiskCacheStrategy.SOURCE).error(R.drawable.error11).into(newImage);
                } else {
                    Picasso.with(context).load(path).placeholder(R.drawable.load_2).into(newImage);
                }
                newImage.setOnClickListener(mediaBackListener);
//
                isWebm = false;
                ((ViewPager) items).addView(v, 0);
            }

            v.setOnClickListener(mediaBackListener);
            return v;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (isWebm) {
                SimpleExoPlayerView view = (SimpleExoPlayerView) ((View) object).findViewById(R.id.simpleExoPlayerView);
                if (view.getVisibility() == View.VISIBLE) {
                    if (view.getPlayer() != null) {
                        view.getPlayer().release();
                        view.setPlayer(null);
                    }
                }
            }
            ((ViewPager) items).removeView((View) object);
        }
    }

    private class SaveFileTask extends AsyncTask<Void, Void, String> {
        private final String LOG_TAG = SaveFileTask.class.getSimpleName();

        private void showNotification(String fileName) {
            android.support.v4.app.NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.notification_icon_small)
                            .setContentTitle("Схоронено")
                            .setContentText(fileName);
            File file = new File(Constants.DIRECTORY, fileName);
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file)));
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);

            if (Build.VERSION.SDK_INT >= 24) {
                intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file), type);
            } else {
                intent.setDataAndType(Uri.fromFile(file), type);
            }
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());

            stackBuilder.addNextIntent(intent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(Constants.NOTIFICATIONS_COUNTER, mBuilder.build());

        }

        @Override
        protected void onPostExecute(String fileName) {
            Constants.NOTIFICATIONS_COUNTER++;
            showNotification(fileName);
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.i(LOG_TAG, "Url path " + pathsGeneral.get(pageSelected));

            String path = pathsGeneral.get(pageSelected);
            String fileName = "";
            for (int i = path.length() - 1; i >= 0; i--) {
                if (path.charAt(i) == '/') {
                    fileName = path.substring(i + 1, path.length());
                    break;
                }
            }
            Log.i(LOG_TAG, "fileName " + fileName);
            try {
                URL url = new URL(pathsGeneral.get(pageSelected));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                File save = new File(Constants.DIRECTORY, fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(save);
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;
                while ((read = bufferedInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, read);
                }
                //fileOutputStream.write(bufferedInputStream.read());
                fileOutputStream.flush();
                fileOutputStream.close();
                bufferedInputStream.close();

                return fileName;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

