package com.sync.imusic;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PlaylistContentItemBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    private Context context;
    private TextView name;
    private LinearLayout opt1, opt2, opt3, opt4, opt5, opt6, opt7;
    private PlaylistFiles playlistFile;
    private MusicFiles musicFile;
    private VideoFiles videoFile;
    private boolean isMusicFile = false;
    private Window rootWindow;
    private PlaylistContentAdapter playlistContentAdapter;

    public void setPlaylistContentAdapter(PlaylistContentAdapter playlistContentAdapter) {
        this.playlistContentAdapter = playlistContentAdapter;
    }

    public void setWindow(Window window) {
        this.rootWindow = window;
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
        View view = inflater.inflate(R.layout.playlist_content_bottom_sheet, container, false);
        Bundle bundle = this.getArguments();
        init(view, bundle);
        listeners();
        return view;
    }

    private void init(View view, Bundle arguments) {
        if (arguments != null) {
            playlistFile = (PlaylistFiles) arguments.getSerializable("playlistFile");
            if (playlistFile != null) {
                isMusicFile = playlistFile.isMusicFile;
                if (isMusicFile) {
                    musicFile = playlistFile.getMusicFiles();
                } else {
                    videoFile = playlistFile.getVideoFiles();
                }
            }
        }
        name = view.findViewById(R.id.name_bottom_sheet);
        opt1 = view.findViewById(R.id.option1);
        opt2 = view.findViewById(R.id.option2);
        opt3 = view.findViewById(R.id.option3);
        opt4 = view.findViewById(R.id.option4);
        opt5 = view.findViewById(R.id.option5);
        opt6 = view.findViewById(R.id.option6);
        opt7 = view.findViewById(R.id.option7);

        if (isMusicFile) {
            name.setText(musicFile.getTitle());
        } else {
            opt5.setVisibility(View.GONE);
            name.setText(videoFile.getTitle());
        }
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
        ArrayList<MusicFiles> musicFilesArrayList = new ArrayList<>();
        ArrayList<VideoFiles> videoFilesArrayList = new ArrayList<>();
        if (isMusicFile) {
            musicFilesArrayList.add(musicFile);
        } else {
            videoFilesArrayList.add(videoFile);
        }
        switch (id) {
            case R.id.option1:
                if (isMusicFile) {
                    PlayerActivity.append(musicFilesArrayList);
                } else {
                    VideoPlayerActivity.append(videoFilesArrayList);
                }
                break;
            case R.id.option2:
                if (isMusicFile) {
                    Intent intent = new Intent(context, InfoActivity.class);
                    intent.putExtra("musicFilePlayerAct", musicFilesArrayList);
                    intent.putExtra("posPlayerAct", 0);
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, InfoActivityVideo.class);
                    intent.putExtra(InfoActivityVideo.VIDEO_FILE_INFO_ACT, videoFile);
                    context.startActivity(intent);
                }
                break;
            case R.id.option3:
                if (isMusicFile) {
                    PlayerActivity.insertNext(musicFilesArrayList);
                }
                break;
            case R.id.option4:
                AddToPlaylistPopup popup = new AddToPlaylistPopup();
                if (isMusicFile) {
                    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
                    musicFiles.add(musicFile);
                    popup.addMusicFiles(musicFiles);
                } else {
                    ArrayList<VideoFiles> videoFiles = new ArrayList<>();
                    videoFiles.add(videoFile);
                    popup.addVideoFiles(videoFiles);
                }
                popup.show(((PlaylistContentsActivity) context).getSupportFragmentManager(), popup.getTag());
                break;
            case R.id.option5:
                ApplicationClass.setAsRingtone(rootWindow, context, musicFile);
                break;
            case R.id.option6:
                DeleteBottomSheet deleteBottomSheet = new DeleteBottomSheet();
                Bundle bundle = new Bundle();
                if (isMusicFile) {
                    bundle.putSerializable("musicFile", musicFile);
                } else {
                    bundle.putSerializable("videoFile", videoFile);
                }
                deleteBottomSheet.setDeleteListener(new DeleteBottomSheet.DeleteListener() {
                    @Override
                    public void deleted() {
                        if (playlistContentAdapter != null) {
                            int index = 0;
                            for (PlaylistFiles file : playlistContentAdapter.playlistFiles) {
                                if (file.isMusicFile) {
                                    if (playlistFile.isMusicFile && file.getMusicFiles().getId().equals(playlistFile.getMusicFiles().getId())) {
                                        break;
                                    } else index++;
                                } else {
                                    if (playlistFile.isVideoFile && file.getVideoFiles().getId().equals(playlistFile.getVideoFiles().getId())) {
                                        break;
                                    } else index++;
                                }
                            }
                            if (index < playlistContentAdapter.playlistFiles.size()) {
                                playlistContentAdapter.playlistFiles.remove(index);
                                playlistContentAdapter.notifyItemRemoved(index);
                            }
                        }
                    }
                });
                deleteBottomSheet.setArguments(bundle);
                deleteBottomSheet.show(((PlaylistContentsActivity) context).getSupportFragmentManager(), deleteBottomSheet.getTag());
                break;
            case R.id.option7:
                if (isMusicFile) {
                    ApplicationClass.share(context, musicFile);
                } else {
                    ApplicationClass.share(context, videoFile);
                }
                break;

        }
        dismiss();
    }
}
