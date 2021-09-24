package com.example.imusic;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.StringCharacterIterator;

public class InfoActivityVideo extends AppCompatActivity {
    public static final String VIDEO_FILE_INFO_ACT = "video_file_infoActivityVideo";
    private VideoFiles videoFiles;
    private Toolbar toolbar;
    private TextView path, size, length, codec_video, codec_audio, resolution, frame_rate, bitrate, no_channels, sample_rate;
    private ImageView img;
    private MediaMetadataRetriever mediaMetadataRetriever;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_video);
        init();
        setValues();
    }

    private void init() {
        videoFiles = (VideoFiles) getIntent().getSerializableExtra(VIDEO_FILE_INFO_ACT);
        if (videoFiles == null) {
            Toast.makeText(this, "Cannot display the info. Something went wrong.", Toast.LENGTH_SHORT).show();
            finish();
        }
        toolbar = findViewById(R.id.toolbar_activity_info_video);
        setSupportActionBar(toolbar);
        path = findViewById(R.id.path_activity_info_video);
        size = findViewById(R.id.size_info_activity_video);
        length = findViewById(R.id.length_info_activity_video);
        codec_video = findViewById(R.id.codec_video);
        codec_audio = findViewById(R.id.codec);
        resolution = findViewById(R.id.resolution_info_activity_video);
        frame_rate = findViewById(R.id.frame_rate_video);
        bitrate = findViewById(R.id.bitrate);
        no_channels = findViewById(R.id.channels_info_activity);
        sample_rate = findViewById(R.id.sample_rate);
        img = findViewById(R.id.album_image_activity_details_video);
    }

    private void setValues() {
        toolbar.setTitle(videoFiles.getTitle());

        Glide.with(this)
                .load(new File(videoFiles.getPath()))
                .error(R.drawable.ic_baseline_video)
                .into(img);

        String path_txt = videoFiles.getPath();
        String rootPath = Environment.getExternalStorageDirectory().getPath();
        path_txt = path_txt.replace(rootPath, "  Internal memory");
        if (path_txt.startsWith("/"))
            path_txt = new StringBuilder(path_txt).deleteCharAt(0).toString();
        path_txt = path_txt.replace("/", "  <  ");
        path.setText(path_txt);

        mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(videoFiles.getPath());
        int duration_milli = Integer.parseInt(videoFiles.getDuration());
        length.setText(formattedLength(duration_milli / 1000));

        Double tempSizeLong = Double.parseDouble((videoFiles.getSize()));
        tempSizeLong /= 1000000;
        DecimalFormat dec = new DecimalFormat("#0.00");
        String tempSize = dec.format(tempSizeLong) + " MB";
        size.setText(humanReadableByteCountBin(Long.parseLong(videoFiles.getSize())));

        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(videoFiles.getPath());
            MediaFormat mediaFormat = mex.getTrackFormat(0);
            int bitRateInt = mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
            long bitrateLong = bitRateInt / 1000;
            int sampleRateInt = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            if (dec.format(bitrateLong).contains(".00"))
                bitrate.setText("Bitrate : " + bitrateLong + " KB/s");
            else
                bitrate.setText("Bitrate : " + dec.format(bitrateLong) + " KB/s");
            no_channels.setText(channelCount + " channels");
            sample_rate.setText("Sample rate : " + sampleRateInt + " Hz");
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String formattedLength(int lengthInSec) {  //mCurrentPosition is in seconds
        String result;
        int sec, min = 0, hours = 0;
        while (lengthInSec >= 60) {
            lengthInSec = lengthInSec - 60;
            min++;
            if (min == 60) {
                hours++;
                min = 0;
            }
        }
        sec = lengthInSec;
        StringBuilder stringBuilder = new StringBuilder();
        if (hours == 0 && min == 0)
            stringBuilder.append(sec).append("sec");
        if (hours == 0 && min != 0)
            stringBuilder.append(min).append("min").append(sec).append("sec");
        if (hours != 0)
            stringBuilder.append(hours).append("hr").append(min).append("min").append(sec).append("sec");

        result = stringBuilder.toString();
        return result;
    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %cB", value / 1024.0, ci.current());
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaMetadataRetriever.close();
        }
        super.onDestroy();
    }
}