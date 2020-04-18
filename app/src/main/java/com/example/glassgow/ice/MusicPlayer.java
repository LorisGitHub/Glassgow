package com.example.glassgow.ice;

import android.os.AsyncTask;
import android.util.Log;

import Ice.Communicator;
import Ice.ObjectPrx;
import Ice.Util;

public class MusicPlayer {

    private static  MusicPlayer instance = null;

    public MusicPlayer() {}

    public static MusicPlayer getInstance() {
        if(instance == null){
            instance = new MusicPlayer();
        }
        return instance;
    }

    public void play(String title, CallBackUrl callback){
        new ClientPlay(callback).execute(title);
    }

    private class ClientPlay extends AsyncTask<String, Void, String> {

        private String ip_streaming = "192.168.1.24";
        private String port = "9001";
        private CallBackUrl callBackUrl;

        public ClientPlay(CallBackUrl callBackUrl) {
            this.callBackUrl = callBackUrl;
        }

        @Override
        protected String doInBackground(String... strings) {
            try(Communicator communicator = Util.initialize()){
                ObjectPrx base = communicator.stringToProxy("SimpleMusicManager:default -h " + this.ip_streaming + " -p " + this.port);
                Player.VlcPlayerPrx player = Player.VlcPlayerPrxHelper.checkedCast(base);

                if(player == null){
                    throw new Error("Invalid proxy");
                }
                String urlToStream = player.play(strings[0]);
                this.callBackUrl.onServerResult(urlToStream);
            }
            return "Ice action completed";
        }
    }

}
