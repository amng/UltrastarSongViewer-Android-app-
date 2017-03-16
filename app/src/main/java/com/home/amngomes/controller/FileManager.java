package com.home.amngomes.controller;

import android.os.Environment;

import com.home.amngomes.controller.interfaces.FileManagerInterface;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;

public class FileManager implements FileManagerInterface{

    public final String MAINPATH = Environment.getExternalStorageDirectory() + "/" +"Ultrastar/";
    private static FileManager manager;

    public static FileManager getInstance(){
        if(manager == null)
            manager = new FileManager();
        return manager;
    }

    @Override
    public String[] listSongs(){
        return new File(MAINPATH).list();
    }

    @Override
    public HashMap<String, String> getDataFromFile(String dir) {
        HashMap<String, String> res = new HashMap<>();

        File directory = new File(MAINPATH+dir);
        String file = "";
        if(directory.isDirectory()){
            file = directory.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if(filename.endsWith(Constants.DATA_FILE_EXTENSION))
                        return true;
                    return false;
                }
            })[0];
        }else return res;


        try {
            FileReader freader = new FileReader(new File(MAINPATH+dir+"/"+file));
            BufferedReader reader = new BufferedReader(freader);
            String line = "";
            while((line = reader.readLine()) != null && line.contains("#")){
                if(line.contains(":")){
                    String[] splitter = line.split(":");
                    res.put(splitter[0], splitter[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
