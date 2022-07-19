package com.visioninsight.apklogtool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Document;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private Button btnLog, btnOpen;
    private TextView tvLogFile;
    private LogcatHelper helper;
    private boolean logging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLog = findViewById(R.id.btn_log);
        btnOpen = findViewById(R.id.btn_open);
        tvLogFile = findViewById(R.id.tv_log_file);
        helper = new LogcatHelper(this);
        helper.setLogcatListener(new LogcatHelper.LogcatListener() {
            @Override
            public void startLog() {
                btnLog.setText("stop log");
                btnOpen.setEnabled(false);
                logging = true;
                tvLogFile.setText("");
            }

            @Override
            public void stopLog(String logFile) {
                btnLog.setText("start log");
                if (!TextUtils.isEmpty(logFile)) {
                    btnOpen.setEnabled(true);
                    tvLogFile.setText(logFile);
                }
                logging = false;
            }
        });

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (logging) {
                    helper.stop();
                } else {
                    helper.start();
                }
            }
        });

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".FileProvider", new File(tvLogFile.getText().toString()));
                } else {
                    uri = Uri.fromFile(new File(tvLogFile.getText().toString()).getParentFile());
                }
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setType("*/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        } else {
            btnLog.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btnLog.setEnabled(true);
            } else {
                btnLog.setEnabled(false);
            }
        }
    }
}