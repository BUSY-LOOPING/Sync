package com.example.imusic;

import static com.example.imusic.fragment.MoreFragment.historyAdapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class HistoryDetailsActivityAdapter extends RecyclerView.Adapter<HistoryDetailsActivityAdapter.HistoryDetailsActivityAdapterViewHolder> {
    private Context mContext;
    private DataBaseHelperHistory db;
    private ArrayList<PlaylistFiles> playlistFiles;

    public HistoryDetailsActivityAdapter(Context mContext, ArrayList<PlaylistFiles> playlistFiles) {
        this.mContext = mContext;
        this.playlistFiles = playlistFiles;
        db = new DataBaseHelperHistory(mContext, "history.db", null, 1);
        db.setAdapter(historyAdapter);
        db.setAdapter(HistoryDetailsActivityAdapter.this);
    }

    @NonNull
    @Override
    public HistoryDetailsActivityAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_history_details_item, parent, false);
        return new HistoryDetailsActivityAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryDetailsActivityAdapterViewHolder holder, int position) {
        if (playlistFiles.get(holder.getAdapterPosition()).isVideoFile) {
            holder.name.setText(playlistFiles.get(holder.getAdapterPosition()).getVideoFiles().getTitle());
            holder.artist.setVisibility(View.GONE);

            holder.thumbnail.setVisibility(View.VISIBLE);
            holder.album_art.setVisibility(View.GONE);
            holder.type.setVisibility(View.GONE);
            Glide.with(mContext)
                    .load(new File(playlistFiles.get(holder.getAdapterPosition()).getVideoFiles().getPath()))
                    .error(R.drawable.music_icon)
                    .into(holder.thumbnail);
        } else {
            holder.name.setText(playlistFiles.get(holder.getAdapterPosition()).getMusicFiles().getTitle());
            String artist = playlistFiles.get(holder.getAdapterPosition()).getMusicFiles().getArtist();
            if (artist == null || artist.equals("<unknown>")) {
                holder.artist.setVisibility(View.GONE);
            } else {
                holder.artist.setVisibility(View.VISIBLE);
                holder.artist.setText(artist);
            }
            if (holder.thumbnail.getVisibility() == View.VISIBLE) {
                holder.thumbnail.setVisibility(View.GONE);
                holder.type.setVisibility(View.VISIBLE);
                holder.album_art.setVisibility(View.VISIBLE);
            }
            byte[] array = db.getThumbnail(playlistFiles.get(holder.getAdapterPosition()).getMusicFiles().getId());
            if (array != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.album_art.setImageTintList(null);
                }
                Glide.with(mContext)
                        .load(array)
                        .error(R.drawable.music_icon)
                        .into(holder.album_art);
            } else {
                Glide.with(mContext)
                        .load(R.drawable.ic_music_note_full_freeicons)
                        .into(holder.album_art);
            }
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistFiles.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class HistoryDetailsActivityAdapterViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail, album_art, type;
        TextView name, artist;
        public HistoryDetailsActivityAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.type_img);
            thumbnail = itemView.findViewById(R.id.video_thumbnail);
            album_art = itemView.findViewById(R.id.img_history_item);
            name = itemView.findViewById(R.id.name_history_details_item);
            name.setSelected(true);
            artist = itemView.findViewById(R.id.artist_history_details_item);
        }
    }

    public void add(PlaylistFiles playlistFiles) {
        this.playlistFiles.add(playlistFiles);
        notifyItemInserted(this.playlistFiles.size() - 1);
    }

    public void remove(String id) {
        int index = -1;
        for (int i = 0; i < playlistFiles.size(); i++) {
            if (playlistFiles.get(i).isMusicFile) {
                if (playlistFiles.get(i).getMusicFiles().getId().equals(id)) {
                    index = i;
                    break;
                }
            } else {
                if (playlistFiles.get(i).getVideoFiles().getId().equals(id)) {
                    index = i;
                    break;
                }
            }
        }

        if (index >= 0) {
            playlistFiles.remove(index);
            notifyItemRemoved(index);
        }
    }
}
