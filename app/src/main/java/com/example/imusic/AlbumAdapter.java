package com.example.imusic;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumAdapterViewHolder> {
    private Context mContext;
    private ArrayList<MusicFiles> albumFiles;

    public AlbumAdapter(Context mContext, ArrayList<MusicFiles> albumFiles) {
        this.mContext = mContext;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public AlbumAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        return new AlbumAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumAdapterViewHolder holder, int position) {
        holder.albumName.setText(albumFiles.get(holder.getAdapterPosition()).getAlbum());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AlbumDetails.class);
                intent.putExtra("albumName",albumFiles.get(holder.getAdapterPosition()).getAlbum());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class AlbumAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView albumName;
        public AlbumAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.album_name);
            albumName.setSelected(true);
        }
    }
}