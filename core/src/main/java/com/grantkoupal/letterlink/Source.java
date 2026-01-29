package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.grantkoupal.letterlink.quantum.FontProvider;
import com.grantkoupal.letterlink.quantum.Manager;

public class Source extends Manager {

    protected static FontProvider fontProvider;

    public Source(FontProvider fontProvider){
        this.fontProvider = fontProvider;
    }

    @Override
    public void setUp() {

        PracticePage startPage = new PracticePage();

        loadNewPage(startPage);
    }

    public static BitmapFont generateFont(String path, int size){
        return fontProvider.getFont(path, size);
    }
}
