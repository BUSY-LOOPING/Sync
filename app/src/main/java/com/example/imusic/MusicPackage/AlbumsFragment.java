package com.example.imusic.MusicPackage;

import static com.example.imusic.MainActivity.musicFiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imusic.AlbumAdapter;
import com.example.imusic.MusicFiles;
import com.example.imusic.R;

import java.util.ArrayList;


public class AlbumsFragment extends Fragment {

    public AlbumsFragment() {

    }

    RecyclerView recyclerView;
    AlbumAdapter albumAdapter;
    ArrayList<MusicFiles> albums = new ArrayList<>();

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
        if (!(albums.size() < 1)) {
            albumAdapter = new AlbumAdapter(getActivity(), albums);
            albumAdapter.setHasStableIds(true);   ///////////////very important for smooth scrollingggggg
            recyclerView.setAdapter(albumAdapter);
            recyclerView.setItemViewCacheSize(6);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }
        return view;
    }
}