package com.grantkoupal.letterlink;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.grantkoupal.letterlink.backend.BackendHandler;
import com.grantkoupal.letterlink.quantum.core.Manager;
import com.grantkoupal.letterlink.quantum.font.FontProvider;

public class Source extends Manager {

    protected static FontProvider fontProvider;

    public Source(FontProvider fontProvider){
        Source.fontProvider = fontProvider;
    }

    @Override
    public void setUp() {

        loadAssets();

        RankHandler.loadTextures();

        BackendHandler.startUp(new BackendHandler.StartupCallback() {
            @Override
            public void onSuccess() {
                MainMenu mm = new MainMenu();
                loadNewPage(mm);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });

        //LoadingBoardPage loadingPage = new LoadingBoardPage();
        //loadNewPage(loadingPage);

        //Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
    }

    /**
     * Loads game textures and fonts.
     */
    private void loadAssets() {
        ThemeManager.setCurrentTheme("Cabin");
        DataManager.setIcon("Checkmarks.png");
        DataManager.setUserName("LordMinion777");
    }

    public static BitmapFont generateFont(String path, int size){
        return fontProvider.getFont(path, size);
    }
}
