package com.example.imusic;

import static com.example.imusic.MusicService.MUSIC_NOW_PLAYING;
import static com.example.imusic.MusicService.NOW_PLAYING;
import static com.example.imusic.MusicService.initiated;
import static com.example.imusic.MusicService.playing;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.imusic.fragment.ViewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    //    private int brightness;
    public static final int REQUEST_CODE = 1;
    public static ArrayList<MusicFiles> musicFiles;
    static boolean shuffleBoolean = false, repeatBoolean = false;
    Toolbar mToolbar;
    private final String MY_SORT_PREF = "SortOrder";
    static ViewPager viewPager;
    BottomNavigationView mBottomNavigationView;
    public static MiniPlayer miniPlayer;
    private final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    public static boolean mExternalStorageAvailable = false;
    public static boolean mExternalStorageWriteable = false;
    private Thread.UncaughtExceptionHandler defaultUEH;
    // handler listener
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e("", ex.toString());
            // TODO handle exception here
            SharedPreferences.Editor editor = getSharedPreferences(MUSIC_NOW_PLAYING, MODE_PRIVATE).edit();
            editor.putString(NOW_PLAYING, "false");
            editor.apply();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);
//        hasPermissions(this);
        permission();
        appInitialization();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Set<String> set = MediaStore.getExternalVolumeNames(this);
//            if (set != null && set.size() > 1)
//                no_storages = set.size();
//            Toast.makeText(this, "" + set.size(), Toast.LENGTH_SHORT).show();
//        }

    }

    private void appInitialization() {
        new CheckDb(this).start();
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }

    private void checkExternalMedia() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // Can read and write the media
                mExternalStorageAvailable = mExternalStorageWriteable = true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // Can only read the media
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
            } else {
                // Can't read or write
                mExternalStorageAvailable = mExternalStorageWriteable = false;
            }
        } else {
            Set<String> set = MediaStore.getExternalVolumeNames(this);
            if (set != null && set.size() > 1)
                mExternalStorageAvailable = mExternalStorageWriteable = true;
        }
    }

    /**
     * Method to write ascii text characters to file on SD card. Note that you must add a
     * WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     * a FileNotFound Exception because you won't have write permission.
     */


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean hasPermissions(Context context) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    private void permission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CODE);
        }
//        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
//        }
        else {
            musicFiles = getAllAudio(this);
            initViewPager();
            checkExternalMedia();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //do
                musicFiles = getAllAudio(this);
                initViewPager();
                checkExternalMedia();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_CODE);
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
            }
        }
