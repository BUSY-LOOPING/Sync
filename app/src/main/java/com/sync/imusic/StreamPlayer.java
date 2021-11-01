package com.sync.imusic;

import static com.sync.imusic.StreamActivity.list;
import static com.sync.imusic.StreamActivity.streamActivityAdapter;

import android.app.PictureInPictureParams;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class StreamPlayer extends AppCompatActivity {
    private PictureInPictureParams.Builder pictureInPictureParams;
    private YouTubePlayerView youTubePlayerView;
    private String videoId;
    private Boolean mBackstackLost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_player);
        init(getIntent());
    }

    private void init(Intent intent) {
        if (youTubePlayerView != null) {
            youTubePlayerView = null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPictureParams = new PictureInPictureParams.Builder();
        }
        videoId = intent.getStringExtra("url_id");
        if (videoId == null) {
            Toast.makeText(StreamPlayer.this, "Some error occurred. Try with some other link.", Toast.LENGTH_SHORT).show();
            finish();
        }
        youTubePlayerView = findViewById(R.id.youtube_player_view);
//        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0);
                DataBaseStream db = new DataBaseStream(StreamPlayer.this, DataBaseHelperPlaylistNames.STREAM_DB_NAME, null, 1);
                String original_url = intent.getStringExtra("original_url");
                if (!list.contains(original_url)) {
                    db.add(original_url);
                    list.add(original_url);
                    streamActivityAdapter.notifyItemInserted(list.size() - 1);
                }
            }

            @Override
            public void onError(YouTubePlayer youTubePlayer, PlayerConstants.PlayerError error) {
                Toast.makeText(StreamPlayer.this, "Some error occurred. Try with some other url.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!isInPictureInPictureMode()) {
                pictureInPictureMode();
            }
        }
    }

    private void pictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Rational aspectRation = new Rational(youTubePlayerView.getWidth(), youTubePlayerView.getHeight());
            pictureInPictureParams.setAspectRatio(aspectRation).build();
            enterPictureInPictureMode(pictureInPictureParams.build());
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            mBackstackLost = true;
            if (youTubePlayerView != null && youTubePlayerView.getPlayerUiController().getMenu() != null) {
                youTubePlayerView.getPlayerUiController().getMenu().dismiss();
            }
        }
    }

    @Override
    public void finish() {
        if( mBackstackLost ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            }
//            startActivity(
//                    Intent.makeRestartActivityTask(
//                            new ComponentName(this, MainActivity.class)));
            startActivity(new Intent(this, MainActivity.class));
        } else {
            super.finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        init(intent);
        super.onNewIntent(intent);
    }
}