package com.sync.imusic;

import static com.sync.imusic.VideoAdapter.VIDEO_FILES;
import static com.sync.imusic.VideoAdapter.VIDEO_FILES_POS;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PlaylistCardBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    private Context context;
    private TextView name;
    private LinearLayout opt1, opt2, opt3, opt4, opt5, opt6, opt7;
    private SameNamePlaylistFiles playlistFile;
    private SameNameItemRecyclerAdapter adapter;
    private ArrayList<SameNamePlaylistFiles> arrayList;

    public void setPlaylistFile(SameNamePlaylistFiles playlistFile) {
        this.playlistFile = playlistFile;
    }

    public void setAdapter(SameNameItemRecyclerAdapter adapter) {
        this.adapter = adapter;
        arrayList = adapter.getSameNamePlaylistFiles();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_card_bottom_sheet, container, false);
        init(view);
        set();
        listeners();
        return view;
    }

    private void init(View view) {
        name = view.findViewById(R.id.name_bottom_sheet);
        opt1 = view.findViewById(R.id.option1);
        opt2 = view.findViewById(R.id.option2);
        opt3 = view.findViewById(R.id.option3);
        opt4 = view.findViewById(R.id.option4);
        opt5 = view.findViewById(R.id.option5);
        opt6 = view.findViewById(R.id.option6);
        opt7 = view.findViewById(R.id.option7);
    }

    private void set() {
        name.setText(playlistFile.getPlayListName());
    }

    private void listeners() {
        opt1.setOnClickListener(this);
        opt2.setOnClickListener(this);
        opt3.setOnClickListener(this);
        opt4.setOnClickListener(this);
        opt5.setOnClickListener(this);
        opt6.setOnClickListener(this);
        opt7.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        ArrayList<PlaylistFiles> playlistFiles = new ArrayList<>();
        String playlistName = playlistFile.getPlayListName();
        ArrayList<MusicFiles> musicFiles = playlistFile.getMusicFiles();
        ArrayList<VideoFiles> videoFiles = playlistFile.getVideoFiles();

        switch (id) {
            case R.id.option1:
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
                        Intent intent = new Intent(context, PlayerActivity.class);
                        intent.putExtra("sender", "playlistAdapter");
                        intent.putExtra("listSongs", musicFilesTemp);
                        intent.putExtra("position", 0);
                        context.startActivity(intent);
                    } else {
                        ArrayList<VideoFiles> videoFilesTemp = new ArrayList<>();
                        for (int i = 0; i < playlistFiles.size(); i++) {
                            if (playlistFiles.get(i) != null && playlistFiles.get(i).isVideoFile)
                                videoFilesTemp.add(playlistFiles.get(i).getVideoFiles());
                        }
                        Intent intent = new Intent(context, VideoPlayerActivity.class);

                        intent.putExtra(VIDEO_FILES, videoFilesTemp);
                        intent.putExtra(VIDEO_FILES_POS, 0);
                        context.startActivity(intent);
                    }
                } else {
                    Toast.makeText(context, "No files in playlist", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.option2:
                if (musicFiles == null && videoFiles == null) {
                    return;
                }
                PlayerActivity.append(musicFiles);
                if (musicFiles == null || musicFiles.size() == 0) {
                    VideoPlayerActivity.append(videoFiles);
                }

                break;
            case R.id.option3:
                if (musicFiles == null || videoFiles == null || (musicFiles.size() == 0 && videoFiles.size() == 0))
                    return;
                MainActivity.shuffleBoolean = true;
                if ((PlayerActivity.listSongs == null || PlayerActivity.listSongs.size() == 0) && musicFiles.size() > 0) {
                    Intent intent = new Intent(getContext(), PlayerActivity.class);
                    intent.putExtra("sender", "playlistAdapter");
                    intent.putExtra("listSongs", musicFiles);
                    intent.putExtra("position", PlayerActivity.getRandomNumber(musicFiles.size() - 1));
                    startActivity(intent);
                }
                break;
            case R.id.option4:
                break;
            case R.id.option5:
                if (musicFiles == null || videoFiles == null || (musicFiles.size() == 0 && videoFiles.size() == 0))
                    return;
                if (musicFiles.size() > 0)
                    PlayerActivity.insertNext(musicFiles);
//                if ((PlayerActivity.listSongs == null || PlayerActivity.listSongs.size() == 0) && musicFiles.size() > 0) {
//                    Intent intent = new Intent(getContext(), PlayerActivity.class);
//                    intent.putExtra("sender", "playlistAdapter");
//                    intent.putExtra("listSongs", musicFiles);
//                    intent.putExtra("position", 0);
//                    startActivity(intent);
//                } else {
//                    int pos = PlayerActivity.position + 1;
//
//                    for (int i = 0;i < musicFiles.size(); i ++) {
//                        PlayerActivity.listSongs.add(pos, musicFiles.get(i));
//                        pos++;
//                    }
//                }
                break;
            case R.id.option6:
                AddToPlaylistPopup addToPlaylistPopup = new AddToPlaylistPopup();
                if (musicFiles != null)
                    addToPlaylistPopup.addMusicFiles(musicFiles);
                if (videoFiles != null)
                    addToPlaylistPopup.addVideoFiles(videoFiles);
                addToPlaylistPopup.show(((MainActivity) context).getSupportFragmentManager(), addToPlaylistPopup.getTag());
                break;
            case R.id.option7:
                adapter.delete(playlistFile.getPlayListName());
                break;
        }
        dismiss();
    }
}
