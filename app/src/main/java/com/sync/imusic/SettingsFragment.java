package com.sync.imusic;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.transition.Slide;

public class SettingsFragment extends PreferenceFragmentCompat {
    private Context context;
    private RelativeLayout relativeLayout;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        assert container != null;
        relativeLayout = container.getRootView().findViewById(R.id.main_container);
//        Toolbar toolbar = new Toolbar(context);
//        toolbar.setTitle("Settings");
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250);
//        layoutParams.gravity = Gravity.TOP;
//        toolbar.setLayoutParams(layoutParams);
//        toolbar.setVisibility(View.VISIBLE);
//        toolbar.setBackgroundResource(R.color.color_primary);
//        container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, container.getHeight() - toolbar.getHeight()));
//        container.addView(toolbar);
//        ((AppCompatActivity) context).setSupportActionBar(toolbar);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Toast.makeText(getContext(), "key = " + rootKey, Toast.LENGTH_SHORT).show();
        setPreferencesFromResource(R.xml.prefs, rootKey);
    }

    @Override
    public void onPause() {
        setExitTransition(new Slide(Gravity.START));
        super.onPause();
        relativeLayout.setVisibility(View.VISIBLE);
    }
}