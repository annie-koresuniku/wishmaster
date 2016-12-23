package com.example.koresuniku.a2chclient.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.koresuniku.a2chclient.R;
import com.example.koresuniku.a2chclient.boards_database.DataBaseHelper;
import com.example.koresuniku.a2chclient.fragments.ExpandedListViewFragment;
import com.example.koresuniku.a2chclient.utilities.Constants;
import com.example.koresuniku.a2chclient.boards_database.BoardsContract;
import com.example.koresuniku.a2chclient.boards_database.BoardsHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    public static String[] adultBoards;
    public static String[] gamesBoards;
    public static String[] politicsBoards;
    public static String[] usersBoards;
    public static String[] differentBoards;
    public static String[] creativityBoards;
    public static String[] subjectsBoards;
    public static String[] techBoards;
    public static String[] japaneseBoards;

    private TextView mMainTextView;
    public static ScrollView mScrollView;
    public static TextView mLoadingTextView;
    private LinearLayout mMainLinearLayout;

    private String mBText;

    public static String[] projection = {
            BoardsContract.BoardsEntry._ID,
            BoardsContract.BoardsEntry.COLUMN_SUBJECT,
            BoardsContract.BoardsEntry.COLUMN_ID,
            BoardsContract.BoardsEntry.COLUMN_DESC
    };

    private BoardsHelper mBoardsHelper;


    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.gc();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int jill = Integer.parseInt(sharedPreferences.getString(
                Constants.JILL_STRING, String.valueOf(Constants.JILL_COUNTER)));
        Log.i(LOG_TAG, "int jill " + jill);
        if (jill >= 1) {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                mMainLinearLayout.setBackgroundResource(R.drawable.jill_ver);
            } else {
                mMainLinearLayout.setBackgroundResource(R.drawable.jill_land);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createFolderForContent();

        if (Build.VERSION.SDK_INT >= 23) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Should we show an explanation?
                        if (shouldShowRequestPermissionRationale(
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            // Explain to the user why we need to read the contacts
                        }
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                    return null;
                }


            }.execute();
        }
        mMainLinearLayout = (LinearLayout) findViewById(R.id.activity_main);
        mLoadingTextView = (TextView) findViewById(R.id.boards_loading);
        mMainTextView = (TextView) findViewById(R.id.main_textview);
        mMainTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplication(), ThreadsActivity.class);
                intent.putExtra(Constants.BOARD, "b");
                intent.putExtra(Constants.PAGE, "0");
                startActivity(intent);
            }
        });
        mBoardsHelper = new BoardsHelper(getApplicationContext());
        mScrollView = (ScrollView) findViewById(R.id.scrolview_main);
        mScrollView.setVisibility(View.GONE);

        setupMainTextView();
        try {
            checkDatabaseIfExists();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createFolderForContent() {
        File myDirectory = new File(Environment.getExternalStorageDirectory() + "/Download", "Wishmaster");
        Constants.DIRECTORY = myDirectory;
        if(!myDirectory.exists()) {
            myDirectory.mkdirs();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        final SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(
                Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Код форума...");
        searchView.setMaxWidth(3000);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.equals("Я не джиллопидор") || query.equals("I'm not a jillfaggot")) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Ну ладно", Toast.LENGTH_SHORT);
                    toast.show();

                    mMainLinearLayout.setBackgroundResource(R.color.button_back);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constants.JILL_STRING, "0");
                    Constants.JILL_COUNTER = 0;
                    editor.apply();
                    editor.commit();
                    searchView.setQuery(null, false);
                    searchView.setQueryHint("Код форума...");

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                } else {
                    Intent intent = new Intent(getApplication(), ThreadsActivity.class);
                    intent.putExtra(Constants.BOARD, query);
                    intent.putExtra(Constants.PAGE, "0");
                    startActivity(intent);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void setupMainTextView() {
        mBText = "<font color=\"#FF7000\" href=\"https://2ch.hk/b/index.json\">/Б/</font>";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        //builder.append(" www.google.ua " );
        builder.append(Constants.MAIN_TEXT_FIRST);
        builder.append(mBText);
        builder.append(Constants.MAIN_TEXT_SECOND);

        mMainTextView.setText(Html.fromHtml(builder.toString()), TextView.BufferType.SPANNABLE);
    }

    private void checkDatabaseIfExists() throws IOException {
        Log.v(LOG_TAG, "Inside checkDatabaseIfExists()");

        DataBaseHelper dbManager = new DataBaseHelper(this);
        Log.v(LOG_TAG, "Database is there with version: " + dbManager.getReadableDatabase().getVersion());

        SQLiteDatabase db = dbManager.getReadableDatabase();
        Log.i(LOG_TAG, "db is not null " + (db != null));

        Log.i(LOG_TAG, db.getPath());
        copyDataBase();

        Cursor cursorD = db.rawQuery("SELECT * FROM boards;", null);
        Log.v(LOG_TAG, "Query Result:" + cursorD.getCount());
        cursorD.close();
        db.close();
        dbManager.close();

        initializeBoards();

    }

    private void copyDataBase() throws IOException {
        final String DATABASE_PATH = "/data/data/com.example.koresuniku.a2chclient/databases/";
        final String DATABASE_NAME = "database.db";
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        InputStream myInput = getApplicationContext().getAssets().open(DATABASE_NAME);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myInput.close();
        myOutput.flush();
        myOutput.close();
    }

    private void initializeBoards() {
        Log.v(LOG_TAG, "Inside startUploadingBoardsToAdapter()");

        List<String> adultBoardsL = new ArrayList<>();
        List<String> gamesBoardsL = new ArrayList<>();
        List<String> politicsBoardsL = new ArrayList<>();
        List<String> usersBoardsL = new ArrayList<>();
        List<String> differentBoardsL = new ArrayList<>();
        List<String> creativityBoardsL = new ArrayList<>();
        List<String> subjectsBoardsL = new ArrayList<>();
        List<String> techBoardsL = new ArrayList<>();
        List<String> japaneseBoardsL = new ArrayList<>();

        DataBaseHelper dbManager = new DataBaseHelper(this);
        Log.v(LOG_TAG, "Database is there with version: " + dbManager.getReadableDatabase().getVersion());

        SQLiteDatabase dbase = dbManager.getReadableDatabase();
        Log.i(LOG_TAG, "db is not null " + (dbase != null));

        Log.i(LOG_TAG, dbase.getPath());
        Cursor cursor = dbase.query(
                BoardsContract.BoardsEntry.TABLE_NAME,
                projection, null, null, null, null, null
        );

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(BoardsContract.BoardsEntry.COLUMN_SUBJECT);
            String subject = cursor.getString(index);

            for(int i = 0; i < Constants.SUBJECTS.length; i++) {
                if (subject.equals(Constants.SUBJECTS[i])) {
                    int indexDesc = cursor.getColumnIndex(BoardsContract.BoardsEntry.COLUMN_DESC);
                    String desc = cursor.getString(indexDesc);
                    switch (i) {
                        case 0:
                            adultBoardsL.add(desc);
                            break;
                        case 1:
                            gamesBoardsL.add(desc);
                            break;
                        case 2:
                            politicsBoardsL.add(desc);
                            break;
                        case 3:
                            usersBoardsL.add(desc);
                            break;
                        case 4:
                            differentBoardsL.add(desc);
                            break;
                        case 5:
                            creativityBoardsL.add(desc);
                            break;
                        case 6:
                            subjectsBoardsL.add(desc);
                            break;
                        case 7:
                            techBoardsL.add(desc);
                            break;
                        case 8:
                            japaneseBoardsL.add(desc);
                            break;
                    }
                }
            }
        }

        adultBoards = formatList(adultBoardsL);
        gamesBoards = formatList(gamesBoardsL);
        politicsBoards = formatList(politicsBoardsL);
        usersBoards = formatList(usersBoardsL);
        differentBoards = formatList(differentBoardsL);
        creativityBoards = formatList(creativityBoardsL);
        subjectsBoards = formatList(subjectsBoardsL);
        techBoards = formatList(techBoardsL);
        japaneseBoards = formatList(japaneseBoardsL);

        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new ExpandedListViewFragment())
                .commit();

        dbManager.close();
        dbase.close();
        cursor.close();

    }

    private String[] formatList(List<String> list) {
        String[] result = new String[list.size()];

        for(int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
        }

        return result;
    }

    public class WriteToBoardsDatabaseTask extends AsyncTask<Void, Void, ArrayList<String[]>> {
        private String[] subjects;
        private String[] ids;
        private String[] descs;
        private String[] projection = {
                BoardsContract.BoardsEntry.COLUMN_SUBJECT,
                BoardsContract.BoardsEntry.COLUMN_ID,
                BoardsContract.BoardsEntry.COLUMN_DESC
        };

        private Context mContext;


        BoardsHelper boardsHelper;
        SQLiteDatabase database;

        public WriteToBoardsDatabaseTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected ArrayList<String[]> doInBackground(Void... voids) {
            try {
                URL url = new URL("https://2ch.hk/makaba/mobile.fcgi?task=get_boards");
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

                String rawJSON = builder.toString();

                formatJSON(rawJSON);

                ArrayList<String[]> arrayList = new ArrayList<>();
                arrayList.add(subjects);
                arrayList.add(ids);
                arrayList.add(descs);

                return arrayList;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> strings) {
            boardsHelper = new BoardsHelper(mContext);
            database = boardsHelper.getWritableDatabase();

            String[] localSubjects = subjects;
            String[] localIds = ids;
            String[] localDescs = descs;

            for(int i = 0; i < localIds.length; i++) {
                ContentValues values = new ContentValues();

                values.put(BoardsContract.BoardsEntry.COLUMN_SUBJECT, localSubjects[i]);
                values.put(BoardsContract.BoardsEntry.COLUMN_ID, localIds[i]);
                values.put(BoardsContract.BoardsEntry.COLUMN_DESC, localDescs[i]);

                long rowId = database.insert(
                        BoardsContract.BoardsEntry.TABLE_NAME,
                        null,
                        values
                );
            }
            localSubjects = null;
            localIds = null;
            localDescs = null;
            initializeBoards();


            //checkDatabase();
        }

        private void checkDatabase() {
            Cursor cursor = database.query(
                    BoardsContract.BoardsEntry.TABLE_NAME,
                    projection, null, null, null, null, null
            );

            Log.v("Boards count", String.valueOf(cursor.getCount()));
        }

        private void formatJSON(String rawJSON) throws JSONException {
            JSONObject main = new JSONObject(rawJSON);
            List<String> subjectsList = new ArrayList<>();
            List<String> idsList = new ArrayList<>();
            List<String> descsList = new ArrayList<>();

            JSONArray adult = main.getJSONArray(BoardsContract.BoardsEntry.ADULT_STRING);
            for(int i = 0; i < adult.length(); i++) {
                JSONObject index = adult.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.ADULT_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray games = main.getJSONArray(BoardsContract.BoardsEntry.GAMES_STRING);

            for(int i = 0; i < games.length(); i++) {
                JSONObject index = games.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.GAMES_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray politics = main.getJSONArray(BoardsContract.BoardsEntry.POLITICS_STRING);
            for(int i = 0; i < politics.length(); i++) {
                JSONObject index = politics.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.POLITICS_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray users = main.getJSONArray(BoardsContract.BoardsEntry.USERS_STRING);
            for(int i = 0; i < users.length(); i++) {
                JSONObject index = users.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.USERS_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray different = main.getJSONArray(BoardsContract.BoardsEntry.DIFFERENT_STRING);
            for(int i = 0; i < different.length(); i++) {
                JSONObject index = different.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.DIFFERENT_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray creativity = main.getJSONArray(BoardsContract.BoardsEntry.CREATIVITY_STRING);
            for(int i = 0; i < creativity.length(); i++) {
                JSONObject index = creativity.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.CREATIVITY_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray subjectsA = main.getJSONArray(BoardsContract.BoardsEntry.SUBJECTS_STRING);
            for(int i = 0; i < subjectsA.length(); i++) {
                JSONObject index = subjectsA.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.SUBJECTS_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray tech = main.getJSONArray(BoardsContract.BoardsEntry.TECH_STRING);
            for(int i = 0; i < tech.length(); i++) {
                JSONObject index = tech.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.TECH_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray japanese = main.getJSONArray(BoardsContract.BoardsEntry.JAPANESE_STRING);
            for(int i = 0; i < japanese.length(); i++) {
                JSONObject index = japanese.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.JAPANESE_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            //Log.v("My tag", subjectsList.toString());
            subjects = new String[subjectsList.size()];

            for(int i = 0; i < subjects.length; i++) {
                subjects[i] = subjectsList.get(i);
            }


            ids = new String[idsList.size()];
            for(int i = 0; i < ids.length; i++) {
                ids[i] = idsList.get(i);
            }

            descs = new String[descsList.size()];
            for(int i = 0; i < descs.length; i++) {
                descs[i] = descsList.get(i);
            }


        }
    }
}
