package com.koresuniku.wishmaster.boards_database;


import android.provider.BaseColumns;

public class BoardsContract {
    public static final String CONTENT_AUTHORITY = "com.example.koresuniku.a2chclient";
    public static final String CONTENT_URI = "content://" + CONTENT_AUTHORITY;
    public static final String DATABASE_NAME = "boards.db";

    public class BoardsEntry implements BaseColumns {
        public static final String TABLE_NAME = "boards";
        public static final String _ID = "_id";
        public static final String COLUMN_SUBJECT = "subject";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_DESC = "desc";

        public static final String ADULT_STRING = "Взрослым";
        public static final String GAMES_STRING = "Игры";
        public static final String POLITICS_STRING = "Политика";
        public static final String USERS_STRING = "Пользовательские";
        public static final String DIFFERENT_STRING = "Разное";
        public static final String CREATIVITY_STRING = "Творчество";
        public static final String SUBJECTS_STRING = "Тематика";
        public static final String TECH_STRING = "Техника и софт";
        public static final String JAPANESE_STRING = "Японская культура";
    }
}
