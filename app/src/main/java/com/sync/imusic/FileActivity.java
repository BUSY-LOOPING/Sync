package com.sync.imusic;

import static com.sync.imusic.VideoAdapter.VIDEO_FILES;
import static com.sync.imusic.VideoAdapter.VIDEO_FILES_POS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sync.imusic.fragment.BrowseFragment;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class FileActivity extends AppCompatActivity implements OnClickListenerActivityFileAdapter, SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener;
    private boolean show_all_files = true, show_hidden_files = true;
    public static final String PARENT = "file_parent";
    private File parent;
    boolean isFav = false;
    public static final String IS_FAV = "IS_FAVOURITE";
    public static final String STRING_FAV = "STRING_FAV";
    private DataBaseHelper mydb;
    private List<File> list = new ArrayList<>();
    private ArrayList<Integer> pairList;
    private Handler handler = new Handler();
    private String sortOrder_name = "ASC";
    private String sortOrder_size = "ASC";
    private WeakReference<AsyncTask> asyncTaskWeakReference;

    private RecyclerView recyclerView;
    private ActivityFileAdapter activityFileAdapter;

    //    private BottomSheetDialog addToPlaylistDialog;
//    private View addToPlaylistView;
//    private EditText newPlaylistEditText;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_menu, menu); //menu should be inflated first
        if (mydb.isFavourite(parent)) {
            menu.findItem(R.id.fav_icon_file_menu).setIcon(R.drawable.ic_baseline_star_rate).setChecked(true);
        }

        MenuItem menuItem = menu.findItem(R.id.search_file_menu);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search in current list");
        searchView.setOnQueryTextListener(this);
        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                setItemsVisibility(menu, menuItem, false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                invalidateOptionsMenu();
                return true;
            }
        });
        return true;
    }

    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fav_icon_file_menu:
                if (item.isChecked()) {
                    item.setChecked(false);
                    item.setIcon(R.drawable.ic_baseline_star_border);
                    isFav = false;
                    mydb.deleteData(parent.getAbsolutePath());
                    BrowseFragment.browseAdapter.removeFile(parent);
                } else {
                    item.setChecked(true);
                    item.setIcon(R.drawable.ic_baseline_star_rate);
                    isFav = true;
                    if (mydb.insertData(parent.getName(), parent.getPath(), parent.getAbsolutePath())) {
                        BrowseFragment.browseAdapter.updateFile(parent);
                    }
                }
                break;
            case R.id.sort_by_name_file_menu:
                Collections.sort(list, new MySortByName_FileActivity(sortOrder_name));
//                sort_no_media(list, pairList);
                activityFileAdapter.update(list, pairList, recyclerView);
                if (sortOrder_name.equals("ASC"))
                    sortOrder_name = "DES";
                else
                    sortOrder_name = "ASC";
                break;

            case R.id.sort_by_size_file_menu:
                SortBySize sortBySize = new SortBySize(this, list);
                sortBySize.execute();
                asyncTaskWeakReference = new WeakReference<>(sortBySize);

