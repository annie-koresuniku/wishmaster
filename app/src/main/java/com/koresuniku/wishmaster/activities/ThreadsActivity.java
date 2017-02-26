package com.koresuniku.wishmaster.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.koresuniku.wishmaster.R;
import com.koresuniku.wishmaster.adapters.MediaFragmentPagerAdapter;
import com.koresuniku.wishmaster.adapters.ThreadsAdapter;
import com.koresuniku.wishmaster.asynktasks.ThreadsActivityAsynktasks;
import com.koresuniku.wishmaster.fragments.PostFragment;
import com.koresuniku.wishmaster.ui.UIUtilities;
import com.koresuniku.wishmaster.utilities.Constants;
import com.koresuniku.wishmaster.utilities.FetchPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.koresuniku.wishmaster.utilities.CacheReduce.trimCache;
import static com.koresuniku.wishmaster.utilities.Constants.BOARD;
import static com.koresuniku.wishmaster.utilities.Constants.COLLAPSED_THREADS;
import static com.koresuniku.wishmaster.utilities.Constants.JSON_PAGES;
import static com.koresuniku.wishmaster.utilities.Constants.PAGE;
import static com.koresuniku.wishmaster.utilities.Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY;


public class ThreadsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, ObservableScrollViewCallbacks {
    private static final String LOG_TAG = ThreadsActivity.class.getSimpleName();
    private static final int PICKFILE_RESULT_CODE = 1;
    public int threadItemsLoaded;

    public String intentBoard;
    public String intentPage;
    public int chosenPage = 0;

    public String boardName;
    public String pagesOnBoard;
    public String defaultOpName;
    public String subjectOfThread;
    public String opName;
    public String date;
    public String number;
    public String thumb;
    public String comment;
    public String op;
    public String answersCount;
    public String filesCount;
    public String size;
    public String width;
    public String height;
    public String fullname;
    public String path;
    public String duration;
    private boolean postingFragmentAvailable;
    public boolean firstTimeLoaded = true;
    public boolean viewPagerOpened = false;

    public ArrayList<Map<String, String>> threadsList = new ArrayList<>();
    public ArrayList<Map<String, String>> temporaryThreadList = new ArrayList<>();
    public ArrayList<String> numbersGeneral = new ArrayList<>();
    public ArrayList<String> unformattedComments = new ArrayList<>();
    public ArrayList<String> unformattedPageComments = new ArrayList<>();
    public Map<Integer, String> formattedTextGeneral;
    public ArrayList<String> formattedTextsGeneral = new ArrayList<>();
    public ArrayList<View> itemViews = new ArrayList<>();
    public Map<String, Map<Integer, String>> pathsToMediaFiles = new HashMap<>();
    public String[] banner = new String[2];

    public LruCache<String, Bitmap> mMemoryCache;


    public ObservableListView mThreadsListView;
    public ThreadsAdapter adapter;
    public LayoutInflater mLayoutInflater;
    private SwipeRefreshLayout swipeRefreshLayout;
    public FrameLayout frameLayoutInner;
    private MenuItem mPageIndex;
    private FrameLayout postingFragmentContainer;
    private PostFragment pf;
    public static Menu mMenu;
    public ThreadsActivity thisActivity = this;

    private static int position;

    public static Animation fallingUp;
    public static Animation fallingDown;

    public ProgressBar pb;
    public FrameLayout loadingImageContainer;
    private android.support.v7.widget.Toolbar toolbar;

    public FrameLayout mainThreadsContainer;
    public ViewPager viewPager;
    public MediaFragmentPagerAdapter mfpa;

    public Map<Integer, ArrayList<String>> spoilersLocations;

    ThreadsActivityAsynktasks.GetPagesOnBoardTask pagesOnBoardTask;

    @SuppressLint("UseSparseArrays")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        Log.i(LOG_TAG, "onCreate()");

