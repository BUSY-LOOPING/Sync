package com.example.imusic;

import static com.example.imusic.HistoryDetailsActivity.historyDetailsActivityAdapter;
import static com.example.imusic.PlayerActivity.musicService;
import static com.example.imusic.VideoAdapter.VIDEO_FILES;
import static com.example.imusic.VideoAdapter.VIDEO_FILES_POS;
import static com.example.imusic.fragment.MoreFragment.historyAdapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Rational;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView video_title, total_duration, current_time, brightness_txt, volume_txt, seek_secs, seek_current_time;
    private ImageView back, play_pause_btn, unlock_btn, lock_btn, nextBtn, prevBtn, brightness_icon, volume_icon;
    private ArrayList<VideoFiles> currentVideo;
    private int position;
    private FrameLayout frameLayout;
    private ProgressBar progressBar, brightnessProgressBar, volumeProgressBar;
    private View decorView;
    private SeekBar seekBar;
    private boolean changingSeekBar = false;

    public enum ControlsMode {
        LOCK, FULLCONTROLS
    }

    private PictureInPictureParams.Builder pictureInPictureParams;
    private ControlsMode controlsState = ControlsMode.FULLCONTROLS;
    private AnimatedVectorDrawableCompat avd;
    private AnimatedVectorDrawable avd2;
    private final Handler handler = new Handler();
    private final Runnable hideControls = new Runnable() {
        @Override
        public void run() {
            hideAllControls();
        }
    };
    private final Runnable hideVolumeContainer = new Runnable() {
        @Override
        public void run() {
            hideVolumeBar();
        }
    };
    private final Runnable hideBrightnessBarContainer = new Runnable() {
        @Override
        public void run() {
            hideBrightnessBar();
        }
    };

    private AudioManager audioManager;
    private ContentResolver cResolver;
    private Window window;
    private Display display;
    private Point size;
    private float brightness, seekSpeed = 1;
    private int sWidth, sHeight, mediaVolume;
    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView playerView;
    private LinearLayout root, unlockPanel, controlsLinearLayout, topControls, brightnessBarContainer, volumeBarContainer, cast_container, seekBarCenterTxtContainer;
    private long totalDuration, diffX, diffY;
    float baseX, baseY;
    private boolean isStopped = false, intLeft, intRight, intBottom, intTop, screen_swipe_move, tested_ok, mBackstackLost = false, left_to_right = false;
    private int initialSystemUiVisibility;
    int uiImmersiveOptions = (
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LOW_PROFILE |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    private static final int MAX_CLICK_DURATION = 200, MIN_DISTANCE = 10;
    private int calculatedTime = 0;
    private long startClickTime;

    //Implementing chromecast
    private CastContext castContext;
    private MediaRouteButton mediaRouteButton;
    //    private CastContext castContext;
    private CastSession castSession;
    //    private PlaybackState playbackState;
//    private SessionManager sessionManager;
    private final SessionManagerListener<CastSession> sessionSessionManagerListener = new SessionManagerListenerImpl();

    private class SessionManagerListenerImpl implements SessionManagerListener<CastSession> {
        @Override
        public void onSessionStarting(@NonNull CastSession castSession) {
            Toast.makeText(VideoPlayerActivity.this, "onSessionStarting", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSessionStarted(@NonNull CastSession castSession, @NonNull String s) {
            //when tv and chromecast both are connected
            onApplicationConnected(castSession);
            Toast.makeText(VideoPlayerActivity.this, "onSessionStarted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSessionStartFailed(@NonNull CastSession castSession, int i) {

        }

        @Override
        public void onSessionEnding(@NonNull CastSession castSession) {

        }

        @Override
        public void onSessionEnded(@NonNull CastSession castSession, int i) {

        }

        @Override
        public void onSessionResuming(@NonNull CastSession castSession, @NonNull String s) {

        }

        @Override
        public void onSessionResumed(@NonNull CastSession castSession, boolean b) {
            //re connected or re resumed
            onApplicationConnected(castSession);
        }

        @Override
        public void onSessionResumeFailed(@NonNull CastSession castSession, int i) {

        }

        @Override
        public void onSessionSuspended(@NonNull CastSession castSession, int i) {

        }
    }

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
//                        Intent data = result.getData();
//                        doSomeOperations();
                    }
                }
            });

    public void openActivityForResult() {
        Intent intent = new Intent(this, ExpandedControllerActivity.class);
        activityResultLauncher.launch(intent);
    }

    private void onApplicationConnected(CastSession castSession) {
        this.castSession = castSession;
        loadMedia(0, true);
    }

    private void loadMedia(int position, boolean autoPlay) {
        if (castSession == null) {
            return;
        }

        final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }
        remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                Intent intent = new Intent(VideoPlayerActivity.this, ExpandedControllerActivity.class);
                startActivity(intent);
                remoteMediaClient.removeListener(this);
                if (simpleExoPlayer.isPlaying()) {
                    simpleExoPlayer.pause();
                    anim_PauseToPlay();
                }
            }

            @Override
            public void onMetadataUpdated() {

            }

            @Override
            public void onQueueStatusUpdated() {

            }

            @Override
            public void onPreloadStatusUpdated() {

            }

            @Override
            public void onSendingRemoteMediaRequest() {

            }

            @Override
            public void onAdBreakStatusUpdated() {

            }
        });
        remoteMediaClient.load(buildMediaInfo(remoteMediaClient), autoPlay, position);
    }

    private MediaInfo buildMediaInfo(RemoteMediaClient remoteMediaClient) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, currentVideo.get(position).getTitle());

        return new MediaInfo.Builder(currentVideo.get(position).getPath())
                .setContentUrl(currentVideo.get(position).getPath())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(mediaMetadata)
                .setStreamDuration(totalDuration)
                .build();
    }

    private MediaItem mediaItem;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_activity);
        if (musicService != null && !musicService.is_mediaPlayerNull() && musicService.isPlaying() && musicService.actionPlaying != null) {
            musicService.actionPlaying.playPauseBtnClicked();
        }
        init(getIntent());
        play();
    }

    private void setMediaData() {
        total_duration.setText(milliSecondsToTimer(totalDuration));
//        animateProgression();

        VideoPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!tested_ok) {
                    if (isStopped) handler.removeCallbacks(this);
                    seekBar.setProgress((int) (simpleExoPlayer.getCurrentPosition()));
                    current_time.setText(milliSecondsToTimer(simpleExoPlayer.getCurrentPosition()));
                    handler.post(this);
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    changingSeekBar = true;
                    showAllControls();
                    seekBarCenterTxtContainer.setVisibility(View.VISIBLE);
                    seek_secs.setVisibility(View.GONE);
                    seek_current_time.setText(milliSecondsToTimer(progress));
                    simpleExoPlayer.seekTo(progress);
//                    showAllControls();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                hideViewWithAnim(seekBarCenterTxtContainer, 1000);
                changingSeekBar = false;
            }
        });
    }

    private void animateProgression() {
        final ObjectAnimator animator = ObjectAnimator.ofInt(seekBar, "progress", 0, 100);
        animator.setDuration(totalDuration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
        seekBar.clearAnimation();
    }


    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     *
     * @param currentDuration
     * @param totalDuration
     */
    public int getProgressPercentage(long currentDuration, long totalDuration) {
        double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return (int) percentage;
    }

    /**
     * This function converts percentage to Ms
     */

    public long getProgressMs(int percentage) {
        return (((long) percentage / 100) * totalDuration);
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      -
     * @param totalDuration returns current duration in milliseconds
     */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            if (isInPictureInPictureMode()) {
//            } else
//                simpleExoPlayer.play();
//        }
    }

    private void play() {
        VideoExtensions videoExtensions = new VideoExtensions();
        video_title.setText(videoExtensions.replaceExtension(currentVideo.get(position).getFilename()));
        DataBaseHelperHistory dataBaseHelperHistory = new DataBaseHelperHistory(this, "history.db", null, 1);
        dataBaseHelperHistory.setAdapter(historyAdapter);
        dataBaseHelperHistory.setAdapter(historyDetailsActivityAdapter);
        dataBaseHelperHistory.insertData(false, true, currentVideo.get(position).getPath(),
                currentVideo.get(position).getTitle(),
                "",
                "",
                currentVideo.get(position).getDuration(),
                currentVideo.get(position).getId(),
                currentVideo.get(position).getSize(),
                currentVideo.get(position).getFilename(),
                currentVideo.get(position).getDateAdded(),
                currentVideo.get(position).getResolution(),
                null);
        dataBaseHelperHistory.close();


        mediaItem = MediaItem.fromUri(Uri.parse(currentVideo.get(position).getPath()));
        simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
        simpleExoPlayer.setMediaItem(mediaItem, true);
        simpleExoPlayer.prepare();
        playerView.setPlayer(simpleExoPlayer);

        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        simpleExoPlayer.play();
        if (play_pause_btn.getDrawable() != ContextCompat.getDrawable(this, R.drawable.avd_play_to_pause)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                anim_playToPause();
            }
        }
        simpleExoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    releasePlayer();
                    finish();
                }
                if (playbackState == Player.STATE_BUFFERING) {
                    if (!tested_ok)
                        progressBar.setVisibility(View.VISIBLE);
                }
                if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                    totalDuration = simpleExoPlayer.getDuration();
                    seekBar.setMax((int) totalDuration);
                    setMediaData();
                }
                // STATE_IDLE, STATE_ENDED
                // This prevents the screen from getting dim/lock
                playerView.setKeepScreenOn(playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED &&
                        playWhenReady);
            }
        });
    }

    private void releasePlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.setPlayWhenReady(false);
            simpleExoPlayer.stop();
            simpleExoPlayer.release();
            isStopped = true;
        }
    }


    private void init(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pictureInPictureParams = new PictureInPictureParams.Builder();
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        decorView = getWindow().getDecorView();
        initialSystemUiVisibility = decorView.getSystemUiVisibility();
        handler.postDelayed(hideControls, 3000);

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        sWidth = size.x;
        sHeight = size.y;

        root = findViewById(R.id.root);
        controlsLinearLayout = findViewById(R.id.controls);
        brightnessBarContainer = findViewById(R.id.brightness_slider_container);
        topControls = findViewById(R.id.top_video_player_activity);
        cast_container = findViewById(R.id.chrome_cast_container);
        mediaRouteButton = new MediaRouteButton(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaRouteButton.setBackgroundTintMode(PorterDuff.Mode.MULTIPLY);
            mediaRouteButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.white));
        }
        cast_container.addView(mediaRouteButton);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
        castContext = CastContext.getSharedInstance(this);
        castContext.addCastStateListener(new CastStateListener() {
            @Override
            public void onCastStateChanged(int i) {
                Toast.makeText(VideoPlayerActivity.this, "onCastStateChanged", Toast.LENGTH_SHORT).show();
            }
        });
        castContext.getSessionManager().addSessionManagerListener(sessionSessionManagerListener, CastSession.class);
