package com.example.koresuniku.a2chclient.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.koresuniku.a2chclient.R;
import com.example.koresuniku.a2chclient.fragments.PostFragment;
import com.example.koresuniku.a2chclient.utilities.CommentTagHandler;
import com.example.koresuniku.a2chclient.utilities.Constants;
import com.example.koresuniku.a2chclient.utilities.CustomLinkMovementMethod;
import com.example.koresuniku.a2chclient.utilities.FetchPath;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.koresuniku.a2chclient.utilities.CacheReduce.trimCache;
import static com.example.koresuniku.a2chclient.utilities.Constants.BOARD;
import static com.example.koresuniku.a2chclient.utilities.Constants.COLLAPSED_THREADS;
import static com.example.koresuniku.a2chclient.utilities.Constants.JSON_PAGES;
import static com.example.koresuniku.a2chclient.utilities.Constants.PAGE;
import static com.example.koresuniku.a2chclient.utilities.Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY;
import static com.example.koresuniku.a2chclient.utilities.Constants.THREADS_ITEMS_LOADED;

public class ThreadsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String LOG_TAG = ThreadsActivity.class.getSimpleName();
    private static final int PICKFILE_RESULT_CODE = 1;

    public static String intentBoard;
    public static String intentPage;
    private int chosenPage = 0;

    private String boardName;
    private String pagesOnBoard;
    private String defaultOpName;
    private String subjectOfThread;
    private String opName;
    private String date;
    private String number;
    private String thumb;
    private String comment;
    private String op;
    private String answersCount;
    private String filesCount;
    private String size;
    private String width;
    private String height;
    private String fullname;
    private String path;
    private String duration;
    private boolean postingFragmentAvailable;
    private boolean isScrolled = false;
    private boolean firstTimeLoaded = true;

    private ArrayList<Map<String, String>> threadsList = new ArrayList<>();
    private ArrayList<Map<String, String>> temporaryThreadList = new ArrayList<>();
    private ArrayList<String> numbersGeneral = new ArrayList<>();
    public ArrayList<String> unformattedComments = new ArrayList<>();
    public ArrayList<String> unformattedPageComments = new ArrayList<>();
    public Map<Integer, String> formattedTextGeneral;
    public static ArrayList<String> formattedTextsGeneral = new ArrayList<>();
    public ArrayList<View> itemViews = new ArrayList<>();
    private LruCache<String, Bitmap> mMemoryCache;

    private ListView mThreadsListView;
    private ThreadsAdapter adapter;
    private LayoutInflater mLayoutInflater;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private FrameLayout frameLayoutInner;
    private MenuItem mPageIndex;
    private FrameLayout postingFragmentContainer;
    private PostFragment pf;
    public static Menu mMenu;
    private ThreadsActivity thisActivity = this;

    private static int position;

    public static Animation fallingUp;
    public static Animation fallingDown;

    private ProgressBar pb;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        intentBoard = savedInstanceState.getString(BOARD);
        chosenPage = Integer.parseInt(savedInstanceState.getString(PAGE));
        intentPage = String.valueOf(chosenPage);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.BOARD, intentBoard);
        outState.putString(Constants.PAGE, String.valueOf(chosenPage));
        outState.putBoolean("pfAvailable", postingFragmentAvailable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");

        createMemoryCacheForBitmaps();

        postingFragmentAvailable = Constants.POSTING_FRAGMENT_IS_OPENED;

        formattedTextGeneral = new HashMap<>();
        setContentView(R.layout.threads_layout_container);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        frameLayoutInner = (FrameLayout) findViewById(R.id.threads_layout_containe_inner);
        frameLayoutInner.setVisibility(View.GONE);
        position = 0;
        GetPagesOnBoardTask pagesOnBoardTask = new GetPagesOnBoardTask();
        pagesOnBoardTask.execute();

        Log.i(LOG_TAG, "SPOILERS_LOCATIONS_FOR_thRead " + SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY);
    }

    @Override
    protected void onStart() {
        super.onStart();

        intentBoard = getIntent().getStringExtra(BOARD);
        intentPage = getIntent().getStringExtra(PAGE);

        Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY = new HashMap<>();
        Constants.COLLAPSED_THREADS_POSITIONS = new HashMap<>();


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        //mThreadsRecyclerView = (RecyclerView) findViewById(R.id.threads_recycler_view);
        //mThreadsRecyclerView.setHasFixedSize(true);
        //mThreadsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mLayoutInflater = getLayoutInflater();
        mThreadsListView = (ListView) findViewById(R.id.threads_listview);
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
        mThreadsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

                if (!isScrolled) {
                    Log.i(LOG_TAG, "touch scrolled ");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            for (int i = 1; i < Integer.parseInt(pagesOnBoard) - 1; i++) {
                                try {
                                    URL url;
                                    if (i == 0) {
                                        url = new URL("https://2ch.hk/" + intentBoard + "/index" + ".json");
                                    } else {
                                        url = new URL("https://2ch.hk/" + intentBoard + "/" + i + ".json");
                                    }

                                    Log.i(LOG_TAG, "rerceived url " + url);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                    connection.setRequestMethod("GET");
                                    connection.connect();
                                    StringBuilder builder = new StringBuilder();
                                    BufferedReader reader = new BufferedReader(
                                            new InputStreamReader(connection.getInputStream()));
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        builder.append(line);
                                    }
                                    JSON_PAGES.add(builder.toString());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }
                    }.execute();
                }
                isScrolled = true;
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
        postingFragmentContainer = (FrameLayout) findViewById(R.id.posting_fragment_container);
        // postingFragmentContainer.setVisibility(View.VISIBLE);

        fallingUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.falling_up);
        fallingDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.falling_down);
        pf = new PostFragment(getApplicationContext(), true, this, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY = new HashMap<Integer, ArrayList<String>>();
        Constants.THREADS_ITEMS_LOADED = 0;
        Constants.JSON_PAGES = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.threads_menu, menu);
        //mPageIndex = menu.findItem(R.id.page_index);
        //mPageIndex.setTitle(intentPage);
        MenuItem actionSend = menu.findItem(R.id.action_send);
        actionSend.setVisible(false);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
