package com.sync.imusic;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ActivityFileAdapter extends RecyclerView.Adapter<ActivityFileAdapter.MyActivityFileViewHolder> {
    private Context mContext;
    private List<File> list;
    private OnClickListenerActivityFileAdapter mListener;
    private ArrayList<Integer> tempList;
    private RecyclerView recyclerView;


    ActivityFileAdapter() {

    }

    ActivityFileAdapter(Context mContext, List<File> list, ArrayList<Integer> tempList, OnClickListenerActivityFileAdapter mListener, RecyclerView recyclerView) {
        this.mContext = mContext;
        this.list = list;
        this.mListener = mListener;
        this.tempList = tempList;
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).hashCode();
    }

    @NonNull
    @Override
    public MyActivityFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_file_item, parent, false);
        return new MyActivityFileViewHolder(view, mListener);
    }

    //    @SuppressLint("UseCompatLoadingForDrawables")
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ActivityFileAdapter.MyActivityFileViewHolder holder, int position) {
        holder.folder_file_name.setText(list.get(position).getName());
        if (list.get(position).isDirectory()) {
            holder.folder_img.setImageResource(R.drawable.ic_baseline_folder_24);
            holder.folder_img.setColorFilter(ContextCompat.getColor(mContext, R.color.folder_color), PorterDuff.Mode.SRC_IN);
            if (tempList.get(position) == 0)
                holder.no_items.setText("Empty");
            else {
                String text = tempList.get(position) + " ";
                holder.no_items.setTransformationMethod(null);
                SpannableString ss = new SpannableString(text);
                Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.file_icon);
                if (drawable != null) {
                    drawable.setBounds(0, 0, holder.no_items.getLineHeight(), holder.no_items.getLineHeight());
                }
                ss.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE) {
                    public void draw(Canvas canvas, CharSequence text, int start,
                                     int end, float x, int top, int y, int bottom,
                                     @NonNull Paint paint) {
                        if (mVerticalAlignment != ALIGN_BASELINE) {
                            super.draw(canvas, text, start, end, x, top, y, bottom, paint);
                            return;
                        }
                        Drawable b = getDrawable();
                        canvas.save();
                        // If we set transY = 0, then the drawable will be drawn at the top of the text.
                        // y is the the distance from the baseline to the top of the text, so
                        // transY = y will draw the top of the drawable on the baseline. We want the             // bottom of the drawable on the baseline, so we subtract the height
                        // of the drawable.
                        int transY = y - (b.getBounds().bottom);
                        canvas.translate(x, transY);
                        b.draw(canvas);

                        canvas.restore();
                    }
                }, text.length() - 1, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                holder.no_items.setText(ss, TextView.BufferType.SPANNABLE);
//                holder.no_items.setText(String.valueOf(tempList.get(position)));
            }
        } else {
            if (isVideoFile(list.get(position).getPath())) {
                holder.folder_img.setColorFilter(null);
                Glide.with(mContext)
                        .load(Uri.fromFile(new File(list.get(position).getPath())))
                        .error(R.drawable.music_icon)
                        .into(holder.folder_img);
                holder.loadDetails = new LoadDetails((FileActivity) mContext, holder.no_items, false);
                holder.loadDetails.execute(list.get(position).getAbsolutePath());
//                if (videoFiles != null) {
//                    String duration = videoFiles.getDuration();
//                    if (duration == null || duration.equals("")) duration = "0";
//                    holder.no_items.setText(MiniPlayer.milliSecondsToTimer(Long.parseLong(duration)) + " · " + videoFiles.getResolution());
//                }
            } else if (isMusicFile(list.get(position).getAbsolutePath())) {
                holder.folder_img.setColorFilter(null);
                MyImageLoader.from myImageLoader = new MyImageLoader.from(mContext);
                long id = getSongIdFromMediaStore(list.get(position).getPath());
                myImageLoader.load(id);
                myImageLoader.into(holder.folder_img);
//                MusicFiles musicFiles = getMusicFileForPath(list.get(position), mContext);
//                if (musicFiles != null) {
//                    load(holder.no_items, true, list.get(position).getPath());
                holder.loadDetails = new LoadDetails((FileActivity) mContext, holder.no_items, true);
                holder.loadDetails.execute(list.get(position).getPath());
//                }
//            Drawable drawable = new BitmapDrawable(mContext.getResources(), BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_baseline_music_note_24));
//            myImageLoader.setmPlaceholder(drawable);
            } else if (isImageFile(list.get(position).getPath())) {
                holder.folder_img.setColorFilter(null);
                Glide.with(mContext)
                        .load(Uri.fromFile(list.get(position)))
                        .dontAnimate()
                        .into(holder.folder_img);
                holder.no_items.setText(InfoActivityVideo.humanReadableByteCountBin(list.get(position).length()));
            } else if (isApkFile(list.get(position).getPath())) {
                holder.folder_img.setImageResource(R.drawable.ic_baseline_android_24);
                holder.folder_img.setColorFilter(ContextCompat.getColor(mContext, R.color.av_green), PorterDuff.Mode.SRC_IN);
                holder.no_items.setText(InfoActivityVideo.humanReadableByteCountBin(list.get(position).length()));
            } else {
                holder.folder_img.setImageResource(R.drawable.ic_baseline_folder_24);
                holder.folder_img.setColorFilter(ContextCompat.getColor(mContext, R.color.folder_color), PorterDuff.Mode.SRC_IN);
                holder.loadDetails = new LoadDetails((FileActivity) mContext, holder.no_items, list.get(position));
                holder.loadDetails.execute("");
                holder.no_items.setText(InfoActivityVideo.humanReadableByteCountBin(list.get(position).length()));
            }
        }

    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MyActivityFileViewHolder holder) {
        if (holder.loadDetails != null) {
            if (holder.loadDetails.getStatus() == AsyncTask.Status.RUNNING) {
                holder.loadDetails.cancel(true);
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MyActivityFileViewHolder holder) {
        if (holder.loadDetails != null) {
            if (holder.loadDetails.getStatus() == AsyncTask.Status.FINISHED && holder.no_items.getVisibility() == View.GONE) {
                boolean isMusicFile = holder.loadDetails.isMusicFile;
                holder.loadDetails = new LoadDetails((FileActivity) mContext, holder.no_items, isMusicFile);
                holder.loadDetails.execute(list.get(holder.getAdapterPosition()).getAbsolutePath());
            }
        }
    }

    public static boolean isApkFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("application/vnd.android.package-archive");
    }

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static boolean isMusicFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("audio");
    }

    public long getSongIdFromMediaStore(String songPath) {
        long id = 0;
        ContentResolver cr = mContext.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.DATA;
        String[] selectionArgs = {songPath};
        String[] projection = {MediaStore.Audio.Media._ID};
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = cr.query(uri, projection, selection + "=?", selectionArgs, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                id = Long.parseLong(cursor.getString(idIndex));
            }
            cursor.close();
        }
        return id;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MyActivityFileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView folder_file_name, no_items;
        private final ImageView folder_img, more;
        private LoadDetails loadDetails;
        private OnClickListenerActivityFileAdapter listener;

        public MyActivityFileViewHolder(@NonNull View itemView, OnClickListenerActivityFileAdapter listener) {
            super(itemView);
            this.listener = listener;
            folder_file_name = itemView.findViewById(R.id.folder_file_name_file_item);
            folder_file_name.setSelected(true);
            no_items = itemView.findViewById(R.id.no_items_activity_file);
            folder_img = itemView.findViewById(R.id.folder_img_file_item);
            more = itemView.findViewById(R.id.more_file_item);
            itemView.setOnClickListener(this);
            more.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.more_file_item) {
                listener.onMoreClick(getAdapterPosition(), list.get(getAdapterPosition()));
            } else {
                stopAllAsync();
                listener.onItemViewClick(getAdapterPosition(), list.get(getAdapterPosition()));
            }
        }
    }

    public void update(List<File> list, ArrayList<Integer> tempList, RecyclerView recyclerView) {
//        this.list = new ArrayList<>();
//        this.list.addAll(list);
        this.list = list;
        this.tempList = tempList;
        notifyDataSetChanged();
//        recyclerView.scheduleLayoutAnimation();
    }

    public static MusicFiles getMusicFileForPath(File file, Context context) {
        MusicFiles res = new MusicFiles();
        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA, //for path
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED
        };
        Cursor cursor;
        cursor = context.getContentResolver().query(
                uri,
                projection,
                MediaStore.Audio.Media.DATA + " like ? and " + MediaStore.Audio.Media.DATA + " not like ? and " + MediaStore.Audio.Media.DISPLAY_NAME + " like ? ",
                new String[]{"%" + file.getAbsolutePath().replace("/sdcard", "") + "%",
                        "%" + file.getAbsolutePath() + "/%/%",
                        "%" + file.getName() + "%"},
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                String date_added = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));

                if (path != null) {
                    res = new MusicFiles(path, title, artist, album, duration, id, size);
                }
            }
            cursor.close();
        }

        Uri uriForOpus = MediaStore.Files.getContentUri("external");

