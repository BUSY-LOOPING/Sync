package com.example.imusic;

import static com.example.imusic.HistoryDetailsActivity.historyDetailsActivityAdapter;
import static com.example.imusic.PlayerActivity.musicService;
import static com.example.imusic.VideoAdapter.VIDEO_FILES;
import static com.example.imusic.VideoAdapter.VIDEO_FILES_POS;
import static com.example.imusic.fragment.MoreFragment.historyAdapter;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.ArrayList;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView video_title, total_duration, current_time, brightness_txt, volume_txt;
    private ImageView back, play_pause_btn, unlock_btn, lock_btn, nextBtn, prevBtn, brightness_icon, volume_icon;
    private ArrayList<VideoFiles> currentVideo;
    private int position;
    private FrameLayout frameLayout;
    private ProgressBar progressBar, brightnessProgressBar, volumeProgressBar;
    private View decorView;
    private SeekBar seekBar;

    public enum ControlsMode {
        LOCK, FULLCONTROLS
    }

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

    private AudioManager audioManager;
    private ContentResolver cResolver;
    private Window window;
    private Display display;
    private Point size;
    private int sWidth, sHeight, brightness, mediaVolume;
    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView playerView;
    private LinearLayout root, unlockPanel, controlsLinearLayout, brightnessBarContainer, brightnessCenterText, volumeBarContainer, volumeCenterText;
    private long totalDuration, diffX, diffY;
    float baseX, baseY;
    private boolean isStopped = false, intLeft, intRight, intBottom, intTop, screen_swipe_move, tested_ok;
    private int initialSystemUiVisibility;
    int uiImmersiveOptions = (
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LOW_PROFILE |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime;

    //Implementing chromecast
//    private MediaRouteButton mediaRouteButton;
//    private CastContext castContext;
//    private CastSession castSession;
//    private PlaybackState playbackState;
//    private SessionManager sessionManager;
    MediaItem mediaItem;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_activity);
        if (musicService != null && !musicService.is_mediaPlayerNull() && musicService.isPlaying() && musicService.actionPlaying != null) {
            musicService.actionPlaying.playPauseBtnClicked();
        }
        init();
        play();
    }

    private void setMediaData() {
        total_duration.setText(milliSecondsToTimer(totalDuration));
        seekBar.setMax(100);
//        animateProgression();

        VideoPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isStopped) handler.removeCallbacks(this);
                seekBar.setProgress((int) ((simpleExoPlayer.getCurrentPosition() * 100) / simpleExoPlayer.getDuration()));
                current_time.setText(milliSecondsToTimer(simpleExoPlayer.getCurrentPosition()));
                handler.postDelayed(this, 1000);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    simpleExoPlayer.seekTo((progress * 100) / totalDuration);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
        simpleExoPlayer.play();
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
                    progressBar.setVisibility(View.VISIBLE);
                }
                if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                    totalDuration = simpleExoPlayer.getDuration();
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


    private void init() {
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
        brightnessCenterText = findViewById(R.id.brightness_center_text);
        volumeBarContainer = findViewById(R.id.volume_slider_container);
        volumeCenterText = findViewById(R.id.volume_center_text);

        brightness_txt = findViewById(R.id.brightness_percent_txt);
        volume_txt = findViewById(R.id.volume_percent_txt);
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
        Intent intent = getIntent();
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
    protected void onDestroy() {
        releasePlayer();
        finish();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
        finish();
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @SuppressLint("SetTextI18n")
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
            diffX = 0;
            diffY = 0;
        }
//            int upperLimit = (sHeight / 4) + 100;
//            int lowerLimit = ((sHeight / 4) * 3) - 150;
//            if (y < upperLimit) {
//                intBottom = false;
//                intTop = true;
//            } else if (y > lowerLimit) {
//                intBottom = true;
//                intTop = false;
//            } else {
//                intBottom = false;
//                intTop = false;
//            }


        // TOUCH STARTED
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            long clickDuration = System.currentTimeMillis() - startClickTime;
            if (clickDuration > MAX_CLICK_DURATION) {
                screen_swipe_move = true;
                if (controlsState == ControlsMode.FULLCONTROLS) {
//                root.setVisibility(View.GONE);
                    hideAllControls();
                    diffX = (long) (Math.ceil(ev.getX() - baseX));
                    diffY = (long) (Math.ceil(ev.getY() - baseY));
                    double brightnessSpeed = 0.08;
//                if (Math.abs(diffY) > MIN_DISTANCE) {
//                    tested_ok = true;
//                }
                    if (Math.abs(diffY) > Math.abs(diffX)) { //checking vertical swipe

                        if (intLeft) {  //checking vertical swipe in left corner
                            cResolver = getContentResolver();
                            window = getWindow();
                            try {
                                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                                brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
                            } catch (Settings.SettingNotFoundException e) {
                                e.printStackTrace();
                            }
                            int new_brightness = (int) (brightness - (diffY * brightnessSpeed));
                            if (new_brightness > 250) {  //max is 250
                                new_brightness = 250;
                            } else if (new_brightness < 1) {
                                new_brightness = 1;  //min is 1
                            }
                            double brightPerc = Math.ceil((((double) new_brightness / (double) 250) * (double) 100));
                            brightnessBarContainer.setVisibility(View.VISIBLE);
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
                            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, new_brightness);
//                        WindowManager.LayoutParams layoutParams = window.getAttributes();
//                        layoutParams.screenBrightness = brightness / (float) 225;
//                        window.setAttributes(layoutParams);

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
                            volumeBarContainer.setVisibility(View.VISIBLE);
                            volumeProgressBar.setProgress((int) volPer);
                            volume_txt.setText((String.valueOf(volPer)).replace(".0", "") + "%");

                        }
                    } else {

                    }

                }
            }
        }

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            long clickDuration = System.currentTimeMillis() - startClickTime;
            if (clickDuration < MAX_CLICK_DURATION) {
                screen_swipe_move = false;
                tested_ok = false;
                brightnessBarContainer.setVisibility(View.GONE);
                volumeBarContainer.setVisibility(View.GONE);
                if (root.getVisibility() == View.GONE) {
                    showAllControls();
                    if (!simpleExoPlayer.isPlaying()) {
                        handler.removeCallbacks(hideControls);
                    }
                } else {
                    int[] arr = new int[2];
                    seekBar.getLocationOnScreen(arr);
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
}
