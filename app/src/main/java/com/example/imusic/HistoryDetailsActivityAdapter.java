package com.example.imusic;

import static com.example.imusic.InfoActivityVideo.VIDEO_FILE_INFO_ACT;
import static com.example.imusic.VideoAdapter.VIDEO_FILES;
import static com.example.imusic.VideoAdapter.VIDEO_FILES_POS;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HistoryDetailsActivityAdapter extends RecyclerView.Adapter<HistoryDetailsActivityAdapter.HistoryDetailsActivityAdapterViewHolder> {
    private final Context mContext;
    private final DataBaseHelperHistory db;
    private ArrayList<PlaylistFiles> playlistFiles;

    private ArrayList<PlaylistFiles> selectedList;
    private ArrayList<Integer> positions;
    private boolean selected = false;
    private ActionMode actionMode;
    private MenuItem browseParentMenuItem, infoMenuItem;

    public HistoryDetailsActivityAdapter(Context mContext, ArrayList<PlaylistFiles> playlistFiles, DataBaseHelperHistory db) {
        this.mContext = mContext;
        this.playlistFiles = playlistFiles;
        this.db = db;
        selectedList = new ArrayList<>();
        positions = new ArrayList<>();
//        db = new DataBaseHelperHistory(mContext, "history.db", null, 1);
//        db.setAdapter(historyAdapter);
//        db.setAdapter(HistoryDetailsActivityAdapter.this);
    }

    @NonNull
    @Override
    public HistoryDetailsActivityAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_history_details_item, parent, false);
        return new HistoryDetailsActivityAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryDetailsActivityAdapterViewHolder holder, int position) {
        if (selected) {
            if (selectedList.size() == 1 && selectedList.get(0).isMusicFile ?
                    (playlistFiles.get(position).isMusicFile && selectedList.get(0).getMusicFiles().getId().equals(playlistFiles.get(position).getMusicFiles().getId())) :
                    playlistFiles.get(position).isVideoFile && selectedList.get(0).getVideoFiles().getId().equals(playlistFiles.get(position).getVideoFiles().getId())) {
                holder.itemView.setBackgroundResource(R.color.ripple_color_light);
            }
            else
                holder.itemView.setBackgroundResource(R.drawable.custom_ripple);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.custom_ripple);
        }

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
                if (!selected) {
                    if (playlistFiles.get(holder.getAdapterPosition()).isMusicFile) {
                        Intent intent = new Intent(mContext, PlayerActivity.class);
                        intent.putExtra("sender", "historyAdapter");
                        intent.putExtra("position", 0);
                        ArrayList<MusicFiles> temp = new ArrayList<>();
                        temp.add(playlistFiles.get(holder.getAdapterPosition()).getMusicFiles());
                        intent.putExtra("listSongs", (Serializable) temp);
                        mContext.startActivity(intent);
                    } else {
                        Intent intent = new Intent(mContext, VideoPlayerActivity.class);
                        ArrayList<VideoFiles> temp = new ArrayList<>();
                        temp.add(playlistFiles.get(holder.getAdapterPosition()).getVideoFiles());
                        intent.putExtra(VIDEO_FILES, temp);
                        intent.putExtra(VIDEO_FILES_POS, 0);
                        mContext.startActivity(intent);
                    }
                } else {
                    int index = positions.indexOf(position);
                    if (index != -1) {
                        selectedList.remove(index);
                        positions.remove((Integer) position);
                        holder.itemView.setBackgroundResource(R.drawable.custom_ripple);
                    } else {
                        selectedList.add(playlistFiles.get(position));
                        positions.add(position);
                        holder.itemView.setBackgroundResource(R.color.ripple_color_light);
                    }
                    if (infoMenuItem != null) {
                        infoMenuItem.setVisible(selectedList.size() == 1);
                    }
                    if (browseParentMenuItem != null) {
                        browseParentMenuItem.setVisible(selectedList.size() == 1);
                    }
                    if (selectedList.size() == 0) {
                        actionMode.finish();
                    }
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!selected) {
                    ActionMode.Callback callback = new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            actionMode = mode;
                            mode.getMenuInflater().inflate(R.menu.contextual_menu_history, menu);
                            browseParentMenuItem = menu.findItem(R.id.browse_parent_contextual_menu_history);
                            infoMenuItem = menu.findItem(R.id.info_contextual_menu_history);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            selected = true;
                            selectedList.clear();
                            positions.clear();
                            selectedList.add(playlistFiles.get(position));
                            positions.add(position);
                            holder.itemView.setBackgroundResource(R.color.ripple_color_light);
                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            SharedPreferences.Editor editor = mContext.getSharedPreferences("back_pressed", Context.MODE_PRIVATE).edit();
                            editor.clear();
                            editor.apply();
                            int id = item.getItemId();
                            browseParentMenuItem.setVisible(selectedList.size() <= 1);
                            infoMenuItem.setVisible(selectedList.size() <= 1);
                            switch (id) {
                                case R.id.browse_parent_contextual_menu_history:
                                    Intent intent = new Intent(mContext, FileActivity.class);
                                    List<File> tempList = new ArrayList<>();
                                    tempList.add(new File(selectedList.get(0).isMusicFile ?
                                            selectedList.get(0).getMusicFiles().getPath() : selectedList.get(0).getVideoFiles().getPath()).getParentFile());
                                    intent.putExtra("parent", (Serializable) tempList);
                                    mContext.startActivity(intent);
                                    break;

                                case R.id.info_contextual_menu_history:
                                    Intent intent1;
                                    if (playlistFiles.get(position).isVideoFile) {
                                        intent1 = new Intent(mContext, InfoActivityVideo.class);
                                        intent1.putExtra(VIDEO_FILE_INFO_ACT, playlistFiles.get(position).getVideoFiles());
                                    } else {
                                        intent1 = new Intent(mContext, InfoActivity.class);
                                        ArrayList<MusicFiles> temp = new ArrayList<>();
                                        temp.add(playlistFiles.get(position).getMusicFiles());
                                        intent1.putExtra("musicFilePlayerAct", temp);
                                        intent1.putExtra("posPlayerAct", 0);
                                    }
                                    mContext.startActivity(intent1);
                                    break;
                            }
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            selected = false;
                            selectedList.clear();
                            for (int i = 0; i < positions.size(); i++) {
                                notifyItemChanged(positions.get(i));
                            }
//                            notifyDataSetChanged();
                            positions.clear();
                            actionMode = null;
                        }
                    };
                    ((AppCompatActivity) mContext).startSupportActionMode(callback);
                } else {
                    if (!positions.contains(position)) {
                        positions.add(position);
                        selectedList.add(playlistFiles.get(position));
                        holder.itemView.setBackgroundResource(R.color.ripple_color_light);
                    } else {
                        positions.remove((Integer) position);
                        selectedList.remove(playlistFiles.get(position));
                        holder.itemView.setBackgroundResource(R.drawable.custom_ripple);
                    }
                    if (infoMenuItem != null && browseParentMenuItem != null && selectedList.size() > 1) {
                        infoMenuItem.setVisible(false);
                        browseParentMenuItem.setVisible(false);
                    }
                    if (selectedList.size() == 0) {
                        actionMode.finish();
                    }
                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return playlistFiles.size();
    }

//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    @Override
//    public long getItemId(int position) {
//        return playlistFiles.get(position).hashCode();
//    }

    //    @Override
//    public int getItemViewType(int position) {
//        return position;
//    }

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

    public void removeAll() {
        int size = playlistFiles.size();
        playlistFiles.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void update(ArrayList<PlaylistFiles> playlistFiles, RecyclerView recyclerView) {
        this.playlistFiles = playlistFiles;
        notifyDataSetChanged();
//        recyclerView.scheduleLayoutAnimation();
    }

}
