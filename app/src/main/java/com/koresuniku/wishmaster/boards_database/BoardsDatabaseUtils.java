package com.koresuniku.wishmaster.boards_database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.koresuniku.wishmaster.activities.MainActivity;
import com.koresuniku.wishmaster.utilities.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BoardsDatabaseUtils {

    public static void initializeBoards(MainActivity activity) {
        // Log.v(LOG_TAG, "Inside startUploadingBoardsToAdapter()");

        List<String> adultBoardsL = new ArrayList<>();
        List<String> gamesBoardsL = new ArrayList<>();
        List<String> politicsBoardsL = new ArrayList<>();
        List<String> usersBoardsL = new ArrayList<>();
        List<String> differentBoardsL = new ArrayList<>();
        List<String> creativityBoardsL = new ArrayList<>();
        List<String> subjectsBoardsL = new ArrayList<>();
        List<String> techBoardsL = new ArrayList<>();
        List<String> japaneseBoardsL = new ArrayList<>();

        DataBaseHelper dbManager = new DataBaseHelper(activity);
        // Log.v(LOG_TAG, "Database is there with version: " + dbManager.getReadableDatabase().getVersion());

        SQLiteDatabase dbase = dbManager.getReadableDatabase();
        //Log.i(LOG_TAG, "db is not null " + (dbase != null));

        //Log.i(LOG_TAG, dbase.getPath());
        Cursor cursor = dbase.query(
                BoardsContract.BoardsEntry.TABLE_NAME,
                activity.projection, null, null, null, null, null
        );

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(BoardsContract.BoardsEntry.COLUMN_SUBJECT);
            String subject = cursor.getString(index);

            for (int i = 0; i < Constants.SUBJECTS.length; i++) {
                if (subject.equals(Constants.SUBJECTS[i])) {
                    int indexDesc = cursor.getColumnIndex(BoardsContract.BoardsEntry.COLUMN_DESC);
                    String desc = cursor.getString(indexDesc);
                    switch (i) {
                        case 0:
                            adultBoardsL.add(desc);
                            break;
                        case 1:
                            gamesBoardsL.add(desc);
                            break;
                        case 2:
                            politicsBoardsL.add(desc);
                            break;
                        case 3:
                            usersBoardsL.add(desc);
                            break;
                        case 4:
                            differentBoardsL.add(desc);
                            break;
                        case 5:
                            creativityBoardsL.add(desc);
                            break;
                        case 6:
                            subjectsBoardsL.add(desc);
                            break;
                        case 7:
                            techBoardsL.add(desc);
                            break;
                        case 8:
                            japaneseBoardsL.add(desc);
                            break;
                    }
                }
            }
        }

        activity.adultBoards = activity.formatList(adultBoardsL);
        activity.gamesBoards = activity.formatList(gamesBoardsL);
        activity.politicsBoards = activity.formatList(politicsBoardsL);
        activity.usersBoards = activity.formatList(usersBoardsL);
        activity.differentBoards = activity.formatList(differentBoardsL);
        activity.creativityBoards = activity.formatList(creativityBoardsL);
        activity.subjectsBoards = activity.formatList(subjectsBoardsL);
        activity.techBoards = activity.formatList(techBoardsL);
        activity.japaneseBoards = activity.formatList(japaneseBoardsL);

//        getFragmentManager().beginTransaction()
//                .add(R.id.fragment_container, new ExpandedListViewFragment())
//                .commit();

        dbManager.close();
        dbase.close();
        cursor.close();

    }

    public class WriteToBoardsDatabaseTask extends AsyncTask<Void, Void, ArrayList<String[]>> {
        private String[] subjects;
        private String[] ids;
        private String[] descs;
        private String[] projection = {
                BoardsContract.BoardsEntry.COLUMN_SUBJECT,
                BoardsContract.BoardsEntry.COLUMN_ID,
                BoardsContract.BoardsEntry.COLUMN_DESC
        };

        private Context mContext;


        BoardsHelper boardsHelper;
        SQLiteDatabase database;

        public WriteToBoardsDatabaseTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected ArrayList<String[]> doInBackground(Void... voids) {
            try {
                URL url = new URL("https://2ch.hk/makaba/mobile.fcgi?task=get_boards");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

                String rawJSON = builder.toString();

                formatJSON(rawJSON);

                ArrayList<String[]> arrayList = new ArrayList<>();
                arrayList.add(subjects);
                arrayList.add(ids);
                arrayList.add(descs);

                return arrayList;
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
        protected void onPostExecute(ArrayList<String[]> strings) {
            boardsHelper = new BoardsHelper(mContext);
            database = boardsHelper.getWritableDatabase();

            String[] localSubjects = subjects;
            String[] localIds = ids;
            String[] localDescs = descs;

            for (int i = 0; i < localIds.length; i++) {
                ContentValues values = new ContentValues();

                values.put(BoardsContract.BoardsEntry.COLUMN_SUBJECT, localSubjects[i]);
                values.put(BoardsContract.BoardsEntry.COLUMN_ID, localIds[i]);
                values.put(BoardsContract.BoardsEntry.COLUMN_DESC, localDescs[i]);

                long rowId = database.insert(
                        BoardsContract.BoardsEntry.TABLE_NAME,
                        null,
                        values
                );
            }
            localSubjects = null;
            localIds = null;
            localDescs = null;
            //initializeBoards();


            //checkDatabase();
        }

        private void checkDatabase() {
            Cursor cursor = database.query(
                    BoardsContract.BoardsEntry.TABLE_NAME,
                    projection, null, null, null, null, null
            );

            Log.v("Boards count", String.valueOf(cursor.getCount()));
        }

        private void formatJSON(String rawJSON) throws JSONException {
            JSONObject main = new JSONObject(rawJSON);
            List<String> subjectsList = new ArrayList<>();
            List<String> idsList = new ArrayList<>();
            List<String> descsList = new ArrayList<>();

            JSONArray adult = main.getJSONArray(BoardsContract.BoardsEntry.ADULT_STRING);
            for (int i = 0; i < adult.length(); i++) {
                JSONObject index = adult.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.ADULT_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray games = main.getJSONArray(BoardsContract.BoardsEntry.GAMES_STRING);

            for (int i = 0; i < games.length(); i++) {
                JSONObject index = games.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.GAMES_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray politics = main.getJSONArray(BoardsContract.BoardsEntry.POLITICS_STRING);
            for (int i = 0; i < politics.length(); i++) {
                JSONObject index = politics.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.POLITICS_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray users = main.getJSONArray(BoardsContract.BoardsEntry.USERS_STRING);
            for (int i = 0; i < users.length(); i++) {
                JSONObject index = users.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.USERS_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray different = main.getJSONArray(BoardsContract.BoardsEntry.DIFFERENT_STRING);
            for (int i = 0; i < different.length(); i++) {
                JSONObject index = different.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.DIFFERENT_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray creativity = main.getJSONArray(BoardsContract.BoardsEntry.CREATIVITY_STRING);
            for (int i = 0; i < creativity.length(); i++) {
                JSONObject index = creativity.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.CREATIVITY_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray subjectsA = main.getJSONArray(BoardsContract.BoardsEntry.SUBJECTS_STRING);
            for (int i = 0; i < subjectsA.length(); i++) {
                JSONObject index = subjectsA.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.SUBJECTS_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray tech = main.getJSONArray(BoardsContract.BoardsEntry.TECH_STRING);
            for (int i = 0; i < tech.length(); i++) {
                JSONObject index = tech.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.TECH_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            JSONArray japanese = main.getJSONArray(BoardsContract.BoardsEntry.JAPANESE_STRING);
            for (int i = 0; i < japanese.length(); i++) {
                JSONObject index = japanese.getJSONObject(i);
                subjectsList.add(BoardsContract.BoardsEntry.JAPANESE_STRING);

                String id = index.getString("id");
                idsList.add(id);

                String desc = index.getString("name");
                descsList.add(desc);
            }

            //Log.v("My tag", subjectsList.toString());
            subjects = new String[subjectsList.size()];

            for (int i = 0; i < subjects.length; i++) {
                subjects[i] = subjectsList.get(i);
            }


            ids = new String[idsList.size()];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = idsList.get(i);
            }

            descs = new String[descsList.size()];
            for (int i = 0; i < descs.length; i++) {
                descs[i] = descsList.get(i);
            }


        }
    }
}
