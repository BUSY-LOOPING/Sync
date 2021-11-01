package com.sync.imusic;

import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
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
        private MediaMetadataRetriever mediaMetadataRetriever;
        private boolean useMediaMetadata = false, useDefaultHolder = true;
        private OnLoadedListener listener;
        private Bitmap bitmap = null;
        private Boolean ready = false;
        private LruCache<Long, Bitmap> mBitmapCache;
        private BitmapDrawable mPlaceholder;
        private final Context mContext;
        private Long id;
        private Size size;

        public from(Context mContext) {
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
            this.mPlaceholder = ((BitmapDrawable) drawable);
        }

        private void loadAlbumArt(ImageView icon, Long id) {
            if (cancelLoadTask(icon, id)) {
                LoadAlbumArt loadAlbumArt = new LoadAlbumArt(icon, mContext);
                loadAlbumArt.execute(id);
//                if (useDefaultHolder) {
                    AsyncDrawable drawable = new AsyncDrawable(mContext.getResources(), mPlaceholder.getBitmap(), loadAlbumArt);
                    icon.setImageDrawable(drawable);
//                }
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
            protected void onPreExecute() {
                ready = false;
                bitmap = null;
            }

            @Override
            protected Bitmap doInBackground(Long... params) {
                id = params[0];
                Bitmap bmp = null;
                if (!useMediaMetadata) {
                    Uri artworkUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
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
                } else {
                    byte[] array = mediaMetadataRetriever.getEmbeddedPicture();
                    if (array != null)
                        bmp = BitmapFactory.decodeByteArray(array, 0, array.length);
                }
                ready = true;
                bitmap = bmp;
                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (listener != null) {
                    listener.onLoaded(bitmap);
                }
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

        public Bitmap getBitmap() {
            return bitmap;
        }

        public interface OnLoadedListener {
            void onLoaded(Bitmap bitmap);
        }

        public void setOnLoadedListener(OnLoadedListener listener) {
            this.listener = listener;
        }

        public void setMediaMetadata(MediaMetadataRetriever mediaMetadataRetriever) {
            this.mediaMetadataRetriever = mediaMetadataRetriever;
        }

        public void useMediaMetadata(boolean useMediaMetadata) {
            if (mediaMetadataRetriever != null && useMediaMetadata) {
                this.useMediaMetadata = true;
            } else
                this.useMediaMetadata = false;
        }

        public void useDefaultHolder(boolean useDefaultHolder) {
            this.useDefaultHolder = useDefaultHolder;
        }

    }
}
