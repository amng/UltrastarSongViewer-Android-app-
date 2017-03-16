package com.home.amngomes.controller;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private String serverIp = "http://192.168.1.70:8080";

    private UltrastarSongApi service;

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverIp)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(UltrastarSongApi.class);
    }

    public UltrastarSongApi getService(){
        return service;
    }

    public static RetrofitClient getInstance(){
        return instance == null ? instance = new RetrofitClient() : instance;
    }

    public String getImagePath(long songId){
        return serverIp + "/songs/"+songId+"/image";
    }

    public String getSongPath(long songId){
        return serverIp + "/songs/"+songId+"/song";
    }

    public void updateServerIp(String serverIp){
        this.serverIp = serverIp;
        instance = new RetrofitClient();
    }

}
