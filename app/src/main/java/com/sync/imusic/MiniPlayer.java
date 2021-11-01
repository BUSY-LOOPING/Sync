package com.sync.imusic;

import static android.content.Context.BIND_AUTO_CREATE;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MiniPlayer implements ServiceConnection {

    public static boolean flag = false, dragging = false;
    public static int peekHeight = 0;
    private int originalDragHolderPos = -1;
    public final Handler handler = new Handler();
    public ToggleButton tb_img;
    public String playingFrom = "";
    public ContentLoadingProgressBar bar;
    private RelativeLayout peekRelLayout;
    public TextView time, song_name, artist, bottomTxt;
    public RecyclerView recyclerView;
    public boolean reachedTop = false;
    public ImageView miniPlayPause, album_art, bg, bottomPlayPause, nextBtn, prevBtn, repeatBtn, centerAlbumArt, searchBtn, moreBtn;
    public SeekBar seekBar;
    private TextInputLayout textInputLayout;
    public EditText searchViewEditText;
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
//                    setBottomTxt();
                    byte[] array = getAlbumArt(musicService.nowPlaying().getPath());
                    if (array != null) {
                        album_art.setVisibility(View.VISIBLE);
                        Bitmap bmp = BitmapFactory.decodeByteArray(array, 0, array.length);
                        album_art.setImageBitmap(bmp);
                        album_art.setContentDescription("hideable");
                        bg.setImageBitmap(bmp);
                        centerAlbumArt.setImageBitmap(bmp);
                    } else {
                        album_art.setVisibility(View.GONE);
                        album_art.setContentDescription("");
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
                String readableTime = milliSecondsToTimer(mCurrentPosition);
                time.setText(readableTime);
                setBottomTxt(txtTime(mCurrentPosition));
                handler.post(this);
            }
        }
    };

    private String txtTime(int milliseconds) {
        String finalTimerString = "", secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0) {
            finalTimerString = hours + "h";
            return finalTimerString;
        }

        if (minutes > 0) {
            finalTimerString = minutes + "min";
            return finalTimerString;
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = secondsString + "s";

        // return timer string
        return finalTimerString;
    }

    private void setBottomTxt(String readableTime) {
        StringBuilder txt = new StringBuilder();
        txt.append("Track: ");
        txt.append(PlayerActivity.position + 1);
        txt.append("/");
        txt.append(PlayerActivity.listSongs.size());
        txt.append(" · Progress: ");
        txt.append(readableTime);
        txt.append("/");


        int totalDuration = musicService.getDuration();
        String totalDurationString = "";
        int dur = totalDuration / (1000 * 60 * 60);
        totalDurationString = dur + "h";
        if (dur == 0) {
            dur = (totalDuration % (1000 * 60 * 60) / (1000 * 60));
            totalDurationString = dur + "min";
        }
        if (dur == 0) {
            dur = ((totalDuration % (1000 * 60 * 60)) % (1000 * 60) / 1000);
            totalDurationString = dur + "s";
        }
        txt.append(totalDurationString);
        bottomTxt.setText(txt.toString());
    }

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

    ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
            ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {

        @Override
        public void onSelectedChanged(@Nullable @org.jetbrains.annotations.Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                dragging = true;
                originalDragHolderPos = viewHolder != null ? viewHolder.getAdapterPosition() : -1;

            } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                dragging = false;
                originalDragHolderPos = -1;
            }
            Log.d("mylog", "originalDragHolderPos = "+ originalDragHolderPos);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int startPos = viewHolder.getAdapterPosition();
            int endPos = target.getAdapterPosition();
//            MusicFiles musicFileAtStart = listSongs.get(startPos);
//            listSongs.remove(startPos);
//            listSongs.add(startPos, listSongs.get(endPos - 1));
//            listSongs.remove(endPos - 1);
//            listSongs.add(endPos - 1, musicFileAtStart);
            Collections.swap(PlayerActivity.listSongs, startPos, endPos);
            adapter.notifyItemMoved(startPos, endPos);
