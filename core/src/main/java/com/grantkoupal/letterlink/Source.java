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

        loadAssets();

        LoadingBoardPage loadingPage = new LoadingBoardPage();

        loadNewPage(loadingPage);
    }

    /**
     * Loads game textures and fonts.
     */
    private void loadAssets() {
        DataManager.setTileTexture("Wood Piece.png");
        DataManager.setBoardTexture("Bronco.png");
        DataManager.setBackgroundTexture("Waves.png");
        DataManager.setFontName("Coiny");
        DataManager.setBottomTextTexture("Grey.png");
        DataManager.setIcon("Checkmarks.png");
        DataManager.setUserName("Veerdex");
    }

    public static BitmapFont generateFont(String path, int size){
        return fontProvider.getFont(path, size);
    }
}
