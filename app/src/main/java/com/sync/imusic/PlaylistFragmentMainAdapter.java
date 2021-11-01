package com.sync.imusic;

import static com.sync.imusic.AddToPlaylistPopup.PLAYLIST_NAME;
import static com.sync.imusic.VideoAdapter.VIDEO_FILES;
import static com.sync.imusic.VideoAdapter.VIDEO_FILES_POS;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sync.imusic.fragment.PlaylistsFragment;

import java.util.ArrayList;
import java.util.Objects;


public class PlaylistFragmentMainAdapter extends RecyclerView.Adapter<PlaylistFragmentMainAdapter.PlaylistFragmentMainAdapterViewHolder> implements PlaylistCardListener {
    private ArrayList<String> startingCharList;
    private Context mContext;
    private PlaylistsFragment fragment;
    public SameNameItemRecyclerAdapter adapter;
    private RecyclerView parentRecyclerView;
    private ArrayList<ArrayList<PlaylistFiles>> arrPlaylistFiles;

    PlaylistFragmentMainAdapter() {
    }

    public PlaylistFragmentMainAdapter(Context mContext, ArrayList<String> startingCharList, RecyclerView parentRecyclerView) {
        this.mContext = mContext;
        this.startingCharList = startingCharList;
        arrPlaylistFiles = new ArrayList<>();
        for (int i = 0; i < startingCharList.size(); i++) {
            arrPlaylistFiles.add(getPlaylistFiles(startingCharList.get(i)));
        }
        this.parentRecyclerView = parentRecyclerView;
    }

    @NonNull
    @Override
    public PlaylistFragmentMainAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_playlist_item, parent, false);
        return new PlaylistFragmentMainAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistFragmentMainAdapter.PlaylistFragmentMainAdapterViewHolder holder, int position) {
        holder.txt.setText(startingCharList.get(position));
        setSameNameItemRecycler(mContext, holder.itemRecycler, holder.getAdapterPosition());
        Log.d("mylog", "onBindViewHolder called");
    }

    @Override
    public int getItemCount() {
        return startingCharList.size();
    }

//    @Override
//    public int getItemViewType(int position) {
//        return position;
//    }