//        brightnessCenterText = findViewById(R.id.brightness_center_text);
        volumeBarContainer = findViewById(R.id.volume_slider_container);
        seekBarCenterTxtContainer = findViewById(R.id.seekbar_center_text);
//        volumeCenterText = findViewById(R.id.volume_center_text);

        brightness_txt = findViewById(R.id.brightness_percent_txt);
        volume_txt = findViewById(R.id.volume_percent_txt);
        seek_secs = findViewById(R.id.txt_seek_secs);
        seek_current_time = findViewById(R.id.txt_seek_currTime);
        brightnessProgressBar = findViewById(R.id.brightness_slider);
        volumeProgressBar = findViewById(R.id.volume_slider);
        unlockPanel = findViewById(R.id.unlock_panel);
        lock_btn = findViewById(R.id.btn_lock);
        unlock_btn = findViewById(R.id.btn_unlock);

        play_pause_btn = findViewById(R.id.btn_pause_video_player_activity);
        nextBtn = findViewById(R.id.btn_next_video_player_activity);
        prevBtn = findViewById(R.id.btn_prev_video_player_activity);
        brightness_icon = findViewById(R.id.brightness_icon);
        volume_icon = findViewById(R.id.volIcon);

        seekBar = findViewById(R.id.seekbar_exoplayer);
        progressBar = findViewById(R.id.progress_bar);
        playerView = findViewById(R.id.exoPlayerView);
