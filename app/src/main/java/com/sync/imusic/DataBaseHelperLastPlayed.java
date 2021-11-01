package com.sync.imusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DataBaseHelperLastPlayed extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "last_played";

    public DataBaseHelperLastPlayed(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (id TEXT , pos INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


    public boolean insertData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("pos", 0);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public int getPrevPlayedPos(String id) {
        int pos = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * from " + TABLE_NAME + " where id = ?";
        Cursor res = db.rawQuery(query, new String[]{id});
        if (res != null) {
            if (res.moveToFirst())
                pos = res.getInt(1);
            res.close();
        }
        db.close();
        return pos;
    }

    public void storePrevPlayedPos(String id, int pos) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("pos", pos);
        db.update(TABLE_NAME, contentValues, " id = ? ", new String[]{id});
    }


    public void deleteForId(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, " id = ? ", new String[]{id});
    }

    public boolean contains (String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + TABLE_NAME + " where id = " + id;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

}
