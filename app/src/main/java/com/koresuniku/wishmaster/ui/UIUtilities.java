package com.koresuniku.wishmaster.ui;


import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.view.WindowManager;

public class UIUtilities {
    public static void showActionBar(ActionBar ac) {
        ac.show();
    }

    public static void hideActionBar(ActionBar ac) {
        ac.hide();
    }

    public static void setStatusBarTranslucent(Activity activity, boolean makeTranslucent) {
        if (makeTranslucent) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
