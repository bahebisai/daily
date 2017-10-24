package com.leebai.daily.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by swd1 on 17-10-9.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "daily.db";
    protected static final String TABLE_NAME = "main_info";
    private static final String ID = "_id";
    public static final String TITLE = "tile";
    public static final String CONTENT = "content";
    public static final String ORIGINAL_TEXT = "original_text";
    public static final String TIME_MODIFIED = "time_modified";
    private static final int DATABASE_VERSION = 1;


    DatabaseHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TITLE + " TEXT,"
                + CONTENT + " TEXT,"
                + ORIGINAL_TEXT + " TEXT,"
                + TIME_MODIFIED + " INTEGER"
                + ");");



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
