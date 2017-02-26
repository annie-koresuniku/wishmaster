package com.koresuniku.wishmaster.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.koresuniku.wishmaster.R;
import com.koresuniku.wishmaster.adapters.BoardsExpandableListViewAdapter;
import com.koresuniku.wishmaster.boards_database.BoardsDatabaseUtils;
import com.koresuniku.wishmaster.boards_database.DataBaseHelper;
import com.koresuniku.wishmaster.easter_eggs.CommonEasterEggs;
import com.koresuniku.wishmaster.files_utils.CommonFilesHandle;
import com.koresuniku.wishmaster.ui.UIUtilities;
import com.koresuniku.wishmaster.utilities.Constants;
import com.koresuniku.wishmaster.boards_database.BoardsContract;
import com.koresuniku.wishmaster.utilities.DatabaseUtilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public LinearLayout mMainLinearLayout;

    private ExpandableListView mExpandableListView;
    private TextView mAppNameTextView;

    public static String[] projection = {
            BoardsContract.BoardsEntry._ID,
            BoardsContract.BoardsEntry.COLUMN_SUBJECT,
            BoardsContract.BoardsEntry.COLUMN_ID,
            BoardsContract.BoardsEntry.COLUMN_DESC
    };

    @Override
    protected void onResume() {
        super.onResume();
        System.gc();
    }

    @Override
    protected void onStart() {
        super.onStart();
        CommonEasterEggs.checkIfGill(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Log.i(LOG_TAG, "inside on create");
        CommonFilesHandle.createFolderForContent();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("");


        if (Build.VERSION.SDK_INT >= 23) {
            new AsyncTask<Void, Void, Void>() {
                @SuppressLint("NewApi")
                @Override
                protected Void doInBackground(Void... voids) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                    return null;
                }


            }.execute();
        }

        mExpandableListView = (ExpandableListView) findViewById(R.id.expandablelistview);


        try {
            DatabaseUtilities.copyDataBase(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BoardsDatabaseUtils.initializeBoards(this);
        //createAdapterForExpandedListView();


        ArrayList<String[]> childBoardsToSend = new ArrayList<>();
        childBoardsToSend.add(adultBoards);
        childBoardsToSend.add(gamesBoards);
        childBoardsToSend.add(politicsBoards);
        childBoardsToSend.add(usersBoards);
        childBoardsToSend.add(differentBoards);
        childBoardsToSend.add(creativityBoards);
        childBoardsToSend.add(subjectsBoards);
        childBoardsToSend.add(techBoards);
        childBoardsToSend.add(japaneseBoards);

        BoardsExpandableListViewAdapter belva = new BoardsExpandableListViewAdapter(this, childBoardsToSend);
        mExpandableListView.setAdapter(belva);

        mExpandableListView.setDividerHeight(1);

        mExpandableListView.setGroupIndicator(null);


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

    private void createAdapterForExpandedListView() {
        ArrayList<Map<String, String>> groupData;
        ArrayList<Map<String, String>> childDataItem;
        ArrayList<ArrayList<Map<String, String>>> childData;
        Map<String, String> map;

        groupData = new ArrayList<>();
        for (String group : Constants.SUBJECTS) {
            //Log.v(LOG_TAG, group);
            map = new HashMap<>();
            map.put("groupName", group);
            groupData.add(map);
        }

        Log.i("LOG_TAG", "AdultBoards " + adultBoards);
        //Log.v(LOG_TAG, groupData.toString());
        String groupFrom[] = new String[]{"groupName"};
        int groupTo[] = new int[]{android.R.id.text1};

        childData = new ArrayList<>();

        childDataItem = new ArrayList<>();
        for (String element : adultBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for (String element : gamesBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for (String element : politicsBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for (String element : usersBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for (String element : differentBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for (String element : creativityBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for (String element : subjectsBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for (String element : techBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for (String element : japaneseBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        String[] childFrom = new String[]{"boardName"};
        int[] childTo = new int[]{R.id.board_item};

        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                groupFrom,
                groupTo,
                childData,
                R.layout.boards_listview_layout,
                childFrom,
                childTo
        );

        mExpandableListView.setAdapter(adapter);

        //Log.v(LOG_TAG, String.valueOf(adapter.getGroupCount()));

        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {

                Log.i(LOG_TAG, "childs currently " + expandableListView.getChildCount());


                if (expandableListView.isGroupExpanded(i)) {
                    expandableListView.collapseGroup(i);
                } else {
                    expandableListView.expandGroup(i);
                }
                return true;
            }

        });
        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                int childs = 0;
                switch (i) {
                    case 0:
                        childs = adultBoards.length;
                        break;
                    case 1:
                        childs = gamesBoards.length;
                        break;
                    case 2:
                        childs = politicsBoards.length;
                        break;
                    case 3:
                        childs = usersBoards.length;
                        break;
                    case 4:
                        childs = differentBoards.length;
                        break;
                    case 5:
                        childs = creativityBoards.length;
                        break;
                    case 6:
                        childs = subjectsBoards.length;
                        break;
                    case 7:
                        childs = techBoards.length;
                        break;
                    case 8:
                        childs = japaneseBoards.length;
                        break;
                }

            }
        });

        mExpandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                int childs = 0;
                switch (i) {
                    case 0:
                        childs = adultBoards.length;
                        break;
                    case 1:
                        childs = gamesBoards.length;
                        break;
                    case 2:
                        childs = politicsBoards.length;
                        break;
                    case 3:
                        childs = usersBoards.length;
                        break;
                    case 4:
                        childs = differentBoards.length;
                        break;
                    case 5:
                        childs = creativityBoards.length;
                        break;
                    case 6:
                        childs = subjectsBoards.length;
                        break;
                    case 7:
                        childs = techBoards.length;
                        break;
                    case 8:
                        childs = japaneseBoards.length;
                        break;
                }


            }
        });

        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {


                TextView descriptionTextView = (TextView) view.findViewById(R.id.board_item);
                String description = descriptionTextView.getText().toString();

//                Cursor cursor = mBoardsDatabaseReadable.query(
//                        BoardsContract.BoardsEntry.TABLE_NAME,
//                        projection, BoardsContract.BoardsEntry.COLUMN_DESC + " =? ",
//                        new String[] { description }, null, null, null
//                );
                DataBaseHelper dbManager = new DataBaseHelper(getApplicationContext());
                //Log.v(LOG_TAG,"Database is there with version: "+dbManager.getReadableDatabase().getVersion());

                SQLiteDatabase db = dbManager.getReadableDatabase();
                //Log.i(LOG_TAG, "db is not null " + (db != null));

                //Log.i(LOG_TAG, db.getPath());
                //copyDataBase();
//        Cursor cursorD = db.query(
//                BoardsContract.BoardsEntry.TABLE_NAME,
//                projection, null, null, null, null, null
//        );
                Cursor cursor = db.query(
                        BoardsContract.BoardsEntry.TABLE_NAME,
                        projection, BoardsContract.BoardsEntry.COLUMN_DESC + " =? ",
                        new String[]{description}, null, null, null
                );

                Log.v("OnChildClick", "Cursor found " + cursor.getCount() + " coincidences");

                cursor.moveToFirst();
                int index = cursor.getColumnIndex(BoardsContract.BoardsEntry.COLUMN_ID);
                String id = cursor.getString(index);
                cursor.close();
                ///cursor.close();
                db.close();
                dbManager.close();

                Intent intent = new Intent(getApplicationContext(), ThreadsActivity.class);
                Log.i("id ", id);
                intent.putExtra(Constants.BOARD, id);
                intent.putExtra(Constants.PAGE, "0");

                startActivity(intent);
                return true;
            }
        });

    }

    public String[] formatList(List<String> list) {
        String[] result = new String[list.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
        }

        return result;
    }

    private void setChildsColor() {

    }

}