//    @Override
//    public long getItemId(int position) {
//        return startingCharList.get(position).hashCode();
//    }

    public static class PlaylistFragmentMainAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView txt;
        public RecyclerView itemRecycler;

        public PlaylistFragmentMainAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.playlist_item_txt);
            itemRecycler = itemView.findViewById(R.id.playlist_item_recyclerView);
        }
    }

    private void setSameNameItemRecycler(Context mContext, RecyclerView recyclerView, int pos) {
        adapter = new SameNameItemRecyclerAdapter(mContext, return_sameNamePlaylistFiles(arrPlaylistFiles.get(pos)), this, recyclerView, PlaylistFragmentMainAdapter.this);
        adapter.setHasStableIds(false);
        LinearLayoutManager llm = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(4);
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<SameNamePlaylistFiles> return_sameNamePlaylistFiles(ArrayList<PlaylistFiles> playlistFiles) {
        ArrayList<SameNamePlaylistFiles> sameNamePlaylistFiles = new ArrayList<>();
        for (int i = 0; i < playlistFiles.size(); i++) {
            int index = sameNamePlaylistFiles.indexOf(new SameNamePlaylistFiles(playlistFiles.get(i)));
            if (index == -1) {
                sameNamePlaylistFiles.add(new SameNamePlaylistFiles(playlistFiles.get(i)));
            } else {
                if (playlistFiles.get(i).isVideoFile || playlistFiles.get(i).isMusicFile) {
                    sameNamePlaylistFiles.get(index).add(playlistFiles.get(i));
                }
            }
        }

        return sameNamePlaylistFiles;
    }

    private ArrayList<PlaylistFiles> getPlaylistFiles(String character) {
        ArrayList<PlaylistFiles> temp = new ArrayList<>();
        DataBaseHelperPlaylist db = new DataBaseHelperPlaylist(mContext, PLAYLIST_NAME, null, 1);
        Cursor res = db.getAllData();
        if (res != null) {
            while (res.moveToNext()) {
                if (res.getString(13).startsWith(character)) {
                    if (res.getString(1).equals("1")) {
                        temp.add(new PlaylistFiles(new MusicFiles(
                                res.getString(3),
                                res.getString(4),
                                res.getString(5),
                                res.getString(6),
                                res.getString(7),
                                res.getString(8),
                                res.getString(9)
                        ), res.getString(13)));
                    }
                    if (res.getString(2).equals("1")) {
                        temp.add(new PlaylistFiles(new VideoFiles(
                                res.getString(8),
                                res.getString(3),
                                res.getString(4),
                                res.getString(10),
                                res.getString(9),
                                res.getString(11),
                                res.getString(7),
                                res.getString(12)
                        ), res.getString(13)));
                    }
                    if (res.getString(1).equals("0") && res.getString(2).equals("0")) {
                        temp.add(new PlaylistFiles(res.getString(13)));
                    }
                }
            }

            if (temp.size() == 0) {
                res = db.getAllData();
                while (res.moveToNext()) {
                    if (res.getString(13).startsWith(character)) {
                        temp.add(new PlaylistFiles(res.getString(13)));
                        break;
                    }
                }
            }
            res.close();
        }
//        db.close();
        return temp;
    }

    public void add(String character) {
        if (!startingCharList.contains(character)) {
            startingCharList.add(character);
            int index = startingCharList.size() - 1;
            arrPlaylistFiles.add(getPlaylistFiles(startingCharList.get(index)));
            notifyItemInserted(startingCharList.size());
        }
//        notifyDataSetChanged();
    }

    public void refresh() {
        for (int i = 0; i < startingCharList.size(); i++) {
            if ((parentRecyclerView.findViewHolderForAdapterPosition(i)) != null) {
                ((SameNameItemRecyclerAdapter) (((PlaylistFragmentMainAdapterViewHolder) Objects.requireNonNull(parentRecyclerView.findViewHolderForAdapterPosition(i))).itemRecycler.getAdapter())).refresh();
            }
        }
    }

    @Override
    public void itemClick(SameNamePlaylistFiles sameNamePlaylistFiles, SameNameItemRecyclerAdapter adapter, int pos) {
        ApplicationClass ref = (ApplicationClass)mContext.getApplicationContext();
        if (ref != null) {
            ref.setSameNameItemRecyclerAdapter_pos(adapter, pos);
        }
        Intent intent = new Intent(mContext, PlaylistContentsActivity.class);
        intent.putExtra("sameNamePlaylistFiles", sameNamePlaylistFiles);
        mContext.startActivity(intent);
    }

    @Override
    public void fabClick(SameNamePlaylistFiles sameNamePlaylistFiles) {
        ArrayList<PlaylistFiles> playlistFiles = new ArrayList<>();
        String playlistName = sameNamePlaylistFiles.getPlayListName();
        ArrayList<MusicFiles> musicFiles = sameNamePlaylistFiles.getMusicFiles();
        ArrayList<VideoFiles> videoFiles = sameNamePlaylistFiles.getVideoFiles();
        for (MusicFiles musicFile : musicFiles) {
            playlistFiles.add(new PlaylistFiles(musicFile, playlistName));
        }

        for (VideoFiles videoFile : videoFiles) {
            playlistFiles.add(new PlaylistFiles(videoFile, playlistName));
        }
        if (playlistFiles.size() > 0) {
            if (playlistFiles.get(0).isMusicFile) {
                ArrayList<MusicFiles> musicFilesTemp = new ArrayList<>();
                for (int i = 0; i < playlistFiles.size(); i++) {
                    if (playlistFiles.get(i).isMusicFile) {
                        musicFilesTemp.add(playlistFiles.get(i).getMusicFiles());
                    }
                }
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "playlistAdapter");
                intent.putExtra("listSongs", musicFilesTemp);
                intent.putExtra("position", 0);
                mContext.startActivity(intent);
            } else {
                ArrayList<VideoFiles> videoFilesTemp = new ArrayList<>();
                for (int i = 0; i < playlistFiles.size(); i++) {
                    if (playlistFiles.get(i) != null && playlistFiles.get(i).isVideoFile)
                        videoFilesTemp.add(playlistFiles.get(i).getVideoFiles());
                }
                Intent intent = new Intent(mContext, VideoPlayerActivity.class);

                intent.putExtra(VIDEO_FILES, videoFilesTemp);
                intent.putExtra(VIDEO_FILES_POS, 0);
                mContext.startActivity(intent);
            }
        } else {
            Toast.makeText(mContext, "No files in playlist", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void moreClick(SameNamePlaylistFiles playlistFile, SameNameItemRecyclerAdapter adapter) {
        PlaylistCardBottomSheet bottomSheet = new PlaylistCardBottomSheet();
        bottomSheet.setPlaylistFile(playlistFile);
        bottomSheet.setAdapter(adapter);
        bottomSheet.setShowsDialog(true);
        bottomSheet.show(((MainActivity) mContext).getSupportFragmentManager(), bottomSheet.getTag());
    }

    @Override
    public void longPress() {
    }

    public void delete(String startingChar) {
        for (int i = 0; i < startingCharList.size(); i++) {
            if (startingCharList.get(i).equals(startingChar)) {
                startingCharList.remove(startingCharList.get(i));
                notifyItemRemoved(i);
            }
        }
    }

}
