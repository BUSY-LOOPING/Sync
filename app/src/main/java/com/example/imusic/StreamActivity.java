package com.example.imusic;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamActivity extends AppCompatActivity {
    private EditText editText;
    private ImageView pasteBtn, searchBtn;
    private String url;

    public StreamActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        init();
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
                if (editText.getText() != null) {
                    url = editText.getText().toString();
                    Intent intent = new Intent(StreamActivity.this, StreamPlayer.class);
                    intent.putExtra("url_id", extractYTId(url));
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