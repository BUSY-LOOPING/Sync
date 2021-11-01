package com.sync.imusic;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.MyBrowseViewHolder> {
    private Context mContext;
    private List<File> favorites;
    private DataBaseHelper mydb;
    private RecyclerView recyclerView;
    private String rootPath;

    public BrowseAdapter() {
    }

    public BrowseAdapter(Context mContext, List<File> favorites, RecyclerView recyclerView) {
        this.mContext = mContext;
        this.favorites = favorites;
        this.recyclerView = recyclerView;
        rootPath = Environment.getExternalStorageDirectory().getPath();
    }

    @NonNull
    @Override
    public MyBrowseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.fav_files_item, parent, false);
        return new MyBrowseViewHolder(view);
    }

//    @Override
//    public int getItemViewType(int position) {
//        return position;
//    }

    @Override
    public void onBindViewHolder(@NonNull MyBrowseViewHolder holder, int position) {
        mydb = new DataBaseHelper(mContext, "favourite.db", null, 1);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FileActivity.class);
                mContext.startActivity(intent);
            }
        });
        if (favorites.get(position).getPath().equals(rootPath)) {
            holder.folder_name.setText("Internal memory");
        } else
            holder.folder_name.setText(favorites.get(position).getName());
        String[] array = favorites.get(position).list();
        if (array != null) {
            if (array.length == 0)
                holder.no_files.setText("Empty");
            else
                holder.no_files.setText(String.valueOf(array.length));
        }else holder.no_files.setText("Empty");
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext,
                        R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(mContext).inflate(R.layout.layout_bottom_sheet_browse_fragment,
                        holder.itemView.findViewById(R.id.bottomSheetContainer_browse_fragment));
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();

                TextView txt = bottomSheetView.findViewById(R.id.folder_name_bottom_sheet);
                txt.setText(favorites.get(position).getPath().equals(rootPath) ? "Internal memory" : favorites.get(position).getName());
                bottomSheetView.findViewById(R.id.option1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mydb.deleteData(favorites.get(position).getAbsolutePath());
                        favorites.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, favorites.size());
                        bottomSheetDialog.cancel();
                    }
                });
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favorites.get(position).exists()) {
                    Intent intent = new Intent(mContext, FileActivity.class);
                    intent.putExtra("item_selected", favorites.get(position).getName());
                    List<File> tempList = new ArrayList<>();
                    tempList.add(favorites.get(position));
                    intent.putExtra("parent", (Serializable) tempList);
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Cannot open the file location " + favorites.get(position).getPath() + " .It no longer exists.", Toast.LENGTH_SHORT).show();
                    mydb.deleteData(favorites.get(position).getAbsolutePath());

                }
            }
        });
        mydb.close();
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    public void updateFile(File fileToAdd) {
        favorites.add(0, fileToAdd);
        notifyItemInserted(0);
        recyclerView.smoothScrollToPosition(0);
    }

    public void removeFile(File fileToRemove) {
        for (int i = 0; i < favorites.size(); i++) {
            if (favorites.get(i).equals(fileToRemove)) {
                favorites.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, favorites.size());
                break;
            }
        }
    }

    public static class MyBrowseViewHolder extends RecyclerView.ViewHolder {
        ImageView more;
        TextView folder_name, no_files;

        public MyBrowseViewHolder(@NonNull View itemView) {
            super(itemView);
            more = itemView.findViewById(R.id.more_fav_file_item);
            folder_name = itemView.findViewById(R.id.folder_name);
            folder_name.setSelected(true);
            no_files = itemView.findViewById(R.id.no_files);
        }
    }
}
