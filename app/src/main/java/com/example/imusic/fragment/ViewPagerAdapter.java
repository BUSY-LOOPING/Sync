package com.example.imusic.fragment;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MusicFragment();
            case 1:
                return new VideosFragment();
            case 2:
                return new BrowseFragment();
            case 3:
                return new PlaylistsFragment();
            case 4:
                return new MoreFragment();
        }
        return new MusicFragment();
    }

    @Override
    public int getCount() {
        return 5;
    }
}
