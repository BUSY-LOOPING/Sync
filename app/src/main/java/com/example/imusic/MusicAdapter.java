package com.example.imusic;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.LruCache;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.taishi.library.Indicator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> { //our custom viewholder

    private Context mContext;
    public static ArrayList<MusicFiles> mFiles;
    private ArrayList<MusicFiles> selectedList;
    private String now_playing_id;
    private OnItemClickListenerMusicAdapter listener;
    private int lastPosition = -1;
    private RecyclerView recyclerView;
    private int prev_played_pos = -1;
    private ActionMode actionMode;
    private MenuItem browseParentMenuItem, setAsRingtoneMenuItem, infoMenuItem;
    private boolean isSelected = false;
    private ArrayList<Integer> positions;

    private Bitmap bitmap = null;
    private LruCache<Long, Bitmap> mBitmapCache;
    private BitmapDrawable mPlaceholder;

    public MusicAdapter() {

    }

    public MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles, OnItemClickListenerMusicAdapter listener, RecyclerView recyclerView) {
        this.mContext = mContext;
        MusicAdapter.mFiles = mFiles;
        this.listener = listener;
        this.recyclerView = recyclerView;
        selectedList = new ArrayList<>();
        positions = new ArrayList<>();

        mPlaceholder = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.music_icon);
        int maxSize = (int) (Runtime.getRuntime().maxMemory() * 1024);
        int cacheSize = maxSize / 8;
        if (cacheSize == 0) cacheSize = 10;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        int availMemInBytes = am.getMemoryClass() * 1024 * 1024;
        mBitmapCache = new LruCache<Long, Bitmap>(availMemInBytes) {
            @Override
            protected int sizeOf(Long key, Bitmap value) {
//                return value.getByteCount() * 1024;
                return value.getRowBytes() * value.getHeight();
            }
        };
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyViewHolder(view, listener);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
//        holder.itemView.clearAnimation();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
//        setAnimation(holder.itemView, position);
        if (holder.checkBox.getVisibility() == View.VISIBLE) {
            if (!isSelected || !selectedList.contains(mFiles.get(position))) {
                clickItemWhenChecked(holder, position);
            }
        } else {
            if (isSelected && selectedList.contains(mFiles.get(position))) {
                clickItemWhenChecked(holder, position);
            }
        }

        if (holder.indicator.getVisibility() == View.VISIBLE) {
            holder.indicator.setVisibility(View.GONE);
            holder.album_art.setAlpha(1f);
        }
        if (now_playing_id != null) {
            if (mFiles.get(position).getId().equals(now_playing_id)) {
                holder.indicator.setVisibility(View.VISIBLE);
                holder.album_art.setAlpha(0.5f);
                prev_played_pos = position;
            }
        }
        holder.file_name.setText(mFiles.get(position).getTitle());
        String artist = mFiles.get(position).getArtist();
        String album = mFiles.get(position).getAlbum();
        holder.artist_album.setText(new StringBuilder().append(artist).append("  ·  ").append(album).toString());

