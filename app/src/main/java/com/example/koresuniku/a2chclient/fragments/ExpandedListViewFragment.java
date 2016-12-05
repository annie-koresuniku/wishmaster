package com.example.koresuniku.a2chclient.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.example.koresuniku.a2chclient.R;
import com.example.koresuniku.a2chclient.activities.MainActivity;
import com.example.koresuniku.a2chclient.activities.ThreadsActivity;
import com.example.koresuniku.a2chclient.utilities.Constants;
import com.example.koresuniku.a2chclient.boards_database.BoardsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.koresuniku.a2chclient.activities.MainActivity.adultBoards;
import static com.example.koresuniku.a2chclient.activities.MainActivity.creativityBoards;
import static com.example.koresuniku.a2chclient.activities.MainActivity.differentBoards;
import static com.example.koresuniku.a2chclient.activities.MainActivity.gamesBoards;
import static com.example.koresuniku.a2chclient.activities.MainActivity.japaneseBoards;
import static com.example.koresuniku.a2chclient.activities.MainActivity.mBoardsDatabaseReadable;
import static com.example.koresuniku.a2chclient.activities.MainActivity.politicsBoards;
import static com.example.koresuniku.a2chclient.activities.MainActivity.projection;
import static com.example.koresuniku.a2chclient.activities.MainActivity.subjectsBoards;
import static com.example.koresuniku.a2chclient.activities.MainActivity.techBoards;
import static com.example.koresuniku.a2chclient.activities.MainActivity.usersBoards;

public class ExpandedListViewFragment extends Fragment {
    ExpandableListView mExpandableListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.expanded_listview_fragment, container, false);
        mExpandableListView = (ExpandableListView) rootView.findViewById(R.id.expandablelistview);

        createAdapterForExpandedListView();
        return rootView;

    }



    int totalChilds = 0;
    private void createAdapterForExpandedListView() {
        ArrayList<Map<String, String>> groupData;
        ArrayList<Map<String, String>> childDataItem;
        ArrayList<ArrayList<Map<String, String>>> childData;
        Map<String, String> map;

        groupData = new ArrayList<>();
        for(String group : Constants.SUBJECTS) {
            //Log.v(LOG_TAG, group);
            map = new HashMap<>();
            map.put("groupName", group);
            groupData.add(map);
        }

        //Log.v(LOG_TAG, groupData.toString());
        String groupFrom[] = new String[] { "groupName" };
        int groupTo[] = new int[] { android.R.id.text1 };

        childData = new ArrayList<>();

        childDataItem = new ArrayList<>();
        for(String element : adultBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for(String element : gamesBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for(String element : politicsBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for(String element : usersBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for(String element : differentBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for(String element : creativityBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for(String element : subjectsBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for(String element : techBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        childDataItem = new ArrayList<>();
        for(String element : japaneseBoards) {
            map = new HashMap<>();
            map.put("boardName", element);
            childDataItem.add(map);
        }
        childData.add(childDataItem);

        String[] childFrom = new String[] { "boardName" };
        int[] childTo = new int[] { R.id.board_item };

        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                getActivity(),
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
        MainActivity.mScrollView.setSmoothScrollingEnabled(true);
        MainActivity.mLoadingTextView.setVisibility(View.GONE);

        MainActivity.mScrollView.fullScroll(ScrollView.FOCUS_UP);
        MainActivity.mScrollView.smoothScrollTo(0, 0);
//        MainActivity.mScrollView.scrollTo(mScrollView.getTop(), mScrollView.getTop());
        setListViewHeightBasedOnChildren(mExpandableListView, 0);
        MainActivity.mScrollView.setVisibility(View.VISIBLE);
        //Log.v(LOG_TAG, String.valueOf(adapter.getGroupCount()));


        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {

                if(expandableListView.isGroupExpanded(i)) {
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
                totalChilds += childs;
                setListViewHeightBasedOnChildren(mExpandableListView, totalChilds);
                Log.v("OnExpand", String.valueOf(totalChilds));

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
                totalChilds = totalChilds - childs;
                setListViewHeightBasedOnChildren(mExpandableListView, totalChilds);
                Log.v("OnCollapse", String.valueOf(totalChilds));
            }
        });

        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                TextView descriptionTextView = (TextView) view.findViewById(R.id.board_item);
                String description = descriptionTextView.getText().toString();

                Cursor cursor = mBoardsDatabaseReadable.query(
                        BoardsContract.BoardsEntry.TABLE_NAME,
                        projection, BoardsContract.BoardsEntry.COLUMN_DESC + " =? ",
                        new String[] { description }, null, null, null
                );

                Log.v("OnChildClick", "Cursor found " + cursor.getCount() + " coincidences");

                cursor.moveToFirst();
                int index = cursor.getColumnIndex(BoardsContract.BoardsEntry.COLUMN_ID);
                String id = cursor.getString(index);
                cursor.close();

                Intent intent = new Intent(getActivity(), ThreadsActivity.class);
                intent.putExtra(Constants.BOARD, id);
                intent.putExtra(Constants.PAGE, "0");

                startActivity(intent);
                return true;
            }
        });

    }

    public static void setListViewHeightBasedOnChildren(ListView listView, int childs) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight * 3 + (listView.getDividerHeight() * (listAdapter.getCount() - 1)) - 9;

        params.height -= childs * 9;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}