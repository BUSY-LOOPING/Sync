package com.example.imusic.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.imusic.AddPlaylistFilesToDb;
import com.example.imusic.DataBaseHelperPlaylist;
import com.example.imusic.PlaylistFragmentMainAdapter;
import com.example.imusic.R;

import java.util.ArrayList;


public class PlaylistsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String PLAYLIST_NAME = "myplaylist.db";
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<String> startingCharList;
    private PlaylistFragmentMainAdapter adapter;
    private final Handler handler = new Handler();
    private Context context;

    public PlaylistsFragment() {

    }

    public static PlaylistsFragment newInstance() {

        Bundle args = new Bundle();

        PlaylistsFragment fragment = new PlaylistsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.main_recyclerView_fragment_playlist);
        startingCharList = getCharacterList();
        adapter = new PlaylistFragmentMainAdapter(context, startingCharList, recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.swipe_container_fragment_playlist);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.tab_highlight, R.color.white);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        adapter.refresh();
    }

    private ArrayList<String> getCharacterList() {
        ArrayList<String> temp = new ArrayList<>();
        DataBaseHelperPlaylist db = new DataBaseHelperPlaylist(context, PLAYLIST_NAME, null, 1);
        Cursor res = db.getAllData();
        if (res != null) {
            while (res.moveToNext()) {
                String string = res.getString(13);
                string = string.substring(0, 1);
                if (!temp.contains(string))
                    temp.add(string);
            }
            res.close();
        }
        db.close();
        return temp;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (AddPlaylistFilesToDb.ready) {
                    ArrayList<String> temp = getCharacterList();
                    for (String s : temp) {
//                        if (!startingCharList.contains(s)) {
//                            startingCharList.add(s);
                        adapter.add(s);
//                        }
                    }
                    adapter.refresh();
                    handler.removeCallbacks(this);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 1000);

                } else
                    handler.post(this);
            }
        });

//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                swipeRefreshLayout.setRefreshing(false);
//            }
//        }, 1500);

    }
}