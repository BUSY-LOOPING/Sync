package com.sync.imusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataBaseHelperPlaylistNames extends SQLiteOpenHelper {
    private final String TABLE_NAME = "PlaylistNames";
    public static final String STREAM_DB_NAME = "stream.db";

    public DataBaseHelperPlaylistNames(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME +" (playlistName text, numberOfMedia Integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + TABLE_NAME, null);
    }

    public void add(String playlistName, int no_media) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("playlistName", playlistName);
        contentValues.put("numberOfMedia", no_media);
        db.insert(TABLE_NAME, null, contentValues);
    }

    public void delete(String playlistName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, " playlistName = ? ", new String[]{playlistName});
    }
}
