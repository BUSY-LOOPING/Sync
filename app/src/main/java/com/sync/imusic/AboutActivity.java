package com.sync.imusic;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sync.imusic.AboutPackage.AboutPagerAdapter;
import com.sync.imusic.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NestedScrollView scrollView = binding.scrollView;
        scrollView.setFillViewport(true);

        ViewPager viewPager = binding.viewPager;
        AboutPagerAdapter adapter = new AboutPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

//        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
//        ViewPager viewPager = binding.viewPager;
//        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        View view = View.inflate(this, R.layout.tab_title_layout, null);
        tabs.setupWithViewPager(viewPager);
//        tabs.getTabAt(0).setCustomView(view);
//        tabs.getTabAt(1).setCustomView(view);
        Toolbar toolbar = binding.toolbar;
        String versionCode = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = String.valueOf(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        toolbar.setTitle(getApplicationName(this) + " v-" + versionCode);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
}