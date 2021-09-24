package com.example.imusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.io.File;

public class DataBaseHelperPlaylist extends SQLiteOpenHelper {
    private final String TABLE_NAME = "PlaylistTable";
    private Context context;
    private String name;
    private SQLiteDatabase.CursorFactory factory;
    private int version;


    public DataBaseHelperPlaylist(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
        this.name = name;
        this.factory = factory;
        this.version = version;
//        check();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (Sno Integer PRIMARY KEY AUTOINCREMENT, isAudio BOOL, isVideo BOOL, path TEXT, title TEXT, artist TEXT, album TEXT, duration TEXT, id TEXT, size TEXT, filename TEXT, dateAdded TEXT, resolution TEXT, playlistName TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(boolean isAudio, boolean isVideo, String path, String title, String artist, String album, String duration, String id, String size, String filename, String dateAdded, String resolution, String playlistName) {
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
        contentValues.put("playlistName", playlistName);
        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return result != -1;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE_NAME, null);
    }

    public void deleteVideoFile(VideoFiles videoFiles) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, " id = ? ", new String[]{videoFiles.getId()});
        db.close();
    }

    public void deleteMusicFile(MusicFiles musicFiles) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, " id = ? ", new String[]{musicFiles.getId()});
        db.close();
    }

    public void deleteForId(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, " id = ? ", new String[]{id});
//        db.close();
    }

    public void deleteForPlaylistName(String playlistName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, " playlistName = ? ", new String[]{playlistName});
//        db.close();
    }

    public void check() {
        Cursor cursor = getAllData();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(3);
                String id = cursor.getString(8);
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
            File dbFile = context.getDatabasePath(TABLE_NAME);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(3);
                    String id = cursor.getString(8);
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
