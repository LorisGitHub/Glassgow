package com.example.glassgow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.glassgow.ui.login.LoginActivity;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.Media;


public class MainActivity extends AppCompatActivity {

    private static MainActivity instance = null;

    private MediaPlayer vlc;
    private LibVLC libVLC;
    private String url = "http://192.168.1.24:8080/0.mp3";
    private Button button;
    private Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button3);
        nextBtn = findViewById(R.id.button2);

        this.libVLC = new LibVLC(this);
        this.vlc = new MediaPlayer(libVLC);

        Media media = new Media(libVLC, Uri.parse(url));
        this.vlc.setMedia(media);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vlc.play();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
}
