package com.leebai.daily;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.leebai.daily.adapter.NoteViewAdapter;
import com.leebai.daily.database.DatabaseHelper;
import com.leebai.daily.database.NotesProvider;
import com.leebai.daily.decoration.ListDividerItemDecoration;

import java.util.List;


/**
 * Created by swd1 on 17-10-1.
 */

public class NoteListActivity extends Activity implements View.OnClickListener {

    private ImageButton mAddNote;
    private ImageButton mSettings;
    private ImageButton mViewMode;

    private RecyclerView mRecyclerView;
    private NoteViewAdapter mAdapter;
    ListDividerItemDecoration mListDivider;

    private final int NEW_NOTE = 0;
    private final int EDIT_NOTE = 1;

    //list or grid
    private static boolean mIsList;
    private boolean mIsPortraitOrientation;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mPreferenceEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_note);
        if (mAdapter == null) {
            Cursor cursor = getContentResolver().query(NotesProvider.CONTENT_URI, null, null, null, null);
            mAdapter = new NoteViewAdapter(this, cursor, 0);
        }
        setAdapterClickListener();
        initLoader();

        mSharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        mPreferenceEditor = mSharedPreferences.edit();

        mIsList = mSharedPreferences.getBoolean("view_mode", true);
        mIsPortraitOrientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        mListDivider = new ListDividerItemDecoration(this, ListDividerItemDecoration.LIST_VERTICAL);
        mRecyclerView = findViewById(R.id.recycler_view);
        setLayoutManager();


        mAddNote = findViewById(R.id.add_note);
        mSettings = findViewById(R.id.settings);
        mViewMode = findViewById(R.id.view_mode);

        mAddNote.setOnClickListener(this);
        mSettings.setOnClickListener(this);
        mViewMode.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initLoader() {
        getLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader cursorLoader = new CursorLoader(NoteListActivity.this, NotesProvider.CONTENT_URI, null, null, null, DatabaseHelper.TIME_MODIFIED + " desc");
                return cursorLoader;
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapCursor(null);

            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mAdapter.swapCursor(data);
            }
        });
    }

    @Override
    public void onClick(View view) {

        if (view == mAddNote) {
            Intent intent = new Intent(this, EditNoteActivity.class);
            intent.setFlags(NEW_NOTE);
            startActivity(intent);
        }

        if (view == mViewMode) {
            mIsList = !mIsList;
            mPreferenceEditor.putBoolean("view_mode", mIsList);
            mPreferenceEditor.commit();

            setLayoutManager();
        }

    }

    public static boolean isList() {
        return mIsList;
    }

    public int getGridColumn() {
        if (mIsPortraitOrientation) {
            return 2;
        }else {
            return 3;
        }
    }

    private void setLayoutManager() {
        if (mIsList) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.addItemDecoration(mListDivider);
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, getGridColumn()));
            mRecyclerView.removeItemDecoration(mListDivider);
        }
        if (mAdapter != null) {
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    private void setAdapterClickListener() {
        mAdapter.setOnItemClickListener(new NoteViewAdapter.onItemClickListener() {
            @Override
            public void onItemClicked(View view, int position) {
                startViewItem(view, position);
            }

            @Override
            public void onItemLongClicked(View view, int position) {
                deleteItem(view, position);
            }
        });
    }

    private void startViewItem(View view, int position) {
        Cursor cursor = mAdapter.getCursor();
        String originalText = null;
        if (cursor.moveToPosition(position)) {
            originalText = cursor.getString(cursor.getColumnIndex(DatabaseHelper.ORIGINAL_TEXT));
        }
        Intent intent = new Intent(this, EditNoteActivity.class);
        intent.setFlags(EDIT_NOTE);
        intent.putExtra("original_text", originalText);
        intent.putExtra("id", mAdapter.getItemId(position));
        startActivity(intent);


    }

    private void deleteItem(View view, int position) {
        Toast.makeText(this, "delete position = " + position, Toast.LENGTH_SHORT).show();

        getContentResolver().delete(NotesProvider.CONTENT_URI, "_id=?", new String[]{String.valueOf(mAdapter.getItemId(position))});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
