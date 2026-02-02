package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Texture;

public class DataManager {
    protected static Texture tileTexture;
    protected static Texture boardTexture;
    protected static Texture backgroundTexture;
    protected static String fontName;

    public static void setAssets(String tileName, String boardName, String backgroundName, String fontName){
        tileTexture = new Texture("Boggle Board/Pieces/" + tileName);
        boardTexture = new Texture("Boggle Board/Boards/" + boardName);
        backgroundTexture = new Texture("Backgrounds/" + backgroundName);
        DataManager.fontName = fontName;
    }

    public static void dispose(){
        tileTexture.dispose();
        boardTexture.dispose();
        backgroundTexture.dispose();
    }
}