//            int index = listSongs.indexOf(musicService.nowPlaying());
//            if (endPos > index) {
//                if (musicService.nowPlaying().getId().equals(adapter.getNowPlaying().getId())) {
//                    PlayerActivity.position = endPos;
//                    MiniPlayerRecyclerViewAdapter.prevPos = endPos;
//                }
//            }
//            else {
//                PlayerActivity.position = index;
//            }
            if (!dragging && endPos == PlayerActivity.position && originalDragHolderPos != PlayerActivity.position) {
                dragging = true;
            }

            if (originalDragHolderPos != -1 && originalDragHolderPos == PlayerActivity.position && dragging) {
                PlayerActivity.position = MiniPlayerRecyclerViewAdapter.prevPos = originalDragHolderPos = endPos;
            } else {
                if (originalDragHolderPos != -1 && originalDragHolderPos != PlayerActivity.position && endPos == PlayerActivity.position && dragging) {
                    PlayerActivity.position = MiniPlayerRecyclerViewAdapter.prevPos = startPos;
                    dragging = false;
                }

            }
//            else {
//                if (startPos == PlayerActivity.position + 1) {
//                    PlayerActivity.position++;
//                    MiniPlayerRecyclerViewAdapter.prevPos++;
//                } else if (startPos == PlayerActivity.position - 1){
//                    PlayerActivity.position--;
//                    MiniPlayerRecyclerViewAdapter.prevPos--;
//                }
//            }
//            if (musicService.nowPlaying().getId().equals(adapter.getNowPlaying().getId())) {
//                MiniPlayerRecyclerViewAdapter.prevPos = index;
//            }
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            PlayerActivity.listSongs.remove(pos);
            adapter.notifyItemRemoved(pos);
            if (pos < PlayerActivity.position) {
                PlayerActivity.position--;
            }
        }
    };

    private void setRecyclerView() {
//        if (listSongs == null || listSongs.size() == 0)
//            adapter = new MiniPlayerRecyclerViewAdapter(context, musicFiles, recyclerView);
//        else
        adapter = new MiniPlayerRecyclerViewAdapter(context, PlayerActivity.listSongs, recyclerView, bg);
        adapter.setHasStableIds(false);
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
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private void init() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MusicService.MUSIC_NOW_PLAYING, Context.MODE_PRIVATE);
        RelativeLayout relativeLayout = view.findViewById(R.id.mini_player_container);
        bottomSheetBehavior = BottomSheetBehavior.from(relativeLayout);
        peekHeight = bottomSheetBehavior.getPeekHeight();
        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        recyclerView = view.findViewById(R.id.miniPlayer_recyclerView);
        seekBar = view.findViewById(R.id.seekbar_miniPlayer);
        bar = view.findViewById(R.id.contentLoadingProgressBar);
        int max = sharedPreferences.getInt(MusicService.MUSIC_TOTAL_DURATION, 100);
        int progress = sharedPreferences.getInt(MusicService.MUSIC_CURRENT_DURATION, 0);
        bar.setMax(max);
        seekBar.setMax(max);
        bar.setProgress(progress);
        seekBar.setProgress(progress);
        time = view.findViewById(R.id.time_mini_player);
        time.setText(milliSecondsToTimer(sharedPreferences.getInt(MusicService.MUSIC_CURRENT_DURATION, 0)));
        scrollView = view.findViewById(R.id.scrollViewMiniPlayer);
        linearLayoutCompat = view.findViewById(R.id.finalView);


        song_name = view.findViewById(R.id.song_name_mini_player);
        song_name.setSelected(true);
        song_name.setText(sharedPreferences.getString(MusicService.MUSIC_TITLE, ""));
        bottomTxt = view.findViewById(R.id.txt_bottom_miniPlayer);
        artist = view.findViewById(R.id.artist_mini_player);
        if (sharedPreferences.getString(MusicService.MUSIC_ARTIST, "").equals("<unknown>")) {
            artist.setVisibility(View.GONE);
        } else {
            artist.setVisibility(View.VISIBLE);
            artist.setText(sharedPreferences.getString(MusicService.MUSIC_ARTIST, ""));
        }
        album_art = relativeLayout.findViewById(R.id.img_mini_player);
        centerAlbumArt = view.findViewById(R.id.albumArt_center);
        bg = view.findViewById(R.id.bg_mini_player_art);
        byte[] array = getAlbumArt(sharedPreferences.getString(MusicService.MUSIC_PATH, ""));
        if (array != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
            album_art.setVisibility(View.VISIBLE);
            album_art.setContentDescription("hideable");
            album_art.setImageBitmap(bitmap);
            bg.setImageBitmap(bitmap);
            centerAlbumArt.setImageBitmap(bitmap);
        } else {
            album_art.setVisibility(View.GONE);
            album_art.setContentDescription("");
            bg.setImageResource(R.color.black);
            centerAlbumArt.setImageResource(R.drawable.ic_music_note_full_freeicons);
        }

        miniPlayPause = relativeLayout.findViewById(R.id.play_pause_mini_player);
        bottomPlayPause = view.findViewById(R.id.play_pause_mini_player_bottom);
        nextBtn = view.findViewById(R.id.nxtBtnMiniPlayer);
        prevBtn = view.findViewById(R.id.prevBtnMiniPlayer);
        repeatBtn = view.findViewById(R.id.repeatBtnMiniPlayer);
        if (MainActivity.repeatBoolean) {
            repeatBtn.setColorFilter(ContextCompat.getColor(context, R.color.tab_highlight), android.graphics.PorterDuff.Mode.SRC_IN);
        } else
            repeatBtn.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);

        searchBtn = view.findViewById(R.id.search_miniPlayer);
        searchViewEditText = view.findViewById(R.id.editText);
        textInputLayout = view.findViewById(R.id.textInputLayout);
        peekRelLayout = view.findViewById(R.id.relative_layout_mini_player_visible);
        searchBtn.setOnClickListener(v -> {
            if (!tb_img.isChecked()) {
                tb_img.setChecked(true);
            }
            for (int i = 0; i < peekRelLayout.getChildCount(); i++) {
                View child = peekRelLayout.getChildAt(i);
                if (child.getContentDescription() != null && child.getContentDescription().equals("hideable")) {
                    child.setVisibility(View.INVISIBLE);
                }
            }
            searchViewEditText.setText("");
            textInputLayout.setVisibility(View.VISIBLE);
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchViewEditText, InputMethodManager.SHOW_IMPLICIT);
            searchViewEditText.requestFocus();
        });
        searchViewEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    for (int i = 0; i < peekRelLayout.getChildCount(); i++) {
                        View child = peekRelLayout.getChildAt(i);
                        if (child.getContentDescription() != null && child.getContentDescription().equals("hideable")) {
                            child.setVisibility(View.VISIBLE);
                        }
                    }
                    textInputLayout.setVisibility(View.GONE);