//            case R.id.back_item: {
//                if (chosenPage != 0) {
//                    chosenPage--;
//                    //ThreadsAdapter adapter = new ThreadsAdapter(getApplicationContext(), threadsList.get(chosenPage));
//                    //mThreadsListView.setAdapter(adapter);
//                    Log.v(LOG_TAG, "Back item pressed, go to page " + chosenPage);
//                    mPageIndex.setTitle(String.valueOf(chosenPage));
//                } break;
//            }
//            case R.id.forward_item: {
//                int pagesOnBoardL = Integer.parseInt(pagesOnBoard);
//                if (chosenPage <= pagesOnBoardL - 3) {
//                    chosenPage++;
//                    Log.i(LOG_TAG, "clicked to page " + chosenPage);
//                    //formattedTextGeneral = new HashMap<>();
//
//
//                    //ThreadsTask tt = new ThreadsTask(getApplicationContext());
//                    //tt.execute();
//
//
//                } break;
//            }
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
        if (Constants.POSTING_FRAGMENT_IS_OPENED) {
            closePostingFragment();
            Constants.POSTING_FRAGMENT_IS_OPENED = false;
            postingFragmentAvailable = false;
            return;
        }
        super.onBackPressed();
        JSON_PAGES = new ArrayList<>();
        Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY = null;
        THREADS_ITEMS_LOADED = 0;
        JSON_PAGES = new ArrayList<>();
        formattedTextsGeneral = new ArrayList<>();
        numbersGeneral = new ArrayList<>();
        //itemViews = new ArrayList<>();

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
        super.onStop();
        trimCache(getApplicationContext());
        Log.i(LOG_TAG, "inside onStop()");
        System.gc();
        mMemoryCache.evictAll();
        Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY = new HashMap<>();
        Constants.THREADS_ITEMS_LOADED = 0;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY = new HashMap<Integer, ArrayList<String>>();
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

    public void showPopup(View v) {
        //Log.i(LOG_TAG, "Index of v " + mThreadsListView.indexOfChild(v));
        final String number = String.valueOf(v.getContentDescription());
        Log.i(LOG_TAG, "number " + numbersGeneral.indexOf(number));
        final int position = numbersGeneral.indexOf(number);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
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
        //Constants.COLLAPSED_THREADS_SHORT.put(number, collapsedItemView);
        itemViews.remove(threadPosition);
        itemViews.add(threadPosition, collapsedItemView);

        adapter.notifyDataSetChanged();


        //Log.i(LOG_TAG, "Constants.COLLAPSED_THREADS " + Constants.COLLAPSED_THREADS_SHORT);
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

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private class ThreadsAdapter extends BaseAdapter {
        private ArrayList<Map<String, String>> threadsList = new ArrayList<>();
        private Context mContext;
        private ViewHolder viewHolder;

        public ThreadsAdapter(Context context, ArrayList<Map<String, String>> list) {
            mContext = context;
            threadsList = list;
            unformattedPageComments = new ArrayList<>();
            viewHolder = new ViewHolder();
        }

        @Override
        public int getCount() {
            return threadsList.size();
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
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View rootView = itemViews.get(i);
            view = rootView;
            return view;
        }


        private SpannableString setSpoilerSpans(int position, SpannableString ss) {
            int preCount = 0;
            for (int i = 0; i < chosenPage; i++) {
                //Map threadsPage = threadsList.get(i);
                //Log.i(LOG_TAG, "threadsPAge " + threadsPage.keySet());
                preCount += threadsList.size();
            }
            Log.i(LOG_TAG, "preCount -- " + preCount);
            ArrayList<String> spoilersArray = Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.get(preCount + position);
            if (spoilersArray != null) {
                for (String spoiler : spoilersArray) {
                    String[] locals = spoiler.split(" ");
                    int start = Integer.parseInt(locals[0]);
                    int end = Integer.parseInt(locals[1]);
                    Log.i(LOG_TAG, "BEFORE SETTING SPAN");
                    Log.i(LOG_TAG, "spoiler as it " + spoiler);
                    Log.i(LOG_TAG, "ss to span " + ss.toString());
                    Log.i(LOG_TAG, "ss length " + ss.length());
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

    public class ThreadsRecyclerViewAdapter extends RecyclerView.Adapter<ThreadsRecyclerViewAdapter.ViewHolder> {
        //private String[] mDataSet = {"1", "2", "3"};

        class ViewHolder extends RecyclerView.ViewHolder {
            View mView;
            TextView mCommentText;

            public ViewHolder(View itemView, TextView commentText) {
                super(itemView);
                mView = itemView;
                mCommentText = commentText;
            }
        }

        @Override
        public ThreadsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.thread_item_single_image, parent, false);

            Log.i(LOG_TAG, "onCreateViewHolder()");
            Log.i(LOG_TAG, "viewType " + viewType);
            //Log.i(LOG_TAG, )

            //View convertView = new TextView(getApplicationContext());
            return new ViewHolder(convertView, ((TextView) convertView.findViewById(R.id.thread_item_body)));
        }

        @Override
        public void onBindViewHolder(ThreadsRecyclerViewAdapter.ViewHolder holder, int position) {
            //holder.mTextView.setText(mDataSet[position]);
            Log.i(LOG_TAG, "onBindView()");
            //TextView tv = (TextView) holder.mView.findViewById(R.id.thread_item_body);
            //Log.i(LOG_TAG, "tv text " + tv.getText());
            Log.i(LOG_TAG, "itemView not null " + (itemViews.get(position) != null));
            holder.mView = itemViews.get(position);
            holder.mCommentText.setText("qwerty");


        }

        @Override
        public int getItemCount() {
            return itemViews.size();
        }
    }

    private class ViewHolder {
        TextView mThreadItemHeader;
        ImageView mThreadItemImage;
        TextView mThreadItemBody;
        TextView mThreadItemAnswersAndFiles;
        TextView mThreadItemDisplayName;
        TextView mThreadItemShortInfo;
        ImageView mExpandOptions;
    }

    private class GetPagesOnBoardTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            if (isOnline()) {
                try {
                    URL url = new URL("https://2ch.hk/" + intentBoard + "/index" + ".json");

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    Log.i(LOG_TAG, "getpagesonboard " + url);
                    int responseCode = connection.getResponseCode();
                    Log.v("Response Code ", String.valueOf(responseCode));
                    if (responseCode == 404) {
                        Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                        startActivity(intent);
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
                        Log.i(LOG_TAG, "url " + url);
                        pagesOnBoard = result(rawJSON);

                        url = new URL("https://2ch.hk/" + intentBoard + "/index" + ".json");
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
                        return result(rawJSON);
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
        protected void onPostExecute(String s) {
            if (pagesOnBoard != null) {
                Log.v(LOG_TAG, "Pages on board " + pagesOnBoard);
                Log.i(LOG_TAG, "jsonPages " + JSON_PAGES.size());
                ThreadsTask threadsTask = new ThreadsTask(getApplicationContext());
                threadsTask.execute();


            }
        }

        private String result(String rawJSON) {
            try {
                JSONObject main = new JSONObject(rawJSON);
                JSONArray pagesArray = main.getJSONArray("pages");
                return String.valueOf(pagesArray.length());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class ThreadsTask extends AsyncTask<Void, Void, ArrayList<ArrayList<Map<String, String>>>> {
        private final String LOG_TAG = ThreadsTask.class.getSimpleName();
        private Context mContext;
        private boolean startAdapter = true;
        private ArrayList<Map<String, String>> localItem;

        public ThreadsTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected ArrayList<ArrayList<Map<String, String>>> doInBackground(Void... strings) {
            Log.v(LOG_TAG, "Inside doInBackground()");
            int counter = -1;
            URL url;
            try {

                if (chosenPage == 0) {
                    url = new URL("https://2ch.hk/" + intentBoard + "/index" + ".json");
                } else {
                    url = new URL("https://2ch.hk/" + intentBoard + "/" + chosenPage + ".json");
                }

                Log.v(LOG_TAG, "URl " + url);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.v(LOG_TAG, "Response Code " + connection.getResponseCode());

                if (responseCode == 404) {
                    Intent intent = new Intent(getApplicationContext(), ErrorActivity.class);
                    startActivity(intent);
                } else {
                    StringBuilder builder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    String rawJSON = builder.toString();
                    localItem = formatJSON(JSON_PAGES.get(chosenPage));
                    //threadsList.add(localItem);
                }
                //}
                return null;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<Map<String, String>>> maps) {
            Log.v(LOG_TAG, "Inside onPostExecute() " + intentPage);
            Log.i(LOG_TAG, "SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY " + Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY);
            CreateViewsTask cvt = new CreateViewsTask();
            cvt.execute();

        }

        private void getSpoilers(int position) {
            //Log.i(LOG_TAG, "getSpoilers(), " + position);
            ArrayList<String> spoilersLocations = new ArrayList<>();
            ArrayList<String> spoilers = new ArrayList<>();
            Pattern p = Pattern
                    .compile("(<span[^>]+class\\s*=\\s*(\"|')spoiler\\2[^>]*>)[^<]*(</span>)");
            Matcher m = p.matcher(unformattedComments.get(position));
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

                String commentFormatted = formattedTextsGeneral.get(position);

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
                if (Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.get(position) == null) {
                    Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.put(position, spoilersLocations);
                }
                if (Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.get(position).size() == 0) {
                    Constants.SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY.put(position, spoilersLocations);
                }
            }
        }

        int counter = 0;

        private ArrayList<Map<String, String>> formatJSON(String rawJSON) {
            ArrayList<Map<String, String>> result = new ArrayList<>();
            temporaryThreadList = new ArrayList<>();
            Map<String, String> item;
            try {
                JSONObject main = new JSONObject(rawJSON);
                boardName = main.getString("BoardName");
                defaultOpName = main.getString("default_name");
                JSONArray threadsArray = main.getJSONArray("threads");

                for (int v = 0; v < threadsArray.length(); v++) {
                    JSONObject thread = threadsArray.getJSONObject(v);

                    answersCount = thread.getString("posts_count");
                    number = thread.getString("thread_num");
                    filesCount = thread.getString("files_count");

                    JSONArray postArray = thread.getJSONArray("posts");
                    JSONObject post = postArray.getJSONObject(0);
                    date = post.getString("date");
                    op = post.getString("op");
                    comment = post.getString("comment");
                    subjectOfThread = post.getString("subject");
                    opName = post.getString("name");

                    JSONArray filesArray = post.getJSONArray("files");
                    if (!(filesArray.length() == 0)) {
                        JSONObject file = filesArray.getJSONObject(0);
                        thumb = file.getString("thumbnail");
                        path = file.getString("path");
                        size = file.getString("size");
                        width = file.getString("width");
                        height = file.getString("height");
                        if (path.substring(path.length() - 4, path.length()).equals("webm")) {
                            duration = file.getString("duration");
                        } else {
                            duration = "";
                        }
                    } else {
                        thumb = "";
                    }

                    item = new HashMap<>();
                    item.put(Constants.ANSWERS_COUNT, answersCount);
                    item.put(Constants.NUMBER, number);
                    item.put(Constants.FILES_COUNT, filesCount);
                    item.put(Constants.DATE, date);
                    item.put(Constants.OP, op);
                    item.put(Constants.COMMENT, comment);
                    item.put(Constants.THUMB, thumb);
                    item.put(Constants.SUBJECT_OF_THREAD, subjectOfThread);
                    item.put(Constants.OP_NAME, opName);
                    item.put(Constants.SIZE, size);
                    item.put(Constants.HEIGHT, height);
                    item.put(Constants.WIDTH, width);
                    item.put(Constants.PATH, path);
                    item.put(Constants.DURATION, duration);

                    unformattedComments.add(comment);
                    numbersGeneral.add(number);

                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                    spannableStringBuilder.append(comment);
                    String commentFormatted = String.valueOf(Html.fromHtml(spannableStringBuilder.toString()));
                    formattedTextGeneral.put(counter, commentFormatted);
                    formattedTextsGeneral.add(commentFormatted);
//                    getSpoilers(counter + Constants.THREADS_ITEMS_LOADED);
//                    counter++;

                    threadsList.add(item);
                    temporaryThreadList.add(item);
                }

                if (firstTimeLoaded) {
                    firstTimeLoaded = false;
                } else {
                    Constants.THREADS_ITEMS_LOADED += temporaryThreadList.size();
                }

                for (int i = 0; i < temporaryThreadList.size(); i++) {
                    getSpoilers(i + Constants.THREADS_ITEMS_LOADED);
                }
                Log.i(LOG_TAG, "Constants.THREADS_ITEMS_LOADED " + Constants.THREADS_ITEMS_LOADED);
                Log.i(LOG_TAG, "unformattedComment.size() " + unformattedComments.size());
                return result;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private class CreateViewsTask extends AsyncTask<Void, Void, Void> {
        private final String LOG_TAG = CreateViewsTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... voids) {

            for (int i = 0; i < temporaryThreadList.size(); i++) {
                final ViewHolder viewHolder = new ViewHolder();

                final View rootView = mLayoutInflater.inflate(R.layout.thread_item_single_image, null, false);

//                final int finalI2 = i;
//                rootView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        position = mThreadsListView.getPositionForView(view);
//                        // Log.i(LOG_TAG, "On click position " + String.valueOf(position));
//                        Constants.FILES_TO_ATTACH = new ArrayList<String>();
//                        Constants.FILES_NAMES_TO_ATTACH = new ArrayList<String>();
//                        Map<String, String> itemThreadClicked = threadsList.get(finalI2);
//                        //Map<String, String> itemThreadClicked = threadPageClicked.get(i);
//                        String threadNumber = itemThreadClicked.get(Constants.NUMBER);
//
//                        Intent intent = new Intent(getApplicationContext(), SingleThreadActivity.class);
//                        intent.putExtra(Constants.NUMBER, threadNumber);
//                        intent.putExtra(Constants.BOARD, intentBoard);
//                        //Log.i(LOG_TAG, "Chosen Page " + chosenPage);
//                        intent.putExtra(Constants.PAGE, String.valueOf(chosenPage));
//                        Constants.FROM_SINGLE_THREAD = false;
//                        startActivity(intent);
//                    }
//                });

                //Log.i(LOG_TAG, "threadList size " + threadsList.size());
                //Log.i(LOG_TAG, "chosenPage " + chosenPage);
                Map<String, String> item = temporaryThreadList.get(i);
                //Log.i(LOG_TAG, "arraylist size " + threadsList.get(chosenPage).size());
                String date = item.get(Constants.DATE);
                String number = item.get(Constants.NUMBER);
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

                unformattedPageComments.add(comment);

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
                        showPopup(viewHolder.mExpandOptions);
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
                        || intentBoard.equals("b") || subjectOfThread.equals(" ")) {
                    subjectOfThread = "";
                } else {
                    subjectOfThread = "<b><font color=\"#002249\">" + subjectOfThread + "</font></b>";
                }
                if (opName.equals("")) {
                    opName = defaultOpName;
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
                                    addBitmapToMemoryCache(String.valueOf(finalI1), BitmapFactory.decodeStream(input));
                                } catch (IOException e) {
                                    // Log exception
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Bitmap bitmap) {
//                            Picasso.with(getApplicationContext()).load("https://2ch.hk/" + thumb)
//                                    .into(mThreadItemImage);
                                viewHolder.mThreadItemImage.setImageBitmap(getBitmapFromMemCache(String.valueOf(finalI1)));
                            }
                        }.execute();
                    }
                } else {
                    LinearLayout imageContainer =
                            (LinearLayout) rootView.findViewById(R.id.image_item_container);
                    imageContainer.setVisibility(View.GONE);
                }


                viewHolder.mThreadItemBody.setMovementMethod(CustomLinkMovementMethod.getInstance(
                        getApplicationContext(), true, null, null, i, null
                ));
                viewHolder.mThreadItemBody.setFocusable(false);
                //viewHolder.mThreadItemBody.setClickable(false);
                viewHolder.mThreadItemBody.setLongClickable(false);

                viewHolder.mThreadItemBody.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // position = mThreadsListView.getPositionForView(view);

                        Intent intent = new Intent(getApplicationContext(), SingleThreadActivity.class);
                        intent.putExtra(Constants.NUMBER, rootView.getContentDescription());
                        intent.putExtra(Constants.BOARD, intentBoard);
                        intent.putExtra(Constants.PAGE, String.valueOf(chosenPage));
                        Constants.FROM_SINGLE_THREAD = false;
                        startActivity(intent);
                    }
                });

                //SpannableString ss = new SpannableString(builderBody);
                //ss = setSpoilerSpans(i, ss);
                //Log.i(LOG_TAG, "i + Constants.THREADS_ITEMS_LOADED " + (i + Constants.THREADS_ITEMS_LOADED));
                CommentTagHandler commentTagHandler = new CommentTagHandler(i + Constants.THREADS_ITEMS_LOADED, false, viewHolder.mThreadItemBody);
                viewHolder.mThreadItemBody.setText(Html.fromHtml(builderBody.toString(), null, commentTagHandler), TextView.BufferType.SPANNABLE);
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
                itemViews.add(rootView);
                //itemViews.add(views);

            }
            //itemViews.put(chosenPage, views);
            //itemViews.addAll(views);
            Log.i(LOG_TAG, "itemViews.size() " + itemViews.size());
            Log.i(LOG_TAG, "numbersgeneral " + numbersGeneral.size());

            for (int i = 0; i < itemViews.size(); i++) {
                if (Constants.COLLAPSED_THREADS.containsKey(numbersGeneral.get(i))) {
                    Constants.COLLAPSED_THREADS_POSITIONS.put(numbersGeneral.get(i), i);
                    Log.i(LOG_TAG, "Found collapsed view " + numbersGeneral.get(i));
                    //View viewToReplace = Constants.COLLAPSED_THREADS.get(numbersGeneral.get(i));
                    int threadPosition = i;
                    Log.i(LOG_TAG, "threadPosition " + threadPosition);
                    View collapsedItemView = getLayoutInflater().inflate(R.layout.collapsed_thread, null, false);
                    //collapsedItemView.setContentDescription(number);
                    View itemToSave = itemViews.get(threadPosition);
                    TextView threadNumberTextView = (TextView) collapsedItemView.findViewById(R.id.collapsed_thread_number);
                    TextView threadDescriptionTextView = (TextView) collapsedItemView.findViewById(R.id.collapsed_thread_description);
                    threadNumberTextView.setText("№" + numbersGeneral.get(i));
                    //String desc = String.valueOf(((TextView) itemToSave.findViewById(R.id.thread_item_body)).getText());
                    String desc = formattedTextsGeneral.get(threadPosition);
                    threadDescriptionTextView.setText("(" + desc + ")");

                    collapsedItemView.setContentDescription(numbersGeneral.get(i));
                    itemViews.remove(threadPosition);
                    itemViews.add(threadPosition, collapsedItemView);
                }
            }

            Log.i(LOG_TAG, "Constants.COLLAPSED_THREADS " + COLLAPSED_THREADS.size());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i(LOG_TAG, "chosenPage " + chosenPage);
            if (chosenPage == 0) {
                adapter = new ThreadsAdapter(getApplicationContext(), threadsList);
                //chosenPage++;
                Log.i(LOG_TAG, "threadsList.size() " + threadsList.size());
                mThreadsListView.setAdapter(adapter);
                pb = new ProgressBar(thisActivity, null, android.R.attr.progressBarStyle);
                pb.setContentDescription("footer");
                pb.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View view) {
                        Log.i(LOG_TAG, "onviewattached");
                        chosenPage++;
                        ThreadsTask tt = new ThreadsTask(getApplicationContext());
                        tt.execute();
                    }

                    @Override
                    public void onViewDetachedFromWindow(View view) {
                        Log.i(LOG_TAG, "on view detached");
                    }
                });
                pb.setOnClickListener(null);
                mThreadsListView.addFooterView(pb);
                frameLayoutInner.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                setTitle(boardName);
            } else {
                mThreadsListView.removeFooterView(pb);
                mMemoryCache.evictAll();
                System.gc();
                if (chosenPage >= Integer.parseInt(pagesOnBoard) - 2) {
                    return;
                }
                Log.i(LOG_TAG, "threadsList.size() " + threadsList.size());
                adapter.notifyDataSetChanged();
                if (Integer.parseInt(pagesOnBoard) != chosenPage) {
                    pb = new ProgressBar(thisActivity, null, android.R.attr.progressBarStyle);
                    pb.setContentDescription("footer");
                    pb.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                        @Override
                        public void onViewAttachedToWindow(View view) {
                            Log.i(LOG_TAG, "onviewattached");
                            chosenPage++;
                            ThreadsTask tt = new ThreadsTask(getApplicationContext());
                            tt.execute();
                        }

                        @Override
                        public void onViewDetachedFromWindow(View view) {
                            Log.i(LOG_TAG, "on view detached");
                        }
                    });
                    pb.setOnClickListener(null);
                    mThreadsListView.addFooterView(pb);
                }
            }

            new AsyncTask<Void, Void, Void>() {
                private final String LOG_TAG = "Image fetching task";

                @Override
                protected Void doInBackground(Void... voids) {

                    return null;
                }
            }.execute();
        }
    }
}






