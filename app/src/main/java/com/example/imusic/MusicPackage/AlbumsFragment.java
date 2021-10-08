package com.example.imusic.MusicPackage;

import static com.example.imusic.MainActivity.musicFiles;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imusic.AlbumAdapterSection;
import com.example.imusic.ApplicationClass;
import com.example.imusic.MainActivity;
import com.example.imusic.MusicFiles;
import com.example.imusic.R;

import java.util.ArrayList;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;


public class AlbumsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<MusicFiles> albums = new ArrayList<>();
    private Context context;
    private ArrayList<String> list = new ArrayList<>();
    private ApplicationClass ref;

    public AlbumsFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ref = (ApplicationClass) ((MainActivity)context).getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        albums = musicFiles;
        ArrayList<String> discreteAlbumNames = new ArrayList<>();
        for (int i = 0; i < albums.size(); i++) {
            if (!discreteAlbumNames.contains(albums.get(i).getAlbum())) {
                discreteAlbumNames.add(albums.get(i).getAlbum());
            }
        }
        albums = new ArrayList<>();
        for (int i = 0; i < discreteAlbumNames.size(); i++) {
            for (int j = 0; j < musicFiles.size(); j++) {
                if (musicFiles.get(j).getAlbum().equals(discreteAlbumNames.get(i))) {
                    albums.add(musicFiles.get(j));
                    break;
                }
            }
        }

        recyclerView = view.findViewById(R.id.recyclerView_fragment_albums);
        recyclerView.setHasFixedSize(true);
//        recyclerView.addItemDecoration(new AlbumItemDecorator(context, R.drawable.ic_music_note_full_freeicons));
//        if (!(albums.size() < 1)) {
        list = getList();
        SectionedRecyclerViewAdapter sectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter();
        ref.setSectionedRecyclerViewAdapter(sectionedRecyclerViewAdapter);
        for (int i = 0; i < list.size() ; i ++) {
            sectionedRecyclerViewAdapter.addSection(new AlbumAdapterSection(context, list.get(i), getAlbumsBeginningWith(list.get(i).substring(0 , 1))));
        }
        recyclerView.setItemViewCacheSize(6);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (sectionedRecyclerViewAdapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                    return 2;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(sectionedRecyclerViewAdapter);
//        }
        return view;
    }

    private ArrayList<MusicFiles> getAlbumsBeginningWith(String s) {
        ArrayList<MusicFiles> temp = new ArrayList<>();
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getAlbum().startsWith(s)) {
                temp.add(albums.get(i));
            }
        }
        return temp;
    }

    private ArrayList<String> getList () {
        ArrayList<String> temp = new ArrayList<>();
        for (int i =0 ; i < albums.size(); i ++) {
            String str = albums.get(i).getAlbum().substring(0 , 1);
            if (!temp.contains(str)) {
                temp.add(str);
            }
        }
        return temp;
    }
}