// every column, although that is huge waste, you probably need
        // exclude media files, they would be here also.
        String selectionForOpus = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;

        Cursor allOpusMediaFiles = context.getContentResolver().query(uriForOpus, null,
                MediaStore.Files.FileColumns.DATA + " like ? and " + MediaStore.Audio.Media.DATA + " not like ? and " + MediaStore.Audio.Media.DISPLAY_NAME + " like ? ",
                new String[]{"%" + file.getAbsolutePath().replace("/sdcard", "") + "%",
                        "%" + file.getAbsolutePath() + "/%/%",
                        "%" + file.getName() + "%"},
                null);
        if (allOpusMediaFiles != null) {
            if (allOpusMediaFiles.moveToFirst()) {
                String album = "unknown";
                String title = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                String duration = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.DURATION));
                String path = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String artist = "unknown";
                String id = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns._ID));
                String size = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                String date_added = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED));

                if (path != null && title != null && path.endsWith(".opus")) {
                    res = new MusicFiles(path, title, artist, album, duration, id, size);
                }

            }
            allOpusMediaFiles.close();
        }

        return res;
    }

    public static VideoFiles getVideoFileForPath(File file, Context context) {
//        Log.d("mylog", "file name = "+ file.getName());
        VideoFiles videoFiles = new VideoFiles();
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,  //this is for path
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DISPLAY_NAME,  //for filename
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DURATION,
                MediaStore.Files.FileColumns.RESOLUTION
        };
        Cursor cursor;
