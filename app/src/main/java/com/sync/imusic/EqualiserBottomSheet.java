package com.sync.imusic;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class EqualiserBottomSheet extends BottomSheetDialogFragment implements SeekBar.OnSeekBarChangeListener {
    private Context context;
    private int audioSessionId;
    private ArrayList<String> spinnerArray;
    private static short preset;
    private short minEQLevel;
    private short maxEQLevel;
    private Equalizer equalizer;
    private Virtualizer virtualizer;
    private int no_bands, no_presets;
    private MediaPlayer mediaPlayer;
    private static boolean isChecked = false;

    private TextView band_value1, band_value2, band_value3, band_value4, band_value5, band_value6, band_value7, band_value8, band_value9, band_value10;
    private SeekBar bar1, bar2, bar3, bar4, bar5, bar6, bar7, bar8, bar9, bar10, preamp;
    //    private VerticalSeekBarWrapper[] seekBarWrapper = new VerticalSeekBarWrapper[13];
    private SwitchCompat switchCompat;
    private Spinner spinner;

    public EqualiserBottomSheet() {

    }

    public void setAudioSessionId(int id) {
        audioSessionId = id;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.equaliser_bottom_sheet, container, false);
        init(view);
        initEqualiser();
        listeners();
        return view;
    }

    private void init(View view) {
        spinnerArray = new ArrayList<>();
        switchCompat = view.findViewById(R.id.switchEqualizer);
        spinner = view.findViewById(R.id.spinner_eq);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.infoActDarkGrey), PorterDuff.Mode.SRC_ATOP);
        preamp = view.findViewById(R.id.preamp_seekbar);
        bar1 = view.findViewById(R.id.bar1);
        bar2 = view.findViewById(R.id.bar2);
        bar3 = view.findViewById(R.id.bar3);
        bar4 = view.findViewById(R.id.bar4);
        bar5 = view.findViewById(R.id.bar5);
        bar6 = view.findViewById(R.id.bar6);
        bar7 = view.findViewById(R.id.bar7);
        bar8 = view.findViewById(R.id.bar8);
        bar9 = view.findViewById(R.id.bar9);
        bar10 = view.findViewById(R.id.bar10);
//        seekBarWrapper[0] = view.findViewById(R.id.wrapper1);
//        seekBarWrapper[1] = view.findViewById(R.id.wrapper2);
//        seekBarWrapper[2] = view.findViewById(R.id.wrapper3);
//        seekBarWrapper[3] = view.findViewById(R.id.wrapper4);
//        seekBarWrapper[4] = view.findViewById(R.id.wrapper5);
//        seekBarWrapper[5] = view.findViewById(R.id.wrapper6);
//        seekBarWrapper[6] = view.findViewById(R.id.wrapper7);
//        seekBarWrapper[7]= view.findViewById(R.id.wrapper8);
//        seekBarWrapper[8] = view.findViewById(R.id.wrapper9);
//        seekBarWrapper[9] = view.findViewById(R.id.wrapper10);
//        seekBarWrapper[10] = view.findViewById(R.id.wrapper11);
//        seekBarWrapper[11] = view.findViewById(R.id.wrapper12);
//        seekBarWrapper[12] = view.findViewById(R.id.wrapper13);

        band_value1 = view.findViewById(R.id.db_txt_1);
        band_value2 = view.findViewById(R.id.db_txt_2);
        band_value3 = view.findViewById(R.id.db_txt_3);
        band_value4 = view.findViewById(R.id.db_txt_4);
        band_value5 = view.findViewById(R.id.db_txt_5);
        band_value6 = view.findViewById(R.id.db_txt_6);
        band_value7 = view.findViewById(R.id.db_txt_7);
        band_value8 = view.findViewById(R.id.db_txt_8);
        band_value9 = view.findViewById(R.id.db_txt_9);
        band_value10 = view.findViewById(R.id.db_txt_10);
        if (isChecked) {
            switchCompat.setChecked(true);
        }
    }

    private void initEqualiser() {
        equalizer = new Equalizer(0, audioSessionId);
        virtualizer = new Virtualizer(0, audioSessionId);
        virtualizer.setEnabled(true);
        no_bands = equalizer.getNumberOfBands();
        no_presets = equalizer.getNumberOfPresets();
        minEQLevel = (short) (equalizer.getBandLevelRange()[0]);
        maxEQLevel = (short) (equalizer.getBandLevelRange()[1]);

        preamp.setMax(1000);
        bar1.setMax(maxEQLevel / 2 - minEQLevel);
        bar2.setMax(maxEQLevel - maxEQLevel / 2);
        bar3.setMax(maxEQLevel / 2 - minEQLevel);
        bar4.setMax(maxEQLevel - maxEQLevel / 2);
        bar5.setMax(maxEQLevel / 2 - minEQLevel);
        bar6.setMax(maxEQLevel - maxEQLevel / 2);
        bar7.setMax(maxEQLevel / 2 - minEQLevel);
        bar8.setMax(maxEQLevel - maxEQLevel / 2);
        bar9.setMax(maxEQLevel / 2 - minEQLevel);
        bar10.setMax(maxEQLevel - maxEQLevel / 2);
        for (short i = 0; i < no_presets; i++) {
            spinnerArray.add(equalizer.getPresetName(i));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        spinner.setAdapter(spinnerArrayAdapter);

        if (isChecked) {
            equalizer.setEnabled(true);
            equalizer.usePreset(preset);
            spinner.setSelection(preset);
            initBars();
        }
    }

    private void initBars() {
        preamp.setProgress(virtualizer.getRoundedStrength());
        bar1.setProgress(equalizer.getBandLevel((short) 0) / 2);
        bar2.setProgress(equalizer.getBandLevel((short) 0) - equalizer.getBandLevel((short) 0) / 2);
        bar3.setProgress(equalizer.getBandLevel((short) 1) / 2);
        bar4.setProgress(equalizer.getBandLevel((short) 1) - equalizer.getBandLevel((short) 1) / 2);
        bar5.setProgress(equalizer.getBandLevel((short) 2) / 2);
        bar6.setProgress(equalizer.getBandLevel((short) 2) - equalizer.getBandLevel((short) 2) / 2);
        bar7.setProgress(equalizer.getBandLevel((short) 3) / 2);
        bar8.setProgress(equalizer.getBandLevel((short) 3) / 2 - equalizer.getBandLevel((short) 3) / 2);
        bar9.setProgress(equalizer.getBandLevel((short) 4) / 2);
        bar10.setProgress(equalizer.getBandLevel((short) 4) - equalizer.getBandLevel((short) 4) / 2);
    }

    private void listeners() {
        //switch listener
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EqualiserBottomSheet.isChecked = isChecked;
                equalizer.setEnabled(isChecked);
                if (isChecked) {
                    equalizer.usePreset(preset);
                }
            }
        });

        //spinner item selected listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preset = (short) position;
                switchCompat.setChecked(true);
                equalizer.usePreset(preset);
                initBars();
