package com.home.amngomes.controller.interfaces;

import java.util.HashMap;

public interface FileManagerInterface {

    String[] listSongs();
    HashMap<String, String> getDataFromFile(String dir);
}
