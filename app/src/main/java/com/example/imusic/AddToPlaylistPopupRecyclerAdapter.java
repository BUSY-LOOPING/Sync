package com.example.imusic;

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
import android.util.LruCache;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class AddToPlaylistPopupRecyclerAdapter extends RecyclerView.Adapter<AddToPlaylistPopupRecyclerAdapter.AddToPlaylistPopupRecyclerAdapterViewHolder> {

    private ArrayList<SameNamePlaylistFiles> list;
    private Context mContext;
    private AddToPlaylistPopup ref;

    private Bitmap bitmap = null;
    private LruCache<Long, Bitmap> mBitmapCache;
    private BitmapDrawable mPlaceholder;

    AddToPlaylistPopupRecyclerAdapter() {
    }

    AddToPlaylistPopupRecyclerAdapter(Context mContext, ArrayList<SameNamePlaylistFiles> list, AddToPlaylistPopup ref) {
        this.mContext = mContext;
        this.list = list;
        this.ref = ref;
        mPlaceholder = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.music_icon);
        int maxSize = (int) (Runtime.getRuntime().maxMemory() * 1024);
        int cacheSize = maxSize / 8;
        if (cacheSize == 0) cacheSize = 10;
        mBitmapCache = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    @NonNull
    @Override
    public AddToPlaylistPopupRecyclerAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.add_to_playlist_item, parent, false);
        return new AddToPlaylistPopupRecyclerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddToPlaylistPopupRecyclerAdapter.AddToPlaylistPopupRecyclerAdapterViewHolder holder, int position) {

        if (list.get(holder.getAdapterPosition()).getSize() >= 2) {
            holder.art.setVisibility(View.GONE);
            holder.art1.setVisibility(View.VISIBLE);
            holder.art2.setVisibility(View.VISIBLE);
            holder.art3.setVisibility(View.VISIBLE);
            holder.art4.setVisibility(View.VISIBLE);
//            final Bitmap bitmap1 = mBitmapCache.get(id1);
//            if (bitmap1 != null) {
//                holder.art1.setImageBitmap(bitmap1);
//            } else {
//                loadAlbumArt(holder.art1, id1);
//            }

        } else {
            holder.art.setVisibility(View.VISIBLE);
            holder.art1.setVisibility(View.GONE);
            holder.art2.setVisibility(View.GONE);
            holder.art3.setVisibility(View.GONE);
            holder.art4.setVisibility(View.GONE);
        }
        holder.playlistName.setText(list.get(holder.getAdapterPosition()).getPlayListName());
        if (list.get(position).getSize() != 0)
            holder.no_media.setText(list.get(holder.getAdapterPosition()).getSize() + " media");
        else
            holder.no_media.setText("No media");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref.addToExistingPlaylist(list.get(holder.getAdapterPosition()).getPlayListName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class AddToPlaylistPopupRecyclerAdapterViewHolder extends RecyclerView.ViewHolder {
        ImageView art, art1, art2, art3, art4;
        TextView playlistName, no_media;

        public AddToPlaylistPopupRecyclerAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistName = itemView.findViewById(R.id.playlist_name_add_to_playlist_item);
            no_media = itemView.findViewById(R.id.no_media_add_to_playlist_item);
            art = itemView.findViewById(R.id.img_playlist_add_to_playlist_item);
            art1 = itemView.findViewById(R.id.img1_playlist_add_to_playlist_item);
            art2 = itemView.findViewById(R.id.img2_playlist_add_to_playlist_item);
            art3 = itemView.findViewById(R.id.img3_playlist_add_to_playlist_item);
            art4 = itemView.findViewById(R.id.img4_playlist_add_to_playlist_item);
        }
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

}