//                Collections.sort(list, new MySortBySize_FileActivity(sortOrder_size));
//                sort_no_media(list, pairList);
//                activityFileAdapter.update(list, pairList, recyclerView);
                if (sortOrder_size.equals("ASC"))
                    sortOrder_size = "DES";
                else
                    sortOrder_size = "ASC";
                break;

            case R.id.show_all_files:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    show_all_files = true;
                    ShowAllFiles showAllFiles = new ShowAllFiles(this);
                    showAllFiles.execute("start");
                    asyncTaskWeakReference = new WeakReference<>(showAllFiles);
                } else {
                    show_all_files = false;
                    ShowOnlyMediaFiles showOnlyMediaFiles = new ShowOnlyMediaFiles(this);
                    showOnlyMediaFiles.execute("start");
                    asyncTaskWeakReference = new WeakReference<>(showOnlyMediaFiles);
                }
                break;
            case R.id.show_hidden_files:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    show_hidden_files = true;
                    ShowHiddenFiles showHiddenFiles = new ShowHiddenFiles(this);
                    showHiddenFiles.execute("start");
                    asyncTaskWeakReference = new WeakReference<>(showHiddenFiles);
                } else {
                    show_hidden_files = false;
                    ShowNonHiddenFiles showNonHiddenFiles = new ShowNonHiddenFiles(this);
                    showNonHiddenFiles.execute("start");
                    asyncTaskWeakReference = new WeakReference<>(showNonHiddenFiles);
                }
                break;
            case R.id.add_this_folder_n_subfoldes:
                Bundle bundle = new Bundle();
                bundle.putSerializable(PARENT, parent);
                AddToPlaylistPopup popup = new AddToPlaylistPopup();
                popup.setArguments(bundle);
                popup.setShowsDialog(true);
                popup.show(getSupportFragmentManager(), popup.getTag());
                break;
            case R.id.refresh_file_menu:
                swipeRefreshLayout.setRefreshing(true);
                onRefreshListener.onRefresh();
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        List<File> temp = (List<File>) getIntent().getSerializableExtra("parent");
        if (temp == null) {
            Toast.makeText(this, "Cannot open the file location", Toast.LENGTH_SHORT).show();
            finish();
        } else
            parent = temp.get(0);
        if (!parent.exists()) {
            Toast.makeText(this, "Cannot open the file location " + parent.getPath() + " .It no longer exists.", Toast.LENGTH_SHORT).show();
        }
        mydb = new DataBaseHelper(this, "favourite.db", null, 1);
        pairList = new ArrayList<>();
//        File[] tempFiles = parent.listFiles();
//        if (tempFiles != null) {
//            list = new ArrayList<>(Arrays.asList(tempFiles));
//            for (int i = 0; i < list.size(); i++) {
//                File[] array = list.get(i).listFiles();
//                if (array != null) {
//                    pairList.add(array.length);
//                } else {
//                    pairList.add(0);
//                }
//            }
//        } else pairList.add(0);

        swipeRefreshLayout = findViewById(R.id.swipe_container_activity_file);

        swipeRefreshLayout.setColorSchemeResources(R.color.tab_highlight, R.color.white);
        onRefreshListener = this;
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        Toolbar toolbar = findViewById(R.id.toolbar_activity_file);
        toolbar.setTitle(parent.getName().equals("sdcard") | parent.getName().equals("0") ? "Internal memory" : parent.getName());
        TextView path_parent = findViewById(R.id.path_parent);
        String string = new StringBuilder().append((parent.getAbsolutePath().startsWith("/sdcard")) ? parent.getAbsolutePath().replace("/sdcard", "Internal memory") : parent.getAbsolutePath()).toString().replaceAll("/", "  <  ");
        if (string.startsWith("  <  storage  <  emulated  <  0")) {
            string = string.replace("  <  storage  <  emulated  <  0", "Internal memory");
        }
        path_parent.setText(string);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MainActivity.getMainIntent();
                startActivity(intent);
