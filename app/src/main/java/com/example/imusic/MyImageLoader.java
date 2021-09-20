package com.example.imusic;

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
import android.util.LruCache;
import android.util.Size;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class MyImageLoader {

    public static class from {
        private Bitmap bitmap = null;
        private LruCache<Long, Bitmap> mBitmapCache;
        private BitmapDrawable mPlaceholder;
        private final Context mContext;
        private Long id;
        private ImageView icon;
        private Size size;

        public from (Context mContext) {
            this.mContext = mContext;
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

        public void load(Long id) {
            this.id = id;
        }

        public void into(ImageView icon) {
            this.icon = icon;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                size = new Size(120, 120);
            }
            loadAlbumArt(icon, id);
            final Bitmap bitmap = mBitmapCache.get(id);
            if (bitmap != null) {
                icon.setImageBitmap(bitmap);
            } else {
                loadAlbumArt(icon, id);
            }
        }

        public void setSize(Size size) {
            this.size = size;
        }

        public void setmPlaceholder(Drawable drawable) {
            this.mPlaceholder = ((BitmapDrawable)drawable);
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
                                size,
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
}
