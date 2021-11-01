package com.sync.imusic.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sync.imusic.ApplicationClass;
import com.sync.imusic.MainActivity;
import com.sync.imusic.R;
import com.sync.imusic.VideoAdapter;
import com.sync.imusic.VideoFiles;
import com.sync.imusic.VideoPlayerActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VideosFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
    private ArrayList<VideoFiles> videoFiles;
    private VideoAdapter videoAdapter;
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener;
    private ApplicationClass ref = null;
    private LinearLayoutManager llm;
    private String sortByNameOrder = "ASC";
    private String sortByLengthOrder = "ASC";
    private String sortByRecentlyAddedOrder = "ASC";

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
        if (context != null) {
            videoFiles = getAllVideos(context);
            ref = (ApplicationClass) ((MainActivity) context).getApplication();
        }
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
        llm = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(videoAdapter);
        if (ref != null) {
            ref.setVideoAdapter(videoAdapter);
        }

        swipeRefreshLayout = view.findViewById(R.id.swipe_container_fragment_videos);
        onRefreshListener = this;
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
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
                Log.d("newLog", "date = " + dateAdded);

                tempVideoFiles.add(new VideoFiles(id, path, title, filename, size, dateAdded, duration, resolution));
            }
            cursor.close();
        }
        return tempVideoFiles;
    }

    @Override
    public void onRefresh() {
        ArrayList<VideoFiles> tempArrayList = getAllVideos(context);
        if (!videoFiles.equals(tempArrayList)) {
            videoFiles = tempArrayList;
            videoAdapter.refresh(tempArrayList);
        }
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_video_fragment:
                swipeRefreshLayout.setRefreshing(true);
                onRefreshListener.onRefresh();
                break;
            case R.id.resume_playback:
                SharedPreferences sharedPreferences = context.getSharedPreferences("last_played_vid", MODE_PRIVATE);
                String vidId = sharedPreferences.getString("id", null);
                String path = sharedPreferences.getString("path", null);
                String title = sharedPreferences.getString("title", null);
                String filename = sharedPreferences.getString("filename", null);
                String size = sharedPreferences.getString("size", null);
                String dateAdded = sharedPreferences.getString("dateAdded", null);
                String duration = sharedPreferences.getString("duration", null);
                String resolution = sharedPreferences.getString("resolution", null);
                if (vidId == null || path == null || title == null || filename == null || size == null || dateAdded == null || duration == null || resolution == null) {
                    Toast.makeText(context, "Playback cannot be resumed", Toast.LENGTH_SHORT).show();
                    return true;
                }
                int pos = sharedPreferences.getInt("pos", 0);
                VideoFiles lastPlayedFile = new VideoFiles(vidId, path, title, filename, size, dateAdded, duration, resolution);
                ArrayList<VideoFiles> tempList = new ArrayList<>();
                tempList.add(lastPlayedFile);
                int index = videoFiles.indexOf(lastPlayedFile);
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra(VideoAdapter.VIDEO_FILES, index == -1 ? tempList : videoFiles);
                intent.putExtra(VideoAdapter.VIDEO_FILES_POS, index == -1 ? 0 : index);
                intent.putExtra(VideoAdapter.VIDEO_PLAYBACK_POS, pos);
                context.startActivity(intent);
                break;
            case R.id.sortByName:
                Collections.sort(videoFiles, new SortByName(sortByNameOrder));
                videoAdapter.notifyDataSetChanged();
                if (sortByNameOrder.equals("ASC")) {
                    sortByNameOrder = "DES";
                } else {
                    sortByNameOrder = "ASC";
                }
                break;

            case R.id.sortByLength:
                Collections.sort(videoFiles, new SortByLength(sortByLengthOrder));
                videoAdapter.notifyDataSetChanged();
                if (sortByLengthOrder.equals("ASC")) {
                    sortByLengthOrder = "DES";
                } else {
                    sortByLengthOrder = "ASC";
                }
                break;
            case R.id.sortRecentlyAdded:
//                Collections.sort(videoFiles, new SortByRecentlyAdded(sortByRecentlyAddedOrder));
//                videoAdapter.notifyDataSetChanged();
//                if (sortByRecentlyAddedOrder.equals("ASC")) {
//                    sortByRecentlyAddedOrder = "DES";
//                } else {
//                    sortByRecentlyAddedOrder = "ASC";
//                }
                break;
        }
        return super.onOptionsItemSelected(item);
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (!sharedPreferences.getBoolean("resume_playback", true)) {
            if (llm != null) {
                int startPos = llm.findFirstVisibleItemPosition();
                int lastPos = llm.findLastVisibleItemPosition();
                for (int i = startPos; i <= lastPos; i++) {
                    videoAdapter.notifyItemChanged(i);
                }
            }
        }
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            FloatingActionButton fab = mainActivity.findViewById(R.id.fab_shuffle);
            if (fab != null) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                params.setAnchorId(R.id.mini_player_container);
                params.gravity = Gravity.END;
                fab.setLayoutParams(params);
                fab.setImageResource(R.drawable.ic_play);
                fab.show();

                fab.setOnClickListener(v -> {
                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                    intent.putExtra(VideoAdapter.VIDEO_FILES, videoFiles);
                    intent.putExtra(VideoAdapter.VIDEO_FILES_POS, 0);
                    context.startActivity(intent);
                });
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            FloatingActionButton fab = mainActivity.findViewById(R.id.fab_shuffle);
            if (fab != null) {
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                layoutParams.setAnchorId(View.NO_ID);
                fab.setLayoutParams(layoutParams);
                fab.hide(
                        new FloatingActionButton.OnVisibilityChangedListener() {
                            @Override
                            public void onHidden(FloatingActionButton fab) {
                                super.onHidden(fab);
                                fab.setVisibility(View.INVISIBLE);
                            }

                        });
            }
        }
    }

    private static class SortByRecentlyAdded implements Comparator<VideoFiles> {
        private final String sortOrder;
        public SortByRecentlyAdded(String sortOrder) {
            this.sortOrder = sortOrder;
        }
        @Override
        public int compare(VideoFiles o1, VideoFiles o2) {
            long len1 = Integer.parseInt(o1.getDateAdded());
            long len2 = Integer.parseInt(o2.getDateAdded());
            if (sortOrder.equals("DES")){
                return (int)(len2 - len1);
            }
            return (int)(len1 - len2);
        }
    }

    private static class SortByLength implements Comparator<VideoFiles> {
        private final String sortOrder;
        public SortByLength(String sortOrder) {
            this.sortOrder = sortOrder;
        }
        @Override
        public int compare(VideoFiles o1, VideoFiles o2) {
            int len1 = Integer.parseInt(o1.getDuration());
            int len2 = Integer.parseInt(o2.getDuration());
            if (sortOrder.equals("DES")){
                return len2 - len1;
            }
            return len1 - len2;
        }
    }

    private static class SortByName implements Comparator<VideoFiles> {
        private final String sortOrder;
        public SortByName(String sortOrder) {
            this.sortOrder = sortOrder;
        }
        @Override
        public int compare(VideoFiles o1, VideoFiles o2) {
            if (sortOrder.equals("DES")){
                return -o1.getTitle().toUpperCase().compareTo(o2.getTitle().toUpperCase());
            }
            return o1.getTitle().toUpperCase().compareTo(o2.getTitle().toUpperCase());
        }
    }
}