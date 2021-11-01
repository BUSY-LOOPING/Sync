package com.sync.imusic;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.io.File;
import java.text.CharacterIterator;
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
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
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

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void setValues() {
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoFiles.getPath()));
        SimpleExoPlayer simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
        simpleExoPlayer.setMediaItem(mediaItem, true);
        simpleExoPlayer.prepare();


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
        size.setText(humanReadableByteCountBin(Long.parseLong(videoFiles.getSize())));

        resolution.setText("Resolution : " + videoFiles.getResolution());
        simpleExoPlayer.addListener(new Player.Listener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    Format videoFormat = simpleExoPlayer.getVideoFormat();
                    if (videoFormat != null) {
                        frame_rate.setText(String.format("Frame Rate : %.2f Hz" ,videoFormat.frameRate));
                    }
                }
            }
        });

        MediaExtractor mex = new MediaExtractor();
        long bitrateLong = 0;
        try {
            mex.setDataSource(videoFiles.getPath());
            MediaFormat mediaFormat = mex.getTrackFormat(0);
            for (int i =0; i < mex.getTrackCount(); i++) {
                MediaFormat format = mex.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio")) {
                    mediaFormat = format;
                    break;
                }
            }
            int bitRateInt = mediaFormat.getInteger(MediaFormat.KEY_BIT_RATE);
            bitrateLong = bitRateInt / 1000;
            int sampleRateInt = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            no_channels.setText(channelCount + (channelCount > 1 ? " channels" : " channel"));
            sample_rate.setText("Sample rate : " + sampleRateInt + " Hz");
        } catch (Exception e) {
            e.printStackTrace();
        }

        bitrate.setText(String.format("Bitrate : %.2f KB/s" ,(float) bitrateLong));



        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (codecInfo.isEncoder()) {
                String[] types = codecInfo.getSupportedTypes();
                for (String type : types) {
                    if (type.startsWith("audio")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            codec_audio.setText("Codec : " + codecInfo.getCanonicalName());
                        } else
                            codec_audio.setText("Codec : " + codecInfo.getName());
                        break;
                    }
                    if (type.startsWith("video")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            codec_video.setText("Codec : " + codecInfo.getCanonicalName());
                        } else
                            codec_video.setText("Codec : " + codecInfo.getName());
                        break;
                    }
                }
            }

        }
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
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