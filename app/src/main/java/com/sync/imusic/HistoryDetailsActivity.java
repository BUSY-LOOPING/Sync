package com.sync.imusic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.sync.imusic.fragment.MoreFragment;

import java.util.ArrayList;
import java.util.List;

public class HistoryDetailsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ArrayList<PlaylistFiles> playlistFiles;
    public static HistoryDetailsActivityAdapter historyDetailsActivityAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeRefreshLayout.OnRefreshListener refreshListener;
    private DataBaseHelperHistory db;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);
        init();
        listeners();
        setRecyclerView();
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        view = super.onCreateView(parent, name, context, attrs);
        return view;
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

    @SuppressLint("NonConstantResourceId")
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
            case R.id.clear_history_details_menu:
                Snackbar.make(getWindow().getDecorView().findViewById(R.id.container_history_details), "Clear history?", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        historyDetailsActivityAdapter.removeAll();
                        MoreFragment.historyAdapter.removeAll();
                        boolean flag = HistoryDetailsActivity.this.deleteDatabase("history.db");
                        if (flag)
                            Toast.makeText(HistoryDetailsActivity.this, "Cleared history", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(HistoryDetailsActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                }).setActionTextColor(ContextCompat.getColor(this, R.color.tab_highlight)).show();
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
        db = new DataBaseHelperHistory(HistoryDetailsActivity.this, "history.db", null, 1);
        db.setAdapter(MoreFragment.historyAdapter);
        db.setAdapter(historyDetailsActivityAdapter);
        historyDetailsActivityAdapter = new HistoryDetailsActivityAdapter(this, playlistFiles, db);
//        historyDetailsActivityAdapter.setHasStableIds(true);
        //sethasstableids will cause crash if we use notifyitemchanged
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
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(historyDetailsActivityAdapter);
    }

    private void listeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (playlistFiles.size() > 0) {
                    recyclerView.smoothScrollToPosition(playlistFiles.size() - 1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 1500);
                } else swipeRefreshLayout.setRefreshing(false);
            }
        };
        swipeRefreshLayout.setOnRefreshListener(refreshListener);
    }

    private void init() {
        playlistFiles = (ArrayList<PlaylistFiles>) getIntent().getSerializableExtra(MoreFragment.HISTORY_FILES);
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
        String userInput = newText.toLowerCase();
        ArrayList<PlaylistFiles> temp = new ArrayList<>();
        for (int i = 0; i < playlistFiles.size(); i++) {
            if (playlistFiles.get(i).isMusicFile) {
                if (playlistFiles.get(i).getMusicFiles().getTitle().toLowerCase().contains(userInput))
                    temp.add(playlistFiles.get(i));
            } else {
                if (playlistFiles.get(i).getVideoFiles().getTitle().toLowerCase().contains(userInput))
                    temp.add(playlistFiles.get(i));
            }
        }
        historyDetailsActivityAdapter.update(temp, recyclerView);
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

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            final int dragFlags = 0;
            final int swipeFlags = historyDetailsActivityAdapter.actionMode != null ? 0 : ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            String id = playlistFiles.get(pos).isMusicFile ? playlistFiles.get(pos).getMusicFiles().getId() : playlistFiles.get(pos).getVideoFiles().getId();
            playlistFiles.remove(pos);
            historyDetailsActivityAdapter.notifyItemRemoved(pos);
            MoreFragment.historyAdapter.remove(id);
            db.delete(id);

        }
    };
}