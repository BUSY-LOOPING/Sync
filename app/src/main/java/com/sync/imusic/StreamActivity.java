package com.sync.imusic;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamActivity extends AppCompatActivity {
    private EditText editText;
    private ImageView pasteBtn, searchBtn;
    private String url;
    private RecyclerView recyclerView;
    public static StreamActivityAdapter streamActivityAdapter;
    public static ArrayList<String> list;
    private ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            DataBaseStream db = new DataBaseStream(StreamActivity.this, DataBaseHelperPlaylistNames.STREAM_DB_NAME, null, 1);
            db.delete(list.get(pos));
            list.remove(pos);
            streamActivityAdapter.notifyItemRemoved(pos);
            streamActivityAdapter.notifyItemRangeChanged(pos, list.size());
            db.close();
        }
    };

    public StreamActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        init();
        setRecyclerView();
    }

    private void setRecyclerView() {
        list = getList();
        streamActivityAdapter = new StreamActivityAdapter(this, list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(streamActivityAdapter);
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private ArrayList<String> getList() {
        ArrayList<String> temp = new ArrayList<>();
        DataBaseStream db = new DataBaseStream(this, DataBaseHelperPlaylistNames.STREAM_DB_NAME, null, 1);
        Cursor cursor = db.getAllData();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                temp.add(cursor.getString(0));
            }
            cursor.close();
        }
        db.close();
        return temp;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && "text/plain".equals(type)) {
            editText.setText(intent.getStringExtra("android.intent.extra.TEXT"));
            url = editText.getText().toString();
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if ("android.intent.action.SEND".equals(action) && "text/plain".equals(type)) {
            editText.setText(intent.getStringExtra("android.intent.extra.TEXT"));
            url = editText.getText().toString();
        }
        super.onResume();
    }

    private void init(){
        Toolbar toolbar = findViewById(R.id.toolbar_activity_stream);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        editText = findViewById(R.id.editText_activity_stream);
        pasteBtn = findViewById(R.id.pasteBtn_activity_stream);
        searchBtn = findViewById(R.id.search_btn_activity_stream);
        recyclerView = findViewById(R.id.activity_stream_adapter);
        pasteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager manager = (ClipboardManager) StreamActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData pasteData = manager.getPrimaryClip();
                if (pasteData == null) {
                    Toast.makeText(StreamActivity.this, "Nothing to paste", Toast.LENGTH_SHORT).show();
                } else {
                    ClipData.Item item = pasteData.getItemAt(0);
                    editText.setText(item.getText().toString());
                }
            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText.getText().toString().equals("")) {
                    url = editText.getText().toString();
                    Intent intent = new Intent(StreamActivity.this, StreamPlayer.class);
                    intent.putExtra("url_id", extractYTId(url));
                    intent.putExtra("original_url", url);
                    startActivity(intent);
                }
            }
        });
    }

    public static String extractYTId(String ytUrl) {
        String vId = null;
        Pattern pattern = Pattern.compile(
                "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(ytUrl);
        if (matcher.matches()){
            vId = matcher.group(1);
        }
        return vId;
    }
}