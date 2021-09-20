package com.example.imusic;

import static com.example.imusic.VideoAdapter.VIDEO_FILES;
import static com.example.imusic.VideoAdapter.VIDEO_FILES_POS;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
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
import java.io.Serializable;
import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryAdapterViewHolder> implements DeleteMusicFile{
    private Context mContext;
    private ArrayList<PlaylistFiles> playlistFiles;
    private final DataBaseHelperHistory db;
    private RecyclerView recyclerView;


    public HistoryAdapter(Context mContext, ArrayList<PlaylistFiles> playlistFiles, RecyclerView recyclerView) {
        this.mContext = mContext;
        this.playlistFiles = playlistFiles;
        this.recyclerView = recyclerView;
        db = new DataBaseHelperHistory(mContext, "history.db", null, 1);
        db.setAdapter(HistoryAdapter.this);
    }

    @Override
    public int getItemCount() {
        return playlistFiles.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

//    @Override
//    public long getItemId(int position) {
//        return playlistFiles.hashCode();
//    }

    @NonNull
    @Override
    public HistoryAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_history_item, parent, false);
        return new HistoryAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapterViewHolder holder, int position) {
//        db = new DataBaseHelperHistory(mContext, "history.db", null, 1);
        holder.name.setText(playlistFiles.get(holder.getAdapterPosition()).isMusicFile ? playlistFiles.get(holder.getAdapterPosition()).getMusicFiles().getTitle() : playlistFiles.get(holder.getAdapterPosition()).getVideoFiles().getFilename());
        if (playlistFiles.get(holder.getAdapterPosition()).isVideoFile) {
            holder.details.setText(playlistFiles.get(holder.getAdapterPosition()).getVideoFiles().getResolutionInGeneral() == null ? playlistFiles.get(holder.getAdapterPosition()).getVideoFiles().getResolution() : playlistFiles.get(holder.getAdapterPosition()).getVideoFiles().getResolutionInGeneral());
            if (holder.thumbnail.getVisibility() == View.INVISIBLE) {
                holder.thumbnail.setVisibility(View.VISIBLE);
                holder.album_art.setVisibility(View.INVISIBLE);
                holder.type.setVisibility(View.INVISIBLE);
            }
            Glide.with(mContext)
                    .load(Uri.fromFile(new File(playlistFiles.get(holder.getAdapterPosition()).getVideoFiles().getPath())))
                    .into(holder.thumbnail);
        } else {
            if (holder.thumbnail.getVisibility() == View.VISIBLE) {
                holder.thumbnail.setVisibility(View.INVISIBLE);
                holder.type.setVisibility(View.VISIBLE);
                holder.album_art.setVisibility(View.VISIBLE);
            }
            String artist = playlistFiles.get(holder.getAdapterPosition()).getMusicFiles().getArtist();
            String album = playlistFiles.get(holder.getAdapterPosition()).getMusicFiles().getAlbum();
            String detailsTxt = "";
            if (artist.equals("<unknown>") && album.equals("<unknown>")) {
                if (holder.details.getVisibility() == View.VISIBLE)
                    holder.details.setVisibility(View.GONE);
            } else {
                if (holder.details.getVisibility() == View.GONE)
                    holder.details.setVisibility(View.VISIBLE);
                if (!artist.equals("<unknown>")) {
                    detailsTxt = artist + "  ·  ";
                }
                if (!album.equals("<unknown>")) {
                    detailsTxt += album;
                }
                holder.details.setText(detailsTxt);
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
//        db.close();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playlistFiles.get(holder.getAdapterPosition()).isMusicFile) {
                    Intent intent = new Intent(mContext, PlayerActivity.class);
                    intent.putExtra("sender" , "historyAdapter");
                    intent.putExtra("position", 0);
                    ArrayList<MusicFiles> temp = new ArrayList<>();
                    temp.add(playlistFiles.get(holder.getAdapterPosition()).getMusicFiles());
                    intent.putExtra("listSongs",(Serializable) temp);
                    mContext.startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, VideoPlayerActivity.class);
                    ArrayList<VideoFiles> temp = new ArrayList<>();
                    temp.add(playlistFiles.get(holder.getAdapterPosition()).getVideoFiles());
                    intent.putExtra(VIDEO_FILES, temp);
                    intent.putExtra(VIDEO_FILES_POS, 0);
                    mContext.startActivity(intent);
                }
            }
        });

    }

    @Override
    public void delete(MusicFiles fileDeleted) {
        Log.d("mylog", "deleted");
    }


    public class HistoryAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView album_art, thumbnail, type;
        TextView name, details;

        public HistoryAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.type_img);
            album_art = itemView.findViewById(R.id.img_history_item);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                album_art.setClipToOutline(true);
            }
            thumbnail = itemView.findViewById(R.id.thumbnail_history_item);
            name = itemView.findViewById(R.id.name_history_item);
            details = itemView.findViewById(R.id.details_history_item);
//            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }

    private Bitmap getAlbumArt(String uri) {
        byte[] art = null;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri);
            art = retriever.getEmbeddedPicture();
            retriever.release();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (art == null) return null;
        return BitmapFactory.decodeByteArray(art, 0, art.length);
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

    public void add(PlaylistFiles playlistFiles) {
        this.playlistFiles.add(playlistFiles);
        notifyItemInserted(this.playlistFiles.size() - 1);
    }

    public void removeAll() {
        int size = playlistFiles.size();
        notifyItemRangeRemoved(0, size);
        playlistFiles.clear();
    }
}