//                SharedPreferences.Editor newEditor = getSharedPreferences("back_pressed", MODE_PRIVATE).edit();
//                newEditor.putBoolean("back", true);
//                newEditor.apply();
//                finish();
            }
        });

        recyclerView = findViewById(R.id.recyclerView_activity_file);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        activityFileAdapter = new ActivityFileAdapter(this, list, pairList, this, recyclerView);
        activityFileAdapter.setHasStableIds(true);
        recyclerView.setAdapter(activityFileAdapter);
        mydb.close();
        ShowAllFiles showAllFiles = new ShowAllFiles(this);
        showAllFiles.execute("start");
        showAllFiles.setAnimate(true);
        asyncTaskWeakReference = new WeakReference<>(showAllFiles);
    }


    @Override
    protected void onResume() {
        super.onResume();
//        SharedPreferences pref = getSharedPreferences("back_pressed", MODE_PRIVATE);
//        if (pref.getBoolean("back", false)) {
//            finish();
//        }
    }


    @Override
    public void onItemViewClick(int position, File file) {
        if (file == null || !file.exists()) {
            Toast.makeText(this, "Cannot open the file location " + parent.getPath() + " .It no longer exists.", Toast.LENGTH_SHORT).show();
//            finish();
            return;
        }
        if (file.isDirectory()) {
//            if (!file.getName().equals("Android") || Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            Intent intent = new Intent(this, FileActivity.class);
            List<File> tempList = new ArrayList<>();
            tempList.add(file);
            intent.putExtra("parent", (Serializable) tempList);
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (ActivityFileAdapter.isMusicFile(file.getPath())) {
            int newPos = 0;
            ArrayList<MusicFiles> musicFiles = getAllAudioExcludingSub(activityFileAdapter.getList().get(position), this);
            for (MusicFiles musicFile : musicFiles) {
                if (musicFile.getPath().equals(file.getPath())) {
                    break;
                } else
                    newPos++;
            }
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("sender", "fileActivity");
            intent.putExtra("listSongs", musicFiles);
            intent.putExtra("position", newPos);
            startActivity(intent);
        } else if (ActivityFileAdapter.isVideoFile(file.getPath())) {
            int newPos = 0;
            ArrayList<VideoFiles> videoFiles = getAllVideosExcludingSub(activityFileAdapter.getList().get(position), this);
            for (VideoFiles videoFile : videoFiles) {
                if (videoFile.getPath().equals(file.getPath())) {
                    break;
                } else
                    newPos++;
            }
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra(VIDEO_FILES, videoFiles);
            intent.putExtra(VIDEO_FILES_POS, newPos);
            startActivity(intent);
        }
    }

    public static ArrayList<VideoFiles> getAllVideosExcludingSub(File parent, Context context) {
        ArrayList<VideoFiles> tempVideoFiles = new ArrayList<>();
//        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//        String[] projection = new String[]{
//                MediaStore.Video.Media._ID,
//                MediaStore.Video.Media.DATA,  //this is for path
//                MediaStore.Video.Media.TITLE,
//                MediaStore.Video.Media.DISPLAY_NAME,  //for filename
//                MediaStore.Video.Media.SIZE,
//                MediaStore.Video.Media.DATE_ADDED,
//                MediaStore.Video.Media.DURATION,
//                MediaStore.Video.Media.RESOLUTION
//        };
//        Cursor cursor;
////        if (!parent.getAbsolutePath().equals("/sdcard")) {
//        cursor = context.getContentResolver().query(
//                uri,
//                projection,
//                MediaStore.Video.Media.DATA + " like ? and " + MediaStore.Video.Media.DATA + " not like ? ",
//                new String[]{"%" + parent.getAbsolutePath().replace("/sdcard", "") + "%", "%" + parent.getAbsolutePath() + "/%/%"},
//                null);
////        }
////        else {
////            cursor = context.getContentResolver().query(
////                    uri,
////                    projection,
////                    null,
////                    null,
////                    null);
////        }
//
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                String id = cursor.getString(0);
//                String path = cursor.getString(1);
//                String title = cursor.getString(2);
//                String filename = cursor.getString(3);
//                String size = cursor.getString(4);
//                String dateAdded = cursor.getString(5);
//                String duration = cursor.getString(6);
//                String resolution = cursor.getString(7);

        Uri uri = MediaStore.Files.getContentUri("external");
        Cursor cursor;
//        if (!parent.getAbsolutePath().equals("/sdcard")) {
        cursor = context.getContentResolver().query(
                uri,
                null,
                MediaStore.Files.FileColumns.DATA + " like ? and " + MediaStore.Files.FileColumns.DATA + " not like ? ",
                new String[]{"%" + parent.getAbsolutePath().replace(Environment.getExternalStorageDirectory().getPath(), "") + "%",
                        "%" + parent.getAbsolutePath() + "/%"},
                null);

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
                if (parent.exists()) {
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
                } else {
                    duration = "0";
                    resolution = "";
                }
                tempVideoFiles.add(new VideoFiles(id, path, title, filename, size, dateAdded, duration, resolution));
            }
            cursor.close();
        }
        return tempVideoFiles;
    }

    public static ArrayList<MusicFiles> getAllAudioExcludingSub(File parent, Context context) {
        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        Uri uri = MediaStore.Files.getContentUri("external");
        Cursor cursor;
//        if (!parent.getAbsolutePath().equals("/sdcard")) {
        cursor = context.getContentResolver().query(
                uri,
                null,
                MediaStore.Files.FileColumns.DATA + " like ? and " + MediaStore.Files.FileColumns.DATA + " not like ? ",
                new String[]{"%" + parent.getAbsolutePath().replace("/sdcard", "") + "%",
                        "%" + parent.getAbsolutePath() + "/%/%"},
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.ALBUM));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DURATION));
                if (parent.exists()) {
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(path);
                    if (duration == null) {
                        duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    }
                    if (artist == null) {
                        artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    }
                    if (album == null) {
                        album = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        mediaMetadataRetriever.close();
                    }
                } else {
                    duration = "0";
                }

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

        Cursor allOpusMediaFiles = context.getContentResolver().query(uriForOpus, null,
                MediaStore.Files.FileColumns.DATA + " like ? and " + MediaStore.Audio.Media.DATA + " not like ? ",
                new String[]{"%" + parent.getAbsolutePath().replace("/sdcard", "") + "%",
                        "%" + parent.getAbsolutePath() + "/%/%"},
                null);
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

    @Override
    public void onMoreClick(int position, File file) {
        if (file == null) {
            return;
        }
        ActivityFileAdapterBottomSheet bottomSheet = new ActivityFileAdapterBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putSerializable("file_bottom_sheet", file);
        bottomSheet.setArguments(bundle);
        bottomSheet.setAdapter(activityFileAdapter);
        bottomSheet.setPos(position);
        bottomSheet.setList(list);
        bottomSheet.setShowsDialog(true);
        bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return (!(query == null || query.equals("")));
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        List<File> temp = new ArrayList<>();
        ArrayList<Integer> tempPairList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().toLowerCase().contains(userInput)) {
                temp.add(list.get(i));
                tempPairList.add(pairList.get(i));
            }
        }
        activityFileAdapter.update(temp, tempPairList, recyclerView);
        return true;
    }

    @Override
    public void onRefresh() {
        if (show_all_files && show_hidden_files) {
            ShowAllFiles showAllFiles = new ShowAllFiles(this);
            showAllFiles.execute("start");
        } else if (!show_hidden_files && !show_all_files) {
            ShowNonHiddenFiles showNonHiddenFiles = new ShowNonHiddenFiles(this);
            showNonHiddenFiles.execute("start");
        } else if (show_all_files && !show_hidden_files) {
            ShowNonHiddenFiles showNonHiddenFiles = new ShowNonHiddenFiles(this);
            showNonHiddenFiles.execute("start");
        } else if (!show_all_files && show_hidden_files) {
            ShowOnlyMediaFiles showOnlyMediaFiles = new ShowOnlyMediaFiles(this);
            showOnlyMediaFiles.execute("start");
        }
    }


    private static class ShowAllFiles extends AsyncTask<String, String, String> {
        private final WeakReference<FileActivity> fileActivityWeakReference;
        private List<File> tempList;
        private ArrayList<Integer> tempPairList;
        private boolean animate = false;

        ShowAllFiles(FileActivity fileActivity) {
            fileActivityWeakReference = new WeakReference<>(fileActivity);
            tempList = new ArrayList<>();
            tempPairList = new ArrayList<>();
        }

        interface ShowAllFilesCompletionListener {
            void onComplete();
        }

        private ShowAllFilesCompletionListener listener;

        public void setAnimate(boolean animate) {
            this.animate = animate;
        }

        public void setListener(ShowAllFilesCompletionListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return;
            }
//            fileActivity.list.clear();
//            fileActivity.pairList.clear();
            fileActivity.swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return null;
            }
            File[] fileArray;
            fileArray = fileActivity.parent.listFiles();


            if (fileArray != null) {
                tempList = new ArrayList<>(Arrays.asList(fileArray));
                if (!fileActivity.show_hidden_files) {
                    for (int i = 0; i < tempList.size(); i++) {
                        if (tempList.get(i).isHidden())
                            tempList.remove(tempList.get(i));
                    }
                }
                for (int i = 0; i < tempList.size(); i++) {
                    File[] array = tempList.get(i).listFiles();
                    if (array != null) {
                        tempPairList.add(array.length);
                    } else {
                        tempPairList.add(0);
                    }
                }
            } else
                tempPairList.add(0);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return;
            }
            fileActivity.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fileActivity.swipeRefreshLayout.setRefreshing(false);
                }
            }, 500);
            if (fileActivity.list != tempList) {
                fileActivity.list = tempList;
                fileActivity.pairList = tempPairList;
                fileActivity.activityFileAdapter.update(tempList, tempPairList, fileActivity.recyclerView);
                if (animate)
                    fileActivity.recyclerView.scheduleLayoutAnimation();
            }
        }
    }

    private static class ShowOnlyMediaFiles extends AsyncTask<String, String, String> {
        private final WeakReference<FileActivity> fileActivityWeakReference;
        private List<File> tempList;
        private ArrayList<Integer> tempPairList;

        ShowOnlyMediaFiles(FileActivity fileActivity) {
            fileActivityWeakReference = new WeakReference<>(fileActivity);
            tempList = new ArrayList<>();
            tempPairList = new ArrayList<>();

        }

        @Override
        protected void onPreExecute() {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return;
            }
//            fileActivity.list.clear();
//            fileActivity.pairList.clear();
            fileActivity.swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return null;
            }
            File[] fileArray;
            fileArray = fileActivity.parent.listFiles();
            boolean show_hidden = fileActivity.show_hidden_files;


            if (fileArray != null) {
                for (File file : fileArray) {
                    if (file.isDirectory()
                            || ActivityFileAdapter.isMusicFile(file.getAbsolutePath())
                            || ActivityFileAdapter.isVideoFile(file.getAbsolutePath())
                    ) {
                        if (!show_hidden) {
                            if (!file.isHidden())
                                tempList.add(file);
                        } else {
                            tempList.add(file);
                        }
                    }
                }


                for (int i = 0; i < tempList.size(); i++) {
                    File[] array = tempList.get(i).listFiles();
                    if (array != null) {
                        tempPairList.add(array.length);
                    } else {
                        tempPairList.add(0);
                    }
                }
            } else
                tempPairList.add(0);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return;
            }
            fileActivity.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fileActivity.swipeRefreshLayout.setRefreshing(false);
                }
            }, 500);
            if (fileActivity.list != tempList) {
                fileActivity.list = tempList;
                fileActivity.pairList = tempPairList;
                fileActivity.activityFileAdapter.update(tempList, tempPairList, fileActivity.recyclerView);
            }
        }
    }

    private static class ShowHiddenFiles extends AsyncTask<String, String, String> {
        private final WeakReference<FileActivity> fileActivityWeakReference;
        private List<File> tempList;
        private ArrayList<Integer> tempPairList;

        ShowHiddenFiles(FileActivity fileActivity) {
            fileActivityWeakReference = new WeakReference<>(fileActivity);
            tempList = new ArrayList<>();
            tempPairList = new ArrayList<>();

        }

        @Override
        protected void onPreExecute() {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return;
            }
            fileActivity.swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return null;
            }
            File[] fileArray;
            fileArray = fileActivity.parent.listFiles();

            if (fileArray != null) {
                for (File file : fileArray) {
                    if (fileActivity.show_all_files) {
                        tempList.add(file);
                    } else {
                        if (ActivityFileAdapter.isMusicFile(file.getPath()) ||
                                ActivityFileAdapter.isVideoFile(file.getPath()) ||
                                file.isDirectory()) {
                            tempList.add(file);
                        }
                    }
                }


                for (int i = 0; i < tempList.size(); i++) {
                    File[] array = tempList.get(i).listFiles();
                    if (array != null) {
                        tempPairList.add(array.length);
                    } else {
                        tempPairList.add(0);
                    }
                }
            } else
                tempPairList.add(0);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return;
            }
            fileActivity.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fileActivity.swipeRefreshLayout.setRefreshing(false);
                }
            }, 500);
            if (fileActivity.list != tempList) {
                fileActivity.list = tempList;
                fileActivity.pairList = tempPairList;
                fileActivity.activityFileAdapter.update(tempList, tempPairList, fileActivity.recyclerView);
            }
        }
    }

    private static class ShowNonHiddenFiles extends AsyncTask<String, String, String> {
        private final WeakReference<FileActivity> fileActivityWeakReference;
        private List<File> tempList;
        private ArrayList<Integer> tempPairList;
        private boolean animate = false;

        ShowNonHiddenFiles(FileActivity fileActivity) {
            fileActivityWeakReference = new WeakReference<>(fileActivity);
            tempList = new ArrayList<>();
            tempPairList = new ArrayList<>();
        }

        public void setAnimate(boolean animate) {
            this.animate = animate;
        }

        @Override
        protected void onPreExecute() {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return;
            }
//            fileActivity.list.clear();
//            fileActivity.pairList.clear();
            fileActivity.swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return null;
            }
            File[] fileArray;
            fileArray = fileActivity.parent.listFiles();

            boolean show_all_files = fileActivity.show_all_files;
            if (fileArray != null) {
                tempList = new ArrayList<>(Arrays.asList(fileArray));
                for (int i = 0; i < tempList.size();i ++) {
                    File file = tempList.get(i);
                    if (!file.isHidden()) {
                        if (!show_all_files) {
                            if (!(ActivityFileAdapter.isMusicFile(file.getAbsolutePath()) || ActivityFileAdapter.isVideoFile(file.getAbsolutePath()))) {
                                tempList.remove(file);
                            }
                        }
                    } else {
                        tempList.remove(file);
                    }
                }

                for (int i = 0; i < tempList.size(); i++) {
                    File[] array = tempList.get(i).listFiles();
                    if (array != null) {
                        tempPairList.add(array.length);
                    } else {
                        tempPairList.add(0);
                    }
                }
            } else
                tempPairList.add(0);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            FileActivity fileActivity = fileActivityWeakReference.get();
            if (fileActivity == null) {
                return;
            }
            fileActivity.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fileActivity.swipeRefreshLayout.setRefreshing(false);
                }
            }, 500);
            if (fileActivity.list != tempList) {
                fileActivity.list = tempList;
                fileActivity.pairList = tempPairList;
                fileActivity.activityFileAdapter.update(tempList, tempPairList, fileActivity.recyclerView);
            }
        }
    }

    private static class SortBySize extends AsyncTask<Void, Void, Void> {
        private final WeakReference<FileActivity> fileActivityWeakReference;
        private List<File> originalList;
        private List<File> newList;
        private ArrayList<Integer> newPairList;

        public SortBySize(FileActivity fileActivity, List<File> list) {
            fileActivityWeakReference = new WeakReference<>(fileActivity);
            originalList = list;
            newList = new ArrayList<>(list);
            newPairList = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            FileActivity activity = fileActivityWeakReference.get();
            if (activity == null) {
                return;
            }
            activity.swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            FileActivity activity = fileActivityWeakReference.get();
            if (activity == null) {
                return null;
            }
            HashMap<File, Integer> hashMap = new HashMap<>();
            int i = 0;
            for (File file : newList) {
                hashMap.put(file, activity.pairList.get(i));
                i++;
            }
            Collections.sort(newList, new MySortBySize_FileActivity(activity.sortOrder_size));
            for (File file : newList) {
                newPairList.add((Integer) hashMap.get(file));
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void unused) {
            FileActivity activity = fileActivityWeakReference.get();
            if (activity == null) {
                return;
            }
            originalList = newList;
            activity.pairList = newPairList;
            activity.activityFileAdapter.update(originalList, activity.pairList, activity.recyclerView);
            activity.swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected void onDestroy() {
        if (asyncTaskWeakReference != null) {
            AsyncTask asyncTask = asyncTaskWeakReference.get();
            if (asyncTask != null) {
                asyncTask.cancel(true);
                asyncTaskWeakReference.clear();
                asyncTaskWeakReference = null;
            }
        }
        super.onDestroy();
    }

}