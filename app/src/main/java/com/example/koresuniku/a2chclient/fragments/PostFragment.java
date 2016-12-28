package com.example.koresuniku.a2chclient.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.example.koresuniku.a2chclient.utilities.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static com.example.koresuniku.a2chclient.activities.SingleThreadActivity.deleteDir;

public class PostFragment extends android.support.v4.app.Fragment {
    private final static String LOG_TAG = PostFragment.class.getSimpleName();
    private static final int PICKFILE_RESULT_CODE = 1;

    private Context mContext;
    private SingleThreadActivity mActivity;
    private ThreadsActivity mThreadsActivity;
    private String mAnswer;
    private boolean createNewThread;

    private EditText postEditText;
    private LinearLayout filesLayout;
    private ImageView captchaImage;
    private EditText emailEditText;
    private EditText captchaAnswerEditText;
    private CheckBox sageCheckbox;
    private MenuItem actionSend;
    private MenuItem actionAttach;
    private LinearLayout captchaImageContainer;
    private LinearLayout filesContainer;
    private FrameLayout timeoutContainer;

    private String captchaId;
    private static String postingPath;
    private String email;
    private String comment;
    private String image;
    private String captchaAnswer;

    private View mRootView;

    public PostFragment(Context context, SingleThreadActivity activity, String answer) {

        mContext = context;
        mActivity = activity;
        mAnswer = Constants.LINK_TO_ANSWER;
    }

    public PostFragment(Context context) {
        mContext = context;
        Constants.POSTING_FRAGMENT_IS_OPENED = true;
    }

    public PostFragment(Context context, boolean newThread, ThreadsActivity threadsActivity, String answer) {
        createNewThread = newThread;
        mContext = context;
        mThreadsActivity = threadsActivity;
        mAnswer = answer;
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchCaptchaImage fci = new FetchCaptchaImage();
        fci.execute();

        filesContainer = (LinearLayout) mRootView.findViewById(R.id.files_row);
        Log.i(LOG_TAG, "filescontainer");
        if (Constants.FILES_TO_ATTACH != null) {
            //filesLayout.setVisibility(View.VISIBLE);
            if (filesContainer.getChildCount() > 0) {
                filesContainer.removeAllViews();
            }
            ArrayList<String> fileTexts = new ArrayList<>();

            for (String path : Constants.FILES_TO_ATTACH) {
                try {
                    File file = new File(path);
                    Log.i(LOG_TAG, "name " + file.getName());
                    fileTexts.add(file.getName());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            for (String textSingle : fileTexts) {
                View view = getLayoutInflater(null).inflate(R.layout.file_item, null, false);
                final TextView fileTextView = (TextView) view.findViewById(R.id.file_text);
                ImageView imageCloseButton = (ImageView) view.findViewById(R.id.close_image_button);
                fileTextView.setText(textSingle);
                imageCloseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String fileName = fileTextView.getText().toString();
                        Log.i(LOG_TAG, "onclick");
                        Log.i(LOG_TAG, "filename " + fileName);
                        for (int i = 0; i < Constants.FILES_TO_ATTACH.size(); i++) {
                            Log.i(LOG_TAG, "seeking for " +
                                    Constants.FILES_TO_ATTACH.get(i).substring(
                                            Constants.FILES_TO_ATTACH.get(i).length() - fileName.length(),
                                            Constants.FILES_TO_ATTACH.get(i).length()));
                            if (Constants.FILES_TO_ATTACH.get(i).substring(
                                    Constants.FILES_TO_ATTACH.get(i).length() - fileName.length(),
                                    Constants.FILES_TO_ATTACH.get(i).length()).equals(fileName)) {
                                Log.i(LOG_TAG, "Found file to delete " + i);
                                Constants.FILES_TO_ATTACH.remove(i);
                                filesContainer.removeViewAt(i);
                            }
                        }
                    }
                });
                filesContainer.addView(view);
            }
        }
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        Log.i(LOG_TAG, "Inside onCreateView()");
        final View rootView = inflater.inflate(R.layout.posting, container, false);
        mRootView = rootView;

        postingPath = "https://2ch.hk/makaba/posting.fcgi";

