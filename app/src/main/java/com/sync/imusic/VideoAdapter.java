package com.sync.imusic;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyVideoHolder> {
    private final Context mContext;
    private ArrayList<VideoFiles> videoFiles;
    private ArrayList<VideoFiles> selectedList;
    private ArrayList<Integer> positions;
    private RecyclerView recyclerView;
    private boolean isSelected = false;
    public ActionMode actionMode;
    private MenuItem menuItemBrowseInParent;
    private DataBaseHelperLastPlayed dataBaseHelperLastPlayed;

    public static final String VIDEO_FILES = "video_files_video_playerAct";
    public static final String VIDEO_FILES_POS = "video_files_pos_playerAct";
    public static final String VIDEO_PLAYBACK_POS = "video_files_playback_pos_playerAct";
    private boolean showMoreBtn = true;

    public VideoAdapter(Context mContext, ArrayList<VideoFiles> videoFiles, RecyclerView recyclerView) {
        this.mContext = mContext;
        this.videoFiles = videoFiles;
        this.recyclerView = recyclerView;
        selectedList = new ArrayList<>();
        positions = new ArrayList<>();
        dataBaseHelperLastPlayed = new DataBaseHelperLastPlayed(mContext, "lastPlayed.db", null, 1);
    }

    public ArrayList<VideoFiles> getSelectedList() {
        return selectedList;
    }

    public ArrayList<VideoFiles> getVideoFiles() {
        return videoFiles;
    }

    @NonNull
    @Override
    public MyVideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.video_item, parent, false);
        return new MyVideoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVideoHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.menuMore.setVisibility(showMoreBtn ? View.VISIBLE : View.INVISIBLE);
        holder.bar.setMax(Integer.parseInt(videoFiles.get(position).getDuration()));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int playBackPos;
        if (sharedPreferences.getBoolean("resume_playback", true)) {
            playBackPos = dataBaseHelperLastPlayed.getPrevPlayedPos(videoFiles.get(position).getId());
        } else {
            playBackPos = -1;
        }
        if (playBackPos == -1) {
            holder.bar.setVisibility(View.INVISIBLE);
            if (dataBaseHelperLastPlayed.contains(videoFiles.get(position).getId()))
                holder.seen.setVisibility(View.VISIBLE);
            else
                holder.seen.setVisibility(View.INVISIBLE);
        } else {
            holder.bar.setVisibility(View.VISIBLE);
            holder.seen.setVisibility(View.VISIBLE);
            holder.bar.setProgress(playBackPos);
        }
        if (holder.checkBox.getVisibility() == View.VISIBLE) {
            if (!isSelected || !selectedList.contains(videoFiles.get(position))) {
                clickItemWhenChecked(holder);
            }
        } else {
            if (isSelected && selectedList.contains(videoFiles.get(position))) {
                clickItemWhenChecked(holder);
            }
        }
        VideoExtensions videoExtensions = new VideoExtensions();
        String videoNameString = videoFiles.get(position).getFilename();
        videoNameString = videoExtensions.replaceExtension(videoNameString);
        holder.displayName.setText(videoNameString);
        int duration = Integer.parseInt(videoFiles.get(position).getDuration());
        String resolutionInGeneral = videoFiles.get(position).getResolutionInGeneral();
        if (resolutionInGeneral == null) {
            holder.duration_resolution.setText(new StringBuilder()
                    .append(formattedLength(duration / 1000))
                    .append("  ·  ")
                    .append(videoFiles.get(position).getResolution()));
        } else {
            holder.duration_resolution.setText(new StringBuilder()
                    .append(formattedLength(duration / 1000))
                    .append("  ·  ")
                    .append(resolutionInGeneral));
        }

        Glide.with(mContext)
                .load(Uri.fromFile(new File(videoFiles.get(position).getPath())))
                .error(R.drawable.music_icon)
                .into(holder.thumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelected) {
                    if (selectedList.size() == 1 && videoFiles.get(position).getId().equals(selectedList.get(0).getId())) {
                        actionMode.finish();
                    }
                    clickItemWhenChecked(holder);
                } else {
                    Intent intent = new Intent(mContext, VideoPlayerActivity.class);
                    intent.putExtra(VIDEO_FILES, videoFiles);
                    intent.putExtra(VIDEO_FILES_POS, position);
                    intent.putExtra(VIDEO_PLAYBACK_POS, playBackPos == -1 ? 0 : playBackPos);
                    mContext.startActivity(intent);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!isSelected) {
                    ActionMode.Callback callback = new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            showMoreBtn = false;
                            LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                            if (llm != null) {
                                for (int i = llm.findFirstVisibleItemPosition(); i <= llm.findLastVisibleItemPosition(); i++) {
                                    notifyItemChanged(i);
                                }
                            }
                            VideoAdapter.this.actionMode = mode;
                            mode.getMenuInflater().inflate(R.menu.contextual_menu_video_adapter, menu);
                            menuItemBrowseInParent = menu.findItem(R.id.browse_parent_contextual_menu_videoAdapter);
                            MenuItem item = menu.getItem(1);
                            View itemChooser = item.getActionView();
                            if (itemChooser != null) {
                                itemChooser.setOnClickListener(v -> {
                                    Toast.makeText(mContext, "Appended to current list", Toast.LENGTH_SHORT).show();
                                    VideoPlayerActivity.append(selectedList);
                                });
                                itemChooser.setOnLongClickListener(v -> {
                                    return true;
                                });
                            }

                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            isSelected = true;
                            clickItemWhenChecked(holder);
                            return true;
                        }

                        @SuppressLint("NonConstantResourceId")
                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            Intent intent;
                            int id = item.getItemId();
                            menuItemBrowseInParent.setVisible(selectedList.size() <= 1);
                            switch (id) {
                                case R.id.play_contextual_menu_videoAdapter:
                                    intent = new Intent(mContext, VideoPlayerActivity.class);
                                    intent.putExtra(VIDEO_FILES, selectedList);
                                    intent.putExtra(VIDEO_FILES_POS, 0);
                                    mContext.startActivity(intent);
                                    break;
                                case R.id.append_contextual_menu_videoAdapter:
                                    VideoPlayerActivity.append(selectedList);
                                    break;
                                case R.id.browse_parent_contextual_menu_videoAdapter:
                                    intent = new Intent(mContext, FileActivity.class);
                                    List<File> tempList = new ArrayList<>();
                                    tempList.add(new File(selectedList.get(0).getPath()).getParentFile());
                                    intent.putExtra("parent", (Serializable) tempList);
                                    mContext.startActivity(intent);
                                    break;
                                case R.id.add_to_playlist_contextual_menu_videoAdapter:
                                    AddToPlaylistPopup addToPlaylistPopup = new AddToPlaylistPopup();
                                    addToPlaylistPopup.setShowsDialog(true);
                                    addToPlaylistPopup.show(((MainActivity) mContext).getSupportFragmentManager(), addToPlaylistPopup.getTag());
                                    addToPlaylistPopup.addVideoFiles(selectedList);
                                    break;
                                case R.id.mark_unseen:
                                    for (VideoFiles videoFile : selectedList)
                                        dataBaseHelperLastPlayed.deleteForId(videoFile.getId());
                                    actionMode.finish();
                                    break;
                            }
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            showMoreBtn = true;
                            isSelected = false;
                            selectedList.clear();
                            LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                            if (llm != null) {
                                for (int i = llm.findFirstVisibleItemPosition(); i <= llm.findLastVisibleItemPosition(); i++) {
                                    notifyItemChanged(i);
                                }
                            }
                            for (int i = 0; i < positions.size(); i++) {
                                notifyItemChanged(positions.get(i));
                            }
                            actionMode = null;
                        }
                    };
                    ((AppCompatActivity) mContext).startActionMode(callback);
                } else {
                    if (selectedList.size() == 1 && videoFiles.get(position).getId().equals(selectedList.get(0).getId())) {
                        actionMode.finish();
                    }
                    clickItemWhenChecked(holder);
                }
                return true;
            }
        });
        holder.menuMore.setOnClickListener(v -> {
            VideoAdapterBottomSheet bottomSheet = new VideoAdapterBottomSheet();
            bottomSheet.setVideoAdapter(VideoAdapter.this);
            Bundle bundle = new Bundle();
            bundle.putSerializable("arraylist", videoFiles);
            bundle.putSerializable("position", position);
            bundle.putSerializable("playBackPos", playBackPos);
            bottomSheet.setArguments(bundle);
            bottomSheet.show(((MainActivity)mContext).getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MyVideoHolder holder) {
        holder.menuMore.setVisibility(showMoreBtn ? View.VISIBLE : View.INVISIBLE);
    }

    private void removeMoreButton(MyVideoHolder holder) {
        holder.menuMore.setVisibility(View.INVISIBLE);
//        LinearLayoutManager manager = ((LinearLayoutManager)recyclerView.getLayoutManager());
//        if (manager != null) {
//            int from = Math.max(manager.findFirstVisibleItemPosition() - 10, 0);
//            int to = Math.min(manager.findLastVisibleItemPosition() + 10, videoFiles.size());
//            notifyItemRangeChanged(from, to);
//        }
//        notifyDataSetChanged();
    }

    private void clickItemWhenChecked(MyVideoHolder holder) {
        if (holder.checkBox.getVisibility() == View.GONE) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.color.ripple_color_light);
            if (holder.getAdapterPosition() >= 0) {
                selectedList.add(videoFiles.get(holder.getAdapterPosition()));
                positions.add(holder.getAdapterPosition());
            }
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setBackgroundResource(R.drawable.custom_ripple);
            if (holder.getAdapterPosition() >= 0) {
                selectedList.remove(videoFiles.get(holder.getAdapterPosition()));
            }

        }
    }

    @Override
    public int getItemCount() {
        return videoFiles.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }

    public static class MyVideoHolder extends RecyclerView.ViewHolder {
        private final TextView displayName, duration_resolution;
        private final ImageView thumbnail, menuMore, checkBox, seen;
        private final ContentLoadingProgressBar bar;

        public MyVideoHolder(@NonNull View itemView) {
            super(itemView);
            displayName = itemView.findViewById(R.id.video_displayName_video_item);
            duration_resolution = itemView.findViewById(R.id.duration_resolution);
            thumbnail = itemView.findViewById(R.id.thumbnail_video_item);
            menuMore = itemView.findViewById(R.id.menuMore_video_item);
            checkBox = itemView.findViewById(R.id.thumbnail_video_item_selected);
            seen = itemView.findViewById(R.id.seen);
            bar = itemView.findViewById(R.id.bar);
        }
    }

    public static String formattedLength(int lengthInSec) {  //mCurrentPosition is in seconds
        String result;
        int sec, min = 0, hours = 0;
        while (lengthInSec >= 60) {
            lengthInSec = lengthInSec - 60;
            min++;
            if (min == 60) {
                hours++;
                min = 0;
            }
        }
        sec = lengthInSec;
        StringBuilder stringBuilder = new StringBuilder();
        if (hours == 0 && min == 0)
            stringBuilder.append(sec).append("sec");
        if (hours == 0 && min != 0)
            stringBuilder.append(min).append("min").append(sec).append("sec");
        if (hours != 0)
            stringBuilder.append(hours).append("hr").append(min).append("min").append(sec).append("sec");

        result = stringBuilder.toString();
        return result;
    }

    public void refresh(ArrayList<VideoFiles> videoFiles) {
//        this.videoFiles.addAll(videoFiles);
//        notifyItemRangeInserted(this.videoFiles.size() - videoFiles.size(), videoFiles.size());
        this.videoFiles = videoFiles;
        notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    public void updateList(ArrayList<VideoFiles> videoFiles) {
        this.videoFiles = videoFiles;
        notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

}
