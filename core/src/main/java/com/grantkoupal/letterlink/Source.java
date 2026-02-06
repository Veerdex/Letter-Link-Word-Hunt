package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.grantkoupal.letterlink.quantum.font.FontProvider;
import com.grantkoupal.letterlink.quantum.core.Manager;

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
        ThemeManager.setCurrentTheme("Cabin");
        DataManager.setIcon("Checkmarks.png");
        DataManager.setUserName("Veerdex");
    }

    public static BitmapFont generateFont(String path, int size){
        return fontProvider.getFont(path, size);
    }
}
