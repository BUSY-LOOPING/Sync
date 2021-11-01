package com.sync.imusic.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.sync.imusic.MainActivity;
import com.sync.imusic.MusicPackage.MusicViewPagerAdapter;
import com.sync.imusic.R;

public class MusicFragment extends Fragment {

    public MusicFragment() {
    }

    public static MusicFragment newInstance() {

        Bundle args = new Bundle();

        MusicFragment fragment = new MusicFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager viewPager = view.findViewById(R.id.viewPager);

        MusicViewPagerAdapter adapter = new MusicViewPagerAdapter(getChildFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onResume() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            FloatingActionButton fab = mainActivity.findViewById(R.id.fab_shuffle);
            if (fab != null) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                params.setAnchorId(R.id.mini_player_container);
                params.gravity = Gravity.END;
                fab.setLayoutParams(params);
                fab.setImageResource(R.drawable.ic_shuffle_off);
                fab.show();
                fab.setOnClickListener(null);
            }
        }
        super.onResume();
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
}