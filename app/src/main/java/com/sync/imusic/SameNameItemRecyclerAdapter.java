package com.sync.imusic;

import static com.sync.imusic.AddToPlaylistPopup.PLAYLIST_NAME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class SameNameItemRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    //    private ArrayList<PlaylistFiles> playlistFiles;
    //    private ArrayList<String> playListNames;
//    private ArrayList<Integer> number_media;
    public ArrayList<SameNamePlaylistFiles> sameNamePlaylistFiles;
    private final RecyclerView recyclerView;
    private final PlaylistFragmentMainAdapter mainAdapter;
    private final int FIRST_HOLDER = 0;
    private final int SECOND_HOLDER = 1;
    boolean flag = false;
    private int i = 0;

    PlaylistCardListener listener;

    public ArrayList<SameNamePlaylistFiles> getSameNamePlaylistFiles() {
        return sameNamePlaylistFiles;
    }

    SameNameItemRecyclerAdapter(Context mContext, ArrayList<SameNamePlaylistFiles> sameNamePlaylistFiles, PlaylistCardListener listener, RecyclerView recyclerView, PlaylistFragmentMainAdapter mainAdapter) {
        this.mContext = mContext;
        this.listener = listener;
        this.sameNamePlaylistFiles = sameNamePlaylistFiles;
        this.recyclerView = recyclerView;
        this.mainAdapter = mainAdapter;
    }

    private ArrayList<SameNamePlaylistFiles> return_sameNamePlaylistFiles(ArrayList<PlaylistFiles> playlistFiles) {
        ArrayList<SameNamePlaylistFiles> sameNamePlaylistFiles = new ArrayList<>();
        for (int i = 0; i < playlistFiles.size(); i++) {
            if (!sameNamePlaylistFiles.contains(new SameNamePlaylistFiles(playlistFiles.get(i)))) {
                sameNamePlaylistFiles.add(new SameNamePlaylistFiles(playlistFiles.get(i)));
            } else {
                if (playlistFiles.get(i).isVideoFile || playlistFiles.get(i).isMusicFile) {
                    sameNamePlaylistFiles.get(sameNamePlaylistFiles.indexOf(new SameNamePlaylistFiles(playlistFiles.get(i)))).add(playlistFiles.get(i));
                }
            }
        }
        return sameNamePlaylistFiles;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == SECOND_HOLDER) {
            view = LayoutInflater.from(mContext).inflate(R.layout.playlist_item_recycler_view_item2, parent, false);
            return new SameNameItemRecyclerAdapterViewHolder2(view, listener);
        }
        view = LayoutInflater.from(mContext).inflate(R.layout.playlist_item_recycler_view_item, parent, false);
        return new SameNameItemRecyclerAdapterViewHolder(view, listener);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (sameNamePlaylistFiles.get(position).getSize() > 3) {
            SameNameItemRecyclerAdapterViewHolder2 tempHolder = (SameNameItemRecyclerAdapterViewHolder2) holder;
            tempHolder.playListName.setText(sameNamePlaylistFiles.get(position).getPlayListName());
            if (sameNamePlaylistFiles.get(position).getSize() != 0)
                tempHolder.no_media.setText(sameNamePlaylistFiles.get(position).getSize() + " media");
            int pos = 0;
            if (sameNamePlaylistFiles.get(position).getVideoFiles() != null) {
                for (int i = 0; i < sameNamePlaylistFiles.get(position).getVideoFiles().size() && pos < 4; i++) {
                    Glide.with(mContext)
                            .load(Uri.fromFile(new File(sameNamePlaylistFiles.get(position).getVideoFiles().get(i).getPath())))
                            .into(tempHolder.img[pos]);
                    pos++;
                }
            }

            if (pos != 4) {
                if (sameNamePlaylistFiles.get(position).getMusicFiles() != null) {
                    for (int i = 0; i < sameNamePlaylistFiles.get(position).getMusicFiles().size(); i++) {
                        MyImageLoader.from myImageLoader = new MyImageLoader.from(mContext);
                        myImageLoader.load(Long.valueOf(sameNamePlaylistFiles.get(holder.getAdapterPosition()).getMusicFiles().get(i).getId()));
                        myImageLoader.into(tempHolder.img[pos]);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            myImageLoader.setSize(new Size(120, 120));
                        }
//                        Glide.with(mContext)
//                                .load(artworkUri)
//                                .error(R.drawable.music_icon)
//                                .into(tempHolder.img[pos]);
                        pos++;
                        if (pos > 3) break;
                    }
                }
            }


        } else {
            SameNameItemRecyclerAdapterViewHolder tempHolder = (SameNameItemRecyclerAdapterViewHolder) holder;
            tempHolder.playListName.setText(sameNamePlaylistFiles.get(position).getPlayListName());
            if (sameNamePlaylistFiles.get(position).getSize() != 0)
                tempHolder.no_media.setText(sameNamePlaylistFiles.get(position).getSize() + " media");
            else
                tempHolder.no_media.setText("No media");
            boolean loaded = false;
            if (sameNamePlaylistFiles.get(position).getVideoFiles() != null && sameNamePlaylistFiles.get(position).getVideoFiles().size() > 0) {
                Glide.with(mContext)
                        .load(Uri.fromFile(new File(sameNamePlaylistFiles.get(position).getVideoFiles().get(0).getPath())))
                        .into(tempHolder.imageView);
                loaded = true;
            } else if (sameNamePlaylistFiles.get(position).getMusicFiles() != null) {
                    i = 0;
                    MyImageLoader.from myImageLoader = new MyImageLoader.from(mContext);
                    myImageLoader.load(Long.parseLong(sameNamePlaylistFiles.get(position).getMusicFiles().get(i).getId()));
                    myImageLoader.into(tempHolder.imageView);
                    myImageLoader.setOnLoadedListener(new MyImageLoader.from.OnLoadedListener() {
                        @Override
                        public void onLoaded(Bitmap bitmap) {
                            if (bitmap == null) {
                                i++;
                                if (sameNamePlaylistFiles.get(position).getMusicFiles().size() > i) {
                                    MyImageLoader.from myImageLoader = new MyImageLoader.from(mContext);
                                    myImageLoader.load(Long.parseLong(sameNamePlaylistFiles.get(position).getMusicFiles().get(i).getId()));
                                    myImageLoader.into(tempHolder.imageView);
                                }
                            }
                        }
                    });
            }
        }


