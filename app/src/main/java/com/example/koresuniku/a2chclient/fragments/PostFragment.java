package com.example.koresuniku.a2chclient.fragments;


import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.koresuniku.a2chclient.R;
import com.example.koresuniku.a2chclient.activities.SingleThreadActivity;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static com.example.koresuniku.a2chclient.activities.SingleThreadActivity.fragmentCotainer;
import static com.example.koresuniku.a2chclient.activities.SingleThreadActivity.mMenu;
import static com.example.koresuniku.a2chclient.activities.SingleThreadActivity.pf;

public class PostFragment extends Fragment {
    private final static String LOG_TAG = PostFragment.class.getSimpleName();

    private Context mContext;
    private SingleThreadActivity mActivity;

    private EditText postEditText;
    private LinearLayout filesLayout;
    private ImageView captchaImage;
    private EditText emailEditText;
    private EditText nameEditText;
    private EditText subjectEditText;
    private EditText captchaAnswerEditText;
    private Button sendButton;

    private String captchaId;
    private static String postingPath;
    private String email;
    private String name;
    private String subject;
    private String comment;
    private String image;
    private String captchaAnswer;

    public PostFragment(Context context, SingleThreadActivity activity) {
        mContext = context;
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOG_TAG, "Inside onCreateView()");
        View rootView = inflater.inflate(R.layout.posting, container, false);

        postingPath = "https://2ch.hk/makaba/posting.fcgi";

        filesLayout = (LinearLayout) rootView.findViewById(R.id.files_row);
        filesLayout.setVisibility(View.GONE);
        postEditText = (EditText) rootView.findViewById(R.id.edit_post);
        captchaImage = (ImageView) rootView.findViewById(R.id.captcha_image);
        emailEditText = (EditText) rootView.findViewById(R.id.edit_options);
        nameEditText = (EditText) rootView.findViewById(R.id.edit_name);
        subjectEditText = (EditText) rootView.findViewById(R.id.edit_subject);
        captchaAnswerEditText = (EditText) rootView.findViewById(R.id.edit_captcha_answer);
        sendButton = (Button) rootView.findViewById(R.id.send_button);

        sendButton.setOnClickListener(sendButtonClickListener);



        FetchCaptchaImage fci = new FetchCaptchaImage();
        fci.execute();

        return rootView;
    }

    private View.OnClickListener sendButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SendPostToServer spts = new SendPostToServer();
            spts.execute();
        }
    };
    private class FetchCaptchaImage extends AsyncTask<Void, Void, URL> {

        @Override
        protected URL doInBackground(Void... voids) {
            try {
                URL getCaptchaImageId = new URL("https://2ch.hk/api/captcha/2chaptcha/id?board="
                        + SingleThreadActivity.intentBoard
                        + "&thread="
                        + SingleThreadActivity.intentThreadNumber);
                HttpURLConnection connection = (HttpURLConnection) getCaptchaImageId.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) { builder.append(line); }

                JSONObject getId = new JSONObject(builder.toString());
                captchaId = getId.getString("id");

                String getImage = "https://2ch.hk/api/captcha/2chaptcha/image/";
                URL preparedUrl = new URL(getImage + captchaId);

                return preparedUrl;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(URL url) {
            Glide.with(getActivity()).load(url).placeholder(R.drawable.load_2).into(captchaImage);
        }
    }

    private class SendPostToServer extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            email = emailEditText.getText().toString();
            name = nameEditText.getText().toString();
            subject = subjectEditText.getText().toString();
            comment = postEditText.getText().toString();
            captchaAnswer = captchaAnswerEditText.getText().toString();

            StringBuilder sb = new StringBuilder();
            sb.append("json=1&task=post&board="
                    + SingleThreadActivity.intentBoard
                    + "&thread="
                    + SingleThreadActivity.intentThreadNumber);
            if (!email.equals("")) sb.append("&email=" + email);
            if (!name.equals("")) sb.append("&name=" + name);
            if (!subject.equals("")) sb.append("&subject=" + subject);
            //sb.append("&comment=" + comment);
            sb.append("&comment=" + "%D0%9F%D1%80%D0%B8%D0%B2%D0%B5%D1%82");
            sb.append("&captcha_type=2chaptcha&2chaptcha_id=" + captchaId);
            sb.append("&2chaptcha_value=" + captchaAnswer);

            Log.i(LOG_TAG, sb.toString());
            String send = sb.toString();
            try {
                URL sendUrl = new URL(postingPath);
                HttpURLConnection httpURLConnection = (HttpURLConnection) sendUrl.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type","multipart/form-data; charset=UTF-8");
                httpURLConnection.setRequestProperty("Accept-Language", "ru-Ru,ru;0.5");
                httpURLConnection.setDoOutput(true);
                DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                dataOutputStream.writeBytes(send);
                dataOutputStream.flush();
                dataOutputStream.close();


                Log.v("Response Code", String.valueOf(httpURLConnection.getResponseCode()));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(LOG_TAG, "Inside run()");
                        mActivity.closePostingFragment();
                        mActivity.refreshThread();
                    }
                });
                //runOnUiThread.run();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}