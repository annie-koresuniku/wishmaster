package com.example.koresuniku.a2chclient.utilities;


import com.example.koresuniku.a2chclient.R;
import com.example.koresuniku.a2chclient.boards_database.BoardsContract;

import java.io.File;
import java.util.ArrayList;

public class Constants {
    public static final String MAIN_TEXT_FIRST = "Два.ч - это система форумов, где можно общаться быстро и свободно, где любая точка зрения имеет право на жизнь.\n" +
            "        Здесь нет регистрации и подписываться не нужно, хотя это не избавляет вас от необходимости соблюдать правила.\n" +
            "        Все форумы (кроме ";
    public static final String MAIN_TEXT_SECOND = "реда), а их список находится снизу, имеют собственную чётко ограниченную тематику.\n" +
            "        Словом, всё, что не запрещено правилами отдельно взятого форума и относится к его тематике, на этом форуме разрешено. ";

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
    public static final String LISTVIEW_POSITION = "listview_position";
    public static final String FULLNAME = "fullname";
    public static final String PATH = "path";
    public static final String DURATION = "duration";
    public static final String FROM_THREAD = "from_thread";
    public static final String EMAIL = "email";

    public static File DIRECTORY = null;


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

}
