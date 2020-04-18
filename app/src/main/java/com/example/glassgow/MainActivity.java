package com.example.glassgow;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.glassgow.Music.Music;
import com.example.glassgow.ice.CallBackUrl;
import com.example.glassgow.ice.MusicPlayer;

import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements CallBackUrl {

    private static MainActivity instance = null;

    private MediaPlayer vlc;
    private LibVLC libVLC;

    private Button playBtn;
    private Button pauseBtn;
    private Button stopBtn;
    private Button resetBtn;

    private ImageButton imageButton;

    private TextView textView;
    private TextView textOutput;

    private SeekBar seekBar;

    RequestQueue requestQueue;
    String URL = "http://192.168.1.10:3019/parse";

    private Music currentMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.requestQueue = Volley.newRequestQueue(getApplicationContext());

        playBtn = findViewById(R.id.playBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        stopBtn = findViewById(R.id.stopBtn);
        resetBtn = findViewById(R.id.resetBtn);

        imageButton = findViewById(R.id.imageButton);

        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView2);
        textOutput = findViewById(R.id.textView3);

        this.libVLC = new LibVLC(this);
        this.vlc = new MediaPlayer(libVLC);

        //final Media media = new Media(libVLC, Uri.parse(url));
        //this.vlc.setMedia(media);

       /* this.vlc.setEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                if(event.type == MediaPlayer.Event.TimeChanged){
                    try{
                        textView.setText((int) media.getDuration());
                    } catch (Exception e){
                        textView.setText(e.getMessage());
                    }

                }
            }
        });*/

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vlc.play();
                textView.setText("Playing...");
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vlc.pause();
                textView.setText("Paused");
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vlc.stop();
                textView.setText("Stopped");
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vlc.setTime(0);
                vlc.play();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });


    }


    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak something...");
        try {
            startActivityForResult(intent, 666);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Speech recognition is not supported in this device.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Callback for speech recognition activity
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 666: {
                if (resultCode == RESULT_OK && null != data) {
                    try{
                        ArrayList<String> result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        String text = result.get(0);

                        textOutput.setText(URL + "/" + text);

                        JsonObjectRequest objectRequest = new JsonObjectRequest(
                                Request.Method.GET,
                                this.URL + "/" + text,
                                null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            if(response != null )
                                            {
                                                if(response.getString("action").equals("Play")){
                                                    JSONObject jsonObject = (JSONObject) response.get("track");
                                                    currentMusic = new Music(jsonObject.getString("name"), jsonObject.getString("artist"), jsonObject.getString("duration"), jsonObject.getString("type"));
                                                    textView.setText("Playing...");
                                                    textOutput.setText(currentMusic.getName() + " / " + currentMusic.getArtist());
                                                    onServerResponse(currentMusic.getName());
                                                } else if(response.getString("action").equals("Pause")){
                                                    vlc.pause();
                                                    textView.setText("Paused");
                                                } else if(response.getString("action").equals("Stop")){
                                                    vlc.stop();
                                                    textView.setText("Stopped");
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        textView.setText(error.toString());
                                    }
                                }
                        );

                        this.requestQueue.add(objectRequest);
                    } catch (Exception e){
                       textView.setText(e.getMessage());
                    }

                }
                break;
            }
        }
    }

    @Override
    public void onServerResult(String urlToStream) {
        final Media media = new Media(libVLC, Uri.parse(urlToStream));
        this.vlc.setMedia(media);
        this.vlc.play();
    }

    private void onServerResponse(String trackName) {
        MusicPlayer.getInstance().play(trackName, this);
    }
}
