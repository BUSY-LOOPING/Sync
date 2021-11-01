package com.sync.imusic;


import static com.sync.imusic.MusicPackage.SongsFragment.musicAdapter;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.sync.imusic.fragment.MoreFragment;

import java.io.File;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
    private IBinder mBinder = new MyBinder();
    private MediaPlayer mediaPlayer;
    private ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    public int position = -1;
    public ActionPlaying actionPlaying;
    private MiniPlayer miniPlayer;
    private byte[] album_art = null;


    public static final String MUSIC_NOW_PLAYING = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String MUSIC_ID = "ID";
    public static final String NOW_PLAYING = "FALSE";
    public static final String PLAYING_FROM = "FROM";
    public static final String MUSIC_PATH = "MUSIC_PATH";
    public static final String MUSIC_TITLE = "MUSIC_TITLE";
    public static final String MUSIC_ARTIST = "MUSIC_ARTIST";
    public static final String MUSIC_TOTAL_DURATION = "MUSIC_TOTAL_DURATION";
    public static final String MUSIC_CURRENT_DURATION = "MUSIC_CURRENT_DURATION";
    public static boolean playing;
    public static boolean initiated = false;
    public static boolean flag = false;

    private MusicFiles nowPlayingFile;

    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();
        editor = getSharedPreferences(MUSIC_NOW_PLAYING, MODE_PRIVATE).edit();
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int myPosition = -1;
        String actionName = null;
        if (intent != null) {
            myPosition = intent.getIntExtra("servicePosition", -1);
            actionName = intent.getStringExtra("ActionName");
        }
        if (myPosition != -1) {
            playMedia(myPosition);
        }
        if (actionName != null) {
            switch (actionName) {
                case "playPause":
                    if (actionPlaying != null) {
                        actionPlaying.playPauseBtnClicked();
                        if (isPlaying()) miniPlayer.anim_playToPause();
                        else miniPlayer.anim_PauseToPlay();
                        MiniPlayer.setFlag(false);
                        miniPlayer.showPlayer();
                    }
                    break;
                case "next":
                    nextBtnClicked();
                    break;
                case "previous":
                    if (actionPlaying != null) {
                        actionPlaying.prevBtnClicked();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    public void nextBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked(false);
        }
    }

    private void playMedia(int Startposition) {
        musicFiles = PlayerActivity.listSongs;
        position = Startposition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null) {
                createMediaPlayer(position);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    playing = true;
                    initiated = true;
                }
            }
        } else {
            createMediaPlayer(position);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                initiated = true;
                playing = true;
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class MyBinder extends Binder {
        MusicService getService() {
//            MiniPlayer.setFlag(false);
            return MusicService.this;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void start() {
        if (musicFiles.get(position) != null && new File(musicFiles.get(position).getPath()).exists()) {
            mediaPlayer.start();
            miniPlayer.anim_playToPause();
            MiniPlayer.setFlag(false);
            miniPlayer.showPlayer();
            editor.putString(NOW_PLAYING, "true");
            editor.apply();
            playing = true;
            initiated = true;
        }
    }

    boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    boolean is_mediaPlayerNull() {
        return mediaPlayer == null;
    }

    void stop() {
        mediaPlayer.stop();
        editor.putString(NOW_PLAYING, "false");
        editor.apply();
        playing = false;
        initiated = false;
    }

    void release() {
        mediaPlayer.release();
        mediaPlayer = null;
        initiated = false;
        playing = false;
    }

    int getDuration() {
        return mediaPlayer.getDuration();
    }


    void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    void createMediaPlayer(int positionInner) {
//        if (flag) {
        musicFiles = PlayerActivity.listSongs;
        flag = false;
//        }
        MiniPlayer.setFlag(false);
        SharedPreferences preferences = getSharedPreferences(MUSIC_NOW_PLAYING, MODE_PRIVATE);
        try {
            int prevPlayedTrack = position;
            position = positionInner;
            File file = new File(musicFiles.get(position).getPath());
            if (file.exists()) {
                Uri uri = Uri.fromFile(file);
                try {
                    mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
                } catch (Exception e) {
                    if (actionPlaying != null)
                        actionPlaying.nextBtnClicked(false);
                }
                album_art = getAlbumArt(musicFiles.get(position).getPath());
                nowPlayingFile = musicFiles.get(position);
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                boolean storeInHistoryAllowed = pref.getBoolean("history", true);
                if (storeInHistoryAllowed) {
                    DataBaseHelperHistory dataBaseHelperHistory = new DataBaseHelperHistory(this, "history.db", null, 1);
                    dataBaseHelperHistory.setAdapter(MoreFragment.historyAdapter);
                    dataBaseHelperHistory.setAdapter(HistoryDetailsActivity.historyDetailsActivityAdapter);
                    dataBaseHelperHistory.insertData(true, false, musicFiles.get(position).getPath(),
                            musicFiles.get(position).getTitle(),
                            musicFiles.get(position).getArtist(),
                            musicFiles.get(position).getAlbum(),
                            musicFiles.get(position).getDuration(),
                            musicFiles.get(position).getId(),
                            musicFiles.get(position).getSize(),
                            "",
                            "",
                            "",
                            album_art);
                    dataBaseHelperHistory.close();
                }
                DataBaseHelperLastPlayed dataBaseHelperLastPlayed = new DataBaseHelperLastPlayed(this, "lastPlayed.db", null, 1);
                dataBaseHelperLastPlayed.insertData(musicFiles.get(position).getId());
                dataBaseHelperLastPlayed.close();
                editor.putString(NOW_PLAYING, "true");
//                playing = false;
                initiated = true;
                editor.putString(MUSIC_FILE, uri.toString());
                editor.putString(MUSIC_ID, musicFiles.get(position).getId());
                editor.putString(MUSIC_PATH, musicFiles.get(position).getPath());
                editor.putString(MUSIC_ARTIST, musicFiles.get(position).getArtist());
                editor.putString(MUSIC_TITLE, musicFiles.get(position).getTitle());
                editor.putInt(MUSIC_TOTAL_DURATION, getDuration());
                editor.putInt(MUSIC_CURRENT_DURATION, getCurrentPosition());
                editor.apply();

                String now_playing = preferences.getString(MUSIC_ID, null);
                if (now_playing != null) {
                    if (musicAdapter != null)
                        musicAdapter.updateNowPlaying(now_playing, prevPlayedTrack);
                    if (AlbumDetails.albumDetailsAdapter != null)
                        AlbumDetails.albumDetailsAdapter.updateNowPlaying(now_playing);
                }
            } else if (actionPlaying != null) {
                actionPlaying.nextBtnClicked(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mediaPlayer = null;
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                    .getInstance(getApplicationContext());
            localBroadcastManager.sendBroadcast(new Intent("com.playerAct.action.close"));
        }
    }

    int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void pause() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//            stopForeground(STOP_FOREGROUND_DETACH);
//        else
//            stopForeground(false);
        mediaPlayer.pause();
        editor.putString("initialized", "true");
        editor.putString(NOW_PLAYING, "false");
        editor.apply();
        playing = false;
        if (miniPlayer != null) {
            miniPlayer.anim_PauseToPlay();
            MiniPlayer.setFlag(false);
            miniPlayer.showPlayer();
        }
        if (mediaPlayer.getDuration() != mediaPlayer.getCurrentPosition()) {
            DataBaseHelperLastPlayed dataBaseHelperLastPlayed = new DataBaseHelperLastPlayed(this, "lastPlayed.db", null, 1);
            dataBaseHelperLastPlayed.storePrevPlayedPos(musicFiles.get(position).getId(), mediaPlayer.getCurrentPosition());
            dataBaseHelperLastPlayed.close();
        }

    }

    public void OnCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (musicFiles.size() > 1 || (!is_mediaPlayerNull() && mediaPlayer.getDuration() > 10000)) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            if (pref.getBoolean("auto_skip", true)) {
                if (actionPlaying != null) {
                    actionPlaying.nextBtnClicked(true);
                    if (mediaPlayer != null) {
                        createMediaPlayer(position);
                        PlayerActivity.audioSessionId = getAudioSessionId();
                        if (PlayerActivity.audioSessionId != -1) {
                            PlayerActivity.barVisualizer.setAudioSessionId(PlayerActivity.audioSessionId);
                        }
                        mediaPlayer.start();
                        editor.putString(NOW_PLAYING, "true");
                        editor.apply();
                        playing = true;
                        initiated = true;
                        showNotification(R.drawable.ic_pause);
                        OnCompleted();
                    }
                }
            } else {
                showNotification(R.drawable.ic_play);
                mediaPlayer.start();
                playing = true;
                initiated = true;
                actionPlaying.playPauseBtnClicked();
//                miniPlayer.anim_playToPause();
            }
        } else {
            if (actionPlaying != null) {
                if (mediaPlayer != null) mediaPlayer.start();
                actionPlaying.playPauseBtnClicked();
                playing = true;
                initiated = true;
            }
        }
    }

    void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    void showNotification(int playPauseBtn) {
        boolean onGoing = false;
        if (mediaPlayer.isPlaying()) {
            playPauseBtn = R.drawable.ic_pause;
            onGoing = true;
        } else {
            playPauseBtn = R.drawable.ic_play;
            onGoing = false;
        }
        Intent intent = new Intent(this, MainActivity.class).putExtra("fromNotificationTap", "fromNotificationTap");
//        intent.setAction(Intent.ACTION_MAIN);
//        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ApplicationClass.ACTION_PLAY);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ApplicationClass.ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ApplicationClass.ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent_delete = new Intent(this, NotificationReceiver.class);
        intent.putExtra("com.example.imusic.notification_id", 1);
        PendingIntent pendingIntent_delete = PendingIntent.getBroadcast(this, 1, intent_delete, PendingIntent.FLAG_UPDATE_CURRENT);

//        if (album_art == null)
//        album_art = getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb = null;
        if (album_art != null) {
            thumb = BitmapFactory.decodeByteArray(album_art, 0, album_art.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.music_icon);
        }

        int timeOut = 15000;
        Notification notification = new NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID_2)
                .setContentIntent(contentIntent)
                .setDeleteIntent(pendingIntent_delete)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(nowPlayingFile.getTitle())
                .setContentText(nowPlayingFile.getArtist())
                .addAction(R.drawable.ic_skip_previous_rounded, "Previous", prevPendingIntent)
                .addAction(playPauseBtn, "Pause", pausePendingIntent)
                .addAction(R.drawable.ic_skip_next_rounded, "Next", nextPendingIntent)
                .setStyle(
                        new androidx.media.app.NotificationCompat.MediaStyle()
                )
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setChannelId(ApplicationClass.CHANNEL_ID_2)
//                .setShowWhen(isPlaying())
                .setSound(null)
                .setOngoing(onGoing)
                .setVibrate(null)
                .setTimeoutAfter(timeOut)
                .build();


//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.notify(0, notification);

        startForeground(1, notification); //ID MUST NOT BE 0....IT WONT SHOW NOTIFICATION OTHERWISE AND SERVICE WONT WORK IN BACKGROUND
        if (!mediaPlayer.isPlaying())
            demoteNotifyBar();
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

    MusicFiles nowPlayingFile() {
        return musicFiles.get(position);
    }

    MusicService returnService() {
        return MusicService.this;
    }

    public void demoteNotifyBar() {
        stopForeground(false);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
//            stopForeground(false);
//        else
//            stopForeground(false);
        SharedPreferences.Editor editor = getSharedPreferences("demoteNotifyBar", MODE_PRIVATE).edit();
        editor.putBoolean("demoted", true);
        editor.apply();
    }

    public MusicFiles nowPlaying() {
        return nowPlayingFile;
    }

    public void miniPlayerCallback(MiniPlayer miniPlayer) {
        this.miniPlayer = miniPlayer;
        miniPlayerPlayPauseClickListener();
    }

    public MiniPlayer getMiniPlayerInstance() {
        return miniPlayer;
    }

    private void miniPlayerPlayPauseClickListener() {
        miniPlayer.miniPlayPause.setOnClickListener(this);
        miniPlayer.bottomPlayPause.setOnClickListener(this);
        miniPlayer.nextBtn.setOnClickListener(this);
        miniPlayer.prevBtn.setOnClickListener(this);
        miniPlayer.repeatBtn.setOnClickListener(this);
        miniPlayer.moreBtn.setOnClickListener(this);
        miniPlayer.tb_img.setOnCheckedChangeListener(this);
        miniPlayer.seekBar.setOnSeekBarChangeListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        if (actionPlaying != null && miniPlayer != null) {
            int id = v.getId();
            switch (id) {
                case R.id.play_pause_mini_player:
                case R.id.play_pause_mini_player_bottom:
                    actionPlaying.playPauseBtnClicked();
                    if (isPlaying()) miniPlayer.anim_playToPause();
                    else miniPlayer.anim_PauseToPlay();
                    break;
                case R.id.nxtBtnMiniPlayer:
                    actionPlaying.nextBtnClicked(false);
                    break;
                case R.id.prevBtnMiniPlayer:
                    actionPlaying.prevBtnClicked();
                    break;
                case R.id.repeatBtnMiniPlayer:
                    MainActivity.repeatBoolean = !MainActivity.repeatBoolean;
                    if (MainActivity.repeatBoolean)
                        miniPlayer.repeatBtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.tab_highlight), android.graphics.PorterDuff.Mode.SRC_IN);
                    else
                        miniPlayer.repeatBtn.setColorFilter(null);
                    break;
                case R.id.more_mini_player:
                    break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (miniPlayer != null) {
            if (!isChecked) {
                miniPlayer.centerAlbumArt.animate().alpha(1.0f);
                miniPlayer.recyclerView.animate().alpha(0f);
                miniPlayer.recyclerView.setVisibility(View.INVISIBLE);
            } else {
                miniPlayer.centerAlbumArt.animate().alpha(0f);
                miniPlayer.recyclerView.animate().alpha(1.0f);
                miniPlayer.recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mediaPlayer != null && fromUser) {
            seekTo(progress * 1000);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void deleted(int position, String path) {
        if (mediaPlayer != null && miniPlayer != null) {
            pause();
            stop();
            release();
            Log.d("mylog", "here");
            miniPlayer.hidePlayer();
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
//            stopSelf();
            MusicService.initiated = false;
            MusicService.playing = false;
        }
//        if ((this.position + 1) >= musicFiles.size()) {
//            this.position = 0;
//        } else {
//            this.position++;
//        }
    }

    public void update(ArrayList<MusicFiles> newlist, int position) {
        musicFiles = newlist;
        this.position = position;
    }


}
