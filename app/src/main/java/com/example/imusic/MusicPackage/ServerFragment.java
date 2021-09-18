package com.example.imusic.MusicPackage;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.imusic.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;


public class ServerFragment extends Fragment {

    public String url = null;
    public ImageView pasteBtn, searchBtn;
    public EditText enteredURL;
    NestedScrollView nestedScrollView;
    SimpleExoPlayer simpleExoPlayer;
    PlayerView playerView;

    public ServerFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server, container, false);
        pasteBtn = view.findViewById(R.id.pasteBtn);
        searchBtn = view.findViewById(R.id.searchBtn);
        enteredURL = view.findViewById(R.id.enteredURL);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        simpleExoPlayer = new SimpleExoPlayer.Builder(getContext()).build();
        playerView = view.findViewById(R.id.exoPlayerView);
        playerView.setPlayer(simpleExoPlayer);

        pasteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager manager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData pasteData = manager.getPrimaryClip();
                if (pasteData == null) {
                    Toast.makeText(getContext(), "Nothing to paste", Toast.LENGTH_SHORT).show();
                } else {
                    ClipData.Item item = pasteData.getItemAt(0);
                    enteredURL.setText(item.getText().toString());
                    url = enteredURL.getText().toString();
                }
            }
        });


        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String[] arr = url.split("https://youtu.be/");
//                if (url.length() == 2)
//                    url = arr[1];
                MediaItem mediaItem = MediaItem.fromUri(url);
                simpleExoPlayer.addMediaItem(mediaItem);
                simpleExoPlayer.prepare();
                simpleExoPlayer.play();
            }

        });

        return view;
    }

}