package com.sync.imusic;

import static com.sync.imusic.VideoAdapter.VIDEO_FILES;
import static com.sync.imusic.VideoAdapter.VIDEO_FILES_POS;

import android.content.Intent;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

public class PlaylistContentsActivity extends AppCompatActivity implements PlaylistContentListener {
    private SameNamePlaylistFiles sameNamePlaylistFiles;
    private RecyclerView recyclerView;
    private ItemTouchHelper itemTouchHelper;
    private ArrayList<PlaylistFiles> playlistFiles = new ArrayList<>();
    private PlaylistContentAdapter adapter;
    private FloatingActionButton fab;

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
        fab = findViewById(R.id.fab);
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
            fab.setOnClickListener(v -> {
                if (playlistFiles != null && playlistFiles.size() > 0) {
                    if (playlistFiles.get(0).isMusicFile) {
                        ArrayList<MusicFiles> musicFilesTemp = new ArrayList<>();
                        for (int i = 0; i < playlistFiles.size(); i++) {
                            if (playlistFiles.get(i).isMusicFile) {
                                musicFilesTemp.add(playlistFiles.get(i).getMusicFiles());
                            }
                        }
                        Intent intent = new Intent(this, PlayerActivity.class);
                        intent.putExtra("sender", "playlistAdapter");
                        intent.putExtra("listSongs", musicFilesTemp);
                        intent.putExtra("position", 0);
                        startActivity(intent);
                    } else {
                        ArrayList<VideoFiles> videoFilesTemp = new ArrayList<>();
                        for (int i = 0; i < playlistFiles.size(); i++) {
                            if (playlistFiles.get(i) != null && playlistFiles.get(i).isVideoFile)
                                videoFilesTemp.add(playlistFiles.get(i).getVideoFiles());
                        }
                        Intent intent = new Intent(this, VideoPlayerActivity.class);

                        intent.putExtra(VIDEO_FILES, videoFilesTemp);
                        intent.putExtra(VIDEO_FILES_POS, 0);
                        startActivity(intent);
                    }
                }

            });

        }
    }

            @Override
            public void onStartDrag (RecyclerView.ViewHolder viewHolder){
                itemTouchHelper.startDrag(viewHolder);
            }

            @Override
            public void onClick (ArrayList <PlaylistFiles> playlistFiles,int pos){
                int newPos = -1;
                if (playlistFiles.get(pos).isMusicFile) {
                    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
                    for (int i = 0; i < playlistFiles.size(); i++) {
                        if (playlistFiles.get(i).isMusicFile) {
                            musicFiles.add(playlistFiles.get(i).getMusicFiles());
                            if (playlistFiles.get(i).getMusicFiles().getId().equals(playlistFiles.get(pos).getMusicFiles().getId())) {
                                newPos = musicFiles.size() - 1;
                            }
                        }
                    }
                    Intent intent = new Intent(this, PlayerActivity.class);
                    intent.putExtra("sender", "playlistAdapter");
                    intent.putExtra("listSongs", musicFiles);
                    intent.putExtra("position", newPos);
                    startActivity(intent);
                } else if (playlistFiles.get(pos).isVideoFile) {
                    ArrayList<VideoFiles> videoFiles = new ArrayList<>();
                    for (int i = 0; i < playlistFiles.size(); i++) {
                        if (playlistFiles.get(i) != null && playlistFiles.get(i).isVideoFile)
                            videoFiles.add(playlistFiles.get(i).getVideoFiles());
                        if (i == pos)
                            newPos = videoFiles.size() - 1;
                    }
                    Intent intent = new Intent(this, VideoPlayerActivity.class);

                    intent.putExtra(VIDEO_FILES, videoFiles);
                    intent.putExtra(VIDEO_FILES_POS, newPos);
                    startActivity(intent);
                }
            }

            @Override
            public void moreClick (ArrayList<PlaylistFiles> playlistFiles, int pos) {
                PlaylistContentItemBottomSheet bottomSheet = new PlaylistContentItemBottomSheet();
                Bundle bundle = new Bundle();
                bundle.putSerializable("playlistFile", playlistFiles.get(pos));
                bottomSheet.setArguments(bundle);
                bottomSheet.setWindow(getWindow());
                bottomSheet.setPlaylistContentAdapter(adapter);
                bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
            }

            @Override
            public void longClick () {

            }
        }