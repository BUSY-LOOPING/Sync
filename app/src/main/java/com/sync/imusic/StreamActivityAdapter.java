package com.sync.imusic;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StreamActivityAdapter extends RecyclerView.Adapter<StreamActivityAdapter.StreamActivityAdapterViewHolder> {
    private Context mContext;
    private ArrayList<String> list;

    public StreamActivityAdapter(Context mContext, ArrayList<String> list) {
        this.list = list;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public StreamActivityAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.stream_activity_item, parent, false);
        return new StreamActivityAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StreamActivityAdapterViewHolder holder, int position) {
        holder.url.setText(list.get(position));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, StreamPlayer.class);
            intent.putExtra("url_id", StreamActivity.extractYTId(list.get(position)));
            intent.putExtra("original_url", list.get(position));
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class StreamActivityAdapterViewHolder extends RecyclerView.ViewHolder {
        private final TextView url;

        public StreamActivityAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            url = itemView.findViewById(R.id.url_txt);
        }
    }
}
