package com.example.imusic;

import static com.example.imusic.AddToPlaylistPopup.PLAYLIST_NAME;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class PlaylistFragmentMainAdapter extends RecyclerView.Adapter<PlaylistFragmentMainAdapter.PlaylistFragmentMainAdapterViewHolder> implements PlaylistCardListener {
    private ArrayList<String> startingCharList;
    private Context mContext;
    public SameNameItemRecyclerAdapter adapter;
    private RecyclerView parentRecyclerView;
    private ArrayList<ArrayList<PlaylistFiles>> arrPlaylistFiles;

    PlaylistFragmentMainAdapter() {
    }

    public PlaylistFragmentMainAdapter(Context mContext, ArrayList<String> startingCharList, RecyclerView parentRecyclerView) {
        this.mContext = mContext;
        this.startingCharList = startingCharList;
        arrPlaylistFiles = new ArrayList<>();
        for (int i =0; i <startingCharList.size();i++) {
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
        setSameNameItemRecycler(mContext, holder.itemRecycler, position);
    }

    @Override
    public int getItemCount() {
        return startingCharList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return startingCharList.get(position).hashCode();
    }

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
        adapter = new SameNameItemRecyclerAdapter(mContext, return_sameNamePlaylistFiles(arrPlaylistFiles.get(pos)), this, recyclerView);
        adapter.setHasStableIds(true);
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
        long start = System.currentTimeMillis();
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
        db.close();
        return temp;
    }

    public void add(String character) {
        if (!startingCharList.contains(character)) {
            startingCharList.add(character);
        }
        notifyItemInserted(startingCharList.size() - 1);
    }

    public void refresh() {
        for (int i = 0; i < parentRecyclerView.getAdapter().getItemCount(); i++) {
            if ((parentRecyclerView.findViewHolderForAdapterPosition(i)) != null)
                ((SameNameItemRecyclerAdapter) (((PlaylistFragmentMainAdapterViewHolder) parentRecyclerView.findViewHolderForAdapterPosition(i)).itemRecycler.getAdapter())).refresh();
        }
    }

    @Override
    public void itemClick(SameNamePlaylistFiles sameNamePlaylistFiles) {
        Intent intent = new Intent(mContext, PlaylistContentsActivity.class);
        intent.putExtra("sameNamePlaylistFiles", sameNamePlaylistFiles);
        mContext.startActivity(intent);
    }

    @Override
    public void fabClick() {
        Toast.makeText(mContext, "fab clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void moreClick() {
        Toast.makeText(mContext, "more clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void longPress() {
        Toast.makeText(mContext, "long clicked", Toast.LENGTH_SHORT).show();
    }

}
