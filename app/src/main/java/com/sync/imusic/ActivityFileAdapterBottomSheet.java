package com.sync.imusic;

import static com.sync.imusic.FileActivity.PARENT;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sync.imusic.fragment.BrowseFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActivityFileAdapterBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    private Context context;
    private TextView name, fav_txt;
    private ImageView fav_icon;
    private LinearLayout opt1, opt2, opt3, opt4;
    private File file;
    private DataBaseHelper mydb;
    private boolean isFav = false, isMusicFile, isVideoFile;
    private MusicFiles musicFile;
    private VideoFiles videoFile;
    private ActivityFileAdapter adapter;
    private int pos = 0;
    private List<File> list;

    public void setList(List<File> list) {
        this.list = list;
    }

    public void setAdapter(ActivityFileAdapter adapter) {
        this.adapter = adapter;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public void setFile(File file) {
        this.file = file;
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
        View view = inflater.inflate(R.layout.activity_file_bottom_sheet, container, false);
        Bundle bundle = this.getArguments();
        init(view, bundle);
        listeners();
        return view;
    }

    private void init(View view, Bundle arguments) {
        if (arguments != null)
            file = (File) arguments.getSerializable("file_bottom_sheet");
        mydb = new DataBaseHelper(context, "favourite.db", null, 1);
        isFav = mydb.isFavourite(file);
        name = view.findViewById(R.id.name_bottom_sheet);
        opt1 = view.findViewById(R.id.option1);
        opt2 = view.findViewById(R.id.option2);
        opt3 = view.findViewById(R.id.option3);
        opt4 = view.findViewById(R.id.option4);
        name.setText(file.getName());
        fav_txt = view.findViewById(R.id.add_to_fav_txt);
        fav_icon = view.findViewById(R.id.add_to_fav_img);
        isMusicFile = ActivityFileAdapter.isMusicFile(file.getAbsolutePath());
        isVideoFile = ActivityFileAdapter.isVideoFile(file.getAbsolutePath());
        if (!(isMusicFile || isVideoFile)) {
            opt1.setVisibility(View.GONE);
        } else {
            if (isMusicFile) {
                musicFile = ActivityFileAdapter.getMusicFileForPath(file, context);
            } else {
                videoFile = ActivityFileAdapter.getVideoFileForPath(file, context);
            }
        }
        if (!file.isDirectory()) {
            opt2.setVisibility(View.GONE);
            opt3.setVisibility(View.GONE);
        }

        if (isFav) {
            fav_icon.setImageResource(R.drawable.ic_baseline_star_border);
            fav_txt.setText("Remove from favorites");
        }
    }

    private void listeners() {
        opt1.setOnClickListener(this);
        opt2.setOnClickListener(this);
        opt3.setOnClickListener(this);
        opt4.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.option1:
                if (isMusicFile) {
                    ArrayList<MusicFiles> arrayList = new ArrayList<>();
                    arrayList.add(musicFile);
                    Intent intent = new Intent(context, PlayerActivity.class);
                    intent.putExtra("sender", "fileActivity");
                    intent.putExtra("listSongs", arrayList);
                    intent.putExtra("position", 0);
                    startActivity(intent);
                } else if (isVideoFile) {
                    ArrayList<VideoFiles> arrayList = new ArrayList<>();
                    arrayList.add(videoFile);
                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                    intent.putExtra(VIDEO_FILES, arrayList);
                    intent.putExtra(VIDEO_FILES_POS, 0);
                    startActivity(intent);
                }
                break;
            case R.id.option2:
                AddToPlaylistPopup addToPlaylistPopup = new AddToPlaylistPopup();
                if (isMusicFile) {
                    ArrayList<MusicFiles> arrayList = new ArrayList<>();
                    arrayList.add(musicFile);
                    addToPlaylistPopup.addMusicFiles(arrayList);
                }
                else if (isVideoFile) {
                    ArrayList<VideoFiles> arrayList = new ArrayList<>();
                    arrayList.add(videoFile);
                    addToPlaylistPopup.addVideoFiles(arrayList);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(PARENT, file);
                    addToPlaylistPopup.setArguments(bundle);
                }
                addToPlaylistPopup.show(((FileActivity) context).getSupportFragmentManager(), addToPlaylistPopup.getTag());
                break;
            case R.id.option3:
                if (isFav) {
                    mydb.deleteData(file.getAbsolutePath());
                    BrowseFragment.browseAdapter.removeFile(file);
                } else {
                    if (mydb.insertData(file.getName(), file.getPath(), file.getAbsolutePath())) {
                        BrowseFragment.browseAdapter.updateFile(file);
                    }
                }
                break;
            case R.id.option4:
                DeleteBottomSheet deleteBottomSheet = new DeleteBottomSheet();
                Bundle bundle = new Bundle();
                bundle.putSerializable("file", file);
                deleteBottomSheet.setArguments(bundle);
                deleteBottomSheet.setActivityFileAdapter(adapter);
                deleteBottomSheet.setList(list);
                deleteBottomSheet.setPos(pos);
                deleteBottomSheet.show(((FileActivity) context).getSupportFragmentManager(), deleteBottomSheet.getTag());
                break;
        }
        dismiss();
    }
}
