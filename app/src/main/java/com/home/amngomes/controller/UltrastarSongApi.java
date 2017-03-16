package com.home.amngomes.controller;

import com.home.amngomes.models.Song;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UltrastarSongApi {

    @GET("songs/")
    Call<List<Song>> getSongs(@Header("Range") String range);

}
