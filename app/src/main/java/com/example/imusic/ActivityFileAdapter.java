package com.example.imusic;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ActivityFileAdapter extends RecyclerView.Adapter<ActivityFileAdapter.MyActivityFileViewHolder> {
    private Context mContext;
    private List<File> list;
    private OnClickListenerActivityFileAdapter mListener;
    long start;
    String[] temp;
    ArrayList<Integer> tempList;

    ActivityFileAdapter() {

    }

    ActivityFileAdapter(Context mContext, List<File> list, ArrayList<Integer> tempList , OnClickListenerActivityFileAdapter mListener) {
        this.mContext = mContext;
        this.list = list;
        this.mListener = mListener;
        this.tempList = tempList;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).hashCode();
    }

    @NonNull
    @Override
    public MyActivityFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_file_item, parent, false);
        return new MyActivityFileViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityFileAdapter.MyActivityFileViewHolder holder, int position) {
        start = System.currentTimeMillis();
//        temp = list.get(position).list();
//        holder.folder_file_name.setText(list.get(position).getName());
//        if (list.get(position).isDirectory()) {
//            if (temp != null) {
//                if (temp.length == 0)
//                    holder.no_items.setText("Empty");
//                else
//                    holder.no_items.setText(String.valueOf(temp.length));
//            }
//        } else {
//            holder.no_items.setVisibility(View.GONE);
//        }

        holder.folder_file_name.setText(list.get(position).getName());
        if (list.get(position).isDirectory()) {
                if (tempList.get(position) == 0)
                    holder.no_items.setText("Empty");
                else
                    holder.no_items.setText(String.valueOf(tempList.get(position)));
        } else {
            holder.no_items.setVisibility(View.GONE);
        }


        if (isVideoFile(list.get(position).getPath())) {
            Glide.with(mContext)
                    .load(Uri.fromFile(new File(list.get(position).getPath())))
                    .error(R.drawable.music_icon)
                    .into(holder.folder_img);
        }

        Log.d("myfile", "time = " + (System.currentTimeMillis() - start));
    }

    private boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    private boolean isMusicFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("music");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyActivityFileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView folder_file_name, no_items;
        ImageView folder_img, more;
        OnClickListenerActivityFileAdapter listener;

        public MyActivityFileViewHolder(@NonNull View itemView, OnClickListenerActivityFileAdapter listener) {
            super(itemView);
            this.listener = listener;
            folder_file_name = itemView.findViewById(R.id.folder_file_name_file_item);
            folder_file_name.setSelected(true);
            no_items = itemView.findViewById(R.id.no_items_activity_file);
            folder_img = itemView.findViewById(R.id.folder_img_file_item);
            more = itemView.findViewById(R.id.more_file_item);
            itemView.setOnClickListener(this);
            more.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.more_file_item) {
                listener.onMoreClick(getAdapterPosition(), list.get(getAdapterPosition()));
            } else {
                listener.onItemViewClick(getAdapterPosition(), list.get(getAdapterPosition()));
            }
        }
    }

    public void update(List<File> list, RecyclerView recyclerView) {
//        this.list = new ArrayList<>();
//        this.list.addAll(list);
        this.list = list;
        notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }
}