//        if (!hasPermissions(this)) {
//            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
//        } else {
//            musicFiles = getAllAudio(this);
//            initViewPager();
//        }
    }


    private void initViewPager() {
        miniPlayer = new MiniPlayer(this, findViewById(R.id.coordinator_main).getRootView());
        viewPager = findViewById(R.id.view_pager_main);
        viewPager.setOffscreenPageLimit(5);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
//        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mBottomNavigationView.getLayoutParams();
//        layoutParams.setBehavior(new BottomNavigationViewBehavior());
        FloatingActionButton fab = findViewById(R.id.fab_shuffle);
        CoordinatorLayout.LayoutParams layoutParams_fab = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
//        layoutParams_fab.setBehavior(new BottomNavigationFABBehavior());
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mBottomNavigationView.getMenu().findItem(R.id.page_1).setChecked(true);
                        break;
                    case 1:
                        mBottomNavigationView.getMenu().findItem(R.id.page_2).setChecked(true);
                        break;
                    case 2:
                        mBottomNavigationView.getMenu().findItem(R.id.page_3).setChecked(true);
                        break;
                    case 3:
                        mBottomNavigationView.getMenu().findItem(R.id.page_4).setChecked(true);
                        break;
                    case 4:
                        mBottomNavigationView.getMenu().findItem(R.id.page_5).setChecked(true);
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.page_1:
                            viewPager.setCurrentItem(0);
                            break;
                        case R.id.page_2:
                            viewPager.setCurrentItem(1);
                            break;
                        case R.id.page_3:
                            viewPager.setCurrentItem(2);
                            break;
                        case R.id.page_4:
                            viewPager.setCurrentItem(3);
                            break;
                        case R.id.page_5:
                            viewPager.setCurrentItem(4);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            }
        });
    }


    public ArrayList<MusicFiles> getAllAudio(Context context) {
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
                MediaStore.Audio.Media.DATE_ADDED,
        };
        Cursor cursor = context.getContentResolver().query(uri, projection,
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);
                String size = cursor.getString(6);
                String date_added = cursor.getString(7);

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

        Cursor allOpusMediaFiles = context.getContentResolver().query(uriForOpus, null, selectionForOpus, null, null);
        if (allOpusMediaFiles != null) {
            while (allOpusMediaFiles.moveToNext()) {
                String album = "<unknown>";
                String title = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                String duration = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.DURATION));
                String path = allOpusMediaFiles.getString(allOpusMediaFiles.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String artist = "<unknown>";
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

        SharedPreferences preferences = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE);
        String sortOrder = preferences.getString("sorting", "sortByName");
        if (sortOrder.equals("sortBySize")) {
            tempAudioList = sortBySize(tempAudioList);
        } else {
            tempAudioList = sortByName(tempAudioList);
        }
        return tempAudioList;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        try {
//            brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
//        } catch (Settings.SettingNotFoundException e) {
//            e.printStackTrace();
//        }
        if (miniPlayer != null)
            miniPlayer.removeCallbacks();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (brightness > 0 && Settings.System.canWrite(this)) {
//                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
//            }
//        }


        SharedPreferences sharedPreferences = getSharedPreferences(MUSIC_NOW_PLAYING, MODE_PRIVATE);
        if (miniPlayer != null) {
            if (miniPlayer.isShown && !new File(miniPlayer.getMusicService().nowPlaying().getPath()).exists()) {
                miniPlayer.hidePlayer();
                NotificationManager notificationManager = (NotificationManager) this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1);
                miniPlayer.getMusicService().stopSelf();
                initiated = false;
                playing = false;
            }
//            sharedPreferences.getString(NOW_PLAYING, "false").equals("true")
            if (initiated) {
                miniPlayer.addCallbacks();
                miniPlayer.bindService();
                MiniPlayer.setFlag(false);
                miniPlayer.showPlayer();
                if (playing)
                    miniPlayer.anim_playToPause();
                else
                    miniPlayer.anim_PauseToPlay();
            }
        }
    }

    ArrayList<MusicFiles> sortByName(ArrayList<MusicFiles> listToSort) {
        Collections.sort(listToSort, new MySortByName());
        return listToSort;
    }

    ArrayList<MusicFiles> sortBySize(ArrayList<MusicFiles> listToSort) {
        Collections.sort(listToSort, new MySortBySize());
        return listToSort;
    }

    @Override
    public void onBackPressed() {
        if (miniPlayer == null) super.onBackPressed();
        else {
            if (miniPlayer.returnInstance().getState() == BottomSheetBehavior.STATE_COLLAPSED)
                super.onBackPressed();
            else {
                if (miniPlayer.searchViewEditText.hasFocus()) {
                    miniPlayer.searchViewEditText.clearFocus();
                } else {
                    miniPlayer.reachedTop = true;
                    miniPlayer.returnInstance().setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        miniPlayer.destroy();
//        if (initiated && !playing) {
//            NotificationManager notificationManager = (NotificationManager) this.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.cancel(1);
//        }
//        SharedPreferences sharedPreferences = getSharedPreferences("demoteNotifyBar", MODE_PRIVATE);
//        if (sharedPreferences.getBoolean("demoted", false)) {

//        }
//        getSupportFragmentManager().beginTransaction().remove(new AlbumsFragment()).commitAllowingStateLoss();

    }
}