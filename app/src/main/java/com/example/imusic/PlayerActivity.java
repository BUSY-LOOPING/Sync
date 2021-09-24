package com.example.imusic;

import static com.example.imusic.AlbumDetails.albumDetailsAdapter;
import static com.example.imusic.AlbumDetailsAdapter.albumFiles;
import static com.example.imusic.MainActivity.miniPlayer;
import static com.example.imusic.MainActivity.repeatBoolean;
import static com.example.imusic.MainActivity.shuffleBoolean;
import static com.example.imusic.MusicAdapter.mFiles;
import static com.example.imusic.MusicPackage.SongsFragment.musicAdapter;
import static com.example.imusic.MusicService.MUSIC_NOW_PLAYING;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn, menuBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    public static int position = -1;
    static String prevPlayedId = "";
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    private Handler handler = new Handler();
    private Thread playThread;
    private Thread prevThread;
    static BarVisualizer barVisualizer;
    static int audioSessionId;
    static MusicService musicService;
    private boolean successful = false;
    private LocalBroadcastManager localBroadcastManager;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.playerAct.action.close")) {
                end();
            }
        }
    };

    public static final String SERVICE_SONGS = "listSongs_MusicService";
    public final String PLAYING_FROM = "FROM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.playerAct.action.close");
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);
        initView();
        getIntentMethod();


        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        PlayerActivity.this, R.style.BottomSheetDialogTheme
                );

                View bottomSheetView = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.layout_bottom_sheet_activity_player,
                                (LinearLayout) findViewById(R.id.bottomSheetContainer));

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();

                bottomSheetView.findViewById(R.id.addToPlaylistBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(PlayerActivity.this, "Add to Playlist Pressed", Toast.LENGTH_SHORT).show();
                    }
                });

                bottomSheetView.findViewById(R.id.setAsRingtoneBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setASRingtonePressed(listSongs, position);
                        bottomSheetDialog.cancel();
                    }
                });

                bottomSheetView.findViewById(R.id.equaliserBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.cancel();
                        equaliser();
                    }
                });

                bottomSheetView.findViewById(R.id.viewDetailsBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(PlayerActivity.this, InfoActivity.class);
                        intent.putExtra("musicFilePlayerAct", listSongs);
                        intent.putExtra("posPlayerAct", position);
                        startActivity(intent);
                        bottomSheetDialog.cancel();
                    }
                });

                bottomSheetView.findViewById(R.id.deleteMediaBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(PlayerActivity.this, "Delete Media Pressed", Toast.LENGTH_SHORT).show();
                    }
                });

                bottomSheetView.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Thread tempThread = new Thread() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.post(this); //if we dont write this statement the textview of currentDuration wont change
            }
        };
        tempThread.start();
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                } else {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_rounded);
                } else {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_rounded_selected);
                }
            }
        });
    }

    private void equaliser() {
        EqualiserBottomSheet equaliserBottomSheet = new EqualiserBottomSheet();
        equaliserBottomSheet.setAudioSessionId(musicService.getAudioSessionId());
        if (musicService != null && !musicService.is_mediaPlayerNull()) {
            equaliserBottomSheet.setMediaPlayer(musicService.getMediaPlayer());
        }
        equaliserBottomSheet.setShowsDialog(true);
        equaliserBottomSheet.show(getSupportFragmentManager(), equaliserBottomSheet.getTag());
    }


    private void setASRingtonePressed(ArrayList<MusicFiles> listSongs, int position) {
        if (!checkSystemWritePermission()) {
            Toast.makeText(PlayerActivity.this, "There was some error. Make sure you give permission !"
                    , Toast.LENGTH_SHORT).show();
        } else {
            Snackbar.make(getWindow().getDecorView().findViewById(R.id.mContainer), "Set '" + listSongs.get(position).getTitle() + "' as ringtone?", Snackbar.LENGTH_LONG)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            File ringFile = new File(listSongs.get(position).getPath());
                            if (ringFile.exists()) {
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.MediaColumns.DATA, ringFile.getAbsolutePath());
                                values.put(MediaStore.MediaColumns.TITLE, listSongs.get(position).getTitle());
                                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                                values.put(MediaStore.MediaColumns.SIZE, ringFile.length());
                                values.put(MediaStore.Audio.Media.DURATION, 230);
                                values.put(MediaStore.Audio.Media.ARTIST, listSongs.get(position).getArtist());
                                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                                values.put(MediaStore.Audio.Media.IS_ALARM, false);
                                values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                                Uri newUri = MediaStore.Audio.Media.getContentUriForPath(ringFile.getAbsolutePath());
                                Uri newUri2 = Uri.parse("content://media/external/audio/media/" + listSongs.get(position).getId());
//                if (newUri2 == null)
//                {
//                    newUri2 = Uri.parse("content://media/external/audio/media/" + listSongs.get(position).getId());
//                }
                                Log.d("newUri", newUri + "");
                                Log.d("newUri", newUri2 + "");

                                try {
                                    RingtoneManager.setActualDefaultRingtoneUri(PlayerActivity.this, RingtoneManager.TYPE_RINGTONE, newUri2);
                                    successful = true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    successful = false;
                                }
                            }
                            if (successful)
                                Toast.makeText(PlayerActivity.this, "The file " + listSongs.get(position).getTitle() + " has been set as the ringtone successfully"
                                        , Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(PlayerActivity.this, "There was some error. Make sure you give permission !"
                                        , Toast.LENGTH_SHORT).show();

                        }
                    })
                    .setActionTextColor(ContextCompat.getColor(PlayerActivity.this, R.color.tab_highlight))
                    .show();

        }
    }

    private boolean checkSystemWritePermission() {
        boolean retVal = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(this);
            if (!retVal) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivity(intent);
            }
            retVal = Settings.System.canWrite(this);
        }
        return retVal;
    }

    //these 2 methods will allow music to keep getting played even if orientation changes
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        int pos = savedInstanceState.getInt("possition");
//        musicService.seekTo(pos);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unbindService(this);
    }

    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {
//        MiniPlayer.setFlag(false);
        SharedPreferences preferences = getSharedPreferences(MUSIC_NOW_PLAYING, MODE_PRIVATE);
        if (preferences.getString(PLAYING_FROM, null).equals("mainActivity")) {
            if (position != -1)
                musicAdapter.notifyItemChanged(PlayerActivity.position);
        }
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandomNumber(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.post(this);
                }

            });
            musicService.start();
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
        } else {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandomNumber(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.post(this);
                }

            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
        }
        audioSessionId = musicService.getAudioSessionId();
        if (audioSessionId != -1) {
            barVisualizer.setAudioSessionId(audioSessionId);
        }
        prevPlayedId = listSongs.get(position).getId();
        changeMiniPlayerNowPlaying();
    }


    private void nextThreadBtn() {
        Thread nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked(false);
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked(boolean flag) {
//        MiniPlayer.setFlag(false);
        SharedPreferences preferences = getSharedPreferences(MUSIC_NOW_PLAYING, MODE_PRIVATE);
        String playing_from = preferences.getString(PLAYING_FROM, null);
        if (playing_from != null && playing_from.equals("mainActivity")) {
            if (position != -1)
                musicAdapter.notifyItemChanged(PlayerActivity.position);
        }
        if (playing_from != null && playing_from.equals("albumDetails")) {
            if (position != -1)
                albumDetailsAdapter.notifyItemChanged(PlayerActivity.position);
        }
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            barVisualizer.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandomNumber(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listSongs.size());
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.post(this);
                }
            });
            musicService.start();
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            if (audioSessionId != -1) {
                barVisualizer.setAudioSessionId(audioSessionId);
            }