        setTitle("");
        setContentView(R.layout.threads_layout_container);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        UIUtilities.setStatusBarTranslucent(this, false);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().isHideOnContentScrollEnabled();
        getSupportActionBar().setShowHideAnimationEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        createMemoryCacheForBitmaps();
        setupProgressBar();
        adapter = new ThreadsAdapter(this, threadsList);
        //pathsToMediaFiles = new HashMap<>();
        pagesOnBoardTask = new ThreadsActivityAsynktasks.GetPagesOnBoardTask(this);
        pagesOnBoardTask.execute();


        postingFragmentAvailable = Constants.POSTING_FRAGMENT_IS_OPENED;

        formattedTextGeneral = new HashMap<>();
        spoilersLocations = new HashMap<>();


        frameLayoutInner = (FrameLayout) findViewById(R.id.threads_layout_containe_inner);
        frameLayoutInner.setVisibility(View.GONE);
        position = 0;

        intentBoard = getIntent().getStringExtra(BOARD);
        intentPage = getIntent().getStringExtra(PAGE);

        Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY = new HashMap<>();
        Constants.COLLAPSED_THREADS_POSITIONS = new HashMap<>();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        mLayoutInflater = getLayoutInflater();
        mThreadsListView = (ObservableListView) findViewById(R.id.threads_listview);
        mThreadsListView.setScrollViewCallbacks(this);
        mThreadsListView.setSmoothScrollbarEnabled(true);

        mThreadsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(LOG_TAG, "adapterView.getContentDescription() " + view.getContentDescription());
                Log.i(LOG_TAG, "COLLAPSED_THREADS " + COLLAPSED_THREADS.keySet());
                String number = numbersGeneral.get(i);
                Log.i(LOG_TAG, "number " + number);
                if (Constants.COLLAPSED_THREADS.containsKey(number)) {
                    Log.i(LOG_TAG, "needToExpand");
                    actionExpandCollapsedView(String.valueOf(number), i);
                } else {
                    position = mThreadsListView.getPositionForView(view);
                    Log.i(LOG_TAG, "On click position " + String.valueOf(position));
                    Constants.FILES_TO_ATTACH = new ArrayList<String>();
                    Constants.FILES_NAMES_TO_ATTACH = new ArrayList<String>();
                    Map<String, String> itemThreadClicked = threadsList.get(i);
                    //Map<String, String> itemThreadClicked = threadPageClicked.get(i);
                    String threadNumber = itemThreadClicked.get(Constants.NUMBER);

                    Log.i(LOG_TAG, "JSON_PAGES " + JSON_PAGES.size());
                    Intent intent = new Intent(getApplicationContext(), SingleThreadActivity.class);
                    intent.putExtra(Constants.NUMBER, threadNumber);
                    intent.putExtra(Constants.BOARD, intentBoard);
                    //Log.i(LOG_TAG, "Chosen Page " + chosenPage);
                    intent.putExtra(Constants.PAGE, String.valueOf(chosenPage));
                    Constants.FROM_SINGLE_THREAD = false;
                    startActivity(intent);
                }
            }
        });


        postingFragmentContainer = (FrameLayout) findViewById(R.id.posting_fragment_container);

        fallingUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.falling_up);
        fallingDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.falling_down);
        pf = new PostFragment(getApplicationContext(), true, this, null);
        //viewPager = (ViewPager) findViewById(R.id.view_pager);
        mainThreadsContainer = (FrameLayout) findViewById(R.id.main_threads_container);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY = new HashMap<Integer, ArrayList<String>>();

        Constants.JSON_PAGES = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.threads_menu, menu);
        MenuItem actionSend = menu.findItem(R.id.action_send);
        actionSend.setVisible(false);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                Log.i(LOG_TAG, "Action home");
                onBackPressed();
                break;
            }
            case R.id.action_write: {
                Log.i(LOG_TAG, "Action new thread");
                actionWrite();
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (viewPagerOpened) {
            //viewPager.setVisibility(View.GONE);
            mainThreadsContainer.removeView(viewPager);
            View decorView = getWindow().getDecorView();
// Hide the status bar.
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            //decorView.setSystemUiVisibility(uiOptions);
            UIUtilities.setStatusBarTranslucent(this, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            }
            //UIUtilities.setStatusBarTranslucent(this, false);
            viewPager = null;
            viewPagerOpened = false;
            return;
        }
        if (Constants.POSTING_FRAGMENT_IS_OPENED) {
            closePostingFragment();
            Constants.POSTING_FRAGMENT_IS_OPENED = false;
            postingFragmentAvailable = false;
            return;
        }
        super.onBackPressed();
        JSON_PAGES = new ArrayList<>();
        Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY = null;

        JSON_PAGES = new ArrayList<>();
        formattedTextsGeneral = new ArrayList<>();
        numbersGeneral = new ArrayList<>();
        //itemViews = new ArrayList<>();

        pagesOnBoard = null;
        intentBoard = null;
        intentPage = null;
        chosenPage = 0;
        unformattedComments = new ArrayList<>();
        unformattedPageComments = new ArrayList<>();
        formattedTextGeneral = new HashMap<>();
        formattedTextsGeneral = new ArrayList<>();
        itemViews = new ArrayList<>();
        Log.i(LOG_TAG, "going back");
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Constants.POSTING_FRAGMENT_IS_OPENED = true;
        Log.i(LOG_TAG, "omActivityResult");
        switch (requestCode) {
            case PICKFILE_RESULT_CODE: {
                if (resultCode == RESULT_OK) {
                    String filePath = FetchPath.getPath(getApplicationContext(), data.getData());
                    Log.i(LOG_TAG, "real filepath " + filePath);
                    Constants.FILES_TO_ATTACH.add(filePath);
                    String name = "";
                    for (int i = filePath.length() - 1; i >= 0; i--) {
                        if (filePath.substring(i, i + 1).equals("/")) {
                            name = filePath.substring(i, filePath.length());
                        }
                    }
                    Log.i(LOG_TAG, "Received name " + name);
                    Constants.FILES_NAMES_TO_ATTACH.add(name);
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        //AppIndex.AppIndexApi.end(client, getIndexApiAction());
        trimCache(getApplicationContext());
        Log.i(LOG_TAG, "inside onStop()");
        System.gc();
        mMemoryCache.evictAll();
        //Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY = new HashMap<>();
        //Constants.THREADS_ITEMS_LOADED = 0;
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client.disconnect();
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY = new HashMap<Integer, ArrayList<String>>();
                JSON_PAGES = new ArrayList<String>();
                swipeRefreshLayout.setRefreshing(true);
                Intent intent = new Intent(getApplicationContext(), ThreadsActivity.class);
                intent.putExtra(BOARD, intentBoard);
                intent.putExtra(PAGE, "0");
                Log.i(LOG_TAG, "Before startActivity");
                startActivity(intent);
                Log.i(LOG_TAG, "After startActivity");
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void setupProgressBar() {
        loadingImageContainer = (FrameLayout) findViewById(R.id.loading_image_container);
        View inflatedLoadingViewToAdd = getLayoutInflater().inflate(R.layout.loading_yoba, null, false);
        ImageView loadingImage = (ImageView) inflatedLoadingViewToAdd.findViewById(R.id.yoba);
        loadingImage.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.loading_yoba));
        loadingImageContainer.addView(inflatedLoadingViewToAdd);
    }

    public View.OnClickListener onThumbnailClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(LOG_TAG, "onThumbnailClickListener()");
            viewPagerOpened = true;
            viewPager = (ViewPager) getLayoutInflater().inflate(R.layout.view_pager_view, null, false);


            boolean isItemAnImage;
            String number = (String) view.getContentDescription();
            //frameLayoutInner.setVisibility(View.GONE);
            //viewPager.setVisibility(View.VISIBLE);
            if (pathsToMediaFiles.get(number).get(0).substring(
                    pathsToMediaFiles.get(number).get(0).length() - 4,
                    pathsToMediaFiles.get(number).get(0).length()).equals("webm")) {
                Log.i(LOG_TAG, "it is webm");
                isItemAnImage = false;
            } else {
                isItemAnImage = true;
            }
            mfpa = new MediaFragmentPagerAdapter(
                    getSupportFragmentManager(),
                    thisActivity,
                    isItemAnImage,
                    number
            );
            viewPager.setAdapter(mfpa);
            mainThreadsContainer.addView(viewPager);

            //Log.i(LOG_TAG, "view contentdescription " + view.getContentDescription());
            //threadThumbChosen = (String) view.getContentDescription();

        }
    };

    public void showPopup(View v) {
        //Log.i(LOG_TAG, "Index of v " + mThreadsListView.indexOfChild(v));
        final String number = String.valueOf(v.getContentDescription());
        Log.i(LOG_TAG, "number " + numbersGeneral.indexOf(number));
        final int position = numbersGeneral.indexOf(number);
        ActionBar actionBar = getSupportActionBar();
        PopupMenu popup = new PopupMenu(actionBar.getThemedContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_options_menu_general_threads, popup.getMenu());
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

    private void actionWrite() {
        postingFragmentContainer.setVisibility(View.VISIBLE);
        Constants.POSTING_FRAGMENT_IS_OPENED = true;
        MenuItem actionSend = mMenu.findItem(R.id.action_send);
        actionSend.setVisible(true);
        MenuItem actionWrite = mMenu.findItem(R.id.action_write);
        actionWrite.setVisible(false);
//        MenuItem backItem = mMenu.findItem(R.id.back_item);
//        backItem.setVisible(false);
//        MenuItem pageIndex = mMenu.findItem(R.id.page_index);
//        pageIndex.setVisible(false);
//        MenuItem forwardItem = mMenu.findItem(R.id.forward_item);
//        forwardItem.setVisible(false);
        MenuItem actionAttach = mMenu.findItem(R.id.action_attach);
        actionAttach.setVisible(true);
        Log.i(LOG_TAG, "postingFragmentContainre " + (postingFragmentContainer != null));
        postingFragmentContainer.setAnimation(fallingUp);
        //postingFragmentContainer.setId(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.posting_fragment_container, pf)
                .commit();
        postingFragmentContainer.startAnimation(fallingUp);
        //postingFragmentContainer.setVisibility(View.VISIBLE);
        Log.i(LOG_TAG, "Post animation");
    }

    private void actionWrite(String number) {
        //Constants.POSTING_COMMENT = number;
        postingFragmentContainer.setVisibility(View.VISIBLE);
        Constants.POSTING_FRAGMENT_IS_OPENED = true;
        MenuItem actionSend = mMenu.findItem(R.id.action_send);
        actionSend.setVisible(true);
        MenuItem actionWrite = mMenu.findItem(R.id.action_write);
        actionWrite.setVisible(false);
//        MenuItem backItem = mMenu.findItem(R.id.back_item);
//        backItem.setVisible(false);
//        MenuItem pageIndex = mMenu.findItem(R.id.page_index);
//        pageIndex.setVisible(false);
//        MenuItem forwardItem = mMenu.findItem(R.id.forward_item);
//        forwardItem.setVisible(false);
        MenuItem actionAttach = mMenu.findItem(R.id.action_attach);
        actionAttach.setVisible(true);
        Log.i(LOG_TAG, "postingFragmentContainre " + (postingFragmentContainer != null));
        postingFragmentContainer.setAnimation(fallingUp);
        //postingFragmentContainer.setId(View.VISIBLE);
        pf = new PostFragment(getApplicationContext(), false, this, number);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.posting_fragment_container, pf)
                .commit();
        postingFragmentContainer.startAnimation(fallingUp);
        //postingFragmentContainer.setVisibility(View.VISIBLE);
        Log.i(LOG_TAG, "Post animation");
    }

    private void actionCollapse(String number) {
        Log.i(LOG_TAG, "inside actionCollapse() " + number);
        Log.i(LOG_TAG, "numbersGeneral " + this.numbersGeneral);
        int threadPosition;
        if (Constants.COLLAPSED_THREADS_POSITIONS.get(number) != null) {
            Log.i(LOG_TAG, "not null");
            threadPosition = Constants.COLLAPSED_THREADS_POSITIONS.get(number);
        } else {
            Log.i(LOG_TAG, "is null");
            threadPosition = numbersGeneral.indexOf(number);
        }
        Log.i(LOG_TAG, "threadPosition " + threadPosition);
        View collapsedItemView = getLayoutInflater().inflate(R.layout.collapsed_thread, null, false);
        View itemToSave = itemViews.get(threadPosition);
        TextView threadNumberTextView = (TextView) collapsedItemView.findViewById(R.id.collapsed_thread_number);
        TextView threadDescriptionTextView = (TextView) collapsedItemView.findViewById(R.id.collapsed_thread_description);
        threadNumberTextView.setText("№" + number);
        String desc = formattedTextsGeneral.get(threadPosition);
        threadDescriptionTextView.setText("(" + desc + ")");

        Constants.COLLAPSED_THREADS.put(number, itemToSave);
        itemViews.remove(threadPosition);
        itemViews.add(threadPosition, collapsedItemView);
        adapter.notifyDataSetChanged();
    }

    private void actionExpandCollapsedView(String number, int i) {
        Log.i(LOG_TAG, "inside actionExpandCollapseView " + number);
        View viewToAdd = Constants.COLLAPSED_THREADS.get(number);
        Log.i(LOG_TAG, "viewToAdd is not null " + (viewToAdd != null));
        int threadPosition = i;
        Log.i(LOG_TAG, "threadPosiyion " + threadPosition);
        ImageView expandOptions = (ImageView) viewToAdd.findViewById(R.id.expand_options);
        expandOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
        itemViews.remove(threadPosition);
        itemViews.add(threadPosition, viewToAdd);
        adapter.notifyDataSetChanged();

        Constants.COLLAPSED_THREADS.remove(number);
    }

    public void closePostingFragment() {

        postingFragmentContainer.setAnimation(fallingDown);
        Log.i(LOG_TAG, "Before start animation falling down");
        postingFragmentContainer.startAnimation(fallingDown);
        fallingDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                getSupportFragmentManager().beginTransaction()
                        .remove(pf)
                        .commit();
                MenuItem actionSend = mMenu.findItem(R.id.action_send);
                actionSend.setVisible(false);
                MenuItem actionWrite = mMenu.findItem(R.id.action_write);
                actionWrite.setVisible(true);
//                MenuItem backItem = mMenu.findItem(R.id.back_item);
//                backItem.setVisible(true);
//                MenuItem pageIndex = mMenu.findItem(R.id.page_index);
//                pageIndex.setVisible(true);
//                MenuItem forwardItem = mMenu.findItem(R.id.forward_item);
                //forwardItem.setVisible(true);
                MenuItem actionAttach = mMenu.findItem(R.id.action_attach);
                actionAttach.setVisible(false);
                postingFragmentContainer.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ////fragmentCotainer.setOnClickListener(null);
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
//            UIUtilities.setStatusBarTranslucent(this, true);

        } else if (scrollState == ScrollState.DOWN) {
            if (!getSupportActionBar().isShowing()) {
                UIUtilities.showActionBar(getSupportActionBar());
            }
            //UIUtilities.setStatusBarTranslucent(this, false);
        }

    }

    public static class ViewHolder {
        public TextView mThreadItemHeader;
        public ImageView mThreadItemImage;
        public TextView mThreadItemBody;
        public TextView mThreadItemAnswersAndFiles;
        public TextView mThreadItemDisplayName;
        public TextView mThreadItemShortInfo;
        public ImageView mExpandOptions;
    }

}






