package com.example.imusic;

import static com.example.imusic.FileActivity.PARENT;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.ArrayList;


public class AddToPlaylistPopup extends BottomSheetDialogFragment {
    private Context mContext;
    private File parent;
    private Dialog dialog;
    private ArrayList<PlaylistFiles> playlistFiles = new ArrayList<>();
    private ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    private ArrayList<VideoFiles> videoFiles = new ArrayList<>();
    private Thread thread;

    private ProgressBar progressBar;
    private View view;
    private EditText editText;
    private Button addBtn;
    private RecyclerView recyclerView;
    TextView no_media1, no_media2;

    private DataBaseHelperPlaylist db;

    public static final String PLAYLIST_NAME = "myplaylist.db";

    public AddToPlaylistPopup() {
    }

//    AddToPlaylistPopup(Context mContext, File parent, View view) {
//        this.mContext = mContext;
//        this.parent = parent;
//        this.view = view;
//        init();
//    }

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

//    @Override
//    public int getTheme() {
//        return R.style.BottomSheetDialogTheme;
//    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = super.onCreateDialog(savedInstanceState);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.add_to_playlist, container, false);
        initView(view);
        Bundle bundle = this.getArguments();
        setList(bundle);
        initAdapter();
        listeners();
        return view;
    }

    private void initView(View view) {
        progressBar = view.findViewById(R.id.progress_bar_add_to_playlist);
        recyclerView = view.findViewById(R.id.add_to_playlist_RecyclerView);
        editText = view.findViewById(R.id.editText_add_to_playlist);
        addBtn = view.findViewById(R.id.addBtn_add_to_playlist);
        no_media1 = view.findViewById(R.id.no_media_add_to_playlist);
        no_media2 = view.findViewById(R.id.no_media_add_to_playlist_2);
        playlistFiles = getPlaylistFiles();
    }

    private void initAdapter() {
        AddToPlaylistPopupRecyclerAdapter adapter = new AddToPlaylistPopupRecyclerAdapter(mContext, return_sameNamePlaylistFiles(playlistFiles), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
        if (playlistFiles.size() <= 0) {
            recyclerView.setVisibility(View.GONE);
            TextView textView = view.findViewById(R.id.no_playlist_found);
            textView.setVisibility(View.VISIBLE);
        }
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


    private void listeners() {
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                db = new DataBaseHelperPlaylist(mContext, PLAYLIST_NAME, null, 1);
                String playListName = editText.getText().toString();
                AddPlaylistFilesToDb addPlaylistFilesToDb = new AddPlaylistFilesToDb(mContext);
                addPlaylistFilesToDb.set(musicFiles, videoFiles, playListName);
                addPlaylistFilesToDb.execute("start");
                dialog.cancel();
            }
        });
    }

    private void setList(Bundle bundle) {
        if (bundle != null) {
            parent = (File) bundle.get(PARENT);
            if (parent != null) {
                new getFilesInParent().execute("start");
            }
        }
    }

    public void init() {
//        addToPlaylistDialog = new BottomSheetDialog(mContext, R.style.BottomSheetAddToPlaylist);
//        addToPlaylistView = LayoutInflater.from(mContext).inflate(R.layout.add_to_playlist, view.findViewById(R.id.addToPlaylistContainer));
//        addToPlaylistDialog.setContentView(addToPlaylistView);
//        progressBar = addToPlaylistView.findViewById(R.id.progress_bar_add_to_playlist);
//        RecyclerView recyclerView = addToPlaylistDialog.findViewById(R.id.add_to_playlist_RecyclerView);
//        editText = addToPlaylistView.findViewById(R.id.editText_add_to_playlist);
//        addBtn = addToPlaylistView.findViewById(R.id.addBtn_add_to_playlist);
//        no_media1 = addToPlaylistView.findViewById(R.id.no_media_add_to_playlist);
//        no_media2 = addToPlaylistView.findViewById(R.id.no_media_add_to_playlist_2);
//        db = new DataBaseHelperPlaylist(mContext, PLAYLIST_NAME, null, 1);

//        ArrayList<PlaylistFiles> playlistFiles = getPlaylistFiles();
//        AddToPlaylistPopupRecyclerAdapter adapter = new AddToPlaylistPopupRecyclerAdapter(mContext, playlistFiles);
//        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
//        recyclerView.setAdapter(adapter);
//        if (playlistFiles.size() <= 0) {
//            recyclerView.setVisibility(View.GONE);
//            TextView textView = addToPlaylistView.findViewById(R.id.no_playlist_found);
//            textView.setVisibility(View.VISIBLE);
//        }

//        ArrayList<MusicFiles> musicFiles = getAllAudio(mContext);
//        ArrayList<VideoFiles> videoFiles = getAllVideos(mContext);

    }

    private int cal_total_no_media(ArrayList<PlaylistFiles> playlistFiles) {
        int result = 0;
        for (int i = 0; i < playlistFiles.size(); i++) {
            if (playlistFiles.get(i).isMusicFile || playlistFiles.get(i).isVideoFile)
                result++;
        }
        return result;
    }

    private ArrayList<PlaylistFiles> getPlaylistFiles() {
        ArrayList<PlaylistFiles> temp = new ArrayList<>();
        DataBaseHelperPlaylist db = new DataBaseHelperPlaylist(mContext, PLAYLIST_NAME, null, 1);
        Cursor res = db.getAllData();
        while (res.moveToNext()) {
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
        res.close();
        db.close();
        return temp;
    }

    public void show() {
        dialog.show();
    }

    public ArrayList<VideoFiles> getAllVideos(File parent) {
        ArrayList<VideoFiles> tempVideoFiles = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,  //this is for path
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DISPLAY_NAME,  //for filename
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.RESOLUTION
        };
        Cursor cursor;
//        if (!parent.getAbsolutePath().equals("/sdcard")) {
        cursor = mContext.getContentResolver().query(
                uri,
                projection,
                MediaStore.Video.Media.DATA + " like ? ",
                new String[]{"%" + parent.getAbsolutePath().replace("/sdcard", "") + "%"},
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
                Log.d("playlist", "inside while");
                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                Log.d("playlist", title);
                String filename = cursor.getString(3);
                String size = cursor.getString(4);
                String dateAdded = cursor.getString(5);
                String duration = cursor.getString(6);
                String resolution = cursor.getString(7);
                Log.d("myvideo", "filename : " + filename);
                Log.d("myvideo", "duration : " + duration);

                tempVideoFiles.add(new VideoFiles(id, path, title, filename, size, dateAdded, duration, resolution));
            }
            cursor.close();
        }
        return tempVideoFiles;
    }

    public ArrayList<MusicFiles> getAllAudio(File parent) {
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
        cursor = mContext.getContentResolver().query(
                uri,
                projection,
                MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%" + parent.getAbsolutePath().replace("/sdcard", "") + "%"},
                null
        );

        if (cursor != null) {
            Log.d("playlist", "getAllAudio: not null");
            while (cursor.moveToNext()) {
                Log.d("playlist", "getAllAudio: inside while");
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                String date_added = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                Log.d("playlist", "music file in dir : " + parent.getAbsolutePath() + " : " + title);

                if (path != null) {
                    MusicFiles musicFiles = new MusicFiles(path, title, artist, album, duration, id, size);
                    tempAudioList.add(musicFiles);
                }
            }
            cursor.close();
        }

        Uri uriForOpus = MediaStore.Files.getContentUri("external");

// every column, although that is huge waste, you probably need
        // exclude media files, they would be here also.
        String selectionForOpus = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;

        Cursor allOpusMediaFiles = mContext.getContentResolver().query(uriForOpus, null,
                MediaStore.Files.FileColumns.DATA + " like ? ",
                new String[]{"%" + parent.getAbsolutePath().replace("/sdcard", "") + "%"}, null);
        if (allOpusMediaFiles != null) {
            while (allOpusMediaFiles.moveToNext()) {
                String album = "unknown";
                String title = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                String duration = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.DURATION));
                String path = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String artist = "unknown";
                String id = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns._ID));
                String size = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                String date_added = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED));
                if (path != null && !path.endsWith(".opus"))
                    continue;

                if (path != null && title != null) {
                    MusicFiles musicFiles = new MusicFiles(path, title, artist, album, duration, id, size);
                    tempAudioList.add(musicFiles);
                }

            }
            allOpusMediaFiles.close();
        }

        return tempAudioList;
    }

    class getFilesInParent extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            addBtn.setEnabled(false);
            addBtn.setTextColor(ContextCompat.getColor(mContext, R.color.ripple_color_light));
        }

        @Override
        protected String doInBackground(String... strings) {
            musicFiles = getAllAudio(parent);
            videoFiles = getAllVideos(parent);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.GONE);
            no_media1.setVisibility(View.VISIBLE);
            no_media2.setVisibility(View.VISIBLE);
            no_media1.setText((musicFiles.size() + videoFiles.size()) + " media");
            no_media2.setText((musicFiles.size() + videoFiles.size()) + " media");
            addBtn.setEnabled(true);
            addBtn.setTextColor(ContextCompat.getColor(mContext, R.color.tab_highlight));

        }
    }

    public void addMusicFiles(ArrayList<MusicFiles> musicFiles) {
        this.musicFiles = musicFiles;
    }

    public void addVideoFiles(ArrayList<VideoFiles> videoFiles){
        this.videoFiles = videoFiles;
    }

    public void addToExistingPlaylist(String playListName){
        AddPlaylistFilesToDb addPlaylistFilesToDb = new AddPlaylistFilesToDb(mContext);
        addPlaylistFilesToDb.set(musicFiles, videoFiles, playListName);
        addPlaylistFilesToDb.execute("start");
        dialog.cancel();
    }
}
