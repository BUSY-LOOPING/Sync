package com.example.imusic.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imusic.DataBaseHelperHistory;
import com.example.imusic.HistoryAdapter;
import com.example.imusic.HistoryDetailsActivity;
import com.example.imusic.MusicFiles;
import com.example.imusic.PlaylistFiles;
import com.example.imusic.R;
import com.example.imusic.SettingsFragment;
import com.example.imusic.StreamActivity;
import com.example.imusic.VideoFiles;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class MoreFragment extends Fragment {
    private Context context;
    private Button settings;
    private FragmentActivity myContext;
    private FloatingActionButton fab;
    private MaterialCardView newStreamBtn;
    private RecyclerView recyclerView;
    public static HistoryAdapter historyAdapter;
    private ImageView arrow;
    private ArrayList<PlaylistFiles> playlistFiles;

    public static final String HISTORY_FILES = "history_files";

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
        if (fab != null) fab.hide();
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        RelativeLayout relativeLayout = container.getRootView().findViewById(R.id.main_container);
        fab = relativeLayout.findViewById(R.id.fab_shuffle);
        settings = view.findViewById(R.id.settings_btn);
        arrow = view.findViewById(R.id.arrowBtn);
        recyclerView = view.findViewById(R.id.recyclerView_fragment_more);
        setRecyclerView();
        newStreamBtn = view.findViewById(R.id.new_stream_btn);
        newStreamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StreamActivity.class);
                startActivity(intent);
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayout.setVisibility(View.INVISIBLE);
                Fragment someFragment = new SettingsFragment();
                FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
//                transaction.setCustomAnimations(android.R.anim.slide_out_right, 0);
                transaction.add(android.R.id.content, someFragment); // give your fragment container id in first parameter
                transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
                transaction.commit();
//                ViewPager viewPager = container.findViewById(R.id.view_pager_main);
//                viewPager.setCurrentItem(5);
//                if (viewPager == null) Toast.makeText(context, "adasdad", Toast.LENGTH_SHORT).show();
            }
        });
        arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HistoryDetailsActivity.class);
                intent.putExtra(HISTORY_FILES, (Serializable) playlistFiles);
                context.startActivity(intent);
            }
        });


        return view;
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
                } else if (res.getString(1).equals("1")){
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