//        if (sameNamePlaylistFiles.get(position).getSize() > 3) {
//
//            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(holder.relativeLayout.getWidth() / 2, holder.relativeLayout.getHeight() / 2);
//            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(holder.relativeLayout.getWidth() / 2, holder.relativeLayout.getHeight() / 2);
//            RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(holder.relativeLayout.getWidth() / 2, holder.relativeLayout.getHeight() / 2);
//            RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(holder.relativeLayout.getWidth() / 2, holder.relativeLayout.getHeight() / 2);
//
//            ImageView img1 = new ImageView(mContext);
//            img1.setId(View.generateViewId());
//            img1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.round_top_left));
//            img1.setImageResource(R.drawable.music_icon);
//            params1.setMargins(dpToPx(2),dpToPx(2),0,0);
//            img1.setClipToOutline(true);
//
//            ImageView img2 = new ImageView(mContext);
//            img2.setId(View.generateViewId());
//            img2.setImageResource(R.drawable.ic_play);
//
//            ImageView img3 = new ImageView(mContext);
//            img3.setId(View.generateViewId());
//            img3.setImageResource(R.drawable.ic_launcher_background);
//
//            ImageView img4 = new ImageView(mContext);
//            img4.setId(View.generateViewId());
//            img4.setImageResource(R.drawable.ic_launcher_foreground);
//
//            params1.addRule(RelativeLayout.ALIGN_PARENT_START);
//            params2.addRule(RelativeLayout.ALIGN_PARENT_END);
//            params3.addRule(RelativeLayout.ALIGN_PARENT_START);
//            params3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//            params4.addRule(RelativeLayout.ALIGN_PARENT_END);
//            params4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//
//            img1.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            img2.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            img3.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            img4.setScaleType(ImageView.ScaleType.CENTER_CROP);
//
//
//            holder.relativeLayout.addView(img1, params1);
//            holder.relativeLayout.addView(img2, params2);
//            holder.relativeLayout.addView(img3, params3);
//            holder.relativeLayout.addView(img4, params4);
//            img1.setVisibility(View.VISIBLE);
//            img2.setVisibility(View.VISIBLE);
//            img3.setVisibility(View.VISIBLE);
//            img4.setVisibility(View.VISIBLE);
//        }
    }

    public int dpToPx(int dpMeasure) {
        Resources r = mContext.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpMeasure,
                r.getDisplayMetrics()
        );
        return px;
    }

    @Override
    public int getItemCount() {
        return sameNamePlaylistFiles.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (sameNamePlaylistFiles.get(position).getSize() > 3)
            return SECOND_HOLDER;
        return FIRST_HOLDER;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private byte[] getAlbumArt(String uri) {
        byte[] art = null;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri);
            art = retriever.getEmbeddedPicture();
            retriever.release();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return art;
    }



    public class SameNameItemRecyclerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView playListName, no_media;
        ImageView imageView, more;
        RelativeLayout relativeLayout;
        PlaylistCardListener listener;
        FloatingActionButton fab;

        public SameNameItemRecyclerAdapterViewHolder(@NonNull View itemView, PlaylistCardListener listener) {
            super(itemView);
            this.listener = listener;
            more = itemView.findViewById(R.id.more_playlist_item_recycler_view_item);
            imageView = itemView.findViewById(R.id.img_playlist_item_recyclerView_item);
            fab = itemView.findViewById(R.id.fab);
            playListName = itemView.findViewById(R.id.playlist_name);
            playListName.setSelected(true);
            no_media = itemView.findViewById(R.id.playlist_no_items);
            itemView.setOnClickListener(this);
            more.setOnClickListener(this);
            fab.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == itemView.getId()) {
                listener.itemClick(sameNamePlaylistFiles.get(getAdapterPosition()), SameNameItemRecyclerAdapter.this, getAdapterPosition());
            }
            if (v.getId() == more.getId()) {
                listener.moreClick(sameNamePlaylistFiles.get(getAdapterPosition()), SameNameItemRecyclerAdapter.this);
            }
            if (v.getId() == fab.getId()) {
                listener.fabClick(sameNamePlaylistFiles.get(getAdapterPosition()));
            }
        }

        @Override
        public boolean onLongClick(View v) {
            listener.longPress();
            return true;
        }
    }

    public class SameNameItemRecyclerAdapterViewHolder2 extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView playListName, no_media;
        RelativeLayout relativeLayout;
        ImageView[] img = new ImageView[4];
        ImageView more;
        PlaylistCardListener listener;
        FloatingActionButton fab;

        public SameNameItemRecyclerAdapterViewHolder2(@NonNull View itemView, PlaylistCardListener listener) {
            super(itemView);
            this.listener = listener;
            playListName = itemView.findViewById(R.id.playlist_name2);
            playListName.setSelected(true);
            no_media = itemView.findViewById(R.id.playlist_no_items2);
            fab = itemView.findViewById(R.id.fab2);
            more = itemView.findViewById(R.id.more_playlist_item_recycler_view_item2);
            relativeLayout = itemView.findViewById(R.id.img_playlist_item_recyclerView_item_container2);
            img[0] = itemView.findViewById(R.id.img_playlist_item_recyclerView_item2_img1);
            img[1] = itemView.findViewById(R.id.img_playlist_item_recyclerView_item2_img2);
            img[2] = itemView.findViewById(R.id.img_playlist_item_recyclerView_item2_img3);
            img[3] = itemView.findViewById(R.id.img_playlist_item_recyclerView_item2_img4);
            itemView.setOnClickListener(this);
            more.setOnClickListener(this);
            fab.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == itemView.getId()) {
                listener.itemClick(sameNamePlaylistFiles.get(getAdapterPosition()), SameNameItemRecyclerAdapter.this, getAdapterPosition());
            }
            if (v.getId() == more.getId()) {
                listener.moreClick(sameNamePlaylistFiles.get(getAdapterPosition()), SameNameItemRecyclerAdapter.this);
            }
            if (v.getId() == fab.getId()) {
                listener.fabClick(sameNamePlaylistFiles.get(getAdapterPosition()));
            }
        }

        @Override
        public boolean onLongClick(View v) {
            listener.longPress();
            return true;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        ArrayList<PlaylistFiles> temp = getPlaylistFiles(sameNamePlaylistFiles.get(0).getPlayListName().substring(0, 1));
        ArrayList<SameNamePlaylistFiles> sameNamePlaylistFilesTemp = return_sameNamePlaylistFiles(temp);
        int prevSize = sameNamePlaylistFiles.size();
        this.sameNamePlaylistFiles = sameNamePlaylistFilesTemp;
        notifyDataSetChanged();
        if (sameNamePlaylistFilesTemp.size() > prevSize) {
            recyclerView.smoothScrollToPosition(sameNamePlaylistFiles.size() - 1);
        }

//        sameNamePlaylistFilesTemp.removeAll(sameNamePlaylistFiles);

//        if (sameNamePlaylistFilesTemp.size() > 0) {
//            sameNamePlaylistFiles.addAll(sameNamePlaylistFilesTemp);
//            notifyItemRangeInserted(sameNamePlaylistFiles.size() - sameNamePlaylistFilesTemp.size(), sameNamePlaylistFilesTemp.size());
//            recyclerView.smoothScrollToPosition(sameNamePlaylistFiles.size() - 1);
//        }
    }

    private ArrayList<PlaylistFiles> getPlaylistFiles(String character) {
        ArrayList<PlaylistFiles> temp = new ArrayList<>();
        DataBaseHelperPlaylist db = new DataBaseHelperPlaylist(mContext, PLAYLIST_NAME, null, 1);
        Cursor res = db.getAllData();
        if (res != null) {
            while (res.moveToNext()) {
                if (res.getString(13).startsWith(character)) {
                    if (res.getString(1).equals("1")) {
                        temp.add(new PlaylistFiles(new MusicFiles(
                                res.getString(3),
                                res.getString(4),
                                res.getString(5),
                                res.getString(6),
                                res.getString(7),
                                res.getString(8),
                                res.getString(9)
                        ), res.getString(13)));
                    }
                    if (res.getString(2).equals("1")) {
                        temp.add(new PlaylistFiles(new VideoFiles(
                                res.getString(8),
                                res.getString(3),
                                res.getString(4),
                                res.getString(10),
                                res.getString(9),
                                res.getString(11),
                                res.getString(7),
                                res.getString(12)
                        ), res.getString(13)));
                    }
                    if (res.getString(1).equals("0") && res.getString(2).equals("0")) {
                        temp.add(new PlaylistFiles(res.getString(13)));
                    }
                }
            }
            if (temp.size() == 0) {
                res = db.getAllData();
                while (res.moveToNext()) {
                    if (res.getString(13).startsWith(character)) {
                        temp.add(new PlaylistFiles(res.getString(13)));
                    }
                }
            }
            res.close();
        }
        db.close();
        return temp;
    }

    public void delete(String playlistName) {
        for (int i = 0; i < sameNamePlaylistFiles.size(); i++) {
            if (sameNamePlaylistFiles.get(i).getPlayListName().equals(playlistName)) {
                String startingChar = sameNamePlaylistFiles.get(i).getPlayListName().substring(0, 1);
                sameNamePlaylistFiles.remove(sameNamePlaylistFiles.get(i));
                notifyItemRemoved(i);
                DataBaseHelperPlaylist db = new DataBaseHelperPlaylist(mContext, PLAYLIST_NAME, null, 1);
                db.deleteForPlaylistName(playlistName);
                db.close();
                if (sameNamePlaylistFiles.size() == 0) {
                    mainAdapter.delete(startingChar);
                }
                break;
            }
        }
    }
}
