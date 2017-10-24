package com.leebai.daily.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import android.text.TextUtils;
import android.util.Log;


/**
 * Created by swd1 on 17-10-9.
 */

public class NotesProvider extends ContentProvider {

    private static final String AUTHORITY = "com.leebai.daily";
    private DatabaseHelper mDBHelper;

    private static final UriMatcher mUriMatcher;
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/main_info");

    private static final int NOTES = 1;
    private static final int ITEM = 2;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, "main_info", NOTES);
        mUriMatcher.addURI(AUTHORITY, "main_info/#", ITEM);
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.d("bai", "insert");
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        if (mUriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("Unknown uri: " + uri.toString());
        }

        ContentValues values;
        if (contentValues != null) {
            values = new ContentValues(contentValues);
        } else {
            values = new ContentValues();
        }

        long rowid = db.insert(DatabaseHelper.TABLE_NAME, null, values);
        Log.d("bai", "rowid = " + rowid);
        if (rowid > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowid);
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
            return newUri;
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] whereArg) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int count;
        switch (mUriMatcher.match(uri)) {
            case NOTES:
                count = db.update(DatabaseHelper.TABLE_NAME, contentValues, where, whereArg);
                break;
            case ITEM:
                String id = uri.getPathSegments().get(1);
                count = db.update(DatabaseHelper.TABLE_NAME, contentValues, "_id=" + id
                        + (!TextUtils.isEmpty(where)?"AND (" + where + ")": ""), whereArg);
                break;
            default:
                throw new IllegalArgumentException("unknow uri: " + uri.toString());
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, projection, selection, selectionArgs,null, null, sortOrder);
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }


    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int count;
        switch (mUriMatcher.match(uri)) {
            case NOTES:
                count = db.delete(DatabaseHelper.TABLE_NAME, where, whereArgs);
                break;

            case ITEM:
                String id = uri.getPathSegments().get(1);
                count = db.delete(DatabaseHelper.TABLE_NAME, "_id" + "=" + id
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
