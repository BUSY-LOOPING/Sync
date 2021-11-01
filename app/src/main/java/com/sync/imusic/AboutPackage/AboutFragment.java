package com.sync.imusic.AboutPackage;

import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sync.imusic.databinding.FragmentAbout2Binding;


public class AboutFragment extends Fragment {
    private FragmentAbout2Binding binding;
    private Context context;
    private TextView link1, link2;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAbout2Binding.inflate(inflater, container, false);
        init();
        listeners();
        return binding.getRoot();
    }

    private void init() {
        link1 = binding.link1;
        link1.setMovementMethod(LinkMovementMethod.getInstance());
        link2 = binding.link2;
    }

    private void listeners() {

    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}