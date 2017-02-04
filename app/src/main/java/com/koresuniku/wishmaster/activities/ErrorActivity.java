package com.koresuniku.wishmaster.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import com.koresuniku.wishmaster.R;
import com.koresuniku.wishmaster.utilities.Constants;


import java.util.Date;
import java.util.Random;

public class ErrorActivity extends AppCompatActivity {
    //private String fromSingleThread;
    private static String intentThreadNumber;
    private static String intentBoard;
    private static String intentPage;
    private static String fromThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error_404);

        //fromSingleThread = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        intentThreadNumber = getIntent().getStringExtra(com.koresuniku.wishmaster.utilities.Constants.NUMBER);
        intentBoard = getIntent().getStringExtra(com.koresuniku.wishmaster.utilities.Constants.BOARD);
        intentPage = String.valueOf(getIntent().getStringExtra(com.koresuniku.wishmaster.utilities.Constants.PAGE));
        fromThread = getIntent().getStringExtra(com.koresuniku.wishmaster.utilities.Constants.FROM_THREAD);

        Log.i("Error", "num " + fromThread);
        Log.i("Error", "brd " + intentBoard);
        Log.i("Error", "pg " + intentPage);
        //Log.i("Error", fromSingleThread);
        ImageView errorImage = (ImageView) findViewById(R.id.error_image);

        Random random = new Random(new Date().getTime());
        int randomInt = random.nextInt(com.koresuniku.wishmaster.utilities.Constants.ERROR_IMAGES.length);

        errorImage.setImageResource(Constants.ERROR_IMAGES[randomInt]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                onBackPressed();
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
