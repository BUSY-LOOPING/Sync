package com.sync.imusic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeleteBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    private static final int REQUEST_PERM_DELETE = 10;
    private Context context;
    private File file;
    private ImageView img;
    private TextView txt;
    private Button deleteBtn, cancelBtn;
    private ActivityFileAdapter activityFileAdapter;
    private int pos = 0;
    private List<File> list;
    private boolean isFile = false, isMusicFile = false, isVideoFile = false;
    private MusicFiles musicFile;
    private VideoFiles videoFile;
    private DeleteListener deleteListener;

    public interface DeleteListener {
        void deleted();
    }

    public void setDeleteListener(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setList(List<File> list) {
        this.list = list;
    }

    private AnimatedVectorDrawableCompat animatedVectorDrawableCompat;
    private AnimatedVectorDrawable animatedVectorDrawable;


    public void setActivityFileAdapter(ActivityFileAdapter activityFileAdapter) {
        this.activityFileAdapter = activityFileAdapter;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.delete_bottom_sheet, container, false);
        Bundle bundle = this.getArguments();
        init(view, bundle);
        listeners();
        return view;
    }

    private void init(View view, Bundle arguments) {
        if (arguments != null) {
            file = (File) arguments.getSerializable("file");
            if (file != null)
                isFile = true;
            else {
                musicFile = (MusicFiles) arguments.getSerializable("musicFile");
                if (musicFile != null)
                    isMusicFile = true;
                else {
                    videoFile = (VideoFiles) arguments.getSerializable("videoFile");
                    if (videoFile != null)
                        isVideoFile = true;
                }


            }
        }

        img = view.findViewById(R.id.delete_icon);
        txt = view.findViewById(R.id.title);
        cancelBtn = view.findViewById(R.id.cancel_button);
        deleteBtn = view.findViewById(R.id.btn_delete);
        img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_delete));
        Drawable drawable = img.getDrawable();
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            animatedVectorDrawableCompat = (AnimatedVectorDrawableCompat) drawable;
            animatedVectorDrawableCompat.start();
            animatedVectorDrawableCompat.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    animatedVectorDrawableCompat.start();
                }
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (drawable instanceof AnimatedVectorDrawable) {
                animatedVectorDrawable = (AnimatedVectorDrawable) drawable;
                animatedVectorDrawable.start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    animatedVectorDrawable.registerAnimationCallback(new Animatable2.AnimationCallback() {
                        @Override
                        public void onAnimationEnd(Drawable drawable) {
                            animatedVectorDrawable.start();
                        }
                    });
                }
            }
        }

        if (isFile) {
            if (file.isDirectory()) {
                txt.setText("Delete the folder '" + file.getName() + "' and all its contents?");
            } else {
                txt.setText("Delete the file '" + file.getName() + "'?");
            }
        } else if (isMusicFile) {
            txt.setText("Delete the file '" + musicFile.getTitle() + "'?");
        } else if (isVideoFile) {
            txt.setText("Delete the file '" + videoFile.getTitle() + "'?");
        }

    }

    private void listeners() {
        cancelBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cancel_button:
                break;
            case R.id.btn_delete:

                if (isFile) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                        boolean deleted = deleteDirectory(file);
                        if (deleted) {
                            if (deleteListener != null)
                                deleteListener.deleted();
                            Toast.makeText(context, "Deleted the file", Toast.LENGTH_SHORT).show();
                            if (activityFileAdapter != null) {
                                list.remove(pos);
                                activityFileAdapter.notifyItemRemoved(pos);
                                activityFileAdapter.notifyItemRangeChanged(pos, list.size());
                            }
                        } else
                            Toast.makeText(context, "Something went wrong! File not deleted.", Toast.LENGTH_SHORT).show();
                    } else {
                        int n = context.getContentResolver().delete(Uri.fromFile(file), null, null);
                        context.getApplicationContext().deleteFile(file.getName());
                        if (n > 0) {
                            if (deleteListener != null)
                                deleteListener.deleted();
                            Toast.makeText(context, "Deleted the file", Toast.LENGTH_SHORT).show();
                            if (activityFileAdapter != null) {
                                list.remove(pos);
                                activityFileAdapter.notifyItemRemoved(pos);
                                activityFileAdapter.notifyItemRangeChanged(pos, list.size());
                            }
                        } else {
                            Toast.makeText(context, "Something went wrong! File not deleted.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                        File newFile;
                        if (isMusicFile) {
                            newFile = new File(musicFile.getPath());
                        } else {
                            newFile = new File(videoFile.getPath());
                        }
                        boolean deleted = deleteDirectory(newFile);
                        if (deleted) {
                            if (deleteListener != null)
                                deleteListener.deleted();
                            if (isMusicFile)
                                ApplicationClass.delete(context, musicFile);
                            else
                                ApplicationClass.delete(context, videoFile);
                            Toast.makeText(context, "Deleted the file", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(context, "Something went wrong! File not deleted.", Toast.LENGTH_SHORT).show();
                    } else {
//                        int n = context.getContentResolver().delete(Uri.fromFile(file), null, null);
//                        context.getApplicationContext().deleteFile(file.getName());
//                        if (n > 0) {
//                            if (deleteListener != null)
//                                deleteListener.deleted();
//                            Toast.makeText(context, "Deleted the file", Toast.LENGTH_SHORT).show();
//                            if (activityFileAdapter != null) {
//                                list.remove(pos);
//                                activityFileAdapter.notifyItemRemoved(pos);
//                                activityFileAdapter.notifyItemRangeChanged(pos, list.size());
//                            }
//                        } else {
//                            Toast.makeText(context, "Something went wrong! File not deleted.", Toast.LENGTH_SHORT).show();
//                        }
                        List<Uri> list = new ArrayList<>();
                        list.add(Uri.fromFile(file));
                        requestDeletePermission(list);
                    }
                }

        }
        dismiss();
    }

    public static boolean deleteDirectory(File path) {
// TODO Auto-generated method stub
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (path.delete());
    }

    private void requestDeletePermission(List<Uri> uriList) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            PendingIntent pi = MediaStore.createDeleteRequest(context.getContentResolver(), uriList);
            try {
                ((Activity)context).startIntentSenderForResult(pi.getIntentSender(), REQUEST_PERM_DELETE, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
            }
        }
    }

//    public static Uri getUriFromDisplayName(Context context, String displayName) {
//
//        String[] projection;
//        projection = new String[]{MediaStore.Files.FileColumns._ID};
//
//        // TODO This will break if we have no matching item in the MediaStore.
//        Cursor cursor = context.getContentResolver().query(extUri, projection,
//                MediaStore.Files.FileColumns.DISPLAY_NAME + " LIKE ?", new String[]{displayName}, null);
//        assert cursor != null;
//        cursor.moveToFirst();
//
//        if (cursor.getCount() > 0) {
//            int columnIndex = cursor.getColumnIndex(projection[0]);
//            long fileId = cursor.getLong(columnIndex);
//
//            cursor.close();
//            return Uri.parse(extUri.toString() + "/" + fileId);
//        } else {
//            return null;
//        }
//
//    }
//
//    public static boolean deleteFileUsingDisplayName(Context context, String displayName) {
//
//        Uri uri = getUriFromDisplayName(context, displayName);
//        if (uri != null) {
//            final ContentResolver resolver = context.getContentResolver();
//            String[] selectionArgsPdf = new String[]{displayName};
//
//            try {
//                resolver.delete(uri, MediaStore.Files.FileColumns.DISPLAY_NAME + "=?", selectionArgsPdf);
//                return true;
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                // show some alert message
//            }
//        }
//        return false;
//
//    }
}
