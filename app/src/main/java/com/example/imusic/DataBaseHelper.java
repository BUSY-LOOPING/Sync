package com.example.imusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.File;

public class DataBaseHelper extends SQLiteOpenHelper {
    private final String TABLE_NAME_1 = "FavTable";

    public DataBaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME_1 + " (Sno Integer PRIMARY KEY AUTOINCREMENT, filename TEXT, path TEXT, absolutePath TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME_1);
        onCreate(db);
    }

    public boolean insertData(String filename, String path, String absolutePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("filename", filename);
        contentValues.put("path", path);
        contentValues.put("absolutePath", absolutePath);
        long result = db.insert(TABLE_NAME_1, null, contentValues);
        db.close();
        return result != -1;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor =db.rawQuery("select * from " + TABLE_NAME_1, null);
        return cursor;
    }

    public Integer deleteData (String absolutePath)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME_1, "absolutePath = ?" , new String[] {absolutePath});
    }

    public Boolean isFavourite(File file){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * from " + TABLE_NAME_1 + " where " + "absolutePath" + " =?";
        Cursor cursor = db.rawQuery(query, new String[]{file.getAbsolutePath()});
        if (cursor.getCount() <= 0)
        {
            cursor.close();
            return false;
        }
        cursor.close();
        db.close();
        return true;
    }
}
