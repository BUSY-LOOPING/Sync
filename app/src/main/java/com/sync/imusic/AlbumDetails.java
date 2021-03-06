package com.sync.imusic;

import static com.sync.imusic.MusicService.MUSIC_ID;
import static com.sync.imusic.MusicService.MUSIC_NOW_PLAYING;
import static com.sync.imusic.MusicService.PLAYING_FROM;

import android.content.Context;
import android.content.Intent;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView albumPhoto;
    private String albumName;
    private ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    public static AlbumDetailsAdapter albumDetailsAdapter;
    private Toolbar mToolbar;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialisations
        setContentView(R.layout.activity_album_details);
        mToolbar = findViewById(R.id.album_details_album_name);
        recyclerView = findViewById(R.id.recyclerView_album_details);
        albumPhoto = findViewById(R.id.albumPhoto);
        fab = findViewById(R.id.fab_activity_album_details);

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
        if (!(MainActivity.musicFiles.size() < 1)) {
            for (int i = 0; i < MainActivity.musicFiles.size(); i++) {
                if (albumName.equals(MainActivity.musicFiles.get(i).getAlbum())) {
                    albumSongs.add(j, MainActivity.musicFiles.get(i));
                    j++;
                }
            }


            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
//                albumDetailsAdapter.setHasStableIds(true);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

            fab.setOnClickListener(v -> {
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra("sender", "albumDetails");
                albumDetailsAdapter.setPrev_played_id(albumSongs.get(0).getId());
                albumDetailsAdapter.notifyItemChanged(0);
                intent.putExtra("position", 0);
                startActivity(intent);
            });

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