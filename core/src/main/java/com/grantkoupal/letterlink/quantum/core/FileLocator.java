package com.grantkoupal.letterlink.quantum.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class FileLocator {
    private String pathSkip = "";

    public FileLocator(){}

    public FileLocator(String newPathSkip){
        pathSkip = newPathSkip + "/";
    }

    public void setPathSkip(String newPathSkip){
        pathSkip = newPathSkip + "/";
    }

    public FileHandle getAsset(String path){
        return Gdx.files.internal(pathSkip + path);
    }

    public FileHandle[] getPNGs(String... paths){
        FileHandle[] files = new FileHandle[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = getPNG(paths[i]);
        }
        return files;
    }

    public FileHandle getPNG(String path){
        return Gdx.files.internal(pathSkip + path + ".png");
    }

    public FileHandle getTXT(String path){
        return Gdx.files.internal(pathSkip + path + ".txt");
    }
}