//        playerView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    if (root.getVisibility() == View.GONE) {
//                        showAllControls();
//                        if (!simpleExoPlayer.isPlaying()) {
//                            handler.removeCallbacks(hideControls);
//                        }
//                    } else {
//                        hideAllControls();
//                    }
//                }
//                return false;
//            }
//        });
        frameLayout = findViewById(R.id.frame_layout);
        simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
        root = findViewById(R.id.root);
        back = findViewById(R.id.back_btn_video_player_activity);
        video_title = findViewById(R.id.video_title_video_player_activity);
        total_duration = findViewById(R.id.txt_totalDuration_video_player_activity);
        current_time = findViewById(R.id.txt_currentTime_video_player_activity);
        currentVideo = (ArrayList<VideoFiles>) intent.getSerializableExtra(VIDEO_FILES);
        //***for this to work , video files must extend serializable***
        position = intent.getIntExtra(VIDEO_FILES_POS, -1);
        if (position == -1) {
            Toast.makeText(this, "The video file cannot be played", Toast.LENGTH_SHORT).show();
            finish();
        }

        back.setOnClickListener(this);
//        root.setOnClickListener(this);
        play_pause_btn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        lock_btn.setOnClickListener(this);
        unlock_btn.setOnClickListener(this);
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (simpleExoPlayer != null && simpleExoPlayer.isPlaying()) {
//            simpleExoPlayer.stop();
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
//        releasePlayer();
//        finish();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            if (isInPictureInPictureMode()) {
//                Toast.makeText(this, "pause called when in pip", Toast.LENGTH_SHORT).show();
//            } else {
//                releasePlayer();
//                finish();
//            }
//        } else {
//            releasePlayer();
//            finish();
//        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releasePlayer();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_btn_video_player_activity:
                releasePlayer();
                finish();
                break;
            case R.id.btn_pause_video_player_activity:
                if (simpleExoPlayer.isPlaying()) {
                    simpleExoPlayer.pause();
                    anim_PauseToPlay();
                } else {
                    simpleExoPlayer.play();
                    anim_playToPause();
                }
                break;
            case R.id.btn_next_video_player_activity:
                nextBtnClicked();
                handler.postDelayed(hideControls, 3000);
                break;
            case R.id.btn_prev_video_player_activity:
                prevBtnClicked();
                handler.postDelayed(hideControls, 3000);
                break;
            case R.id.btn_lock:
                controlsState = ControlsMode.LOCK;
                root.setVisibility(View.GONE);
                unlockPanel.setVisibility(View.VISIBLE);
                hideAllControls();
                break;
            case R.id.btn_unlock:
                controlsState = ControlsMode.FULLCONTROLS;
                root.setVisibility(View.VISIBLE);
                unlockPanel.setVisibility(View.GONE);
                decorView.setSystemUiVisibility(initialSystemUiVisibility);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void anim_PauseToPlay() {
        play_pause_btn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avd_pause_to_play));
        Drawable drawable = play_pause_btn.getDrawable();
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();

        } else if (drawable instanceof AnimatedVectorDrawable) {
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void anim_playToPause() {
        play_pause_btn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avd_play_to_pause));
        Drawable drawable = play_pause_btn.getDrawable();
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();

        } else if (drawable instanceof AnimatedVectorDrawable) {
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }
    }

    private void hideAllControls() {
        if (controlsState == ControlsMode.FULLCONTROLS) {
            if (root.getVisibility() == View.VISIBLE) {
                root.setVisibility(View.GONE);
            }
        } else if (controlsState == ControlsMode.LOCK) {
            if (unlockPanel.getVisibility() == View.VISIBLE) {
                unlockPanel.setVisibility(View.GONE);
            }
        }
        decorView.setSystemUiVisibility(uiImmersiveOptions);
    }

    private void showAllControls() {
        if (controlsState == ControlsMode.FULLCONTROLS) {
            if (root.getVisibility() == View.GONE) {
                root.setVisibility(View.VISIBLE);
                decorView.setSystemUiVisibility(initialSystemUiVisibility);
            }
        } else if (controlsState == ControlsMode.LOCK) {
            if (unlockPanel.getVisibility() == View.GONE) {
                unlockPanel.setVisibility(View.VISIBLE);
            }
        }
        handler.removeCallbacks(hideControls);
        handler.postDelayed(hideControls, 3000);
    }

    private void showVolumeBar() {
        volumeBarContainer.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hideVolumeContainer);
        handler.postDelayed(hideVolumeContainer, 900);
    }

    private void hideVolumeBar() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Transition transition = new Fade();
            transition.setDuration(500);
            transition.addTarget(volumeBarContainer);
            TransitionManager.beginDelayedTransition((ViewGroup) volumeBarContainer.getRootView(), transition);
        }
        volumeBarContainer.setVisibility(View.GONE);
    }

    private void showBrightnessBar() {
        brightnessBarContainer.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hideBrightnessBarContainer);
        handler.postDelayed(hideBrightnessBarContainer, 900);
    }

    private void hideBrightnessBar() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Transition transition = new Fade();
            transition.setDuration(500);
            transition.addTarget(brightnessBarContainer);
            TransitionManager.beginDelayedTransition((ViewGroup) brightnessBarContainer.getRootView(), transition);
        }
        brightnessBarContainer.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) (ev.getX());
        int y = (int) (ev.getY());
