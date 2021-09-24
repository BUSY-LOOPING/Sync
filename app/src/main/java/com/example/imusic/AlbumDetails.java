package com.example.imusic;

import static com.example.imusic.MainActivity.musicFiles;
import static com.example.imusic.MusicService.MUSIC_ID;
import static com.example.imusic.MusicService.MUSIC_NOW_PLAYING;
import static com.example.imusic.MusicService.PLAYING_FROM;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView albumPhoto;
    private String albumName;
    private ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    public static AlbumDetailsAdapter albumDetailsAdapter;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialisations
        setContentView(R.layout.activity_album_details);
        mToolbar = findViewById(R.id.album_details_album_name);
        recyclerView = findViewById(R.id.recyclerView_album_details);
        albumPhoto = findViewById(R.id.albumPhoto);

        albumName = getIntent().getStringExtra("albumName");
        mToolbar.setTitle(albumName);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        int j = 0;
        if (!(musicFiles.size() < 1)) {
            for (int i = 0; i < musicFiles.size(); i++) {
                if (albumName.equals(musicFiles.get(i).getAlbum())) {
                    albumSongs.add(j, musicFiles.get(i));
                    j++;
                }
            }

            if (!(albumSongs.size() < 1)) {
                albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
//                albumDetailsAdapter.setHasStableIds(true);
                recyclerView.setHasFixedSize(false);
                recyclerView.setAdapter(albumDetailsAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            }

            byte[] image = getAlbumArt(albumSongs.get(0).getPath());
            if (image != null) {
                Glide.with(this)
                        .load(image)
                        .into(albumPhoto);
            } else {
                Glide.with(this)
                        .load(R.drawable.music_icon)
                        .into(albumPhoto);
            }
        }

    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(MUSIC_NOW_PLAYING, Context.MODE_PRIVATE);
        String last_played_id = preferences.getString(MUSIC_ID, null);
        String play_from = preferences.getString(PLAYING_FROM, null);
        if (play_from != null && play_from.equals("albumDetails")) {
            if (last_played_id != null) {
                albumDetailsAdapter.updateNowPlaying(preferences.getString(MUSIC_ID, null));
                albumDetailsAdapter.notifyDataSetChanged();
            }
        }
    }
}