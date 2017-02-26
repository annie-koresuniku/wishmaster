package com.koresuniku.wishmaster.adapters;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.koresuniku.wishmaster.R;
import com.koresuniku.wishmaster.activities.MainActivity;
import com.koresuniku.wishmaster.activities.ThreadsActivity;
import com.koresuniku.wishmaster.boards_database.BoardsContract;
import com.koresuniku.wishmaster.boards_database.DataBaseHelper;
import com.koresuniku.wishmaster.utilities.Constants;

import java.util.ArrayList;

import static com.koresuniku.wishmaster.activities.MainActivity.projection;

public class BoardsExpandableListViewAdapter extends BaseExpandableListAdapter {
    MainActivity activity;

    ArrayList<String> groupData = new ArrayList<>();
    ArrayList<String[]> childData = new ArrayList<>();

    public BoardsExpandableListViewAdapter(MainActivity a, ArrayList<String[]> children) {
        groupData.add(BoardsContract.BoardsEntry.ADULT_STRING);
        groupData.add(BoardsContract.BoardsEntry.GAMES_STRING);
        groupData.add(BoardsContract.BoardsEntry.POLITICS_STRING);
        groupData.add(BoardsContract.BoardsEntry.USERS_STRING);
        groupData.add(BoardsContract.BoardsEntry.DIFFERENT_STRING);
        groupData.add(BoardsContract.BoardsEntry.CREATIVITY_STRING);
        groupData.add(BoardsContract.BoardsEntry.SUBJECTS_STRING);
        groupData.add(BoardsContract.BoardsEntry.TECH_STRING);
        groupData.add(BoardsContract.BoardsEntry.JAPANESE_STRING);

        childData = children;

        activity = a;
    }

    @Override
    public int getGroupCount() {
        return groupData.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return childData.get(i).length;
    }

    @Override
    public Object getGroup(int i) {
        return null;
    }

    @Override
    public Object getChild(int i, int i1) {
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return 0;
    }

    @Override
    public long getChildId(int i, int i1) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        view = activity.getLayoutInflater().inflate(R.layout.group_item, viewGroup, false);

        TextView name = (TextView) view.findViewById(R.id.boards_group_name);
        name.setText(groupData.get(i));

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        view = activity.getLayoutInflater().inflate(R.layout.child_item, viewGroup, false);

        TextView name = (TextView) view.findViewById(R.id.child_board_name);
        final String desc = childData.get(i)[i1];
        name.setText(desc);
        view.setContentDescription(desc);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = desc;

//                Cursor cursor = mBoardsDatabaseReadable.query(
//                        BoardsContract.BoardsEntry.TABLE_NAME,
//                        projection, BoardsContract.BoardsEntry.COLUMN_DESC + " =? ",
//                        new String[] { description }, null, null, null
//                );
                DataBaseHelper dbManager = new DataBaseHelper(activity.getApplicationContext());
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

                Intent intent = new Intent(activity.getApplicationContext(), ThreadsActivity.class);
                Log.i("id ", id);
                intent.putExtra(Constants.BOARD, id);
                intent.putExtra(Constants.PAGE, "0");

                activity.startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}
