package com.koresuniku.wishmaster.files_utils;


import android.os.Environment;

import com.koresuniku.wishmaster.utilities.Constants;

import java.io.File;

public class CommonFilesHandle {

    public static void createFolderForContent() {
        File myDirectory = new File(Environment.getExternalStorageDirectory() + "/Download", "Wishmaster");
        Constants.DIRECTORY = myDirectory;
        if (!myDirectory.exists()) {
            myDirectory.mkdirs();
        }
    }
}
