package com.example.glassgow;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

    private ImageButton playBtn;
    private ImageButton pauseBtn;
    // private ImageButton stopBtn;
    // private ImageButton resetBtn;
    private ImageButton prevBtn;
    private ImageButton nextBtn;

    private ImageView imageView;
    private ImageView prevImage;
    private ImageView nextImage;
    private ImageButton imageButton;

    private CountDownTimer countDownTimer;

    private TextView statusText;
    private TextView trackInfoText;

    private TextView timer;
    private SeekBar seekBar;

    SimpleDateFormat format = new SimpleDateFormat("mm:ss");

    RequestQueue requestQueue;
    String URL = "http://192.168.1.78:3019/parse";

    private Music currentMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.requestQueue = Volley.newRequestQueue(getApplicationContext());

        playBtn = findViewById(R.id.playBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        // stopBtn = findViewById(R.id.stopBtn);
        // resetBtn = findViewById(R.id.resetBtn);
        prevBtn = findViewById(R.id.prevButton);
        nextBtn = findViewById(R.id.nextButton);

        imageView = findViewById(R.id.imageView);
        prevImage = findViewById(R.id.prevImg);
        nextImage = findViewById(R.id.nextImg);
        imageButton = findViewById(R.id.imageButton);

        timer = findViewById(R.id.textView5);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setClickable(false);

        statusText = findViewById(R.id.textView2);
        trackInfoText = findViewById(R.id.textView3);

        this.libVLC = new LibVLC(this);
        this.vlc = new MediaPlayer(libVLC);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonHandler();
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseButtonHandler();
            }
        });

        /*stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopButtonHandler();
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtonHandler();
            }
        });*/

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevButtonHandler();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButtonHandler();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseButtonHandler();
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

                        JsonObjectRequest objectRequest = new JsonObjectRequest(
                                Request.Method.GET,
                                this.URL + "/" + text,
                                null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        onResponseFromParsingServer(response);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                }
                        );

                        this.requestQueue.add(objectRequest);
                    } catch (Exception e){
                       e.printStackTrace();
                    }

                }
                break;
            }
        }
    }

    public void initializeTimer(){
        countDownTimer = new CountDownTimer(currentMusic.getTimeInMillis(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentMusic.setMillisUntilFinished(currentMusic.getMillisUntilFinished() + 1000);
                int m = (currentMusic.getMillisUntilFinished()/1000)/60;
                int s = (currentMusic.getMillisUntilFinished()/1000)%60;
                timer.setText(m + ":" + s + "/" + currentMusic.getMinutes() + ":" + currentMusic.getSecondes());
                seekBar.setProgress( currentMusic.getMillisUntilFinished()/1000);
                if(m == currentMusic.getMinutes() && s == currentMusic.getSecondes()){
                    onFinish();
                }
            }

            @Override
            public void onFinish() {
                currentMusic = null;
                countDownTimer.cancel();
                statusText.setText("Finished");
            }
        };
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

    private void playButtonHandler(){
        if(currentMusic != null){
            vlc.play();
            countDownTimer.start();
            statusText.setText("Playing...");
        }
    }

    private void pauseButtonHandler(){
        if(currentMusic != null){
            vlc.pause();
            countDownTimer.cancel();
            statusText.setText("Paused");
        }
    }

    private void stopButtonHandler(){
        if(currentMusic != null){
            vlc.stop();
            countDownTimer.cancel();
            statusText.setText("Stopped");
        }
    }

    private void resetButtonHandler(){
        if(currentMusic != null){
            vlc.setTime(0);
            vlc.play();
        }
    }

    private void prevButtonHandler(){
        if(currentMusic != null && currentMusic.getPreviousMusic() != null){
            vlc.pause();
            countDownTimer.cancel();
            currentMusic = currentMusic.getPreviousMusic();
            setPrevAndNextImg();
            currentMusic.setMillisUntilFinished(0);
            initializeTextAndTimer();
            imageView.setImageBitmap(currentMusic.getLob());
            onServerResponse(currentMusic.getName());
        }
    }

    private void nextButtonHandler(){
        if(currentMusic != null && currentMusic.getNextMusic() != null){
            vlc.pause();
            countDownTimer.cancel();
            currentMusic = currentMusic.getNextMusic();
            setPrevAndNextImg();
            currentMusic.setMillisUntilFinished(0);
            initializeTextAndTimer();
            imageView.setImageBitmap(currentMusic.getLob());
            onServerResponse(currentMusic.getName());
        }
    }

    private void setPrevAndNextImg(){
        if(currentMusic.getPreviousMusic() != null){
            prevImage.setImageBitmap(currentMusic.getPreviousMusic().getLob());
        } else {
            prevImage.setImageBitmap(null);
        }
        if(currentMusic.getNextMusic() != null){
            nextImage.setImageBitmap(currentMusic.getNextMusic().getLob());
        } else {
            nextImage.setImageBitmap(null);
        }
    }

    private void onResponseFromParsingServer(JSONObject response){
        try {
            if(response != null )
            {
                if(response.getString("action").equals("Play")){
                    JSONObject jsonObject = (JSONObject) response.get("track");
                    if(currentMusic == null){
                        currentMusic = new Music(jsonObject.getString("name"), jsonObject.getString("artist"), jsonObject.getString("duration"), jsonObject.getString("type"));
                    } else {
                        Music newMusic = new Music(jsonObject.getString("name"), jsonObject.getString("artist"), jsonObject.getString("duration"), jsonObject.getString("type"));
                        currentMusic.setNextMusic(newMusic);
                        newMusic.setPreviousMusic(currentMusic);
                        currentMusic = newMusic;
                        prevImage.setImageBitmap(currentMusic.getPreviousMusic().getLob());
                    }

                    initializeTextAndTimer();

                    String pureBase64 = jsonObject.getString("lob").substring(jsonObject.getString("lob").indexOf(",") + 1);
                    byte[] decodedString = android.util.Base64.decode(pureBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    currentMusic.setLob(decodedByte);
                    imageView.setImageBitmap(decodedByte);

                    onServerResponse(currentMusic.getName());
                } else if(response.getString("action").equals("Pause")){
                    pauseButtonHandler();
                } else if(response.getString("action").equals("Stop")){
                    stopButtonHandler();
                } else if(response.getString("action").equals("Prev")){
                    prevButtonHandler();
                } else if(response.getString("action").equals("Next")){
                    nextButtonHandler();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initializeTextAndTimer(){
        try{
            statusText.setText("Playing...");

            Date date = format.parse(currentMusic.getDuration());
            currentMusic.setMinutes(date.getMinutes());
            currentMusic.setSecondes(date.getSeconds());
            currentMusic.setTimeInMillis((currentMusic.getMinutes()*60 + currentMusic.getSecondes())*1000);
            trackInfoText.setText(currentMusic.getName().replace("_", " ") + " - " + currentMusic.getArtist() + " - " + currentMusic.getType() );

            timer.setText(currentMusic.getTimeInMillis() + " 0:00 / " + currentMusic.getMinutes() + ":" + currentMusic.getSecondes());
            seekBar.setProgress(0);
            seekBar.setMax(currentMusic.getTimeInMillis()/1000);

            initializeTimer();
            countDownTimer.start();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
