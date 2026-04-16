package com.grantkoupal.letterlink.quantum.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class FileLocator {
    private String pathSkip = "";

    public void setPathSkip(String newPathSkip){
        pathSkip = newPathSkip;
    }

    public FileHandle getAsset(String path){
        return Gdx.files.internal(pathSkip + path);
    }

    public FileHandle getPNG(String path){
        return Gdx.files.internal(pathSkip + path + ".png");
    }

    public FileHandle getTXT(String path){
        return Gdx.files.internal(pathSkip + path + ".txt");
    }
}
