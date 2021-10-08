package com.example.imusic;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
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
import com.taishi.library.Indicator;

import java.util.ArrayList;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.MyHolder> {
    private Context mContext;
    private String prev_played_id;
    public static ArrayList<MusicFiles> albumFiles;
    View view;

    public void setPrev_played_id(String prev_played_id) {
        this.prev_played_id = prev_played_id;
    }

    public AlbumDetailsAdapter(Context mContext, ArrayList<MusicFiles> albumFiles) {
        this.mContext = mContext;
        AlbumDetailsAdapter.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(albumFiles.get(position).getId());
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        if (holder.indicator.getVisibility() == View.VISIBLE) {
            holder.indicator.setVisibility(View.GONE);
            holder.album_img.setAlpha(1f);
        }
        if (prev_played_id != null) {
            if (albumFiles.get(position).getId().equals(prev_played_id)) {
                holder.indicator.setVisibility(View.VISIBLE);
                holder.album_img.setAlpha(0.5f);
            }
        }
        holder.album_name.setText(albumFiles.get(position).getTitle());
        holder.artist_album.setText(new StringBuilder().append(albumFiles.get(position).getArtist()).append("  .  ").append(albumFiles.get(position).getAlbum()).toString());
        MyImageLoader.from loader = new MyImageLoader.from(mContext);
        loader.load(Long.parseLong(albumFiles.get(position).getId()));
        loader.into(holder.album_img);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.album_img.setAlpha(0.5f);
                holder.indicator.setVisibility(View.VISIBLE);
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "albumDetails");
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView album_img;
        TextView album_name, artist_album;
        Indicator indicator;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            album_img = view.findViewById(R.id.music_img);
            album_name = view.findViewById(R.id.music_file_name);
            artist_album = view.findViewById(R.id.artist_album);
            indicator = itemView.findViewById(R.id.music_indicator);
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mContext, Uri.parse(uri));
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    public void updateNowPlaying(String prev_played_id) {
        this.prev_played_id = prev_played_id;
        for (int i = 0; i < albumFiles.size(); i++) {
            if (albumFiles.get(i).getId().equals(prev_played_id)) {
                notifyItemChanged(i);
                break;
            }
        }
    }
}