//                virtualizer.setEnabled(true);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //bar move listeners
        preamp.setOnSeekBarChangeListener(this);
        bar1.setOnSeekBarChangeListener(this);
        bar2.setOnSeekBarChangeListener(this);
        bar3.setOnSeekBarChangeListener(this);
        bar4.setOnSeekBarChangeListener(this);
        bar5.setOnSeekBarChangeListener(this);
        bar6.setOnSeekBarChangeListener(this);
        bar7.setOnSeekBarChangeListener(this);
        bar8.setOnSeekBarChangeListener(this);
        bar9.setOnSeekBarChangeListener(this);
        bar10.setOnSeekBarChangeListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        try {
            switchCompat.setChecked(true);
            short band = -1;
            short min_eq = minEQLevel;
            switch (seekBar.getId()) {
                case R.id.preamp_seekbar:
                    if (fromUser) {
                        virtualizer.setStrength((short) preamp.getProgress());
                    }
                    break;
                case R.id.bar1:
                    band = 0;
                    min_eq = minEQLevel;
                    break;
                case R.id.bar2:
                    min_eq = (short) (maxEQLevel / 2);
                    band = 0;
                    break;
                case R.id.bar3:
                    band = 1;
                    min_eq = minEQLevel;
                    break;
                case R.id.bar4:
                    min_eq = (short) (maxEQLevel / 2);
                    band = 1;
                    break;
                case R.id.bar5:
                    band = 2;
                    min_eq = minEQLevel;
                    break;
                case R.id.bar6:
                    min_eq = (short) (maxEQLevel / 2);
                    band = 2;
                    break;
                case R.id.bar7:
                    band = 3;
                    min_eq = minEQLevel;
                    break;
                case R.id.bar8:
                    band = 3;
                    min_eq = (short) (maxEQLevel / 2);
                    break;
                case R.id.bar9:
                    band = 4;
                    min_eq = minEQLevel;
                    break;
                case R.id.bar10:
                    band = 4;
                    min_eq = (short) (maxEQLevel / 2);
                    break;
                default:
                    band = -1;
            }
            if (fromUser && band != -1) {
                equalizer.setBandLevel(band, (short) (progress + min_eq));
                virtualizer.setStrength((short) progress);
            }
        } catch (Exception e) {
            Log.d("error", "Failed to change equalizer : ", e);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
