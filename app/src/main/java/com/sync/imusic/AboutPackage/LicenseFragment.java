package com.sync.imusic.AboutPackage;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.sync.imusic.databinding.FragmentLicenseBinding;


public class LicenseFragment extends Fragment {
    private Context context;
    private int refIds[] = new int[3];
    private NestedScrollView scrollView;
    private FragmentLicenseBinding binding;
    public LicenseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLicenseBinding.inflate(inflater, container, false);
        init();
        listeners();
        return binding.getRoot();
    }

    private void init() {
        scrollView = binding.scrollView;
        refIds [0] = binding.backToTop1.getId();
        refIds [1] = binding.backToTop2.getId();
        refIds [2] = binding.backToTop3.getId();
//        backToTop[0] = binding.backToTop1;
//        backToTop[1] = binding.backToTop2;
//        backToTop[2] = binding.backToTop3;
    }

    private void listeners() {
        for (int id : refIds) {
            binding.getRoot().findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scrollView.fullScroll(View.FOCUS_UP);
                }
            });
        }

    }

    @Override
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}