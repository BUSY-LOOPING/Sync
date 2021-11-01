package com.sync.imusic;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class InfoActivity extends AppCompatActivity {
    Toolbar mtoolBar;
    MusicFiles musicFiles;
    ImageView albumPic;
    int pos = -1;
    TextView path, size, length, codec, bitrate, channels, sampleRate;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialisations
        setContentView(R.layout.activity_info);
        mtoolBar = findViewById(R.id.toolbar_activity_info);
        albumPic = findViewById(R.id.album_image_activity_details);
        pos = getIntent().getIntExtra("posPlayerAct", -1);
        ArrayList<MusicFiles> tempArrayList = (ArrayList<MusicFiles>) getIntent().getSerializableExtra("musicFilePlayerAct");
        musicFiles = tempArrayList.get(pos);
        path = findViewById(R.id.path_activity_info);
        size = findViewById(R.id.size_info_activity);
        length = findViewById(R.id.length_info_activity);
        codec = findViewById(R.id.codec);
        bitrate = findViewById(R.id.bitrate);
        channels = findViewById(R.id.channels_info_activity);
        sampleRate = findViewById(R.id.sample_rate);
        setSupportActionBar(mtoolBar);

        // back button click listener
        mtoolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (musicFiles != null) {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(musicFiles.getPath());
            int milliSec = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaMetadataRetriever.close();
            }
            length.setText(formattedLength(milliSec / 1000));

            long tempSizeLong = Long.parseLong((musicFiles.getSize()));
            DecimalFormat dec = new DecimalFormat("#0.00");
            size.setText(InfoActivityVideo.humanReadableByteCountBin(tempSizeLong));
            MediaExtractor mex = new MediaExtractor();
            try {
                mex.setDataSource(musicFiles.getPath());
                MediaFormat mediaFormat = mex.getTrackFormat(0);
                int bitRateInt = mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
                long bitrateLong = bitRateInt / 1000;
                int sampleRateInt = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                if (dec.format(bitrateLong).contains(".00"))
                    bitrate.setText("Bitrate : " + bitrateLong + " KB/s");
                else
                    bitrate.setText("Bitrate : " + dec.format(bitrateLong) + " KB/s");
                channels.setText(channelCount + " channels");
                sampleRate.setText("Sample rate : " + sampleRateInt + " Hz");
            } catch (Exception e) {
                e.printStackTrace();
            }


            int numCodecs = MediaCodecList.getCodecCount();
            for (int i =0; i <numCodecs; i++)
            {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
                if (codecInfo.isEncoder())
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        codec.setText("Codec : " + codecInfo.getCanonicalName());
                    } else
                        codec.setText("Codec : " + codecInfo.getName());
                    break;
                }
            }


            mtoolBar.setTitle(musicFiles.getTitle());

            String tempPath = musicFiles.getPath();
            if (tempPath.startsWith("/storage/emulated/0/"))
                tempPath = tempPath.replace("/storage/emulated/0/", "    Internal Storage  <  ");
            tempPath = tempPath.replace("/", "  <  ");
            tempPath += "    ";
            path.setText(tempPath);
            byte[] image = getAlbumArt(musicFiles.getPath());
            if (image != null) {
                Glide.with(this)
                        .load(image)
                        .into(albumPic);
            } else {
                Glide.with(this)
                        .load(R.drawable.music_icon)
                        .into(albumPic);
            }
        }

    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
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
}