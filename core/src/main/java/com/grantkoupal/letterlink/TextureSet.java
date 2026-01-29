package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.Texture;

public class TextureSet {
    protected Texture tileTexture;
    protected Texture boardTexture;
    protected Texture backgroundTexture;

    public TextureSet(String tileName, String boardName, String backgroundName){
        tileTexture = new Texture("Boggle Board/Pieces/" + tileName);
        boardTexture = new Texture("Boggle Board/Boards/" + boardName);
        backgroundTexture = new Texture("Backgrounds/" + backgroundName);
    }

    public void dispose(){
        tileTexture.dispose();
        boardTexture.dispose();
        backgroundTexture.dispose();
    }
}