//        final Cursor mCursor = mContext.getContentResolver().query(
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                new String[]{MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID}, null, null,
//                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");
//        long album_id=mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
//        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
//        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, album_id);
//        Uri uri=Uri.parse(mFiles.get(position).getAlbum());
//        try {
//            bitmap =  MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
//            holder.album_art.setImageBitmap(bitmap);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Uri artworkUri = Uri.parse("content://media/external/audio/media/" + mFiles.get(position).getId() + "/albumart");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Glide.with(mContext)
                    .load(artworkUri)
                    .error(R.drawable.music_icon)
                    .into(holder.album_art);
        } else {
            final Bitmap bitmap = mBitmapCache.get(Long.valueOf(mFiles.get(position).getId()));
            if (bitmap != null) {
                holder.album_art.setImageBitmap(bitmap);
            } else {
                loadAlbumArt(holder.album_art, Long.valueOf(mFiles.get(position).getId()));
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelected) {
                    if (infoMenuItem != null) {
                        infoMenuItem.setVisible(selectedList.size() <= 1);
                    }
                    if (selectedList.size() == 1 && mFiles.get(position).getId().equals(selectedList.get(0).getId())) {
                        actionMode.finish();
                    }
                    clickItemWhenChecked(holder, position);
                } else {
                    Intent intent = new Intent(mContext, PlayerActivity.class);
                    intent.putExtra("sender", "mainActivity");
                    intent.putExtra("position", position);
                    mContext.startActivity(intent);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isSelected) {
                    ActionMode.Callback callback = new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            MusicAdapter.this.actionMode = mode;
                            mode.getMenuInflater().inflate(R.menu.contextual_menu_music_adapter, menu);
                            browseParentMenuItem = menu.findItem(R.id.browse_parent_contextual_menu_musicAdapter);
                            setAsRingtoneMenuItem = menu.findItem(R.id.setAsRingtone_contextual_menu_musicAdapter);
                            infoMenuItem = menu.findItem(R.id.info_contextual_menu_music_adapter);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            isSelected = true;
                            clickItemWhenChecked(holder, position);
                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            SharedPreferences.Editor editor = mContext.getSharedPreferences("back_pressed", Context.MODE_PRIVATE).edit();
                            editor.clear();
                            editor.apply();
                            int id = item.getItemId();
                            browseParentMenuItem.setVisible(selectedList.size() <= 1);
                            setAsRingtoneMenuItem.setVisible(selectedList.size() <= 1);
                            switch (id) {
                                case R.id.info_contextual_menu_music_adapter:
                                    infoBtnClicked(positions.get(0));
                                    break;
                                case R.id.share_contextual_menu_music_adapter:
                                    shareBtnClicked(positions);
                                    break;
                                case R.id.play_contextual_menu_music_adapter:
                                    break;
                                case R.id.add_to_playlist_contextual_menu_musicAdapter:
                                    break;
                                case R.id.delete_contextual_menu_musicAdapter:
                                    break;
                                case R.id.setAsRingtone_contextual_menu_musicAdapter:
                                    break;
                                case R.id.browse_parent_contextual_menu_musicAdapter:
                                    Intent intent = new Intent(mContext, FileActivity.class);
                                    List<File> tempList = new ArrayList<>();
                                    tempList.add(new File(selectedList.get(0).getPath()).getParentFile());
                                    intent.putExtra("parent", (Serializable) tempList);
                                    mContext.startActivity(intent);
                                    break;
                            }
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            isSelected = false;
                            selectedList.clear();
                            for (int i = 0; i < positions.size(); i++) {
                                notifyItemChanged(positions.get(i));
                            }
                            positions.clear();
                            actionMode = null;
                        }
                    };
                    ((AppCompatActivity) mContext).startSupportActionMode(callback);
                } else {
                    if (infoMenuItem != null && selectedList.size() > 1) {
                        infoMenuItem.setVisible(false);
                    }
                    if (selectedList.size() == 1 && mFiles.get(position).getId().equals(selectedList.get(0).getId())) {
                        actionMode.finish();
                    }
                    clickItemWhenChecked(holder, position);
                }
                return true;
            }
        });

    }

    private void shareBtnClicked(ArrayList<Integer> positions) {
        Toast.makeText(mContext, "selected = " + mFiles.get(positions.get(0)).getTitle(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
//        intent.putExtra(Intent.EXTRA_TITLE, positions.size() > 1 ? mFiles.get(positions.get(0)).getTitle() + " + " + positions.size() + " more" : mFiles.get(positions.get(0)).getTitle());
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("*/*");
//        String[] mimetypes = new String[positions.size()];
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            intent.putExtra(Intent.EXTRA_MIME_TYPES, );
//        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        ArrayList<Uri> files = new ArrayList<Uri>();

        for (int i = 0; i < positions.size(); i++) {
            File file = new File(mFiles.get(positions.get(i)).getPath());
            Uri uri = Uri.fromFile(file);
            files.add(uri);
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        mContext.startActivity(intent);
    }

    private void infoBtnClicked(int position) {
        Intent intent = new Intent(mContext, InfoActivity.class);
        intent.putExtra("musicFilePlayerAct", mFiles);
        intent.putExtra("posPlayerAct", position);
        mContext.startActivity(intent);
    }

    private void clickItemWhenChecked(MyViewHolder holder, int position) {
        if (holder.checkBox.getVisibility() == View.GONE) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.color.ripple_color_light);
//            if (holder.getAdapterPosition() >= 0) {
                if (!selectedList.contains(mFiles.get(position))) {
                    selectedList.add(mFiles.get(position));
                    positions.add(position);
                }
//            }
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setBackgroundResource(R.drawable.custom_ripple);
//            if (holder.getAdapterPosition() >= 0) {
//                selectedList.remove(mFiles.get(holder.getAdapterPosition()));
//                positions.remove((Integer) holder.getAdapterPosition());
//            }
            selectedList.remove(mFiles.get(position));
            positions.remove((Integer) position);
        }
        if (infoMenuItem != null) {
            infoMenuItem.setVisible(selectedList.size() == 1);
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

//    @Override
//    public long getItemId(int position) {
//        return mFiles.get(position).hashCode();
//    }
//
//    @Override
//    public int getItemViewType(int position) {  //this method is v important
//        return position;
//    }

    private void loadAlbumArt(ImageView icon, Long id) {
        if (cancelLoadTask(icon, id)) {
            LoadAlbumArt loadAlbumArt = new LoadAlbumArt(icon, mContext);
            AsyncDrawable drawable = new AsyncDrawable(mContext.getResources(), mPlaceholder.getBitmap(), loadAlbumArt);
            loadAlbumArt.execute(id);
            icon.setImageDrawable(drawable);
        }
    }

    public boolean cancelLoadTask(ImageView icon, long id) {
        LoadAlbumArt loadAlbumArt = (LoadAlbumArt) getLoadTask(icon);
        if (loadAlbumArt == null) {
            return true;
        }
        if (loadAlbumArt.id != id) {
            loadAlbumArt.cancel(true);
            return true;
        }

        return false;
    }

    public AsyncTask getLoadTask(ImageView icon) {
        LoadAlbumArt task = null;
        Drawable drawable = icon.getDrawable();
        if (drawable instanceof AsyncDrawable) {
            task = ((AsyncDrawable) drawable).getLoadArtWorkTask();
        }
        return task;
    }

    private class LoadAlbumArt extends AsyncTask<Long, Void, Bitmap> {
        public WeakReference<ImageView> mIcon;
        public Long id = 0L;
        private Context mContext;

        public LoadAlbumArt(ImageView icon, Context mContext) {
            mIcon = new WeakReference<ImageView>(icon);
            this.mContext = mContext;
        }

        @Override
        protected Bitmap doInBackground(Long... params) {
            id = params[0];
            Uri artworkUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
            Bitmap bmp = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    bmp = mContext.getContentResolver().loadThumbnail(
                            artworkUri,
                            new Size(120, 120),
                            null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled() || bitmap == null) {
                return;
            }
            if (mIcon != null && mIcon.get() != null) {
                ImageView icon = mIcon.get();
                Drawable drawable = icon.getDrawable();
                if (drawable instanceof AsyncDrawable) {
                    LoadAlbumArt task = ((AsyncDrawable) drawable).getLoadArtWorkTask();
                    if (task != null && task == this) {
                        icon.setImageBitmap(bitmap);
                    }
                }
            }
            mBitmapCache.put(id, bitmap);
            super.onPostExecute(bitmap);
        }
    }

    private static class AsyncDrawable extends BitmapDrawable {
        WeakReference<LoadAlbumArt> loadAlbumArtWeakReference;

        public AsyncDrawable(Resources resources, Bitmap bitmap, LoadAlbumArt task) {
            super(resources, bitmap);
            loadAlbumArtWeakReference = new WeakReference<LoadAlbumArt>(task);
        }

        public LoadAlbumArt getLoadArtWorkTask() {
            return loadAlbumArtWeakReference.get();
        }
    }


    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.item_animation_fall_down);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView file_name, artist_album;
        ImageView album_art, menuMore, checkBox;
        Indicator indicator;
        OnItemClickListenerMusicAdapter listener;

        public MyViewHolder(@NonNull View itemView, OnItemClickListenerMusicAdapter listener) {
            super(itemView);
            this.listener = listener;
            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);
            checkBox = itemView.findViewById(R.id.album_art_music_item_item_selected);
            artist_album = itemView.findViewById(R.id.artist_album);
            indicator = itemView.findViewById(R.id.music_indicator);
//            itemView.setOnClickListener(this);
            menuMore.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.menuMore) {
                listener.onMoreClick(getAdapterPosition(), v);
            }
        }
    }

    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }

        return hasImage;
    }


    public void updateList() {
        notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }


    public void updateList(ArrayList<MusicFiles> musicFilesArrayList) {
//        mFiles = new ArrayList<>();
////        mFiles.addAll(musicFilesArrayList);
        mFiles = musicFilesArrayList;
        notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    public void updateNowPlaying(String now_playing_id, int prevPlayedPos) {
        this.now_playing_id = now_playing_id;
        for (int i = 0; i < mFiles.size(); i++) {
            if (mFiles.get(i).getId().equals(now_playing_id)) {
                notifyItemChanged(i);
                break;
            }
        }
        if (prevPlayedPos != -1) notifyItemChanged(prevPlayedPos);
        if (this.prev_played_pos != -1) notifyItemChanged(this.prev_played_pos);
    }

    public void delete(int position) {
        MusicFiles temp = mFiles.get(position);
        mFiles.remove(position);
        MainActivity.musicFiles.remove(temp);
    }

    public void refresh(ArrayList<MusicFiles> musicFiles) {
        mFiles = musicFiles;
        notifyItemRangeChanged(0, mFiles.size());
    }

    // Function to remove duplicates from an ArrayList
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {

        // Create a new LinkedHashSet
        Set<T> set = new LinkedHashSet<>();

        // Add the elements to set
        set.addAll(list);

        // Clear the list
        list.clear();

        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);

        // return the list
        return list;
    }

    public ArrayList<MusicFiles> getFiles() {
        return mFiles;
    }

}
