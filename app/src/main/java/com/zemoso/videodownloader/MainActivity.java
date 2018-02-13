package com.zemoso.videodownloader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressBar progressBar;
    private static final int EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private long downloadedSize = 0;
    private boolean downloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        Button download = findViewById(R.id.download);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "App needs to save data", Toast.LENGTH_SHORT).show();
            }
            askPermission();
        }
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadFile();
            }
        });
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    private void downloadFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String filePath = Environment.getExternalStorageDirectory()
                        + File.separator;
                OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build();
                Request downloadRequest = new Request.Builder().url("http://d25mb3l1y0y93p.cloudfront.net/test3944752f005c429a9a90000.mp4?Expires=1567804910&Signature=lPVomvEJveQFBYPMT93kn3h7xMZsIcMvwIzB~cyL0-QeYbsCfKBiEt2bmcrGaqvMvnoqf3GNGmhimC5r1a2FYTCQKXNg4WEYH~WGM~GpBKOHCZ7ORk13scQn3AzpFDXZBD~vE4GjZluCwPWbQ57DSkypgSiNLkESZcjBuMEzWftE4ADdsa9HwmjURIfZWtMOyEWV7tKdudQKNaQWeAs2kye3OKv6A0eFzP5c29s0~xGU5FheKoylbYxOgECos9jjiU6BgOt2~nq1UYRNuZuiCGvhxmrGX8rb8VkdAPsjDz5RDRvvWfSUV1~LgLm5T9wsegqwigqlU~xCpvOwYqOE3g__&Key-Pair-Id=APKAJTR6P6J64Z3PLQNA").get().build();
                try {
                    Response response = okHttpClient.newCall(downloadRequest).execute();
                    ResponseBody responseBody = response.body();
                    BufferedSource source = responseBody.source();
                    if (!response.isSuccessful()) {
                        Log.d(TAG, "unsuccessful");
                        return;
                    } else {
                        long length = responseBody.contentLength();
                        progressBar.setMax((int) length);
                        File file = new File(filePath + "test.mp4");
                        if (file.exists() && downloaded == true) {
                            file.delete();
                            file = new File(filePath + "test.mp4");
                            Log.d(TAG, "file deleted");
                            downloadedSize = 0;
                            downloaded = false;
                            progressBar.setProgress((int) downloadedSize);
                        }
                        if (downloadedSize != 0) {
                            source.skip(downloadedSize);
                            Log.d(TAG, "skipping source" + downloadedSize);
                        }
                        BufferedInputStream input = new BufferedInputStream(responseBody.byteStream());
                        OutputStream output;
                        if (downloadedSize != 0) {
                            output = new FileOutputStream(file, true);
                        } else {
                            output = new FileOutputStream(file, false);
                        }
                        byte[] data = new byte[1024];
                        int count = 0;
                        while ((count = input.read(data)) != -1) {
                            downloadedSize += count;
                            progressBar.setProgress((int) downloadedSize);
                            Log.d(TAG, "downloaded" + downloadedSize);
                            output.write(data, 0, count);
                        }
                        downloaded = true;
                        Log.d(TAG, "is downloaded" + downloaded);
                        output.flush();
                        output.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }
}
