package com.koresuniku.wishmaster.utilities;


import com.koresuniku.wishmaster.activities.MainActivity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseUtilities {
    public static void copyDataBase(MainActivity activity) throws IOException {
        final String DATABASE_PATH = "/data/data/com.koresuniku.wishmaster/databases/";
        final String DATABASE_NAME = "database.db";
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        InputStream myInput = activity.getApplicationContext().getAssets().open(DATABASE_NAME);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myInput.close();
        myOutput.flush();
        myOutput.close();
    }
}
