package com.sync.imusic;

import static com.sync.imusic.AddToPlaylistPopup.PLAYLIST_NAME;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.sync.imusic.fragment.MoreFragment;

import java.io.File;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ApplicationClass extends Application {
    private SectionedRecyclerViewAdapter sectionedRecyclerViewAdapter;
    private VideoAdapter videoAdapter;
    private static boolean successful = false;
    private static SameNameItemRecyclerAdapter sameNameItemRecyclerAdapter = null;
    private static int pos = -1;

    public static final String CHANNEL_ID_1 = "CHANNEL_1";
    public static final String CHANNEL_ID_2 = "CHANNEL_2";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREV = "PREVIOUS";
    public static final String ACTION_PLAY = "PLAY";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    public void setSameNameItemRecyclerAdapter_pos(SameNameItemRecyclerAdapter sameNameItemRecyclerAdapter, int pos) {
        ApplicationClass.sameNameItemRecyclerAdapter = sameNameItemRecyclerAdapter;
        ApplicationClass.pos = pos;
    }

    public void setSectionedRecyclerViewAdapter(SectionedRecyclerViewAdapter sectionedRecyclerViewAdapter) {
        this.sectionedRecyclerViewAdapter = sectionedRecyclerViewAdapter;
    }

    public SectionedRecyclerViewAdapter getSectionedRecyclerViewAdapter() {
        return sectionedRecyclerViewAdapter;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel notificationChannel1 = new NotificationChannel(CHANNEL_ID_1,
//                    "Channel(1)", NotificationManager.IMPORTANCE_LOW);
//            notificationChannel1.setDescription("channel 1 description");

            NotificationChannel notificationChannel2 = new NotificationChannel(CHANNEL_ID_2,
                    "Current Track", NotificationManager.IMPORTANCE_LOW);
            notificationChannel2.setDescription("channel 2 description");
            notificationChannel2.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel2.setShowBadge(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(notificationChannel1);
            notificationManager.createNotificationChannel(notificationChannel2);
        }
    }

    public void setVideoAdapter(VideoAdapter videoAdapter) {
        this.videoAdapter = videoAdapter;
    }

    public VideoAdapter getVideoAdapter() {
        return videoAdapter;
    }

    public static void setAsRingtone(Window window, Context context, MusicFiles musicFile) {

        if (!checkSystemWritePermission(context)) {
            Toast.makeText(context, "There was some error. Make sure you give permission !"
                    , Toast.LENGTH_SHORT).show();
        } else {
            Snackbar.make(window.getDecorView().findViewById(android.R.id.content), "Set '" + musicFile.getTitle() + "' as ringtone?", Snackbar.LENGTH_LONG)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            File ringFile = new File(musicFile.getPath());
                            if (ringFile.exists()) {
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.MediaColumns.DATA, ringFile.getAbsolutePath());
                                values.put(MediaStore.MediaColumns.TITLE, musicFile.getTitle());
                                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                                values.put(MediaStore.MediaColumns.SIZE, ringFile.length());
                                values.put(MediaStore.Audio.Media.DURATION, 230);
                                values.put(MediaStore.Audio.Media.ARTIST, musicFile.getArtist());
                                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                                values.put(MediaStore.Audio.Media.IS_ALARM, false);
                                values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                                Uri newUri = MediaStore.Audio.Media.getContentUriForPath(ringFile.getAbsolutePath());
                                Uri newUri2 = Uri.parse("content://media/external/audio/media/" + musicFile.getId());

                                try {
                                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri2);
                                    successful = true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    successful = false;
                                }
                            }
                            if (successful)
                                Toast.makeText(context, "The file " + musicFile.getTitle() + " has been set as the ringtone successfully"
                                        , Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(context, "There was some error. Make sure you give permission !"
                                        , Toast.LENGTH_SHORT).show();

                        }
                    })
                    .setActionTextColor(ContextCompat.getColor(context, R.color.tab_highlight))
                    .show();

        }
    }

    public static boolean checkSystemWritePermission(Context context) {
        boolean retVal = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(context);
            if (!retVal) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
                context.startActivity(intent);
            }
            retVal = Settings.System.canWrite(context);
        }
        return retVal;
    }

    public static void share(Context context, MusicFiles musicFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
        intent.setType("audio/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicFile.getPath()));
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
//        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
//        mContext.startActivity(intent);
        context.startActivity(Intent.createChooser(intent, "Share file to..."));
    }

    public static void share(Context context, VideoFiles videoFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
        intent.setType("audio/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoFile.getPath()));
        context.startActivity(Intent.createChooser(intent, "Share file to..."));
    }

    public static void delete(Context context, MusicFiles musicFile) {
        DataBaseHelperPlaylist dataBaseHelperPlaylist = new DataBaseHelperPlaylist(context, PLAYLIST_NAME, null, 1);
        dataBaseHelperPlaylist.deleteForId(musicFile.getId());

        DataBaseHelperHistory db = new DataBaseHelperHistory(context, "history.db", null, 1);
        db.setAdapter(MoreFragment.historyAdapter);
        db.setAdapter(HistoryDetailsActivity.historyDetailsActivityAdapter);
        db.delete_notify(musicFile.getId());

        if (sameNameItemRecyclerAdapter != null && pos != -1) {
            sameNameItemRecyclerAdapter.sameNamePlaylistFiles.get(pos).getMusicFiles().remove(musicFile);
            String playlistName = "";
            if (sameNameItemRecyclerAdapter.sameNamePlaylistFiles.get(pos).getMusicFiles().size() == 0) {
                playlistName = sameNameItemRecyclerAdapter.sameNamePlaylistFiles.get(pos).getPlayListName();
//                sameNameItemRecyclerAdapter.sameNamePlaylistFiles.remove(pos);
//                sameNameItemRecyclerAdapter.notifyItemRemoved(pos);
                sameNameItemRecyclerAdapter.delete(playlistName);
//                sameNameItemRecyclerAdapter.notifyItemRangeChanged();
            }
            else {
                sameNameItemRecyclerAdapter.notifyItemChanged(pos);
            }

            pos = -1;
            sameNameItemRecyclerAdapter = null;

            MusicAdapter.remove(musicFile);
        }
    }

    public static void delete(Context context, VideoFiles videoFile) {
        DataBaseHelperPlaylist dataBaseHelperPlaylist = new DataBaseHelperPlaylist(context, PLAYLIST_NAME, null, 1);
        dataBaseHelperPlaylist.deleteForId(videoFile.getId());

        DataBaseHelperHistory db = new DataBaseHelperHistory(context, "history.db", null, 1);
        db.setAdapter(MoreFragment.historyAdapter);
        db.setAdapter(HistoryDetailsActivity.historyDetailsActivityAdapter);
        db.delete_notify(videoFile.getId());
    }
}
