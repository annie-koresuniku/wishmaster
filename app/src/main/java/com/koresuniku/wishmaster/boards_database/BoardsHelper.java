package com.koresuniku.wishmaster.boards_database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BoardsHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "boards.db";


    public BoardsHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_SQL_TABLE = "CREATE TABLE " + BoardsContract.BoardsEntry.TABLE_NAME
                + " (" + BoardsContract.BoardsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BoardsContract.BoardsEntry.COLUMN_SUBJECT + " TEXT, "
                + BoardsContract.BoardsEntry.COLUMN_ID + " TEXT, "
                + BoardsContract.BoardsEntry.COLUMN_DESC + " TEXT);";
        sqLiteDatabase.execSQL(CREATE_SQL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
