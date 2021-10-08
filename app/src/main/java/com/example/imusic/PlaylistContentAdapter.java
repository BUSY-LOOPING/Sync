package com.example.imusic;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class PlaylistContentAdapter extends RecyclerView.Adapter<PlaylistContentAdapter.PlaylistContentAdapterViewHolder> {
    private final Context mContext;
    private final ArrayList<PlaylistFiles> playlistFiles;
    private final PlaylistContentListener playlistContentListener;
    private LruCache<Long, Bitmap> mBitmapCache;
    private BitmapDrawable mPlaceholder;


    @SuppressLint("UseCompatLoadingForDrawables")
    PlaylistContentAdapter(Context mContext, ArrayList<PlaylistFiles> playlistFiles, PlaylistContentListener playlistContentListener) {
        this.mContext = mContext;
        this.playlistContentListener = playlistContentListener;
        this.playlistFiles = playlistFiles;
        mPlaceholder = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.music_icon);
        int maxSize = (int) (Runtime.getRuntime().maxMemory() * 1024);
        int cacheSize = maxSize / 8;
        if (cacheSize == 0) cacheSize = 10;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        int availMemInBytes = am.getMemoryClass() * 1024 * 1024;
        mBitmapCache = new LruCache<Long, Bitmap>(availMemInBytes / 8) {
            @Override
            protected int sizeOf(Long key, Bitmap value) {
                //returns the size of the in-memory bitmap counted against maxSizeBytes
                return value.getByteCount() * 1024;
//                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    @NonNull
    @Override
    public PlaylistContentAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.playlist_content_item, parent, false);
        return new PlaylistContentAdapterViewHolder(view, playlistContentListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull PlaylistContentAdapter.PlaylistContentAdapterViewHolder holder, int position) {
        if (playlistFiles.get(position).isMusicFile) {
            holder.name.setText(playlistFiles.get(position).getMusicFiles().getTitle());

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                Uri artworkUri = Uri.parse("content://media/external/audio/media/" + playlistFiles.get(position).getMusicFiles().getId() + "/albumart");
                Glide.with(mContext)
                        .load(artworkUri)
                        .error(R.drawable.music_icon)
                        .into(holder.img);
            } else {
                final Bitmap bitmap = mBitmapCache.get(Long.valueOf(playlistFiles.get(position).getMusicFiles().getId()));
                if (bitmap != null) {
                    holder.img.setImageBitmap(bitmap);
                } else {
                    loadAlbumArt(holder.img, Long.valueOf(playlistFiles.get(position).getMusicFiles().getId()));
                }
            }
        } else {
            holder.name.setText(playlistFiles.get(position).getVideoFiles().getFilename());
            Log.d("img", new File(playlistFiles.get(position).getVideoFiles().getPath()).getPath());
            Glide.with(mContext)
                    .load(Uri.fromFile(new File(playlistFiles.get(position).getVideoFiles().getPath())))
                    .error(R.drawable.music_icon)
                    .into(holder.img);

        }
        holder.drag_handle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                playlistContentListener.onStartDrag(holder);
                return false;
            }
        });

    }

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
        if (loadAlbumArt != null) {
            if (loadAlbumArt.id != id) {
                loadAlbumArt.cancel(true);
                return true;
            }
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
                            new Size(200, 200),
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

    @Override
    public int getItemCount() {
        return playlistFiles.size();
    }


//    @Override
//    public long getItemId(int position) {
//        return position;
//    }

    public class PlaylistContentAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener, View.OnLongClickListener {
        TextView name, description;
        ImageView img, more, drag_handle;
        private PlaylistContentListener listener;

        public PlaylistContentAdapterViewHolder(@NonNull View itemView, PlaylistContentListener listener) {
            super(itemView);
            this.listener = listener;
            name = itemView.findViewById(R.id.name_playlist_content_item);
            name.setSelected(true);
            description = itemView.findViewById(R.id.description_playlist_content_item);
            img = itemView.findViewById(R.id.img_playlist_content_item);
            drag_handle = itemView.findViewById(R.id.drag_handle);
            more = itemView.findViewById(R.id.more_playlist_content_item);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            more.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == itemView.getId()) {
                listener.onClick(playlistFiles, getAdapterPosition());
            }
            if (view.getId() == R.id.more_playlist_content_item) {
                listener.moreClick();
            }
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onLongClick(View view) {
            if (view.getId() == itemView.getId()) {
                listener.longClick();
            }
            return false;
        }
    }
}