//            if (musicService != null) {
//                audioSessionId = musicService.getAudioSessionId();
//                if (audioSessionId != -1) {
//                    barVisualizer.setAudioSessionId(audioSessionId);
//                }
//            }

        } else {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean) {
                position = getRandomNumber(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean) {
                position = ((position + 1) % listSongs.size());
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.post(this);
                }

            });
            musicService.OnCompleted();
//            if (!flag) {
//                musicService.showNotification(R.drawable.ic_play);
//                if (audioSessionId != -1) {
//                    barVisualizer.setAudioSessionId(audioSessionId);
//                }
//            }
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);
            if (!flag)
                musicService.showNotification(R.drawable.ic_pause);
        }
        audioSessionId = musicService.getAudioSessionId();
        if (audioSessionId != -1) {
            barVisualizer.setAudioSessionId(audioSessionId);
        }
        prevPlayedId = listSongs.get(position).getId();
        changeMiniPlayerNowPlaying();
    }

    private int getRandomNumber(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);  //it will generate random numbers btw 0 and size -1
    }


    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.pause();
            playPauseBtn.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.post(this);
                }

            });
        } else {
            musicService.start();
            musicService.showNotification(R.drawable.ic_pause);
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.post(this);
                }

            });

        }
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    private String formattedTime(int mCurrentPosition) {  //mCurrentPosition is in seconds
        String totalout = "";
        String totalnew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalnew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalnew;
        } else
            return totalout;
    }

    private void getIntentMethod() {
        Intent intent = getIntent();
        String sender = "";
        if (intent != null) {
            position = intent.getIntExtra("position", -1);
            SharedPreferences.Editor editor = getSharedPreferences(MUSIC_NOW_PLAYING, MODE_PRIVATE).edit();
            sender = intent.getStringExtra("sender");
            if (sender != null) {
                if (sender.equals("albumDetails")) {
                    listSongs = albumFiles;
                    editor.putString(PLAYING_FROM, "albumDetails");
                    editor.apply();
                }
                if (sender.equals("historyAdapter")) {
                    listSongs = (ArrayList<MusicFiles>) intent.getSerializableExtra("listSongs");
                    MusicService.flag = true;
                }
                if (sender.equals("mainActivity")) {
                    listSongs = mFiles;
                    editor.putString(PLAYING_FROM, "mainActivity");
                    editor.apply();
                }
                if (sender.equals("miniPlayerRecyclerViewAdapter")) {
                    listSongs = (ArrayList<MusicFiles>) intent.getSerializableExtra("listSongs");
                }
            }
        }
        if (listSongs == null || listSongs.get(position) == null || listSongs.get(position).getPath() == null || !new File(listSongs.get(position).getPath()).exists()) {
            end();
        }

        if (musicService != null && prevPlayedId.equals(listSongs.get(position).getId())) {
            initView();
            uri = Uri.parse(listSongs.get(position).getPath());
            metaData(uri);
            if (musicService.isPlaying()) {
                playPauseBtn.setImageResource(R.drawable.ic_pause);
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        handler.post(this);
                    }
                });
            } else {
                seekBar.setProgress(musicService.getCurrentPosition() / 1000);
                playPauseBtn.setImageResource(R.drawable.ic_play);
            }

        } else {

            if (listSongs != null) {
                playPauseBtn.setImageResource(R.drawable.ic_pause);
                uri = Uri.parse(listSongs.get(position).getPath());
            }

            Intent intentService = new Intent(this, MusicService.class);
            intentService.putExtra("servicePosition", position);
            intentService.putExtra(SERVICE_SONGS, (Serializable) listSongs);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intentService);
            } else {
                startService(intentService);
            }
        }
        prevPlayedId = listSongs.get(position).getId();
        if (repeatBoolean)
            repeatBtn.setImageResource(R.drawable.ic_repeat_rounded_selected);
        if (shuffleBoolean)
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
        updateMiniPlayerAdapter();
    }

    private void updateMiniPlayerAdapter() {
        if (miniPlayer != null) {
            String playingFrom = getSharedPreferences(MUSIC_NOW_PLAYING, MODE_PRIVATE).getString(PLAYING_FROM, "");
            miniPlayer.updateList(listSongs);
            miniPlayer.updateNowPlaying(listSongs.get(position));
        }
    }

    private void changeMiniPlayerNowPlaying() {
        if (miniPlayer != null) {
            miniPlayer.updateNowPlaying(listSongs.get(position));
        }

    }

    private void changeMiniPlayerList() {
        if (miniPlayer != null) {
            miniPlayer.updateList(listSongs);
        }
    }

    private void initView() {
        barVisualizer = findViewById(R.id.bar);
        menuBtn = findViewById(R.id.menu_btn);
        song_name = findViewById(R.id.song_name);
        song_name.setSelected(true);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.duratinPlayed);
        duration_total = findViewById(R.id.duratinTotal);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
    }


    private void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(listSongs.get(position).getPath());
        int mTotalDuration = musicService.getDuration() / 1000;
        duration_total.setText(formattedTime(mTotalDuration));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if (art != null) {
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this, cover_art, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null) {
                        ImageView gradient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});

                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, //orientation is from bottom to top
                                new int[]{swatch.getRgb(), swatch.getRgb()});  //start color , end color
                        mContainer.setBackground(gradientDrawableBg);

                        song_name.setTextColor(swatch.getTitleTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());
                        barVisualizer.setColor(swatch.getBodyTextColor());
                    } else {
                        ImageView gradient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                }
            });
        } else {
            bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.music_icon);
            ImageAnimation(this, cover_art, bitmap);
            ImageView gradient = findViewById(R.id.imageViewGradient);
            RelativeLayout mContainer = findViewById(R.id.mContainer);
            gradient.setBackgroundResource(R.drawable.gradient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);
            song_name.setTextColor(Color.WHITE);
            artist_name.setTextColor(Color.DKGRAY);
            barVisualizer.setColor(getResources().getColor(R.color.music_item_bg));
        }
    }

    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap) {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

//                animIn.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//
//                    }
//                });


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }

    @Override
    public void nxtSong_OnComplete() {

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
        musicService.setCallBack(this);
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        musicService.OnCompleted();
        musicService.showNotification(R.drawable.ic_pause);

        audioSessionId = musicService.getAudioSessionId();
        if (audioSessionId != -1 && barVisualizer != null) {
            try {
                barVisualizer.setAudioSessionId(audioSessionId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }


    @Override
    protected void onDestroy() {
        Log.d("mytag", "onDestroy Player activity called");
        if (barVisualizer != null) {
            barVisualizer.release();
        }
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    MusicService returnServiceInstance() {
        return musicService;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void end() {
        Toast.makeText(this, "The file cannot be played.", Toast.LENGTH_SHORT).show();
        finish();
    }
}