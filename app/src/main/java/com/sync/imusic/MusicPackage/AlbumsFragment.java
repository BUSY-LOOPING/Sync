package com.sync.imusic.MusicPackage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sync.imusic.AlbumAdapterSection;
import com.sync.imusic.AlbumFiles;
import com.sync.imusic.ApplicationClass;
import com.sync.imusic.MainActivity;
import com.sync.imusic.MusicFiles;
import com.sync.imusic.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;


public class AlbumsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String SORT_PREF_ALBUMS = "soring_album_fragment";
    private RecyclerView recyclerView;
    private SectionedRecyclerViewAdapter sectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter();
    private ArrayList<MusicFiles> albums = new ArrayList<>();
    private Context context;
    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<Pair<String, ArrayList<MusicFiles>>> pairArrayList = new ArrayList<>();
    private ArrayList<AlbumFiles> albumFilesArrayList = new ArrayList<>();
    private ApplicationClass ref;
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String sortBy, sortOrder;

    public AlbumsFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.album_fragment_menu, menu);
//        MenuItem menuItem = menu.findItem(R.id.search);
//        SearchView searchView = (SearchView) menuItem.getActionView();
//        searchView.setOnQueryTextListener(this);
//        searchView.setQueryHint("Search in current list");
//        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                setItemsVisibility(menu, menuItem, false);
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                ((Activity) context).invalidateOptionsMenu();
//                return false;
//            }
//        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        SharedPreferences.Editor editor = context.getSharedPreferences(SORT_PREF_ALBUMS, Context.MODE_PRIVATE).edit();
        SharedPreferences sharedPreferences = context.getSharedPreferences(SORT_PREF_ALBUMS, Context.MODE_PRIVATE);
        switch (id) {
            case R.id.sort_by_name:
                editor.putString("sorting", "sortByName");
                sortOrder = sortOrder.equals("ASC") ? "DES" : "ASC";
                editor.putString("order", sortOrder);
                new SortByName(this, sortOrder).execute();
                break;
//            case R.id.sort_by_length:
//                editor.putString("sorting", "sortByLength");
//                sortOrder = sortOrder.equals("ASC") ? "DES" : "ASC";
//                editor.putString("order", sharedPreferences.getString("order", "ASC").equals("ASC") ? "DES" : "ASC");
////                new SortByLength(context, pairArrayList, sectionedRecyclerViewAdapter, swipeRefreshLayout, sortOrder).execute();
//                break;
            case R.id.refresh:
                swipeRefreshLayout.setRefreshing(true);
                onRefreshListener.onRefresh();

        }
        editor.apply();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ref = (ApplicationClass) ((MainActivity) context).getApplication();
        SharedPreferences sharedPreferences = context.getSharedPreferences(SORT_PREF_ALBUMS, Context.MODE_PRIVATE);
        sortBy = sharedPreferences.getString("sorting", "sortByName");
        sortOrder = sharedPreferences.getString("order", "ASC");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        albums = MainActivity.musicFiles;
//        ArrayList<String> discreteAlbumNames = new ArrayList<>();
        for (int i = 0; i < albums.size(); i++) {
            ArrayList<MusicFiles> arrayList = new ArrayList<>();
            arrayList.add(albums.get(i));
            AlbumFiles albumFile = new AlbumFiles(albums.get(i).getAlbum(), 0, 0, arrayList);
            int index = albumFilesArrayList.indexOf(albumFile);
            if (index == -1) {
                albumFilesArrayList.add(albumFile);
            } else {
                String albumName = albums.get(i).getAlbum();
                boolean duplicate = false;
                ArrayList<MusicFiles> list = albumFilesArrayList.get(index).getMusicFiles();
                for (MusicFiles musicFiles : list) {
                    if (musicFiles.getAlbum().equals(albumName)) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate)
                    albumFilesArrayList.get(index).getMusicFiles().add(albums.get(i));
//                long dur = albumFilesArrayList.get(index).getDuration() + Long.parseLong(albums.get(i).getDuration());
//                albumFilesArrayList.get(index).setDuration(dur);
            }
        }
//        albums = new ArrayList<>();
//        for (int i = 0; i < discreteAlbumNames.size(); i++) {
//            for (int j = 0; j < musicFiles.size(); j++) {
//                if (musicFiles.get(j).getAlbum().equals(discreteAlbumNames.get(i))) {
//                    albums.add(musicFiles.get(j));
//                    break;
//                }
//            }
//        }
        swipeRefreshLayout = view.findViewById(R.id.swipe_container_fragment_albums);
        onRefreshListener = this;
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        swipeRefreshLayout.setColorSchemeResources(R.color.tab_highlight, R.color.white);
//        sectionedRecyclerViewAdapter.setHasStableIds(false);
        recyclerView = view.findViewById(R.id.recyclerView_fragment_albums);
        recyclerView.setHasFixedSize(true);
