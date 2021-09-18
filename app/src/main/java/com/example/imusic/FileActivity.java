package com.example.imusic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.example.imusic.fragment.BrowseFragment;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class FileActivity extends AppCompatActivity implements OnClickListenerActivityFileAdapter, SearchView.OnQueryTextListener {
    public static final String PARENT = "file_parent";
    File parent;
    boolean isFav = false;
    public static final String IS_FAV = "IS_FAVOURITE";
    public static final String STRING_FAV = "STRING_FAV";
    private DataBaseHelper mydb;
    private List<File> list = new ArrayList<>();
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
                    Toast.makeText(this, "Removed", Toast.LENGTH_SHORT).show();
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
                activityFileAdapter.update(list, recyclerView);
                if (sortOrder_name.equals("ASC"))
                    sortOrder_name = "DES";
                else
                    sortOrder_name = "ASC";
                break;

            case R.id.sort_by_size_file_menu:
                Collections.sort(list, new MySortBySize_FileActivity(sortOrder_size));
                activityFileAdapter.update(list, recyclerView);
                if (sortOrder_size.equals("ASC"))
                    sortOrder_size = "DES";
                else
                    sortOrder_size = "ASC";
                break;

            case R.id.show_all_files:
                item.setChecked(!item.isChecked());
                break;
            case R.id.show_hidden_files:
                item.setChecked(!item.isChecked());
                break;
            case R.id.add_this_folder_n_subfoldes:
                Bundle bundle = new Bundle();
                bundle.putSerializable(PARENT, parent);
                AddToPlaylistPopup popup = new AddToPlaylistPopup();
                popup.setArguments(bundle);
                popup.setShowsDialog(true);
                popup.show(getSupportFragmentManager(), popup.getTag());
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        List<File> temp = (List<File>) getIntent().getSerializableExtra("parent");
        parent = temp.get(0);
        mydb = new DataBaseHelper(this, "favourite.db", null, 1);
        ArrayList<Integer> pairList = new ArrayList<>();
        File[] tempFiles = parent.listFiles();
        if (tempFiles != null) {
            list = Arrays.asList(tempFiles);
            for (int i = 0; i < list.size(); i++) {
                File[] array = list.get(i).listFiles();
                if (array != null) {
                    pairList.add(array.length);
                } else {
                    pairList.add(0);
                }
            }
        } else pairList.add(0);


        Toolbar toolbar = findViewById(R.id.toolbar_activity_file);
        toolbar.setTitle(parent.getName().equals("sdcard") ? "Internal memory" : parent.getName());
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
//            }
//        } else {
//
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
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().toLowerCase().contains(userInput)) {
                temp.add(list.get(i));
            }
        }
        activityFileAdapter.update(temp, recyclerView);
        return true;
    }
}