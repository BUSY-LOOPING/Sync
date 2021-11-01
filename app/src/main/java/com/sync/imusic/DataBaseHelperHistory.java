package com.sync.imusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.io.File;

public class DataBaseHelperHistory extends SQLiteOpenHelper {
    private HistoryAdapter adapter;
    private HistoryDetailsActivityAdapter historyDetailsActivityAdapter;
    public static final String HISTORY_TABLE_NAME = "history";
    private Context context;

    public DataBaseHelperHistory(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
//        check();
//        thread = new Thread(runnable);
//        thread.start();
    }

    public void setAdapter(HistoryAdapter adapter) {
        this.adapter = adapter;
    }

    public void setAdapter(HistoryDetailsActivityAdapter historyDetailsActivityAdapter) {
        this.historyDetailsActivityAdapter = historyDetailsActivityAdapter;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + HISTORY_TABLE_NAME + " (isAudio BOOL, isVideo BOOL, path TEXT, title TEXT, artist TEXT, album TEXT, duration TEXT, id TEXT, size TEXT, filename TEXT, dateAdded TEXT, resolution TEXT, albumArt BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


    public boolean insertData(boolean isAudio, boolean isVideo, String path, String title, String artist, String album, String duration, String id, String size, String filename, String dateAdded, String resolution, byte[] albumArt) {
        exists(isAudio, isVideo, path, title, artist, album, duration, id, size, filename, dateAdded, resolution);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("isAudio", isAudio);
        contentValues.put("isVideo", isVideo);
        contentValues.put("path", path);
        contentValues.put("title", title);
        contentValues.put("artist", artist);
        contentValues.put("album", album);
        contentValues.put("duration", duration);
        contentValues.put("id", id);
        contentValues.put("size", size);
        contentValues.put("filename", filename);
        contentValues.put("dateAdded", dateAdded);
        contentValues.put("resolution", resolution);
        if (albumArt != null)
            contentValues.put("albumArt", albumArt);
        long result = db.insert(HISTORY_TABLE_NAME, null, contentValues);
        db.close();
        if (adapter != null) {
            PlaylistFiles playlistFiles;
            if (isAudio) {
                playlistFiles = new PlaylistFiles(new MusicFiles(path, title, artist, album, duration, id, size), "");
            } else {
                playlistFiles = new PlaylistFiles(new VideoFiles(id, path, title, filename, size, dateAdded, duration, resolution), "");
            }
            adapter.add(playlistFiles);
            if (historyDetailsActivityAdapter != null)
                historyDetailsActivityAdapter.add(playlistFiles);
        }
        return result != -1;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from " + HISTORY_TABLE_NAME, null);
    }

    public byte[] getThumbnail(String id) {
        byte[] array = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * from " + HISTORY_TABLE_NAME + " where id = ?";
        Cursor res = db.rawQuery(query, new String[]{id});
        if (res != null) {
            if (res.moveToFirst())
                array = res.getBlob(12);
            res.close();
        }
        db.close();
        return array;
    }

//    public int getPrevPlayedPos(String id) {
//        int pos = 0;
//        SQLiteDatabase db = this.getReadableDatabase();
//        String query = "SELECT * from " + HISTORY_TABLE_NAME + " where id = ?";
//        Cursor res = db.rawQuery(query, new String[]{id});
//        if (res != null) {
//            if (res.moveToFirst())
//                pos = res.getInt(13);
//            res.close();
//        }
//        db.close();
//        return pos;
//    }

//    public void storePrevPlayedPos(String id, int pos) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("pos", pos);
//        db.update(HISTORY_TABLE_NAME, contentValues, " id = ? ", new String[]{id});
//    }

    private void exists(boolean isAudio, boolean isVideo, String path, String title, String artist, String album, String duration, String id, String size, String filename, String dateAdded, String resolution) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * from " + HISTORY_TABLE_NAME + " where id =? ";
        Cursor cursor = db.rawQuery(query, new String[]{id});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (delete(id) != -1) {
                    if (adapter != null)
                        adapter.remove(id);
                    if (historyDetailsActivityAdapter != null)
                        historyDetailsActivityAdapter.remove(id);
                }
            }
            cursor.close();
        }
        db.close();
    }

    public int delete(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(HISTORY_TABLE_NAME, " id = ? ", new String[]{id});
    }

    public void deleteForId(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(HISTORY_TABLE_NAME, " id = ? ", new String[]{id});
//        db.close();
    }

    public void delete_notify(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(HISTORY_TABLE_NAME, " id = ? ", new String[]{id});
        if (adapter != null) {
            adapter.remove(id);
        }
        if (historyDetailsActivityAdapter != null)
            historyDetailsActivityAdapter.remove(id);
    }


    public void check() {
        Cursor cursor = getAllData();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(2);
                String id = cursor.getString(7);
                if (!new File(path).exists()) {
                    deleteForId(id);
                }
            }
        }
//        checkIfExists checkIfExists = new checkIfExists(cursor);
//        checkIfExists.execute("start");
    }

    private class checkIfExists extends AsyncTask<String, String, String> {
        Cursor cursor;

        public checkIfExists(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        protected String doInBackground(String... strings) {
//            DataBaseHelperPlaylist.this  = new DataBaseHelperPlaylist(context, name, factory, version);
            File dbFile = context.getDatabasePath(HISTORY_TABLE_NAME);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(2);
                    String id = cursor.getString(7);
                    if (!new File(path).exists()) {
                        deleteForId(id);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            cursor.close();
        }
    }


}
