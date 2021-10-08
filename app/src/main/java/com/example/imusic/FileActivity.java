package com.example.imusic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import com.example.imusic.fragment.BrowseFragment;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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


    RecyclerView recyclerView;
    ActivityFileAdapter activityFileAdapter;

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
                sort_no_media(list, pairList);
                activityFileAdapter.update(list, pairList, recyclerView);
                if (sortOrder_name.equals("ASC"))
                    sortOrder_name = "DES";
                else
                    sortOrder_name = "ASC";
                break;

            case R.id.sort_by_size_file_menu:
                Collections.sort(list, new MySortBySize_FileActivity(sortOrder_size));
                sort_no_media(list, pairList);
                activityFileAdapter.update(list, pairList, recyclerView);
                if (sortOrder_size.equals("ASC"))
                    sortOrder_size = "DES";
                else
                    sortOrder_size = "ASC";
                break;

            case R.id.show_all_files:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    show_all_files = true;
                    ShowAllFiles showAllFiles = new ShowAllFiles();
                    showAllFiles.execute("start");
                } else {
                    show_all_files = false;
                    ShowOnlyMediaFiles showOnlyMediaFiles = new ShowOnlyMediaFiles();
                    showOnlyMediaFiles.execute("start");
                }
                break;
            case R.id.show_hidden_files:
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    show_hidden_files = true;
                } else {
                    show_hidden_files = false;
                    ShowNonHiddenFiles showNonHiddenFiles = new ShowNonHiddenFiles();
                    showNonHiddenFiles.execute("start");
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

    private void sort_no_media(List<File> key, ArrayList<Integer> tempList) {

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
//            list = Arrays.asList(tempFiles);
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
        ShowAllFiles showAllFiles = new ShowAllFiles();
        showAllFiles.execute("start");
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
                SharedPreferences.Editor newEditor = getSharedPreferences("back_pressed", MODE_PRIVATE).edit();
                newEditor.putBoolean("back", true);
                newEditor.apply();
                finish();
            }
        });

        recyclerView = findViewById(R.id.recyclerView_activity_file);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        activityFileAdapter = new ActivityFileAdapter(this, list, pairList, this);
        activityFileAdapter.setHasStableIds(true);
        recyclerView.setAdapter(activityFileAdapter);
        mydb.close();
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = getSharedPreferences("back_pressed", MODE_PRIVATE);
        if (pref.getBoolean("back", false)) {
            finish();
        }
    }

    @Override
    public void onItemViewClick(int position, File file) {
        if (!file.exists()) {
            Toast.makeText(this, "Cannot open the file location " + parent.getPath() + " .It no longer exists.", Toast.LENGTH_SHORT).show();
//            finish();
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
        }
    }

    @Override
    public void onMoreClick(int position, File file) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
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
            ShowAllFiles showAllFiles = new ShowAllFiles();
            showAllFiles.execute("start");
        } else if (!show_hidden_files && !show_all_files) {
            ShowNonHiddenFiles showNonHiddenFiles = new ShowNonHiddenFiles();
            showNonHiddenFiles.execute("start");
        } else if (show_all_files && !show_hidden_files) {
            ShowNonHiddenFiles showNonHiddenFiles = new ShowNonHiddenFiles();
            showNonHiddenFiles.execute("start");
        } else if (!show_all_files && show_hidden_files) {
            ShowOnlyMediaFiles showOnlyMediaFiles = new ShowOnlyMediaFiles();
            showOnlyMediaFiles.execute("start");
        }
    }

    private class ShowNonHiddenFiles extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            FileFilter fileFilter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (!show_all_files) {
                        return ((file.isDirectory()
                                || ActivityFileAdapter.isMusicFile(file.getAbsolutePath())
                                || ActivityFileAdapter.isVideoFile(file.getAbsolutePath())) && !file.isHidden());
                    }
                    return !file.isHidden();
                }
            };
            File[] fileArray;
            fileArray = parent.listFiles(fileFilter);
            list.clear();
            if (fileArray != null) {
                list = new ArrayList<>(Arrays.asList(fileArray));
                for (int i = 0; i < list.size(); i++) {
                    File[] array = list.get(i).listFiles();
                    if (array != null) {
                        pairList.add(array.length);
                    } else {
                        pairList.add(0);
                    }
                }
            } else
                pairList.add(0);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 500);
            activityFileAdapter.update(list, pairList, recyclerView);
        }
    }

    private class ShowAllFiles extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            File[] fileArray;
//            if (!show_hidden_files) {
//                FileFilter fileFilter = new FileFilter() {
//                    @Override
//                    public boolean accept(File file) {
//                        return !file.isHidden();
//                    }
//                };
//                fileArray = parent.listFiles(fileFilter);
//            } else
                fileArray = parent.listFiles();
            list.clear();
            if (fileArray != null) {
                list = new ArrayList<>(Arrays.asList(fileArray));
                for (int i = 0; i < list.size(); i++) {
                    File[] array = list.get(i).listFiles();
                    if (array != null) {
                        pairList.add(array.length);
                    } else {
                        pairList.add(0);
                    }
                }
            } else
                pairList.add(0);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 500);
            activityFileAdapter.update(list, pairList, recyclerView);
        }
    }

    private class ShowOnlyMediaFiles extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            File[] fileArray;
            fileArray = parent.listFiles();
            list.clear();

            if (fileArray != null) {
                for (File file : fileArray) {
                    if (file.isDirectory()
                            || ActivityFileAdapter.isMusicFile(file.getAbsolutePath())
                            || ActivityFileAdapter.isVideoFile(file.getAbsolutePath())
                    ) {
                        list.add(file);
                        Log.d("mylog", "doInBackground: " + file.getName());
                    }
                }

                for (int i = 0; i < list.size(); i++) {
                    File[] array = list.get(i).listFiles();
                    if (array != null) {
                        pairList.add(array.length);
                    } else {
                        pairList.add(0);
                    }
                }
            } else
                pairList.add(0);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 500);
            activityFileAdapter.update(list, pairList, recyclerView);
        }
    }
}