//                    adapter.update(listSongs);
                }
            }
        });
        searchViewEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    String userInput = s.toString().toUpperCase();
                    ArrayList<MusicFiles> myFiles = new ArrayList<>();
                    int pos = 0;
                    for (MusicFiles listSong : PlayerActivity.listSongs) {
                        if (listSong.getTitle().toUpperCase().contains(userInput)) {
//                            myFiles.add(listSong);
//                            recyclerView.smoothScrollToPosition(pos);
                            llm.smoothScrollToPosition(recyclerView, new RecyclerView.State(), pos + 10);
                            break;
                        }
                        pos++;
                    }
//                    adapter.updateList(myFiles);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        moreBtn = view.findViewById(R.id.more_mini_player);
        time = relativeLayout.findViewById(R.id.time_mini_player);
//        imageViewOnClick();
//        TypedValue tv = new TypedValue();
//        if (context.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
//            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
//        }
        tb_img = relativeLayout.findViewById(R.id.toggle_img);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
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

    public void hideWithAnim() {
        isShown = false;
        ViewPager viewPager = view.findViewById(R.id.view_pager_main);
        viewPager.setPadding(0, 0, 0, 0);
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
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
    public static String milliSecondsToTimer(long milliseconds) {
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

    public void updateNowPlaying(int pos) {
        if (adapter != null) {
            adapter.updateNowPlaying(pos);
        }
    }

    public int getAdapterSize() {
        if (adapter != null)
            return adapter.getSize();
        return -1;
    }
}

