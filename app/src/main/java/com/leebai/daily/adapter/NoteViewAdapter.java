package com.leebai.daily.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leebai.daily.NoteListActivity;
import com.leebai.daily.R;
import com.leebai.daily.database.DatabaseHelper;
import com.leebai.daily.exrecyclerview.RecyclerViewCursorAdapter;

import java.text.SimpleDateFormat;

/**
 * Created by swd1 on 17-10-10.
 */

public class NoteViewAdapter extends RecyclerViewCursorAdapter<NoteViewAdapter.MyViewHolder> {
    private Context mContext;

    public NoteViewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.mContext = context;
    }

    @Override
    protected void onContentChanged() {

    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, Cursor cursor) {
        if (holder == null) {
            return;
        }

        if (mOnItemClickListener != null) {
            final int position = holder.getLayoutPosition();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClicked(v, position);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClicked(v, position);
                    return true;
                }
            });
        }

        Log.d("bai", "on bindview cursor 111");

        String title = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TITLE));
        String content = cursor.getString(cursor.getColumnIndex(DatabaseHelper.CONTENT));
        long time_modified = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TIME_MODIFIED));

        if (time_modified == 0) {
            time_modified = System.currentTimeMillis();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(time_modified);

        holder.sTitle.setText(title);
        holder.sBriefContent.setText(content);
        holder.sTimeStamp.setText(time);

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("bai", "on createViewHolder");
        View view;
        if (NoteListActivity.isList()) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_note, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.grid_note, parent, false);
        }
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }


    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView sTitle;
        private TextView sBriefContent;
        private TextView sTimeStamp;

        public MyViewHolder(View view) {
            super(view);

            sTitle = view.findViewById(R.id.title);
            sBriefContent = view.findViewById(R.id.brief_info);
            sTimeStamp = view.findViewById(R.id.time_stamp);

//            view.setBackgroundResource(R.drawable.item_click_bg);
        }

    }

    private onItemClickListener mOnItemClickListener;

    public interface onItemClickListener {
        void onItemClicked(View view, int position);

        void onItemLongClicked(View view, int position);
    }

    public void setOnItemClickListener(onItemClickListener listener) {
        mOnItemClickListener = listener;
    }
}
