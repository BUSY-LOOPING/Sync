package com.sync.imusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataBaseStream extends SQLiteOpenHelper {
    private final String STREAM_TABLE = "Stream_history";

    public DataBaseStream(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + STREAM_TABLE + " (url text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + STREAM_TABLE);
        onCreate(db);
    }

    public void add(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("url", url);
        db.insert(STREAM_TABLE, null, contentValues);
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + STREAM_TABLE , null);
    }

    public void delete(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(STREAM_TABLE, " url = ? ", new String[]{url});
    }
}
