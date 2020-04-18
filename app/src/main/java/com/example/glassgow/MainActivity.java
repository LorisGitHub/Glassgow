package com.example.glassgow;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements CallBackUrl {

    private static MainActivity instance = null;

    private MediaPlayer vlc;
    private LibVLC libVLC;

    private Button playBtn;
    private Button pauseBtn;
    private Button stopBtn;
    private Button resetBtn;

    private ImageView imageView;
    private ImageButton imageButton;

    private CountDownTimer countDownTimer;

    private TextView textView;
    private TextView textOutput;

    private TextView timer;
    private SeekBar seekBar;

    SimpleDateFormat format = new SimpleDateFormat("mm:ss");

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

        imageView = findViewById(R.id.imageView);
        imageButton = findViewById(R.id.imageButton);

        timer = findViewById(R.id.textView5);
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
                countDownTimer.start();
                textView.setText("Playing...");
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vlc.pause();
                countDownTimer.cancel();
                textView.setText("Paused");
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vlc.stop();
                countDownTimer.cancel();
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

                                                    Date date = format.parse(currentMusic.getDuration());
                                                    int minutes = date.getMinutes();
                                                    int secondes = date.getSeconds();
                                                    int timeInMillisecondes = (minutes*60 + secondes)*1000;
                                                    textOutput.setText(currentMusic.getName().replace("_", " ") + " - " + currentMusic.getArtist() + " - " + currentMusic.getType() );

                                                    timer.setText(minutes + "m" + secondes + "s");
                                                    seekBar.setProgress(0);
                                                    seekBar.setMax((minutes*60 + secondes)*1000);

                                                    countDownTimer = new CountDownTimer(timeInMillisecondes, 1000) {
                                                        @Override
                                                        public void onTick(long millisUntilFinished) {
                                                            timer.setText(timeInMillisecondes - (int) millisUntilFinished + "/" + minutes + "m" + secondes + "s");
                                                            seekBar.setProgress( timeInMillisecondes - (int) millisUntilFinished);
                                                        }

                                                        @Override
                                                        public void onFinish() {
                                                            textView.setText("Finished");
                                                        }
                                                    };
                                                    countDownTimer.start();

                                                    String pureBase64 = jsonObject.getString("lob").substring(jsonObject.getString("lob").indexOf(",") + 1);
                                                    byte[] decodedString = android.util.Base64.decode(pureBase64, Base64.DEFAULT);
                                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                                    imageView.setImageBitmap(decodedByte);

                                                    onServerResponse(currentMusic.getName());
                                                } else if(response.getString("action").equals("Pause")){
                                                    vlc.pause();
                                                    textView.setText("Paused");
                                                } else if(response.getString("action").equals("Stop")){
                                                    vlc.stop();
                                                    textView.setText("Stopped");
                                                }
                                            }
                                        } catch (JSONException | ParseException e) {
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
