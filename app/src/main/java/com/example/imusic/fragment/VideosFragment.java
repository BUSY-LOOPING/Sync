package com.example.imusic.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.imusic.R;
import com.example.imusic.VideoAdapter;
import com.example.imusic.VideoFiles;

import java.util.ArrayList;
import java.util.List;

public class VideosFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
    private ArrayList<VideoFiles> videoFiles;
    private VideoAdapter videoAdapter;
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;

    public VideosFragment() {
    }

    public static VideosFragment newInstance() {

        Bundle args = new Bundle();

        VideosFragment fragment = new VideosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (context != null)
            videoFiles = getAllVideos(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_videos, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_fragment_videos);
        videoAdapter = new VideoAdapter(context, videoFiles, recyclerView);
//        videoAdapter.setHasStableIds(false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(videoAdapter);

        swipeRefreshLayout = view.findViewById(R.id.swipe_container_fragment_videos);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.tab_highlight, R.color.white);
        return view;

    }

    public ArrayList<VideoFiles> getAllVideos(Context context) {
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


        Cursor cursor = context.getContentResolver().query(
                uri,
                projection,
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                String filename = cursor.getString(3);
                String size = cursor.getString(4);
                String dateAdded = cursor.getString(5);
                String duration = cursor.getString(6);
                String resolution = cursor.getString(7);

                tempVideoFiles.add(new VideoFiles(id, path, title, filename, size, dateAdded, duration, resolution));
            }
            cursor.close();
        }
        return tempVideoFiles;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        ArrayList<VideoFiles> tempArrayList = getAllVideos(context);
        tempArrayList.removeAll(videoFiles);
        if (tempArrayList.size() > 0)
            videoAdapter.refresh(tempArrayList);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1500);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.video_fragment_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_item_fragment_videos);
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
                ((Activity) context).invalidateOptionsMenu();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<VideoFiles> tempVideoFiles = new ArrayList<>();
        for (VideoFiles video : videoFiles) {
            if (video.getTitle().toLowerCase().contains(userInput)) {
                tempVideoFiles.add(video);
            }
        }
        videoAdapter.updateList(tempVideoFiles);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences.Editor editor = context.getSharedPreferences("back_pressed", Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }
}