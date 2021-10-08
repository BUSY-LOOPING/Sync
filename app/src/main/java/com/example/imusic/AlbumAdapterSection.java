package com.example.imusic;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

public class AlbumAdapterSection extends Section {
    private String title;
    private final ArrayList<MusicFiles> albumFiles;
    private final Context mContext;


    public AlbumAdapterSection(Context context, String title, ArrayList<MusicFiles> albumFiles) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.album_item)
                .headerResourceId(R.layout.album_item_decorator)
                .build());
        mContext = context;
        this.title = title;
        this.albumFiles = albumFiles;
    }

    @Override
    public int getContentItemsTotal() {
        return albumFiles.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new AlbumAdapterItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        AlbumAdapterItemViewHolder itemHolder = (AlbumAdapterItemViewHolder) holder;
        itemHolder.albumName.setText(albumFiles.get(position).getAlbum());
        itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AlbumDetails.class);
                intent.putExtra("albumName",albumFiles.get(position).getAlbum());
                mContext.startActivity(intent);
            }
        });

        MyImageLoader.from loader = new MyImageLoader.from(mContext);
        loader.load(Long.parseLong(albumFiles.get(position).getId()));
        loader.into(itemHolder.albumArt);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loader.setSize(new Size(190, 190));
        }
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        AlbumAdapterHeaderViewHolder headerViewHolder = (AlbumAdapterHeaderViewHolder) holder;
//        if (title.startsWith("<") || title.toLowerCase().startsWith("unknown")) {
//            title = "#";
//        }
        headerViewHolder.txt.setText(title);
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new AlbumAdapterHeaderViewHolder(view);
    }

    public class AlbumAdapterItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView albumName;
        private final ImageView albumArt, moreBtn;

        public AlbumAdapterItemViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.album_name);
            albumName.setSelected(true);
            albumArt = itemView.findViewById(R.id.album_image);
            moreBtn = itemView.findViewById(R.id.more_album_items);
        }
    }

    public class AlbumAdapterHeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView txt;

        public AlbumAdapterHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            txt = itemView.findViewById(R.id.header_title);
        }
    }
}
