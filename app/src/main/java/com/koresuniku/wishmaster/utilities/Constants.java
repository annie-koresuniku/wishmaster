package com.koresuniku.wishmaster.utilities;


import android.graphics.drawable.Drawable;
import android.view.View;

import com.koresuniku.wishmaster.R;
import com.koresuniku.wishmaster.boards_database.BoardsContract;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String DVACH_AUTHORITY = "https://2ch.hk/";

    public static final String[] SUBJECTS = {
            BoardsContract.BoardsEntry.ADULT_STRING,
            BoardsContract.BoardsEntry.GAMES_STRING,
            BoardsContract.BoardsEntry.POLITICS_STRING,
            BoardsContract.BoardsEntry.USERS_STRING,
            BoardsContract.BoardsEntry.DIFFERENT_STRING,
            BoardsContract.BoardsEntry.CREATIVITY_STRING,
            BoardsContract.BoardsEntry.SUBJECTS_STRING,
            BoardsContract.BoardsEntry.TECH_STRING,
            BoardsContract.BoardsEntry.JAPANESE_STRING
    };

    public static final String BOARD = "board";
    public static final String PAGE = "page";

    public static final int PAGES = -1;
    public static final String JILL_STRING = "jill";
    public static int JILL_COUNTER = 0;
    public static ArrayList<Integer> THREADS_STACK = new ArrayList<>();

    public static final String DATE = "date";
    public static final String NUMBER = "number";
    public static final String THUMB = "thumb";
    public static final String COMMENT = "comment";
    public static final String OP = "op";
    public static final String ANSWERS_COUNT = "answers_count";
    public static final String FILES_COUNT = "files_count";
    public static final String SUBJECT_OF_THREAD = "subject_of_thread";
    public static final String OP_NAME = "op_name";
    public static final String NUMBER_SINGLE_THREAD = "num";
    public static final String PARENT = "parent";
    public static final String COMMENT_UNFORMATTED = "comment_unformatted";
    public static final String DISPLAY_NAME = "displayname";
    public static final String SIZE = "size";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String FULLNAME = "fullname";
    public static final String PATH = "path";
    public static final String DURATION = "duration";
    public static final String FROM_THREAD = "from_thread";
    public static final String EMAIL = "email";
    public static boolean FROM_SINGLE_THREAD = false;

    public static boolean POSTING_FRAGMENT_IS_OPENED = false;
    public static String POSTING_EMAIL;
    public static String POSTING_COMMENT;
    public static String POSTING_CAPTCHA_ID;
    public static String POSTING_CAPTCHA_ANSWER;
    public static boolean POSTING_IS_SAGE = false;
    public static Drawable POSTING_CAPTCHA_IMAGE;
    public static String LINK_TO_ANSWER;
    public static int LISTVIEW_POSITION = 0;
    public static ArrayList<String> FILES_TO_ATTACH = new ArrayList<>();
    public static ArrayList<String> FILES_NAMES_TO_ATTACH = new ArrayList<>();

    public static File DIRECTORY = null;

    public static ArrayList<String> JSON_PAGES = new ArrayList<>();
    //public static int THREADS_ITEMS_LOADED = 0;

    public static Map<String, View> COLLAPSED_THREADS = new HashMap<>();
    public static Map<String, Integer> COLLAPSED_THREADS_POSITIONS = new HashMap<>();

    public static Map<String, View> COLLAPSED_COMMENTS = new HashMap<>();
    public static Map<String, Integer> COLLAPSED_COMMENTS_POSITIONS = new HashMap<>();

    public static Map<Integer, Boolean> SPOILER_POSTS_COUNTER;
    public static Map<Integer, ArrayList<String>> SPOILERS_LOCATIONS = new HashMap<>();
    public static Map<Integer, ArrayList<String>> SPOILERS_LOCATIONS_FOR_THREADS_ACTIVITY = new HashMap<>();
    public static Map<Integer, Integer> HIDDEN_STATE = new HashMap<>();
    public static int ANSWER_NUMBER_OPENED = -1;

    public static Integer NOTIFICATIONS_COUNTER = -1;

    public static final Integer[] ERROR_IMAGES = {
            R.drawable.error,
            R.drawable.error1,
            R.drawable.error2,
            R.drawable.error3,
            R.drawable.error4,
            R.drawable.error6,
            R.drawable.error7,
            R.drawable.error8,
            R.drawable.error9,
            R.drawable.error10,
            R.drawable.error11
    };

    public static View actionBarView;


}