//
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            startClickTime = System.currentTimeMillis();
            if (x < (sWidth / 2)) {
                intLeft = true;
                intRight = false;
            } else if (x > (sWidth / 2)) {
                intLeft = false;
                intRight = true;
            }
            baseX = ev.getX();
            baseY = ev.getY();
//            diffX = 0;
//            diffY = 0;
        }
        int upperLimit = (sHeight / 4) + 100;
        int lowerLimit = ((sHeight / 4) * 3) - 150;
        if (y < upperLimit) {
            intBottom = false;
            intTop = true;
        } else if (y > lowerLimit) {
            intBottom = true;
            intTop = false;
        } else {
            intBottom = false;
            intTop = false;
        }


        // TOUCH STARTED
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            long clickDuration = System.currentTimeMillis() - startClickTime;
            if (clickDuration > MAX_CLICK_DURATION) {
                screen_swipe_move = true;
                diffX = (long) (Math.ceil(ev.getX() - baseX));
                diffY = (long) (Math.ceil(ev.getY() - baseY));
//                if (Math.abs(diffY) > MIN_DISTANCE) {
//                    tested_ok = true;
//                }
                if (controlsState == ControlsMode.FULLCONTROLS) {
                    double brightnessSpeed = 0.08;
                    if (Math.abs(diffY) > Math.abs(diffX) && !tested_ok && !changingSeekBar) { //checking vertical swipe

                        if (intLeft) {  //checking vertical swipe in left corner
                            cResolver = getContentResolver();
                            window = getWindow();
//                            try {
//                                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//                                brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
//                            } catch (Settings.SettingNotFoundException e) {
//                                e.printStackTrace();
//                            }
                            WindowManager.LayoutParams layoutParams = window.getAttributes();
                            if (brightness == 0)
                                brightness = layoutParams.screenBrightness;
                            float new_brightness = (float) (brightness - (diffY * brightnessSpeed));
                            if (new_brightness > 250) {  //max is 250
                                new_brightness = 250;
                            } else if (new_brightness < 1) {
                                new_brightness = 1;  //min is 1
                            }
                            double brightPerc = Math.ceil((((double) new_brightness / (double) 250) * (double) 100));
//                            brightnessBarContainer.setVisibility(View.VISIBLE);
                            showBrightnessBar();
//                        brightnessCenterText.setVisibility(View.VISIBLE);
                            brightnessProgressBar.setProgress((int) brightPerc);
                            brightness_txt.setText(String.valueOf(brightPerc).replace(".0", "") + "%");
                            if (brightPerc <= 30) {
                                brightness_icon.setImageResource(R.drawable.low_brightness);
                            } else if (brightPerc > 30 && brightPerc <= 80) {
                                brightness_icon.setImageResource(R.drawable.medium_brightness);
                            } else if (brightPerc > 80) {
                                brightness_icon.setImageResource(R.drawable.full_brightness);
                            }
//                            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, new_brightness);
                            layoutParams.screenBrightness = new_brightness / (float) 250;
                            window.setAttributes(layoutParams);
                            brightness = new_brightness;

                        }//brightness if statement
                        else if (intRight) {
                            mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                            double cal = (double) diffY * ((double) maxVol / 5500);
//                        double cal = diffY * (mediaVolume );
//                        int newMediaVolume = mediaVolume - (int) cal;
                            int newMediaVolume = mediaVolume - ((int) (diffY * 0.01));
                            if (newMediaVolume > maxVol) {
                                newMediaVolume = maxVol;
                            } else if (newMediaVolume < 1) {
                                newMediaVolume = 0;
                            }
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newMediaVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                            double volPer = Math.ceil((((double) newMediaVolume / (double) maxVol) * (double) 100));
                            if (volPer < 1) {
                                volume_icon.setImageResource(R.drawable.volume_off);
                            } else if (volPer >= 1) {
                                volume_icon.setImageResource(R.drawable.ic_baseline_volume);
                            }
                            showVolumeBar();
                            volumeProgressBar.setProgress((int) volPer);
                            volume_txt.setText((String.valueOf(volPer)).replace(".0", "") + "%");

                        }
                    } else if (Math.abs(diffX) > Math.abs(diffY)) {
                        int[] arr = new int[2];
                        total_duration.getLocationOnScreen(arr);
                        if (Math.abs(diffX) > (MIN_DISTANCE + 100) && ev.getY() < arr[1]) {
                            tested_ok = true;
                            root.setVisibility(View.VISIBLE);
                            seekBarCenterTxtContainer.setVisibility(View.VISIBLE);
                            seek_secs.setVisibility(View.VISIBLE);
                            topControls.setVisibility(View.GONE);
                            controlsLinearLayout.setVisibility(View.INVISIBLE);
                            seekBar.setVisibility(View.VISIBLE);
                            String toTime = "";
                            int newCalculatedTime = (int) ((diffX) * seekSpeed);
                            if (newCalculatedTime < calculatedTime && newCalculatedTime >= 0)
                                seekSpeed--;
                            else
                                seekSpeed++;
                            calculatedTime = newCalculatedTime;


//                            seekSpeed++;
                            String seekDur = "";
                            long pos = simpleExoPlayer.getCurrentPosition();
                            pos = pos + calculatedTime;
                            if (pos < 0) {
                                pos = 0;
                                calculatedTime = -(int) simpleExoPlayer.getCurrentPosition();
                            }
                            if (pos > totalDuration) {
                                pos = totalDuration;
                                calculatedTime = (int) totalDuration;
                            }
                            if (calculatedTime > 0) {
                                seekDur = String.format("[+%02d:%02d]",
                                        TimeUnit.MILLISECONDS.toMinutes(calculatedTime) -
                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(calculatedTime)),
                                        TimeUnit.MILLISECONDS.toSeconds(calculatedTime) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(calculatedTime)));
                            } else if (calculatedTime < 0) {
                                seekDur = String.format("[-%02d:%02d]",
                                        TimeUnit.MILLISECONDS.toMinutes(calculatedTime) -
                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(calculatedTime)),
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(calculatedTime)) -
                                                TimeUnit.MILLISECONDS.toSeconds(calculatedTime));
                            }
                            seek_secs.setText(seekDur);
                            toTime = milliSecondsToTimer(pos);
                            seek_current_time.setText(toTime);
                            seekBar.setProgress((int) pos);
                        }
                    }
                }
            }
        }

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            long clickDuration = System.currentTimeMillis() - startClickTime;
            if (tested_ok) {
                simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() + calculatedTime);
                showAllControls();
                topControls.setVisibility(View.VISIBLE);
                controlsLinearLayout.setVisibility(View.VISIBLE);
                hideViewWithAnim(seekBarCenterTxtContainer, 1000);
                tested_ok = false;
                seekSpeed = 1;
                calculatedTime = 0;
            }
            if (clickDuration < MAX_CLICK_DURATION) {
                screen_swipe_move = false;
                tested_ok = false;
                brightnessBarContainer.setVisibility(View.GONE);
//                volumeBarContainer.setVisibility(View.GONE);
                hideVolumeBar();
                hideBrightnessBar();
                if (root.getVisibility() == View.GONE) {
                    showAllControls();
                    if (!simpleExoPlayer.isPlaying()) {
                        handler.removeCallbacks(hideControls);
                    }
                } else {
                    int[] arr = new int[2];
                    total_duration.getLocationOnScreen(arr);
                    if (ev.getY() > arr[1]) {
                        if (simpleExoPlayer.isPlaying())
                            handler.removeCallbacks(hideControls);
                        else
                            handler.postDelayed(hideControls, 3000);
                    } else {
                        hideAllControls();
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideViewWithAnim(LinearLayout linearLayout, int dur) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Transition transition = new Fade();
            transition.setDuration(dur);
            transition.addTarget(linearLayout);
            TransitionManager.beginDelayedTransition((ViewGroup) linearLayout.getRootView(), transition);
        }
        linearLayout.setVisibility(View.GONE);
    }

    private void prevBtnClicked() {
        position--;
        if (position < 0) {
            position = currentVideo.size() - 1;
        }
        releasePlayer();
        play();
    }

    public void nextBtnClicked() {
        position++;
        if (position > currentVideo.size() - 1)
            position = 0;
        releasePlayer();
        play();
    }

    private void pictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Rational aspectRation = new Rational(playerView.getWidth(), playerView.getHeight());
            pictureInPictureParams.setAspectRatio(aspectRation).build();
            enterPictureInPictureMode(pictureInPictureParams.build());
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!isInPictureInPictureMode()) {
                pictureInPictureMode();
            }
//            else {
//                releasePlayer();
//                finish();
//            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
//            getActionBar().hide();
            mBackstackLost = true;
            hideAllControls();
            hideBrightnessBar();
            hideVolumeBar();
        } else {
//            getActionBar().show();
//            showAllControls();

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        releasePlayer();
//        moveTaskToBack(false);
        init(intent);
        play();
    }

    @Override
    public void finish() {
        if (mBackstackLost) {
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
}
