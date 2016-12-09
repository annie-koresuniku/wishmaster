package com.example.koresuniku.a2chclient.fragments;


import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.koresuniku.a2chclient.R;
import com.example.koresuniku.a2chclient.activities.SingleThreadActivity;
import com.example.koresuniku.a2chclient.activities.ThreadsActivity;

import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static android.view.KeyEvent.KEYCODE_SHIFT_LEFT;
import static android.view.KeyEvent.KEYCODE_SHIFT_RIGHT;

public class PostFragment extends android.support.v4.app.Fragment {
    private final static String LOG_TAG = PostFragment.class.getSimpleName();

    private Context mContext;
    private SingleThreadActivity mActivity;
    private String mAnswer;

    private EditText postEditText;
    private LinearLayout filesLayout;
    private ImageView captchaImage;
    private EditText emailEditText;
    private EditText captchaAnswerEditText;
    private CheckBox sageCheckbox;
    private MenuItem actionSend;
    private LinearLayout captchaImageContainer;

    private String captchaId;
    private static String postingPath;
    private String email;
    private String comment;
    private String image;
    private String captchaAnswer;

    public PostFragment(Context context, SingleThreadActivity activity, String answer) {

        mContext = context;
        mActivity = activity;
        mAnswer = answer;
    }

    public PostFragment(Context context) {
        mContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        //if (mActivity != null) {
            FetchCaptchaImage fci = new FetchCaptchaImage();
            fci.execute();
        //}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        Log.i(LOG_TAG, "Inside onCreateView()");
        final View rootView = inflater.inflate(R.layout.posting, container, false);

        postingPath = "https://2ch.hk/makaba/posting.fcgi";

        filesLayout = (LinearLayout) rootView.findViewById(R.id.files_row);
        filesLayout.setVisibility(View.GONE);
        postEditText = (EditText) rootView.findViewById(R.id.edit_post);
        postEditText.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);


        captchaImage = (ImageView) rootView.findViewById(R.id.captcha_image);
        emailEditText = (EditText) rootView.findViewById(R.id.edit_options);
        captchaAnswerEditText = (EditText) rootView.findViewById(R.id.edit_captcha_answer);
        captchaImageContainer = (LinearLayout) rootView.findViewById(R.id.captcha_image_container);
        if (mActivity == null) {
            actionSend = ThreadsActivity.mMenu.findItem(R.id.action_send);
        } else {
            actionSend = mActivity.mMenu.findItem(R.id.action_send);
        }
        final CaptchaTimeoutTask[] ctt = {new CaptchaTimeoutTask()};
       // if (mActivity != null) {
            ctt[0].execute();
        //}

        if (mAnswer != null) {
            comment = mAnswer;
            postEditText.setText(">>" + comment + "\n");
        }

        sageCheckbox = (CheckBox) rootView.findViewById(R.id.sage_checkbox);
        sageCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    emailEditText.setText("sage");
                } else {
                    emailEditText.setText("");
                }
            }
        });
        captchaImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captchaImage.setImageResource(0);
                FetchCaptchaImage fci = new FetchCaptchaImage();
                fci.execute();
                ctt[0].cancel(true);
                Log.i(LOG_TAG, "Timeout tasl is cancelled " + ctt[0].isCancelled());
                ctt[0] = new CaptchaTimeoutTask();
                ctt[0].execute();
            }
        });

        captchaAnswerEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                    sendPostToServer();
                    return true;
                }
                return false;
            }
        });

        actionSend.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                sendPostToServer();
                return true;
            }
        });

//        if (SingleThreadActivity.intentBoard == null || SingleThreadActivity.intentThreadNumber == null) {
//
//        }
        return rootView;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_send: {
                Log.i(LOG_TAG, "Just before send post on server");
                sendPostToServer();
            }
        }
        return true;
    }

    private void sendPostToServer() {
        SendPostToServer spts = new SendPostToServer();
        spts.execute();
    }

    private class CaptchaTimeoutTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i(LOG_TAG, "Inside CaptchaTimeoutTask");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new CountDownTimer(1000 * 60 * 3, 1) {
                        @Override
                        public void onTick(long l) {}
                        @Override
                        public void onFinish() {
                        TextView timeoutTextView = new TextView(mContext);
                        timeoutTextView.setText("Капча протухла!");
                        timeoutTextView.setTextColor(Color.RED);
                        timeoutTextView.setGravity(Gravity.LEFT);
                        captchaImageContainer.addView(timeoutTextView);
                            onStop();
                        }
                    }.start();
                }
            });
            return null;
        }
    }

    private class FetchCaptchaImage extends AsyncTask<Void, Void, URL> {

        @Override
        protected URL doInBackground(Void... voids) {
            try {
                Log.i(LOG_TAG, "Inside fetchCaptchaImage");
                URL getCaptchaImageId;
                if (mActivity == null) {
                    getCaptchaImageId = new URL("https://2ch.hk/api/captcha/2chaptcha/id?board="
                            + ThreadsActivity.intentBoard);
                } else {
                    getCaptchaImageId = new URL("https://2ch.hk/api/captcha/2chaptcha/id?board="
                            + SingleThreadActivity.intentBoard
                            + "&thread="
                            + SingleThreadActivity.intentThreadNumber);
                }
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

                Log.i(LOG_TAG, "ID " + getCaptchaImageId);
                Log.i(LOG_TAG, "Captcha image " + preparedUrl.toString());

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
            comment = postEditText.getText().toString();
            captchaAnswer = captchaAnswerEditText.getText().toString();

            StringBuilder sb = new StringBuilder();
            if (mActivity == null) {
                sb.append("json=1&task=post&board="
                        + ThreadsActivity.intentBoard
                        + "&thread="
                        + "0");
            } else {
                sb.append("json=1&task=post&board="
                        + SingleThreadActivity.intentBoard
                        + "&thread="
                        + SingleThreadActivity.intentThreadNumber);
                if (!email.equals("")) {
                    if (email.equals("sage")) {
                        sb.append("&email=mailto:sage");
                    } else {
                        sb.append("&email=" + email);
                    }
                }
            }

            sb.append("&comment=" + comment);
            sb.append("&captcha_type=2chaptcha&2chaptcha_id=" + captchaId);
            sb.append("&2chaptcha_value=" + captchaAnswer);

            Log.i(LOG_TAG, sb.toString());
            String send = sb.toString();

            try {
                URL sendUrl = new URL(postingPath);

                HttpURLConnection httpURLConnection = (HttpURLConnection) sendUrl.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type","multipart/form-data");
                httpURLConnection.setDoOutput(true);
                DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                dataOutputStream.write(send.getBytes("UTF-8"));
                dataOutputStream.flush();
                dataOutputStream.close();

                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                Log.i(LOG_TAG, builder.toString());

                Log.v("Response Code", String.valueOf(httpURLConnection.getResponseCode()));

                JSONObject response = new JSONObject(builder.toString());
                String error = response.getString("Error");
                if (!error.equals("null")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(mContext, "Капча невалидна", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });

                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(LOG_TAG, "Inside run()");
                            mActivity.closePostingFragment();
                            mActivity.refreshThread();

                        }
                    });
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}