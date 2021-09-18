package com.example.imusic.fragment;

import static com.example.imusic.FileActivity.IS_FAV;
import static com.example.imusic.MainActivity.mExternalStorageAvailable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imusic.BrowseAdapter;
import com.example.imusic.DataBaseHelper;
import com.example.imusic.FileActivity;
import com.example.imusic.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BrowseFragment extends Fragment {
    private Context context;
    public static List<File> favorites;
    public static BrowseAdapter browseAdapter;
    private RecyclerView recyclerView;
    private File internalStorageFile;
    private File externalStorageFile;
    private boolean internalStorageFile_isFav = false;
    private boolean externalStorageFile_isFav = false;
    private SharedPreferences sharedPreferences;
    private DataBaseHelper mydb;

    public BrowseFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favorites = new ArrayList<>();
        sharedPreferences = context.getSharedPreferences(IS_FAV, Context.MODE_PRIVATE);
        mydb = new DataBaseHelper(context, "favourite.db", null, 1);

        if (getFavourites() != null) {
            favorites.addAll(getFavourites());
        }
//        String internalStorage = System.getenv("EXTERNAL_STORAGE");
//        String externalStorage = System.getenv("INTERNAL_STORAGE");
    }

    private ArrayList<File> getFavourites() {
        Cursor res = mydb.getAllData();
        if (res.getCount() == 0) {
            Log.d("mydb", "getFavoirutes returns null");
            return null;
        }
        ArrayList<File> temp = new ArrayList<>();
        while (res.moveToNext()) {
            File file = new File(res.getString(2));
            if (file.exists()) {
                temp.add(file);
                Log.d("mydb", "name " + file.getName());
            }
        }
        return temp;
    }

    @Override
    public void onPause() {
        super.onPause();
        mydb.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            SharedPreferences.Editor editor = getActivity().getSharedPreferences("back_pressed", Context.MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();
        }
    }


    public static BrowseFragment newInstance() {

        Bundle args = new Bundle();
        BrowseFragment fragment = new BrowseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        initInternalStorageViewStub(view, R.id.included);
        if (mExternalStorageAvailable)
            initExternalStorageViewStub(view, R.id.included2);

        recyclerView = view.findViewById(R.id.recyclerView_fav_fragment_browse);
        browseAdapter = new BrowseAdapter(context, favorites, recyclerView);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(browseAdapter);

        return view;
    }

    private void initExternalStorageViewStub(View view, int id) {
        FrameLayout frameLayout = view.findViewById(R.id.viewstub2_frame);
        frameLayout.setVisibility(View.VISIBLE);
//        ContentResolver resolver = context.getContentResolver().query()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES) {
//            if (new File("/storage/emulated/").exists()) {
//                externalStorageFile = new File("/storage/emulated/");
//            } else if (new File("/storage/extSdCard/").exists()) {
//                externalStorageFile = new File("/storage/extSdCard/");
//            } else if (new File("/storage/sdcard1/").exists()) {
//                externalStorageFile = new File("/storage/sdcard1/");
//            } else if (new File("/storage/usbcard1/").exists()) {
//                externalStorageFile = new File("/storage/usbcard1/");
//            } else if (new File("/storage/sdcard0/").exists()) {
//                externalStorageFile = new File("/storage/sdcard0/");
//            }
//            externalStorageFile = context.getExternalFilesDir("");
//        } else

//        Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
//        externalStorageFile = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD);
        File extdir = Environment.getExternalStorageDirectory();
        externalStorageFile = new File(extdir.getAbsolutePath());


        ViewStub stub = view.findViewById(id);
        stub.setLayoutResource(R.layout.fav_files_item);
        View viewStubView = stub.inflate();
        TextView internalStorage = viewStubView.findViewById(R.id.folder_name);
        internalStorage.setText("SD Card");
        internalStorage.setSelected(true);
        TextView no_files = viewStubView.findViewById(R.id.no_files);
        ImageView more = viewStubView.findViewById(R.id.more_fav_file_item);

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                externalStorageFile_isFav = mydb.isFavourite(externalStorageFile);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,
                        R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_browse_fragment,
                        viewStubView.findViewById(R.id.bottomSheetContainer_browse_fragment));
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
                TextView folderName = bottomSheetView.findViewById(R.id.folder_name_bottom_sheet);
                folderName.setText("SD Card");
                if (!externalStorageFile_isFav) {
                    TextView txt = bottomSheetView.findViewById(R.id.text_bottom_frag_browse_fragment);
                    txt.setText("Add to favourites");
                    ImageView img = bottomSheetDialog.findViewById(R.id.fav_marker_bottom_sheet_browse_fragment);
                    img.setImageResource(R.drawable.ic_baseline_star_rate);
                }
                bottomSheetView.findViewById(R.id.option1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        externalStorageFile_isFav = !externalStorageFile_isFav;
                        if (externalStorageFile_isFav) {
                            browseAdapter.updateFile(externalStorageFile);
                            mydb.insertData(externalStorageFile.getName(), externalStorageFile.getPath(), externalStorageFile.getAbsolutePath());
                        } else {
                            browseAdapter.removeFile(externalStorageFile);
                            mydb.deleteData(externalStorageFile.getAbsolutePath());
                        }
                        bottomSheetDialog.cancel();
                    }
                });
            }
        });

        String[] arr = externalStorageFile.list();
        if (arr == null || arr.length == 0)
            no_files.setText("Empty");
        else
            no_files.setText(String.valueOf(arr.length));

        viewStubView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FileActivity.class);
                List<File> temp = new ArrayList<>();
                temp.add(externalStorageFile);
                intent.putExtra("parent", (Serializable) temp);
                context.startActivity(intent);
            }
        });

    }

    private void initInternalStorageViewStub(View view, int id) {

        String internalStoragePath = System.getenv("EXTERNAL_STORAGE");
        if (internalStoragePath != null) {
            internalStorageFile = new File(internalStoragePath);
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            Uri returnUri = Uri.fromFile(internalStorageFile);
//
//            Cursor returnCursor = context.getContentResolver().query(returnUri, new String[]{
//                    OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
//            }, null, null, null);
//            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
//            returnCursor.moveToFirst();
//            String name = (returnCursor.getString(nameIndex));
//            String size = (Long.toString(returnCursor.getLong(sizeIndex)));
//
//            returnCursor.close();
//        }
        ViewStub stub = view.findViewById(id);
        stub.setLayoutResource(R.layout.fav_files_item);
        View viewStubView = stub.inflate();
        TextView internalStorage = viewStubView.findViewById(R.id.folder_name);
        internalStorage.setText("Internal memory");
        internalStorage.setSelected(true);
        TextView no_files = viewStubView.findViewById(R.id.no_files);
        ImageView more = viewStubView.findViewById(R.id.more_fav_file_item);

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                internalStorageFile_isFav = mydb.isFavourite(internalStorageFile);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,
                        R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_browse_fragment,
                        viewStubView.findViewById(R.id.bottomSheetContainer_browse_fragment));
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
                TextView folderName = bottomSheetView.findViewById(R.id.folder_name_bottom_sheet);
                folderName.setText("Internal Storage");
                if (!internalStorageFile_isFav) {
                    TextView txt = bottomSheetView.findViewById(R.id.text_bottom_frag_browse_fragment);
                    txt.setText("Add to favourites");
                    ImageView img = bottomSheetDialog.findViewById(R.id.fav_marker_bottom_sheet_browse_fragment);
                    img.setImageResource(R.drawable.ic_baseline_star_rate);
                }
                bottomSheetView.findViewById(R.id.option1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        internalStorageFile_isFav = !internalStorageFile_isFav;
                        if (internalStorageFile_isFav) {
                            browseAdapter.updateFile(internalStorageFile);
                            mydb.insertData(internalStorageFile.getName(), internalStorageFile.getPath(), internalStorageFile.getAbsolutePath());
                        } else {
                            browseAdapter.removeFile(internalStorageFile);
                            mydb.deleteData(internalStorageFile.getAbsolutePath());
                        }
                        bottomSheetDialog.cancel();
                    }
                });
            }
        });
        String[] arr = internalStorageFile.list();
        if (arr == null || arr.length == 0)
            no_files.setText("Empty");
        else
            no_files.setText(String.valueOf(arr.length));

        viewStubView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FileActivity.class);
                List<File> temp = new ArrayList<>();
                temp.add(internalStorageFile);
                intent.putExtra("parent", (Serializable) temp);
                context.startActivity(intent);
            }
        });

    }

    public void updateFile(File fileToUpdate) {
        for (File f : favorites) {
            if (f.equals(fileToUpdate)) {
                return;
            }
        }
        favorites.add(0, fileToUpdate);
        browseAdapter.updateFile(fileToUpdate);
    }

    public List<File> findFiles(File file) {
        List<File> temp;
        File[] files = file.listFiles();
        temp = Arrays.asList(files);
        return temp;
    }
}