//        if (!parent.getAbsolutePath().equals("/sdcard")) {
        cursor = context.getContentResolver().query(
                uri,
                null,
                MediaStore.Files.FileColumns.DATA + " like ? and " + MediaStore.Files.FileColumns.DATA + " not like ? and " + MediaStore.Files.FileColumns.DISPLAY_NAME + " like ? ",
                new String[]{"%" + file.getAbsolutePath().replace("/sdcard", "") + "%",
                        "%" + file.getAbsolutePath() + "/%",
                        "%" + file.getName() + "%"},
                null);
//        }
//        else {
//            cursor = context.getContentResolver().query(
//                    uri,
//                    projection,
//                    null,
//                    null,
//                    null);
//        }

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                String filename = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DURATION));
                String resolution = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.RESOLUTION));
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(path);
                if (duration == null) {
                    duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                }
                if (resolution == null) {
                    String height = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    String width = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    resolution = height + "x" + width;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mediaMetadataRetriever.close();
                }
                videoFiles = new VideoFiles(id, path, title, filename, size, dateAdded, duration, resolution);
                if (title.equals(filename)) break;
            }
            cursor.close();
        }

        return videoFiles;
    }


    private static class LoadDetails extends AsyncTask<String, String, String> {
        private final WeakReference<FileActivity> activityWeakReference;
        private final WeakReference<TextView> textViewWeakReference;
        private final boolean isMusicFile;
        private final boolean isGeneralFile;
        private File file;
        private String text = "";

        LoadDetails(FileActivity fileActivity, TextView textView, boolean isMusicFile) {
            activityWeakReference = new WeakReference<>(fileActivity);
            textViewWeakReference = new WeakReference<>(textView);
            this.isMusicFile = isMusicFile;
            isGeneralFile = false;
        }

        public LoadDetails(FileActivity fileActivity, TextView textView, File file) {
            activityWeakReference = new WeakReference<>(fileActivity);
            textViewWeakReference = new WeakReference<>(textView);
            this.isMusicFile = false;
            isGeneralFile = true;
            this.file = file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            FileActivity fileActivity = activityWeakReference.get();
            TextView textView = textViewWeakReference.get();
            if (fileActivity == null || textView == null) {
                return;
            }
            textView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... strings) {
            FileActivity fileActivity = activityWeakReference.get();
            TextView textView = textViewWeakReference.get();
            if (fileActivity == null || textView == null) {
                return "GONE";
            }
            if (!isGeneralFile) {
                if (isMusicFile) {
                    MusicFiles musicFiles = getMusicFileForPath(new File(strings[0]), fileActivity);
                    if (musicFiles == null) {
                        return "GONE";
                    }
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(musicFiles.getPath());
                    int milliSec = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        mediaMetadataRetriever.close();
                    }
                    String duration = MiniPlayer.milliSecondsToTimer(milliSec);
                    String artist = musicFiles.getArtist();
                    String album = musicFiles.getAlbum();
                    text = duration;
                    if (artist != null) {
                        text += " · " + artist;
                    }
                    if (album != null) {
                        text += " · " + album;
                    }

                } else {
                    VideoFiles videoFiles = getVideoFileForPath(new File(strings[0]), fileActivity);
                    if (videoFiles == null) {
                        return "GONE";
                    }
                    String dur = videoFiles.getDuration();
                    if (dur.equals("")) dur = "0";
                    text = MiniPlayer.milliSecondsToTimer(Long.parseLong(dur));
                    String resolution = videoFiles.getResolution();
                    if (resolution != null) {
                        text += " · " + resolution;
                    }
                }
            } else {
                long length = file.length();
                text = InfoActivityVideo.humanReadableByteCountBin(length);
            }

            return "VISIBLE";
        }

        @Override
        protected void onPostExecute(String s) {
            FileActivity fileActivity = activityWeakReference.get();
            TextView textView = textViewWeakReference.get();
            if (fileActivity == null || textView == null) {
                return;
            }
            if (s != null && s.equals("GONE")) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setText(text);
                textView.setVisibility(View.VISIBLE);
            }
        }
    }


    private static class AsyncTextView extends androidx.appcompat.widget.AppCompatTextView {
        private WeakReference<LoadDetails> weakReference;

        AsyncTextView(LoadDetails loadDetails, Context context) {
            super(context);
            weakReference = new WeakReference<>(loadDetails);
        }

        public WeakReference<LoadDetails> getWeakReference() {
            return weakReference;
        }

        public LoadDetails getLoadDetailsTask() {
            return weakReference.get();
        }

        public AsyncTextView(@NonNull Context context) {
            super(context);
        }

    }

    void stopAllAsync() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (linearLayoutManager != null) {
            for (int i = linearLayoutManager.findFirstVisibleItemPosition(); i <= linearLayoutManager.findLastVisibleItemPosition(); i++) {
                MyActivityFileViewHolder holder = (MyActivityFileViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null && holder.loadDetails != null) {
                    holder.loadDetails.cancel(true);
                    holder.loadDetails = null;
                }
            }
        }
    }

    public List<File> getList() {
        return list;
    }
}
