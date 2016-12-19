package com.example.koresuniku.a2chclient.activities;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.MovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.koresuniku.a2chclient.R;
import com.example.koresuniku.a2chclient.fragments.PostFragment;
import com.example.koresuniku.a2chclient.utilities.CommentTagHandler;
import com.example.koresuniku.a2chclient.utilities.Constants;
import com.example.koresuniku.a2chclient.utilities.CustomLinkMovementMethod;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.koresuniku.a2chclient.utilities.Constants.BOARD;
import static com.example.koresuniku.a2chclient.utilities.Constants.PAGE;

public class ThreadsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String LOG_TAG = ThreadsActivity.class.getSimpleName();
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

    private ArrayList<ArrayList<Map<String, String>>> threadsList = new ArrayList<>();
    public static ArrayList<String> unformattedComments = new ArrayList<>();
    public static ArrayList<String> unformattedPageComments = new ArrayList<>();
    public static Map<Integer, String> formattedTextGeneral;
    public static ArrayList<String> formattedTextsGeneral = new ArrayList<>();

    private ListView mThreadsListView;
    private LayoutInflater mLayoutInflater;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private FrameLayout frameLayoutInner;
    private MenuItem mPageIndex;
    private FrameLayout postingFragmentContainer;
    private PostFragment pf;
    public static Menu mMenu;

    private static int position;

    public static Animation fallingUp;
    public static Animation fallingDown;

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        intentBoard = getIntent().getStringExtra(BOARD);
        intentPage = getIntent().getStringExtra(PAGE);

        postingFragmentAvailable = false;

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        mLayoutInflater = getLayoutInflater();
        mThreadsListView = (ListView) findViewById(R.id.threads_listview);
        mThreadsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                position = mThreadsListView.getPositionForView(view);
                // Log.i(LOG_TAG, "On click position " + String.valueOf(position));
                ArrayList<Map<String, String>> threadPageClicked = threadsList.get(chosenPage);
                Map<String, String> itemThreadClicked = threadPageClicked.get(i);
                String threadNumber = itemThreadClicked.get(Constants.NUMBER);

                Intent intent = new Intent(getApplicationContext(), SingleThreadActivity.class);
                intent.putExtra(Constants.NUMBER, threadNumber);
                intent.putExtra(Constants.BOARD, intentBoard);
                //Log.i(LOG_TAG, "Chosen Page " + chosenPage);
                intent.putExtra(Constants.PAGE, String.valueOf(chosenPage));
                Constants.FROM_SINGLE_THREAD = false;
                startActivity(intent);
            }
        });
        postingFragmentContainer = (FrameLayout) findViewById(R.id.posting_fragment_container);
        fallingUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.falling_up);
        fallingDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.falling_down);
        pf = new PostFragment(getApplicationContext());
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.i(LOG_TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.SPOILERS_LOCALIZATIONS_FOR_THREADS_ACTIVITY = new HashMap<Integer, ArrayList<String>>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");

        Constants.SPOILERS_LOCALIZATIONS = new HashMap<>();
        formattedTextGeneral = new HashMap<>();
        setContentView(R.layout.threads_layout_container);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        frameLayoutInner = (FrameLayout) findViewById(R.id.threads_layout_containe_inner);
        frameLayoutInner.setVisibility(View.GONE);
        position = 0;
        GetPagesOnBoardTask pagesOnBoardTask = new GetPagesOnBoardTask();
        pagesOnBoardTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.threads_menu, menu);
        mPageIndex = menu.findItem(R.id.page_index);
        mPageIndex.setTitle(intentPage);
        MenuItem actionSend = menu.findItem(R.id.action_send);
        actionSend.setVisible(false);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.back_item: {
                if (chosenPage != 0) {
                    chosenPage--;
                    ThreadsAdapter adapter = new ThreadsAdapter(getApplicationContext(), threadsList.get(chosenPage));
                    mThreadsListView.setAdapter(adapter);
                    Log.v(LOG_TAG, "Back item pressed, go to page " + chosenPage);
                    mPageIndex.setTitle(String.valueOf(chosenPage));
                } break;
            }
            case R.id.forward_item: {
                int pagesOnBoardL = Integer.parseInt(pagesOnBoard);
                if (chosenPage <= pagesOnBoardL - 3) {
                    chosenPage++;
                    Log.i(LOG_TAG, "clicked to page " + chosenPage);

                    ThreadsTask tt = new ThreadsTask(getApplicationContext());
                    tt.execute();


                } break;
            }
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

    private void actionWrite() {
        postingFragmentAvailable = true;
        MenuItem actionSend = mMenu.findItem(R.id.action_send);
        actionSend.setVisible(true);
        MenuItem actionWrite = mMenu.findItem(R.id.action_write);
        actionWrite.setVisible(false);
        MenuItem backItem = mMenu.findItem(R.id.back_item);
        backItem.setVisible(false);
        MenuItem pageIndex = mMenu.findItem(R.id.page_index);
        pageIndex.setVisible(false);
        MenuItem forwardItem = mMenu.findItem(R.id.forward_item);
        forwardItem.setVisible(false);
        Log.i(LOG_TAG, "postingFragmentContainre " + (postingFragmentContainer != null));
        postingFragmentContainer.setAnimation(fallingUp);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.posting_fragment_container, pf)
                .commit();
        postingFragmentContainer.startAnimation(fallingUp);
        Log.i(LOG_TAG, "Post animation");
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
                MenuItem backItem = mMenu.findItem(R.id.back_item);
                backItem.setVisible(true);
                MenuItem pageIndex = mMenu.findItem(R.id.page_index);
                pageIndex.setVisible(true);
                MenuItem forwardItem = mMenu.findItem(R.id.forward_item);
                forwardItem.setVisible(true);
                //fragmentCotainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ////fragmentCotainer.setOnClickListener(null);
    }

    @Override
    public void onBackPressed() {
        if (postingFragmentAvailable) {
            closePostingFragment();
            postingFragmentAvailable = false;
            return;
        }
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                Constants.SPOILERS_LOCALIZATIONS_FOR_THREADS_ACTIVITY = new HashMap<Integer, ArrayList<String>>();
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

    private class ThreadsAdapter extends BaseAdapter {
        private ArrayList<Map<String, String>> threadsList = new ArrayList<>();
        private Context mContext;
        private ViewHolder viewHolder;

        public ThreadsAdapter(Context context, ArrayList<Map<String, String>> list) {
            mContext = context;
            threadsList = list;
            unformattedPageComments = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return threadsList.size();
        }

        @Override
        public Map<String, String> getItem(int i) {
            return threadsList.get(i);
        }

        @Override
        public long getItemId(int i) {
            Map<String, String> item = threadsList.get(i);
            return threadsList.indexOf(item);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final View rootView = mLayoutInflater.inflate(R.layout.thread_item_single_image, viewGroup, false);
            viewHolder = new ViewHolder();

            Map<String, String> item = getItem(i);
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
            rootView.setTag(viewHolder);

            String shortInfo = "(" + size + "Кб, " + width + "x" + height + ")";

            if (duration != null) {
                if (!duration.equals("")) {
                    shortInfo = shortInfo.substring(0, shortInfo.length() - 1);
                    shortInfo += ", " + duration + ")";
                }
            }
            viewHolder.mThreadItemShortInfo.setText(shortInfo);
            if (op.equals("0")) {
                op = "";
            } else {
                op = "<font color=\"#008000\"># OP</font>";
            }
            SpannableStringBuilder builderHeader = new SpannableStringBuilder();
            if (subjectOfThread.equals("")
                    || intentBoard.equals("b") || subjectOfThread.equals(" ") ) {
                subjectOfThread = "";
            } else {
                subjectOfThread ="<b><font color=\"#002249\">" + subjectOfThread + "</font></b>";
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
            if (!(thumb.equals(""))) {
                //Log.i(LOG_TAG, "Path " + path);
                if (path.substring(path.length() - 4, path.length()).equals("webm")) {
                    Picasso.with(mContext).load("https://2ch.hk/" + thumb)
                            .into(viewHolder.mThreadItemImage);

                } else {
                    ImageView webmImageview = (ImageView) rootView.findViewById(R.id.webm_imageview);
                    webmImageview.setVisibility(View.GONE);
                    Picasso.with(mContext).load("https://2ch.hk/" + thumb)
                            .into(viewHolder.mThreadItemImage);
                }
            } else {
                FrameLayout imageContainer =
                        (FrameLayout) rootView.findViewById(R.id.image_item_container);
                imageContainer.setVisibility(View.GONE);

            }
            SpannableStringBuilder builderBody = new SpannableStringBuilder();
            builderBody.append(comment);

            viewHolder.mThreadItemBody.setMovementMethod(CustomLinkMovementMethod.getInstance(
                    getApplicationContext(), true, null, null, i, null
            ));
            viewHolder.mThreadItemBody.setFocusable(false);
            //viewHolder.mThreadItemBody.setClickable(false);
            viewHolder.mThreadItemBody.setLongClickable(false);

            viewHolder.mThreadItemBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    position = mThreadsListView.getPositionForView(view);

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
            CommentTagHandler commentTagHandler = new CommentTagHandler(i, true, viewHolder.mThreadItemBody);
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

            return rootView;
        }

        private SpannableString setSpoilerSpans(int position, SpannableString ss) {
            int preCount = 0;
            for (int i = 0; i < chosenPage; i++) {
                //Map threadsPage = threadsList.get(i);
                //Log.i(LOG_TAG, "threadsPAge " + threadsPage.keySet());
                preCount += threadsList.size();
            }
            Log.i(LOG_TAG, "preCount -- " + preCount);
            ArrayList<String> spoilersArray = Constants.SPOILERS_LOCALIZATIONS_FOR_THREADS_ACTIVITY.get(preCount + position);
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

    private class ViewHolder {
        TextView mThreadItemHeader;
        ImageView mThreadItemImage;
        TextView mThreadItemBody;
        TextView mThreadItemAnswersAndFiles;
        TextView mThreadItemDisplayName;
        TextView mThreadItemShortInfo;
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
            pagesOnBoard = s;
            if (pagesOnBoard != null) {
                Log.v(LOG_TAG, "Pages on board " + pagesOnBoard);
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

    private class ThreadsTask extends AsyncTask<Void, Void, ArrayList<ArrayList<Map<String, String>>>> {
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

                //for(int i = 0; i < Integer.parseInt(pagesOnBoard) - 1; i++) {

                    counter++;
                counter = chosenPage;
                    if (counter == 0) {
                        url = new URL("https://2ch.hk/" + intentBoard + "/index" + ".json");
                    } else {
                        url = new URL("https://2ch.hk/" + intentBoard + "/" + counter +".json");
                    }

                    Log.v(LOG_TAG, "Counter " + counter);

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
                        localItem = formatJSON(rawJSON);
                        threadsList.add(localItem);
                    }
                //}
                return threadsList;
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
            Log.i(LOG_TAG, "SPOILERS_LOCALIZATIONS_FOR_THREADS_ACTIVITY " + Constants.SPOILERS_LOCALIZATIONS_FOR_THREADS_ACTIVITY);

                if (startAdapter) {
                   // setContentView(R.layout.threads_layout_container);
                    ThreadsAdapter adapter = new ThreadsAdapter(mContext, threadsList.get(Integer.parseInt(intentPage)));
                    mThreadsListView.setAdapter(adapter);
                    frameLayoutInner.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    setTitle(boardName);
                }

            if (chosenPage != 0) {
                ThreadsAdapter adapter = new ThreadsAdapter(getApplicationContext(), threadsList.get(chosenPage));
                mThreadsListView.setAdapter(adapter);
                Log.v(LOG_TAG, "Forward item pressed, go to page " + chosenPage);
                mPageIndex.setTitle(String.valueOf(chosenPage));
            }
            }
        }

    private void getSpoilers(int position) {
        ArrayList<String> spoilersLocalizations = new ArrayList<>();
        ArrayList<String> spoilers = new ArrayList<>();
        Pattern p = Pattern
                .compile("(<span[^>]+class\\s*=\\s*(\"|')spoiler\\2[^>]*>)[^<]*(</span>)");
        Matcher m = p.matcher(unformattedComments.get(position));
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
        if (spoilers.size() > 0) {
            for (String spoiler : spoilers) {
                Pattern pattern = Pattern.compile(spoiler.replace(")", "\\)").replace("(", "\\("));
                Matcher matcher = pattern.matcher(formattedTextGeneral.get(position));
                int startAt = 0;
                while (matcher.find(startAt)) {
                    Log.i(LOG_TAG, "mathced groups" + matcher.groupCount());
                    int start = matcher.start();
                    int end = matcher.end();
                    Log.i(LOG_TAG, "start " + start + ", end " + end);
                    spoilersLocalizations.add(start + " " + end);
                    startAt = end;
                }
            }
            if (Constants.SPOILERS_LOCALIZATIONS_FOR_THREADS_ACTIVITY.get(position) == null) {
                Constants.SPOILERS_LOCALIZATIONS_FOR_THREADS_ACTIVITY.put(position, spoilersLocalizations);
            }
            if (Constants.SPOILERS_LOCALIZATIONS_FOR_THREADS_ACTIVITY.get(position).size() == 0) {
                Constants.SPOILERS_LOCALIZATIONS_FOR_THREADS_ACTIVITY.put(position, spoilersLocalizations);
            }
        }
    }

    int counter = 0;
        private ArrayList<Map<String, String>> formatJSON(String rawJSON) {
            ArrayList<Map<String, String>> result = new ArrayList<>();
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

                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                        spannableStringBuilder.append(comment);
                        String commentFormatted = String.valueOf(Html.fromHtml(spannableStringBuilder.toString()));
                        formattedTextGeneral.put(counter, commentFormatted);
                        formattedTextsGeneral.add(commentFormatted);
                        getSpoilers(counter);
                        counter++;

                        result.add(item);
                    }

                    return result;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }



