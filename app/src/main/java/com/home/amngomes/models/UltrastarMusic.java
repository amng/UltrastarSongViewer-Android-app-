package com.home.amngomes.models;

import com.home.amngomes.controller.FileManager;

import java.io.File;
import java.util.HashMap;

public class UltrastarMusic {

    private String path = "";
    private HashMap<String, String> song_data = new HashMap<>();

    public void setData(String path){
        this.path = path;
        song_data = FileManager.getInstance().getDataFromFile(path);
    }

    public String getData(String key){
        return song_data.get(key);
    }

    public String getPath() {
        return path;
    }
}