//        list = getList();

        ref.setSectionedRecyclerViewAdapter(sectionedRecyclerViewAdapter);
        for (int i = 0; i < albumFilesArrayList.size(); i++) {
            String albumName = albumFilesArrayList.get(i).getAlbumName();
            if (!albumName.equals("<unknown>"))
                albumName = albumName.substring(0, 1);
            sectionedRecyclerViewAdapter.addSection(new AlbumAdapterSection(context, albumName, albumFilesArrayList.get(i).getMusicFiles()));
        }


        recyclerView.setItemViewCacheSize(6);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (sectionedRecyclerViewAdapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                    return 2;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(sectionedRecyclerViewAdapter);
//        if (sortBy.equals("sortByName"))
//            new SortByName(this, sortOrder).execute();
        return view;
    }

    private ArrayList<Pair<String, ArrayList<MusicFiles>>> getPairArrayList(ArrayList<String> list) {
        ArrayList<Pair<String, ArrayList<MusicFiles>>> temp = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            temp.add(new Pair<>(list.get(i), getAlbumsBeginningWith(list.get(i))));
        }
        return temp;
    }

    private ArrayList<MusicFiles> getAlbumsBeginningWith(String s) {
        ArrayList<MusicFiles> temp = new ArrayList<>();
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getAlbum().startsWith(s)) {
                temp.add(albums.get(i));
            }
        }
        return temp;
    }

    private ArrayList<String> getList() {
        ArrayList<String> temp = new ArrayList<>();
        for (int i = 0; i < albums.size(); i++) {
            if (sortBy.equals("sortByName")) {
                String str = albums.get(i).getAlbum();
                if (!str.equals("<unknown>"))
                    str = str.substring(0, 1);
                if (!temp.contains(str)) {
                    temp.add(str);
                }
            } else if (sortBy.equals("sortByLength")) {
//                long length = 0;
//                if (length <= 60000)
//                    temp.add("0 - 1 min");
//                else if (length <= 120000)
//                    temp.add("1 - 2 min");
//                else if (length <= 180000)
//                    temp.add("2 - 3 min");
//                else if (length < 900000)
//                    temp.add("< 15 min");
//                else
//                    temp.add("> 15 min");
            }
        }

        return temp;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1500);
    }

//    @Override
//    public boolean onQueryTextSubmit(String query) {
//        return true;
//    }
//
//    @Override
//    public boolean onQueryTextChange(String newText) {
//        String userInput = newText.toUpperCase();
//        return true;
//    }

    private static class SortByName extends AsyncTask<Void, Void, Void> {
        private WeakReference<Fragment> albumsFragmentWeakReference;
        private final String sortOrder;
        private ArrayList<AlbumFiles> newAlbumFilesArrayList;

        SortByName(AlbumsFragment albumsFragment, String sortOrder) {
            albumsFragmentWeakReference = new WeakReference<>(albumsFragment);
            this.sortOrder = sortOrder;
            newAlbumFilesArrayList = new ArrayList<>(albumsFragment.albumFilesArrayList);
        }

        @Override
        protected void onPreExecute() {
            AlbumsFragment albumsFragment = (AlbumsFragment) albumsFragmentWeakReference.get();
            if (albumsFragment == null) {
                return;
            }
            albumsFragment.swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... strings) {
            Collections.sort(newAlbumFilesArrayList, new MyAlbumSortByName(sortOrder));
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            AlbumsFragment albumsFragment = (AlbumsFragment) albumsFragmentWeakReference.get();
            if (albumsFragment == null) {
                return;
            }
            albumsFragment.sectionedRecyclerViewAdapter.removeAllSections();
            for (int i = 0; i < albumsFragment.albumFilesArrayList.size(); i++) {
                String albumName = newAlbumFilesArrayList.get(i).getAlbumName();
                if (!albumName.equals("<unknown>"))
                    albumName = albumName.substring(0, 1);
                albumsFragment.sectionedRecyclerViewAdapter.addSection(new AlbumAdapterSection(albumsFragment.getContext(), albumName, newAlbumFilesArrayList.get(i).getMusicFiles()));
            }
            albumsFragment.sectionedRecyclerViewAdapter.notifyItemRangeChanged(0, newAlbumFilesArrayList.size());
            albumsFragment.swipeRefreshLayout.setRefreshing(false);
        }
    }

    private static class SortByLength extends AsyncTask<Void, Void, Void> {
        private WeakReference<Fragment> albumsFragmentWeakReference;
        private final String sortOrder;
        private ArrayList<AlbumFiles> newAlbumFilesArrayList;

        SortByLength(AlbumsFragment albumsFragment, String sortOrder) {
            albumsFragmentWeakReference = new WeakReference<>(albumsFragment);
            this.sortOrder = sortOrder;
            newAlbumFilesArrayList = new ArrayList<>(albumsFragment.albumFilesArrayList);
        }

        @Override
        protected void onPreExecute() {
            AlbumsFragment albumsFragment = (AlbumsFragment) albumsFragmentWeakReference.get();
            if (albumsFragment == null) {
                return;
            }
            albumsFragment.swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... strings) {
            Collections.sort(newAlbumFilesArrayList, new MyAlbumSortByLength(sortOrder));
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            AlbumsFragment albumsFragment = (AlbumsFragment) albumsFragmentWeakReference.get();
            if (albumsFragment == null) {
                return;
            }
            albumsFragment.sectionedRecyclerViewAdapter.removeAllSections();
            for (int i = 0; i < albumsFragment.albumFilesArrayList.size(); i++) {
                String albumName = newAlbumFilesArrayList.get(i).getAlbumName();
                albumsFragment.sectionedRecyclerViewAdapter.addSection(new AlbumAdapterSection(albumsFragment.getContext(), albumName, newAlbumFilesArrayList.get(i).getMusicFiles()));
            }
            albumsFragment.sectionedRecyclerViewAdapter.notifyItemRangeChanged(0, newAlbumFilesArrayList.size());
            albumsFragment.swipeRefreshLayout.setRefreshing(false);
        }
    }

