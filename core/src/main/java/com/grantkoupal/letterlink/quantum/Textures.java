package com.grantkoupal.letterlink.quantum;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

public class Textures{
    private static Texture whitePixel = null;
    /**
     * Provides a 1x1 white pixel texture. Useful for drawing solid shapes or UI elements.
     * Remember to call Textures.dispose() during cleanup to free GPU memory.
     */
    public static Texture getWhitePixel(){
        if(whitePixel == null){
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            whitePixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return whitePixel;
    }

    /**
     * Disposes whitePixel to avoid memory leaks
     */
    public static void dispose(){
        if(whitePixel != null){
            whitePixel.dispose();
        }
    }
}
