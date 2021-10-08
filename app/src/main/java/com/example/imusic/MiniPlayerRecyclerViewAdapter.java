package com.example.imusic;

import static android.content.Context.MODE_PRIVATE;
import static com.example.imusic.MusicService.MUSIC_ID;
import static com.example.imusic.MusicService.MUSIC_NOW_PLAYING;

import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.LruCache;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.taishi.library.Indicator;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MiniPlayerRecyclerViewAdapter extends RecyclerView.Adapter<MiniPlayerRecyclerViewAdapter.MiniPlayerRecyclerViewAdapterViewHolder> {
    private final Context mContext;
    private ArrayList<MusicFiles> musicFiles;
    private RecyclerView recyclerView;
    private ImageView bg;
    private MusicFiles nowPlaying;
    public static int prevPos = -1;
    private SharedPreferences preferences;

    private Bitmap bitmap = null;
    private LruCache<Long, Bitmap> mBitmapCache;
    private BitmapDrawable placeHolder;

    public MusicFiles getNowPlaying() {
        return nowPlaying;
    }

    public MiniPlayerRecyclerViewAdapter(Context mContext, ArrayList<MusicFiles> musicFiles, RecyclerView recyclerView, ImageView bg) {
        this.mContext = mContext;
        this.musicFiles = musicFiles;
        this.recyclerView = recyclerView;
        this.bg = bg;
        preferences = mContext.getSharedPreferences(MUSIC_NOW_PLAYING, MODE_PRIVATE);
        nowPlaying = new MusicFiles("","","","", "",preferences.getString(MUSIC_ID, ""),"");
        placeHolder = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.music_icon);
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        int availMem = am.getMemoryClass() * 1024 * 1024;
        mBitmapCache = new LruCache<Long, Bitmap>(availMem) {
            @Override
            protected int sizeOf(Long key, Bitmap value) {
//                return value.getByteCount() * 1024;
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    @NonNull
    @Override
    public MiniPlayerRecyclerViewAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.mini_player_item, parent, false);
        return new MiniPlayerRecyclerViewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MiniPlayerRecyclerViewAdapterViewHolder holder, int position) {
        Long id = Long.valueOf(musicFiles.get(holder.getAdapterPosition()).getId());
        final Bitmap bitmap = mBitmapCache.get(id);
        if (bitmap != null) {
            holder.img.setImageBitmap(bitmap);
            if (nowPlaying != null && !nowPlaying.getId().equals(musicFiles.get(position).getId()))
                holder.img.setVisibility(View.VISIBLE);
        } else {
            loadAlbumArt(holder.img, id, position);
        }

        if (nowPlaying != null && nowPlaying.getId().equals(musicFiles.get(position).getId())) {
            holder.indicator.setVisibility(View.VISIBLE);
            holder.cardView.setVisibility(View.GONE);
            holder.img.setVisibility(View.GONE);
        } else {
            if (holder.indicator.getVisibility() == View.VISIBLE) {
                holder.indicator.setVisibility(View.GONE);
                holder.cardView.setVisibility(View.VISIBLE);
            }
        }
        String duration = "0";
//        long milli = getDuration(musicFiles.get(holder.getAdapterPosition()).getPath());
        if (musicFiles.get(holder.getAdapterPosition()).getDuration() != null)
            duration = musicFiles.get(holder.getAdapterPosition()).getDuration();
        String album = musicFiles.get(holder.getAdapterPosition()).getArtist();

        String txt = album.equals("<unknown>") ? milliSecondsToTimer(Long.parseLong(duration)) : album + " · " + milliSecondsToTimer(Long.parseLong(duration));
        holder.song_name.setText(musicFiles.get(holder.getAdapterPosition()).getTitle());
        holder.artist_duration.setText(txt);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "miniPlayerRecyclerViewAdapter");
                intent.putExtra("position", holder.getAdapterPosition());
                intent.putExtra("listSongs", (Serializable) musicFiles);
                mContext.startActivity(intent);
                Bitmap newBmp;
                newBmp = mBitmapCache.get(id);
                if (newBmp != null) {
                    bg.setImageBitmap(newBmp);
                } else {
                    newBmp = getThumbnail(id);
                    if (newBmp != null) {
                        bg.setImageBitmap(newBmp);
                    } else {
                        bg.setImageResource(R.color.black);
                    }
                }
            }
        });

    }

    private long getDuration(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        long milliSec = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaMetadataRetriever.close();
        }
        return milliSec;
    }

    private void loadAlbumArt(ImageView icon, Long id, int position) {
        if (cancelLoadTask(icon, id)) {
            LoadAlbumArt loadAlbumArt = new LoadAlbumArt(icon, mContext, position);
            AsyncDrawable drawable = new AsyncDrawable(mContext.getResources(), placeHolder.getBitmap(), loadAlbumArt);
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
        private int position;


        public LoadAlbumArt(ImageView icon, Context mContext, int position) {
            mIcon = new WeakReference<ImageView>(icon);
            this.mContext = mContext;
            this.position = position;
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
                            new Size(150, 150),
                            null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                bitmap = null;
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView icon = mIcon.get();
            if (isCancelled() || bitmap == null) {
                icon.setVisibility(View.GONE);
                return;
            }

            if (mIcon != null && mIcon.get() != null) {
                Drawable drawable = icon.getDrawable();
                if (drawable instanceof AsyncDrawable) {
                    LoadAlbumArt task = ((AsyncDrawable) drawable).getLoadArtWorkTask();
                    if (task == this) {
                        icon.setImageBitmap(bitmap);
                        if (!preferences.getString(MUSIC_ID, "").equals(musicFiles.get(position).getId()))
                            icon.setVisibility(View.VISIBLE);
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

    @Override
    public int getItemCount() {
        return musicFiles.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class MiniPlayerRecyclerViewAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView song_name, artist_duration;
        LinearLayout linearLayout;
        Indicator indicator;
        CardView cardView;
        ImageView img;

        public MiniPlayerRecyclerViewAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            song_name = itemView.findViewById(R.id.song_name_mini_player_recycler_view_item);
            artist_duration = itemView.findViewById(R.id.artist_duration);
            linearLayout = itemView.findViewById(R.id.touchable);
            indicator = itemView.findViewById(R.id.indicator_mini_player);
            cardView = itemView.findViewById(R.id.card_mask);
            img = itemView.findViewById(R.id.albumArt_mini_player_item);
        }
    }

    public void updateList(ArrayList<MusicFiles> musicFiles) {
        this.musicFiles = musicFiles;
        notifyDataSetChanged();
    }

    public void updateNowPlaying(MusicFiles musicFiles) {
        nowPlaying = musicFiles;
        int pos = -1;
        pos = this.musicFiles.indexOf(musicFiles);
        if (pos != -1) {
            notifyItemChanged(pos);
            if (prevPos != -1)
                notifyItemChanged(prevPos);
            prevPos = pos;
        }

    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString;

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public int getSize() {
        return musicFiles.size();
    }

    private Bitmap getThumbnail(Long id) {
        Uri artworkUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        Bitmap bmp = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                bmp = mContext.getContentResolver().loadThumbnail(
                        artworkUri,
                        new Size(150, 150),
                        null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            bmp = null;
        }
        return bmp;
    }
}
