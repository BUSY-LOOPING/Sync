package com.sync.imusic;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapterBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    private Context context;
    private TextView name;
    private LinearLayout opt1, opt2, opt3, opt4, opt5, opt6, opt7, opt8, opt9, opt10, opt11;
    private ArrayList<VideoFiles> videoFiles = new ArrayList<>();
    int position = 0, playBackPos = 0;
    private Window rootWindow;
    private VideoAdapter videoAdapter;
    private DataBaseHelperLastPlayed dataBaseHelperLastPlayed;

    public void setVideoAdapter(VideoAdapter videoAdapter) {
        this.videoAdapter = videoAdapter;
    }

    public void setWindow(Window window) {
        this.rootWindow = window;
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
        View view = inflater.inflate(R.layout.video_adapter_bottom_sheet, container, false);
        Bundle bundle = this.getArguments();
        init(view, bundle);
        listeners();
        return view;
    }

    private void init(View view, Bundle arguments) {
        if (arguments != null) {
            videoFiles = (ArrayList<VideoFiles>) arguments.getSerializable("arraylist");
            position = arguments.getInt("position");
            playBackPos = arguments.getInt("playBackPos");
        }

        name = view.findViewById(R.id.name_bottom_sheet);
        opt1 = view.findViewById(R.id.option1);
        opt2 = view.findViewById(R.id.option2);
        opt3 = view.findViewById(R.id.option3);
        opt4 = view.findViewById(R.id.option4);
        opt5 = view.findViewById(R.id.option5);
        opt6 = view.findViewById(R.id.option6);
        opt7 = view.findViewById(R.id.option7);
        opt8 = view.findViewById(R.id.option8);
        opt9 = view.findViewById(R.id.option9);
        opt10 = view.findViewById(R.id.option10);
        opt11 = view.findViewById(R.id.option11);
        dataBaseHelperLastPlayed = new DataBaseHelperLastPlayed(context, "lastPlayed.db", null, 1);


        name.setText(videoFiles.get(position).getTitle());

    }

    private void listeners() {
        opt1.setOnClickListener(this);
        opt2.setOnClickListener(this);
        opt3.setOnClickListener(this);
        opt4.setOnClickListener(this);
        opt5.setOnClickListener(this);
        opt6.setOnClickListener(this);
        opt7.setOnClickListener(this);
        opt8.setOnClickListener(this);
        opt9.setOnClickListener(this);
        opt10.setOnClickListener(this);
        opt11.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        ArrayList<VideoFiles> temp = new ArrayList<>();
        switch (id) {
            case R.id.option1:
                intent = new Intent(context, VideoPlayerActivity.class);
                temp.add(videoFiles.get(position));
                intent.putExtra(VideoAdapter.VIDEO_FILES, temp);
                intent.putExtra(VideoAdapter.VIDEO_FILES_POS, 0);
                intent.putExtra(VideoAdapter.VIDEO_PLAYBACK_POS, playBackPos == -1 ? 0 : playBackPos);
                startActivity(intent);
                break;
            case R.id.option2:
                intent = new Intent(context, VideoPlayerActivity.class);
                temp.add(videoFiles.get(position));
                intent.putExtra(VideoAdapter.VIDEO_FILES, temp);
                intent.putExtra(VideoAdapter.VIDEO_FILES_POS, 0);
                startActivity(intent);
                break;
            case R.id.option3:
                intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra(VideoAdapter.VIDEO_FILES, videoFiles);
                intent.putExtra(VideoAdapter.VIDEO_FILES_POS, position);
                startActivity(intent);
                break;
            case R.id.option4:
                temp.add(videoFiles.get(position));
                VideoPlayerActivity.append(temp);
                break;
            case R.id.option5:
                intent = new Intent(context, InfoActivityVideo.class);
                intent.putExtra(InfoActivityVideo.VIDEO_FILE_INFO_ACT, videoFiles.get(position));
                startActivity(intent);
                break;
            case R.id.option6:
                temp.add(videoFiles.get(position));
                VideoPlayerActivity.insertNext(temp);
                break;
            case R.id.option7:
                AddToPlaylistPopup popup = new AddToPlaylistPopup();
                temp.add(videoFiles.get(position));
                popup.addVideoFiles(temp);
                popup.show(((MainActivity)context).getSupportFragmentManager(), popup.getTag());
                break;
            case R.id.option8:
                DeleteBottomSheet bottomSheet = new DeleteBottomSheet();
                bottomSheet.setDeleteListener(new DeleteBottomSheet.DeleteListener() {
                    @Override
                    public void deleted() {
                        if (videoAdapter != null) {
                            videoAdapter.getVideoFiles().remove(position);
                            videoAdapter.notifyItemRemoved(position);
                            videoAdapter.notifyItemRangeChanged(position, videoAdapter.getVideoFiles().size());
                        }
                    }
                });
                Bundle bundle = new Bundle();
                bundle.putSerializable("videoFile", videoFiles.get(position));
                bottomSheet.setArguments(bundle);
                bottomSheet.show(((MainActivity)context).getSupportFragmentManager(), bottomSheet.getTag());
                break;
            case R.id.option9:
                ApplicationClass.share(context, videoFiles.get(position));
                break;
            case R.id.option10:
                dataBaseHelperLastPlayed.deleteForId(videoFiles.get(position).getId());
                videoAdapter.notifyItemChanged(position);
                break;
            case R.id.option11:
                intent = new Intent(context, FileActivity.class);
                List<File> tempList = new ArrayList<>();
                tempList.add(new File(videoFiles.get(position).getPath()).getParentFile());
                intent.putExtra("parent", (Serializable) tempList);
                startActivity(intent);
                break;

        }
        dismiss();
    }
}