        //filesLayout = (LinearLayout) rootView.findViewById(R.id.files_row);
        //filesLayout.setVisibility(View.GONE);
        postEditText = (EditText) rootView.findViewById(R.id.edit_post);
        if (Constants.POSTING_COMMENT != null) {
            postEditText.setText(Constants.POSTING_COMMENT);
        }
        postEditText.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        captchaImage = (ImageView) rootView.findViewById(R.id.captcha_image);
        if (Constants.POSTING_CAPTCHA_IMAGE != null) {
            captchaImage.setBackground(Constants.POSTING_CAPTCHA_IMAGE);
        }

        emailEditText = (EditText) rootView.findViewById(R.id.edit_options);
        if (Constants.POSTING_EMAIL != null) {
            emailEditText.setText(Constants.POSTING_EMAIL);
        }

        captchaAnswerEditText = (EditText) rootView.findViewById(R.id.edit_captcha_answer);
        if (Constants.POSTING_CAPTCHA_ANSWER != null) {
            captchaAnswerEditText.setText(Constants.POSTING_CAPTCHA_ANSWER);
        }



        captchaImageContainer = (LinearLayout) rootView.findViewById(R.id.captcha_image_container);
        if (mActivity == null) {
            actionSend = ThreadsActivity.mMenu.findItem(R.id.action_send);
            actionAttach = ThreadsActivity.mMenu.findItem(R.id.action_attach);
        } else {
            actionSend = mActivity.mMenu.findItem(R.id.action_send);
            actionAttach = mActivity.mMenu.findItem(R.id.action_attach);
        }
        final CaptchaTimeoutTask[] ctt = {new CaptchaTimeoutTask()};

        ctt[0].execute();

        if (mAnswer != null) {
            comment = mAnswer;
            postEditText.setText(">>" + comment + "\n");
        }

        sageCheckbox = (CheckBox) rootView.findViewById(R.id.sage_checkbox);
        sageCheckbox.setChecked(Constants.POSTING_IS_SAGE);

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

                timeoutContainer.setVisibility(View.GONE);
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

        actionAttach.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.i(LOG_TAG, "Action attach called");

                captchaImage.setImageDrawable(null);

                Constants.POSTING_EMAIL = emailEditText.getText().toString();
                if (emailEditText.getText().equals("")) Constants.POSTING_EMAIL = null;

                Constants.POSTING_COMMENT = postEditText.getText().toString();
                if (postEditText.getText().equals("")) Constants.POSTING_COMMENT = null;

                Constants.POSTING_IS_SAGE = sageCheckbox.isChecked();

                Constants.POSTING_CAPTCHA_IMAGE = captchaImage.getDrawable();

                Constants.POSTING_CAPTCHA_ID = captchaId;
                if (captchaId.equals(null)) Constants.POSTING_CAPTCHA_ID = null;
                else if (captchaId.equals("")) Constants.POSTING_CAPTCHA_ID = null;

                Constants.POSTING_CAPTCHA_ANSWER = captchaAnswerEditText.getText().toString();
                if (captchaAnswerEditText.getText().equals(""))
                    Constants.POSTING_CAPTCHA_ANSWER = null;

                Constants.POSTING_FRAGMENT_IS_OPENED = true;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.setType("*/*");
                getActivity().startActivityForResult(intent, PICKFILE_RESULT_CODE);

