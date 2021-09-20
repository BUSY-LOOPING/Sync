package com.example.imusic;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.Collections;

public class PlaylistContentsActivity extends AppCompatActivity implements PlaylistContentListener {
    private SameNamePlaylistFiles sameNamePlaylistFiles;
    private RecyclerView recyclerView;
    private ItemTouchHelper itemTouchHelper;
    private ArrayList<PlaylistFiles> playlistFiles = new ArrayList<>();
    private PlaylistContentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_contents);
        init();
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        adapter = new PlaylistContentAdapter(this, playlistFiles, this);
        adapter.setHasStableIds(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |
            ItemTouchHelper.DOWN, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(playlistFiles, fromPosition, toPosition);
            adapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            //swipe directions
        }

        @Override
        public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target) {
            return true;
        }
    };

    private void init() {
        recyclerView = findViewById(R.id.recyclerView_activity_playlist_content);
        Toolbar toolbar = findViewById(R.id.toolbar_activity_playlist_contents);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        sameNamePlaylistFiles = (SameNamePlaylistFiles) getIntent().getSerializableExtra("sameNamePlaylistFiles");
        if (sameNamePlaylistFiles != null) {
            toolbar.setTitle(sameNamePlaylistFiles.getPlayListName());
            if (sameNamePlaylistFiles.getSize() == 1) {
                ImageView single_img = findViewById(R.id.single_img);
                single_img.setVisibility(View.VISIBLE);

            }
            ImageView img1, img2, img3, img4;
            if (sameNamePlaylistFiles.getSize() == 0) {
                img1 = findViewById(R.id.img1_);
                img1.setImageResource(R.color.black);

                img2 = findViewById(R.id.img2_);
                img2.setImageResource(R.color.black);

                img3 = findViewById(R.id.img3_);
                img3.setImageResource(R.color.black);

                img4 = findViewById(R.id.img4_);
                img4.setImageResource(R.color.black);
                AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout_activity_playlist_content);
                appBarLayout.setExpanded(false);
            } else {

            }


            ArrayList<MusicFiles> musicFiles = sameNamePlaylistFiles.getMusicFiles();
            ArrayList<VideoFiles> videoFiles = sameNamePlaylistFiles.getVideoFiles();

            for (int i = 0; i < musicFiles.size(); i++) {
                playlistFiles.add(new PlaylistFiles(musicFiles.get(i), sameNamePlaylistFiles.getPlayListName()));
            }
            for (int i = 0; i < videoFiles.size(); i++) {
                playlistFiles.add(new PlaylistFiles(videoFiles.get(i), sameNamePlaylistFiles.getPlayListName()));
            }
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onClick() {

    }

    @Override
    public void moreClick() {

    }

    @Override
    public void longClick() {

    }
}