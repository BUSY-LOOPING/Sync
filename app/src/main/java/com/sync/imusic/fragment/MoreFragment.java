package com.sync.imusic.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sync.imusic.AboutActivity;
import com.sync.imusic.DataBaseHelperHistory;
import com.sync.imusic.HistoryAdapter;
import com.sync.imusic.HistoryDetailsActivity;
import com.sync.imusic.MainActivity;
import com.sync.imusic.MusicFiles;
import com.sync.imusic.PlaylistFiles;
import com.sync.imusic.R;
import com.sync.imusic.SettingsActivity;
import com.sync.imusic.StreamActivity;
import com.sync.imusic.VideoFiles;

import java.util.ArrayList;
import java.util.List;


public class MoreFragment extends Fragment {
    public static final String HISTORY_FILES = "history_files";
    public static HistoryAdapter historyAdapter;
    private Context context;
    private Button settings, about;
    private FragmentActivity myContext;
    private MaterialCardView newStreamBtn;
    private RecyclerView recyclerView;
    private ImageView arrow;
    private ArrayList<PlaylistFiles> playlistFiles;
    private FloatingActionButton fab;

    public MoreFragment() {

    }

    public static MoreFragment newInstance() {

        Bundle args = new Bundle();

        MoreFragment fragment = new MoreFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (fab != null) {
//            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
//            layoutParams.setAnchorId(View.NO_ID);
//            fab.setLayoutParams(layoutParams);
//            fab.hide(
//                    new FloatingActionButton.OnVisibilityChangedListener() {
//                        @Override
//                        public void onHidden(FloatingActionButton fab) {
//                            super.onHidden(fab);
//                            fab.setVisibility(View.INVISIBLE);
//                        }
//
//                    });
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            fab = mainActivity.findViewById(R.id.fab_shuffle);
        }

        init(view);
        setRecyclerView();
        newStreamBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, StreamActivity.class);
            startActivity(intent);
        });
        settings.setOnClickListener(v -> {
            startActivity(new Intent(context, SettingsActivity.class));
        });
        about.setOnClickListener(v -> {
            startActivity(new Intent(context, AboutActivity.class));
        });
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HistoryDetailsActivity.class);
                intent.putExtra(HISTORY_FILES, playlistFiles);
                context.startActivity(intent);
            }
        });
        return view;
    }

    private void init(View view) {
        settings = view.findViewById(R.id.settings_btn);
        about = view.findViewById(R.id.about_btn);
        arrow = view.findViewById(R.id.arrowBtn);
        recyclerView = view.findViewById(R.id.recyclerView_fragment_more);
        newStreamBtn = view.findViewById(R.id.new_stream_btn);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        recyclerView.smoothScrollToPosition(playlistFiles.size());
        return true;
    }

    private void setRecyclerView() {
        playlistFiles = getPlaylistFilesFromDB();
        historyAdapter = new HistoryAdapter(context, playlistFiles, recyclerView);
        recyclerView.setHasFixedSize(true);
//        historyAdapter.setHasStableIds(true);
        LinearLayoutManager llm = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true);
        llm.setStackFromEnd(true);
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
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(historyAdapter);
    }

    private ArrayList<PlaylistFiles> getPlaylistFilesFromDB() {
        ArrayList<PlaylistFiles> temp = new ArrayList<>();
        DataBaseHelperHistory db = new DataBaseHelperHistory(context, "history.db", null, 1);
        Cursor res = db.getAllData();
        if (res != null) {
            while (res.moveToNext()) {
                if (res.getString(0).equals("1")) {
                    temp.add(new PlaylistFiles(new MusicFiles(
                            res.getString(2),
                            res.getString(3),
                            res.getString(4),
                            res.getString(5),
                            res.getString(6),
                            res.getString(7),
                            res.getString(8)
                    ), ""));
                } else if (res.getString(1).equals("1")) {
                    temp.add(new PlaylistFiles(new VideoFiles(
                            res.getString(7),
                            res.getString(2),
                            res.getString(3),
                            res.getString(9),
                            res.getString(8),
                            res.getString(10),
                            res.getString(6),
                            res.getString(11)
                    ), ""));

                }
            }
            res.close();
        }
        return temp;
    }

}