                return true;
            }
        });

        timeoutContainer = (FrameLayout) rootView.findViewById(R.id.captcha_timeout_container);

        return rootView;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_send: {
                Log.i(LOG_TAG, "Just before send post on server");
                sendPostToServer();
                break;
            }
            case R.id.action_attach: {

                break;
            }
        }
        return true;
    }

    private void sendPostToServer() {
        SendPostToServer spts = new SendPostToServer();
        if (mActivity != null) {
            SingleThreadActivity.postingFragmentAvailable = false;
        }
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
                            timeoutContainer.setVisibility(View.VISIBLE);
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

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost(postingPath);
                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                reqEntity.addPart("json", new StringBody("1"));
                reqEntity.addPart("task", new StringBody("post"));
                if (mActivity == null) {
                    reqEntity.addPart("board", new StringBody(ThreadsActivity.intentBoard));
                    reqEntity.addPart("thread", new StringBody("0"));
                } else {
                    reqEntity.addPart("board", new StringBody(SingleThreadActivity.intentBoard));
                    reqEntity.addPart("thread", new StringBody(SingleThreadActivity.intentThreadNumber));
                }
                if (!email.equals("")) {
                    if (email.equals("sage")) {
                        reqEntity.addPart("email", new StringBody("sage"));
                    } else {
                        Log.i(LOG_TAG, "else sage");
                        reqEntity.addPart("email", new StringBody(email));
                    }
                }

                Charset chars = Charset.forName("UTF-8");
                StringBody stringBody = new StringBody(comment, chars);
                reqEntity.addPart("comment", stringBody);
                reqEntity.addPart("captcha_type", new StringBody("2chaptcha"));
                reqEntity.addPart("2chaptcha_id", new StringBody(captchaId));
                reqEntity.addPart("2chaptcha_value", new StringBody(captchaAnswer));

                if (Constants.FILES_TO_ATTACH != null) {
                    for (String path : Constants.FILES_TO_ATTACH) {
                        //ContentResolver cR = getActivity().getApplicationContext().getContentResolver();
                        //MimeTypeMap mime = MimeTypeMap.getSingleton();
                        //String type = mime.getExtensionFromMimeType(cR.getType(Uri.parse(path)));
                        //type = cR.getType(Uri.parse(path));

                        String type = "";
                        for (int i = 0; i < path.length(); i++) {
                            if (path.substring(i, i + 1).equals(".")) {
                                type = path.substring(i + 1, path.length());
                            }
                        }

                        if (type.equals("webm")) {
                            File video = new File(path);
                            int size = (int) video.length();
                            byte[] bytes = new byte[size];
                            try {
                                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(video));
                                buf.read(bytes, 0, bytes.length);
                                buf.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            reqEntity.addPart("video", new FileBody(video));
                        } else {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            Log.i(LOG_TAG, "type " + type);
                            int index = Constants.FILES_TO_ATTACH.indexOf(path);
                            if (type.equals("png")) {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                        getActivity().getContentResolver(), Uri.fromFile(new File(path)));
                                bitmap.compress(Bitmap.CompressFormat.PNG, 75, bos);
                                String name = Constants.FILES_NAMES_TO_ATTACH.get(index);
                                reqEntity.addPart("image", new ByteArrayBody(bos.toByteArray(), name));
                            } else if (type.equals("jpeg") || type.equals("jpg")) {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                        getActivity().getContentResolver(), Uri.fromFile(new File(path)));
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
                                String name = Constants.FILES_NAMES_TO_ATTACH.get(index);
                                reqEntity.addPart("image", new ByteArrayBody(bos.toByteArray(), name));
                            } else if (type.equals("gif")) {
                                File gif = new File(path);
                                int size = (int) gif.length();
                                byte[] bytes = new byte[size];
                                try {
                                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(gif));
                                    buf.read(bytes, 0, bytes.length);
                                    buf.close();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                String name = Constants.FILES_NAMES_TO_ATTACH.get(index);
                                reqEntity.addPart(name, new FileBody(gif));
                            } else {
                                Toast t = Toast.makeText(getActivity(), "Непостабельный формат", Toast.LENGTH_SHORT);
                                t.show();
                            }

                        }
                    }
                }

                Constants.FILES_TO_ATTACH = new ArrayList<>();
                //Log.i(LOG_TAG, String.valueOf(reqEntity.getContentLength()));
                postRequest.setEntity(reqEntity);
                HttpResponse response = httpClient.execute(postRequest);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String sResponse;
                StringBuilder s = new StringBuilder();
                while ((sResponse = reader.readLine()) != null) {
                    s = s.append(sResponse);
                }

                Log.i(LOG_TAG, "New response " + s.toString());

                if (mActivity != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(LOG_TAG, "Inside run()");
                            mActivity.closePostingFragment();
                            mActivity.refreshThread();

                        }
                    });
                }

                if (createNewThread) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mThreadsActivity.closePostingFragment();
                            //mThreadsActivity.runNewThreadTask();
                            Intent intent = new Intent(mThreadsActivity, ThreadsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.putExtra(Constants.BOARD, mThreadsActivity.intentBoard);
                            intent.putExtra(Constants.PAGE, "0");
                            startActivity(intent);
                        }
                    });
                }

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