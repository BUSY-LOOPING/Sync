package com.example.imusic;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PlaylistCardBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    private Context context;
    private TextView name;
    private LinearLayout opt1, opt2, opt3, opt4, opt5, opt6, opt7;
    private SameNamePlaylistFiles playlistFile;
    private SameNameItemRecyclerAdapter adapter;

    public void setPlaylistFile(SameNamePlaylistFiles playlistFile) {
        this.playlistFile = playlistFile;
    }

    public void setAdapter(SameNameItemRecyclerAdapter adapter) {
        this.adapter = adapter;
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
        switch (id) {
            case R.id.option1:
                break;
            case R.id.option2:
                break;
            case R.id.option3:
                break;
            case R.id.option4:
                break;
            case R.id.option5:
                break;
            case R.id.option6:
                break;
            case R.id.option7:
                adapter.delete(playlistFile.getPlayListName());
                break;
        }
        dismiss();
    }
}
