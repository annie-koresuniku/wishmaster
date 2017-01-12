package com.koresuniku.wishmaster.asynktasks;


import android.os.AsyncTask;
import android.text.SpannableStringBuilder;

import java.util.ArrayList;

public class AfterHundredCommentsLoadTask extends AsyncTask<Void, Void, SpannableStringBuilder> {
    SpannableStringBuilder mSSB;
    ArrayList<String> mAnswersArrayList;

    public AfterHundredCommentsLoadTask(SpannableStringBuilder ssb, ArrayList<String> answersArrayList) {
        mSSB = ssb;
        mAnswersArrayList = answersArrayList;
    }

    @Override
    protected SpannableStringBuilder doInBackground(Void... voids) {
        for (int index = 100; index < mAnswersArrayList.size(); index++) {
            mSSB.append("<a href=\"/#" + mAnswersArrayList.get(index) + "\">>>");
            mSSB.append(mAnswersArrayList.get(index));
            mSSB.append("\t");
            mSSB.append("</a>");
        }
        return mSSB;
    }

    @Override
    protected void onPostExecute(SpannableStringBuilder spannableStringBuilder) {

    }
}
