package com.example.imusic;

import static com.example.imusic.fragment.MoreFragment.HISTORY_FILES;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class HistoryDetailsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ArrayList<PlaylistFiles> playlistFiles;
    public static HistoryDetailsActivityAdapter historyDetailsActivityAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeRefreshLayout.OnRefreshListener refreshListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);
        init();
        listeners();
        setRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_history_details_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_history_details_menu);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_history_details_menu:
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                        refreshListener.onRefresh();
                    }
                });
                break;
        }
        return true;
    }

    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
    }

    private void setRecyclerView() {
        historyDetailsActivityAdapter = new HistoryDetailsActivityAdapter(this, playlistFiles);
        recyclerView.setHasFixedSize(true);
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
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(historyDetailsActivityAdapter);
    }

    private void listeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.smoothScrollToPosition(playlistFiles.size() - 1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1500);
            }
        };
        swipeRefreshLayout.setOnRefreshListener(refreshListener);
    }

    private void init() {
        playlistFiles = (ArrayList<PlaylistFiles>) getIntent().getSerializableExtra(HISTORY_FILES);
        recyclerView = findViewById(R.id.recycler_view_history_details);
        toolbar = findViewById(R.id.toolbar_history_details);
        setSupportActionBar(toolbar);
        swipeRefreshLayout = findViewById(R.id.swipe_container_history_details);
        swipeRefreshLayout.setColorSchemeResources(R.color.tab_highlight, R.color.white);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }

    @Override
    public void onRefresh() {
        recyclerView.smoothScrollToPosition(playlistFiles.size() - 1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1500);
    }
}