//    private static class SortByLength extends AsyncTask<Void, Void, Void> {
//        //        private final WeakReference<AlbumsFragment> albumsFragmentWeakReference;
//        private final WeakReference<SwipeRefreshLayout> swipeRefreshLayoutWeakReference;
//        private final WeakReference<SectionedRecyclerViewAdapter> adapterWeakReference;
//        private ArrayList<Pair<String, ArrayList<MusicFiles>>> pairArrayList;
//        private final WeakReference<Context> contextWeakReference;
//        private ArrayList<Pair<String, ArrayList<MusicFiles>>> oldList;
//        private final String sortOrder;
//        private ArrayList<String> list;
//
//        SortByLength(Context context, ArrayList<Pair<String, ArrayList<MusicFiles>>> pairArrayList, SectionedRecyclerViewAdapter sectionedRecyclerViewAdapter, SwipeRefreshLayout swipeRefreshLayout , String sortOrder) {
////            albumsFragmentWeakReference = new WeakReference<AlbumsFragment>(context);
//            contextWeakReference = new WeakReference<>(context);
//            adapterWeakReference = new WeakReference<>(sectionedRecyclerViewAdapter);
//            swipeRefreshLayoutWeakReference = new WeakReference<>(swipeRefreshLayout);
//            oldList = pairArrayList;
//            this.pairArrayList = new ArrayList<>(pairArrayList);
//            this.sortOrder = sortOrder;
//            list = new ArrayList<>();
//        }
//
//        @Override
//        protected void onPreExecute() {
//            SwipeRefreshLayout swipeRefreshLayout = swipeRefreshLayoutWeakReference.get();
//            if (swipeRefreshLayout == null)
//                return;
//            swipeRefreshLayout.setRefreshing(true);
//        }
//
//        @Override
//        protected Void doInBackground(Void... strings) {
//            Collections.sort(pairArrayList, new MyAlbumSortByLength(sortOrder));
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void s) {
//            SwipeRefreshLayout swipeRefreshLayout = swipeRefreshLayoutWeakReference.get();
//            if (swipeRefreshLayout == null)
//                return;
//            SectionedRecyclerViewAdapter adapter = adapterWeakReference.get();
//            if (adapter == null)
//                return;
//            Context context = contextWeakReference.get();
//            if (context == null)
//                return;
//            oldList = pairArrayList;
//            adapter.removeAllSections();
//            for (int i = 0; i < pairArrayList.size(); i++) {
//                adapter.addSection(new AlbumAdapterSection(context, pairArrayList.get(i).first, pairArrayList.get(i).second));
//            }
//            adapter.notifyItemRangeChanged(0 , pairArrayList.size());
//            swipeRefreshLayout.setRefreshing(false);
//        }
//    }

    private static class MyAlbumSortByName implements Comparator<AlbumFiles> {
        private final String order;

        public MyAlbumSortByName(String order) {
            this.order = order;
        }

        @Override
        public int compare(AlbumFiles o1, AlbumFiles o2) {
            String s1 = o1.getAlbumName().toUpperCase();
            String s2 = o2.getAlbumName().toUpperCase();
            if (order.equals("DES"))
                return -s1.compareTo(s2);
            return s1.compareTo(s2);
        }
    }

    private static class MyAlbumSortByLength implements Comparator<AlbumFiles> {
        private final String order;

        public MyAlbumSortByLength(String order) {
            this.order = order;
        }

        @Override
        public int compare(AlbumFiles o1, AlbumFiles o2) {
            long dur1 = 0;
            long dur2 = 0;
            for (MusicFiles musicFile : o1.getMusicFiles()) {
                dur1 += Long.parseLong(musicFile.getDuration());
            }
            for (MusicFiles musicFile : o2.getMusicFiles()) {
                dur2 += Long.parseLong(musicFile.getDuration());
            }
            if (order.equals("DES"))
                return ((int) (dur2 - dur1));
            return ((int) (dur1 - dur2));
        }
    }

//    class GetAlbums extends AsyncTask<>
}