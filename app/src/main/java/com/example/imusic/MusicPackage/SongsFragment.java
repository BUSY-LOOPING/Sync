package com.example.imusic.MusicPackage;

import static android.content.Context.MODE_PRIVATE;
import static com.example.imusic.MainActivity.musicFiles;
import static com.example.imusic.MusicService.MUSIC_ID;
import static com.example.imusic.MusicService.MUSIC_NOW_PLAYING;
import static com.example.imusic.MusicService.PLAYING_FROM;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.imusic.DeleteMusicFile;
import com.example.imusic.MusicAdapter;
import com.example.imusic.MusicFiles;
import com.example.imusic.MySortByName;
import com.example.imusic.MySortBySize;
import com.example.imusic.OnItemClickListenerMusicAdapter;
import com.example.imusic.PlayerActivity;
import com.example.imusic.R;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SongsFragment extends Fragment implements OnItemClickListenerMusicAdapter, SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {

    public static MusicAdapter musicAdapter;
    private final String MY_SORT_PREF = "SortOrder";
    DeleteMusicFile deleteMusicFile;
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;

    public SongsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d("mytag", "OnCreate Songs Fragment");
    }

    @Nullable
    @Override
    public View getView() {
        return super.getView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(8);
        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
                return true;
            }

            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
                return true;
            }
        });
        musicAdapter = new MusicAdapter(context, musicFiles, this, recyclerView);
        musicAdapter.setHasStableIds(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(musicAdapter);
        swipeRefreshLayout = view.findViewById(R.id.swipe_container_fragment_songs);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.tab_highlight, R.color.white);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = context.getSharedPreferences(MUSIC_NOW_PLAYING, MODE_PRIVATE);
        String last_played_id = preferences.getString(MUSIC_ID, null);
        String play_from = preferences.getString(PLAYING_FROM, null);
        if (play_from != null && play_from.equals("mainActivity")) {
            if (last_played_id != null) {
                musicAdapter.updateNowPlaying(preferences.getString(MUSIC_ID, null), -1);
//                musicAdapter.notifyDataSetChanged();
            }
        }
        Log.d("mytag", "onresume SongsFragment");
    }

    @Override
    public void onMoreClick(int position, View v) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.delete:
                    deleteFile(position, v);
                    break;
            }
            return true;
        });

    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getContext(), PlayerActivity.class);
        intent.putExtra("sender", "mainActivity");
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @Override
    public void onLongPressed(int position) {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void deleteFile(int position, View v) {
        Snackbar snackbar = Snackbar.make(v, "Delete the file ' " + musicAdapter.getFiles().get(position).getTitle() + " '", Snackbar.LENGTH_LONG)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MusicFiles temp = musicAdapter.getFiles().get(position);
                        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                Long.parseLong(temp.getId()));
                        File file = new File(temp.getPath());
                        if (deleteMusicFile != null) {
                            deleteMusicFile.delete(temp);
                        }
                        boolean deleted = file.delete();
                        Snackbar result;
                        if (deleted) {
                            context.getContentResolver().delete(contentUri, null, null);
                            musicAdapter.delete(position);
                            musicAdapter.notifyItemRemoved(position);

                            result = Snackbar.make(v, "File Deleted", Snackbar.LENGTH_LONG);
                        } else {
                            result = Snackbar.make(v, "File cannot be deleted", Snackbar.LENGTH_LONG);
                        }
                        View resultView = result.getView();
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) resultView.getLayoutParams();
                        params.gravity = Gravity.TOP;
                        resultView.setLayoutParams(params);
                        result.show();
                    }
                });
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.tab_highlight));
        View view = snackbar.getView();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.TOP;
        view.setLayoutParams(params);
        snackbar.show();

    }


    @Override
    public void onRefresh() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                ArrayList<MusicFiles> temp = getAllAudio(context);
//                if (!temp.equals(musicFiles)) {
//                    musicAdapter.updateList(temp);
//            if (miniPlayer != null && miniPlayer.isShown && !new File(miniPlayer.getMusicService().nowPlaying().getPath()).exists()) {
//                miniPlayer.hidePlayer();
//                NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.cancel(1);
//                miniPlayer.getMusicService().stopSelf();
//                MusicService.initiated = false;
//                MusicService.playing = false;
//                }

                musicAdapter.refresh(temp);
                musicFiles = temp;
            }
        };
        thread.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                thread.interrupt();
            }
        }, 1500);
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
        SharedPreferences preferences = context.getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE);
        String sortOrder = preferences.getString("sorting", "sortByName");
        if (sortOrder.equals("sortBySize")) {
            tempAudioList = sortBySize(tempAudioList);
        } else {
            tempAudioList = sortByName(tempAudioList);
        }

        return tempAudioList;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search Media");
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE).edit();
        switch (item.getItemId()) {
            case R.id.by_name:
                editor.putString("sorting", "sortByName");
                editor.apply();
                musicFiles = sortByName(musicFiles);
                musicAdapter.updateList(musicFiles);
                break;

            case R.id.by_date:
//                editor.putString("sorting", "sortByDate");
//                editor.apply();
//                Intent intent2 = new Intent(getContext(), MainActivity.class);
//                startActivity(intent2);
//                this.finish();
                break;

            case R.id.by_size:
                editor.putString("sorting", "sortBySize");
                editor.apply();
                musicFiles = sortBySize(musicFiles);
                musicAdapter.updateList(musicFiles);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<MusicFiles> sortByName(ArrayList<MusicFiles> listToSort) {
        Collections.sort(listToSort, new MySortByName());
        for (int i = 0; i < listToSort.size(); i++) {
            Log.d("sorted", listToSort.get(i).getTitle());
        }
        return listToSort;
    }

    private ArrayList<MusicFiles> sortBySize(ArrayList<MusicFiles> listToSort) {
        Collections.sort(listToSort, new MySortBySize());
        for (int i = 0; i < listToSort.size(); i++) {
            Log.d("sorted", listToSort.get(i).getTitle());
        }
        return listToSort;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<MusicFiles> myFiles = new ArrayList<>();
        for (MusicFiles song : musicFiles) {
            if (song.getTitle().toLowerCase().contains(userInput)) {
                myFiles.add(song);
            }
        }
        musicAdapter.updateList(myFiles);
        return true;
    }

}