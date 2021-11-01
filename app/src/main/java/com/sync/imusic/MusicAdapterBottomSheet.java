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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MusicAdapterBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    private Context context;
    private TextView name;
    private LinearLayout opt1, opt2, opt3, opt4, opt5, opt6, opt7, opt8, opt9;
    private ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    int position = 0;
    private Window rootWindow;
    private MusicAdapter musicAdapter;
    private AlbumDetailsAdapter albumDetailsAdapter;

    public void setAlbumDetailsAdapter(AlbumDetailsAdapter albumDetailsAdapter) {
        this.albumDetailsAdapter = albumDetailsAdapter;
    }

    public void setMusicAdapter(MusicAdapter musicAdapter) {
        this.musicAdapter = musicAdapter;
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
        View view = inflater.inflate(R.layout.music_adapter_bottom_sheet, container, false);
        Bundle bundle = this.getArguments();
        init(view, bundle);
        listeners();
        return view;
    }

    private void init(View view, Bundle arguments) {
        if (arguments != null) {
            musicFiles = (ArrayList<MusicFiles>) arguments.getSerializable("arraylist");
            position = arguments.getInt("position");
        }
        name = view.findViewById(R.id.name_bottom_sheet);
        opt1 = view.findViewById(R.id.option1);
        opt2 = view.findViewById(R.id.option2);
        opt3 = view.findViewById(R.id.option3);
        opt4 = view.findViewById(R.id.option4);
        opt5 = view.findViewById(R.id.option5);
        opt6 = view.findViewById(R.id.option6);
        opt7 = view.findViewById(R.id.option7);
        opt8 = view.findViewById(R.id.option8);
        opt9 = view.findViewById(R.id.option9);

        name.setText(musicFiles.get(position).getTitle());

    }

    private void listeners() {
        opt1.setOnClickListener(this);
        opt2.setOnClickListener(this);
        opt3.setOnClickListener(this);
        opt4.setOnClickListener(this);
        opt5.setOnClickListener(this);
        opt6.setOnClickListener(this);
        opt7.setOnClickListener(this);
        opt8.setOnClickListener(this);
        opt9.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        ArrayList<MusicFiles> temp = new ArrayList<>();
        switch (id) {
            case R.id.option1:
                intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("sender", "bottomSheet");
                intent.putExtra("listSongs", musicFiles);
                intent.putExtra("position", position);
                startActivity(intent);
                break;
            case R.id.option2:
                temp.add(musicFiles.get(position));
                PlayerActivity.append(temp);
                break;
            case R.id.option3:
                intent = new Intent(context, InfoActivity.class);
                intent.putExtra("musicFilePlayerAct", musicFiles);
                intent.putExtra("posPlayerAct", position);
                startActivity(intent);
                break;
            case R.id.option4:
                temp.add(musicFiles.get(position));
                PlayerActivity.insertNext(temp);
                break;
            case R.id.option5:
                AddToPlaylistPopup popup = new AddToPlaylistPopup();
                temp.add(musicFiles.get(position));
                popup.addMusicFiles(temp);
                popup.show(((MainActivity) context).getSupportFragmentManager(), popup.getTag());
                break;
            case R.id.option6:
                ApplicationClass.setAsRingtone(rootWindow, context, musicFiles.get(position));
                break;
            case R.id.option7:
                DeleteBottomSheet deleteBottomSheet = new DeleteBottomSheet();
                Bundle bundle = new Bundle();
                bundle.putSerializable("musicFile", musicFiles.get(position));
                deleteBottomSheet.setDeleteListener(new DeleteBottomSheet.DeleteListener() {
                    @Override
                    public void deleted() {
                        MusicAdapter.remove(musicFiles.get(position));
                    }
                });
                deleteBottomSheet.setArguments(bundle);
                deleteBottomSheet.show(getParentFragmentManager(), deleteBottomSheet.getTag());
                break;
            case R.id.option8:
                ApplicationClass.share(context, musicFiles.get(position));
                break;
            case R.id.option9:
                intent = new Intent(context, FileActivity.class);
                List<File> tempList = new ArrayList<>();
                tempList.add(new File(musicFiles.get(position).getPath()).getParentFile());
                intent.putExtra("parent", (Serializable) tempList);
                startActivity(intent);
                break;

        }
        dismiss();
    }
}
