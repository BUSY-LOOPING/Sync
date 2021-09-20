package com.example.imusic;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.example.imusic.MainActivity.repeatBoolean;
import static com.example.imusic.MusicService.MUSIC_ARTIST;
import static com.example.imusic.MusicService.MUSIC_CURRENT_DURATION;
import static com.example.imusic.MusicService.MUSIC_NOW_PLAYING;
import static com.example.imusic.MusicService.MUSIC_PATH;
import static com.example.imusic.MusicService.MUSIC_TITLE;
import static com.example.imusic.MusicService.MUSIC_TOTAL_DURATION;
import static com.example.imusic.PlayerActivity.listSongs;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

public class MiniPlayer implements ServiceConnection {

    public static boolean flag = false;
    public static int peekHeight = 0;
    public final Handler handler = new Handler();
    public ToggleButton tb_img;
    public String playingFrom = "";
    public ContentLoadingProgressBar bar;
    public TextView time, song_name, artist;
    public RecyclerView recyclerView;
    public boolean reachedTop = false;
    public ImageView miniPlayPause, album_art, bg, bottomPlayPause, nextBtn, prevBtn, repeatBtn, centerAlbumArt, searchBtn, moreBtn;
    public SeekBar seekBar;
    public boolean isShown = false;
    private BottomSheetBehavior bottomSheetBehavior;
    private Context context;
    private View view;
    private BottomNavigationView bottomNavigationView;
    private ScrollView scrollView;
    private LinearLayoutCompat linearLayoutCompat;
    private MusicService musicService;
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (bar != null && musicService != null && !musicService.is_mediaPlayerNull()) {
                if (!MiniPlayer.flag) {
                    byte[] array = getAlbumArt(musicService.nowPlaying().getPath());
                    if (array != null) {
                        album_art.setVisibility(View.VISIBLE);
                        Bitmap bmp = BitmapFactory.decodeByteArray(array, 0, array.length);
                        album_art.setImageBitmap(bmp);
                        bg.setImageBitmap(bmp);
                        centerAlbumArt.setImageBitmap(bmp);
                    } else {
                        album_art.setVisibility(View.GONE);
                        bg.setImageResource(R.color.black);
                        centerAlbumArt.setImageResource(R.drawable.ic_music_note_full_freeicons);
                    }
                    int max = musicService.getDuration() / 1000;
                    bar.setMax(max);
                    seekBar.setMax(max);
                    if (!song_name.getText().toString().equals(musicService.nowPlaying().getTitle()) || song_name.getText().equals("")) {
                        song_name.setText(musicService.nowPlaying().getTitle());
                    }
                    if (musicService.nowPlaying().getArtist().equals("<unknown>")) {
                        artist.setVisibility(View.GONE);
                    } else {
                        artist.setVisibility(View.VISIBLE);
                        if (!artist.getText().equals(musicService.nowPlaying().getArtist()))
                            artist.setText(musicService.nowPlaying().getArtist());
                    }
                    MiniPlayer.flag = true;
                }
                int mCurrentPosition = musicService.getCurrentPosition();
                bar.setProgress(mCurrentPosition / 1000);
                seekBar.setProgress(mCurrentPosition / 1000);
                time.setText(milliSecondsToTimer(mCurrentPosition));
                handler.post(this);
            }
        }
    };
    private LinearLayoutManager llm;
    private MiniPlayerRecyclerViewAdapter adapter;
    private AnimatedVectorDrawableCompat avd;
    private AnimatedVectorDrawable avd2;


    public MiniPlayer() {

    }

    public MiniPlayer(Context context, View view) {
        this.context = context;
        this.view = view;
        bindService();
        init();
        hidePlayer();
        setRecyclerView();
//        showPlayer();
    }

    public static void setFlag(boolean flag) {
        MiniPlayer.flag = flag;
    }

    private void setRecyclerView() {
//        if (listSongs == null || listSongs.size() == 0)
//            adapter = new MiniPlayerRecyclerViewAdapter(context, musicFiles, recyclerView);
//        else
        adapter = new MiniPlayerRecyclerViewAdapter(context, listSongs, recyclerView, bg);
        adapter.setHasStableIds(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(15);
        llm = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
                return true;
            }

            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                reachedTop = llm.findFirstCompletelyVisibleItemPosition() == 0;
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED)
                    bottomNavigationView.setVisibility(View.GONE);
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void init() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MUSIC_NOW_PLAYING, Context.MODE_PRIVATE);
        RelativeLayout relativeLayout = view.findViewById(R.id.mini_player_container);
        bottomSheetBehavior = BottomSheetBehavior.from(relativeLayout);
        peekHeight = bottomSheetBehavior.getPeekHeight();
        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        recyclerView = view.findViewById(R.id.miniPlayer_recyclerView);
        seekBar = view.findViewById(R.id.seekbar_miniPlayer);
        bar = view.findViewById(R.id.contentLoadingProgressBar);
        int max = sharedPreferences.getInt(MUSIC_TOTAL_DURATION, 100);
        int progress = sharedPreferences.getInt(MUSIC_CURRENT_DURATION, 0);
        bar.setMax(max);
        seekBar.setMax(max);
        bar.setProgress(progress);
        seekBar.setProgress(progress);
        time = view.findViewById(R.id.time_mini_player);
        time.setText(milliSecondsToTimer(sharedPreferences.getInt(MUSIC_CURRENT_DURATION, 0)));
        scrollView = view.findViewById(R.id.scrollViewMiniPlayer);
        linearLayoutCompat = view.findViewById(R.id.finalView);


        song_name = view.findViewById(R.id.song_name_mini_player);
        song_name.setSelected(true);
        song_name.setText(sharedPreferences.getString(MUSIC_TITLE, ""));
        artist = view.findViewById(R.id.artist_mini_player);
        if (sharedPreferences.getString(MUSIC_ARTIST, "").equals("<unknown>")) {
            artist.setVisibility(View.GONE);
        } else {
            artist.setVisibility(View.VISIBLE);
            artist.setText(sharedPreferences.getString(MUSIC_ARTIST, ""));
        }
        album_art = relativeLayout.findViewById(R.id.img_mini_player);
        centerAlbumArt = view.findViewById(R.id.albumArt_center);
        bg = view.findViewById(R.id.bg_mini_player_art);
        byte[] array = getAlbumArt(sharedPreferences.getString(MUSIC_PATH, ""));
        if (array != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
            album_art.setVisibility(View.VISIBLE);
            album_art.setImageBitmap(bitmap);
            bg.setImageBitmap(bitmap);
            centerAlbumArt.setImageBitmap(bitmap);
        } else {
            album_art.setVisibility(View.GONE);
            bg.setImageResource(R.color.black);
            centerAlbumArt.setImageResource(R.drawable.ic_music_note_full_freeicons);
        }

        miniPlayPause = relativeLayout.findViewById(R.id.play_pause_mini_player);
        bottomPlayPause = view.findViewById(R.id.play_pause_mini_player_bottom);
        nextBtn = view.findViewById(R.id.nxtBtnMiniPlayer);
        prevBtn = view.findViewById(R.id.prevBtnMiniPlayer);
        repeatBtn = view.findViewById(R.id.repeatBtnMiniPlayer);
        if (repeatBoolean) {
            repeatBtn.setColorFilter(ContextCompat.getColor(context, R.color.tab_highlight), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        searchBtn = view.findViewById(R.id.search_miniPlayer);
        moreBtn = view.findViewById(R.id.more_mini_player);
        time = relativeLayout.findViewById(R.id.time_mini_player);
//        imageViewOnClick();
//        TypedValue tv = new TypedValue();
//        if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
//            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
//        }
        tb_img = relativeLayout.findViewById(R.id.toggle_img);


        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    scrollView.setClickable(true);
                }
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    scrollView.setClickable(false);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (bottomNavigationView != null) {
                    int navigationHeight = bottomNavigationView.getHeight();
                    float slideY = navigationHeight - navigationHeight * (1 - slideOffset);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    bottomNavigationView.setTranslationY(slideY);
                }
                int navigationHeight = linearLayoutCompat.getHeight();
                float slideY = navigationHeight - navigationHeight * (1 - slideOffset);
                if (slideOffset < 0.5) {
                    scrollView.setAlpha(1 - slideOffset);
                } else {
                    scrollView.setAlpha(slideOffset);
                }
                scrollView.scrollTo(0, (int) slideY);
            }
        });
    }


    public BottomSheetBehavior returnInstance() {
        return bottomSheetBehavior;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void anim_PauseToPlay() {
        miniPlayPause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_pause_to_play));
        bottomPlayPause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_pause_to_play));
        Drawable drawable = miniPlayPause.getDrawable();
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();

        } else if (drawable instanceof AnimatedVectorDrawable) {
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }
        drawable = bottomPlayPause.getDrawable();
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
        miniPlayPause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_play_to_pause));
        bottomPlayPause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_play_to_pause));
        Drawable drawable = miniPlayPause.getDrawable();
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();

        } else if (drawable instanceof AnimatedVectorDrawable) {
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }
        drawable = bottomPlayPause.getDrawable();
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();

        } else if (drawable instanceof AnimatedVectorDrawable) {
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
        musicService.miniPlayerCallback(MiniPlayer.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    public void hidePlayer() {
        isShown = false;
        ViewPager viewPager = view.findViewById(R.id.view_pager_main);
        viewPager.setPadding(0, 0, 0, 0);
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setPeekHeight(0);
            bottomSheetBehavior.setDraggable(false);
//            bottomSheetBehavior.setHideable(true);
//            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    public void bindService() {
        Intent intent = new Intent(context, MusicService.class);
        context.bindService(intent, MiniPlayer.this, BIND_AUTO_CREATE);

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    flag = false;
                    showPlayer();
                    handler.removeCallbacks(this);
                }
            }
        });
    }

    public void unbindService() {
        context.unbindService(this);
    }

    public void showPlayer() {
        isShown = true;
        ViewPager viewPager = view.findViewById(R.id.view_pager_main);
        viewPager.setPadding(0, 0, 0, dpToPx(64));
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setDraggable(true);
            bottomSheetBehavior.setPeekHeight(peekHeight);
            ((Activity) context).runOnUiThread(runnable);
        }
    }

    private byte[] getAlbumArt(String uri) {
        byte[] art = null;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri);
            art = retriever.getEmbeddedPicture();
            retriever.release();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return art;
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
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
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      -
     * @param totalDuration returns current duration in milliseconds
     */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public void destroy() {
        context.unbindService(this);
        removeCallbacks();
    }

    public void removeCallbacks() {
        handler.removeCallbacks(runnable);
    }

    public void addCallbacks() {
        handler.postDelayed(runnable, 500);
    }

    public MusicService getMusicService() {
        return musicService;
    }

    public void updateList(ArrayList<MusicFiles> musicFiles) {
        if (adapter != null) {
            adapter.updateList(musicFiles);
        }
    }

    public void updateNowPlaying(MusicFiles musicFiles) {
        if (adapter != null) {
            adapter.updateNowPlaying(musicFiles);
        }
    }

    public int getAdapterSize() {
        if (adapter != null)
            return adapter.getSize();
        return -1;
    }
}

